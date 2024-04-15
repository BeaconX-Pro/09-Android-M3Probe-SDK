package com.moko.support.probe.service;

import com.moko.support.probe.entity.DeviceInfo;

public interface DeviceInfoAnalysis<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
