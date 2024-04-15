package com.moko.bxp.probe.dialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.moko.bxp.probe.databinding.DialogBottomProbeBinding;

import java.util.ArrayList;

public class BottomDialog extends MokoBaseDialog<DialogBottomProbeBinding> {
    private ArrayList<String> mDatas;
    private int mIndex;

    @Override
    protected DialogBottomProbeBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogBottomProbeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void onCreateView() {
        mBind.wvBottomD.setData(mDatas);
        mBind.wvBottomD.setDefault(mIndex);
        mBind.tvCancel.setOnClickListener(v -> dismiss());
        mBind.tvConfirm.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mBind.wvBottomD.getSelectedText())) {
                return;
            }
            dismiss();
            final int selected = mBind.wvBottomD.getSelected();
            if (listener != null) {
                listener.onValueSelected(selected);
            }
        });
        super.onCreateView();
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    public void setDatas(ArrayList<String> datas, int index) {
        this.mDatas = datas;
        this.mIndex = index;
    }

    private OnBottomListener listener;

    public void setListener(OnBottomListener listener) {
        this.listener = listener;
    }

    public interface OnBottomListener {
        void onValueSelected(int value);
    }
}
