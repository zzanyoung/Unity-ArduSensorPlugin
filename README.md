![arduSensor-Cover](https://github.com/zzanyoung/Unity-ArduSensorPlugin/assets/53194702/f010c2cb-267a-433d-84dc-c7395d436093)

# Unity-ArduSensorPlugin

ArduSensor Plugin is a Unity plugin that receives sensor values from an Arduino board communicating with Android via serial communication. The Plugin is implemented in Android native code, and the invocation takes place in Unity.Implemented sensors include BME680 and SGP30, which detect temperature, humidity, pressure, gas resistance, altitude, CO2, and TVOC. The sensor values are updated every 4 seconds.


 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

All files in the folder are under this Apache License, Version 2.0.
Files in the jni/libjpeg, jni/libusb and jin/libuvc folders may have a different license,
see the respective files.


# **How to use:**

Import the plugin package and build the demo scene to verify the AAR plugin is being imported correctly. 
If not, refer to Unity's official documentation* to properly import the plugin.
All implementations are written in the **AarBridge.cs** file. Create a Java instance to communicate with Arduino through Android's Native Methods.

* Official Unity documentation related to AAR Plugin :
https://docs.unity3d.com/kr/2021.3/Manual/AndroidAARPlugins.html
* Note: This plugin only works when built for the Android platform.
* This plugin requires Android SDK 26 or higher.


# Technical Details

The code for implementing USB serial communication in Android is included in the aar plugin.
The aar plugin contains library and Android Activity for USB serial connection.
It opens the port and retrieves the vendor ID and device ID of the sensor connected to the Arduino.
It receives sensor data measured from the device.
It implements a method for transmitting the measured sensor data to Unity.

In Unity, the measured values from the Arduino sensor are displayed.
Corresponding C# scripts are implemented for the methods in the aar plugin. Android Java Object and Android Java Instance have been used.
The implementation of the UI connected to this C# script is also included. It can be customized by assigning to button events


# Assisted Projects
@mik3y https://github.com/mik3y/usb-serial-for-android
