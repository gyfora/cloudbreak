package com.sequenceiq.freeipa.api.v1.diagnostics.model;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.sequenceiq.common.api.telemetry.model.VmLog;
import com.sequenceiq.common.api.telemetry.model.DiagnosticsDestination;

public class DiagnosticsCollectionRequest {

    private String environmentCrn;

    private String supportTicket;

    private List<String> hosts;

    private List<String> labels;

    @NotNull
    private DiagnosticsDestination destination;

    private List<VmLog> additionalLogs = List.of();

    public String getEnvironmentCrn() {
        return environmentCrn;
    }

    public void setEnvironmentCrn(String environmentCrn) {
        this.environmentCrn = environmentCrn;
    }

    public String getSupportTicket() {
        return supportTicket;
    }

    public void setSupportTicket(String supportTicket) {
        this.supportTicket = supportTicket;
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

    public DiagnosticsDestination getDestination() {
        return destination;
    }

    public void setDestination(DiagnosticsDestination destination) {
        this.destination = destination;
    }

    public List<VmLog> getAdditionalLogs() {
        return additionalLogs;
    }

    public void setAdditionalLogs(List<VmLog> additionalLogs) {
        this.additionalLogs = additionalLogs;
    }
}
