package ph.bxtdev.WifiControlledRobot;

import android.app.Activity;
import android.content.Context;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

@DesignerComponent(
        versionName = "1.1",
        version = 1,
        description = "Wi-Fi Robot Sensor Data Extension",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "aiwebres/icon.png")

@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE")

public class WifiRobotSensorData extends AndroidNonvisibleComponent {

    private Context context;
    private Activity activity;
    private Socket socket;
    private InputStream inputStream;
    private String robotIp = "";
    private int robotPort = 0;

    public WifiRobotSensorData(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
        this.context = container.$context();
    }

    @SimpleFunction(description = "Connect to the robot and start receiving sensor data.")
    public void Connect(final String ip, final int port) {
        this.robotIp = ip;
        this.robotPort = port;

        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(robotIp, robotPort);
                    inputStream = socket.getInputStream();
                    SensorDataReceived();
                    StartReceivingData();
                } catch (IOException e) {
                    ConnectionFailed();
                }
            }
        });
    }

    @SimpleFunction(description = "Disconnect from the robot.")
    public void Disconnect() {
         AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                    Disconnected();
                } catch (IOException e) {
                    DisconnectionFailed();
                }
            }
        };
    }

    @SimpleFunction(description = "Start receiving sensor data from the robot.")
    public void StartReceivingData() {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytesRead;

                try {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        String sensorData = new String(buffer, 0, bytesRead);
                        SensorData(sensorData); 
                    }
                } catch (IOException e) {
                    DataReceptionFailed();
                }
            }
        });
    }

    @SimpleEvent(description = "Received sensor data from robot.")
    public void SensorData(String data) {
        DispatchEvent("SensorData", data); 
    }

    @SimpleEvent(description = "Connection to robot failed.")
    public void ConnectionFailed() {
        DispatchEvent("ConnectionFailed");
    }

    @SimpleEvent(description = "Disconnected from robot.")
    public void Disconnected() {
        DispatchEvent("Disconnected");
    }

    @SimpleEvent(description = "Disconnection from robot failed.")
    public void DisconnectionFailed() {
        DispatchEvent("DisconnectionFailed");
    }

    @SimpleEvent(description = "Sensor data reception failed.")
    public void DataReceptionFailed() {
        DispatchEvent("DataReceptionFailed");
    }

    @SimpleEvent(description = "Ready to receive sensor data.")
    public void SensorDataReceived() {
        DispatchEvent("SensorDataReceived");
    }

    private void DispatchEvent(final String eventName, final String data) {
        form.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(WifiRobotSensorData.this, eventName, data);
            }
        });
    }

    private void DispatchEvent(final String eventName) {
        form.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(WifiRobotSensorData.this, eventName);
            }
        });
    }
}
