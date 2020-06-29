package com.sequenceiq.cloudbreak.orchestrator.salt;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

@Component
public class SaltTelemetryOrchestrator implements TelemetryOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaltTelemetryOrchestrator.class);

    private static final String FLUENT_AGENT_STOP = "fluent.agent-stop";

    @Inject
    private ExitCriteria exitCriteria;

    @Inject
    private SaltService saltService;

    @Inject
    private SaltRunner saltRunner;

    @Value("${cb.max.salt.new.service.telemetry.stop.retry:5}")
    private int maxTelemetryStopRetry;

    @Override
    public void installAndStartMonitoring(List<GatewayConfig> allGateway, Set<Node> nodes, ExitCriteriaModel exitModel)
            throws CloudbreakOrchestratorFailedException {
        GatewayConfig primaryGateway = saltService.getPrimaryGatewayConfig(allGateway);
        Set<String> serverHostname = Sets.newHashSet(primaryGateway.getHostname());
        try (SaltConnector sc = saltService.createSaltConnector(primaryGateway)) {
            StateAllRunner stateAllJobRunner = new StateAllRunner(serverHostname, nodes, "monitoring.init");
            OrchestratorBootstrap saltJobIdTracker = new SaltJobIdTracker(sc, stateAllJobRunner);
            Callable<Boolean> saltJobRunBootstrapRunner = saltRunner.runner(saltJobIdTracker, exitCriteria, exitModel);
            saltJobRunBootstrapRunner.call();
        } catch (Exception e) {
            LOGGER.info("Error occurred during cluster monitoring start", e);
            throw new CloudbreakOrchestratorFailedException(e);
        }
    }

    @Override
    public void stopTelemetryAgent(List<GatewayConfig> allGateway, Set<Node> nodes, ExitCriteriaModel exitModel)
            throws CloudbreakOrchestratorFailedException {
        GatewayConfig primaryGateway = saltService.getPrimaryGatewayConfig(allGateway);
        Set<String> targetHostnames = nodes.stream().map(Node::getHostname).collect(Collectors.toSet());
        try (SaltConnector sc = saltService.createSaltConnector(primaryGateway)) {
            StateRunner stateRunner = new StateRunner(targetHostnames, nodes, FLUENT_AGENT_STOP);
            OrchestratorBootstrap saltJobIdTracker = new SaltJobIdTracker(sc, stateRunner);
            Callable<Boolean> saltJobRunBootstrapRunner = saltRunner.runner(saltJobIdTracker, exitCriteria, exitModel, maxTelemetryStopRetry, false);
            saltJobRunBootstrapRunner.call();
        } catch (Exception e) {
            LOGGER.info("Error occurred during telemetry agent stop", e);
            throw new CloudbreakOrchestratorFailedException(e);
        }
    }
}
