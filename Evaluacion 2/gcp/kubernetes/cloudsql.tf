locals {
  microservices = [
    "tariffs-service",        # M1 - Configuración de Tarifas y Duración de Reservas
    "group-discounts-service", # M2 - Descuentos por Número de Personas
    "customer-discounts-service", # M3 - Descuentos para Clientes Frecuentes
    "special-rates-service",  # M4 - Tarifas para Días Especiales
    "reservations-service",   # M5 - Registro de Reservas y Comprobante de Pago
    "track-schedule-service", # M6 - Rack Semanal de Ocupación de la Pista
    "reports-service"         # M7 - Reportes de Ingresos
  ]
}

resource "google_sql_database_instance" "microservice_db" {
  for_each = toset(local.microservices)

  name             = "${each.value}-db-instance"
  database_version = "POSTGRES_17"
  region           = var.region

  settings {
    tier = "db-f1-micro"

    ip_configuration {
      ipv4_enabled    = false
      private_network = google_compute_network.microservices_vpc.id
    }

    backup_configuration {
      enabled                        = false
    }
  }

  deletion_protection = false

  depends_on = [google_service_networking_connection.private_vpc_connection]
}

resource "google_sql_database" "microservice_database" {
  for_each = toset(local.microservices)

  name     = replace(each.value, "-", "_")
  instance = google_sql_database_instance.microservice_db[each.value].name
}

resource "google_sql_user" "microservice_user" {
  for_each = toset(local.microservices)

  name     = "${replace(each.value, "-", "_")}_user"
  instance = google_sql_database_instance.microservice_db[each.value].name
  password = "KartingRM2025!"

}

output "database_connections" {
  description = "Información de conexión para cada microservicio KartingRM"
  value = {
    for service in local.microservices :
    service => {
      host     = google_sql_database_instance.microservice_db[service].private_ip_address
              port     = "5432"
      database = google_sql_database.microservice_database[service].name
      username = google_sql_user.microservice_user[service].name
      password = google_sql_user.microservice_user[service].password
      jdbc_url = "jdbc:postgresql://${google_sql_database_instance.microservice_db[service].private_ip_address}:5432/${google_sql_database.microservice_database[service].name}?sslmode=disable&serverTimezone=America/Santiago"
    }
  }
  sensitive = true
}

output "spring_boot_properties_config" {
  description = "Config application.properties microservicio"
  value = {
    for service in local.microservices :
    service => {
      properties = [
        "# Database Configuration for ${service}",
        "spring.datasource.url=jdbc:postgresql://${google_sql_database_instance.microservice_db[service].private_ip_address}:5432/${google_sql_database.microservice_database[service].name}?sslmode=disable",
        "spring.datasource.username=${google_sql_user.microservice_user[service].name}",
        "spring.datasource.password=${google_sql_user.microservice_user[service].password}",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "",
        "# JPA/Hibernate Configuration",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.properties.hibernate.format_sql=true",
        "",
        "# Server Configuration",
        "server.port=0",
        "",
        "# Eureka Client Configuration",
        "eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka/",
        "eureka.instance.prefer-ip-address=true",
        "",
        "# Spring Cloud Config",
        "spring.cloud.config.uri=http://config-server:8888",
        "spring.application.name=${service}",
        "",
        "# Management endpoints",
        "management.endpoints.web.exposure.include=health,info,metrics",
        "management.endpoint.health.show-details=always"
      ]
    }
  }
  sensitive = true
}

output "k8s_database_config" {
  description = "ConfigMaps y Secrets"
  value = {
    for service in local.microservices :
    service => {
      config_map_data = {
        "DB_HOST"     = google_sql_database_instance.microservice_db[service].private_ip_address
        "DB_PORT"     = "5432"
        "DB_NAME"     = google_sql_database.microservice_database[service].name
        "DB_USERNAME" = google_sql_user.microservice_user[service].name
        "JDBC_URL"    = "jdbc:postgresql://${google_sql_database_instance.microservice_db[service].private_ip_address}:5432/${google_sql_database.microservice_database[service].name}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago"
      }
      secret_data = {
        "DB_PASSWORD" = base64encode(google_sql_user.microservice_user[service].password)
      }
    }
  }
  sensitive = true
}

output "database_instances_info" {
  description = "Información general de las instancias de base de datos"
  value = {
    for service in local.microservices :
    service => {
      instance_name    = google_sql_database_instance.microservice_db[service].name
      connection_name  = google_sql_database_instance.microservice_db[service].connection_name
      private_ip       = google_sql_database_instance.microservice_db[service].private_ip_address
      database_version = google_sql_database_instance.microservice_db[service].database_version
      tier            = google_sql_database_instance.microservice_db[service].settings[0].tier
    }
  }
}
