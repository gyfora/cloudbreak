package com.sequenceiq.environment.environment.experience.dwx;

import com.sequenceiq.environment.environment.experience.common.responses.CpInternalEnvironmentResponse;

import javax.validation.constraints.NotNull;

public interface DWXApi {

    CpInternalEnvironmentResponse listClustersByEnvCrn(@NotNull String envCrn);

}
