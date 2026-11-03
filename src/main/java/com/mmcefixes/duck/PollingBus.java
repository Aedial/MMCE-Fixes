package com.mmcefixes.duck;

import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;

import com.mmcefixes.polling.MEBusPolling;


public interface PollingBus {
    public MEBusPolling mmcefixes$getPolling();

    public boolean mmcefixes$hasWork();

    public GuiType mmcefixes$getMainGui();
}
