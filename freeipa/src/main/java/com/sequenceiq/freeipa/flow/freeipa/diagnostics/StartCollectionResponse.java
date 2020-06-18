package com.sequenceiq.freeipa.flow.freeipa.diagnostics;

import java.util.Map;

public class StartCollectionResponse extends AbstractDiagnosticsCollectionEvent {
    public StartCollectionResponse(String selector, Long stackId, String accountId, String environmentCrn, String operationId,
            Map<String, Object> parameters) {
        super(selector, stackId, accountId, environmentCrn, operationId, parameters);
    }

    public StartCollectionResponse(DiagnosticsCollectionEvent event) {
        super(event);
    }
}
