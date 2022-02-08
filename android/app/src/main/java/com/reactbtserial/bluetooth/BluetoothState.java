package com.reactbtserial.bluetooth;

import com.reactbtserial.bluetooth.BluetoothStateEnum;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothAdapter;
import com.reactbtserial.bluetooth.JSEventManager;
import android.content.Intent;
import android.content.Context;

public class BluetoothState extends BroadcastReceiver {

    private JSEventManager mEventManager;

    public BluetoothState(JSEventManager mEventManager) {
        this.mEventManager = mEventManager;
    }

    public void sendBluetoothState(BluetoothStateEnum state) {
        this.mEventManager.sendBluetoothState(state);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    sendBluetoothState(BluetoothStateEnum.BLUETOOTH_OFF);
                    break;

                case BluetoothAdapter.STATE_ON:
                    sendBluetoothState(BluetoothStateEnum.BLUETOOTH_ON);
                    break;
            }
        }
    }
}