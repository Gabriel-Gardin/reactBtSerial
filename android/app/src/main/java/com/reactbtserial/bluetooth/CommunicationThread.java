package com.reactbtserial.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Intent;
import android.util.Log;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;

import java.io.IOException;
import java.util.Arrays;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class CommunicationThread extends Thread {
    private static final String TAG = "CommunicationThread";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final ReactContext reactContext;
    private boolean mState;

    public CommunicationThread(BluetoothSocket socket, ReactContext reactContext) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.mState = true;
        this.reactContext = reactContext;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectedThread");
        byte[] buffer = new byte[64];
        int bytes;

        // Keep listening to the InputStream while connected
        while (this.mState) {
            try {
                // Read from the InputStream
                Arrays.fill(buffer, (byte) 0);
                bytes = mmInStream.read(buffer); // Le os dados que chegam
                Log.i(TAG, String.format("Dados: %s", Arrays.toString(buffer)));
                Log.i(TAG, String.format("Tamando: %d", bytes));

                WritableMap payload = Arguments.createMap();
                payload.putInt("state", 10); // Dados bluetooth;
                payload.putString("dados", Arrays.toString(buffer));
                this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
                        "BluetoothState",
                        payload);

            } catch (IOException e) {
                if (this.mState) {
                    WritableMap payload = Arguments.createMap();
                    payload.putInt("state", 7); // Conexão perdida;
                    this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
                            "BluetoothState",
                            payload);
                }
                Log.e(TAG, "Conexão bluetooth caiu!!", e);
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    public void write(byte[] buffer) {
        try {
            mmOutStream.write(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public void cancel() {
        this.mState = false;
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}