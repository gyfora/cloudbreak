{%- from 'filecollector/settings.sls' import filecollector with context %}

{% if filecollector.destination == "CLOUD_STORAGE" %}
filecollector_cloud_storage_start:
  cmd.run:
    - name: "python3 /opt/filecollector/filecollector.py --config /opt/filecollector/filecollector-cloud-storage.yaml"
    - env:
        - LC_ALL: "en_US.utf8"
{% endif %}