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
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.reactbtserial.bluetooth.BluetoothStateEnum;
import com.reactbtserial.bluetooth.JSEventManager;

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
    private JSEventManager mEventManager;

    public boolean connected = false;

    public ConnectThread(
            BluetoothDevice device,
            BluetoothAdapter bluetoothAdapter, JSEventManager mEventManager) {

        this.mmDevice = device;
        this.mEventManager = mEventManager;
        BluetoothSocket tmp = null;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        this.mmSocket = tmp; // Abriu o socket
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public BluetoothSocket getMmSocket() {
        return this.mmSocket;
    }

    public BluetoothDevice getMmDevice() {
        return this.mmDevice;
    }

    public void run() {
        this.bluetoothAdapter.cancelDiscovery(); // Antes de se conectar sempre devemos cancelar o discovery. Precaução.

        try {
            this.mmSocket.connect();
        } catch (IOException connectException) {
            try {
                this.mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        this.connected = true;
        this.mEventManager.sendBluetoothState(BluetoothStateEnum.BLUETOOTH_CONECTADO);
    }

    // Fecha a conexão e faz com que a thread termine
    public void cancel() {
        try {
            this.mEventManager.sendBluetoothState(BluetoothStateEnum.BLUETOOTH_DESCONECTADO);
            this.connected = false;
            this.mmSocket.close(); // Bluetooth socket
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}