package com.reactbtserial.bluetooth;

import java.util.LinkedHashMap;
import java.util.Map;

public enum BluetoothStateEnum {
    BLUETOOTH_ON(1),
    BLUETOOTH_OFF(0),
    BLUETOOTH_TURNING_ON(4),
    BLUETOOTH_TURNING_OFF(5),
    BLUETOOTH_CONNECTED(3),
    BLUETOOTH_DISCONNECTED(2);
    

    private int bluetoothTypeCode;

    BluetoothStateEnum(int bluetoothTypeCode) {
        this.bluetoothTypeCode = bluetoothTypeCode;
    }

    private static final Map<BluetoothStateEnum, Integer> commandsEnum = new LinkedHashMap<BluetoothStateEnum, Integer>();

    static {
        for (BluetoothStateEnum bluetoothType : BluetoothStateEnum.values()) {
            commandsEnum.put(bluetoothType, bluetoothType.bluetoothTypeCode);
        }
    }

    public int getBluetoothTypeCode() {
        return this.bluetoothTypeCode;
    }
}
