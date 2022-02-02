/**
* This exposes the native CalendarModule module as a JS module. This has a
* function 'createCalendarEvent' which takes the following parameters:

* 1. String name: A string representing the name of the event
* 2. String location: A string representing the location of the event
*/
import { NativeModules } from 'react-native';

const { BluetoothModule } = NativeModules;
// const {BLUETOOTH_ON} = 0;
// const {BLUETOOTH_OFF} = 1;
// const {BLUETOOTH_DISCONNECTED} = 2;
// const {BLUETOOTH_CONNECTED} = 3;


export default BluetoothModule;  