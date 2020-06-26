{%- from 'filecollector/settings.sls' import filecollector with context %}

{% if filecollector.destination == "SRE" %}
run_filecollector_cleanup_all:
  cmd.run:
    - name: "sh /opt/filecollector/cleanup.sh true"
{% else %}
run_filecollector_cleanup_files:
  cmd.run:
    - name: "sh /opt/filecollector/cleanup.sh false"
{% endif %}