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

        private AndroidJavaObject activityContext = null;
        private AndroidJavaClass javaClass = null;
        private AndroidJavaObject javaClassInstance = null;

        private bool isOnInvoke = false;

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

            if (javaClassInstance.Call<int>("GetValue") == 888)
                test.text = "Plugin Connected";
        }

        public void OnCreate()
        {
            javaClassInstance.Call("OnCreate");

        }
        public void VirtualClick()
        {
            javaClassInstance.Call("VirtualClick");
        }

        public void Connected()
        {
            var isEnvConnected = javaClassInstance.Call<bool>("IsEnvConnected");
            if (javaClassInstance.Call<bool>("IsEnvConnected") == true)
            {
                envCheck.text = "USB Connected";
            }
            else
            {
                envCheck.text = "USB Device Not Found";
                envValue.text = "Please try again after a moment.";
            }
        }

        public void GetValue()
        {
            Connected();
            if (isOnInvoke == false)
                InvokeRepeating(nameof(TakeEnvValue), 0f, 4f);
        }

        public void TakeEnvValue()
        {
            javaClassInstance.Call("GetEnvSensorData");
            var envStrOrigin = javaClassInstance.Call<string>("GetEnvUnity");
            string[] envStrSplit = envStrOrigin.Substring(3).Split(',');

            isOnInvoke = true;
            envValue.text = "Temp " + envStrSplit[0] + "\r\nHum " + envStrSplit[1]
                    + "\r\nPress: " + envStrSplit[2] + "\r\ngas_resist: " + envStrSplit[3]
                    + "\r\nAltitude: " + envStrSplit[4] + "\r\nCO2: " + envStrSplit[5] + "\r\nTVOC: " + envStrSplit[6];
            if (envStrOrigin != null) envCheck.text = "USB Connected";
            if (!javaClassInstance.Call<bool>("IsEnvConnected")) envCheck.text = "USB Device Not Found";
        }

        public void CancelAarInvoke()
        {
            CancelInvoke();
            envValue.text = "";
            isOnInvoke = false;
            envCheck.text = "USB Device Not Found";
        }
    }
}