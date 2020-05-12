package com.sequenceiq.datalake.flow.datalake.upgrade.event;

import com.sequenceiq.datalake.flow.SdxEvent;

public class DatalakeUpgradeStartEvent extends SdxEvent {

    private final String imageId;

    private final boolean repairAfterUpgrade;

    public DatalakeUpgradeStartEvent(String selector, Long sdxId, String userId, String imageId, boolean repairAfterUpgrade) {
        super(selector, sdxId, userId);
        this.imageId = imageId;
        this.repairAfterUpgrade = repairAfterUpgrade;
    }

    public String getImageId() {
        return imageId;
    }

    public boolean isRepairAfterUpgrade() {
        return repairAfterUpgrade;
    }

    @Override
    public String selector() {
        return "DatalakeUpgradeStartEvent";
    }
}
