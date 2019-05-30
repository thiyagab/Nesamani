package com.droidapps.nesamani;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static final String TAG="nesamani";
    TextView counter;
    ImageButton pray;

    @Override
    protected void onPause() {

        saveToCloud();
        super.onPause();
    }

    private void saveToCloud() {
        int counterValue=Integer.parseInt(counter.getText().toString());
        Map counterMap = new HashMap();
        counterMap.put("count",counterValue);
        FirebaseFirestore.getInstance().collection("praycount").document("byUC5qAvb2YFJq5uyYO2").set(counterMap);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendMessage();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
                counter = findViewById(R.id.counter);
                pray = findViewById(R.id.pray);
                pray.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        increment();
                    }
                });

   initDB();


    }

    private void initDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new user with a first, middle, and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Alan");
        user.put("middle", "Mathison");
        user.put("last", "Turing");


// Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

//        db.collection("praycount")
//                .where("count", ">", 0)
//                .orderBy("count", "asc")
        // Create a new user with a first and last name
        db.collection("praycount").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        counter.setText(""+document.get("count"));
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
//                Log.d(TAG, "Total Count: " + snapshots.getDocuments().get(0).get("count"));
//                ((TextView)findViewById(R.id.counter)).setText(""+snapshots.getDocuments().get(0).get("count"));
            }
        });

//

    }

    public void setCounterValue( int counterValue){
//        ValueAnimator animator = new ValueAnimator();
//        animator.setObjectValues(0, counterValue);// here you set the range, from 0 to "count" value
//        animator.addUpdateListener(new AnimatorUpdateListener() {
//            public void onAnimationUpdate(ValueAnimator animation) {
//                highscoretext.setText(String.valueOf(animation.getAnimatedValue()));
//            }
//        });
//        animator.setDuration(1000); // here you set the duration of the anim
//        animator.start();
        counter.setText(""+counterValue);
    }

    public void increment(){
       String text= counter.getText().toString();
       int counterValue=Integer.parseInt(text);
       counterValue++;
       counter.setText(""+counterValue);
    }

    public void sendMessage() throws FileNotFoundException {
        String whatsAppMessage = "Hello my friend";
        //You can read the image from external drove too
        Uri uri = Uri.parse("android.resource://com.droidapps.nesamani/drawable/web_hi_res_512.png");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, whatsAppMessage);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.setType("image/jpeg");
        startActivity(intent);
    }

}