#!/bin/bash

REGION=us-east1


PROJECT=$1
if [[ -z "$PROJECT" ]]; then
  echo "Must provide project as first param"
  exit 1
fi
``
FUNC_NAME=$2
if [[ -z "$FUNC_NAME" ]]; then
  echo "Must provide function name as second param"
  exit 1
fi

# Deploy the Function
gcloud functions deploy $FUNC_NAME --runtime python38 --trigger-http --allow-unauthenticated --region=$REGION --project $PROJECT
