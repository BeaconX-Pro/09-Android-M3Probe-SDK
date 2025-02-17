package com.moko.support.probe;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoBleLib;
import com.moko.ble.lib.MokoBleManager;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.support.probe.entity.ExportData;
import com.moko.support.probe.entity.OrderCHAR;
import com.moko.support.probe.handler.MokoCharacteristicHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ProbeMokoSupport extends MokoBleLib {
    private HashMap<OrderCHAR, BluetoothGattCharacteristic> mCharacteristicMap;

    private static volatile ProbeMokoSupport INSTANCE;

    private Context mContext;

    private MokoBleConfig mBleConfig;

    private ProbeMokoSupport() {
        //no instance
    }

    public static ProbeMokoSupport getInstance() {
        if (INSTANCE == null) {
            synchronized (ProbeMokoSupport.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ProbeMokoSupport();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context;
        super.init(context);
    }


    @Override
    public MokoBleManager getMokoBleManager() {
        mBleConfig = new MokoBleConfig(mContext, this);
        return mBleConfig;
    }

    ///////////////////////////////////////////////////////////////////////////
    // connect
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeviceConnected(BluetoothGatt gatt) {
        mCharacteristicMap = new MokoCharacteristicHandler().getCharacteristics(gatt);
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCOVER_SUCCESS);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCONNECTED);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public BluetoothGattCharacteristic getCharacteristic(Enum orderCHAR) {
        return mCharacteristicMap.get(orderCHAR);
    }

    ///////////////////////////////////////////////////////////////////////////
    // order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isCHARNull() {
        if (mCharacteristicMap == null || mCharacteristicMap.isEmpty()) {
            disConnectBle();
            return true;
        }
        return false;
    }

    @Override
    public void orderFinish() {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_FINISH);
        EventBus.getDefault().post(event);
    }

    @Override
    public void orderTimeout(OrderTaskResponse response) {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_TIMEOUT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }

    @Override
    public void orderResult(OrderTaskResponse response) {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_RESULT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }

    @Override
    public boolean orderResponseValid(BluetoothGattCharacteristic characteristic, OrderTask orderTask) {
        final UUID responseUUID = characteristic.getUuid();
        final OrderCHAR orderCHAR = (OrderCHAR) orderTask.orderCHAR;
        if (responseUUID.equals(OrderCHAR.CHAR_PARAMS_RESULT.getUuid()))
            return true;
        return responseUUID.equals(orderCHAR.getUuid());
    }


    @Override
    public boolean orderNotify(BluetoothGattCharacteristic characteristic, byte[] value) {
        final UUID responseUUID = characteristic.getUuid();
        OrderCHAR orderCHAR = null;
        if (responseUUID.equals(OrderCHAR.CHAR_DISCONNECT.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_DISCONNECT;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_TEMP_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_TEMP_NOTIFY;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_TH_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_TH_NOTIFY;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_WATER_LEAK_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_WATER_LEAK_NOTIFY;
        }
        if (orderCHAR == null)
            return false;
        XLog.i(orderCHAR.name());
        OrderTaskResponse response = new OrderTaskResponse();
        response.orderCHAR = orderCHAR;
        response.responseValue = value;
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_CURRENT_DATA);
        event.setResponse(response);
        EventBus.getDefault().post(event);
        return true;
    }

    public void enableTHNotify() {
        if (mBleConfig != null)
            mBleConfig.enableTHNotify();
    }

    public void disableTHNotify() {
        if (mBleConfig != null)
            mBleConfig.disableTHNotify();
    }

    public void enableTempNotify() {
        if (mBleConfig != null)
            mBleConfig.enableTempNotify();
    }

    public void disableTempNotify() {
        if (mBleConfig != null)
            mBleConfig.disableTempNotify();
    }

    public void enableWaterLeakNotify() {
        if (mBleConfig != null)
            mBleConfig.enableWaterLeakNotify();
    }

    public void disableWaterLeakNotify() {
        if (mBleConfig != null)
            mBleConfig.disableWaterLeakNotify();
    }
}
