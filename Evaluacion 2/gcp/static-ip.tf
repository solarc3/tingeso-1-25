data "google_compute_global_address" "microservices_ip" {
  name = "microservices-static-ip"
}

output "static_ip" {
  value = data.google_compute_global_address.microservices_ip.address
}
