resource "google_compute_network" "microservices_vpc" {
  name                    = "microservices-vpc"
  auto_create_subnetworks = false
  routing_mode           = "REGIONAL"
  lifecycle {
    prevent_destroy = true
  }
}

resource "google_compute_subnetwork" "gke_subnet" {
  name          = "gke-subnet"
  ip_cidr_range = "10.0.0.0/16"
  region        = var.region
  network       = google_compute_network.microservices_vpc.name

  secondary_ip_range {
    range_name    = "pods-range"
    ip_cidr_range = "10.1.0.0/16"
  }

  secondary_ip_range {
    range_name    = "services-range"
    ip_cidr_range = "10.2.0.0/16"
  }
}

#cloud sql
resource "google_compute_subnetwork" "database_subnet" {
  name          = "database-subnet"
  ip_cidr_range = "10.3.0.0/24"
  region        = var.region
  network       = google_compute_network.microservices_vpc.name
}

resource "google_compute_firewall" "allow_internal" {
  name    = "allow-internal"
  network = google_compute_network.microservices_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["0-65535"]
  }

  allow {
    protocol = "udp"
    ports    = ["0-65535"]
  }

  allow {
    protocol = "icmp"
  }

  source_ranges = ["10.0.0.0/8"]
}


resource "google_compute_firewall" "allow_http_https" {
  name    = "allow-http-https"
  network = google_compute_network.microservices_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["80", "443", "8080", "8761"] #erukera tbm
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["microservices"]
}

resource "google_compute_global_address" "private_ip_address" {
  name          = "private-ip-address"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.microservices_vpc.id
}

resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = google_compute_network.microservices_vpc.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_address.name]
}
