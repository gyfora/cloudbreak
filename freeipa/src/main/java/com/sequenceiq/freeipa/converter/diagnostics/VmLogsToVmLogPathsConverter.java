package com.sequenceiq.freeipa.converter.diagnostics;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.telemetry.model.VmLogs;
import com.sequenceiq.freeipa.api.v1.diagnostics.model.VmLogPath;
import com.sequenceiq.freeipa.api.v1.diagnostics.model.VmLogPathsResponse;

@Component
public class VmLogsToVmLogPathsConverter {

    public VmLogPathsResponse convert(List<VmLogs> vmLogs) {
        VmLogPathsResponse result = new VmLogPathsResponse();
        result.setLogPaths(Optional.ofNullable(vmLogs).orElse(List.of()).stream()
                .map(logs -> {
                    VmLogPath path = new VmLogPath();
                    path.setName(logs.getName());
                    path.setPath(logs.getPath());
                    path.setType(logs.getType());
                    path.setLabel(logs.getLabel());
                    path.setExcludes(logs.getExcludes());
                    return path;
                }).collect(Collectors.toList()));
        return result;
    }
}
