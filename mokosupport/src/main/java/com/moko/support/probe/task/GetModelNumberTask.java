package com.moko.support.probe.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.probe.entity.OrderCHAR;

public class GetModelNumberTask extends OrderTask {

    public byte[] data;

    public GetModelNumberTask() {
        super(OrderCHAR.CHAR_MODEL_NUMBER, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
