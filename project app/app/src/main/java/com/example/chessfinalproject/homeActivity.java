package com.example.chessfinalproject;

import static com.example.chessfinalproject.LoginActivity.bitmapsBlackPieces;
import static com.example.chessfinalproject.LoginActivity.bitmapsWhitePieces;
import static com.example.chessfinalproject.LoginActivity.colorOne;
import static com.example.chessfinalproject.LoginActivity.colorTwo;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Random;

public class homeActivity extends Activity {
    boolean queenClicked = false;
    Context context = this;
    BoardTile[][] boardTilesMatrix;


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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent1 = new Intent(this, MusicService.class);
        startService(intent1);

        // bind to the MusicService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        buttonPauseResume = findViewById(R.id.stopResumeButton);
        buttonPauseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    musicService.pauseResume();
                    updateButtonState();
                }
            }
        });



        //load all bitmap

        //bitmapsBlackPieces: "pawn","rook","knight","bishop","queen","king"
        //bitmapsWhitePieces: "pawn","rook","knight","bishop","queen","king"

        int[][] queenMovement = new int[][]{{1,0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}    };
        int[][] queenAttack = new int[][]{{1,0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}    };
        int[][] pawnAttack =new int[][]{{-1,1} ,{-1,-1}};
        int[][] pawnMovement = new int[][]{{-1,0}};

        int cols = 9, rows = 15;


        LinearLayout boardLayout = findViewById(R.id.main);

        Board homeBoard = new Board(this, cols, rows, colorOne, colorTwo);
        boardLayout.addView(homeBoard);

        boardTilesMatrix = homeBoard.boardTileMatrix;

        boardTilesMatrix[8][4].currentSoldier = new Queen(bitmapsWhitePieces[4],true,new int[]{8,4},queenMovement,true,queenAttack, false,false,true);

        //set game
        boardTilesMatrix[8][1].currentSoldier = new Pawn(bitmapsBlackPieces[0],false,new int[]{8,1},pawnMovement,false,pawnAttack , false,false,false);

        //customize
        boardTilesMatrix[8][7].currentSoldier = new Pawn(bitmapsBlackPieces[0],false,new int[]{8,7},pawnMovement,false,pawnAttack , false,false,false);


        //settings
        boardTilesMatrix[5][4].currentSoldier = new Pawn(bitmapsBlackPieces[0],false,new int[]{5,4},pawnMovement,false,pawnAttack , false,false,false);


        boardTilesMatrix[8][4].invalidate();
        boardTilesMatrix[8][1].invalidate();
        boardTilesMatrix[8][7].invalidate();
        boardTilesMatrix[5][4].invalidate();

        //logout
        boardTilesMatrix[8][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!queenClicked)return;
                queenClicked = false;

                // Create a confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(homeActivity.this);
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Yes"
                        SharedPreferences sharedPreferences = getSharedPreferences("UsernamePref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String username = "";
                        editor.putString("username", username);
                        editor.apply();

                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);

                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "No", close the dialog
                        dialog.dismiss();
                    }
                });

                // Show the confirmation dialog
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

        //mark the pawns
        boardTilesMatrix[8][4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                queenClicked = true;
                boardTilesMatrix[8][1].setRedAura(true);
                boardTilesMatrix[8][7].setRedAura(true);
                boardTilesMatrix[5][4].setRedAura(true);

                boardTilesMatrix[8][1].invalidate();
                boardTilesMatrix[8][7].invalidate();
                boardTilesMatrix[5][4].invalidate();
            }
        });

        //open the customize activity
        boardTilesMatrix[8][7].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!queenClicked)return;
                queenClicked = false;
                Intent intent = new Intent(context, CustomizeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //open set game
        boardTilesMatrix[5][4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!queenClicked)return;
                queenClicked = false;
                Intent intent = new Intent(context, setGame.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        boardTilesMatrix[8][1].setRedAura(false);
        boardTilesMatrix[8][7].setRedAura(false);
        boardTilesMatrix[5][4].setRedAura(false);

        for (int i = 0; i < boardTilesMatrix.length; i++) {
            for (int j = 0; j < boardTilesMatrix[i].length; j++) {
                boardTilesMatrix[i][j].invalidate();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unbind from the MusicService
        if (isBound) {
            unbindService(serviceConnection);
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