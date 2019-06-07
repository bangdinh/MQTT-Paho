package paho.android.mqtt_example;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * This is an implementation of MQTT Paho library MQTT Service which allows you to
 * connect to MQTT server via WebSockets, TCP or TLS with a stored certificate in resources folder
 * How to use:
 * Just uncomment desired BROKER and launch the application, then check logs for MQTT data
 */

public class MainActivity extends AppCompatActivity {


    /**
     * Test servers from http://test.mosquitto.org and tps://www.hivemq.com/try-out/
     */

    //public static final String BROKER = "ssl://test.mosquitto.org:8883";
    //pblic static final String BROKER = "tcp://test.mosquitto.org:1883";
//    public static final String BROKER = "ws://broker.hivemq.com:8000";

    public String BROKER = "tcp://m2m.fcam.vn:1883";
    //# Means subscribe to everything
    public static final String TOPIC = "ipc/fptbang";

    //Optional
    public static final String USERNAME = "ipcfpt1";
    public static final String PASSWORD = "Z42y13L!OnYwo*Z24eGY";
    public static final String CLIENT_ID = "ipc-" + MqttClient.generateClientId();


    public MqttAndroidClient CLIENT;
    public MqttConnectOptions MQTT_CONNECTION_OPTIONS;

    private TextView tv_connect, tv_topic, tv_subscribe, tv_message;

    private void changeLinkSsl(boolean isChange) {
        if (isChange) {
            BROKER = "ssl://test.mosquitto.org:8883";

        } else {
            BROKER = "tcp://m2m.fcam.vn:1883";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(paho.android.mqtt_example.R.layout.activity_main);

        tv_connect = findViewById(R.id.tv_connect);
        tv_topic = findViewById(R.id.tv_topic);
        tv_subscribe = findViewById(R.id.tv_subscribe);
        tv_message = findViewById(R.id.tv_message);
        SwitchMaterial btn_switch = findViewById(R.id.btn_switch);

        btn_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeLinkSsl(isChecked);
            }
        });

        MqttSetup(this);
        MqttConnect();

        CLIENT.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            //background notification
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("topic:" + topic, "message:" + message.toString());
                tv_message.setText(message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    void MqttSetup(Context context) {

        CLIENT = new MqttAndroidClient(getBaseContext(), BROKER, CLIENT_ID);
        MQTT_CONNECTION_OPTIONS = new MqttConnectOptions();


        /**
         * Depending on your MQTT broker, you might want to set these
         */
        MQTT_CONNECTION_OPTIONS.setCleanSession(true);
        MQTT_CONNECTION_OPTIONS.setUserName(USERNAME);
        MQTT_CONNECTION_OPTIONS.setPassword(PASSWORD.toCharArray());


        /**
         * SSL broker requires a certificate to authenticate their connection
         * Certificate can be found in resources folder /res/raw/
         */
        if (BROKER.contains("ssl")) {
            SocketFactory.SocketFactoryOptions socketFactoryOptions = new SocketFactory.SocketFactoryOptions();
            try {
                socketFactoryOptions.withCaInputStream(context.getResources().openRawResource(paho.android.mqtt_example.R.raw.icp_ssl));
                MQTT_CONNECTION_OPTIONS.setSocketFactory(new SocketFactory(socketFactoryOptions));
            } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException | KeyManagementException | UnrecoverableKeyException e) {
                Log.e("Mqtt", e.toString());
                e.printStackTrace();
            }
        }
    }

    void MqttConnect() {
        try {

            final IMqttToken token = CLIENT.connect(MQTT_CONNECTION_OPTIONS);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("mqtt:", "connected, token:" + asyncActionToken.toString());
                    tv_connect.setText("Connected");
                    subscribe(TOPIC, (byte) 1);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("mqtt:", "not connected" + asyncActionToken.toString() + exception.toString());
                    tv_connect.setText("Not Connected");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void subscribe(String topic, byte qos) {
        tv_topic.setText("Topic: " + topic);
        try {
            IMqttToken subToken = CLIENT.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("mqtt:", "subscribed" + asyncActionToken.toString());
                    tv_subscribe.setText("Subscribe : success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    tv_subscribe.setText("Subscribe : failed");
                    Log.d("mqtt:", "subscribing error");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void publish(String topic, String msg) {

        //0 is the Qos
        MQTT_CONNECTION_OPTIONS.setWill(topic, msg.getBytes(), 0, false);
        try {
            IMqttToken token = CLIENT.connect(MQTT_CONNECTION_OPTIONS);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("mqtt:", "send done" + asyncActionToken.toString());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("mqtt:", "publish error" + asyncActionToken.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void unsubscribe(String topic) {

        try {
            IMqttToken unsubToken = CLIENT.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.d("mqtt:", "unsubcribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {


                    Log.d("mqtt:", "couldnt unregister");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void disconnect() {
        try {
            IMqttToken disconToken = CLIENT.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("mqtt:", "disconnected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {


                    Log.d("mqtt:", "couldnt disconnect");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void onClick(View view) {
        int idView = view.getId();
        switch (idView) {
            case R.id.btn_connect:
                break;
            case R.id.btn_subscribe:
                break;
            case R.id.btn_publish:
                String message = "{'name','Đây là message publish'}";
                publish(TOPIC, message);
                break;
            case R.id.btn_unsubscribe:
                break;
            case R.id.btn_disconnect:
                break;
        }
    }
}
