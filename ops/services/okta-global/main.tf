// Create the required simple report scope

// We need to import and manage the pdi scope as well.
// So we can remove it later

resource "okta_auth_server_scope" "sr" {
  auth_server_id   = data.okta_auth_server.default.id
  name             = "simple_report"
  description      = "Default OAUTH scope for Simple Report application"
  metadata_publish = "NO_CLIENTS"
  default          = false
}

// Add the required claims
resource "okta_auth_server_claim" "family_name" {
  auth_server_id = data.okta_auth_server.default.id
  claim_type     = "RESOURCE"
  name           = "family_name"
  value          = "user.lastName"
}

resource "okta_auth_server_claim" "given_name" {
  auth_server_id = data.okta_auth_server.default.id
  claim_type     = "RESOURCE"
  name           = "given_name"
  value          = "user.firstName"
}

// Create the CDC/USDS user groups
resource "okta_group" "prime_users" {
  name        = "Prime Team Members"
  description = "All Prime team members"
}

// Create a sign on policy requiring MFA

resource "okta_policy_signon" "mfa_require" {
  name            = "simple-report-mfa-require"
  status          = "ACTIVE"
  description     = "Require MFA for all users"
  groups_included = [var.all_users_group_id]
}

resource "okta_policy_rule_signon" "app_mfa" {
  policyid           = okta_policy_signon.mfa_require.id
  name               = "simple-report-mfa-require"
  status             = "ACTIVE"
  mfa_required       = true
  mfa_prompt         = "SESSION"
  network_connection = "ANYWHERE"
  authtype           = "ANY"
  mfa_lifetime       = 720
  session_idle       = 720
  session_lifetime   = 720
}
