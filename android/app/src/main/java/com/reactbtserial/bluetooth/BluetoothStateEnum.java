package com.reactbtserial.bluetooth;

import java.util.LinkedHashMap;
import java.util.Map;

public enum BluetoothStateEnum {
    BLUETOOTH_ON(1),
    BLUETOOTH_OFF(2),
    BLUETOOTH_TURNING_ON(3),
    BLUETOOTH_TURNING_OFF(4);

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
