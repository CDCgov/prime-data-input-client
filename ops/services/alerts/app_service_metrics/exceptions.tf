# To do log-based alerts, we need to make sure we're actually collecting AppServiceConsoleLogs from the API
data "azurerm_resource_group" "management" {
  name = "prime-simple-report-management"
}

data "azurerm_log_analytics_workspace" "global" {
  name                = "simple-report-log-workspace-global"
  resource_group_name = data.azurerm_resource_group.management.name
}

resource "azurerm_monitor_diagnostic_setting" "collect_appserviceconsolelogs" {
  name                       = "${local.env_title} API App Service console logs"
  target_resource_id         = var.app_service_id
  log_analytics_workspace_id = data.azurerm_log_analytics_workspace.global.id

  log {
    category = "AppServiceConsoleLogs"
    enabled  = true

    retention_policy {
      days    = 7
      enabled = true
    }
  }
}

# Add an alert for GraphQL query validation failures (more than 3 in a 5-minute window)
data "azurerm_resource_group" "app" {
  name = var.rg_name
}

resource "azurerm_monitor_scheduled_query_rules_alert" "graphql_query_validation_failures" {
  name                = "${var.env}-graphql-query-validation-failures"
  description         = "${local.env_title} GraphQL query validation failures"
  location            = data.azurerm_resource_group.app.location
  resource_group_name = var.rg_name

  action {
    action_group = var.action_group_ids
  }

  data_source_id = var.app_service_id
  enabled        = true

  query = <<-QUERY
AppServiceConsoleLogs
  | where tolower(ResultDescription) contains "query failed to validate"
  QUERY

  severity    = 1
  frequency   = 5
  time_window = 5
  trigger {
    operator  = "GreaterThan"
    threshold = 0
  }
}

resource "azurerm_monitor_scheduled_query_rules_alert" "first_error_in_a_week" {
  name                = "${var.env}-first-error-in-a-week"
  description         = "${local.env_title} alert when an error is seen for the first time in a week"
  location            = data.azurerm_resource_group.app.location
  resource_group_name = var.rg_name

  action {
    action_group = var.action_group_ids
  }

  data_source_id = var.app_insights_id
  enabled        = true

  # - Collect all requests that were exceptions in the week preceeding today
  # - Do the same for today
  # - leftanti join the two result sets to return only results from today that were not found in the week preceeding today
  query = <<-QUERY
requests
| where timestamp <= now() and timestamp > now(-1d) and success == false
| join kind= inner (
    exceptions
    | where timestamp <= now() and timestamp > now(-1d)
    )
    on operation_Id
| project exceptionType = type, failedMethod = method, requestName = name, combinedErrorString = strcat(type, method, name)
| join kind= leftanti (
    requests
    | where timestamp <= now(-1d) and timestamp > now(-8d) and success == false
    | join kind= inner (
        exceptions
        | where timestamp <= now(-1d) and timestamp > now(-8d)
        )
        on operation_Id
    | project exceptionType = type, failedMethod = method, requestName = name, combinedErrorString = strcat(type, method, name)
    )
    on combinedErrorString
| distinct exceptionType, failedMethod, requestName
  QUERY

  severity    = 2
  frequency   = 1440 // Run daily
  time_window = 1440
  trigger {
    operator  = "GreaterThan"
    threshold = 0
  }
}

resource "azurerm_monitor_scheduled_query_rules_alert" "account_request_failures" {
  name                = "${var.env}-account-request-failures"
  description         = "${local.env_title} alert when an AccountRequestFailureException is seen"
  location            = data.azurerm_resource_group.app.location
  resource_group_name = var.rg_name

  action {
    action_group = var.action_group_ids
  }

  data_source_id = var.app_insights_id
  enabled        = true

  query = <<-QUERY
requests
| where timestamp > ago(2h) and success == false
| join kind= inner (
    exceptions
    | where timestamp > ago(2h)
    )
    on operation_Id
| where type hassuffix "AccountRequestFailureException"
| project timestamp, exceptionType = type, failedMethod = method, requestName = name
  QUERY

  severity    = 1
  frequency   = 5
  time_window = 5
  trigger {
    operator  = "GreaterThan"
    threshold = 0
  }
}

resource "azurerm_monitor_scheduled_query_rules_alert" "frontend_error_boundary" {
  name                = "${var.env}-frontend-error-boundary"
  description         = "${local.env_title} uncaught frontend exceptions caught by PrimeErrorBoundary"
  location            = data.azurerm_resource_group.app.location
  resource_group_name = var.rg_name

  action {
    action_group = var.action_group_ids
  }

  data_source_id = var.app_insights_id
  enabled        = true

  query = <<-QUERY
 exceptions
 | where type startswith "PrimeErrorBoundary"
  QUERY

  severity    = 3
  frequency   = 5
  time_window = 5
  trigger {
    operator  = "GreaterThan"
    threshold = 0
  }
}
