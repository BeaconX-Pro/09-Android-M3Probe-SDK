package com.moko.bxp.probe.entity;

import java.io.Serializable;

public class AdvInfo implements Serializable {


    public String name;
    public int rssi;
    public String mac;
    public String scanRecord;
    public long intervalTime;
    public long scanTime;
    public int waterLeakage;
    public int connectState;

    public int temperature;
    public int humidity;
    public int tofRanging;

    @Override
    public String toString() {
        return "AdvInfo{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }
}
