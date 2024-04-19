package com.moko.support.probe.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.probe.entity.OrderCHAR;

public class GetAdvSlotTask extends OrderTask {

    public byte[] data;

    public GetAdvSlotTask() {
        super(OrderCHAR.CHAR_ADV_SLOT, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
