package com.abcx.rapidchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    public static final int GALLEY_PICK = 1;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef;
    private CircleImageView settings_image;
    private TextView settings_displayName, settings_status;
    private Button settings_imageBtn, settings_statusBtn;
    private ProgressDialog mProgressDialogue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        settings_displayName = findViewById(R.id.settings_displayName);
        settings_image = findViewById(R.id.settings_image);
        settings_imageBtn = findViewById(R.id.settings_imageBtn);
        settings_status = findViewById(R.id.settings_status);
        settings_statusBtn = findViewById(R.id.settings_statusBtn);

        settings_statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = settings_status.getText().toString();
                Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
                intent.putExtra("status_value", status_value);
                startActivity(intent);
            }
        });

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        String currentuserid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserid);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                settings_displayName.setText(name);
                settings_status.setText(status);

                if (!image.equals("default")) {
                    Picasso.get().load(image).into(settings_image);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        settings_imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             /*   CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLEY_PICK);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GALLEY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialogue = new ProgressDialog(SettingsActivity.this);
                mProgressDialogue.setTitle("Uploading Image...");
                mProgressDialogue.setMessage("Please wait a moment while we upload your Image");
                mProgressDialogue.setCanceledOnTouchOutside(false);
                mProgressDialogue.show();
                Uri resultUri = result.getUri();

                String current_user_id = mCurrentUser.getUid();

                final StorageReference filepath = mStorageRef.child("profile_images").child(current_user_id + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String download_url = uri.toString();
                                    mUserDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mProgressDialogue.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Profile Picture uploaded successfully", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });
                                }
                            });

                        } else {
                            Toast.makeText(SettingsActivity.this, "Error in uploading the image", Toast.LENGTH_SHORT).show();
                            mProgressDialogue.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}


