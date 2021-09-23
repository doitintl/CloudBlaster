#!/bin/bash
REGION=us-east1

PROJECT=$1
if [[ -z "$PROJECT" ]]; then
  echo "Must provide project as first param"
  exit 1
fi

SERVICE=$2
if [[ -z "$SERVICE" ]]; then
  echo "Must provide service as second param"
  exit 1
fi

gcloud run deploy $SERVICE --image gcr.io/cloudrun/hello --platform managed --region=$REGION --allow-unauthenticated --project $PROJECT
