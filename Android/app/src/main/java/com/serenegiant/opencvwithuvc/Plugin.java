package com.serenegiant.opencvwithuvc;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.unity3d.player.UnityPlayer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class Plugin extends Service implements
        View.OnClickListener {

    //
    private static final String TAG = "UsbConnectionService";
    private static final String ACTION_USB_PERMISSION = "com.serenegiant.opencvwithuvc.USB_PERMISSION";
    private PendingIntent permissionIntent;
    private BroadcastReceiver usbReceiver;
    //

    private static final boolean DEBUG = true;
    Button TrackOnly;
    boolean checkPermission;

    protected static final int SETTINGS_HIDE_DELAY_MS = 2500;
    private UsbDevice deviceImu;
    private UsbDevice deviceEnv;
    private UsbManager usbManager;

    private UsbSerialPort port;
    private TextView tvIMUSensor;
    private TextView tvEnvSensor;

    private enum UsbPermission { Unknown, Requested, Granted, Denied }

    private static final String INTENT_ACTION_GRANT_USB = /*BuildConfig.APPLICATION_ID + */ ".GRANT_USB";
    private static final int WRITE_WAIT_MILLIS = 2000;
    private static final int READ_WAIT_MILLIS = 2000;

    private int deviceId, portNum, baudRate;
    private boolean withIoManager;

    private SerialInputOutputManager usbIoManager;
    private UsbSerialPort envPort;
    private UsbSerialPort imuPort;
    private UsbPermission usbPermission = UsbPermission.Unknown;
    private boolean envConnected = false;
    private boolean imuConnected = false;

    private final static int EnvSensorDevID = 2003;
    private static Plugin m_instance;
    private Context context;
    private Queue<String> imuQ = new LinkedList<>();
    private Queue<String> envQ = new LinkedList<>();


    public void OnCreate() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver(), filter);

        usbManager.requestPermission(deviceImu, permissionIntent);
        usbManager.requestPermission(deviceEnv, permissionIntent);
    }

    private BroadcastReceiver usbReceiver() {
        if (usbReceiver == null) {
            usbReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ACTION_USB_PERMISSION.equals(action)) {
                        synchronized (this) {
                            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                if (device != null) {
                                    // USB 접근 권한이 허용된 경우 해당 장치의 연결을 처리합니다.
                                }
                            } else {
                                Log.d(TAG, "permission denied for device " + device);
                            }
                        }
                    }
                }
            };
        }
        return usbReceiver;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static class ListItem {
        UsbDevice device;
        int port;
        UsbSerialDriver driver;

        ListItem(UsbDevice device, int port, UsbSerialDriver driver) {
            this.device = device;
            this.port = port;
            this.driver = driver;
        }
    }

    public String GetImuSensorData() {
            try {
                String envData;
                byte[] response = new byte[1024];
                //int len = port.read(response, 100);
                int len = imuPort.read(response, 100);

                response[len] = '\0';

                //String rxStr = response.toString();
                String rxStr = new String(response, 0, len);
                //rxStr.replace("\r\n", "\0");
                rxStr.trim();

                imuQ.offer("IMU" + rxStr);

                Log.e(TAG, "IMU[" + len + "]" + rxStr);

            } catch (IOException e) {
                e.printStackTrace();
            }
        return null;
    }

    public String GetEnvSensorData() {
            try {
                String imuData;
                byte[] response = new byte[1024];
                int len = envPort.read(response, 100);

                response[len] = '\0';

                String rxStr = new String(response, 0, len);
                rxStr.trim();

                envQ.offer("Env" + rxStr);

                Log.e(TAG, "Env[" + len + "]" + rxStr);

            } catch (IOException e) {
                e.printStackTrace();
            }
        return null;
    }

    private void connectForEnvSensor() {
        UsbDevice envDevice = null;

        usbManager = (UsbManager) UnityPlayer.currentActivity.getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values()) {
            //if(v.getDeviceId() == EnvSensorDevID) // deviceId == 2003  DeviceID 대신 driver 문자열로 설정
            //    envDevice = v;
            if (v.getProductId() == 8963) envDevice = v;
        }

        if(envDevice == null) {
            Log.e(TAG, "connection failed: env sensor device not found");
            return;
        }

        UsbSerialDriver envDriver = UsbSerialProber.getDefaultProber().probeDevice(envDevice);
        if(envDriver == null) {
            envDriver = CustomProber.getCustomProber().probeDevice(envDevice);
        }
        if(envDriver == null) {
            Log.e(TAG, "connection failed: no driver for device");
            return;
        }
        if(envDriver.getPorts().size() < portNum) {
            Log.e(TAG, "connection failed: not enough ports at device");
            return;
        }
        envPort = envDriver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(envDevice);
        if(usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(envDriver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(UnityPlayer.currentActivity.getApplicationContext(), 0, new Intent(INTENT_ACTION_GRANT_USB), PendingIntent.FLAG_IMMUTABLE);
            usbManager.requestPermission(envDriver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(envDriver.getDevice()))
                Log.e(TAG, "connection failed(ENV): permission denied");
            else
                Log.e(TAG, "connection failed: open failed");
            return;
        }

        try {
            envPort.open(usbConnection);
            envPort.setParameters(9600, 8, 1, UsbSerialPort.PARITY_NONE);
            Log.e(TAG, "Env Sensor connected");
            envConnected = true;
        } catch (Exception e) {
            Log.e(TAG, "connection failed (Env): " + e.getMessage());
            disconnectForEnvSensor();
        }
    }

    private void connectForIMUSensor() {
        UsbDevice imuDevice = null;
        usbManager = (UsbManager) UnityPlayer.currentActivity.getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values()) {
            if (v.getProductId() == 22336) imuDevice = v;
        }

        if(imuDevice == null) {
            Log.e(TAG, "connection failed: IMU sensor device not found");
            return;
        }

        UsbSerialDriver imuDriver = UsbSerialProber.getDefaultProber().probeDevice(imuDevice);
        if(imuDriver == null) {
            imuDriver = CustomProber.getCustomProber().probeDevice(imuDevice);
        }
        if(imuDriver == null) {
            Log.e(TAG, "connection failed: no driver for imu device");
            return;
        }
        if(imuDriver.getPorts().size() < portNum) {
            Log.e(TAG, "connection failed: not enough ports at imu device");
            return;
        }
        imuPort = imuDriver.getPorts().get(portNum);
        //UsbDeviceConnection usbConnection = usbManager.openDevice(imuDriver.getDevice());
        UsbDeviceConnection usbConnection = usbManager.openDevice(imuDevice);
        if(usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(imuDriver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            //PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            PendingIntent usbPermissionIntent2 = PendingIntent.getBroadcast(UnityPlayer.currentActivity.getApplicationContext(), 0, new Intent(INTENT_ACTION_GRANT_USB), PendingIntent.FLAG_IMMUTABLE);
            usbManager.requestPermission(imuDriver.getDevice(), usbPermissionIntent2);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(imuDriver.getDevice()))
                Log.e(TAG, "connection failed (IMU): permission denied");
            else
                Log.e(TAG, "connection failed (IMU): open failed");
            return;
        }

        try {
            imuPort.open(usbConnection);
            imuPort.setParameters(9600, 8, 1, UsbSerialPort.PARITY_NONE);
            //if(withIoManager) {
            //    usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
            //    usbIoManager.start();
            //}
            Log.e(TAG, "IMU Sensor connected");
            imuConnected = true;
            //controlLines.start();
        } catch (Exception e) {
            Log.e(TAG, "connection failed (IMU): " + e.getMessage());
            disconnectForIMUSensor();
        }
    }

    private void disconnectForEnvSensor() {
        envConnected = false;
        //controlLines.stop();
        if(usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
        }
        usbIoManager = null;
        try {
            envPort.close();
        } catch (IOException ignored) {}
        envPort = null;
    }

    private void disconnectForIMUSensor() {
        envConnected = false;
        //controlLines.stop();
        if(usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
        }
        usbIoManager = null;
        try {
            imuPort.close();
        } catch (IOException ignored) {}
        imuPort = null;
    }

    //maxiaoba
    @Override
    public void onClick(View v) {}

    public void VirtualClick()
    {
            usbManager = (UsbManager) UnityPlayer.currentActivity.getSystemService(Context.USB_SERVICE);
            UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
            UsbSerialProber usbCustomProber = CustomProber.getCustomProber();

            for (UsbDevice device : usbManager.getDeviceList().values()) {
                UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
                if (driver == null) {
                    driver = usbCustomProber.probeDevice(device);
                }
                if (driver != null) {
                    for (int port = 0; port < driver.getPorts().size(); port++) {
                        Log.e(TAG, "device: " + device.getDeviceId() + ", product id: " + driver.getDevice().getProductId() + ", port: " + port + ", driver: " + driver.toString());
                    }
                }
                connectForEnvSensor();
                connectForIMUSensor();
            }

    }

    //public static Plugin instance(){
    private static Plugin instance() {
        if (m_instance == null) {
            m_instance = new Plugin();
        }
        return m_instance;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    private int GetValue()
    {
        return 888;
    }

    private String GetImuUnity()
    {
        if(imuQ.size() > 0)
        {
            return imuQ.poll();
        }
        else
        {
            return null;
        }
    }

    private String GetEnvUnity()
    {
        if(envQ.size() > 0)
        {
            return envQ.poll();
        }
        else
        {
            return null;
        }
    }

    private boolean IsImuConnected()
    {
        return imuConnected;
    };
    private boolean IsEnvConnected()
    {
        return envConnected;
    };
}
