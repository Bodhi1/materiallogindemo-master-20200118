package com.sourcey.materiallogindemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class statisticsMenuActivity extends AppCompatActivity {

    String authorization;
    RequestQueue requestQueue;
    static final String REQ_TAG = "VACTIVITY";
    ArrayList<String> idList = new ArrayList<String>();
    ArrayList<String> nameList = new ArrayList<String>();
    ArrayList<String> urlList = new ArrayList<String>();
    ArrayList<String> otherUrl = new ArrayList<String>();
    String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_layout);

        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext())
                .getRequestQueue();
        Intent intent = getIntent();
        parentId = intent.getStringExtra("id");

        PostHttpRequest(); //將Data寫入listview的程式碼放在此函數內，否則會有Callback時間差的問題，原因在於listener
    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (urlList.get(position).equals("0"))
            {
                Intent intent = new Intent(statisticsMenuActivity.this, statisticsMenuActivity.class);
                intent.putExtra("id", otherUrl.get(position));
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(statisticsMenuActivity.this, statisticsPageActivity.class);
                intent.putExtra("id", urlList.get(position));
                startActivity(intent);
            }
        }

    };

    private void PostHttpRequest() {
        JSONObject json = new JSONObject();
        try {
            json.put("parentId", parentId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = getResources().getString(R.string.statisticsMenus_api_url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        serverResp.setText("String Response : "+ response.toString());
                        try {
                            // Log.e("here", response.toString());
                            if (response.getString("data").length() > 0) {
                                try {
                                    JSONArray array = response.getJSONArray("data");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject jsonObject = array.getJSONObject(i);
                                        String id = jsonObject.getString("id");
                                        String name = jsonObject.getString("name");
                                        String url = jsonObject.getString("unitId");
                                        String other;
                                        if (jsonObject.getString("unitId").equals("0")) {
                                            other = jsonObject.getString("id");
                                            otherUrl.add(other);
                                        }
                                        else
                                            otherUrl.add(url);

                                        idList.add(id);
                                        nameList.add(name);
                                        urlList.add(url);
                                    }
                                    //找到ListView
                                    ListView list = (ListView) findViewById(R.id.listview);
                                    //建立Adapter，並將要顯示的結果陣列傳入
                                    WordAdapter adapter = new WordAdapter(nameList);
                                    //將Adapter設定給ListView
                                    list.setAdapter(adapter);
                                    list.setOnItemClickListener(onClickListView);
                                } catch (Exception e) {
                                    Toast.makeText(getBaseContext(), "response error", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getBaseContext(), "No return data", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                serverResp.setText("Error getting response");
            }
        }) {
            /* Passing some request headers*/
            @Override
            public Map<String, String> getHeaders() {
                GlobalVariable gv = (GlobalVariable) getApplicationContext();
                authorization = gv.getAuthorization();
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", authorization);
                return headers;
            }
        };
        jsonObjectRequest.setTag(REQ_TAG);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_basic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } else if (id == R.id.logout) {
            GlobalVariable gv = (GlobalVariable) getApplicationContext();
            gv.setLoginToken(false);
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
}
