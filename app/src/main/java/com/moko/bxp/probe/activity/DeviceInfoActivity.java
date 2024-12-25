package com.moko.bxp.probe.activity;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.probe.AppConstants;
import com.moko.bxp.probe.R;
import com.moko.bxp.probe.databinding.ActivityDeviceInfoProbeBinding;
import com.moko.bxp.probe.dialog.AlertMessageDialog;
import com.moko.bxp.probe.dialog.LoadingMessageDialog;
import com.moko.bxp.probe.dialog.ModifyPasswordDialog;
import com.moko.bxp.probe.fragment.SensorFragment;
import com.moko.bxp.probe.fragment.DeviceFragment;
import com.moko.bxp.probe.fragment.SettingFragment;
import com.moko.bxp.probe.service.DfuServiceProbe;
import com.moko.bxp.probe.utils.FileUtils;
import com.moko.bxp.probe.utils.ToastUtils;
import com.moko.support.probe.ProbeMokoSupport;
import com.moko.support.probe.OrderTaskAssembler;
import com.moko.support.probe.dfu.DfuProgressListener;
import com.moko.support.probe.dfu.DfuProgressListenerAdapter;
import com.moko.support.probe.dfu.DfuServiceInitiator;
import com.moko.support.probe.dfu.DfuServiceListenerHelper;
import com.moko.support.probe.entity.OrderCHAR;
import com.moko.support.probe.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;

    private ActivityDeviceInfoProbeBinding mBind;
    private FragmentManager fragmentManager;
    private SensorFragment sensorFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mPassword;
    public String mDeviceMac;
    private boolean mIsClose;
    private boolean mReceiverTag = false;
    private int mDisconnectType;
    private boolean isModifyPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceInfoProbeBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mPassword = getIntent().getStringExtra(AppConstants.EXTRA_KEY_PASSWORD);
        fragmentManager = getFragmentManager();
        initFragment();
        mBind.rgOptions.setOnCheckedChangeListener(this);
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!ProbeMokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            ProbeMokoSupport.getInstance().enableBluetooth();
        }
        showSyncingProgressDialog();
        mBind.tvTitle.postDelayed(() -> {
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.getDeviceMac());
            orderTasks.add(OrderTaskAssembler.getLockState());
            ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        }, 500);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                if (mIsClose) return;
                if (mDisconnectType > 0) return;
                if (ProbeMokoSupport.getInstance().isBluetoothOpen()) {
                    if (isUpgrading) {
                        mBind.tvTitle.postDelayed(this::dismissDFUProgressDialog, 2000);
                    } else {
                        AlertMessageDialog dialog = new AlertMessageDialog();
                        dialog.setTitle("Dismiss");
                        dialog.setMessage("The device disconnected!");
                        dialog.setConfirm("Exit");
                        dialog.setCancelGone();
                        dialog.setOnAlertConfirmListener(() -> {
                            setResult(RESULT_OK);
                            finish();
                        });
                        dialog.show(getSupportFragmentManager());
                    }
                }
            }
        });
    }

    private String unLockResponse;

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_DISCONNECT) {
                    if (value.length >= 1) {
                        mDisconnectType = value[0] & 0xff;
                        if (mDisconnectType == 1 && isModifyPassword) {
                            isModifyPassword = false;
                            dismissSyncProgressDialog();
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setMessage("Modify password success!\nPlease reconnect the Device.");
                            dialog.setCancelGone();
                            dialog.setConfirm(R.string.ok);
                            dialog.setOnAlertConfirmListener(() -> {
                                setResult(RESULT_OK);
                                finish();
                            });
                            dialog.show(getSupportFragmentManager());
                        } else if (mDisconnectType == 2) {
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setMessage("Reset success!\nBeacon is disconnected.");
                            dialog.setCancelGone();
                            dialog.setConfirm(R.string.ok);
                            dialog.setOnAlertConfirmListener(() -> {
                                setResult(RESULT_OK);
                                finish();
                            });
                            dialog.show(getSupportFragmentManager());
                        }
                    }
                }
            }
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        if (value.length > 3) {
                            int header = value[0] & 0xFF;// 0xEB
                            int cmd = value[1] & 0xFF;
                            if (header != 0xEB) return;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) return;
                            int length = MokoUtils.toInt(Arrays.copyOfRange(value, 2, 4));
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_DEVICE_MAC) {
                                if (length == 6) {
                                    String mac = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 10));
                                    StringBuilder stringBuffer = new StringBuilder(mac);
                                    stringBuffer.insert(2, ":");
                                    stringBuffer.insert(5, ":");
                                    stringBuffer.insert(8, ":");
                                    stringBuffer.insert(11, ":");
                                    stringBuffer.insert(14, ":");
                                    mDeviceMac = stringBuffer.toString().toUpperCase();
                                    deviceFragment.setMacAddress(mDeviceMac);
                                }
                            }
                        }
                        break;

                    case CHAR_MODEL_NUMBER:
                        deviceFragment.setProductMode(new String(value).trim());
                        break;

                    case CHAR_SOFTWARE_REVISION:
                        deviceFragment.setSoftwareVersion(new String(value).trim());
                        break;

                    case CHAR_FIRMWARE_REVISION:
                        deviceFragment.setFirmwareVersion(new String(value).trim());
                        break;

                    case CHAR_HARDWARE_REVISION:
                        deviceFragment.setHardwareVersion(new String(value).trim());
                        break;

                    case CHAR_SERIAL_NUMBER:
                        deviceFragment.setProductDate(new String(value).trim());
                        break;

                    case CHAR_MANUFACTURER_NAME:
                        deviceFragment.setManufacturer(new String(value).trim());
                        break;
                    case CHAR_BATTERY:
                        deviceFragment.setBattery(MokoUtils.toInt(value));
                        break;
                    case CHAR_LOCK_STATE:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            int enable = MokoUtils.toInt(value);
                            settingFragment.setPwdShown(!TextUtils.isEmpty(mPassword));
                            settingFragment.setResetShown(enable);
                        }
                        break;
                    case CHAR_UNLOCK:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            unLockResponse = MokoUtils.bytesToHexString(value);
                            XLog.i("返回的随机数：" + unLockResponse);
                            showSyncingProgressDialog();
                            ProbeMokoSupport.getInstance().sendOrder(OrderTaskAssembler.setUnLock(mPassword, value));
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            ProbeMokoSupport.getInstance().sendOrder(OrderTaskAssembler.getLockState());
                        }
                        break;
                }
            }
        });
    }

    private void getDeviceInfo() {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.getBattery());
        orderTasks.add(OrderTaskAssembler.getDeviceModel());
        orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
        orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        orderTasks.add(OrderTaskAssembler.getProductDate());
        orderTasks.add(OrderTaskAssembler.getManufacturer());
        ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                        dismissSyncProgressDialog();
                        AlertMessageDialog dialog = new AlertMessageDialog();
                        dialog.setTitle("Dismiss");
                        dialog.setCancelGone();
                        dialog.setMessage("The current system of bluetooth is not available!");
                        dialog.setConfirm(R.string.ok);
                        dialog.setOnAlertConfirmListener(() -> finish());
                        dialog.show(getSupportFragmentManager());
                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_FIRMWARE) {
            if (resultCode == RESULT_OK) {
                //得到uri，后面就是将uri转化成file的过程。
                Uri uri = data.getData();
                String firmwareFilePath = FileUtils.getPath(this, uri);
                if (TextUtils.isEmpty(firmwareFilePath)) return;
                final File firmwareFile = new File(firmwareFilePath);
                if (firmwareFile.exists()) {
                    final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceMac)
                            .setKeepBond(false)
                            .setDisableNotification(true);
                    starter.setZip(null, firmwareFilePath);
                    starter.start(this, DfuServiceProbe.class);
                    showDFUProgressDialog("Waiting...");
                } else {
                    Toast.makeText(this, "file is not exists!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    private void back() {
        ProbeMokoSupport.getInstance().disConnectBle();
        mIsClose = false;
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void initFragment() {
        sensorFragment = SensorFragment.newInstance();
        settingFragment = SettingFragment.newInstance();
        deviceFragment = DeviceFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, sensorFragment)
                .add(R.id.frame_container, settingFragment)
                .add(R.id.frame_container, deviceFragment)
                .show(sensorFragment)
                .hide(settingFragment)
                .hide(deviceFragment)
                .commit();
    }

    private void showSlotFragment() {
        if (sensorFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(settingFragment)
                    .hide(deviceFragment)
                    .show(sensorFragment)
                    .commit();
        }
        mBind.tvTitle.setText("SENSOR");
    }

    private void showSettingFragment() {
        if (settingFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(sensorFragment)
                    .hide(deviceFragment)
                    .show(settingFragment)
                    .commit();
        }
        mBind.tvTitle.setText(getString(R.string.setting_title));
    }

    private void showDeviceFragment() {
        if (deviceFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(sensorFragment)
                    .hide(settingFragment)
                    .show(deviceFragment)
                    .commit();
        }
        mBind.tvTitle.setText(getString(R.string.device_title));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.radioBtn_sensor) {
            showSlotFragment();
        } else if (checkedId == R.id.radioBtn_setting) {
            showSettingFragment();
        } else if (checkedId == R.id.radioBtn_device) {
            showDeviceFragment();
            getDeviceInfo();
        }
    }


    public void modifyPassword(String password) {
        isModifyPassword = true;
        showSyncingProgressDialog();
        ProbeMokoSupport.getInstance().sendOrder(OrderTaskAssembler.setLockState(password));
    }

    public void resetDevice() {
        showSyncingProgressDialog();
        ProbeMokoSupport.getInstance().sendOrder(OrderTaskAssembler.resetDevice());
    }

    public void chooseFirmwareFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_SELECT_FIRMWARE);
        } catch (ActivityNotFoundException ex) {
            ToastUtils.showToast(this, "install file manager app");
        }
    }

    public void onBack(View view) {
        back();
    }

    public void onTempProbe(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, SensorProbeActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SENSOR_TYPE, AppConstants.SENSOR_TYPE_TEMP);
        startActivity(intent);
    }

    public void onTHProbe(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, SensorProbeActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SENSOR_TYPE, AppConstants.SENSOR_TYPE_TH);
        startActivity(intent);
    }

    public void onWaterLeakProbe(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, SensorProbeActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SENSOR_TYPE, AppConstants.SENSOR_TYPE_WATER_LEAK);
        startActivity(intent);
    }


    public void onSensor(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, SensorConfigActivity.class);
        startActivity(intent);
    }

    public void onAdv(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, AdvConfigActivity.class);
        startActivity(intent);
    }

    public void onQuickSwitch(View view) {
        if (isWindowLocked()) return;
        quickSwitchLauncher.launch(new Intent(this, QuickSwitchActivity.class));
    }

    private ActivityResultLauncher<Intent> quickSwitchLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onQuickSwitchResult);

    private void onQuickSwitchResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            boolean enablePasswordVerify = result.getData().getBooleanExtra(AppConstants.EXTRA_KEY_PASSWORD_VERIFICATION, false);
            settingFragment.setPwdShown(enablePasswordVerify ? !TextUtils.isEmpty(mPassword) : false);
            settingFragment.setResetShown(enablePasswordVerify ? 1 : 0);
        }
    }

    public void onResetBeacon(View view) {
        if (isWindowLocked()) return;
        AlertMessageDialog resetDeviceDialog = new AlertMessageDialog();
        resetDeviceDialog.setTitle("Warning！");
        resetDeviceDialog.setMessage("Are you sure to reset the Beacon？");
        resetDeviceDialog.setConfirm(R.string.ok);
        resetDeviceDialog.setOnAlertConfirmListener(this::resetDevice);
        resetDeviceDialog.show(getSupportFragmentManager());
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(DeviceInfoActivity.this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
        AlertMessageDialog dialog = new AlertMessageDialog();
        if (isUpgradeCompleted) {
            dialog.setMessage("DFU Successfully!\nPlease reconnect the device.");
        } else {
            dialog.setMessage("Opps!DFU Failed.\nPlease try again!");
        }
        dialog.setCancelGone();
        dialog.setConfirm(R.string.ok);
        dialog.setOnAlertConfirmListener(() -> {
            isUpgrading = false;
            setResult(RESULT_OK);
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    private int mDeviceConnectCount;
    private boolean isUpgrading;
    private boolean isUpgradeCompleted;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            XLog.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                ToastUtils.showToast(DeviceInfoActivity.this, "Error:DFU Failed");
                ProbeMokoSupport.getInstance().disConnectBle();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceInfoActivity.this);
                final Intent abortAction = new Intent(DfuServiceProbe.BROADCAST_ACTION);
                abortAction.putExtra(DfuServiceProbe.EXTRA_ACTION, DfuServiceProbe.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            XLog.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(@NonNull String deviceAddress) {
            isUpgrading = true;
            mDFUDialog.setMessage("DfuProcessStarting...");
        }

        @Override
        public void onEnablingDfuMode(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(@NonNull String deviceAddress) {
            XLog.w("onDfuCompleted...");
            isUpgradeCompleted = true;
        }

        @Override
        public void onDfuAborted(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(@NonNull String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            String progress = String.format("Progress:%d%%", percent);
            XLog.i(progress);
            mDFUDialog.setMessage(progress);
        }

        @Override
        public void onError(@NonNull String deviceAddress, int error, int errorType, String message) {
            XLog.i("DFU Error:" + message + error);
            dismissDFUProgressDialog();
        }
    };

    public void onDFU(View view) {
        if (isWindowLocked()) return;
        chooseFirmwareFile();
    }

    public void onModifyPassword(View view) {
        if (isWindowLocked()) return;
        final ModifyPasswordDialog modifyPasswordDialog = new ModifyPasswordDialog();
        modifyPasswordDialog.setOnModifyPasswordClicked(new ModifyPasswordDialog.ModifyPasswordClickListener() {
            @Override
            public void onEnsureClicked(String password) {
                modifyPassword(password);
            }

            @Override
            public void onPasswordNotMatch() {
                AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setMessage("Password do not match!\nPlease try again.");
                dialog.setConfirm(R.string.ok);
                dialog.setCancelGone();
                dialog.show(getSupportFragmentManager());
            }
        });
        modifyPasswordDialog.show(getSupportFragmentManager());
    }
}
