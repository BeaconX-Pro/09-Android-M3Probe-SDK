package com.moko.support.probe.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.probe.entity.OrderCHAR;

public class GetBatteryTask extends OrderTask {

    public byte[] data;

    public GetBatteryTask() {
        super(OrderCHAR.CHAR_BATTERY, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
