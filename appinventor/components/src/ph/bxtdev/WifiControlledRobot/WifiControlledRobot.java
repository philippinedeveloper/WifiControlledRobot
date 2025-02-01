package ph.bxtdev.WifiControlledRobot;

import android.app.Activity;
import android.content.Context;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

@DesignerComponent(
        versionName = "1.2",
        version = 3,
        description = "Wi-Fi Controlled Robot Extension",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "aiwebres/icon.png")

@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE")

public class WifiControlledRobot extends AndroidNonvisibleComponent {

    private Context context;
    private Activity activity;
    private Socket socket;
    private PrintWriter output;
    private String robotIp = "";
    private int robotPort = 0;

    public WifiControlledRobot(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
        this.context = container.$context();
    }

    @SimpleFunction(description = "Connect to the robot using Wi-Fi.")
    public void Connect(final String ip, final int port) {
        this.robotIp = ip;
        this.robotPort = port;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(robotIp, robotPort);
                    output = new PrintWriter(socket.getOutputStream(), true);
                    Connected();
                } catch (IOException e) {
                    ConnectionFailed();
                }
            }
        }).start();
    }

    @SimpleFunction(description = "Send a movement command to the robot.")
    public void SendCommand(final String command) {
        if (output != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    output.println(command);
                    output.flush();
                    CommandSent();
                }
            }).start();
        } else {
            CommandFailed();
        }
    }

    @SimpleFunction(description = "Disconnect from the robot.")
    public void Disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (output != null) {
                        output.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                    Disconnected();
                } catch (IOException e) {
                    DisconnectionFailed();
                }
            }
        }).start();
    }

    @SimpleEvent(description = "Disconnected to robot")
    public void Disconnected(){
            DispatchEvent("Disconnected");
    }

    @SimpleEvent(description = "Disconnection to robot failed")
    public void DisconnectionFailed(){
            DispatchEvent("DisconnectionFailed");
    }

    @SimpleEvent(description = "Sent command to robot")
    public void CommandSent(){
            DispatchEvent("CommandSent");
    }

    @SimpleEvent(description = "When command failed to send")
    public void CommandFailed(){
            DispatchEvent("CommandFailed");
    }

    @SimpleEvent(description = "When connected to robot")
    public void Connected(){
            DispatchEvent("Connected");
    }

    @SimpleEvent(description = "When connection failed to robot")
    public void ConnectionFailed(){
            DispatchEvent("ConnectionFailed");
    }
        
    private void DispatchEvent(final String eventName) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(WifiControlledRobot.this, eventName);
            }
        });
    }
}
