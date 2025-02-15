package com.example.chessfinalproject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends Activity {

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private Button mSignupButton;
    public static String currentUser;
    public static Bitmap[] bitmapsWhitePieces = new Bitmap[6];
    public static Bitmap[] bitmapsBlackPieces = new Bitmap[6];
    static int colorOne, colorTwo;
    private DatabaseReference mUsersRef;
    SharedPreferences sharedPreferences ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Get references to the views in the layout
        mUsernameEditText = findViewById(R.id.usernameEditText);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        mLoginButton = findViewById(R.id.loginButton);
        mSignupButton = findViewById(R.id.signupButton);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);


        // Get a reference to the "users" node in the database
        mUsersRef = FirebaseDatabase.getInstance().getReference("users");



        sharedPreferences = getSharedPreferences("UsernamePref", Context.MODE_PRIVATE);
        if (!sharedPreferences.getString("username", "").equals("")) {
            // login

            progressBar.setVisibility(View.VISIBLE);
            currentUser = sharedPreferences.getString("username", "");
            setBoardColors();
            setPiecesBitmapToSaved();

            Handler handler = new Handler();
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), homeActivity.class);
                startActivity(intent);
                handler.post(()->{    progressBar.setVisibility(View.GONE);});
                finish();
            });
            thread.start();


        }


        // Set a click listener on the login button
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user's input from the EditText views
                String username = mUsernameEditText.getText().toString();
                String password = getMd5Hash( mPasswordEditText.getText().toString());

                mUsernameEditText.setError(null);
                mPasswordEditText.setError(null);

                // Check if the input is valid
                if (TextUtils.isEmpty(username)) {
                    mUsernameEditText.setError("Username is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPasswordEditText.setError("Password is required");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);


                // Check if the entered username and password are in the database
                    mUsersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // If the username node exists in the database and its password matches the entered password, proceed to the next activity
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                if (user.getPassword().equals(password)) {
                                    // Password is correct
                                    currentUser = username;

                                    // Get the SharedPreferences editor
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    // Save a string variable named "password" to the SharedPreferences
                                    editor.putString("username", username);
                                    editor.apply();

                                    setBoardColors();
                                    setPiecesBitmapToSaved();

                                    Handler handler = new Handler();
                                    Thread thread = new Thread(() -> {
                                        try {
                                            Thread.sleep(6000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        Intent intent = new Intent(getApplicationContext(), homeActivity.class);
                                        startActivity(intent);
                                        handler.post(()->{    progressBar.setVisibility(View.GONE);});
                                        finish();
                                    });
                                    thread.start();



                                } else {
                                    // Password is incorrect
                                    mPasswordEditText.setError("Incorrect password");
                                    progressBar.setVisibility(View.GONE);
                                }
                            } else {
                                // Username is not in the database
                                mUsernameEditText.setError("Username not found");
                                progressBar.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // An error occurred while reading from the database
                            // Handle the error as appropriate
                        }
                    });
                }
        });


        // Set a click listener on the sign up button
        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user's input from the EditText views
                String username = mUsernameEditText.getText().toString();
                String password = getMd5Hash( mPasswordEditText.getText().toString());
                mUsernameEditText.setError(null);
                mPasswordEditText.setError(null);

                // Check if the input is valid
                if (TextUtils.isEmpty(username)) {
                    mUsernameEditText.setError("Username is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPasswordEditText.setError("Password is required");
                    return;
                }

                // Check if the entered username is already in the database
                mUsersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Username is already taken
                            mUsernameEditText.setError("Username is already taken");
                        } else {
                            // Username is available
                            // Create a new user object with the input fields
                            User user = new User(username, password, new UserCustomize());
                            progressBar.setVisibility(View.VISIBLE);
                            // Write the user object to the database
                            mUsersRef.child(username).setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // User was successfully added to the database

                                            currentUser = username;

                                            // Get the SharedPreferences editor
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            // Save a string variable named "password" to the SharedPreferences
                                            editor.putString("username", username);
                                            editor.apply();
                                            setBoardColorsToDiffault();
                                            setPiecesBitmapToDefault();
                                            Handler handler = new Handler();
                                            Thread thread = new Thread(()->{
                                                try {
                                                    Thread.sleep(6000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                Intent intent = new Intent(getApplicationContext(), homeActivity.class);
                                                handler.post(()->{    progressBar.setVisibility(View.GONE);});
                                                startActivity(intent);
                                                finish();
                                            });
                                            thread.start();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // An error occurred while writing to the database
                                            // Handle the error as appropriate
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // An error occurred while reading from the database
                        // Handle the error as appropriate
                    }
                });
            }
        });
    }

    //pull the user's bitmaps from the fire storage
    public void setPiecesBitmapToSaved(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String[] names = new String[]{"pawn","rook","knight","bishop","queen","king"};

        for (int i = 0; i < names.length; i++) {
            String currentName = currentUser+"w"+names[i];
            int index = i;

            // Replace "path/to/image" with the path to the image in Firebase Storage
            StorageReference storageRef = storage.getReference().child(currentName+".jpg");
            // Create a temporary file to save the image to
            File localFile = null;
            try {
                localFile = File.createTempFile(currentName, "jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Download the image from Firebase Storage and save it to the local file
            File finalLocalFile = localFile;
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Use the local file to set the source of the ImageView
                    bitmapsWhitePieces[index] = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failure event here
                    // Replace "path/to/image" with the path to the image in Firebase Storage
                    StorageReference storageRef = storage.getReference().child("w"+names[index]+".png");
                    // Create a temporary file to save the image to
                    File localFile = null;
                    try {
                        localFile = File.createTempFile("w"+names[index], "png");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Download the image from Firebase Storage and save it to the local file
                    File finalLocalFile = localFile;
                    storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Use the local file to set the source of the ImageView
                            bitmapsWhitePieces[index] = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle failure event here


                        }
                    });

                }
            });

        }


        for (int i = 0; i < names.length; i++) {
            String currentName = currentUser+"b"+names[i];
            int index = i;

            // Replace "path/to/image" with the path to the image in Firebase Storage
            StorageReference storageRef = storage.getReference().child(currentName+".jpg");
            // Create a temporary file to save the image to
            File localFile = null;
            try {
                localFile = File.createTempFile(currentName, "jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Download the image from Firebase Storage and save it to the local file
            File finalLocalFile = localFile;
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Use the local file to set the source of the ImageView
                    bitmapsBlackPieces[index] = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failure event here
                    // Replace "path/to/image" with the path to the image in Firebase Storage
                    StorageReference storageRef = storage.getReference().child("b"+names[index]+".png");
                    // Create a temporary file to save the image to
                    File localFile = null;
                    try {
                        localFile = File.createTempFile("b"+names[index], "png");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Download the image from Firebase Storage and save it to the local file
                    File finalLocalFile = localFile;
                    storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Use the local file to set the source of the ImageView
                            bitmapsBlackPieces[index] = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle failure event here


                        }
                    });

                }
            });

        }

    }


    //set default images to the user's soldiers
    public void setPiecesBitmapToDefault(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String[] names = new String[]{"pawn","rook","knight","bishop","queen","king"};

        for (int i = 0; i < names.length; i++) {
            String currentName = "w"+names[i]+".png";
            int index = i;


// Replace "path/to/image" with the path to the image in Firebase Storage
            StorageReference storageRef = storage.getReference().child(currentName);
// Create a temporary file to save the image to
            File localFile = null;
            try {
                localFile = File.createTempFile("w"+names[i], "png");
            } catch (IOException e) {
                e.printStackTrace();
            }

// Download the image from Firebase Storage and save it to the local file
            File finalLocalFile = localFile;
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Use the local file to set the source of the ImageView
                    bitmapsWhitePieces[index] = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failure event here
                }
            });

        }



        for (int i = 0; i < names.length; i++) {
            String currentName = "b"+names[i]+".png";
            int index = i;


// Replace "path/to/image" with the path to the image in Firebase Storage
            StorageReference storageRef = storage.getReference().child(currentName);


// Create a temporary file to save the image to
            File localFile = null;
            try {
                localFile = File.createTempFile("b"+names[i], "png");
            } catch (IOException e) {
                e.printStackTrace();
            }

// Download the image from Firebase Storage and save it to the local file
            File finalLocalFile = localFile;
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Use the local file to set the source of the ImageView
                    bitmapsBlackPieces[index] = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failure event here
                }
            });

        }
    }


    //pull the user's board colors
    public void setBoardColors(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        usersRef.child(currentUser).child("userCustomize").child("playerTwoColor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long customizeVariable = dataSnapshot.getValue(Long.class);
                // Do something with the customizeVariable value here
                colorTwo = Math.toIntExact(customizeVariable);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here
            }
        });

        usersRef.child(currentUser).child("userCustomize").child("playerOneColor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long customizeVariable = dataSnapshot.getValue(Long.class);
                // Do something with the customizeVariable value here
                colorOne = Math.toIntExact(customizeVariable);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here
            }
        });
    }

    //set default colors to the user's colors
    public void setBoardColorsToDiffault(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        usersRef.child(currentUser).child("userCustomize").child("playerOneColor").setValue(-2370091);
        usersRef.child(currentUser).child("userCustomize").child("playerTwoColor").setValue(-13816531);
        colorOne = Math.toIntExact(-2370091);
        colorTwo = Math.toIntExact(-13816531);

    }

    //get the password encrypt and return it
    public String getMd5Hash(String password) {
        try {
            // Get a MessageDigest instance for MD5 hashing algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Add the password bytes to the MessageDigest
            md.update(password.getBytes());

            // Get the hash bytes and convert them to a hexadecimal string
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}