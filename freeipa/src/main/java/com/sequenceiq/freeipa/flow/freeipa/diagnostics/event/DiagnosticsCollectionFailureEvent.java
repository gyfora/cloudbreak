package com.sequenceiq.freeipa.flow.freeipa.diagnostics.event;

import static com.sequenceiq.freeipa.flow.freeipa.diagnostics.event.DiagnosticsCollectionStateSelectors.FAILED_DIAGNOSTICS_COLLECTION_EVENT;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.flow.reactor.api.event.BaseFlowEvent;

public class DiagnosticsCollectionFailureEvent extends BaseFlowEvent implements Selectable {

    private final Exception exception;

    public DiagnosticsCollectionFailureEvent(Long resourceId, Exception exception, String resourceCrn) {
        super(FAILED_DIAGNOSTICS_COLLECTION_EVENT.name(), resourceId, resourceCrn);
        this.exception = exception;
    }

    @Override
    public String selector() {
        return FAILED_DIAGNOSTICS_COLLECTION_EVENT.name();
    }

    public Exception getException() {
        return exception;
    }
}

