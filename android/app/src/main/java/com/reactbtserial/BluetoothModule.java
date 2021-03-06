package com.reactbtserial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;

import com.reactbtserial.bluetooth.BluetoothState;
import com.reactbtserial.bluetooth.JSEventManager;
import com.reactbtserial.bluetooth.ConnectThread;
import com.reactbtserial.bluetooth.CommunicationThread;
import com.reactbtserial.bluetooth.Discovery;
import com.reactbtserial.bluetooth.NativeDevice;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReadableArray;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

/*
* Esta classe representa o módulo nativo para ser consumido pela aplicação em react native. 
* Aqui expomos uma API para todas as ferramentas necessárias para:
* - Escanear novos dispositivos bluetooth;
* - Parear e conectar em novos dispositivos;
* - Conectar em dispositivos já pareados;
* - Notifica a aplicação caso a conexão caia;
* - Transmite os dados recebidos pelo equipamento bluetooth através da classe JSEventManager
* - Fechar as conexões em aberto.
*/
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
    private BroadcastReceiver mDiscoveryReceiver;
    private JSEventManager mEventManager;

    public BluetoothModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.mEventManager = new JSEventManager(this.reactContext);
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
     * Inicializa o bluetooth state listener
     * Este modulo é responsável por transmitir eventos de bluetooth como bluetooth
     * ligado/desligado para a nossa aplicação.
     */
    @ReactMethod
    public void initBluetoothStateListener() {
        this.bluetoothStateReciver = new BluetoothState(this.mEventManager);
        IntentFilter bluetoothStateFilter = new IntentFilter();
        bluetoothStateFilter.addAction(this.bluetoothAdapter.ACTION_STATE_CHANGED);

        getReactApplicationContext().registerReceiver(
                bluetoothStateReciver,
                bluetoothStateFilter);
    }

    @ReactMethod
    public void isBluetoothEnabled(Promise promise) {
        promise.resolve(this.checkBluetoothAdapter());
    }

    /**
     * Retorna uma lista de dispositivos já pareados
     */
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

    /**
     * Conecta a um dispositivo a partir de seu adress
     * Esta função utiliza a classe ConnecThread para inicializar a conexão e a
     * classe CommunicationThread que fica o tempo todo monitorando o estado da
     * conexão e recebendo dados.
     */
    @ReactMethod
    public void connect(String address) throws IOException {

        BluetoothDevice device = this.bluetoothAdapter.getRemoteDevice(address);
        NativeDevice nativeDevice = new NativeDevice(device);
        ConnectThread connection = new ConnectThread(
                device,
                this.bluetoothAdapter,
                this.mEventManager);

        connection.run(); // Inicia a thread de conexão

        Log.e(TAG, "Communication iniciada");

        int i = 0;
        while (i < 15) {
            i++;
            try {
                Thread.currentThread().sleep(200); // Pausa a thread por 200ms
                if (connection.connected) {
                    this.connection = connection;
                    this.mmSocket = this.connection.getMmSocket();
                    CommunicationThread communication = new CommunicationThread(this.mmSocket, this.mEventManager);
                    communication.start(); // Inicia a threa que recebe dados pelo socket bluetooth.
                    this.communication = communication;
                    return;
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Fecha uma conexão ativa com um dispositivo.
     * Termina a thread de communicação e a thread de conexão.
     */
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

    /*
     * Retorna o estado atual da conexão.
     */
    @ReactMethod
    public void get_bt_status(Promise promise) {
        if (this.connection == null) {
            promise.resolve(false);
            return;
        } else {
            promise.resolve(this.connection.connected);
        }
    }

    /*
     * Envia comandos em binário para o dispositivo conectado.
     */
    @ReactMethod
    public void writeFmb(ReadableArray message, Promise promise) throws IOException {

        final byte[] res;
        res = new byte[message.size()];

        for (int i = 0; i < message.size(); i++) {
            res[i] = ((byte) message.getDouble(i));
        }

        this.communication.write(res);
    }

    @ReactMethod
    public void writeStringCommand(String message, Promise promise) throws IOException {
        String msg = message + "\r\n";
        this.communication.write(msg.getBytes());
    }

    private boolean checkBluetoothAdapter() {
        return (this.bluetoothAdapter != null && this.bluetoothAdapter.isEnabled());
    }

    /*
     * Inicializa o processo de discovery de equipamentos bluetooth e retorna uma
     * lista dos dispositivos disponíveis.
     */
    @ReactMethod
    public void discovery(final Promise promise) {
        Log.i(TAG, "Iniciando discovery");

        mDiscoveryReceiver = new Discovery(new Discovery.DiscoveryCallback() {
            @Override
            public void onDeviceDiscovered(NativeDevice device) {
                // TODO: Renderizar item a item retornando por aqui...
                return;
            }

            @Override
            public void onDiscoveryFinished(Collection<NativeDevice> devices) {
                Log.i(TAG, "Acabou o discovery!!");
                WritableArray array = Arguments.createArray();
                for (NativeDevice device : devices) {
                    array.pushMap(device.map());
                    Log.i(TAG, String.format("Encontrou equipamento: %s", device.getName()));
                }

                promise.resolve(array);
                mDiscoveryReceiver = null;
            }

            @Override
            public void onDiscoveryFailed(Throwable e) {
                mDiscoveryReceiver = null;
            }
        });

        getReactApplicationContext().registerReceiver(mDiscoveryReceiver,
                Discovery.intentFilter());

        this.bluetoothAdapter.startDiscovery();
    }
}