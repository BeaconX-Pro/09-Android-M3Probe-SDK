package com.moko.bxp.probe.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.bxp.probe.R;
import com.moko.bxp.probe.entity.AdvInfo;

public class DeviceListAdapter extends BaseQuickAdapter<AdvInfo, BaseViewHolder> {
    public DeviceListAdapter() {
        super(R.layout.list_item_device_probe);
    }

    @Override
    protected void convert(BaseViewHolder helper, AdvInfo item) {
        helper.setText(R.id.tv_mac, item.mac);
        helper.setText(R.id.tv_rssi, String.format("%ddBm", item.rssi));
        helper.setText(R.id.tv_interval_time, item.intervalTime == 0 ? "<->N/A" : String.format("<->%dms", item.intervalTime));
        helper.addOnClickListener(R.id.tv_connect);
        helper.setGone(R.id.tv_connect, item.connectState == 1);
        helper.setText(R.id.tv_battery, "");
        LinearLayout parent = helper.getView(R.id.ll_adv_info);
        parent.removeAllViews();
        if (item.advType == 2) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_device_info_probe, null);
            helper.setText(R.id.tv_battery, item.batterPercent < 0 ? "N/A" : (item.batterPercent * 10 + "%"));
            TextView tvAdvChannel = view.findViewById(R.id.tvAdvChannel);
            TextView tvAdvInterval = view.findViewById(R.id.tvAdvInterval);
            TextView tvTxPower = view.findViewById(R.id.tvTxPower);
            TextView tvAlarmStatus = view.findViewById(R.id.tvAlarmStatus);
            TextView tvAlarmCount = view.findViewById(R.id.tvAlarmCount);
            TextView tvTemp = view.findViewById(R.id.tvTemp);
            tvAdvChannel.setText(TextUtils.isEmpty(item.deviceInfoMhz) ? "N/A" : item.deviceInfoMhz);
            tvAdvInterval.setText(item.deviceInfoAdvInterval != -1 ? (item.deviceInfoAdvInterval + "ms") : "N/A");
            tvTxPower.setText(TextUtils.isEmpty(item.deviceInfoTxPower) ? "N/A" : item.deviceInfoTxPower);
            tvAlarmStatus.setText(TextUtils.isEmpty(item.alarmStatus) ? "N/A" : item.alarmStatus);
            tvAlarmCount.setText(item.alarmCount != -1 ? String.valueOf(item.alarmCount) : "N/A");
            tvTemp.setText(item.temperature != -1 ? (item.temperature + "℃") : "N/A");
            parent.addView(view);
        }
    }
}
