
# react-native-benibeacon

## Getting started

`$ npm install react-native-benibeacon --save`

### Mostly automatic installation

`$ react-native link react-native-benibeacon`

### Manual installation


#### Android

1. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-benibeacon'
  	project(':react-native-benibeacon').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-benibeacon/android')
  	```
2. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-benibeacon')
  	```
3. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.mhandharbeni.benibeacon.RNBenibeaconPackage;` to the imports at the top of the file
  - Add `new RNBenibeaconPackage()` to the list returned by the `getPackages()` method

4. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.mhandharbeni.benibeacon.RNBenibeaconModule;` to the imports at the top of the file
  - Add ` RNBenibeaconModule.checkPermissions(MainActivity.this, getApplicationContext());` to the `onCreate` method


## Usage
```javascript
import RNBenibeacon from 'react-native-benibeacon';

// TODO: What to do with the module?
RNBenibeacon;
```
  