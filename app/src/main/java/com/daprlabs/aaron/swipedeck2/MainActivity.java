package com.daprlabs.aaron.swipedeck2;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SwipeDeck cardStack;
    private Context context = this;
    private SwipeDeckAdapter adapter;
    private ArrayList<String> testData;
    private CheckBox dragCheckbox;
    ArrayList<ShopCardItem> datalist = new ArrayList<>();
    private RequestQueue mRequestQueue;
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
    public void prepareList(JSONObject js)
    {
        try {
            JSONArray main = js.getJSONArray("MainProductsList");
            for(int i=0;i<main.length();i++)
            {
                JSONObject sub = main.getJSONObject(i);
                JSONArray products = sub.getJSONArray("products");
                for (int j=0;j<products.length();j++)
                {
                    JSONObject pro = products.getJSONObject(j);
                    datalist.add(new ShopCardItem(pro.getJSONArray("Images").getString(0),pro.getString("name")));
                }
            }
            Log.e("data len",":"+datalist.size());
            adapter = new SwipeDeckAdapter(datalist, this);
            if(cardStack != null){
                cardStack.setAdapter(adapter);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);
        dragCheckbox = (CheckBox) findViewById(R.id.checkbox_drag);
        String  tag_string_req = "string_req";

        String url = "https://www.numbermall.com/native_kiosk/library.php?method=ShoppingOffers_new&deviceType=kiosk_customer";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, response.toString());
                pDialog.hide();
                try {
                    prepareList(new JSONObject(response.toString()));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
            }
        });
        /*testData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            testData.add(String.valueOf(i));
        }
*/
        addToRequestQueue(strReq, tag_string_req);

        cardStack.setCallback(new SwipeDeck.SwipeDeckCallback() {
            @Override
            public void cardSwipedLeft(long stableId) {
                Log.i("MainActivity", "card was swiped left, position in adapter: " + stableId);
                int id= (int)stableId;
                Log.e("left:",datalist.get((int)stableId).name);
            }

            @Override
            public void cardSwipedRight(long stableId) {
                Log.i("MainActivity", "card was swiped right, position in adapter: " + stableId);
                Log.e("right:",datalist.get((int)stableId).name);
            }

            @Override
            public void cardSwipedTop(long itemId) {
                Log.e("","");
                Log.e("top:",datalist.get((int)itemId).name);
            }


            @Override
            public boolean isDragEnabled(long itemId) {
                return dragCheckbox.isChecked();
            }
        });

        cardStack.setLeftImage(R.id.left_image);
        cardStack.setRightImage(R.id.right_image);
        cardStack.setMiddleImage(R.id.middle_image);

        Button btn = (Button) findViewById(R.id.button_left);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardStack.swipeTopCardLeft(500);

            }
        });
        Button btn2 = (Button) findViewById(R.id.button_right);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardStack.swipeTopCardRight(500);
            }
        });

        Button btn3 = (Button) findViewById(R.id.button_center);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                testData.add("a sample string.");
//                adapter.notifyDataSetChanged();
                //cardStack.unSwipeCard();
                cardStack.swipeTopCardTop(500);
            }
        });

    }

    public class SwipeDeckAdapter extends BaseAdapter {

        private ArrayList<ShopCardItem> data;
        private Context context;

        public SwipeDeckAdapter(ArrayList<ShopCardItem> data, Context context) {
            this.data = data;
            this.context = context;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = getLayoutInflater();
                // normally use a viewholder
                v = inflater.inflate(R.layout.test_card2, parent, false);
            }
            //((TextView) v.findViewById(R.id.textView2)).setText(data.get(position));
            ImageView imageView = (ImageView) v.findViewById(R.id.offer_image);
            Picasso.with(context).load(data.get(position).url).fit().centerCrop().into(imageView);
            TextView textView = (TextView) v.findViewById(R.id.sample_text);
           // String item = (String)getItem(position);
            textView.setText(data.get(position).name);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Layer type: ", Integer.toString(v.getLayerType()));
                    Log.i("Hardware Accel type:", Integer.toString(View.LAYER_TYPE_HARDWARE));
                    /*Intent i = new Intent(v.getContext(), BlankActivity.class);
                    v.getContext().startActivity(i);*/
                }
            });
            return v;
        }
    }
}
