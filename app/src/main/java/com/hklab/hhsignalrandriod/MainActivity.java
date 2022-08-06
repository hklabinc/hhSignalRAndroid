package com.hklab.hhsignalrandriod;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    EditText editTextName, editTextMessage;
    Button button;
    HubConnection hubConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = findViewById(R.id.editTextName);
        editTextMessage = findViewById(R.id.editTextMessage);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);

        // 연결 설정
        // (Notice: Android 9부터는 http 연결을 위해 Manifest에 android:usesCleartextTraffic="true" 추가 해야!!)
        String input = "http://hklab.hknu.ac.kr/chathub";
        hubConnection = HubConnectionBuilder.create(input).build();

        // 수신 함수 등록
        hubConnection.on("ReceiveMessage", (user, message) -> {
            textView.append(user + ": " + message + "\n");
        }, String.class, String.class);		// 수신 메시지 개수에 따라 타입을 정의

        // 연결
        try {
            hubConnection.start().blockingAwait();
            Log.d("[HHCHOI]", "SignalR Connected!");
        } catch (Exception e) {
            Log.d("[HHCHOI]", "SignalR Connection Error!: " + e.getMessage());
        }

        // 메시지 전송
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String message = editTextMessage.getText().toString();

                hubConnection.send("SendMessage", name, message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // SignalR 종료.
        hubConnection.stop();
    }
}