{%- from 'telemetry/settings.sls' import telemetry with context %}
{%- from 'filecollector/settings.sls' import filecollector with context %}

/opt/filecollector/cloud_storage_upload.sh:
   file.managed:
    - source: salt://filecollector/template/cloud_storage_upload.sh.j2
    - template: jinja
    - user: "root"
    - group: "root"
    - mode: '0750'

filecollector_upload_to_cloud_storage:
  cmd.run:
    - name: "sh /opt/filecollector/cloud_storage_upload.sh"