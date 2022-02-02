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

public class Connection extends Thread{
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private final BluetoothAdapter bluetoothAdapter;
    private final ReactContext reactContext;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private boolean thread_running;


    public boolean connected = false;


    public Connection(
            BluetoothDevice device,
            BluetoothAdapter bluetoothAdapter,
            ReactContext reactContext
    ) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        this.mmDevice = device;
        this.reactContext = reactContext;
        this.thread_running = true;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            Log.d(TAG, String.valueOf(device));
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }

        this.mmSocket = tmp;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = this.mmSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = this.mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;


        this.bluetoothAdapter = bluetoothAdapter;
        //this.mmInStream = this.mmSocket.getInputStream();
    }

    public BluetoothSocket getMmSocket() {
        return this.mmSocket;
    }

    public BluetoothDevice getMmDevice() {
        return this.mmDevice;
    }

    public void run(){
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
            return;
        }

        this.connected = true;
        WritableMap payload = Arguments.createMap();
        payload.putInt("state", 3); //CONNECTED;
        //this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("BluetoothState", payload);
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("BluetoothState", payload);


        
        byte[] buffer = new byte[1024];
        int len;
        //noinspection InfiniteLoopStatement
        while (this.thread_running) {
            WritableMap payload2 = Arguments.createMap();
            try {
                payload2.putInt("state", 7); //teste;
                len = mmInStream.read(buffer);
                //len = this.mmSocket.getInputStream().read(buffer); //Quando eu desconecto o esp a aplicação crasha, pq não cai no try - catch?
                //Thread.currentThread().sleep(200); // Pause a thread por 200ms
                payload2.putInt("state", len); //DISCONNECTED;
                this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("BluetoothState", payload2);

                
                //byte[] data = Arrays.copyOf(buffer, len);
                //if(listener != null)
                    //  listener.onSerialRead(data);
            }

            catch (Exception e) {
                //WritableMap payload = Arguments.createMap();
                payload.putInt("state", 2); //DISCONNECTED;
                this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("BluetoothState", payload2);
                this.connected = false;
                try {
                    this.mmSocket.close();
                } catch (Exception ignored) {
                }
                this.mmSocket = null;
            }
        } 
    }

    public void stop_thread(){
        this.thread_running = false;
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            WritableMap payload = Arguments.createMap();
            payload.putInt("state", 2); //DISCONNECTED;
            this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("BluetoothState", payload);
            this.connected = false;
            //mmInStream.close();
            //mmOutStream.close();
            this.mmSocket.close();
        } catch (Exception e) {
            //Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
