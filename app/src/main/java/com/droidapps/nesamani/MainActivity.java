package com.droidapps.nesamani;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static final String TAG="nesamani";
    TextView counter;
    TextView hits;
    ImageButton pray;
    ImageButton hammer;

    @Override
    protected void onPause() {

        saveToCloud();
        super.onPause();
    }

    private void saveToCloud() {
        int counterValue=Integer.parseInt(counter.getText().toString());
        int hitsValue=Integer.parseInt(hits.getText().toString());
        Map counterMap = new HashMap();
        counterMap.put("count",counterValue);
        counterMap.put("hits",hitsValue);
        saveLocal(counterValue);
        saveLocalHits(hitsValue);


        FirebaseFirestore.getInstance().collection("praycount").document("byUC5qAvb2YFJq5uyYO2").set(counterMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(counter!=null){
            setCounterValue(getLocal());
        }
    }

    public void saveLocal(int counterValue){
        getPreferences(MODE_PRIVATE).edit().putInt("count",counterValue).commit();
    }

    public void saveLocalHits(int counterValue){
        getPreferences(MODE_PRIVATE).edit().putInt("hits",counterValue).commit();
    }

    public int getLocal(){
       return getPreferences(MODE_PRIVATE).getInt("count",10234);
    }

    public int getLocalHits(){
        return getPreferences(MODE_PRIVATE).getInt("hits",10234);
    }

    public void incrementMyLocalPrayers(){
        int mycount=getMyLocal();
        mycount++;
        getPreferences(MODE_PRIVATE).edit().putInt("mycount",mycount).commit();
    }

    public void incrementMyLocalHits(){
        int mycount=getMyHitsLocal();
        mycount++;
        getPreferences(MODE_PRIVATE).edit().putInt("myhitcount",mycount).commit();
    }

    public int getMyLocal(){
        return getPreferences(MODE_PRIVATE).getInt("mycount",0);
    }

    public int getMyHitsLocal(){
        return getPreferences(MODE_PRIVATE).getInt("myhitcount",0);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_new);


        MobileAds.initialize(this, "ca-app-pub-4898754855127691~3616544402");

        findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementPrayers();
            }
        });
        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    sendMessage();

            }
        });
        counter = findViewById(R.id.counter);
        hits = findViewById(R.id.hammercounter);
        pray = findViewById(R.id.pray);

        hammer = findViewById(R.id.hammer);
        pray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementPrayers();
            }
        });
        hammer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementHits();
            }
        });

        Toast.makeText(this,"Connecting to world...",Toast.LENGTH_SHORT).show();
        fetchFromDB();
        setCounterValue(getLocal());
        initAds();
    }

    void initAds(){
       AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void fetchFromDB() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("praycount").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        counter.setText(""+document.get("count"));
                        hits.setText(""+document.get("hits"));
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }

            }
        });

//

    }

    public void setCounterValue( int counterValue){
        counter.setText(""+counterValue);
    }

    public void incrementPrayers(){
       String text= counter.getText().toString();
       int counterValue=Integer.parseInt(text);
       counterValue++;
       incrementMyLocalPrayers();
       setCounterValue(counterValue);
    }

    public void incrementHits(){
        String text= hits.getText().toString();
        int counterValue=Integer.parseInt(text);
        counterValue++;
        incrementMyLocalHits();
        setCounterValue(counterValue);
    }




    public void sendMessage()  {
        saveToCloud();

        //You can read the image from external drove too
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Pray with me for Nesamani."+" "+getLocal()+" Prayers so far."+ "\nhttps://play.google.com/store/apps/details?id="+getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage("You have done "+getMyLocal()+"  prayers so far.  \nSave Nesamani with more prayers, Invite your friends to pray");
        adb.setTitle("Pray more");
        adb.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
               sendMessage();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                MainActivity.super.onBackPressed();
            }
        });
        adb.show();
    }
}