{%- from 'filecollector/settings.sls' import filecollector with context %}

{% set extra_params="" %}
{% if filecollector.startTime %}
  {% set extra_params = extra_params + " --startTime " + str(filecollector.startTime) %}
{% endif %}
{% if filecollector.endTime %}
  {% set extra_params = extra_params + " --endTime " + str(filecollector.startTime) %}
{% endif %}
{% if filecollector.labelFilter %}
  {% set labels_str = ",".join(labelFilter) %}
  {% set extra_params = extra_params + " --labels" + labels_str %}
{% endif %}

{% if filecollector.destination == "CLOUD_STORAGE" %}
filecollector_cloud_storage_start:
  cmd.run:
    - name: "python3 /opt/filecollector/filecollector.py --config /opt/filecollector/filecollector-cloud-storage.yaml {{ extra_params }}"
    - env:
        - LC_ALL: "en_US.utf8"
{% endif %}