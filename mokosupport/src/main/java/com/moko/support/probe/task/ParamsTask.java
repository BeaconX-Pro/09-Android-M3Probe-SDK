package com.moko.support.probe.task;

import androidx.annotation.IntRange;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.probe.entity.OrderCHAR;
import com.moko.support.probe.entity.ParamsKeyEnum;


public class ParamsTask extends OrderTask {
    public byte[] data;

    public ParamsTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void getData(ParamsKeyEnum key) {
        createGetParamsData(key.getParamsKey());
    }

    public void setData(ParamsKeyEnum key) {
        createSetParamsData(key.getParamsKey());
    }

    private void createGetParamsData(int paramsKey) {
        data = new byte[]{(byte) 0xEA, (byte) paramsKey, (byte) 0x00, (byte) 0x00};
    }

    private void createSetParamsData(int paramsKey) {
        data = new byte[]{(byte) 0xEA, (byte) paramsKey, (byte) 0x01, (byte) 0x00};
    }

    public void setSamplingInterval(@IntRange(from = 1, to = 65535) int interval) {
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.KEY_WRITE_SAMPLING_INTERVAL.getParamsKey(),
                (byte) 0x00,
                (byte) 0x02,
                intervalBytes[0],
                intervalBytes[1]
        };
        response.responseValue = data;
    }

    public void setDetectionInterval(@IntRange(from = 1, to = 86400) int interval) {
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 3);
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.KEY_WRITE_DETECTION_INTERVAL.getParamsKey(),
                (byte) 0x00,
                (byte) 0x03,
                intervalBytes[0],
                intervalBytes[1],
                intervalBytes[2]
        };
        response.responseValue = data;
    }

    public void setButtonPower(int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_BUTTON_POWER.getParamsKey(),
                (byte) 0x00,
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setHWResetEnable(int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_HW_RESET_ENABLE.getParamsKey(),
                (byte) 0x00,
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setTriggerLEDNotifyEnable(int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_TRIGGER_LED_NOTIFICATION.getParamsKey(),
                (byte) 0x00,
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }
}
