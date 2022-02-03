package com.reactbtserial.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Intent;
import android.util.Log;

import com.reactbtserial.helpers.Events;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private static final String TAG = "MY_APP_DEBUG_TAG";
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
                new Intent(Events.CONNECTION_CLOSED);
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
                new Intent(Events.CONNECTION_ERROR);
            }
            // Notificar que a conexão falhou
            return;
        }

        this.connected = true;

        WritableMap payload = Arguments.createMap();
        payload.putInt("state", 3); // CONNECTED;
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("BluetoothState",
                payload);
        // Inciar thread de conexão...
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            WritableMap payload = Arguments.createMap();
            payload.putInt("state", 2); // DISCONNECTED;
            this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("BluetoothState",
                    payload);
            this.connected = false;
            this.mmSocket.close();
        } catch (Exception e) {
            // Log.e(TAG, "Could not close the client socket", e);
        }
    }
}