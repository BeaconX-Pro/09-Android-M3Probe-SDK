package com.moko.bxp.probe.activity;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.probe.databinding.ActivityAdvConfigProbeBinding;
import com.moko.lib.bxpui.dialog.LoadingMessageDialog;
import com.moko.bxp.probe.utils.ToastUtils;
import com.moko.support.probe.OrderTaskAssembler;
import com.moko.support.probe.ProbeMokoSupport;
import com.moko.support.probe.entity.OrderCHAR;
import com.moko.support.probe.entity.TxPowerEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AdvConfigActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private ActivityAdvConfigProbeBinding mBind;
    private final String FILTER_ASCII = "[ -~]*";

    private boolean mSavedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityAdvConfigProbeBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }

            return null;
        };
        mBind.etAdvName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10), filter});
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        mBind.tvTitle.postDelayed(() -> {
            List<OrderTask> orderTasks = new ArrayList<>(3);
            orderTasks.add(OrderTaskAssembler.getSlotData());
            orderTasks.add(OrderTaskAssembler.getAdvInterval());
            orderTasks.add(OrderTaskAssembler.getTxPower());
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
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                int length = value.length;
                if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                    if (orderCHAR == OrderCHAR.CHAR_ADV_SLOT_DATA) {
                        int frameType = value[0] & 0xFF;
                        if (frameType != 0x80) return;
                        byte[] deviceName = Arrays.copyOfRange(value, 1, value.length);
                        mBind.etAdvName.setText(new String(deviceName));
                    } else if (orderCHAR == OrderCHAR.CHAR_ADV_INTERVAL) {
                        int advInterval = MokoUtils.toInt(value);
                        mBind.etAdvInterval.setText(String.valueOf(advInterval / 100));
                    } else if (orderCHAR == OrderCHAR.CHAR_TX_POWER) {
                        int txPower = value[0];
                        TxPowerEnum txPowerEnum = TxPowerEnum.fromTxPower(txPower);
                        assert txPowerEnum != null;
                        mBind.sbTxPower.setProgress(txPowerEnum.ordinal());
                        mBind.tvTxPowerValue.setText(String.format(Locale.getDefault(), "%ddBm", txPower));
                    }
                }
                if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                    if (length == 1 && value[0] == 0x0D)
                        mSavedParamsError = true;
                    if (orderCHAR == OrderCHAR.CHAR_TX_POWER) {
                        if (mSavedParamsError) {
                            ToastUtils.showToast(this, "Setup failed！");
                        } else {
                            ToastUtils.showToast(this, "Setup succeed！");
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
        if (TextUtils.isEmpty(mBind.etAdvName.getText())) {
            ToastUtils.showToast(this, "Cannot be empty!");
            return;
        }
        if (TextUtils.isEmpty(mBind.etAdvInterval.getText())) {
            ToastUtils.showToast(this, "Cannot be empty!");
            return;
        }
        String intervalStr = mBind.etAdvInterval.getText().toString();
        int interval = Integer.parseInt(intervalStr);
        if (interval < 1 || interval > 100) {
            ToastUtils.showToast(this, "Adv interval range is 1~100");
            return;
        }
        // 保存
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>(3);
        final String deviceNameParamsHex = String.format("80%s", MokoUtils.string2Hex(mBind.etAdvName.getText().toString()));
        orderTasks.add(OrderTaskAssembler.setSlotData(MokoUtils.hex2bytes(deviceNameParamsHex)));
        final byte[] intervalBytes = MokoUtils.toByteArray(interval * 100, 2);
        orderTasks.add(OrderTaskAssembler.setAdvInterval(intervalBytes));
        final TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(mBind.sbTxPower.getProgress());
        orderTasks.add(OrderTaskAssembler.setTxPower(MokoUtils.toByteArray(txPowerEnum.getTxPower(), 1)));
        ProbeMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
        assert txPowerEnum != null;
        int txPower = txPowerEnum.getTxPower();
        mBind.tvTxPowerValue.setText(String.format(Locale.getDefault(), "%ddBm", txPower));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
