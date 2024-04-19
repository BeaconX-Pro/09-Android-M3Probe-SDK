package com.moko.support.probe.entity;

import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    KEY_DEVICE_MAC(0x20),
    KEY_READ_SAMPLING_INTERVAL(0x23),
    KEY_WRITE_SAMPLING_INTERVAL(0x33),
    KEY_READ_DETECTION_INTERVAL(0x44),
    KEY_WRITE_DETECTION_INTERVAL(0x54),
    KEY_TRIGGER_LED_STATUS(0x30),
    KEY_BATTERY_VOLTAGE(0x31),
    KEY_NORMAL_ADV_PARAMS(0x35),
    KEY_BUTTON_TRIGGER_PARAMS(0x36),
    KEY_POWER_SAVING_STATIC_TRIGGER_TIME(0x37),
    KEY_SENSOR_TYPE(0x39),
    KEY_PASSWORD(0x51),
    KEY_MODIFY_PASSWORD(0x52),
    GET_BUTTON_POWER(0x28),
    SET_BUTTON_POWER(0x38),
    KEY_VERIFY_PASSWORD_ENABLE(0x53),
    GET_HW_RESET_ENABLE(0x48),
    SET_HW_RESET_ENABLE(0x58),
    GET_TRIGGER_LED_NOTIFICATION(0x47),
    SET_TRIGGER_LED_NOTIFICATION(0x57),
    SET_ERROR(0x0D),
    ;

    private final int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }


    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int paramsKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
