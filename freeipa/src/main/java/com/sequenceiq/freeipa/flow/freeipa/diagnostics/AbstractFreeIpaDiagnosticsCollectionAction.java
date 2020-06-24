package com.sequenceiq.freeipa.flow.freeipa.diagnostics;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;

import com.sequenceiq.cloudbreak.common.event.Payload;
import com.sequenceiq.flow.core.FlowParameters;
import com.sequenceiq.freeipa.entity.FreeIpa;
import com.sequenceiq.freeipa.flow.stack.AbstractStackAction;
import com.sequenceiq.freeipa.flow.stack.StackFailureEvent;
import com.sequenceiq.freeipa.service.freeipa.FreeIpaService;

public abstract class AbstractFreeIpaLogCollectionAction<P extends Payload> extends AbstractStackAction<FreeIpaLogCollectionState, FreeIpaDiagnosticsCollectionEvent, FreeIpaContext, P> {

    @Inject
    private FreeIpaService freeIpaService;

    public AbstractFreeIpaLogCollectionAction(Class<P> payloadClass) {
        super(payloadClass);
    }

    @Override
    protected FreeIpaContext createFlowContext(FlowParameters flowParameters, StateContext<FreeIpaLogCollectionState, FreeIpaDiagnosticsCollectionEvent> stateContext,
            P payload) {
        FreeIpa freeIpa = freeIpaService.findByStackId(payload.getResourceId());
        return new FreeIpaContext(flowParameters, freeIpa);
    }

    @Override
    protected Object getFailurePayload(P payload, Optional<FreeIpaContext> flowContext, Exception ex) {
        return new StackFailureEvent(payload.getResourceId(), ex);
    }
}
