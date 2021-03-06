import React, { Component } from 'react';
import BluetoothModule from './nativeModules/Bluetooth';
import functions from './functions';
import Loading from './components/Loading';
import ListDevices from './components/bluetooth';
import styles from './styles';



import {
  View,
  TextInput,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
  NativeEventEmitter,
  ToastAndroid,
  } from 'react-native';

class App extends Component{

  constructor(props)
  {
    super(props);
    this.state = {
      textoFrase: 'Selecione seu dispositivo',
      bondedDevices: [],
      isLoading: false,
      btConnected: false,
      btOn: false,
      dadosBt: '',
      availableDevices: [],
      scanReady: false

    };
    
    this.sendCommand = this.sendCommand.bind(this);
    this.connectBt = this.connectBt.bind(this);
    this.pegaComando = this.pegaComando.bind(this);
    this.escanearBT = this.escanearBT.bind(this);
  }

  async componentDidMount()
  {
    //let a = await BluetoothModule.teste();
    const allow_bt = await BluetoothModule.checkIfDeviceSupportBT();

    if (!allow_bt) {
      ToastAndroid.show("Dispositivo não oferece suporte para bluetooth", ToastAndroid.LONG);
    }

    BluetoothModule.askToEnableBluetooth();
    BluetoothModule.initBluetoothStateListener();

    const eventEmitter = new NativeEventEmitter();

    this.eventListener = eventEmitter.addListener('BluetoothState', (event) => this.processEvents(event));


    const bondedDevices = await BluetoothModule.getBondedDevices();
    this.setState({bondedDevices: bondedDevices});
    this.setState({btConnected: await BluetoothModule.get_bt_status()});
    this.setState({btOn: await BluetoothModule.isBluetoothEnabled()});
  }
  
  componentWillUnmount()
  {
    this.eventListener.remove(); //Removes the listener
  }

  processEvents(event)
  {
    console.log(event.state);
    switch (event.state) {
      case 0:  //BT off
        this.setState({btOn: false});
        break;

      case 1: //BT ON
        this.setState({btOn: true});
        break;

      case 2:  //BT Desconectado
        console.log("Bluetooth desconectado");
        ToastAndroid.show("Conexão finalizada", ToastAndroid.SHORT);
        this.setState({btConnected: false});
        break;

      case 3:  //BT Conectado
        ToastAndroid.show("Conectado", ToastAndroid.SHORT);
        this.setState({btConnected: true});
        this.setState({textoFrase: "Digite seu comando"});
        break;

      case 4:  //BT Conexão caiu
        this.setState({btConnected: false});
        Alert.alert("Conexão interrompida!");
        this.setState({textoFrase: "Selecione um dispositivo..."});
        break;
      
      case 5: //Dados bluetooth
        console.log("Dados: ", event.dados);
        this.setState({dadosBt: event.dados});
        break;

      default:
        break;
    }
  }

  async connectBt(btDevice)
  {
    if(this.state.btOn)
    {
      this.setState({textoFrase: "Conectando..."});
      this.setState({isLoading: true});
      const granted = await functions.askPermissions();
    
      if (!granted) {
        ToastAndroid.show("Para conectar aos dispositivos precisamos de acesso a sua localização", ToastAndroid.LONG);
      }
    
      BluetoothModule.connect(btDevice.address);
      this.setState({isLoading: false});
      if(!this.state.btConnected){
        this.setState({textoFrase: "Selecione um dispositivo..."});
      }
    }
    else
    {
      this.setState({textoFrase: "Tente novamente..."});
      BluetoothModule.askToEnableBluetooth();
    }
  }

  sendCommand()
  {
    //Alert.alert("Enviando comando");
    //const command = [0x46, 0x4D, 0x42, 0x58, 0x00, 0x00, 0x00, 0x01, 0x00, 0x2e, 0x00, 0x02, 0x00, 0x01, 0xc6, 0x98, 0x0d, 0x0a];
    //BluetoothModule.writeFmb(command);

    if(this.state.comando){
      BluetoothModule.writeStringCommand(this.state.comando);
    }
    //BluetoothModule.writeFmb(this.state.comando);
  }

  async escanearBT()
  {
    this.setState({isLoading: true});
    this.setState({textoFrase: "Procurando novos dispositivos"});
    
    const devices = await BluetoothModule.discovery();
    this.setState({availableDevices: devices});
    this.setState({scanReady: true});
    this.setState({isLoading: false});
    this.setState({textoFrase: "Busca finalizada"});
  }

  pegaComando(texto)
  {
    this.setState({comando: texto})
  }

  async desconectBt()
  {
    if(await BluetoothModule.get_bt_status()){
      await BluetoothModule.close_bt_connection();
    }
    else{
      Alert.alert("FMB", "Já desconectado");
    }
  }


  render()
  {
    return(
      <View style={styles.container}>

        <View style={{justifyContent: 'flex-end'}}>
          <Text style={styles.textoFrase}>{this.state.textoFrase}</Text>
        </View>
        {this.state.isLoading &&  (
        <Loading />
      )}

      {!this.state.isLoading &&  this.state.btConnected && (
        <ScrollView
          contentContainerStyle={{ flexGrow: 1, justifyContent: 'flex-end', flexDirection: 'column' }}
          style={{ backgroundColor: 'white', paddingBottom: 30, padding: 20 }}>
          <View>
            <Text>{this.state.dadosBt}</Text>
          </View>
          <View>
            <TextInput style={styles.input}
            placeholder='Digite o comando'
            onChangeText={this.pegaComando} />
          
            <View style={{flexDirection:"row"}}>
              <TouchableOpacity style={styles.button} onPress={() => this.sendCommand()}>
                <View style={styles.btnArea}>
                  <Text style={styles.btnTexto}>Enviar</Text>
                </View>
              </TouchableOpacity>

              <TouchableOpacity style={styles.button} onPress={() => this.desconectBt()}>
                <View style={styles.btnArea}>
                  <Text style={styles.btnTexto}>Desconectar</Text>
                </View>
              </TouchableOpacity>
            </View>
          </View>
        </ScrollView>
        )}

        {!this.state.isLoading && !this.state.btConnected && !this.state.scanReady &&(
          <ScrollView>
            <TouchableOpacity style={styles.button_procurar} onPress={() => this.escanearBT()}>
              <View style={styles.btnArea}>
                <Text style={styles.btnTextoProcurar}>Procurar dispositivos</Text>
              </View>
            </TouchableOpacity>

        
            <View style={{paddingTop: 20, justifyContent: 'center', alignItems: 'center'}}>
              <Text style={{fontSize: 18, fontWeight: 'bold'}}>Dispositivos já pareados</Text>
              <ListDevices devices={this.state.bondedDevices} handleClick={this.connectBt}></ListDevices> 
            </View>
          </ScrollView>
          )}


        {!this.state.isLoading && this.state.scanReady && !this.state.btConnected &&(
          <ScrollView>
            <TouchableOpacity style={styles.button_procurar} onPress={() => this.escanearBT()}>
              <View style={styles.btnArea}>
                <Text style={styles.btnTextoProcurar}>Procurar dispositivos</Text>
              </View>
            </TouchableOpacity>

            <View style={{paddingTop: 20, justifyContent: 'center', alignItems: 'center'}}>
              <Text style={{fontSize: 18, fontWeight: 'bold'}}>Dispositivos disponíveis</Text>
              <ListDevices devices={this.state.availableDevices} handleClick={this.connectBt} />
            </View>
          </ScrollView>
          )}
      </View>
    );
  }
}

export default App;