#!/bin/sh

function log() {
  echo "$1"
}

function delete_file() {
  log "Deleting file: $1..."
  rm -rf "$1"
}

function delete_all() {
  log "Deleting everything inside /var/lib/filecollector folder..."
  rm -rf /var/lib/filecollector/**
}

function main() {
  if [[ "$1" == "true" ]]; then
    delete_all
  else
    for file in /var/lib/filecollector/*.gz
    do
      delete_file "$file"
    done
  fi
}

main "$@"