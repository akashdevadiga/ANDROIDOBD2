package com.mywholesalemart.www.mybluetoothterminal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.fr3ts0n.ecu.EcuDataPv;
import com.fr3ts0n.pvs.PvList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static com.mywholesalemart.www.mybluetoothterminal.Bluetooth.prefs;

public class FaultRes extends AppCompatActivity {

    TextView textView, textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fault_res);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);

        HashMap<Object,Object> hashMap = new HashMap<>();
        HashMap<Object,HashMap<Object,Object>> ecuData = new HashMap<>();
        hashMap = (HashMap<Object, Object>) getIntent().getSerializableExtra("hashmap");
        // ecuData = (HashMap<String, HashMap<String, String>>) getIntent().getSerializableExtra("ObdProtPidPvs");

        Gson gson = new Gson();
        String json = SharedPreference.getEcuData(FaultRes.this);
        java.lang.reflect.Type type = new TypeToken< HashMap<Object,HashMap<Object,Object>>>(){}.getType();
        ecuData = gson.fromJson(json, type);

        StringBuilder ab = new StringBuilder();
        StringBuilder abc = new StringBuilder();

        for (Object obj : hashMap.keySet()){
            ab.append(hashMap.get(obj)).append("\n");
        }

        textView.setText(ab.toString());

        for (Object obj : ecuData.keySet()){
          if(ecuData.get(obj).containsValue("ecu_voltage"))
          {
                for (Object s : ecuData.get(obj).keySet())  {
                    if (s.equals("VALUE")){
                        abc.append("ecu voltage : " ).append(ecuData.get(obj).get(s)).append("\n");
                    }
                }
          }else if(ecuData.get(obj).containsValue("engine_coolant_temperature"))
          {
              for (Object s : ecuData.get(obj).keySet())  {
                  if (s.equals("VALUE")){
                      abc.append("engine_coolant_temperature : " ).append(ecuData.get(obj).get(s)).append("\n");
                  }
              }
          }
        }

        textView2.setText(abc.toString());



    }
}
