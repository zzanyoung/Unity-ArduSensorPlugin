using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Android;
using UnityEngine.UI;

namespace ArduSensor
{
    public class AarBridge : MonoBehaviour
    {
        [SerializeField] Text test, imuCheck, imuValue, envCheck, envValue;

        private const string USB_PERMISSION = "android.permission.USB_PERMISSION";
        private const string IMU_PERMISSION = "android.permission.BODY_SENSORS";

        private AndroidJavaObject activityContext = null;
        private AndroidJavaClass javaClass = null;
        private AndroidJavaObject javaClassInstance = null;

        void Awake()
        {
            using (AndroidJavaClass activityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
            {
                activityContext = activityClass.GetStatic<AndroidJavaObject>("currentActivity");
            }
        }

        private void Start()
        {
            using (javaClass = new AndroidJavaClass("com.serenegiant.opencvwithuvc.Plugin"))
            {
                if (javaClass != null)
                {
                    javaClassInstance = javaClass.CallStatic<AndroidJavaObject>("instance");
                    javaClassInstance.Call("setContext", activityContext);
                }
            }
            if (!Permission.HasUserAuthorizedPermission(USB_PERMISSION))
                Permission.RequestUserPermission(USB_PERMISSION);

            Permission.RequestUserPermission(IMU_PERMISSION);

            if (javaClassInstance.Call<int>("GetValue") == 888)
                test.text = "Plugin Activate";
        }

        public void OnCreate() //Create Java Instance
        {
            javaClassInstance.Call("OnCreate");

        }
        public void VirtualClick() //USB Permission
        {
            javaClassInstance.Call("VirtualClick");
        }

        public void Connected() //Check Connection
        {
            var isImuConnected = javaClassInstance.Call<bool>("IsImuConnected");
            var isEnvConnected = javaClassInstance.Call<bool>("IsEnvConnected");

            if (javaClassInstance.Call<bool>("IsImuConnected") == true)
            {
                imuCheck.text = "Imu Connected";
            }
            else
            {
                imuCheck.text = "Please try again";
            }

            if (javaClassInstance.Call<bool>("IsEnvConnected") == true)
            {
                envCheck.text = "Env Connected";
            }
            else
            {
                envCheck.text = "Please try again";
            }
        }

        public void GetValue() //Get Value
        {
            Connected();
            InvokeRepeating(nameof(TakeImuValue), 0f, 0.1f);
            InvokeRepeating(nameof(TakeEnvValue), 0f, 4.1f);
        }

        public void TakeImuValue()
        {
            javaClassInstance.Call("GetImuSensorData");

            var imuStrOrigin = javaClassInstance.Call<string>("GetImuUnity");

            string[] imuStrSplit = imuStrOrigin.Substring(4).Split(',');

            imuValue.text = null;
            imuValue.text = "X: " + imuStrSplit[0] + "\r\nY: " + imuStrSplit[1] + "\r\nZ: " + imuStrSplit[2];

            if (imuStrOrigin != null) imuCheck.text = "Imu Connected";
            if (!javaClassInstance.Call<bool>("IsImuConnected")) imuCheck.text = "Disconnected";
        }

        public void TakeEnvValue() //Get 
        {
            javaClassInstance.Call("GetEnvSensorData");

            var envStrOrigin = javaClassInstance.Call<string>("GetEnvUnity");

            string[] envStrSplit = envStrOrigin.Substring(3).Split(',');

            envValue.text = "Temp: " + envStrSplit[0] + "\r\nHum " + envStrSplit[1]
                    + "\r\nPress: " + envStrSplit[2] + "\r\nGas_Resist: " + envStrSplit[3]
                    + "\r\nAltitude: " + envStrSplit[4] + "\r\nCO2: " + envStrSplit[5] + "\r\nTVOC: " + envStrSplit[6]
                    + "\r\nLux: " + envStrSplit[7];
            if (envStrOrigin != null) envCheck.text = "Env Connected";
            if (!javaClassInstance.Call<bool>("IsEnvConnected")) envCheck.text = "Disconnected";
        }

        public void CancelAarInvoke() //Disconnect
        {
            CancelInvoke();
            imuCheck.text = "USB Connection Status";
            envCheck.text = "USB Connection Status";
            envValue.text = "Display sensro data with a 4-sec interval";
            imuValue.text = "Display sensro data with a 0.1-sec interval";
        }
    }
}