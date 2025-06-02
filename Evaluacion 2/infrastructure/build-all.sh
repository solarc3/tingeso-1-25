ls -1d */ \
  | xargs -n1 -P "$(ls -1d */ | wc -l)" -I % sh -c 'cd "%" && gcloud builds submit --region=us-central1 --config=cloudbuild.yaml .'
