variable "all_users_group_id" {
  description = "Group to include in universal Okta resources such as MFA rules"
  type        = string
  sensitive   = true
}
