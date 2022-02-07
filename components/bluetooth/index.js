import React, { Component } from 'react';

import {ActivityIndicator, TouchableOpacity, View, Text} from 'react-native';

import styles from './styles';


class Bluetooth extends Component{
    constructor(props){
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
    }
}



class ListDevices extends Component{
    constructor(props)
    {
        super(props);
        this.handleClick = this.props.handleClick.bind(this);
    }
    
    render()
    {
        return(
            <View>
                {this.props.devices.map((d, idx) => {
                    return (
                    <TouchableOpacity key={idx} style={styles.btnDevice} onPress={() => this.handleClick(d)}>
                    <View style={styles.btnArea}>
                        <Text style={styles.btnTexto}>{d.name}</Text>
                    </View>
                    </TouchableOpacity>
                    )
                })}
            </View>
        );
    }
}

export default ListDevices