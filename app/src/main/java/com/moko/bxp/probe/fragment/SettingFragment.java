package com.moko.bxp.probe.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.bxp.probe.databinding.FragmentSettingProbeBinding;

public class SettingFragment extends Fragment {
    private FragmentSettingProbeBinding mBind;
    private boolean showPwd;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBind = FragmentSettingProbeBinding.inflate(inflater, container, false);
        setPwdShown(showPwd);
        return mBind.getRoot();
    }


    public void setPwdShown(boolean showPwd) {
        this.showPwd = showPwd;
        if (null == mBind) return;
        mBind.llModifyPwd.setVisibility(showPwd ? View.VISIBLE : View.GONE);
    }

    public void setResetShown(int enable) {
        if (null == mBind) return;
        mBind.llResetBeacon.setVisibility(enable == 1 ? View.VISIBLE : View.GONE);
    }
}
