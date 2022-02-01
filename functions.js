import {PermissionsAndroid} from 'react-native';

import RNAndroidLocationEnabler from 'react-native-android-location-enabler';

export default {
  askPermissions: async () => {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
    );

    await RNAndroidLocationEnabler.promptForEnableLocationIfNeeded({
      interval: 10000,
      fastInterval: 5000,
    });

    return granted === PermissionsAndroid.RESULTS.GRANTED;
  },
};
