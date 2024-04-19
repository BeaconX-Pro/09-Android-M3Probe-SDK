package com.moko.support.probe.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.probe.entity.OrderCHAR;

public class SetTxPowerTask extends OrderTask {

    public byte[] data;

    public SetTxPowerTask() {
        super(OrderCHAR.CHAR_TX_POWER, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
