package com.moko.support.probe.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.probe.entity.OrderCHAR;

public class SetAdvTxPowerTask extends OrderTask {

    public byte[] data;

    public SetAdvTxPowerTask() {
        super(OrderCHAR.CHAR_ADV_TX_POWER, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
