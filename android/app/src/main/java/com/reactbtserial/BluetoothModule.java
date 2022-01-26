package com.reactbtserial;// replace com.your-app-name with your app’s name
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import java.util.Map;
import java.util.HashMap;
import com.facebook.react.bridge.Promise;

import javax.annotation.Nonnull;

import android.util.Log;

import android.content.ServiceConnection;

/*Bluetooth*/
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;

import com.reactbtserial.bluetooth.Discovery;
import com.reactbtserial.bluetooth.NativeDevice;

import java.util.Collection;


public class BluetoothModule extends ReactContextBaseJavaModule {
    
    private ReactContext reactContext;

    private int count = 0;


    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver mDiscoveryReceiver;

    public BluetoothModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "BluetoothModule";
    }

    @ReactMethod
    public void teste(Promise promise){
        this.count ++;
        String a;
        a = String.format("Chamou: %d", this.count);
        promise.resolve(a);
    }


    @ReactMethod
    public void checkIfDeviceSupportBT(Promise promise) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            promise.resolve("Naõ tem bluetooth");

            return;
        }

        this.bluetoothAdapter = bluetoothAdapter;

        promise.resolve("Tem bluetooth");
    }

    @ReactMethod
    public void discovery(final Promise promise) {
        mDiscoveryReceiver = new Discovery(new Discovery.DiscoveryCallback() {
            @Override
            public void onDeviceDiscovered(NativeDevice device) {
                return;
            }

            @Override
            public void onDiscoveryFinished(Collection<NativeDevice> devices) {
                WritableArray array = Arguments.createArray();
                for (NativeDevice device : devices) {
                    array.pushMap(device.map());
                }

                promise.resolve(array);
                mDiscoveryReceiver = null;
            }

            @Override
            public void onDiscoveryFailed(Throwable e) {
                mDiscoveryReceiver = null;
            }
        });

        getReactApplicationContext().registerReceiver(mDiscoveryReceiver,
                Discovery.intentFilter());

        this.bluetoothAdapter.startDiscovery();
    }
}