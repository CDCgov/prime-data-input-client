locals {
  project = "prime"
  name    = "simple-report"
  env     = "dev"

  network_cidr = "10.1.0.0/16"
  rg_name      = data.azurerm_resource_group.dev.name
  rg_location  = data.azurerm_resource_group.dev.location
  management_tags = {
    prime-app      = "simple-report"
    environment    = local.env
    resource_group = "${local.project}-${local.name}-${local.env}"
  }
}

module "monitoring" {
  source        = "../../services/monitoring"
  env           = local.env
  management_rg = data.azurerm_resource_group.global.name
  rg_location   = local.rg_location
  rg_name       = local.rg_name

  app_url = "${local.env}.simplereport.gov"

  tags = local.management_tags
}

module "bastion" {
  source = "../../services/bastion_host"
  env    = local.env

  resource_group_location = local.rg_location
  resource_group_name     = local.rg_name

  virtual_network = module.vnet.network

  tags = local.management_tags
}

module "db" {
  source      = "../../services/postgres_db"
  env         = local.env
  rg_location = local.rg_location
  rg_name     = local.rg_name

  global_vault_id      = data.azurerm_key_vault.global.id
  db_vault_id          = data.azurerm_key_vault.db_keys.id
  db_encryption_key_id = data.azurerm_key_vault_key.db_encryption_key.id
  public_access        = false

  log_workspace_id = module.monitoring.log_analytics_workspace_id

  tags = local.management_tags
}

module "vnet" {
  source              = "../../services/virtual_network"
  env                 = local.env
  resource_group_name = local.rg_name
  network_address     = local.network_cidr
  management_tags     = local.management_tags
  location            = local.rg_location
}
