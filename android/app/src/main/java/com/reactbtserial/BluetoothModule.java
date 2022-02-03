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
//import com.driver3sat.bluetooth.Discovery;
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

    /*
     * @ReactMethod
     * public void pairDevice(String address, Promise promise) {
     * if (BuildConfig.DEBUG)
     * Log.d(TAG, String.format("Attempting to pair with device %s", address));
     * 
     * final Pairing pr = new Pairing(getReactApplicationContext(),
     * new Pairing.PairingCallback() {
     * 
     * @Override
     * public void onPairingSuccess(NativeDevice device) {
     * promise.resolve(device.map());
     * }
     * 
     * @Override
     * public void onPairingFailure(Exception cause) {
     * promise.reject(TAG, cause);
     * }
     * });
     * getReactApplicationContext().registerReceiver(pr, Pairing.intentFilter());
     * 
     * try {
     * BluetoothDevice device = this.bluetoothAdapter.getRemoteDevice(address);
     * 
     * Method m = device.getClass().getMethod("createBond", (Class[]) null);
     * m.invoke(device, (Object[]) null);
     * } catch (IllegalAccessException | NoSuchMethodException |
     * InvocationTargetException e) {
     * Log.d("3SAT", String.valueOf(e));
     * }
     * }
     */
    @ReactMethod
    public void connect(String address) throws IOException {

        BluetoothDevice device = this.bluetoothAdapter.getRemoteDevice(address);
        NativeDevice nativeDevice = new NativeDevice(device);
        ConnectThread connection = new ConnectThread(
                device,
                this.bluetoothAdapter,
                this.reactContext);

        // CommunicationThread communication = new
        // CommunicationThread(connection.getMmSocket());
        connection.run();

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
                    communication.start();
                    this.communication = communication;
                    // promise.resolve(true);
                    return;
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
        communication.cancel();
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

    // @ReactMethod
    // public void addListener(String eventName, Promise promise) throws IOException
    // {
    // try {unblockFilterFilter();

    // unblockFilter.addAction(Events.UNBLOCK_COMMAND_NOT_EXECUTED);
    // unblockFilter.addAction(Events.UNBLOCK_COMMAND_EXECUTED);
    // unblockFilter.addAction(Events.CONNECTION_ERROR);
    // unblockFilter.addAction(Events.IGNITION_ON);
    // unblockFilter.addAction(Events.IGNITION_OFF);
    // unblockFilter.addAction(Events.COMMAND_NOT_SENT);
    // unblockFilter.addAction(Events.COMMAND_SENT);
    // unblockFilter.addAction(Events.CONNECTION_CLOSED);

    // getReactApplicationContext().registerReceiver(
    // this.dataReceiver,
    // unblockFilter);

    // promise.resolve(true);
    // } catch (Exception ex) {
    // promise.resolve(false);
    // }
    // }

    // @ReactMethod
    // public void removeListener() throws IOException {
    // try {
    // if (this.dataReceiver != null) {
    // getReactApplicationContext().unregisterReceiver(this.dataReceiver);
    // this.dataReceiver = null;
    // }
    // } catch (Exception ex) {
    // }
    // }

    /*
     * @ReactMethod
     * public void writeMxt(ReadableMap info, int command, Promise promise) {
     * String serialNumber = info.getString("serialNumber");
     * String driverId = info.getString("driverId");
     * Boolean driverAppWithWarningSound =
     * info.getBoolean("driverAppWithWarningSound");
     * 
     * Commands commands = new Commands();
     * 
     * byte[] commandToWrite;
     * 
     * switch (command) {
     * case 2:
     * commandToWrite = commands.sendReleaseCommand(
     * driverAppWithWarningSound,
     * serialNumber
     * );
     * 
     * break;
     * case 3:
     * commandToWrite = commands.sendDriverIdCommand(
     * serialNumber,
     * driverId
     * );
     * 
     * break;
     * case 1:
     * commandToWrite = commands.sendRequestVariablesCommand(
     * serialNumber
     * );
     * 
     * break;
     * default:
     * throw new IllegalStateException("Unexpected value: " + command);
     * }
     * 
     * boolean sent = this.communication.write(commandToWrite);
     * 
     * Log.d(TAG, String.valueOf(sent));
     * 
     * promise.resolve(sent);
     * }
     */

    @ReactMethod
    public void writeFmb(ReadableArray message, Promise promise) throws IOException {

        final byte[] res;
        res = new byte[message.size()];

        for (int i = 0; i < message.size(); i++) {
            res[i] = ((byte) message.getDouble(i));
        }

        OutputStream mmOutStream = this.connection.getMmSocket().getOutputStream();

        try {
            mmOutStream.write(res);
            promise.resolve(true);
        } catch (IOException e) {
            promise.reject("400", "Failed");
        }
    }

    /*
     * @ReactMethod
     * public void startCommunication(Promise promise) {
     * BluetoothSocket socket = this.connection.getMmSocket();
     * 
     * Communication bluetoothService = new Communication(socket,
     * this.reactContext);
     * 
     * bluetoothService.start();
     * 
     * this.communication = bluetoothService;
     * 
     * promise.resolve(true);
     * }
     */

    /*
     * @ReactMethod
     * public void discovery(final Promise promise) {
     * mDiscoveryReceiver = new Discovery(new Discovery.DiscoveryCallback() {
     * 
     * @Override
     * public void onDeviceDiscovered(NativeDevice device) {
     * return;
     * }
     * 
     * @Override
     * public void onDiscoveryFinished(Collection<NativeDevice> devices) {
     * WritableArray array = Arguments.createArray();
     * for (NativeDevice device : devices) {
     * array.pushMap(device.map());
     * }
     * 
     * promise.resolve(array);
     * mDiscoveryReceiver = null;
     * }
     * 
     * @Override
     * public void onDiscoveryFailed(Throwable e) {
     * mDiscoveryReceiver = null;
     * }
     * });
     * 
     * getReactApplicationContext().registerReceiver(mDiscoveryReceiver,
     * Discovery.intentFilter());
     * 
     * this.bluetoothAdapter.startDiscovery();
     * }
     * 
     * @ReactMethod
     * public void endCommunication() {
     * this.communication.cancel();
     * }
     */

    // public void sendEvent(WritableMap data) {
    // ReactContext reactContext = getReactApplicationContext();

    // reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
    // DATA_RECEIVE_EVENT,
    // data);
    // }

    public void sendBluetoothState(BluetoothStateEnum state) {

        ReactContext reactContext = getReactApplicationContext();
        WritableMap params = Arguments.createMap();
        params.putInt("state", state.getBluetoothTypeCode());
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
                BLUETOOTH_STATE,
                params);
    }

    private boolean checkBluetoothAdapter() {
        return (this.bluetoothAdapter != null && this.bluetoothAdapter.isEnabled());
    }

    private class BluetoothState extends BroadcastReceiver {
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
                    // case BluetoothAdapter.STATE_TURNING_OFF:
                    // sendBluetoothState(BluetoothStateEnum.BLUETOOTH_TURNING_OFF);
                    // break;
                    case BluetoothAdapter.STATE_ON:
                        sendBluetoothState(BluetoothStateEnum.BLUETOOTH_ON);
                        break;

                    // case BluetoothAdapter.STATE_CONNECTED:
                    // sendBluetoothState(BluetoothStateEnum.BLUETOOTH_TURNING_ON);
                    // break;

                    // case BluetoothAdapter.STATE_DISCONNECTED:
                    // sendBluetoothState(BluetoothStateEnum.BLUETOOTH_TURNING_OFF);
                    // break;
                    // case BluetoothAdapter.STATE_TURNING_ON:
                    // sendBluetoothState(BluetoothStateEnum.BLUETOOTH_TURNING_ON);
                    // break;
                }
            }

            // if(action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CONNECTED)){
            // sendBluetoothState(BluetoothStateEnum.BLUETOOTH_TURNING_ON);
            // }
        }
    }

    // private final class DataReceiver extends BroadcastReceiver {

    // @Override
    // public void onReceive(Context context, Intent intent) {
    // String action = intent.getAction();

    // WritableMap params = Arguments.createMap();

    // switch (action) {
    // case Events.UNBLOCK_COMMAND_EXECUTED:
    // params.putInt("event", EventsEnum.UNBLOCK_COMMAND_EXECUTED.getEventsCode());
    // break;
    // case Events.UNBLOCK_COMMAND_NOT_EXECUTED:
    // params.putInt("event",
    // EventsEnum.UNBLOCK_COMMAND_NOT_EXECUTED.getEventsCode());
    // break;
    // case Events.CONNECTION_CLOSED:
    // params.putInt("event", EventsEnum.CONNECTION_CLOSED.getEventsCode());
    // break;
    // case Events.CONNECTION_ERROR:
    // params.putInt("event", EventsEnum.CONNECTION_ERROR.getEventsCode());
    // break;
    // case Events.IGNITION_ON:
    // params.putInt("event", EventsEnum.IGNITION_ON.getEventsCode());
    // break;
    // case Events.IGNITION_OFF:
    // params.putInt("event", EventsEnum.IGNITION_OFF.getEventsCode());
    // break;
    // case Events.COMMAND_SENT:
    // params.putInt("event", EventsEnum.COMMAND_SENT.getEventsCode());
    // break;
    // case Events.COMMAND_NOT_SENT:
    // params.putInt("event", EventsEnum.COMMAND_NOT_SENT.getEventsCode());
    // break;
    // }

    // sendEvent(params);
    // }
    // }

}
