data "azurerm_resource_group" "rg" {
  name = "prime-simple-report-management"
}

data "azurerm_key_vault_secret" "slack_metrics_webhook" {
  key_vault_id = azurerm_key_vault.sr.id
  name         = "simple-report-global-slack-webhook"
}

data "okta_group" "everyone" {
  name = "Everyone"
}
