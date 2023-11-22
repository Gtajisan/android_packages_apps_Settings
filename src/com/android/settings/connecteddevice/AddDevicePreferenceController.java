/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.connecteddevice;

import static com.android.settings.accessibility.AccessibilityHearingAidsFragment.KEY_HEARING_DEVICE_ADD_BT_DEVICES;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.accessibility.HearingDevicePairingDetail;
import com.android.settings.accessibility.HearingDevicePairingFragment;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.flags.Flags;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

/**
 * Controller to maintain the {@link androidx.preference.Preference} for add
 * device. It monitor Bluetooth's status(on/off) and decide if need
 * to show summary or not.
 */
public class AddDevicePreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private Preference mPreference;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState(mPreference);
        }
    };
    private IntentFilter mIntentFilter;
    private BluetoothAdapter mBluetoothAdapter;

    public AddDevicePreferenceController(Context context, String key) {
        super(context, key);
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onStart() {
        mContext.registerReceiver(mReceiver, mIntentFilter,
                Context.RECEIVER_EXPORTED_UNAUDITED);
    }

    @Override
    public void onStop() {
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mPreference = screen.findPreference(getPreferenceKey());
            updateState(mPreference);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(preference.getKey(), KEY_HEARING_DEVICE_ADD_BT_DEVICES)) {
            String destination = Flags.newHearingDevicePairingPage()
                    ? HearingDevicePairingFragment.class.getName()
                    : HearingDevicePairingDetail.class.getName();
            new SubSettingLauncher(preference.getContext())
                    .setDestination(destination)
                    .setSourceMetricsCategory(getMetricsCategory())
                    .launch();
            return true;
        }
        return super.handlePreferenceTreeClick(preference);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
                ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        return isBluetoothEnabled()
                ? ""
                : mContext.getString(R.string.connected_device_add_device_summary);
    }

    protected boolean isBluetoothEnabled() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }
}
