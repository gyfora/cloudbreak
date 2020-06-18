package com.sequenceiq.freeipa.flow.freeipa.diagnostics;

import java.util.Map;

public class CollectionStartRequest extends AbstractDiagnosticsCollectionEvent {

    public CollectionStartRequest(DiagnosticsCollectionEvent event) {
        super(event);
    }

    public CollectionStartRequest(String selector, Long stackId, String accountId,
            String environmentCrn, String operationId, Map<String, Object> parameters) {
        super(selector, stackId, accountId, environmentCrn, operationId, parameters);
    }
}
