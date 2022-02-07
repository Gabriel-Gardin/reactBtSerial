import { StyleSheet } from 'react-native';

export default StyleSheet.create({
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
      justifyContent: 'flex-start'
    },
    btnDevice:{
      width: 230,
      height: 50,
      borderWidth: 2,
      borderColor: '#dd7b22',
      justifyContent: 'space-evenly',
      alignItems: 'center',
      marginTop: '2.5%',
      padding: 10,
      borderRadius: 7
    },
    button_procurar:{
      width: 230,
      height: 50,
      borderWidth: 2,
      borderColor: '#dd0000',
      justifyContent: 'space-evenly',
      alignItems: 'center',
      marginTop: '2.5%',
      padding: 10,
      borderRadius: 7
    },
    button:{
      width: 150,
      height: 50,
      borderWidth: 2,
      borderColor: '#dd7b22',
      justifyContent: 'space-evenly',
      alignItems: 'center',
      marginTop: '2.5%',
      margin: '1%',
      padding: 10,
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
    btnTextoProcurar:{
      fontSize: 18,
      fontWeight: 'bold',
      color: '#dd0000',
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
  });