package com.example.smartparking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static ListView profile_listview;
    private static Context context;
    private static ProfileAdapter profileAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setTitle("Profile");
//       String userId = "jY8McJVDqCYDgx8oaZwiA10wgIh1";
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        context = ProfileActivity.this;
        bindViews();

        FirebaseUser mUser = mAuth.getCurrentUser();
        if(mUser!=null){

            getProfileByUserId(mUser.getUid());

        }else {
            // jumps to login page
            Toast.makeText(ProfileActivity.this, R.string.not_login_remind, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

    }

    public static void getProfileByUserId(String userId){

        db.collection("smart parking")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Profile> ps = task.getResult().toObjects(Profile.class);
                            profileAdapter = new ProfileAdapter(ps, context);
                            profile_listview.setAdapter(profileAdapter);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void bindViews(){
        profile_listview = (ListView) findViewById(R.id.profile_listview);
    }

}
