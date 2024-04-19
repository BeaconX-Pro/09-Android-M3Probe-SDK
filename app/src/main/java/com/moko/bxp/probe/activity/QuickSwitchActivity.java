package com.moko.bxp.probe.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.probe.AppConstants;
import com.moko.bxp.probe.R;
import com.moko.bxp.probe.databinding.ActivityQuickSwitchProbeBinding;
import com.moko.bxp.probe.dialog.AlertMessageDialog;
import com.moko.bxp.probe.dialog.LoadingMessageDialog;
import com.moko.bxp.probe.utils.ToastUtils;
import com.moko.support.probe.ProbeMokoSupport;
import com.moko.support.probe.OrderTaskAssembler;
import com.moko.support.probe.entity.OrderCHAR;
import com.moko.support.probe.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class QuickSwitchActivity extends BaseActivity {
    private ActivityQuickSwitchProbeBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityQuickSwitchProbeBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);

        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getConnectable());
        orderTasks.add(OrderTaskAssembler.getTriggerLEDNotifyEnable());
        orderTasks.add(OrderTaskAssembler.getButtonPower());
        orderTasks.add(OrderTaskAssembler.getHWResetEnable());
        orderTasks.add(OrderTaskAssembler.getLockState());
        ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
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
                        if (value.length >= 2) {
                            int key = value[1] & 0xff;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(key);
                            if (configKeyEnum == null) {
                                return;
                            }
                            switch (configKeyEnum) {
                                case GET_BUTTON_POWER:
                                    if (value.length >= 5) {
                                        int enable = value[4] & 0xFF;
                                        setButtonPower(enable);
                                    }
                                    break;
                                case GET_HW_RESET_ENABLE:
                                    if (value.length >= 4) {
                                        mBind.cvHwReset.setVisibility(View.VISIBLE);
                                        int enable = value[4] & 0xFF;
                                        setHWResetEnable(enable);
                                    }
                                    break;
                                case GET_TRIGGER_LED_NOTIFICATION:
                                    if (value.length >= 4) {
                                        mBind.cvTriggerLedNotify.setVisibility(View.VISIBLE);
                                        int enable = value[4] & 0xFF;
                                        setTriggerLEDNotifyEnable(enable);
                                    }
                                    break;
                                case SET_BUTTON_POWER:
                                case SET_HW_RESET_ENABLE:
                                case SET_TRIGGER_LED_NOTIFICATION:
                                    ToastUtils.showToast(this, "Success!");
                                    break;
                                case SET_ERROR:
                                    if (isWindowLocked()) return;
                                    ToastUtils.showToast(this, "Failed");
                                    break;
                            }
                        }
                        break;
                    case CHAR_LOCK_STATE:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            int enable = MokoUtils.toInt(value);
                            setPasswordVerify(enable);
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            ToastUtils.showToast(this, "Success!");
                        }
                        break;
                    case CHAR_CONNECTABLE:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            int enable = MokoUtils.toInt(value);
                            setConnectable(enable);
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            ToastUtils.showToast(this, "Success!");
                        }
                        break;
                }
            }
        });
    }

    private boolean enablePasswordVerify;

    public void setPasswordVerify(int enable) {
        this.enablePasswordVerify = enable == 1;
        mBind.ivPasswordVerify.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        mBind.tvPasswordVerify.setText(enablePasswordVerify ? "Enable" : "Disable");
        mBind.tvPasswordVerify.setEnabled(enablePasswordVerify);
    }

    boolean enableConnected;

    public void setConnectable(int enable) {
        enableConnected = enable == 1;
        mBind.ivConnectable.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        mBind.tvConnectableStatus.setText(enableConnected ? "Enable" : "Disable");
        mBind.tvConnectableStatus.setEnabled(enableConnected);
    }

    private boolean enableButtonPower;

    public void setButtonPower(int enable) {
        this.enableButtonPower = enable == 1;
        mBind.ivButtonPower.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        mBind.tvButtonPower.setText(enableButtonPower ? "Enable" : "Disable");
        mBind.tvButtonPower.setEnabled(enableButtonPower);
    }

    private boolean enableHWReset;

    public void setHWResetEnable(int enable) {
        this.enableHWReset = enable == 1;
        mBind.ivHwReset.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        mBind.tvHwReset.setText(enableHWReset ? "Enable" : "Disable");
        mBind.tvHwReset.setEnabled(enableHWReset);
    }

    private boolean enableTriggerLEDNotify;

    public void setTriggerLEDNotifyEnable(int enable) {
        this.enableTriggerLEDNotify = enable == 1;
        mBind.ivTriggerLedNotify.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        mBind.tvTriggerLedNotify.setText(enableTriggerLEDNotify ? "Enable" : "Disable");
        mBind.tvTriggerLedNotify.setEnabled(enableTriggerLEDNotify);
    }

    public void onChangeConnectable(View view) {
        if (isWindowLocked())
            return;
        if (enableConnected) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("Are you sure to set the Beacon non-connectable？");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setConnectable(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setConnectable(true);
        }
    }

    public void onChangeTriggerLEDNotify(View view) {
        if (isWindowLocked())
            return;
        setTriggerLEDNotify(!enableTriggerLEDNotify);
    }

    public void onChangeButtonPower(View view) {
        if (isWindowLocked())
            return;
        if (enableButtonPower) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("If this function is disabled, you cannot power off the Beacon by button.");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setButtonPower(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setButtonPower(true);
        }
    }

    public void onChangeHWReset(View view) {
        if (isWindowLocked())
            return;
        if (enableHWReset) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("If Button reset is disabled, you cannot reset the Beacon by button operation.");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setHWResetEnable(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setHWResetEnable(true);
        }
    }

    public void onChangePasswordVerify(View view) {
        if (isWindowLocked())
            return;
        if (enablePasswordVerify) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("If Password verification is disabled, it will not need password to connect the Beacon.");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setDirectedConnectable(true);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setDirectedConnectable(false);
        }
    }


    public void setConnectable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setConnectable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getConnectable());
        ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setTriggerLEDNotify(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setTriggerLEDNotifyEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getTriggerLEDNotifyEnable());
        ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setButtonPower(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setButtonPower(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getButtonPower());
        ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setHWResetEnable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setHWResetEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getHWResetEnable());
        ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setDirectedConnectable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setLockStateDirected(enable ? 2 : 1));
        orderTasks.add(OrderTaskAssembler.getLockState());
        ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    public void onBack(View view) {
        back();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        Intent intent = new Intent();
        intent.putExtra(AppConstants.EXTRA_KEY_PASSWORD_VERIFICATION, enablePasswordVerify);
        setResult(RESULT_OK, intent);
        finish();
    }
}
