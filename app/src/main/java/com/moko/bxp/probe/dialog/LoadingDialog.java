package com.moko.bxp.probe.dialog;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.moko.bxp.probe.R;
import com.moko.bxp.probe.databinding.DialogLoadingProbeBinding;
import com.moko.bxp.probe.view.ProgressDrawable;

public class LoadingDialog extends MokoBaseDialog<DialogLoadingProbeBinding> {
    public static final String TAG = LoadingDialog.class.getSimpleName();

    @Override
    protected DialogLoadingProbeBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogLoadingProbeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void onCreateView() {
        ProgressDrawable progressDrawable = new ProgressDrawable();
        progressDrawable.setColor(ContextCompat.getColor(getContext(), R.color.text_black_4d4d4d));
        mBind.ivLoading.setImageDrawable(progressDrawable);
        progressDrawable.start();
    }

    @Override
    public int getDialogStyle() {
        return R.style.CenterDialog;
    }

    @Override
    public int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public boolean getCancelOutside() {
        return false;
    }

    @Override
    public boolean getCancellable() {
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ProgressDrawable) mBind.ivLoading.getDrawable()).stop();
    }
}
