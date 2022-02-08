package com.reactbtserial.bluetooth;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.Arguments;
import com.reactbtserial.bluetooth.BluetoothStateEnum;
import java.util.Arrays;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class JSEventManager {

    private final ReactContext reactContext;

    public JSEventManager(ReactContext reactContext) {
        this.reactContext = reactContext;
    }

    public void sendBluetoothState(BluetoothStateEnum state) {
        WritableMap params = Arguments.createMap();
        params.putInt("state", state.getBluetoothTypeCode());
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
                "BluetoothState",
                params);
    }

    public void sendBluetoothData(byte[] buffer) {
        WritableMap payload = Arguments.createMap();
        payload.putInt("state", BluetoothStateEnum.BLUETOOTH_DATA.getBluetoothTypeCode()); // Dados bluetooth;
        payload.putString("dados", Arrays.toString(buffer));
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
                "BluetoothState",
                payload);
    }
}