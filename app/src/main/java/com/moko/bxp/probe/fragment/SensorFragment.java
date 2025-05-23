package com.moko.bxp.probe.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.bxp.probe.databinding.FragmentSensorProbeBinding;

import androidx.fragment.app.FragmentActivity;

public class SensorFragment extends Fragment {
    private FragmentSensorProbeBinding mBind;

    private FragmentActivity activity;

    public SensorFragment() {
    }

    public static SensorFragment newInstance() {
        return new SensorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBind = FragmentSensorProbeBinding.inflate(inflater, container, false);
        activity = (FragmentActivity) getActivity();
        return mBind.getRoot();
    }


}
