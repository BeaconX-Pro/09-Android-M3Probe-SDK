package com.moko.support.probe;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoBleManager;
import com.moko.ble.lib.callback.MokoResponseCallback;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.probe.entity.OrderCHAR;
import com.moko.support.probe.entity.OrderServices;

import java.util.UUID;

import no.nordicsemi.android.ble.callback.SuccessCallback;

final class MokoBleConfig extends MokoBleManager {

    private MokoResponseCallback mMokoResponseCallback;
    private BluetoothGattCharacteristic paramsCharacteristic;
    private BluetoothGattCharacteristic paramsResultCharacteristic;
    private BluetoothGattCharacteristic disconnectCharacteristic;
    private BluetoothGattCharacteristic thCharacteristic;
    private BluetoothGattCharacteristic waterLeakCharacteristic;
    private BluetoothGattCharacteristic tempCharacteristic;
    private BluetoothGatt gatt;

    public MokoBleConfig(@NonNull Context context, MokoResponseCallback callback) {
        super(context);
        mMokoResponseCallback = callback;
    }

    @Override
    public boolean checkServiceCharacteristicSupported(BluetoothGatt bluetoothGatt) {
        final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid());
        this.gatt = bluetoothGatt;
        if (service != null) {
            paramsCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS.getUuid());
            paramsResultCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS_RESULT.getUuid());
            disconnectCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_DISCONNECT.getUuid());
            thCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_TH_NOTIFY.getUuid());
            waterLeakCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_WATER_LEAK_NOTIFY.getUuid());
            tempCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_TEMP_NOTIFY.getUuid());
            return null != paramsCharacteristic && null != paramsResultCharacteristic && null != disconnectCharacteristic;
        }
        return false;
    }

    @Override
    public void write(BluetoothGattCharacteristic characteristic, byte[] value) {
        mMokoResponseCallback.onCharacteristicWrite(characteristic, value);
    }

    @Override
    public void read(BluetoothGattCharacteristic characteristic, byte[] value) {
        mMokoResponseCallback.onCharacteristicRead(characteristic, value);
    }

    @Override
    public void init() {
        requestMtu(240).done(device -> {
            enableParamsNotify();
            enableParamsResultNotify();
            enableDisconnectNotify();
        }).enqueue();
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {
        mMokoResponseCallback.onDeviceDisconnected(device, reason);
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
        mMokoResponseCallback.onDeviceDisconnected(device, reason);
    }


    public void enableParamsNotify() {
        setNotificationCallback(paramsCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(paramsCharacteristic, value);
        });
        enableNotifications(paramsCharacteristic).enqueue();
    }

    public void disableParamsNotify() {
        disableNotifications(paramsCharacteristic).enqueue();
    }

    public void enableParamsResultNotify() {
        setNotificationCallback(paramsResultCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(paramsResultCharacteristic, value);
        });
        enableNotifications(paramsResultCharacteristic).enqueue();
    }

    public void disableParamsResultNotify() {
        disableNotifications(paramsResultCharacteristic).enqueue();
    }

    public void enableDisconnectNotify() {
        setNotificationCallback(disconnectCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(disconnectCharacteristic, value);
        });
        enableNotifications(disconnectCharacteristic).done(device -> mMokoResponseCallback.onServicesDiscovered(gatt)).enqueue();
    }

    public void disableDisconnectNotify() {
        disableNotifications(disconnectCharacteristic).enqueue();
    }

    public void enableWaterLeakNotify() {
        setNotificationCallback(waterLeakCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(waterLeakCharacteristic, value);
        });
        enableNotifications(waterLeakCharacteristic).enqueue();
    }

    public void disableWaterLeakNotify() {
        disableNotifications(waterLeakCharacteristic).enqueue();
    }

    public void enableTHNotify() {
        setNotificationCallback(thCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(thCharacteristic, value);
        });
        enableNotifications(thCharacteristic).enqueue();
    }

    public void disableTHNotify() {
        disableNotifications(thCharacteristic).enqueue();
    }

    public void enableTempNotify() {
        setNotificationCallback(tempCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(tempCharacteristic, value);
        });
        enableNotifications(tempCharacteristic).enqueue();
    }

    public void disableTempNotify() {
        disableNotifications(tempCharacteristic).enqueue();
    }
}