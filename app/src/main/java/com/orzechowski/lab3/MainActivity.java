package com.orzechowski.lab3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private EditText addressInput, fileSize, fileType;
    Diagnostic task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addressInput = findViewById(R.id.addressInput);
        fileSize = findViewById(R.id.fileSizeDisplay);
        fileType = findViewById(R.id.fileTypeDisplay);

        Button downloadInfoButton = findViewById(R.id.downloadInfoButton);
        downloadInfoButton.setOnClickListener(v -> {
            task = new Diagnostic(addressInput.getText().toString());
            task.start();
        });

        Button downloadButton = findViewById(R.id.downloadFileButton);
        downloadButton.setOnClickListener(v -> {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                NewIntentService.launchService(this, addressInput.getText().toString());
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 112);
            }
        });
    }



    class Diagnostic extends Thread {
        private final String link;

        Diagnostic(String link){
            this.link = link;
        }

        @Override
        public void run(){
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

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] permissions, @NonNull int[] decisions){
        super.onRequestPermissionsResult(code, permissions, decisions);
        if (code == 112) {
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    decisions[0] == PackageManager.PERMISSION_GRANTED) {
                NewIntentService.launchService(this, addressInput.getText().toString());
            } else {
                Toast.makeText(this, "Nie otrzymano uprawnień", Toast.LENGTH_SHORT).show();
            }
        }
    }
}