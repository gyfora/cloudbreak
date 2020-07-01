package com.sequenceiq.cloudbreak.orchestrator.salt.poller.checker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sequenceiq.cloudbreak.orchestrator.model.Node;
import com.sequenceiq.cloudbreak.orchestrator.salt.client.SaltConnector;
import com.sequenceiq.cloudbreak.orchestrator.salt.client.target.HostList;
import com.sequenceiq.cloudbreak.orchestrator.salt.states.SaltStates;

import java.util.Map;
import java.util.Set;

public class ParameterizedStateRunner extends StateRunner {

    private final Map<String, Object> parameters;

    public ParameterizedStateRunner(Set<String> targetHostnames, Set<Node> allNode, String state, Map<String, Object> parameters) {
        super(targetHostnames, allNode, state);
        this.parameters = parameters;
    }

    @Override
    public String submit(SaltConnector saltConnector) throws SaltJobFailedException {
        HostList targets = new HostList(getTargetHostnames());
        try {
            return SaltStates.applyState(saltConnector, state, targets, parameters).getJid();
        } catch (JsonProcessingException e) {
            throw new SaltJobFailedException("ParameterizedStateRunner job failed.", e);
        }
    }

    @Override
    public String toString() {
        return "ParameterizedStateRunner{" + super.toString() + ", state: " + this.state + "}'";
    }
}
