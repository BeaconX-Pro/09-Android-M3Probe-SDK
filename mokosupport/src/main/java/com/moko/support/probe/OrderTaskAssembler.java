package com.moko.support.probe;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.probe.entity.ParamsKeyEnum;
import com.moko.support.probe.task.GetAdvIntervalTask;
import com.moko.support.probe.task.GetAdvSlotDataTask;
import com.moko.support.probe.task.GetBatteryTask;
import com.moko.support.probe.task.GetConnectableTask;
import com.moko.support.probe.task.GetFirmwareRevisionTask;
import com.moko.support.probe.task.GetHardwareRevisionTask;
import com.moko.support.probe.task.GetLockStateTask;
import com.moko.support.probe.task.GetManufacturerNameTask;
import com.moko.support.probe.task.GetModelNumberTask;
import com.moko.support.probe.task.GetTxPowerTask;
import com.moko.support.probe.task.GetSerialNumberTask;
import com.moko.support.probe.task.GetSoftwareRevisionTask;
import com.moko.support.probe.task.GetUnlockTask;
import com.moko.support.probe.task.ParamsTask;
import com.moko.support.probe.task.ResetDeviceTask;
import com.moko.support.probe.task.SetAdvIntervalTask;
import com.moko.support.probe.task.SetAdvSlotDataTask;
import com.moko.support.probe.task.SetConnectableTask;
import com.moko.support.probe.task.SetLockStateTask;
import com.moko.support.probe.task.SetTxPowerTask;
import com.moko.support.probe.task.SetUnlockTask;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import androidx.annotation.IntRange;

public class OrderTaskAssembler {

    /**
     * @Description 获取设备锁状态get lock state
     */
    public static OrderTask getLockState() {
        GetLockStateTask task = new GetLockStateTask();
        return task;
    }

    /**
     * @Description 设置设备锁方式
     */
    public static OrderTask setLockStateDirected(int enable) {
        SetLockStateTask task = new SetLockStateTask();
        task.setData(MokoUtils.toByteArray(enable, 1));
        return task;
    }

    /**
     * @Description 设置设备锁状态set lock state
     */
    public static OrderTask setLockState(String newPassword) {
        if (passwordBytes != null) {
            XLog.i("旧密码：" + MokoUtils.bytesToHexString(passwordBytes));
            byte[] bt1 = newPassword.getBytes();
            byte[] newPasswordBytes = new byte[16];
            for (int i = 0; i < newPasswordBytes.length; i++) {
                if (i < bt1.length) {
                    newPasswordBytes[i] = bt1[i];
                } else {
                    newPasswordBytes[i] = (byte) 0xff;
                }
            }
            XLog.i("新密码：" + MokoUtils.bytesToHexString(newPasswordBytes));
            // 用旧密码加密新密码
            byte[] newPasswordEncryptBytes = encrypt(newPasswordBytes, passwordBytes);
            if (newPasswordEncryptBytes != null) {
                SetLockStateTask task = new SetLockStateTask();
                byte[] unLockBytes = new byte[newPasswordEncryptBytes.length + 1];
                unLockBytes[0] = 0;
                System.arraycopy(newPasswordEncryptBytes, 0, unLockBytes, 1, newPasswordEncryptBytes.length);
                task.setData(unLockBytes);
                return task;
            }
        }
        return null;
    }

    /**
     * @Description 获取解锁加密内容get unlock
     */
    public static OrderTask getUnLock() {
        GetUnlockTask task = new GetUnlockTask();
        return task;
    }

    private static byte[] passwordBytes;

    /**
     * @Description 解锁set unlock
     */
    public static OrderTask setUnLock(String password, byte[] value) {
        byte[] bt1 = password.getBytes();
        passwordBytes = new byte[16];
        for (int i = 0; i < passwordBytes.length; i++) {
            if (i < bt1.length) {
                passwordBytes[i] = bt1[i];
            } else {
                passwordBytes[i] = (byte) 0xff;
            }
        }
        XLog.i("密码：" + MokoUtils.bytesToHexString(passwordBytes));
        byte[] unLockBytes = encrypt(value, passwordBytes);
        if (unLockBytes != null) {
            SetUnlockTask task = new SetUnlockTask();
            task.setData(unLockBytes);
            return task;
        }
        return null;
    }

    /**
     * @Date 2018/1/22
     * @Author wenzheng.liu
     * @Description 加密
     */
    public static byte[] encrypt(byte[] value, byte[] password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password, "AES");// 转换为AES专用密钥
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化为加密模式的密码器
            byte[] result = cipher.doFinal(value);// 加密
            byte[] data = Arrays.copyOf(result, 16);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @Description 获取电池电量
     */
    public static OrderTask getBattery() {
        GetBatteryTask task = new GetBatteryTask();
        return task;
    }

    /**
     * 获取制造商
     */
    public static OrderTask getManufacturer() {
        return new GetManufacturerNameTask();
    }

    /**
     * 获取设备型号
     */
    public static OrderTask getDeviceModel() {
        return new GetModelNumberTask();
    }

    /**
     * 获取生产日期
     */
    public static OrderTask getProductDate() {
        return new GetSerialNumberTask();
    }

    /**
     * 获取硬件版本
     */
    public static OrderTask getHardwareVersion() {
        return new GetHardwareRevisionTask();
    }

    /**
     * 获取固件版本
     */
    public static OrderTask getFirmwareVersion() {
        return new GetFirmwareRevisionTask();
    }

    /**
     * 获取软件版本
     */
    public static OrderTask getSoftwareVersion() {
        return new GetSoftwareRevisionTask();
    }


    public static OrderTask getDeviceMac() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DEVICE_MAC);
        return task;
    }

    public static OrderTask getSamplingInterval() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_READ_SAMPLING_INTERVAL);
        return task;
    }

    public static OrderTask setSamplingInterval(@IntRange(from = 1, to = 65535) int interval) {
        ParamsTask task = new ParamsTask();
        task.setSamplingInterval(interval);
        return task;
    }

    public static OrderTask getDetectionInterval() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_READ_DETECTION_INTERVAL);
        return task;
    }

    public static OrderTask setDetectionInterval(@IntRange(from = 1, to = 86400) int interval) {
        ParamsTask task = new ParamsTask();
        task.setDetectionInterval(interval);
        return task;
    }


    public static OrderTask resetDevice() {
        ResetDeviceTask task = new ResetDeviceTask();
        return task;
    }

    public static OrderTask getSensorType() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_SENSOR_TYPE);
        return task;
    }

    public static OrderTask getAdvInterval() {
        GetAdvIntervalTask task = new GetAdvIntervalTask();
        return task;
    }

    public static OrderTask setAdvInterval(byte[] data) {
        SetAdvIntervalTask task = new SetAdvIntervalTask();
        task.setData(data);
        return task;
    }

    public static OrderTask getTxPower() {
        GetTxPowerTask task = new GetTxPowerTask();
        return task;
    }

    public static OrderTask setTxPower(byte[] data) {
        SetTxPowerTask task = new SetTxPowerTask();
        task.setData(data);
        return task;
    }


    public static OrderTask getSlotData() {
        GetAdvSlotDataTask task = new GetAdvSlotDataTask();
        return task;
    }


    public static OrderTask setSlotData(byte[] data) {
        SetAdvSlotDataTask task = new SetAdvSlotDataTask();
        task.setData(data);
        return task;
    }


    public static OrderTask getConnectable() {
        GetConnectableTask task = new GetConnectableTask();
        return task;
    }


    public static OrderTask setConnectable(int enable) {
        SetConnectableTask task = new SetConnectableTask();
        task.setData(MokoUtils.toByteArray(enable, 1));
        return task;
    }


    public static OrderTask getButtonPower() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.GET_BUTTON_POWER);
        return task;
    }


    public static OrderTask setButtonPower(int enable) {
        ParamsTask task = new ParamsTask();
        task.setButtonPower(enable);
        return task;
    }

    public static OrderTask getHWResetEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.GET_HW_RESET_ENABLE);
        return task;
    }

    public static OrderTask setHWResetEnable(int enable) {
        ParamsTask task = new ParamsTask();
        task.setHWResetEnable(enable);
        return task;
    }

    public static OrderTask getTriggerLEDNotifyEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.GET_TRIGGER_LED_NOTIFICATION);
        return task;
    }


    public static OrderTask setTriggerLEDNotifyEnable(int enable) {
        ParamsTask task = new ParamsTask();
        task.setTriggerLEDNotifyEnable(enable);
        return task;
    }

}
