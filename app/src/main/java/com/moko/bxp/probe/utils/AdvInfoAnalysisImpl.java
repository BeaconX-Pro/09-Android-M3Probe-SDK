package com.moko.bxp.probe.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;
import android.text.TextUtils;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.probe.entity.AdvInfo;
import com.moko.support.probe.entity.DeviceInfo;
import com.moko.support.probe.entity.OrderServices;
import com.moko.support.probe.service.DeviceInfoAnalysis;

import java.util.Arrays;
import java.util.HashMap;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class AdvInfoAnalysisImpl implements DeviceInfoAnalysis<AdvInfo> {
    private final HashMap<String, AdvInfo> advInfoHashMap;

    public AdvInfoAnalysisImpl() {
        this.advInfoHashMap = new HashMap<>();
    }

    @Override
    public AdvInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        if (null == record) return null;
        byte[] bytes = record.getServiceData(new ParcelUuid(OrderServices.SERVICE_ADV.getUuid()));
        if (bytes.length != 12) return null;
        int frameType = bytes[0] & 0xFF; // 0x80 M3 Probe
        int waterLeakage = bytes[1];
        int temperature = MokoUtils.byte2short(Arrays.copyOfRange(bytes, 2, 4));
        int humidity = MokoUtils.toInt(Arrays.copyOfRange(bytes, 4, 6));
        int tofRanging = MokoUtils.toInt(Arrays.copyOfRange(bytes, 6, 8));
        // avoid repeat
        AdvInfo advInfo;
        if (advInfoHashMap.containsKey(deviceInfo.mac)) {
            advInfo = advInfoHashMap.get(deviceInfo.mac);
            if (!TextUtils.isEmpty(deviceInfo.name)) {
                advInfo.name = deviceInfo.name;
            }
            advInfo.rssi = deviceInfo.rssi;
            advInfo.waterLeakage = waterLeakage;
            advInfo.temperature = temperature;
            advInfo.humidity = humidity;
            advInfo.tofRanging = tofRanging;

            if (result.isConnectable())
                advInfo.connectState = 1;
            advInfo.scanRecord = deviceInfo.scanRecord;
            long currentTime = SystemClock.elapsedRealtime();
            long intervalTime = currentTime - advInfo.scanTime;
            advInfo.intervalTime = intervalTime;
            advInfo.scanTime = currentTime;
        } else {
            advInfo = new AdvInfo();
            advInfo.name = deviceInfo.name;
            advInfo.mac = deviceInfo.mac;
            advInfo.rssi = deviceInfo.rssi;
            advInfo.waterLeakage = waterLeakage;
            advInfo.temperature = temperature;
            advInfo.humidity = humidity;
            advInfo.tofRanging = tofRanging;

            if (result.isConnectable()) {
                advInfo.connectState = 1;
            } else {
                advInfo.connectState = 0;
            }
            advInfo.scanRecord = deviceInfo.scanRecord;
            advInfo.scanTime = SystemClock.elapsedRealtime();
            advInfoHashMap.put(deviceInfo.mac, advInfo);
        }
        return advInfo;
    }
}
