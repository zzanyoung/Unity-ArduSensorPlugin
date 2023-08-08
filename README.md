![arduSensor-Cover](https://github.com/zzanyoung/Unity-ArduSensorPlugin/assets/53194702/f010c2cb-267a-433d-84dc-c7395d436093)

# Unity-ArduSensorPlugin

ArduSensor Plugin is a Unity plugin that receives sensor values from an Arduino board communicating with Android via serial communication. The plugin is implemented using Android native code, and the call takes place in Unity. The supported sensors include BME680 and SGP30, which detect temperature, humidity, pressure, gas resistance, altitude, CO2, and TVOC. The sensor values are updated every 4 seconds.

# **How to use:**

Import the plugin package and build the demo scene to verify the AAR plugin is being imported correctly.
If not, refer to Unity's official documentation* to properly import the plugin.
All implementations are written in the AarBridge.cs file. Create a Java instance to communicate with Arduino through Android's Native Methods.

- Official Unity documentation related to AAR Plugin :
https://docs.unity3d.com/kr/2021.3/Manual/AndroidAARPlugins.html
- This plugin only works when built for the Android platform.
- This plugin requires Android SDK 26 or higher.


# Technical Details

The code for implementing USB serial communication in Android is included in the aar plugin.
The aar plugin contains library and Android Activity for USB serial connection.
It opens the port and retrieves the vendor ID and device ID of the sensor connected to the Arduino.
It receives sensor data measured from the device.
It implements a method for transmitting the measured sensor data to Unity. In Unity, the measured values from the Arduino sensor are displayed.
Corresponding C# scripts are implemented for the methods in the aar plugin. Android Java Object and Android Java Instance have been used.
The implementation of the UI connected to this C# script is also included. It can be customized by assigning to button events.


# Assisted Projects
@mik3y https://github.com/mik3y/usb-serial-for-android

 If you'd like to learn more about the serial connection library or are interested in related discussions, please refer to the repository below.

# License
MIT License

Copyright (c) 2011-2013 Google Inc.

Copyright (c) 2013 Mike Wakerly

Copyright (c) 2023 Chanyoung Kim

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
