resource "google_compute_address" "microservices_static_ip" {
  name   = "microservices-static-ip"
  region = var.region
}

output "static_ip" {
  value = google_compute_address.microservices_static_ip.address
}
