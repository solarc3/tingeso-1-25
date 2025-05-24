variable "project_id" {
  description = "GCP ID"
  type        = string
}

variable "region" {
  description = "Region"
  type        = string
}

variable "zone" {
  description = "Zone"
  type        = string
}

variable "spring_mail_host" {
  description = "SMTP host para envío de emails"
  type        = string
  default     = "smtp.gmail.com"
}

variable "spring_mail_username" {
  description = "Usuario SMTP para envío de emails"
  type        = string
  default     = ""
}

variable "spring_mail_password" {
  description = "password"
  type        = string
  sensitive   = true
  default     = ""
}

variable "karting_package_name" {
  description = "package name"
  type        = string
  default     = "com.kartingrm"
}

variable "jpa_ddl_auto" {
  description = "(create, create-drop, update, validate, none)"
  type        = string
  default     = "create-drop"
}

variable "jpa_init_mode" {
  description = "How does JPA start (always, never, embedded)"
  type        = string
  default     = "never"
}

variable "dockerhub_username" {
  description = "dockerhub user"
  type        = string
  default     = "tu-dockerhub-user"
}
