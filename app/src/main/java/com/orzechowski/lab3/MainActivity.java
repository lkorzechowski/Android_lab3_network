package com.orzechowski.lab3;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private EditText addressInput, fileSize, fileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addressInput = findViewById(R.id.addressInput);
        fileSize = findViewById(R.id.fileSizeDisplay);
        fileType = findViewById(R.id.fileTypeDisplay);

        Button downloadInfoButton = findViewById(R.id.downloadInfoButton);
        downloadInfoButton.setOnClickListener(v -> {
            Diagnostic task = new Diagnostic(addressInput.getText().toString());
            task.start();
        });
    }

    class Diagnostic extends Thread {
        private final String link;

        Diagnostic(String link){
            this.link = link;
        }

        @Override
        public void start(){
            HttpURLConnection connection = null;
            String size, type;
            try{
                URL url = new URL(link);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000);
                connection.connect();
                size = String.valueOf(connection.getContentLength());
                type = connection.getContentType();
                runOnUiThread(() -> {
                    fileSize.setText(size);
                    fileType.setText(type);
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
            }
        }
    }
}