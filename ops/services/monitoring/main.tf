locals {
  is_prod = var.env == "prod"
  // This is commented out until we actually have production deployments. Right now, it's just set to test that simplereport.cdc.gov is running.
  //  app_url = local.is_prod ? "simplereport.cdc.gov" : "${var.env}.simplereport.cdc.gov"
  app_url = "simplereport.cdc.gov"
}

data "azurerm_log_analytics_workspace" "law" {
  name = "simple-report-log-workspace-global"
  resource_group_name = var.management_rg
}

resource "azurerm_application_insights" "app_insights" {
  // App Insights doesn't have an option for frontend application types. So I picked NodeJS as it seemed the safest default.
  application_type = "Node.JS"
  location = var.rg_location
  resource_group_name = var.rg_name
  name = "prime-simple-report-${var.env}-client"

  tags = var.tags
}

// Setup website monitoring

resource "azurerm_application_insights_web_test" "web_test" {
  name = "prime-simple-report-${var.env}-web-test"
  location = var.rg_location
  resource_group_name = var.rg_name
  application_insights_id = azurerm_application_insights.app_insights.id
  geo_locations = [
    "us-tx-sn1-azr",
    "us-il-ch1-azr"]
  kind = "ping"
  description = "Verify website is publically available"

  configuration = <<XML
<WebTest Name="WebTest1" Id="ABD48585-0831-40CB-9069-682EA6BB3583" Enabled="True" CssProjectStructure="" CssIteration="" Timeout="0" WorkItemIds="" xmlns="http://microsoft.com/schemas/VisualStudio/TeamTest/2010" Description="" CredentialUserName="" CredentialPassword="" PreAuthenticate="True" Proxy="default" StopOnError="False" RecordedResultFile="" ResultsLocale="">
  <Items>
    <Request Method="GET" Guid="a5f10126-e4cd-570d-961c-cea43999a200" Version="1.1" Url="https://${local.app_url}" ThinkTime="0" Timeout="300" ParseDependentRequests="True" FollowRedirects="True" RecordResult="True" Cache="False" ResponseTimeGoal="0" Encoding="utf-8" ExpectedHttpStatusCode="200" ExpectedResponseUrl="" ReportingName="" IgnoreHttpStatusCode="False" />
  </Items>
</WebTest>
XML


  tags = var.tags

}
