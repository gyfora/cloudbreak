package com.sequenceiq.cloudbreak.orchestrator.salt;

import com.google.common.collect.Sets;
import com.sequenceiq.cloudbreak.orchestrator.OrchestratorBootstrap;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorFailedException;
import com.sequenceiq.cloudbreak.orchestrator.host.TelemetryOrchestrator;
import com.sequenceiq.cloudbreak.orchestrator.model.GatewayConfig;
import com.sequenceiq.cloudbreak.orchestrator.model.Node;
import com.sequenceiq.cloudbreak.orchestrator.salt.client.SaltConnector;
import com.sequenceiq.cloudbreak.orchestrator.salt.poller.SaltJobIdTracker;
import com.sequenceiq.cloudbreak.orchestrator.salt.poller.checker.StateAllRunner;
import com.sequenceiq.cloudbreak.orchestrator.salt.poller.checker.StateRunner;
import com.sequenceiq.cloudbreak.orchestrator.salt.runner.SaltRunner;
import com.sequenceiq.cloudbreak.orchestrator.state.ExitCriteria;
import com.sequenceiq.cloudbreak.orchestrator.state.ExitCriteriaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Component
public class SaltTelemetryOrchestrator implements TelemetryOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaltTelemetryOrchestrator.class);

    private static final String FLUENT_AGENT_STOP = "fluent.agent-stop";

    public static final String MONITORING_INIT = "monitoring.init";

    public static final String FILECOLLECTOR_INIT = "filecollector.init";

    public static final String FILECOLLECTOR_COLLECT = "filecollector.collect";

    public static final String FILECOLLECTOR_UPLOAD = "filecollector.upload";
    public static final String FILECOLLECTOR_CLEANUP = "filecollector.cleanup";

    @Inject
    private ExitCriteria exitCriteria;

    @Inject
    private SaltService saltService;

    @Inject
    private SaltRunner saltRunner;

    @Value("${cb.max.salt.new.service.telemetry.stop.retry:5}")
    private int maxTelemetryStopRetry;

    @Override
    public void installAndStartMonitoring(List<GatewayConfig> allGateways, Set<Node> nodes, ExitCriteriaModel exitModel)
            throws CloudbreakOrchestratorFailedException {
        GatewayConfig primaryGateway = saltService.getPrimaryGatewayConfig(allGateways);
        Set<String> serverHostname = Sets.newHashSet(primaryGateway.getHostname());
        try (SaltConnector sc = saltService.createSaltConnector(primaryGateway)) {
            StateAllRunner stateAllJobRunner = new StateAllRunner(serverHostname, nodes, MONITORING_INIT);
            OrchestratorBootstrap saltJobIdTracker = new SaltJobIdTracker(sc, stateAllJobRunner);
            Callable<Boolean> saltJobRunBootstrapRunner = saltRunner.runner(saltJobIdTracker, exitCriteria, exitModel);
            saltJobRunBootstrapRunner.call();
        } catch (Exception e) {
            LOGGER.info("Error occurred during cluster monitoring start", e);
            throw new CloudbreakOrchestratorFailedException(e);
        }
    }

    @Override
    public void stopTelemetryAgent(List<GatewayConfig> allGateways, Set<Node> nodes, ExitCriteriaModel exitModel)
            throws CloudbreakOrchestratorFailedException {
        runSaltState(allGateways, nodes, exitModel, FLUENT_AGENT_STOP, "Error occurred during telemetry agent stop.");
    }

    @Override
    public void initDiagnosticCollection(List<GatewayConfig> allGateways, Set<Node> nodes, ExitCriteriaModel exitModel)
            throws CloudbreakOrchestratorFailedException {
        runSaltState(allGateways, nodes, exitModel, FILECOLLECTOR_INIT, "Error occurred during diagnostics filecollector init.");
    }

    @Override
    public void executeDiagnosticCollection(List<GatewayConfig> allGateways, Set<Node> nodes, ExitCriteriaModel exitModel)
            throws CloudbreakOrchestratorFailedException {
        runSaltState(allGateways, nodes, exitModel, FILECOLLECTOR_COLLECT, "Error occurred during diagnostics filecollector collect.");
    }

    @Override
    public void uploadCollectedDiagnostics(List<GatewayConfig> allGateways, Set<Node> nodes, ExitCriteriaModel exitModel)
            throws CloudbreakOrchestratorFailedException {
        runSaltState(allGateways, nodes, exitModel, FILECOLLECTOR_UPLOAD, "Error occurred during diagnostics filecollector upload.");
    }

    @Override
    public void cleanupCollectedDiagnostics(List<GatewayConfig> allGateways, Set<Node> nodes, ExitCriteriaModel exitModel)
            throws CloudbreakOrchestratorFailedException {
        runSaltState(allGateways, nodes, exitModel, FILECOLLECTOR_CLEANUP, "Error occurred during diagnostics filecollector cleanup.");
    }

    private void runSaltState(List<GatewayConfig> allGateways, Set<Node> nodes, ExitCriteriaModel exitModel, String saltState, String errorMessage) throws CloudbreakOrchestratorFailedException {
        GatewayConfig primaryGateway = saltService.getPrimaryGatewayConfig(allGateways);
        Set<String> targetHostnames = nodes.stream().map(Node::getHostname).collect(Collectors.toSet());
        try (SaltConnector sc = saltService.createSaltConnector(primaryGateway)) {
            StateRunner stateRunner = new StateRunner(targetHostnames, nodes, saltState);
            OrchestratorBootstrap saltJobIdTracker = new SaltJobIdTracker(sc, stateRunner);
            Callable<Boolean> saltJobRunBootstrapRunner = saltRunner.runner(saltJobIdTracker, exitCriteria, exitModel, maxTelemetryStopRetry, false);
            saltJobRunBootstrapRunner.call();
        } catch (Exception e) {
            LOGGER.info(errorMessage, e);
            throw new CloudbreakOrchestratorFailedException(e);
        }
    }
}
