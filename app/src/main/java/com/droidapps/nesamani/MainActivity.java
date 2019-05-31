package com.droidapps.nesamani;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
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


    int[] prayimages=new int[]{R.drawable.pray_1,R.drawable.pray_2,R.drawable.pray_3,R.drawable.pray_4,R.drawable.pray_5,R.drawable.pray_6,R.drawable.pray_7,R.drawable.pray_8,R.drawable.pray_9,R.drawable.pray_10};
    int[] hitimages=new int[]{R.drawable.hit_1,R.drawable.hit_2,R.drawable.hit_3,R.drawable.hit_4,R.drawable.hit_5,R.drawable.hit_6,R.drawable.hit_7,R.drawable.hit_8,R.drawable.hit_9,R.drawable.hit_10};



    int hitimagecount=0;
    int prayimagecount=0;
    private ImageView image;

    public void setImage(boolean isHit){
        int imageId=0;
        if(isHit){
            if(hitimagecount>=hitimages.length) hitimagecount=0;
            imageId=hitimages[hitimagecount++];
        }else{
            if(prayimagecount>=prayimages.length) prayimagecount=0;
            imageId=prayimages[prayimagecount++];
        }
        image.setImageResource(imageId);

    }

    @Override
    protected void onPause() {

        saveToCloud();
        super.onPause();
    }

    private void saveToCloud() {
        final int counterValue=Integer.parseInt(counter.getText().toString());
        final int hitsValue=Integer.parseInt(hits.getText().toString());
        saveLocal(counterValue);
        saveLocalHits(hitsValue);
        FirebaseFirestore.getInstance().collection("prayhitcount").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().size()>0) {

                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        long newCounter= counterValue;
                        long newHits=hitsValue;
                        if(document.getLong("count")>counterValue || document.getLong("hits")>hitsValue){
                            newCounter=document.getLong("count")+10;
                            newHits=document.getLong("hits")+10;
                        }
                        final Map counterMap = new HashMap();
                        counterMap.put("count",newCounter);
                        counterMap.put("hits",newHits);
                    FirebaseFirestore.getInstance().collection("prayhitcount").document("prayhitid").set(counterMap);
                    }
                 else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }

            }
        });

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
        image = findViewById(R.id.image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                incrementPrayers();
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
                playBell();
                incrementPrayers();
                setImage(false);
            }
        });
        hammer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playHammer();
                incrementHits();
                setImage(true);
            }
        });

        Toast.makeText(this,"Connecting to world...",Toast.LENGTH_SHORT).show();
        fetchFromDB();
        setCounterValue(getLocal());
        initSound();
        initAds();
    }

    void initAds(){
       AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void fetchFromDB() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("prayhitcount").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
    public void setHitsValue( int counterValue){
        hits.setText(""+counterValue);
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
        setHitsValue(counterValue);
    }




    public void sendMessage()  {

        //You can read the image from external drove too
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Pray or Hit Nesamani."+" "+getLocal()+" prayers and "+ getLocalHits()+" hits so far. "+ "\nhttps://play.google.com/store/apps/details?id="+getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);

    }

    @Override
    public void onBackPressed() {
        saveToCloud();
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage("You have done "+getMyLocal()+" prayers and "+ getMyHitsLocal()+" hits so far. Invite your friends to pray or hit");
        adb.setTitle("Pray or Hit");
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

    final int NUMBER_OF_SIMULTANEOUS_SOUNDS=1;
    private final float LEFT_VOLUME_VALUE = 1.0f;
    private final float RIGHT_VOLUME_VALUE = 1.0f;
    private final int MUSIC_LOOP = 0;
    private final int SOUND_PLAY_PRIORITY = 0;
    private final float PLAY_RATE= 1.0f;

    int hammerSoundId;
    int templeSoundId;
    SoundPool soundPool;

    public void initSound(){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool= new SoundPool.Builder()
                    .setMaxStreams(NUMBER_OF_SIMULTANEOUS_SOUNDS)
                    .build();
        } else {
            // Deprecated way of creating a SoundPool before Android API 21.
            soundPool= new SoundPool(NUMBER_OF_SIMULTANEOUS_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        }
        hammerSoundId = soundPool.load(getApplicationContext(), R.raw.hammer, 1);
        templeSoundId = soundPool.load(getApplicationContext(), R.raw.templebell, 1);

    }

    public void playHammer(){
        if(soundPool!=null && hammerSoundId!=0)
             soundPool.play(hammerSoundId , LEFT_VOLUME_VALUE , RIGHT_VOLUME_VALUE, SOUND_PLAY_PRIORITY , MUSIC_LOOP ,PLAY_RATE);
    }

    public void playBell(){
        if(soundPool!=null && templeSoundId!=0)
            soundPool.play(templeSoundId , LEFT_VOLUME_VALUE , RIGHT_VOLUME_VALUE, SOUND_PLAY_PRIORITY , MUSIC_LOOP ,PLAY_RATE);
    }
}