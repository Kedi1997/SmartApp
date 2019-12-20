package com.example.smartparking;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.example.smartparking.Profile;


public class TProfileMgr extends Object  {

    private final String applicationRoot = "smart parking";
    private FirebaseUser user;
    private String userID;



    private int returnvalue;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseauth;
    private CollectionReference fbSmartParkCollection;

    private Profile parkingEvent;


    public TProfileMgr() {

        //Open the Database Instance
        db = FirebaseFirestore.getInstance();
        fbSmartParkCollection = db.collection(applicationRoot);

        //Create or Open
        openUserProfile();
    }

    public void openUserProfile(){

        firebaseauth = FirebaseAuth.getInstance();
        user = firebaseauth.getCurrentUser();
        if (user == null) {

            //Anonymous Login if User not logged in
            //userSigninAnonymously();
            //This is not used. For Future Use
            //NO action;
            //userID = "TestingAnonymous";
            //Toast.makeText(,"User not created",Toast.LENGTH_SHORT).show();
        }
        //Get the User ID and make it hardcoded still if you haven't gotten the userId
        else{
            userID= user.getUid();
        }
    }

    public void userSigninAnonymously(){
        Task<AuthResult>  authtaskResult = firebaseauth.signInAnonymously()
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                          @Override
                                          public void onSuccess(AuthResult authResult) {

                                          }
                                      }
                );
    }

    public void addParkEvent(Profile event){
        //Try Batch Write as Next step
        //WriteBatch batch = db.batch();

        parkingEvent = new Profile(event.getUserId(),event.getLocation(),
                event.getStartTime(),event.getEndTime());

    }

    public int updateProfileHistory(){
        //Use Batch update later
        //Toast.makeText(this, "Entered Update Profile Histrory", Toast.LENGTH_SHORT).show();

        //Root Collection : "smart parking"/userID as the Collection/Documents
        //db.collection("Application").document(userID)
        db.collection(applicationRoot).document().set(parkingEvent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        returnpositive();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(this,"Park Info not updated!", Toast.LENGTH_SHORT).show();
                        returnerror();
                    }
                });

        return 1;
        //batch.se
        //batch.commit();
    }

    public void returnpositive(){
        returnvalue = 1;
    }

    public void returnerror(){
        returnvalue = 0;
    }


    public int getReturnvalue() {
        return returnvalue;
    }

    public void setReturnvalue(int returnvalue) {
        this.returnvalue = returnvalue;
    }

    public String getApplicationRoot() {
        return applicationRoot;
    }

    public FirebaseUser getUser() {
        return user;
    }

    public void setUser(FirebaseUser user) {
        this.user = user;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    public Profile getParkingEvent() {
        return parkingEvent;
    }

    public void setParkingEvent(Profile parkingEvent) {
        this.parkingEvent = parkingEvent;
    }
}
