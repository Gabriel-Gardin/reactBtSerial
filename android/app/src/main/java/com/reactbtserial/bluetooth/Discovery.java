package com.reactbtserial.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Discovery extends BroadcastReceiver {

    private DiscoveryCallback mCallback;
    private Map<String, NativeDevice> unpairedDevices;

    public Discovery(DiscoveryCallback callback) {
        this.mCallback = callback;
        this.unpairedDevices = new HashMap<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (!unpairedDevices.containsKey(device.getAddress())) {
                NativeDevice found = new NativeDevice(device);

                mCallback.onDeviceDiscovered(found);
                unpairedDevices.put(device.getAddress(), found);

                mCallback.onDeviceDiscovered(found);
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Log.d(this.getClass().getSimpleName(),
                    String.format("Discovery found %d device(s)", unpairedDevices.size()));

            mCallback.onDiscoveryFinished(unpairedDevices.values());
            context.unregisterReceiver(this);
        }
    }

    public static IntentFilter intentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        return intentFilter;
    }

    public interface DiscoveryCallback {
        void onDeviceDiscovered(NativeDevice device);

        void onDiscoveryFinished(Collection<NativeDevice> devices);

        void onDiscoveryFailed(Throwable e);
    }

}