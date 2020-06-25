{%- from 'telemetry/settings.sls' import telemetry with context %}
{%- from 'filecollector/settings.sls' import filecollector with context %}

/opt/filecollector/cleanup.sh:
   file.managed:
    - source: salt://filecollector/scripts/cleanup.sh
    - user: "root"
    - group: "root"
    - mode: '0750'

{% if filecollector.destination == "SRE" %}
run_filecollector_cleanup_all:
  cmd.run:
    - name: "sh /opt/filecollector/cleanup.sh true"
{% else %}
run_filecollector_cleanup_files:
  cmd.run:
    - name: "sh /opt/filecollector/cleanup.sh false"
{% endif %}