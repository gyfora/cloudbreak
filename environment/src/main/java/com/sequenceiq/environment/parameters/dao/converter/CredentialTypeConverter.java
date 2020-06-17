package com.sequenceiq.environment.parameters.dao.converter;

import com.sequenceiq.cloudbreak.converter.DefaultEnumConverter;
import com.sequenceiq.environment.api.v1.credential.model.CredentialType;

public class CredentialTypeConverter extends DefaultEnumConverter<CredentialType> {

    @Override
    public CredentialType getDefault() {
        return CredentialType.ENVIRONMENT;
    }
}
