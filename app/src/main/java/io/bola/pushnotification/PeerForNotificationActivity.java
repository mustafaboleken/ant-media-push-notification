package io.bola.pushnotification;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.webrtc.DataChannel;
import org.webrtc.SurfaceViewRenderer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import io.antmedia.webrtcandroidframework.api.DefaultDataChannelObserver;
import io.antmedia.webrtcandroidframework.api.DefaultWebRTCListener;
import io.antmedia.webrtcandroidframework.api.IDataChannelObserver;
import io.antmedia.webrtcandroidframework.api.IWebRTCClient;
import io.antmedia.webrtcandroidframework.api.IWebRTCListener;

public class PeerForNotificationActivity extends AppCompatActivity {
    private TextView broadcastingTextView;
    public static String streamId;
    private IWebRTCClient webRTCClient;
    private String websocketUrl = "ws://localhost:5080/WebRTCAppEE/websocket";

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_for_notification);

        SurfaceViewRenderer fullScreenRenderer = findViewById(R.id.full_screen_renderer);
        SurfaceViewRenderer pipRenderer = findViewById(R.id.pip_view_renderer);

        broadcastingTextView = findViewById(R.id.broadcasting_text_view);

        String serverUrl = websocketUrl;

        webRTCClient = IWebRTCClient.builder()
                .setLocalVideoRenderer(pipRenderer)
                .addRemoteVideoRenderer(fullScreenRenderer)
                .setServerUrl(serverUrl)
                .setActivity(this)
                .setWebRTCListener(createWebRTCListener())
                .setDataChannelObserver(createDatachannelObserver())
                .build();

        Log.i(getClass().getSimpleName(), "Calling play start");
        webRTCClient.join(streamId);
    }

    private IDataChannelObserver createDatachannelObserver() {
        return new DefaultDataChannelObserver() {
            @Override
            public void textMessageReceived(String messageText) {
                super.textMessageReceived(messageText);
                Toast.makeText(PeerForNotificationActivity.this, "Message received: " + messageText, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private IWebRTCListener createWebRTCListener() {
        return new DefaultWebRTCListener() {
            @Override
            public void onPlayStarted(String streamId) {
                super.onPlayStarted(streamId);
                broadcastingTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPlayFinished(String streamId) {
                super.onPlayFinished(streamId);
                broadcastingTextView.setVisibility(View.GONE);
            }
        };
    }

    public void sendTextMessage(String messageToSend) {
        final ByteBuffer buffer = ByteBuffer.wrap(messageToSend.getBytes(StandardCharsets.UTF_8));
        DataChannel.Buffer buf = new DataChannel.Buffer(buffer, false);
        webRTCClient.sendMessageViaDataChannel(streamId, buf);
    }

    public void showSendDataChannelMessageDialog(View view) {
        if (webRTCClient != null && webRTCClient.isDataChannelEnabled()) {
            // create an alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Send Message via Data Channel");
            // set the custom layout
            final View customLayout = getLayoutInflater().inflate(R.layout.send_message_data_channel, null);
            builder.setView(customLayout);
            // add a button
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // send data from the AlertDialog to the Activity
                    EditText editText = customLayout.findViewById(R.id.message_text_input);
                    sendTextMessage(editText.getText().toString());
                }
            });
            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            Toast.makeText(this, R.string.data_channel_not_available, Toast.LENGTH_LONG).show();
        }
    }

}
