import React, { Component } from 'react';
import BluetoothModule from './nativeModules/Bluetooth'


import {
  View,
  TextInput,
  Text,
  StyleSheet,
  Image,
  TouchableOpacity
  } from 'react-native';


class App extends Component{

  constructor(props){
    super(props);
    this.state = {
      textoFrase: 'Inicio'
    };
    this.sendCommand = this.sendCommand.bind(this);
  }

  async sendCommand(){

    //let a = await BluetoothModule.teste();
    await BluetoothModule.checkIfDeviceSupportBT();
    let a = await BluetoothModule.discovery();
    console.log("aejwaiowjiawd");
    console.log(typeof a)
    this.setState({textoFrase: a[100]});
  }

  render(){
    return(
      <View style={styles.container}>

        <Text style={styles.textoFrase}>{this.state.textoFrase}</Text>

        <TouchableOpacity style={styles.button} onPress={this.sendCommand}>
          <View style={styles.btnArea}>
            <Text style={styles.btnTexto}>Enviar</Text>
          </View>
        </TouchableOpacity>
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
  }
})


export default App;