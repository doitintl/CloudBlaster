#!/bin/bash
echo "Deploying App Engine"
PROJECT=$1

if [[ -z "$PROJECT" ]]; then
  echo "Must provide project"
  exit 1
fi

gcloud app deploy -q --project $PROJECT
