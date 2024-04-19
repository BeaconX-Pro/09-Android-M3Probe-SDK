package com.moko.bxp.probe.activity;

import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.probe.AppConstants;
import com.moko.bxp.probe.R;
import com.moko.bxp.probe.databinding.ActivitySensorProbeBinding;
import com.moko.support.probe.ProbeMokoSupport;
import com.moko.support.probe.entity.OrderCHAR;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Locale;

import androidx.core.content.ContextCompat;

public class SensorProbeActivity extends BaseActivity {
    private ActivitySensorProbeBinding mBind;

    private int mSensorType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivitySensorProbeBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mSensorType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_SENSOR_TYPE, AppConstants.SENSOR_TYPE_TEMP);
        if (mSensorType == AppConstants.SENSOR_TYPE_TEMP) {
            mBind.tvTitle.setText("Temperature Probe");
            mBind.clTemp.setVisibility(View.VISIBLE);
            ProbeMokoSupport.getInstance().enableTempNotify();
        } else if (mSensorType == AppConstants.SENSOR_TYPE_TH) {
            mBind.tvTitle.setText("T&H Probe");
            mBind.clTemp.setVisibility(View.VISIBLE);
            mBind.clHumidity.setVisibility(View.VISIBLE);
            ProbeMokoSupport.getInstance().enableTHNotify();
        } else if (mSensorType == AppConstants.SENSOR_TYPE_WATER_LEAK) {
            mBind.tvTitle.setText("Water leakage Probe");
            mBind.clWaterLeak.setVisibility(View.VISIBLE);
            ProbeMokoSupport.getInstance().enableWaterLeakNotify();
        }
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
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_TEMP_NOTIFY) {
                    int temp = MokoUtils.toIntSigned(value);
                    if (temp == -1) return;
                    mBind.tvTemp.setText(String.format(Locale.getDefault(), "%s℃", MokoUtils.getDecimalFormat("0.#").format(temp * 0.1f)));
                } else if (orderCHAR == OrderCHAR.CHAR_TH_NOTIFY) {
                    byte[] tempBytes = Arrays.copyOfRange(value, 0, 2);
                    byte[] humidityBytes = Arrays.copyOfRange(value, 2, 4);
                    int temp = MokoUtils.toIntSigned(tempBytes);
                    int humidity = MokoUtils.toInt(humidityBytes);
                    if (temp == -1) return;
                    mBind.tvTemp.setText(String.format(Locale.getDefault(), "%s℃", MokoUtils.getDecimalFormat("0.#").format(temp * 0.1f)));
                    mBind.tvHumidity.setText(String.format(Locale.getDefault(), "%s%%RH", MokoUtils.getDecimalFormat("0.#").format(humidity * 0.1f)));
                } else if (orderCHAR == OrderCHAR.CHAR_WATER_LEAK_NOTIFY) {
                    int status = value[0] & 0xFF;
                    if (status == 0xFF) return;
                    mBind.tvWaterLeak.setText(value[0] == 1 ? "Leaked" : "Normal");
                    mBind.clWaterLeak.setBackgroundColor(ContextCompat.getColor(SensorProbeActivity.this, status == 1 ? R.color.red_ff0000 : R.color.green_00ff00));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        back();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        if (mSensorType == AppConstants.SENSOR_TYPE_TEMP) {
            ProbeMokoSupport.getInstance().disableTempNotify();
        } else if (mSensorType == AppConstants.SENSOR_TYPE_TH) {
            ProbeMokoSupport.getInstance().disableTHNotify();
        } else if (mSensorType == AppConstants.SENSOR_TYPE_WATER_LEAK) {
            ProbeMokoSupport.getInstance().disableWaterLeakNotify();
        }
        finish();
    }
}
