package com.sequenceiq.cloudbreak.core.flow2.cluster.certrenew;

import com.sequenceiq.flow.core.FlowState;

public enum  ClusterCertificateRenewState implements FlowState {
    INIT_STATE,
    CLUSTER_CERTIFICATE_REISSUE_STATE,
    CLUSTER_CERTIFICATE_REDEPLOY_STATE,
    CLUSTER_CERTIFICATE_RENEWAL_FINISHED_STATE,
    CLUSTER_CERTIFICATE_RENEW_FAILED_STATE,
    FINAL_STATE;
}
