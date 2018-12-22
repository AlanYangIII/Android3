package com.example.alanyang.android3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Data> dataList = new ArrayList<>();
    EditText editText;
    private String text;
    RecyclerAdapter recyclerAdapter;
    Button sendRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        LinearLayoutManager manager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(manager);

        recyclerAdapter = new RecyclerAdapter(dataList);

        recyclerView.setAdapter(recyclerAdapter);

        sendRequest = findViewById(R.id.send_request);
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.send_request) {
                    sendRequestWithHttpURLConnection();
                }
            }
        });
    }


    private void initData() {
        Data data = new Data(text);
        dataList.add(data);
    }


    public void sendRequestWithHttpURLConnection() {
        editText = findViewById(R.id.book);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Http", "run:进行网络连接");
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                InputStream inputStream = null;
                String address = "https://www.apiopen.top/novelSearchApi?name=" + editText.getText().toString();
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);

                    if (connection.getResponseCode() == 200) {
                        inputStream = connection.getInputStream();

                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();

                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                            parseJSONWithJSONObject(response.toString());
                            initData();
                            inputStream.close();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void parseJSONWithJSONObject(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = new JSONArray(jsonObject.getString("data"));
            for (int i = 0; i < jsonArray.length(); i++) {

                text = jsonArray.get(i).toString();
                Data data = new Data(text);
                dataList.add(data);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerAdapter.notifyDataSetChanged();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
