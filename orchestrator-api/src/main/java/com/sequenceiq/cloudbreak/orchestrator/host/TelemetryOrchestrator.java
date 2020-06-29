package com.sequenceiq.cloudbreak.orchestrator.host;

import java.util.List;
import java.util.Set;

import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorFailedException;
import com.sequenceiq.cloudbreak.orchestrator.model.GatewayConfig;
import com.sequenceiq.cloudbreak.orchestrator.model.Node;
import com.sequenceiq.cloudbreak.orchestrator.state.ExitCriteriaModel;

public interface TelemetryOrchestrator {

    void stopTelemetryAgent(List<GatewayConfig> allGateway, Set<Node> nodes, ExitCriteriaModel exitCriteriaModel)
            throws CloudbreakOrchestratorFailedException;

    void installAndStartMonitoring(List<GatewayConfig> allGateway, Set<Node> nodes, ExitCriteriaModel exitModel)
            throws CloudbreakOrchestratorFailedException;
}
