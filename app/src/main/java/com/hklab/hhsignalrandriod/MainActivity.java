package com.hklab.hhsignalrandriod;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

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

        final Handler handler = new Handler();  // Handler

        // 연결 설정
        // (Notice: Android 9부터는 http 연결을 위해 Manifest에 android:usesCleartextTraffic="true" 추가 해야!!)
        String input = "http://hklab.hknu.ac.kr/chathub";
        hubConnection = HubConnectionBuilder.create(input).build();

        // 수신 함수 등록
        hubConnection.on("ReceiveMessage", (user, message) -> {
            //textView.append(user + ": " + message + "\n");

            // Handler 사용 (UI 접근 위해)
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textView.append(user + " : " + message + "\n");
                }
            });
        }, String.class, String.class);		// 수신 메시지 개수에 따라 타입을 정의

        // 연결
        try {
            hubConnection.start().blockingAwait();
            Log.d("[HHCHOI]", "SignalR Connected!");
            Toast.makeText(getApplicationContext(), "연결 성공", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d("[HHCHOI]", "SignalR Connection Error!: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "연결 실패", Toast.LENGTH_SHORT).show();
        }

        // 메시지 전송
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //연결이 되어있다면 전송함.
                if (hubConnection.getConnectionState() != HubConnectionState.DISCONNECTED) {

                    String name = editTextName.getText().toString();
                    String message = editTextMessage.getText().toString();
                    editTextMessage.setText("");

                    //hubConnection.send("SendMessage", name, message);

                    // Thread 사용
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            hubConnection.send("SendMessage", name, message);
                        }
                    }).start();

                } else {
                    Toast.makeText(getApplicationContext(), "연결 필요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // SignalR 종료
        hubConnection.stop();
    }
}