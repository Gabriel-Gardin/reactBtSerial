package com.reactbtserial.bluetooth;

import java.util.LinkedHashMap;
import java.util.Map;

/*
Enum de utilitário para representar as opções de transmissão para o app react-native. 
Esta enum é utilizado muito em conjunto com o JSEventManager.
*/
public enum BluetoothStateEnum {
    BLUETOOTH_OFF(0),
    BLUETOOTH_ON(1),
    BLUETOOTH_DESCONECTADO(2),
    BLUETOOTH_CONECTADO(3),
    BLUETOOTH_INTERUPTED(4),
    BLUETOOTH_DATA(5);

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
