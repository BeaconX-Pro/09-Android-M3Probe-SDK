package com.moko.bxp.probe;

public class AppConstants {
    // data time pattern
    public static final String PATTERN_HH_MM = "HH:mm";
    public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String PATTERN_MM_DD = "MM/dd";
    public static final String PATTERN_MM_DD_2 = "MM-dd";
    public static final String PATTERN_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String PATTERN_YYYY_MM_DD_HH_MM_SS = "dd/MM/yyyy HH:mm:ss";
    public static final String PATTERN_YYYY_MM_DD_T_HH_MM_SS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    // sp
    public static final String SP_NAME = "sp_name_mk_probe";

    public static final String SP_KEY_DEVICE_ADDRESS = "sp_key_device_address";
    public static final String SP_KEY_SAVED_PASSWORD = "SP_KEY_SAVED_PASSWORD";
    // extra_key
    // 设备列表
    public static final String EXTRA_KEY_RESPONSE_ORDER_TYPE = "EXTRA_KEY_RESPONSE_ORDER_TYPE";
    public static final String EXTRA_KEY_SLOT_TYPE = "EXTRA_KEY_SLOT_TYPE";
    public static final String EXTRA_KEY_SLOT_DATA = "EXTRA_KEY_SLOT_DATA";
    public static final String EXTRA_KEY_PASSWORD = "EXTRA_KEY_PASSWORD";
    public static final String EXTRA_KEY_SENSOR_TYPE = "EXTRA_KEY_SENSOR_TYPE";
    public static final String EXTRA_KEY_TRIGGER_TYPE = "EXTRA_KEY_TRIGGER_TYPE";
    public static final String EXTRA_KEY_TRIGGER_DATA = "EXTRA_KEY_TRIGGER_DATA";
    public static final String EXTRA_KEY_PASSWORD_VERIFICATION = "EXTRA_KEY_PASSWORD_VERIFICATION";

    // request_code
    public static final int REQUEST_CODE_DEVICE_INFO = 0x10;
    public static final int REQUEST_CODE_SLOT_DATA = 100;
    public static final int REQUEST_CODE_ENABLE_BT = 1001;
    public static final int REQUEST_CODE_QUICK_SWITCH = 1002;
    public static final int REQUEST_CODE_ALARM_MODE = 1003;


    public static final int REQUEST_CODE_PERMISSION = 120;
    public static final int REQUEST_CODE_PERMISSION_2 = 121;
    public static final int REQUEST_CODE_LOCATION_SETTINGS = 122;
    public static final int PERMISSION_REQUEST_CODE = 1;

    // result_code
    public static final int RESULT_CONN_DISCONNECTED = 2;
    public static final int SENSOR_TYPE_TEMP = 0;
    public static final int SENSOR_TYPE_TH = 1;
    public static final int SENSOR_TYPE_WATER_LEAK = 2;
}
