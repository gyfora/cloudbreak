package com.sequenceiq.freeipa.flow.freeipa.diagnostics;

import java.util.Map;

public class AbstractDiagnosticsCollectionEvent extends DiagnosticsCollectionEvent {

    public AbstractDiagnosticsCollectionEvent(String selector, Long stackId, String accountId, String environmentCrn,
            String operationId, Map<String, Object> parameters) {
        super(selector, stackId, accountId, environmentCrn, operationId, parameters);
    }

    public AbstractDiagnosticsCollectionEvent(DiagnosticsCollectionEvent event) {
        super(event.getSelector(), event.getResourceId(), event.getAccountId(), event.getEnvironmentCrn(),
                event.getOperationId(), event.getParameters());
    }
}
