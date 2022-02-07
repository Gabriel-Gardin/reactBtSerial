package com.reactbtserial.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.util.UUID;

/**
 * Esta thread é executada enquanto tenta se conectar com o
 * equipamento(BluetoothDevice device)
 * É executada até o equipamento se conectar e dps retorna.
 */

public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThreadModule";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private final BluetoothAdapter bluetoothAdapter;
    private final ReactContext reactContext;

    public boolean connected = false;

    public ConnectThread(
            BluetoothDevice device,
            BluetoothAdapter bluetoothAdapter,
            ReactContext reactContext) {

        this.mmDevice = device;
        this.reactContext = reactContext;
        BluetoothSocket tmp = null;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        this.mmSocket = tmp; // Abriu o socket
        // TODO: Notificar que o processo de conexão começou
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public BluetoothSocket getMmSocket() {
        return this.mmSocket;
    }

    public BluetoothDevice getMmDevice() {
        return this.mmDevice;
    }

    public void run() {
        this.bluetoothAdapter.cancelDiscovery();

        try {
            this.mmSocket.connect();
        } catch (IOException connectException) {
            try {
                this.mmSocket.close();
                // TODO: Notificar comunicação fechada
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        this.connected = true;

        WritableMap payload = Arguments.createMap();
        payload.putInt("state", 3); // CONNECTED;
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("BluetoothState",
                payload);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            WritableMap payload = Arguments.createMap();
            payload.putInt("state", 2); // DISCONNECTED;
            this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("BluetoothState",
                    payload);
            this.connected = false;
            this.mmSocket.close(); // Bluetooth socket
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}