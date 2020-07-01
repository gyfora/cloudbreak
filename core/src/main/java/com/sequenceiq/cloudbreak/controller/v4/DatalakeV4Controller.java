package com.sequenceiq.cloudbreak.controller.v4;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import com.sequenceiq.authorization.annotation.InternalOnly;
import com.sequenceiq.cloudbreak.api.endpoint.v4.common.StackType;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.DatalakeV4Endpoint;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.StackViewV4Responses;
import com.sequenceiq.cloudbreak.auth.security.internal.InternalReady;
import com.sequenceiq.cloudbreak.auth.security.internal.TenantAwareParam;
import com.sequenceiq.cloudbreak.service.workspace.WorkspaceService;
import com.sequenceiq.distrox.v1.distrox.StackOperations;

@Controller
@InternalOnly
@InternalReady
public class DatalakeV4Controller implements DatalakeV4Endpoint {

    @Lazy
    @Inject
    private StackOperations stackOperations;

    @Inject
    private WorkspaceService workspaceService;

    @Override
    public StackViewV4Responses list(@NotNull @TenantAwareParam String environmentCrn) {
        return stackOperations.listByEnvironmentCrn(workspaceService.getForCurrentUser().getId(), environmentCrn, List.of(StackType.DATALAKE));
    }
}
