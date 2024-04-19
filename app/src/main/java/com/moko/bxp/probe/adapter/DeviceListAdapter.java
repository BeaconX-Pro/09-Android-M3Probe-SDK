package com.moko.bxp.probe.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.probe.R;
import com.moko.bxp.probe.entity.AdvInfo;

import java.util.Locale;

public class DeviceListAdapter extends BaseQuickAdapter<AdvInfo, BaseViewHolder> {
    public DeviceListAdapter() {
        super(R.layout.list_item_device_probe);
    }

    @Override
    protected void convert(BaseViewHolder helper, AdvInfo item) {
        helper.setText(R.id.tv_mac, String.format("MAC:%s", item.mac));
        helper.setText(R.id.tv_name, item.name);
        helper.setText(R.id.tv_rssi, String.format("%ddBm", item.rssi));
        helper.setText(R.id.tv_interval_time, item.intervalTime == 0 ? "<->N/A" : String.format("<->%dms", item.intervalTime));
        helper.addOnClickListener(R.id.tv_connect);
        helper.setGone(R.id.tv_connect, item.connectState == 1);
        float temperature = item.temperature * 0.1f;
        float humidity = item.humidity * 0.1f;
        helper.setGone(R.id.layoutWaterLeakageStatus, item.waterLeakage != 0xFF);
        helper.setText(R.id.tvWaterLeakStatus, item.waterLeakage == 0 ? "No" : "Yes");
        helper.setGone(R.id.layoutTemperature, item.temperature != 0xFFFF);
        helper.setText(R.id.tvTemp, String.format(Locale.getDefault(), "%s℃", MokoUtils.getDecimalFormat("0.#").format(temperature)));
        helper.setGone(R.id.layoutHumidity, item.humidity != 0xFFFF);
        helper.setText(R.id.tvHumidity, String.format(Locale.getDefault(), "%s%%RH", MokoUtils.getDecimalFormat("0.#").format(humidity)));
        helper.setGone(R.id.layoutTofRanging, item.tofRanging != 0xFFFF);
        helper.setText(R.id.tvTofRanging, String.format(Locale.getDefault(), "%dmm", item.tofRanging));
    }
}
