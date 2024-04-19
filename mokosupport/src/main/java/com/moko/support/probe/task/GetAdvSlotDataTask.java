package com.moko.support.probe.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.probe.entity.OrderCHAR;

public class GetAdvSlotDataTask extends OrderTask {

    public byte[] data;

    public GetAdvSlotDataTask() {
        super(OrderCHAR.CHAR_ADV_SLOT_DATA, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
