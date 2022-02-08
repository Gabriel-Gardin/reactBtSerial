import React, { Component } from 'react';

import {TouchableOpacity, View, Text} from 'react-native';

import styles from './styles';

/*
* Componente utilizado para renderizar uma lista de dispositivos BT.
*/
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