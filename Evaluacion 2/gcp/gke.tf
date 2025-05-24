module "gke" {
  source  = "terraform-google-modules/kubernetes-engine/google"
  version = "~> 30.0"

  project_id = var.project_id
  name       = "microservices-cluster"
  region     = var.region
  zones      = ["${var.region}-b"]
  deletion_protection = false
  network           = google_compute_network.microservices_vpc.name
  subnetwork        = google_compute_subnetwork.gke_subnet.name
  ip_range_pods     = "pods-range"
  ip_range_services = "services-range"

  http_load_balancing        = true
  network_policy             = false
  horizontal_pod_autoscaling = false
  filestore_csi_driver       = false

  node_pools = [
    {
      name               = "microservices-pool"
      machine_type       = "e2-standard-2"
      node_locations     = "${var.region}-b"
      min_count          = 4
      max_count          = 8
      local_ssd_count    = 0
      spot               = false
      disk_size_gb       = 50
      disk_type          = "pd-standard"
      image_type         = "COS_CONTAINERD"
      auto_repair        = true
      auto_upgrade       = true
      preemptible        = false
      initial_node_count = 4
    },
  ]

  node_pools_oauth_scopes = {
    all = [
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring",
      "https://www.googleapis.com/auth/cloud-platform"
    ]
  }

  node_pools_labels = {
    all = {}
    microservices-pool = {
      environment = "test"
      purpose     = "microservices"
    }
  }

  node_pools_tags = {
    all = ["microservices"]
  microservices-pool = ["kartingrm","test"]
  }

  depends_on = [
    google_compute_network.microservices_vpc,
    google_compute_subnetwork.gke_subnet
  ]
}

output "kubernetes_cluster_name" {
  value = module.gke.name
}

output "kubernetes_cluster_host" {
  value = module.gke.endpoint
}
