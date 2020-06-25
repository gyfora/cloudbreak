{%- from 'fluent/settings.sls' import fluent with context %}
{% set filecollector = {} %}

{% set log_folder = salt['pillar.get']('fluent:logFolderName') %}
{% set s3_log_bucket = salt['pillar.get']('fluent:s3LogArchiveBucketName') %}
{% set azure_storage_account = salt['pillar.get']('fluent:azureStorageAccount') %}
{% set azure_container = salt['pillar.get']('fluent:azureContainer') %}
{% set destination = salt['pillar.get']('filecollector:destination') %}
{% set azure_storage_instance_msi = salt['pillar.get']('fluent:azureInstanceMsi') %}
{% if salt['pillar.get']('fluent:azureIdBrokerInstanceMsi') %}
    {% set azure_storage_idbroker_instance_msi = salt['pillar.get']('fluent:azureIdBrokerInstanceMsi') %}
{% else %}
    {% set azure_storage_idbroker_instance_msi = salt['pillar.get']('fluent:azureInstanceMsi') %}
{% endif %}

{% if azure_storage_account and azure_container %}
  {% set azure_storage_diagnostics_base_url = 'https://' + azure_storage_account + '.dfs.core.windows.net/' + azure_container + log_folder + "/bundles" %}
{% else %}
  {% set azure_storage_diagnostics_base_url = '' %}
{% endif %}

{% do filecollector.update({
    "logFolder": log_folder,
    "s3LogBucket": s3_log_bucket,
    "destination": destination,
    "azureInstanceMsi": azure_storage_instance_msi,
    "azureIdBrokerInstanceMsi": azure_storage_idbroker_instance_msi,
    "azureStorageDiagnosticsBaseUrl": azure_storage_diagnostics_base_url
}) %}