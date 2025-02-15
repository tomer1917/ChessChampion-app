package com.example.chessfinalproject.ui.gallery;

import static com.example.chessfinalproject.LoginActivity.bitmapsBlackPieces;
import static com.example.chessfinalproject.LoginActivity.bitmapsWhitePieces;
import static com.example.chessfinalproject.LoginActivity.currentUser;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.example.chessfinalproject.MusicService;
import com.example.chessfinalproject.R;
import com.example.chessfinalproject.homeActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class SoliderFragment extends Fragment {
    Activity context;
    private MusicService musicService;
    private boolean isBound = false;
    private Button buttonPauseResume;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };



    private ImageButton mButtonChooseImage;
    private StorageReference storageRef;
    private ImageView[] pieces = new ImageView[6];
    private ActivityResultLauncher<String> pickImageLauncher;
    TextView[] textViews = new TextView[6];
    int selectedIndex = 0;
    String blackOrWhite = "w";//"w" for white "b" for black
    ActivityResultLauncher<Intent> activity01Launcher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        context = getActivity();
        View root = inflater.inflate(R.layout.fragment_solider, container, false);



        activity01Launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                // code to process data from activity called

                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    pieces[selectedIndex].setImageBitmap(photo);
                    // Get a reference to the Firebase Cloud Storage instance
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    storageRef = storage.getReference();

                    //delete previous image and set new
                    StorageReference imageRef1 = storage.getReference().child(currentUser + blackOrWhite + textViews[selectedIndex].getText().toString() + ".jpg");
                    imageRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Image deleted successfully
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Image not deleted
                        }
                    });




                    // Upload the selected image to Firebase Cloud Storage
                    StorageReference imageRef = storageRef.child(currentUser + blackOrWhite + textViews[selectedIndex].getText().toString() + ".jpg");
                    try {
                        InputStream stream = getActivity().getContentResolver().openInputStream(bitmapToUri(photo));
                        UploadTask uploadTask = imageRef.putStream(stream);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Image uploaded successfully
                                Toast.makeText(context, "Upload successful", Toast.LENGTH_LONG).show();
                                if (blackOrWhite.equals("w")) {
                                    // Get the Bitmap of the ImageView

                                    bitmapsWhitePieces[selectedIndex] = photo;

                                } else {
                                    // Get the Bitmap of the ImageView
                                    bitmapsBlackPieces[selectedIndex] = photo;
                                }

                            }
                        });
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Image upload failed
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }
        });

        return root;

    }


    public Uri bitmapToUri(Bitmap bitmap) {
        File imageFile = new File(context.getCacheDir(), "image.jpg");
        try (OutputStream outputStream = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(imageFile);
    }

    @Override
    public void onStart() {
        super.onStart();


        Intent intent1 = new Intent(context, MusicService.class);
        context.startService(intent1);

        // bind to the MusicService
        Intent intent = new Intent(context, MusicService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);


        mButtonChooseImage = context.findViewById(R.id.button_choose_image);
        pieces[0] = context.findViewById(R.id.pawn);
        pieces[1] = context.findViewById(R.id.rook);
        pieces[2] = context.findViewById(R.id.knight);
        pieces[3] = context.findViewById(R.id.bishop);
        pieces[4] = context.findViewById(R.id.queen);
        pieces[5] = context.findViewById(R.id.king);

        textViews[0] = context.findViewById(R.id.pawn_text);
        textViews[1] = context.findViewById(R.id.rook_text);
        textViews[2] = context.findViewById(R.id.knight_text);
        textViews[3] = context.findViewById(R.id.bishop_text);
        textViews[4] = context.findViewById(R.id.queen_text);
        textViews[5] = context.findViewById(R.id.king_text);



        requestPermission(Manifest.permission.CAMERA);

        //when clicked open a dialog asking the user if he wants to use camera or gallery
        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Choose an Option")
                        .setItems(new CharSequence[]{"Open Camera", "Open Gallery"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                    activity01Launcher.launch(cameraIntent);
/*

                                    Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    context.startActivityForResult(openCamera,123);
*/

                                } else if (which == 1) {
                                    pickImageLauncher.launch("image/*");
                                }
                            }
                        })
                        .setCancelable(true);

                AlertDialog dialog = builder.create();
                // Get the dialog window
                Window dialogWindow = dialog.getWindow();

                if (dialogWindow != null) {
                    // Set the background drawable
                    dialogWindow.setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);
                }
                dialog.show();
            }
        });


        for (int i = 0; i < textViews.length; i++) {
            final int index = i; // need to create a final variable for inner class
            pieces[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int j = 0; j < textViews.length; j++) {
                        if (j == index) {
                            {

                                textViews[j].setTextColor(Color.parseColor("#03A9F4"));
                                selectedIndex = j;

                            }
                        } else {
                            textViews[j].setTextColor(Color.BLACK);
                        }
                    }

                }
            });
        }

        //changes the current soldier to diffault
        ImageButton changeToDefault = context.findViewById(R.id.changeToDefault);
        changeToDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                storageRef = storage.getReference();

                //delete previous image and set new
                StorageReference imageRef1 = storage.getReference().child(currentUser + blackOrWhite + textViews[selectedIndex].getText().toString() + ".jpg");
                imageRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Image deleted successfully
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Image not deleted
                    }
                });


// Replace "path/to/image" with the path to the image in Firebase Storage
                StorageReference storageRef = storage.getReference().child(currentUser);
// Create a temporary file to save the image to
                File localFile = null;
                try {
                    localFile = File.createTempFile( blackOrWhite + textViews[selectedIndex].getText().toString(), "png");
                } catch (IOException e) {
                    e.printStackTrace();
                }

// Download the image from Firebase Storage and save it to the local file
                File finalLocalFile = localFile;
                storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Use the local file to set the source of the ImageView
                        bitmapsWhitePieces[selectedIndex] = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                        pieces[selectedIndex].setImageBitmap(bitmapsWhitePieces[selectedIndex]);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle failure event here
                    }
                });


            }
        });

        //change the soldier to white or black
        Switch piecesSwitch = context.findViewById(R.id.pieces_switch);
        piecesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Player Two Pieces selected
                    blackOrWhite = "b";

                    //set all the images to black Pieces
                    pieces[0].setBackground(getResources().getDrawable(R.drawable.bpawn));
                    pieces[1].setBackground(getResources().getDrawable(R.drawable.brook));
                    pieces[2].setBackground(getResources().getDrawable(R.drawable.bknight));
                    pieces[3].setBackground(getResources().getDrawable(R.drawable.bbishop));
                    pieces[4].setBackground(getResources().getDrawable(R.drawable.bqueen));
                    pieces[5].setBackground(getResources().getDrawable(R.drawable.bking));
                } else {
                    // Player One Pieces selected
                    blackOrWhite = "w";


                    //set all the images to white Pieces
                    pieces[0].setBackground(getResources().getDrawable(R.drawable.wpawn));
                    pieces[1].setBackground(getResources().getDrawable(R.drawable.wrook));
                    pieces[2].setBackground(getResources().getDrawable(R.drawable.wknight));
                    pieces[3].setBackground(getResources().getDrawable(R.drawable.wbishop));
                    pieces[4].setBackground(getResources().getDrawable(R.drawable.wqueen));
                    pieces[5].setBackground(getResources().getDrawable(R.drawable.wking));

                }
            }
        });


        ImageButton returnHome = context.findViewById(R.id.returnHome2);
        returnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, homeActivity.class);
                startActivity(intent);

            }
        });





    }




    //check if the permission is granted
    public boolean requestPermission(String permission)
    {
        if (permissionGranted(permission))
            return true;
        // Ask for permission
        ActivityCompat.requestPermissions(context, new String[]{permission}, 1);
        return false;
    }

    //ask for permission
    public boolean permissionGranted(String permission)
    {
        // Check if the app is api 23 or above
        // If so, must ask for permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (context.checkSelfPermission(permission) ==
                    PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
            return false;
        }
        // Permission is automatically granted on sdk<23 upon installation
        return true;
    }

    //save the image from the gallery on fire storage
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


        // Get a reference to the Firebase Cloud Storage instance
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if(uri != null) {
                    pieces[selectedIndex].setImageURI(uri);
                    //delete previous image and set new
                    StorageReference imageRef1 = storage.getReference().child(currentUser + blackOrWhite + textViews[selectedIndex].getText().toString() + ".jpg");
                    imageRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Image deleted successfully
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Image not deleted
                        }
                    });

                    // Upload the selected image to Firebase Cloud Storage
                    StorageReference imageRef = storageRef.child(currentUser + blackOrWhite + textViews[selectedIndex].getText().toString() + ".jpg");
                    try {
                        InputStream stream = getActivity().getContentResolver().openInputStream(uri);
                        UploadTask uploadTask = imageRef.putStream(stream);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Image uploaded successfully
                                Toast.makeText(context, "Upload successful", Toast.LENGTH_LONG).show();
                                if (blackOrWhite.equals("w")) {
                                    // Get the Bitmap of the ImageView

                                    bitmapsWhitePieces[selectedIndex] = ((BitmapDrawable) (pieces[selectedIndex].getDrawable())).getBitmap();

                                } else {
                                    // Get the Bitmap of the ImageView
                                    bitmapsBlackPieces[selectedIndex] = ((BitmapDrawable) (pieces[selectedIndex].getDrawable())).getBitmap();
                                }

                            }
                        });
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Image upload failed
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });






    }


    @Override
    public void onResume(){
        super.onResume();
        buttonPauseResume = context.findViewById(R.id.stopResumeButtonFrag);
        buttonPauseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBound) {
                    musicService.pauseResume();
                    updateButtonState();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // unbind from the MusicService
        if (isBound) {
            context.unbindService(serviceConnection);
            isBound = false;
        }
    }

    //switch music btn image.
    private void updateButtonState() {
        if (isBound && musicService.isPaused()) {
            buttonPauseResume.setBackground(getResources().getDrawable(R.drawable.ic_baseline_volume_off_24)); // change the text of the button to "Resume"
        } else {
            buttonPauseResume.setBackground(getResources().getDrawable(R.drawable.ic_baseline_volume_up_24)); // change the text of the button to "Stop"
        }
    }



}
