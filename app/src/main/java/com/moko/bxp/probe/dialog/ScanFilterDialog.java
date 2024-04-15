package com.moko.bxp.probe.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.widget.SeekBar;

import com.moko.bxp.probe.databinding.DialogScanFilterProbeBinding;

public class ScanFilterDialog extends BaseDialog<DialogScanFilterProbeBinding> {
    private int filterRssi;
    private String filterMac;

    @Override
    protected DialogScanFilterProbeBinding getViewBind() {
        return DialogScanFilterProbeBinding.inflate(getLayoutInflater());
    }

    public ScanFilterDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate() {
        mBind.tvRssi.setText(String.format("%sdBm", filterRssi + ""));
        mBind.sbRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rssi = (progress * -1);
                mBind.tvRssi.setText(String.format("%sdBm", rssi + ""));
                filterRssi = rssi;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBind.sbRssi.setProgress(Math.abs(filterRssi));
        if (!TextUtils.isEmpty(filterMac)) {
            mBind.etFilterMac.setText(filterMac);
            mBind.etFilterMac.setSelection(filterMac.length());
        }
        setDismissEnable(true);
        mBind.ivFilterMacDelete.setOnClickListener(v -> mBind.etFilterMac.setText(""));
        mBind.tvDone.setOnClickListener(v -> {
            listener.onDone(mBind.etFilterMac.getText().toString(), filterRssi);
            dismiss();
        });
    }

    private OnScanFilterListener listener;

    public void setOnScanFilterListener(OnScanFilterListener listener) {
        this.listener = listener;
    }

    public void setFilterMac(String filterMac) {
        this.filterMac = filterMac;
    }

    public void setFilterRssi(int filterRssi) {
        this.filterRssi = filterRssi;
    }

    public interface OnScanFilterListener {
        void onDone(String filterMac, int filterRssi);
    }
}
