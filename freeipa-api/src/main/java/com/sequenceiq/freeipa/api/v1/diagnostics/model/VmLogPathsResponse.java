package com.sequenceiq.freeipa.api.v1.diagnostics.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;

@ApiModel("VmLogPathsV1Response")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VmLogPathsResponse {

    private List<VmLogPath> logPaths = List.of();

    public List<VmLogPath> getLogPaths() {
        return logPaths;
    }

    public void setLogPaths(List<VmLogPath> logPaths) {
        this.logPaths = logPaths;
    }


}
