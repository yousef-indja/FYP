package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserPage extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    FirebaseAuth auth;
    FirebaseUser fUser;
    DatabaseReference dr;

    private RecyclerView rv;
    MyAdapter adapter;
    private List<ParkingMeters> pm_list = new ArrayList<>();
    private ArrayList<String> favsList = new ArrayList<>();
    TextView error;

    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);
        final TextView e = (TextView)findViewById(R.id.email);
        final TextView un = (TextView)findViewById(R.id.username);
        error = (TextView)findViewById(R.id.noFavs);
        fUser = auth.getCurrentUser();
        final String[] idToken = {""};
        fUser.getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            public void onSuccess(GetTokenResult result) {
                idToken[0] = result.getToken();
            }
        });


        dr= FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User u = dataSnapshot.getValue(User.class);
                    e.setText(u.getEmail());
                    un.setText(u.getUsername());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dr= FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid()).child("Favourites");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    System.out.println(String.valueOf(userSnapshot.getValue()));
                    favsList.add(String.valueOf(userSnapshot.getValue()));
                }
                addToList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    public void addToList(){
        for(final String s: favsList){
            dr= FirebaseDatabase.getInstance().getReference("Parking Meters").child(s);
            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    System.out.println(" 2nd" + String.valueOf(dataSnapshot.getValue()));
                    ParkingMeters pm = dataSnapshot.getValue(ParkingMeters.class);
                    pm_list.add(pm);
                    setUpRV();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    public void setUpRV() {
        rv = (RecyclerView) findViewById(R.id.myRecyclerView);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);

        adapter = new MyAdapter(pm_list);

        rv.addItemDecoration((new DividerItemDecoration(this, LinearLayoutManager.VERTICAL)));

        rv.setAdapter(adapter);

        if(pm_list.isEmpty()){
            rv.setVisibility(View.INVISIBLE);
        }else{
            error.setVisibility(View.INVISIBLE);
        }
    }

    public void logOut(View v){
        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();

        GoogleSignInClient googleSignInClient= GoogleSignIn.getClient(this,gso);
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    FirebaseAuth.getInstance().signOut(); // very important if you are using firebase.
                    Toast.makeText(UserPage.this, "Signed Out.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(UserPage.this, MainActivity.class);
                    startActivity(i);
                }
            }
        });

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
