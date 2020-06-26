package com.sequenceiq.freeipa.service.diagnostics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorFailedException;
import com.sequenceiq.cloudbreak.orchestrator.host.HostOrchestrator;
import com.sequenceiq.cloudbreak.orchestrator.model.GatewayConfig;
import com.sequenceiq.flow.core.FlowConstants;
import com.sequenceiq.freeipa.api.v1.diagnostics.model.DiagnosticsCollectionRequest;
import com.sequenceiq.freeipa.entity.InstanceMetaData;
import com.sequenceiq.freeipa.entity.Stack;
import com.sequenceiq.freeipa.flow.freeipa.diagnostics.event.DiagnosticsCollectionEvent;
import com.sequenceiq.freeipa.flow.freeipa.diagnostics.event.DiagnosticsCollectionStateSelectors;
import com.sequenceiq.freeipa.orchestrator.StackBasedExitCriteriaModel;
import com.sequenceiq.freeipa.repository.InstanceMetaDataRepository;
import com.sequenceiq.freeipa.repository.StackRepository;
import com.sequenceiq.freeipa.service.GatewayConfigService;
import com.sequenceiq.freeipa.service.freeipa.flow.FreeIpaFlowManager;
import com.sequenceiq.freeipa.service.operation.OperationService;
import com.sequenceiq.freeipa.service.stack.StackService;

import reactor.bus.Event;

@Service
public class DiagnosticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticsService.class);

    @Inject
    private StackService stackService;

    @Inject
    private OperationService operationService;

    @Inject
    private FreeIpaFlowManager flowManager;

    @Inject
    private GatewayConfigService gatewayConfigService;

    @Inject
    private StackRepository stackRepository;

    @Inject
    private InstanceMetaDataRepository instanceMetaDataRepository;

    @Inject
    private HostOrchestrator hostOrchestrator;

    public void startDiagnosticsCollection(DiagnosticsCollectionRequest request, String accountId, String userCrn) {
        String environmentCrn = request.getEnvironmentCrn();
        Stack stack = stackService.getByEnvironmentCrnAndAccountIdWithLists(environmentCrn, accountId);
        MDCBuilder.buildMdcContext(stack);
        DiagnosticsCollectionEvent diagnosticsCollectionEvent = DiagnosticsCollectionEvent.builder()
                .withResourceId(stack.getId())
                .withResourceCrn(stack.getResourceCrn())
                .withSelector(DiagnosticsCollectionStateSelectors.START_DIAGNOSTICS_INIT_EVENT.selector())
                .withParameters(createDiagnosticCollectionParams(request))
                .build();
        flowManager.notify(diagnosticsCollectionEvent, getFlowHeaders(userCrn));
    }

    public void init(Long stackId, Map<String, Object> parameters) throws CloudbreakOrchestratorFailedException {
        applyState("filecollector.init", stackId, parameters);
    }

    public void collect(Long stackId, Map<String, Object> parameters) throws CloudbreakOrchestratorFailedException {
        applyState("filecollector.collect", stackId, parameters);
    }

    public void upload(Long stackId, Map<String, Object> parameters) throws CloudbreakOrchestratorFailedException {
        applyState("filecollector.upload", stackId, parameters);
    }

    public void cleanup(Long stackId, Map<String, Object> parameters) throws CloudbreakOrchestratorFailedException {
        applyState("filecollector.cleanup", stackId, parameters);
    }

    private void applyState(String state, Long stackId,
            Map<String, Object> parameters) throws CloudbreakOrchestratorFailedException {
        hostOrchestrator.applyDiagnosticsState(getGatewayConfigs(stackId), state, parameters, new StackBasedExitCriteriaModel(stackId));
    }

    private List<GatewayConfig> getGatewayConfigs(Long stackId) {
        Stack stack = stackService.getStackById(stackId);
        Set<InstanceMetaData> instanceMetaDataSet = instanceMetaDataRepository.findAllInStack(stack.getId());
        return gatewayConfigService.getGatewayConfigs(stack, instanceMetaDataSet);
    }

    private Event.Headers getFlowHeaders(String userCrn) {
        return new Event.Headers(Map.of(FlowConstants.FLOW_TRIGGER_USERCRN, userCrn));
    }

    private Map<String, Object> createDiagnosticCollectionParams(DiagnosticsCollectionRequest request) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("destination", request.getDestination().toString());
        return parameters;
    }
}
