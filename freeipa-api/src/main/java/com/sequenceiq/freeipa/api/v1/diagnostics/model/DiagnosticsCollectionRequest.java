package com.sequenceiq.freeipa.api.v1.diagnostics.model;

import java.util.List;

import com.sequenceiq.common.api.telemetry.model.VmLog;
import com.sequenceiq.common.api.telemetry.model.VmLogsDestination;

public class DiagnosticsCollectionRequest {

    private String environmentCrn;

    private String environmentName;

    private List<String> hosts;

    private List<String> labels;

    private VmLogsDestination destination;

    private List<VmLog> additionalLogs = List.of();

    public String getEnvironmentCrn() {
        return environmentCrn;
    }

    public void setEnvironmentCrn(String environmentCrn) {
        this.environmentCrn = environmentCrn;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public VmLogsDestination getDestination() {
        return destination;
    }

    public void setDestination(VmLogsDestination destination) {
        this.destination = destination;
    }

    public List<VmLog> getAdditionalLogs() {
        return additionalLogs;
    }

    public void setAdditionalLogs(List<VmLog> additionalLogs) {
        this.additionalLogs = additionalLogs;
    }
}
