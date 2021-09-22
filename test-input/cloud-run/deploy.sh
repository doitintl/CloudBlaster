#!/bin/bash
REGION=us-east1
IMAGE=helloworld-cloudrun

export PROJECT=$

if [[ -z "$PROJECT" ]]; then
  echo "Must provide project"
  exit 1
fi
# Submit build
gcloud builds submit --tag gcr.io/$PROJECT/$IMAGE

# Could run with --platform gke (or kubernetes)
gcloud run deploy helloworld-service --image gcr.io/$PROJECT/$IMAGE --platform managed --region=$REGION --allow-unauthenticated
