package com.reactbtserial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;

import com.reactbtserial.bluetooth.BluetoothStateEnum;
//import com.driver3sat.bluetooth.Commands;
//import com.driver3sat.bluetooth.Communication;
import com.reactbtserial.bluetooth.ConnectThread;
import com.reactbtserial.bluetooth.CommunicationThread;
import com.reactbtserial.bluetooth.Discovery;
import com.reactbtserial.bluetooth.NativeDevice;
//import com.driver3sat.bluetooth.Pairing;
import com.reactbtserial.helpers.Events;
import com.reactbtserial.helpers.EventsEnum;
import com.facebook.react.BuildConfig;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import android.util.Log;

import javax.annotation.Nonnull;

public class BluetoothModule extends ReactContextBaseJavaModule {
    private static final String TAG = "3SAT";
    private static final String DATA_RECEIVE_EVENT = "DataReviced";
    private static final String BLUETOOTH_STATE = "BluetoothState";

    private static final int BT_PERMISSION = 1;

    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread connection;
    private CommunicationThread communication;
    private ReactContext reactContext;
    private BluetoothState bluetoothStateReciver;
    private BluetoothSocket mmSocket;
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
    public void checkIfDeviceSupportBT(Promise promise) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            promise.resolve(false);

            return;
        }
        this.bluetoothAdapter = bluetoothAdapter;
        promise.resolve(true);
    }

    @ReactMethod
    public void askToEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            reactContext.startActivityForResult(enableBtIntent, BT_PERMISSION, null);
        }
    }

    /**
     * Inicializa o
     * 
     * @param x The value to square.
     * @return The square root of the given number.
     */
    @ReactMethod
    public void initBluetoothStateListener() {
        this.bluetoothStateReciver = new BluetoothState();
        IntentFilter bluetoothStateFilter = new IntentFilter();
        bluetoothStateFilter.addAction(this.bluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothStateFilter.addAction(this.bluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        getReactApplicationContext().registerReceiver(
                bluetoothStateReciver,
                bluetoothStateFilter);
    }

    @ReactMethod
    public void isBluetoothEnabled(Promise promise) {
        promise.resolve(this.checkBluetoothAdapter());
    }

    @ReactMethod
    public void getBondedDevices(Promise promise) {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        WritableArray bonded = Arguments.createArray();

        // There are paired devices. Get the name and address of each paired device.
        for (BluetoothDevice device : bondedDevices) {
            NativeDevice nativeDevice = new NativeDevice(device);

            bonded.pushMap(nativeDevice.map());
        }

        promise.resolve(bonded);
    }

    @ReactMethod
    public void connect(String address) throws IOException {

        BluetoothDevice device = this.bluetoothAdapter.getRemoteDevice(address);
        NativeDevice nativeDevice = new NativeDevice(device);
        ConnectThread connection = new ConnectThread(
                device,
                this.bluetoothAdapter,
                this.reactContext);

        connection.run(); // Inicia a thread de conexão

        Log.e(TAG, "Communication iniciada");

        int i = 0;
        while (i < 15) {
            i++;
            try {
                Thread.currentThread().sleep(200); // Pause a thread por 200ms
                if (connection.connected) {
                    this.connection = connection;
                    this.mmSocket = this.connection.getMmSocket();
                    CommunicationThread communication = new CommunicationThread(this.mmSocket,
                            this.reactContext);
                    communication.start(); // Inicia a threa que recebe dados pelo socket bluetooth.
                    this.communication = communication;
                    // promise.resolve(true);
                    return;
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @ReactMethod
    public void close_bt_connection() {
        if (this.connection.connected == true) {
            this.communication.cancel();
            try {
                Thread.currentThread().sleep(200); // Pause a thread por 200ms
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
            this.connection.cancel();
        }
    }

    @ReactMethod
    public void get_bt_status(Promise promise) {
        if (this.connection == null) {
            promise.resolve(false);
            return;
        } else {
            promise.resolve(this.connection.connected);
        }
    }

    @ReactMethod
    public void writeFmb(ReadableArray message, Promise promise) throws IOException {

        final byte[] res;
        res = new byte[message.size()];

        for (int i = 0; i < message.size(); i++) {
            res[i] = ((byte) message.getDouble(i));
        }

        this.communication.write(res);
    }

    @ReactMethod
    public void writeStringCommand(String message, Promise promise) throws IOException {

        OutputStream mmOutStream = this.connection.getMmSocket().getOutputStream();
        String msg = message + "\r\n";
        this.communication.write(msg.getBytes());
    }

    private boolean checkBluetoothAdapter() {
        return (this.bluetoothAdapter != null && this.bluetoothAdapter.isEnabled());
    }

    private class BluetoothState extends BroadcastReceiver {

        public void sendBluetoothState(BluetoothStateEnum state) {
            ReactContext reactContext = getReactApplicationContext();
            WritableMap params = Arguments.createMap();
            params.putInt("state", state.getBluetoothTypeCode());
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
                    BLUETOOTH_STATE,
                    params);
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

    @ReactMethod
    public void discovery(final Promise promise) {
        Log.i(TAG, "Iniciando discovery");

        mDiscoveryReceiver = new Discovery(new Discovery.DiscoveryCallback() {
            @Override
            public void onDeviceDiscovered(NativeDevice device) {
                // TODO: Renderizar item a item retornando por aqui...
                return;
            }

            @Override
            public void onDiscoveryFinished(Collection<NativeDevice> devices) {
                Log.i(TAG, "Acabou o discovery!!");
                WritableArray array = Arguments.createArray();
                for (NativeDevice device : devices) {
                    array.pushMap(device.map());
                    Log.i(TAG, String.format("Encontrou equipamento: %s", device.getName()));
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
