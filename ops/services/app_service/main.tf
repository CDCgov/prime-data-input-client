locals {
  all_app_settings = merge(var.app_settings,
    {
      for k, v in var.deploy_info : join("_", ["INFO", "DEPLOY", upper(k)]) => v
      if v != ""
    },
    {
      "DOCKER_REGISTRY_SERVER_PASSWORD" = data.terraform_remote_state.global.outputs.acr_simeplereport_admin_password
      "DOCKER_REGISTRY_SERVER_URL"      = "https://${data.terraform_remote_state.global.outputs.acr_simeplereport_name}.azurecr.io"
      "DOCKER_REGISTRY_SERVER_USERNAME" = data.terraform_remote_state.global.outputs.acr_simeplereport_name
      "WEBSITES_PORT"                   = "8080"
      "WEBSITE_DNS_SERVER"              = "168.63.129.16" # https://docs.microsoft.com/en-us/azure/app-service/web-sites-integrate-with-vnet#azure-dns-private-zones
      "WEBSITE_VNET_ROUTE_ALL"          = "1"
  })
}

resource "azurerm_app_service_plan" "service_plan" {
  name                = "${var.az_account}-appserviceplan-${var.env}"
  location            = var.resource_group_location
  resource_group_name = var.resource_group_name

  # Define Linux as Host OS
  kind     = "Linux"
  reserved = true

  sku {
    tier     = var.instance_tier
    size     = var.instance_size
    capacity = var.instance_count
  }
}

resource "azurerm_app_service" "service" {
  name                = "${var.name}-${var.env}"
  location            = var.resource_group_location
  resource_group_name = var.resource_group_name
  app_service_plan_id = azurerm_app_service_plan.service_plan.id

  # Configure Docker Image to load on start
  site_config {
    linux_fx_version = var.docker_image_uri
    always_on        = "true"
    min_tls_version  = "1.2"
    # this should keep a bad instance from being routed to. let's find out!
    health_check_path = "/actuator/health/readiness"
  }

  app_settings = local.all_app_settings
  https_only   = var.https_only

  identity {
    type = "SystemAssigned"
  }

  logs {
    http_logs {
      file_system {
        retention_in_days = 7
        retention_in_mb   = 30
      }
    }
  }

  lifecycle {
    ignore_changes = [
      app_settings, site_config[0].linux_fx_version
    ]
  }
}

resource "azurerm_app_service_slot" "staging" {
  name                = "staging"
  app_service_name    = azurerm_app_service.service.name
  app_service_plan_id = azurerm_app_service_plan.service_plan.id
  resource_group_name = var.resource_group_name
  location            = var.resource_group_location

  app_settings = local.all_app_settings
  https_only   = var.https_only

  site_config {
    linux_fx_version = var.docker_image_uri
    always_on        = "true"
  }

  identity {
    type = "SystemAssigned"
  }

  logs {
    http_logs {
      file_system {
        retention_in_days = 7
        retention_in_mb   = 30
      }
    }
  }
}

resource "azurerm_key_vault_access_policy" "app_secret_access" {
  key_vault_id = var.key_vault_id
  object_id    = azurerm_app_service.service.identity[0].principal_id
  tenant_id    = var.tenant_id

  key_permissions = [
    "get",
    "list",
  ]
  secret_permissions = [
    "get",
    "list",
  ]
}

resource "azurerm_key_vault_access_policy" "staging_slot_secret_access" {
  key_vault_id = var.key_vault_id
  object_id    = azurerm_app_service_slot.staging.identity[0].principal_id
  tenant_id    = var.tenant_id

  key_permissions = [
    "get",
    "list",
  ]
  secret_permissions = [
    "get",
    "list",
  ]
}

resource "azurerm_app_service_slot_virtual_network_swift_connection" "staging" {
  slot_name      = azurerm_app_service_slot.staging.name
  app_service_id = azurerm_app_service.service.id
  subnet_id      = var.webapp_subnet_id
}
