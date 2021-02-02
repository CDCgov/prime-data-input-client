locals {
  static_backend_pool          = "${var.name}-${var.env}-fe-static"
  static_backend_http_setting  = "${var.name}-${var.env}-fe-static-http"
  static_backend_https_setting = "${var.name}-${var.env}-fe-static-https"
  api_backend_pool             = "${var.name}-${var.env}-be-api"
  api_backend_http_setting     = "${var.name}-${var.env}-be-api-http"
  api_backend_https_setting    = "${var.name}-${var.env}-be-api-https"
  http_listener                = "${var.name}-http"
  https_listener               = "${var.name}-https"
  frontend_config              = "${var.name}-config"
  redirect_rule                = "${var.name}-redirect"
  is_prod                      = var.env == "prod"
}

resource "azurerm_public_ip" "static_gateway" {
  name                = "${var.name}-${var.env}-gateway"
  resource_group_name = var.resource_group_name
  location            = var.resource_group_location
  allocation_method   = "Static"
  sku                 = "Standard"

  tags = var.tags
}

resource "azurerm_application_gateway" "load_balancer" {
  name                = "${var.name}-app-gateway"
  resource_group_name = var.resource_group_name
  location            = var.resource_group_location

  sku {
    name = var.sku_name
    tier = var.sku_tier
  }

  gateway_ip_configuration {
    name      = "${var.name}-${var.env}-gateway-ip-config"
    subnet_id = var.subnet_id
  }

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.gateway.id]
  }

  # ------- Static -------------------------
  backend_address_pool {
    name  = local.static_backend_pool
    fqdns = [var.cdn_hostname]
  }

  backend_http_settings {
    name                                = local.static_backend_http_setting
    cookie_based_affinity               = "Disabled"
    port                                = 80
    protocol                            = "Http"
    request_timeout                     = 20
    pick_host_name_from_backend_address = true
    probe_name                          = "static-http"
  }

  backend_http_settings {
    name                                = local.static_backend_https_setting
    cookie_based_affinity               = "Disabled"
    port                                = 443
    protocol                            = "Https"
    request_timeout                     = 20
    pick_host_name_from_backend_address = true
    probe_name                          = "static-https"
  }

  # Need a custom health check for static sites as app gateway doesn't support it
  probe {
    name                                      = "static-http"
    interval                                  = 10
    path                                      = "/"
    pick_host_name_from_backend_http_settings = true
    protocol                                  = "Http"
    timeout                                   = 10
    unhealthy_threshold                       = 3

    match {
      status_code = ["200-399"]
    }
  }

  probe {
    name                                      = "static-https"
    interval                                  = 10
    path                                      = "/"
    pick_host_name_from_backend_http_settings = true
    protocol                                  = "Https"
    timeout                                   = 10
    unhealthy_threshold                       = 3

    match {
      status_code = ["200-399"]
    }
  }

  # ------- Backend API App -------------------------
  backend_address_pool {
    name         = local.api_backend_pool
    fqdns        = var.fqdns
    ip_addresses = var.ip_addresses
  }

  backend_http_settings {
    name                                = local.api_backend_https_setting
    cookie_based_affinity               = "Disabled"
    port                                = 443
    protocol                            = "Https"
    request_timeout                     = 20
    pick_host_name_from_backend_address = true
    probe_name                          = "backend-https"
  }

  backend_http_settings {
    name                                = local.api_backend_http_setting
    cookie_based_affinity               = "Disabled"
    port                                = 80
    protocol                            = "Http"
    request_timeout                     = 20
    pick_host_name_from_backend_address = true
  }

  # Add custom healthcheck probe to ping the Spring health endpoint
  probe {
    name                                      = "api-http"
    interval                                  = 10
    path                                      = "/actuator/health"
    pick_host_name_from_backend_http_settings = true
    protocol                                  = "Http"
    timeout                                   = 10
    unhealthy_threshold                       = 3

    match {
      status_code = ["200-399"]
    }
  }

  probe {
    name                                      = "api-https"
    interval                                  = 10
    path                                      = "/actuator/health"
    pick_host_name_from_backend_http_settings = true
    protocol                                  = "Https"
    timeout                                   = 10
    unhealthy_threshold                       = 3

    match {
      status_code = ["200-399"]
    }
  }

  # ------- Listeners -------------------------

  frontend_ip_configuration {
    name                 = local.frontend_config
    public_ip_address_id = azurerm_public_ip.static_gateway.id
  }

  # --- HTTP Listener
  frontend_port {
    name = local.http_listener
    port = 80
  }

  http_listener {
    name                           = local.http_listener
    frontend_ip_configuration_name = local.frontend_config
    frontend_port_name             = local.http_listener
    protocol                       = "Http"
  }

  # --- HTTPS Listener ---

  frontend_port {
    name = local.https_listener
    port = 443
  }

  http_listener {
    name                           = local.https_listener
    frontend_ip_configuration_name = local.frontend_config
    frontend_port_name             = local.https_listener
    protocol                       = "Https"
    ssl_certificate_name           = data.azurerm_key_vault_certificate.wildcard_simplereport_gov.name
  }

  ssl_certificate {
    name                = data.azurerm_key_vault_certificate.wildcard_simplereport_gov.name
    key_vault_secret_id = data.azurerm_key_vault_certificate.wildcard_simplereport_gov.secret_id
  }

  ssl_policy {
    policy_name          = "${var.env}-ssl"
    policy_type          = "Custom"
    min_protocol_version = "TLSv1_2"
    cipher_suites = [
      "TLS_RSA_WITH_AES_256_CBC_SHA256",
      "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
      "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
      "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
      "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
      "TLS_RSA_WITH_AES_128_GCM_SHA256",
      "TLS_RSA_WITH_AES_128_CBC_SHA256"
    ]
  }

  # ------- Routing -------------------------
  request_routing_rule {
    name                        = local.redirect_rule
    rule_type                   = "Basic"
    http_listener_name          = "${var.name}-http"
    redirect_configuration_name = "${var.name}-redirect"
  }

  redirect_configuration {
    name = local.redirect_rule

    include_path         = true
    include_query_string = true
    redirect_type        = "Permanent"
    target_listener_name = local.https_listener
  }

  request_routing_rule {
    name                       = "${var.name}-routing-https"
    rule_type                  = "PathBasedRouting"
    http_listener_name         = local.https_listener
    backend_address_pool_name  = local.static_backend_pool
    backend_http_settings_name = local.static_backend_https_setting
    url_path_map_name          = "${var.env}-urlmap"
  }

  url_path_map {
    name                               = "${var.env}-urlmap"
    default_backend_address_pool_name  = local.static_backend_pool
    default_backend_http_settings_name = local.static_backend_https_setting
    //    default_rewrite_rule_set_name      = "simple-report-routing"

    path_rule {
      name                       = "api"
      paths                      = ["/api/*", "/api"]
      backend_address_pool_name  = local.api_backend_pool
      backend_http_settings_name = local.api_backend_https_setting
      //      rewrite_rule_set_name      = "simple-report-routing"
    }

    path_rule {
      name                       = "react-static"
      paths                      = ["/app/static/*"]
      backend_address_pool_name  = local.static_backend_pool
      backend_http_settings_name = local.static_backend_https_setting
    }
  }

  //  rewrite_rule_set {
  //    name = "simple-report-routing"
  //    rewrite_rule {
  //      name          = "api-wildcard"
  //      rule_sequence = 100
  //
  //      condition {
  //        ignore_case = true
  //        negate      = false
  //        pattern     = ".*api/(.*)"
  //        variable    = "var_uri_path"
  //      }
  //    }
  //
  //    rewrite_rule {
  //      name          = "react-app"
  //      rule_sequence = 105
  //
  //      condition {
  //        ignore_case = true
  //        negate      = false
  //        pattern     = ".*app/(.*)"
  //        variable    = "var_uri_path"
  //      }
  //    }
  //  }

  autoscale_configuration {
    min_capacity = var.autoscale_min
    max_capacity = var.autoscale_max
  }

  depends_on = [
    azurerm_public_ip.static_gateway,
    azurerm_key_vault_access_policy.gateway
  ]

  tags = var.tags

  # Azure doesn't not support rewrite rules efficiently so they must be manually built out

  lifecycle {
    ignore_changes = [
      identity,
      rewrite_rule_set,
      url_path_map,
      probe,
      # comment if you are creating the gateway for the first time
      request_routing_rule
    ]
  }
}

// Gateway analytics
resource "azurerm_monitor_diagnostic_setting" "logs_metrics" {
  name                       = "${var.name}-${var.env}-gateway-logs-metrics"
  target_resource_id         = azurerm_application_gateway.load_balancer.id
  log_analytics_workspace_id = var.log_workspace_uri

  dynamic "log" {
    for_each = [
      "ApplicationGatewayAccessLog",
      "ApplicationGatewayPerformanceLog",
      "ApplicationGatewayFirewallLog",
    ]
    content {
      category = log.value

      retention_policy {
        enabled = false
      }
    }
  }

  dynamic "metric" {
    for_each = [
      "AllMetrics",
    ]
    content {
      category = metric.value

      retention_policy {
        enabled = false
      }
    }
  }
}
