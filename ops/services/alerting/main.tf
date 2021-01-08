# Create an action group to handle alerts
locals {
  admins        = split("\n", file("${path.module}/admins.txt"))
  function_code = "${path.module}/functions/build/alertscode.zip"
}

resource "azurerm_monitor_action_group" "admins" {
  name                = "prime-simple-report-global-admins"
  resource_group_name = var.rg_name
  short_name          = "SR Admins"
}

resource "azurerm_app_service_plan" "alerts-plan" {
  location            = var.rg_location
  name                = "alerts-appservice-plan"
  resource_group_name = var.rg_name
  kind                = "FunctionApp"
  sku {
    tier = "Dynamic"
    size = "Y1"
  }
}

# Create an access policy in order to grab the KV secret
resource "azurerm_key_vault_access_policy" "slack_webhook" {
  key_vault_id = var.key_vault_id
  object_id    = azurerm_function_app.alerts.identity[0].principal_id
  tenant_id    = data.azurerm_client_config.current.tenant_id

  secret_permissions = [
    "get"
  ]
}


# Create the azure function
resource "azurerm_function_app" "alerts" {
  app_service_plan_id        = azurerm_app_service_plan.alerts-plan.id
  location                   = var.rg_location
  name                       = "prime-simple-report-error-manager"
  resource_group_name        = var.rg_name
  version                    = "~3"
  storage_account_name       = data.azurerm_storage_account.global.name
  storage_account_access_key = data.azurerm_storage_account.global.primary_access_key
  https_only                 = true

  identity {
    type = "SystemAssigned"
  }

  app_settings = {
    APPINSIGHTS_INSTRUMENTATIONKEY        = var.app_insights_key
    APPLICATIONINSIGHTS_CONNECTION_STRING = var.app_insights_instrumentation_key
    FUNCTIONS_WORKER_RUNTIME              = "node"
    WEBSITE_NODE_DEFAULT_VERSION          = "~12"
    HASH                                  = base64encode(filesha256(local.function_code))
    WEBSITE_RUN_FROM_PACKAGE              = "https://${data.azurerm_storage_account.global.name}.blob.core.windows.net/${azurerm_storage_container.alerts.name}/${azurerm_storage_blob.alertscode.name}${data.azurerm_storage_account_sas.sas.sas}"
    SLACK_WEBHOOK                         = "@Microsoft.KeyVault(SecretUri=${data.azurerm_key_vault_secret.slack_webhook.id})"
  }
}
