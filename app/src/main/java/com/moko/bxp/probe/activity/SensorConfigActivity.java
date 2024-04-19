package com.moko.bxp.probe.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.probe.R;
import com.moko.bxp.probe.databinding.ActivitySensorConfigProbeBinding;
import com.moko.bxp.probe.dialog.LoadingMessageDialog;
import com.moko.bxp.probe.utils.ToastUtils;
import com.moko.support.probe.OrderTaskAssembler;
import com.moko.support.probe.ProbeMokoSupport;
import com.moko.support.probe.entity.OrderCHAR;
import com.moko.support.probe.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorConfigActivity extends BaseActivity {
    private ActivitySensorConfigProbeBinding mBind;

    private boolean mSavedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivitySensorConfigProbeBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        mBind.ivSave.postDelayed(() -> {
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.getSamplingInterval());
            orderTasks.add(OrderTaskAssembler.getDetectionInterval());
            ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        }, 500);
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
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length > 3) {
                        int header = value[0] & 0xFF;// 0xEB
                        int cmd = value[1] & 0xFF;
                        if (header != 0xEB) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) return;
                        int length = MokoUtils.toInt(Arrays.copyOfRange(value, 2, 4));
                        switch (configKeyEnum) {
//                            case KEY_WRITE_SAMPLING_INTERVAL:
//                                if (length != 1) return;
//                                if (result != 1) mSavedParamsError = true;
//                                break;
//                            case KEY_WRITE_DETECTION_INTERVAL:
//                                if (length != 1) return;
//                                if (result != 1) mSavedParamsError = true;
//                                ToastUtils.showToast(this, mSavedParamsError ? "Setup failed！" : "Setup succeed！");
//                                break;
                            case KEY_WRITE_SAMPLING_INTERVAL:
                            case KEY_WRITE_DETECTION_INTERVAL:
                                ToastUtils.showToast(this, "Success!");
                                break;
                            case SET_ERROR:
                                if (isWindowLocked()) return;
                                ToastUtils.showToast(this, "Failed");
                                break;
                        }
                        // read
                        if (configKeyEnum == ParamsKeyEnum.KEY_READ_SAMPLING_INTERVAL) {
                            if (length != 2) return;
                            int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 4 + length));
                            mBind.etSamplingInterval.setText(String.valueOf(interval));
                        }
                        if (configKeyEnum == ParamsKeyEnum.KEY_READ_DETECTION_INTERVAL) {
                            if (length != 3) return;
                            int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 4 + length));
                            mBind.etDetectionInterval.setText(String.valueOf(interval));

                        }
                    }
                }
            }
        });
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

    private void back() {
        // 关闭通知
        ProbeMokoSupport.getInstance().disableTHNotify();
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void onBack(View view) {
        back();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (TextUtils.isEmpty(mBind.etSamplingInterval.getText())) {
            ToastUtils.showToast(this, "Cannot be empty!");
            return;
        }
        String intervalStr = mBind.etSamplingInterval.getText().toString();
        int samplingInterval = Integer.parseInt(intervalStr);
        if (samplingInterval < 1 || samplingInterval > 65535) {
            ToastUtils.showToast(this, "Sampling interval range is 1~65535");
            return;
        }
        if (TextUtils.isEmpty(mBind.etDetectionInterval.getText())) {
            ToastUtils.showToast(this, "Cannot be empty!");
            return;
        }
        String detectionIntervalStr = mBind.etDetectionInterval.getText().toString();
        int detectionInterval = Integer.parseInt(detectionIntervalStr);
        if (detectionInterval < 1 || detectionInterval > 86400) {
            ToastUtils.showToast(this, "Detection interval range is 1~86400");
            return;
        }
        // 保存
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSamplingInterval(samplingInterval));
        orderTasks.add(OrderTaskAssembler.setDetectionInterval(detectionInterval));
        ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
