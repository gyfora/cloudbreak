package com.sequenceiq.freeipa.controller;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.sequenceiq.common.api.telemetry.response.VmLogsResponse;
import com.sequenceiq.freeipa.api.v1.diagnostics.DiagnosticsV1Endpoint;
import com.sequenceiq.freeipa.api.v1.diagnostics.model.DiagnosticsCollectionRequest;
import com.sequenceiq.freeipa.api.v1.operation.model.OperationStatus;
import com.sequenceiq.freeipa.converter.diagnostics.VmLogsToVmLogsResponseConverter;
import com.sequenceiq.freeipa.service.diagnostics.DiagnosticsService;
import com.sequenceiq.freeipa.service.telemetry.VmLogsService;
import com.sequenceiq.freeipa.util.CrnService;

@Controller
public class DiagnosticsV1Controller implements DiagnosticsV1Endpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticsV1Controller.class);

    @Inject
    private CrnService crnService;

    @Inject
    private DiagnosticsService diagnosticsService;

    @Inject
    private VmLogsService vmLogsService;

    @Inject
    private VmLogsToVmLogsResponseConverter vmlogsConverter;

    @Override
    public OperationStatus collectDiagnostics(@Valid DiagnosticsCollectionRequest request) {
        String accountId = crnService.getCurrentAccountId();
        return diagnosticsService.collect(request, accountId);
    }

    @Override
    public VmLogsResponse getVmLogs() {
        return vmlogsConverter.convert(vmLogsService.getVmLogs());
    }
}
