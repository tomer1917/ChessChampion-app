package com.example.chessfinalproject.ui.home;

import static com.example.chessfinalproject.LoginActivity.currentUser;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.chessfinalproject.MusicService;
import com.example.chessfinalproject.R;
import com.example.chessfinalproject.UserCustomize;
import com.example.chessfinalproject.homeActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import yuku.ambilwarna.AmbilWarnaDialog;

public class BoardFragment extends Fragment {

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





    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        View root = inflater.inflate(R.layout.fragment_board, container, false);
        return root;

    }


    int whiteColor = 0, blackColor = 0;

    @Override
    public void onStart() {
        super.onStart();


        Intent intent1 = new Intent(context, MusicService.class);
        context.startService(intent1);

        // bind to the MusicService
        Intent intent = new Intent(context, MusicService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);



        ImageButton button = context.findViewById(R.id.colorPicker);
        ImageView imageView1 = context.findViewById(R.id.imageView1);
        ImageView imageView2 = context.findViewById(R.id.imageView2);

        //get the color and set the background of the image to that color
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(context, Color.BLACK, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        whiteColor = color;
                        imageView1.setBackgroundColor(color);

                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }

                });
                dialog.show();
            }
        });

        //get the color and set the background of the image to that color
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(context, Color.BLACK, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        imageView2.setBackgroundColor(color);
                        blackColor = color;
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }

                });

                dialog.show();
            }
        });

        //add the colors to the fire base
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = database.getReference("users");




                if (whiteColor == 0 || blackColor == 0){
//                    UserCustomize.setPlayerOneColor(R.drawable.brighttile);
//                    UserCustomize.setPlayerTwoColor(R.drawable.browntile);


                    usersRef.child(currentUser).child("userCustomize").child("playerOneColor").setValue(R.drawable.brighttile);
                    usersRef.child(currentUser).child("userCustomize").child("playerTwoColor").setValue(R.drawable.browntile);

                    return;
                }
                //add colors to database


                usersRef.child(currentUser).child("userCustomize").child("playerOneColor").setValue(whiteColor);
                usersRef.child(currentUser).child("userCustomize").child("playerTwoColor").setValue(blackColor);

//                UserCustomize.setPlayerOneColor(whiteColor);
//                UserCustomize.setPlayerTwoColor(blackColor);
            }
        });



        ImageButton returnHome = context.findViewById(R.id.returnHome);
        returnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, homeActivity.class);
                startActivity(intent);
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

    private void updateButtonState() {
        if (isBound && musicService.isPaused()) {
            buttonPauseResume.setBackground(getResources().getDrawable(R.drawable.ic_baseline_volume_off_24)); // change the text of the button to "Resume"
        } else {
            buttonPauseResume.setBackground(getResources().getDrawable(R.drawable.ic_baseline_volume_up_24)); // change the text of the button to "Stop"
        }
    }
}
