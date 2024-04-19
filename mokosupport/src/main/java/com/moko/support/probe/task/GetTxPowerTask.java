package com.moko.support.probe.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.probe.entity.OrderCHAR;

public class GetTxPowerTask extends OrderTask {

    public byte[] data;

    public GetTxPowerTask() {
        super(OrderCHAR.CHAR_TX_POWER, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
