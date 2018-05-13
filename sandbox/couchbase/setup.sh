#!/usr/bin/env bash
COUCH_SETTINGS="--cluster localhost -u Administrator -p couchbase"
set -m
./entrypoint.sh couchbase-server & sleep 10
couchbase-cli cluster-init --cluster 0.0.0.0 --cluster-username Administrator --cluster-password couchbase

couchbase-cli bucket-create $COUCH_SETTINGS --bucket sessions --bucket-type=couchbase --bucket-ramsize 100 --bucket-replica 0

couchbase-cli user-manage $COUCH_SETTINGS --set --rbac-username=sessions --rbac-password Session5 --roles bucket_admin[sessions] --auth-domain local

fg