package com.abcx.rapidchat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private TextInputLayout statusUpdate;
    private Button saveStatusBtn;
    private FirebaseUser current_user;

    private DatabaseReference mDatabaseReference;
    private ProgressDialog mProgressDialogue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        String status_value = getIntent().getStringExtra("status_value");
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current_user.getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mToolBar = findViewById(R.id.editprofile_appBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Edit Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(20);


        statusUpdate = findViewById(R.id.statusUpdate);
        saveStatusBtn = findViewById(R.id.saveStatusBtn);
        statusUpdate.getEditText().setText(status_value);
        saveStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialogue = new ProgressDialog(EditProfileActivity.this);
                mProgressDialogue.setTitle("Saving Changed");
                mProgressDialogue.setMessage("Please wait a moment while we update your status");
                mProgressDialogue.show();
                String status = statusUpdate.getEditText().getText().toString();


                mDatabaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgressDialogue.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(), "There was some error in saving changes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
