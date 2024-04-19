package com.moko.support.probe.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.probe.entity.OrderCHAR;

public class GetConnectableTask extends OrderTask {

    public byte[] data;

    public GetConnectableTask() {
        super(OrderCHAR.CHAR_CONNECTABLE, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
