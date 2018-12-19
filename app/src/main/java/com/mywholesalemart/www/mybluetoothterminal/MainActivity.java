package com.mywholesalemart.www.mybluetoothterminal;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {






    Button b, yes;
    Dialog dialog;
    Context con = MainActivity.this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b = findViewById(R.id.button);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int PERMISSION_ALL = 1;
                final String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET,
                        Manifest.permission.READ_SMS, Manifest.permission.CAMERA,
                        Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};

                if(!hasPermissions(con, PERMISSIONS)){
                    dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.custom_dailogok);
                    dialog.setCancelable(false);
                    yes = dialog.findViewById(R.id.okBut);
                    dialog.show();

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                            dialog.cancel();
                        }

                    });
                }
                else{
                    try {
                        Toast.makeText(MainActivity.this,"ok",Toast.LENGTH_SHORT).show();
                        Intent k = new Intent(MainActivity.this, Bluetooth.class);
                        startActivity(k);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
