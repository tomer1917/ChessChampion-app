package com.example.chessfinalproject;

import static com.example.chessfinalproject.LoginActivity.bitmapsBlackPieces;
import static com.example.chessfinalproject.LoginActivity.bitmapsWhitePieces;
import static com.example.chessfinalproject.LoginActivity.colorOne;
import static com.example.chessfinalproject.LoginActivity.colorTwo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class gameBoardActivity extends Activity {
    private Intent intent;

    private MusicService musicService;
    private boolean isBound = false;
    private Button buttonPauseResume;
    int cols = 8, rows = 8;

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


    ArrayList<Soldier> soldiers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameboard);

        LinearLayout boardLayout = findViewById(R.id.boardLayout);
        LinearLayout timerLayout = findViewById(R.id.timerLayout);
        ImageView turnImage = findViewById(R.id.turnImage);

        ImageView showNames = findViewById(R.id.showNames);


        intent = getIntent();
        int[] blackTime = intent.getIntArrayExtra("blackTime");
        int[] whiteTime = intent.getIntArrayExtra("whiteTime");
        int addTime = intent.getIntExtra("addTime", 15);
        int reduceTime = intent.getIntExtra("reduceTime", 15);
        boolean isWhitePlayer = intent.getBooleanExtra("isWhitePlayer", true);
        String gameMode = intent.getStringExtra("gameMode");

        boolean customGame = intent.getBooleanExtra("customGame", false);



        if(customGame)
        {
            setCustomPieces();
            Soldier.customChess = true;
        }
        else
        {
            setDefaultPiecesPosition();
            Soldier.customChess = false;
        }



        Soldier.rows = rows;
        Soldier.cols = cols;

        GameActivity gameActivity = new GameActivity();
        gameActivity.createCustomBoard(this, cols,rows,colorOne,colorTwo, soldiers, boardLayout);
        gameActivity.createTimer(getApplicationContext(),timerLayout,whiteTime,blackTime,addTime,reduceTime);
        gameActivity.setContext(gameBoardActivity.this);
        gameActivity.setImageView(turnImage);
        gameActivity.setShowNames(showNames);
        music();

        if (!isWhitePlayer) gameActivity.flipBoard();

    }



    public void setDefaultPiecesPosition(){

        int[][] rookMovement = new int[][]{{1,0},{0,1},{-1,0},{0,-1}};
        int[][] pawnMovement = new int[][]{{-1,0}};
        int[][] blackPawnMovement =  new int[][]{{1,0}};

        int[][] knightMovement = new int[][]{{-1,-2},{-1,2},{1,-2},{1,2}, {-2,-1},{-2,1},{2,-1},{2,1}};
        int[][] bishopMovement = new int[][]{ {1,1},{1,-1},{-1,1},{-1,-1} };
        int[][] queenMovement = new int[][]{{1,0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}    };
        int[][] kingMovement = new int[][]{{1,0} , {1,1}, {1,-1}, {0,1}, {0,-1}, {-1,1},{-1,-1},{-1,0}};



        int[][] rookAttack = new int[][]{{1,0},{0,1},{-1,0},{0,-1}};
        int[][] pawnAttack =new int[][]{{-1,1} ,{-1,-1}};
        int[][] blackPawnAttack=new int[][]{{1,-1} ,{1,1}};

        int[][] knightAttack =new int[][]{{-1,-2},{-1,2},{1,-2},{1,2}, {-2,-1},{-2,1},{2,-1},{2,1}};
        int[][] bishopAttack = new int[][]{ {1,1},{1,-1},{-1,1},{-1,-1} };
        int[][] queenAttack = new int[][]{{1,0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}    };
        int[][] kingAttack =new int[][]{{1,0} , {1,1}, {1,-1}, {0,1}, {0,-1}, {-1,1},{-1,-1},{-1,0}};


        //bitmapsBlackPieces: "pawn","rook","knight","bishop","queen","king"
        //bitmapsWhitePieces: "pawn","rook","knight","bishop","queen","king"

        for (int i = 0; i < 8; i++) {
            Pawn pawn = new Pawn(bitmapsBlackPieces[0],false,new int[]{1,i},blackPawnMovement,false,blackPawnAttack , false,false,false);
            soldiers.add(pawn);
        }
        for (int i = 8; i < 16; i++) {
            Pawn pawn = new Pawn(bitmapsWhitePieces[0],true,new int[]{6,i-8},pawnMovement,false,pawnAttack, false,false,false);
            soldiers.add(pawn);
        }

        soldiers.add(new Rook(bitmapsWhitePieces[1],true,new int[]{7,0},rookMovement,true,rookAttack, false,false,true));
        soldiers.add(new Knight(bitmapsWhitePieces[2],true,new int[]{7,1},knightMovement,false,knightAttack, true,true,false));
        soldiers.add(new Bishop(bitmapsWhitePieces[3],true,new int[]{7,2},bishopMovement,true,bishopAttack, false,false,true));
        soldiers.add(new Queen(bitmapsWhitePieces[4],true,new int[]{7,3},queenMovement,true,queenAttack, false,false,true));
        soldiers.add(new King(bitmapsWhitePieces[5],true,new int[]{7,4},kingMovement,false,kingAttack, false,false,false));
        soldiers.add(new Bishop(bitmapsWhitePieces[3],true,new int[]{7,5},bishopMovement,true,bishopAttack, false,false,true));
        soldiers.add(new Knight(bitmapsWhitePieces[2],true,new int[]{7,6},knightMovement,false,knightAttack, true,true,false));
        soldiers.add(new Rook(bitmapsWhitePieces[1],true,new int[]{7,7},rookMovement,true,rookAttack, false,false,true));


        soldiers.add(new Rook(bitmapsBlackPieces[1],false,new int[]{0,0},rookMovement,true,rookAttack, false,false,true));
        soldiers.add(new Knight(bitmapsBlackPieces[2],false,new int[]{0,1},knightMovement,false,knightAttack, true,true,false));
        soldiers.add(new Bishop(bitmapsBlackPieces[3],false,new int[]{0,2},bishopMovement,true,bishopAttack, false,false,true));
        soldiers.add(new Queen(bitmapsBlackPieces[4],false,new int[]{0,3},queenMovement,true,queenAttack, false,false,true));
        soldiers.add(new King(bitmapsBlackPieces[5],false,new int[]{0,4},kingMovement,false,kingAttack, false,false,false));
        soldiers.add(new Bishop(bitmapsBlackPieces[3],false,new int[]{0,5},bishopMovement,true,bishopAttack, false,false,true));
        soldiers.add(new Knight(bitmapsBlackPieces[2],false,new int[]{0,6},knightMovement,false,knightAttack, true,true,false));
        soldiers.add(new Rook(bitmapsBlackPieces[1],false,new int[]{0,7},rookMovement,true,rookAttack, false,false,true));



    }

    //get the movements and attacks of the custom soldiers
    public void setCustomPieces(){
        int[] type = intent.getIntArrayExtra("type");// 0 pawn 1 rook 2 knight 3 bishop 4 queen 5 king

        rows = intent.getIntExtra("row",8);
        cols = intent.getIntExtra("col",8);


        for (int i = 0; i < type.length; i++)  {
            boolean whitePiece = intent.getBooleanExtra("soldier" + i + "_whitePiece", false);
            int[] startingPosition = intent.getIntArrayExtra("soldier" + i + "_startingPosition");
            int[][] validMovement = (int[][]) intent.getSerializableExtra("soldier" + i + "_validMovement");
            boolean infiniteMovement = intent.getBooleanExtra("soldier" + i + "_infiniteMovement", false);
            int[][] validAttack = (int[][]) intent.getSerializableExtra("soldier" + i + "_validAttack");
            boolean canRunOver = intent.getBooleanExtra("soldier" + i + "_canRunOver", false);
            boolean canAttackOver = intent.getBooleanExtra("soldier" + i + "_canAttackOver", false);
            boolean infiniteAttack = intent.getBooleanExtra("soldier" + i + "_infiniteAttack", false);

            if (!whitePiece){
                for (int j = 0; j < validMovement.length; j++) {
                    for (int k = 0; k < validMovement[j].length; k++) {
                        validMovement[j][k] = -1*validMovement[j][k];
                    }
                }
                for (int j = 0; j < validAttack.length; j++) {
                    for (int k = 0; k < validAttack[j].length; k++) {
                        validAttack[j][k] = -1*validAttack[j][k];
                    }
                }
            }

            //add solider
            if(type[i]==0)
            {
                Bitmap bitmap;
                if (whitePiece)
                    bitmap = bitmapsWhitePieces[0];
                else
                    bitmap = bitmapsBlackPieces[0];
                soldiers.add(new Pawn(bitmap,  whitePiece, startingPosition,  validMovement, infiniteMovement,validAttack,canRunOver, canAttackOver,infiniteAttack));
            }
            else if(type[i]==1)
            {
                Bitmap bitmap;
                if (whitePiece)
                    bitmap = bitmapsWhitePieces[1];
                else
                    bitmap = bitmapsBlackPieces[1];
                soldiers.add(new Rook(bitmap,  whitePiece, startingPosition,  validMovement, infiniteMovement,validAttack,canRunOver, canAttackOver,infiniteAttack));
            }
            else if(type[i]==2)
            {
                Bitmap bitmap;
                if (whitePiece)
                    bitmap = bitmapsWhitePieces[2];
                else
                    bitmap = bitmapsBlackPieces[2];
                soldiers.add(new Knight(bitmap,  whitePiece, startingPosition,  validMovement, infiniteMovement,validAttack,canRunOver, canAttackOver,infiniteAttack));
            }
            else if(type[i]==3)
            {
                Bitmap bitmap;
                if (whitePiece)
                    bitmap = bitmapsWhitePieces[3];
                else
                    bitmap = bitmapsBlackPieces[3];
                soldiers.add(new Bishop(bitmap,  whitePiece, startingPosition,  validMovement, infiniteMovement,validAttack,canRunOver, canAttackOver,infiniteAttack));
            }
            else if(type[i]==4)
            {
                Bitmap bitmap;
                if (whitePiece)
                    bitmap = bitmapsWhitePieces[4];
                else
                    bitmap = bitmapsBlackPieces[4];
                soldiers.add(new Queen(bitmap,  whitePiece, startingPosition,  validMovement, infiniteMovement,validAttack,canRunOver, canAttackOver,infiniteAttack));
            }
            else if(type[i]==5)
            {
                Bitmap bitmap;
                if (whitePiece)
                    bitmap = bitmapsWhitePieces[5];
                else
                    bitmap = bitmapsBlackPieces[5];
                soldiers.add(new King(bitmap,  whitePiece, startingPosition,  validMovement, infiniteMovement,validAttack,canRunOver, canAttackOver,infiniteAttack));
            }
        }

    }

    //start the music service
    public void music(){
        intent = new Intent(this, MusicService.class);
        startService(intent);
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
            buttonPauseResume.setBackground(getResources().getDrawable(R.drawable.ic_baseline_volume_off_24)); // change the image of the button to "Resume"
        } else {
            buttonPauseResume.setBackground(getResources().getDrawable(R.drawable.ic_baseline_volume_up_24)); // change the text of the button to "Stop"
        }
    }

}