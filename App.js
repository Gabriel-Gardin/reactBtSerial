import React, { Component } from 'react';
import BluetoothModule from './nativeModules/Bluetooth';
import functions from './functions';
import Loading from './components/Loading'


import {
  View,
  TextInput,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
  } from 'react-native';
//import { computeWindowedRenderLimits } from 'react-native/Libraries/Lists/VirtualizeUtils';


function bluetoothStateListener(){
  BluetoothModule.addListener('BluetoothState', data => {
    console.log("Bt event:", data.state);
    switch (data.state) {

      case BluetoothStateEnum.BLUETOOTH_ON:
        setBluetoothEnabled(true);
        console.log("Bluetooth habilitado");
        break;

      case BluetoothStateEnum.BLUETOOTH_OFF:
        console.log("Bluetooth desabilitado");
        setBluetoothEnabled(false);
        break;

      case BluetoothStateEnum.BLUETOOTH_CONNECTED:
        console.log("Bluetooth conectado");
        this.setState({btConnected: true});
        
        //Alert.alert("FMB", "CONECTADO");
        break;

      case BluetoothStateEnum.BLUETOOTH_DISCONNECTED:
        console.log("Bluetooth desconectado");
        this.setState({btConnected: false});
        //Alert.alert("FMB", "DESCONECTADO");
        break;

      default:
        break;
    }
  });
};



class App extends Component{

  constructor(props){
    super(props);
    this.state = {
      textoFrase: 'Selecione seu dispositivo',
      bondedDevices: [],
      isLoading: false,
      btConnected: false,
    };
    
    this.sendCommand = this.sendCommand.bind(this);
    this.connectBt = this.connectBt.bind(this);
    this.pegaComando = this.pegaComando.bind(this);
  }

  async componentDidMount(){
    //let a = await BluetoothModule.teste();
    const allow_bt = await BluetoothModule.checkIfDeviceSupportBT();

    if (!allow_bt) {
      Toast.show({
      type: 'error',
      text1: 'BLUETOOTH',
      text2: 'Dispositivo oferece suporte para bluetooth',
      });
    }

    BluetoothModule.askToEnableBluetooth();
    BluetoothModule.initBluetoothStateListener();
    bluetoothStateListener();

    const bondedDevices = await BluetoothModule.getBondedDevices();
    this.setState({bondedDevices: bondedDevices})
    this.setState({btConnected: await BluetoothModule.get_bt_status()})

  }

  async connectBt(btDevice){
    this.setState({textoFrase: "Conectando..."});
    this.setState({isLoading: true});
    const granted = await functions.askPermissions();
  
    if (!granted) {
      Toast.show({
        type: 'info',
        text1: 'Bluetooth',
        text2:
          'Para encontrarmos os dispositivos próximos precisamos do acesso a sua localização.',
      });
    }
  
    BluetoothModule.connect(btDevice.address);
    this.setState({isLoading: false});
    this.setState({textoFrase: "Digite seu comando"});
    this.setState({btConnected: true});
  }

  sendCommand(){
    //Alert.alert("Enviando comando");
    const command = [0x46, 0x4D, 0x42, 0x58, 0x00, 0x00, 0x00, 0x01, 0x00, 0x2e, 0x00, 0x02, 0x00, 0x01, 0xc6, 0x98, 0x0d, 0x0a];
    BluetoothModule.writeFmb(command);
    //BluetoothModule.writeFmb(this.state.comando);
  }

  pegaComando(texto){
    this.setState({comando: texto})
  }

  async desconectBt(){
    if(await BluetoothModule.get_bt_status()){
      await BluetoothModule.close_bt_connection();
    }
    else{
      Alert.alert("FMB", "Já desconectado");
    }
  }


  render(){
    return(
      <View style={styles.container}>

        <Text style={styles.textoFrase}>{this.state.textoFrase}</Text>

        {this.state.isLoading &&  (
        <Loading></Loading>
      )}

        <ScrollView>
        {!this.state.isLoading &&  this.state.btConnected && (
          <View>
          <TextInput style={styles.input}
          placeholder='Digite o comando'
          onChangeText={this.pegaComando} />
      
          <TouchableOpacity style={styles.button} onPress={() => this.sendCommand()}>
            <View style={styles.btnArea}>
              <Text style={styles.btnTexto}>Enviar comando</Text>
            </View>
          </TouchableOpacity>

          <TouchableOpacity style={styles.button} onPress={() => this.desconectBt()}>
            <View style={styles.btnArea}>
              <Text style={styles.btnTexto}>Desconectar</Text>
            </View>
          </TouchableOpacity>
          </View>
      )}

        {!this.state.isLoading && !this.state.btConnected && this.state.bondedDevices.map((d, idx) => {
          return (
          <TouchableOpacity key={idx} style={styles.button} onPress={() => this.connectBt(d)}>
            <View style={styles.btnArea}>
              <Text style={styles.btnTexto}>{d.name}</Text>
            </View>
          </TouchableOpacity>)
        })}
        </ScrollView>


      </View>
    );
  }
}

const styles = StyleSheet.create({
  container:{
    flex:1,
    paddingTop: 20,
    alignItems: 'center',
    justifyContent: 'center'
  },
  img:{
    width:250,
    height: 250
  },
  textoFrase:{
    fontSize:20,
    color: '#dd7b22',
    margin: 30,
  },
  button:{
    width: 230,
    height: 50,
    borderWidth: 2,
    borderColor: '#dd7b22',
    justifyContent: 'space-evenly',
    alignItems: 'center',
    marginTop: '2.5%',
    borderRadius: 7
  },
  btnArea:{
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center'
  },
  btnTexto:{
    fontSize: 18,
    fontWeight: 'bold',
    color: '#dd7b22',
  },
  input:{
    height:45,
    borderWidth: 1,
    borderColor: '#222',
    borderColor: '#dd7b22',
    margin: 10,
    fontSize: 20,
    padding: 10,
    borderRadius: 7

  }
})


export default App;