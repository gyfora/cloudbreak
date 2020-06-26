{%- from 'filecollector/settings.sls' import filecollector with context %}

filecollector_upload_to_cloud_storage:
  cmd.run:
    - name: "sh /opt/filecollector/cloud_storage_upload.sh"