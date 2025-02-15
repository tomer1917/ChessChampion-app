package com.example.chessfinalproject;

import static com.example.chessfinalproject.LoginActivity.bitmapsBlackPieces;
import static com.example.chessfinalproject.LoginActivity.bitmapsWhitePieces;
import static com.example.chessfinalproject.LoginActivity.colorOne;
import static com.example.chessfinalproject.LoginActivity.colorTwo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class CustomSetBoard extends Activity {
    private ImageView[] whiteSoldiers = new ImageView[6];
    private ImageView[] blackSoldiers = new ImageView[6];
    int row = 8;
    int col = 8;
    EditText editText1;
    EditText editText2;
    Button applyButton;
    LinearLayout boardLayout;
    Context context = this;
    ArrayList<Soldier> soldiers = new ArrayList<>();
    BoardTile[][] boardTileMatrix;
    Soldier selectedSoldier;
    boolean eraserActive = false;
    boolean deleteSoldiers = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_set_board);


        selectSoldiers();
        setBoard();
        setBoardTileMatrixOnClick();
        changeToDiffaultBtn();

       ImageButton eraserButton = findViewById(R.id.eraserButton);
       //set eraser on
        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < whiteSoldiers.length; i++) {
                    whiteSoldiers[i].setBackground(null);
                    blackSoldiers[i].setBackground(null);
                    selectedSoldier = null;

                }
                eraserActive = true;
            }
        });


        Button saveButton = findViewById(R.id.saveButton);
        //save the board and continue to the next activity
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean whiteKing = false,blackKing = false;
                if (row+col<4)
                {
                    Toast.makeText(getApplicationContext(), "board size is to small", Toast.LENGTH_LONG).show();
                    return;
                }
                for (int i = 0; i < soldiers.size(); i++) {
                    if (soldiers.get(i) instanceof King){
                        if (soldiers.get(i).whitePiece)
                            whiteKing = true;
                        else
                            blackKing = true;
                    }
                    if (soldiers.get(i) instanceof Pawn && (soldiers.get(i).currentPosition[0]==0 || soldiers.get(i).currentPosition[0]==row-1 )){
                        Toast.makeText(getApplicationContext(), "pawns cannot stand on the first or last rows", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (!(blackKing && whiteKing)){
                    Toast.makeText(getApplicationContext(), "must select at least one king from each color", Toast.LENGTH_LONG).show();
                    return;
                }
                if (kingClash()){
                    Toast.makeText(getApplicationContext(), "different kings are one tile from each other", Toast.LENGTH_LONG).show();
                    return;
                }



                //send the soldiers list for the attack/movement modification activity. show a dialog to ask the user if he is sure
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to proceed?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Proceed to new intent
                                boolean[] isWhite = new boolean[soldiers.size()];
                                int[][] position = new int[soldiers.size()][2];
                                int[] type = new int[soldiers.size()];// 0 pawn 1 rook 2 knight 3 bishop 4 queen 5 king

                                for (int i = 0; i < soldiers.size() ; i++) {
                                    isWhite[i] = soldiers.get(i).whitePiece;
                                    position[i] = soldiers.get(i).currentPosition;

                                    if (soldiers.get(i) instanceof Pawn)
                                        type[i] = 0;
                                    else if (soldiers.get(i) instanceof Rook)
                                        type[i] = 1;
                                    else if (soldiers.get(i) instanceof Knight)
                                        type[i] = 2;
                                    else if (soldiers.get(i) instanceof Bishop)
                                        type[i] = 3;
                                    else if (soldiers.get(i) instanceof Queen)
                                        type[i] = 4;
                                    else
                                        type[i] = 5;
                                }


                                Intent intent = getIntent();
                                int[] blackTime = intent.getIntArrayExtra("blackTime");
                                int[] whiteTime = intent.getIntArrayExtra("whiteTime");
                                int addTime = intent.getIntExtra("addTime", 15);
                                int reduceTime = intent.getIntExtra("reduceTime", 15);
                                boolean isWhitePlayer = intent.getBooleanExtra("isWhitePlayer", true);

                                Intent intent2 = new Intent(context, setMovementActivity.class);
                                intent2.putExtra("isWhite", isWhite);
                                intent2.putExtra("position", position);
                                intent2.putExtra("type", type);
                                intent2.putExtra("row", row);
                                intent2.putExtra("col", col);


                                intent2.putExtra("blackTime", blackTime);
                                intent2.putExtra("whiteTime", whiteTime);
                                intent2.putExtra("addTime", addTime);
                                intent2.putExtra("reduceTime", reduceTime);
                                intent2.putExtra("isWhitePlayer", isWhitePlayer);

                                startActivity(intent2);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Close dialog
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                // Get the dialog window
                Window dialogWindow = alert.getWindow();
                if (dialogWindow != null) {
                    // Set the background drawable
                    dialogWindow.setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);
                }
                alert.show();
            }
        });


    }

    //set the board and soldiers to default
    public void changeToDiffaultBtn(){

        Button diffaultBtn = findViewById(R.id.changeToDefault);
        diffaultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soldiers.clear();

                Bitmap[] bitmap = new Bitmap[12];
                for (int i = 0; i < whiteSoldiers.length; i++) {
                    bitmap[i] = ((BitmapDrawable)whiteSoldiers[i].getDrawable()).getBitmap();
                }
                for (int i = 0; i < blackSoldiers.length; i++) {
                    bitmap[whiteSoldiers.length+i] = ((BitmapDrawable)blackSoldiers[i].getDrawable()).getBitmap();
                }


                for (int i = 0; i < 8; i++) {
                    Pawn pawn = new Pawn(bitmap[6],false,new int[]{1,i},new int[][]{{0,0}},false,new int[][]{{0,0}} , false,false,false);
                    soldiers.add(pawn);
                }
                for (int i = 8; i < 16; i++) {
                    Pawn pawn = new Pawn(bitmap[0],true,new int[]{6,i-8},new int[][]{{0,0}},false,new int[][]{{0,0}}, false,false,false);
                    soldiers.add(pawn);
                }

                soldiers.add(new Rook(bitmap[1],true,new int[]{7,0},new int[][]{{0,0}},true,new int[][]{{0,0}}, false,false,true));
                soldiers.add(new Knight(bitmap[2],true,new int[]{7,1},new int[][]{{0,0}},false,new int[][]{{0,0}}, true,true,false));
                soldiers.add(new Bishop(bitmap[3],true,new int[]{7,2},new int[][]{{0,0}},true,new int[][]{{0,0}}, false,false,true));
                soldiers.add(new Queen(bitmap[4],true,new int[]{7,3},new int[][]{{0,0}},true,new int[][]{{0,0}}, false,false,true));
                soldiers.add(new King(bitmap[5],true,new int[]{7,4},new int[][]{{0,0}},false,new int[][]{{0,0}}, false,false,false));
                soldiers.add(new Bishop(bitmap[3],true,new int[]{7,5},new int[][]{{0,0}},true,new int[][]{{0,0}}, false,false,true));
                soldiers.add(new Knight(bitmap[2],true,new int[]{7,6},new int[][]{{0,0}},false,new int[][]{{0,0}}, true,true,false));
                soldiers.add(new Rook(bitmap[1],true,new int[]{7,7},new int[][]{{0,0}},true,new int[][]{{0,0}}, false,false,true));


                soldiers.add(new Rook(bitmap[7],false,new int[]{0,0},new int[][]{{0,0}},true,new int[][]{{0,0}}, false,false,true));
                soldiers.add(new Knight(bitmap[8],false,new int[]{0,1},new int[][]{{0,0}},false,new int[][]{{0,0}}, true,true,false));
                soldiers.add(new Bishop(bitmap[9],false,new int[]{0,2},new int[][]{{0,0}},true,new int[][]{{0,0}}, false,false,true));
                soldiers.add(new Queen(bitmap[10],false,new int[]{0,3},new int[][]{{0,0}},true,new int[][]{{0,0}}, false,false,true));
                soldiers.add(new King(bitmap[11],false,new int[]{0,4},new int[][]{{0,0}},false,new int[][]{{0,0}}, false,false,false));
                soldiers.add(new Bishop(bitmap[9],false,new int[]{0,5},new int[][]{{0,0}},true,new int[][]{{0,0}}, false,false,true));
                soldiers.add(new Knight(bitmap[8],false,new int[]{0,6},new int[][]{{0,0}},false,new int[][]{{0,0}}, true,true,false));
                soldiers.add(new Rook(bitmap[7],false,new int[]{0,7},new int[][]{{0,0}},true,new int[][]{{0,0}}, false,false,true));


                editText1.setText("8");
                editText2.setText("8");
                deleteSoldiers = false;
                applyButton.callOnClick();
                deleteSoldiers = true;
                for (int i = 0; i < soldiers.size(); i++) {
                    int row = soldiers.get(i).currentPosition[0];
                    int col = soldiers.get(i).currentPosition[1];

                    boardTileMatrix[row][col].currentSoldier = soldiers.get(i);
                    boardTileMatrix[row][col].invalidate();
                }
            }
        });
    }

    //set each tile of the board an onclick listener
    public void setBoardTileMatrixOnClick(){
        for (int i = 0; i < boardTileMatrix.length; i++) {
            for (int j = 0; j < boardTileMatrix[i].length; j++) {
                int i1 = i;
                int j1 = j;
                boardTileMatrix[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // handle click event for boardTile

                        if (selectedSoldier ==null && !eraserActive) return;

                        if (eraserActive)
                        {
                            soldiers.remove(boardTileMatrix[i1][j1].currentSoldier);
                            boardTileMatrix[i1][j1].currentSoldier = null;
                        }
                        else
                        {
                            Soldier soldier;

                            selectedSoldier.currentPosition[0] = i1;
                            selectedSoldier.currentPosition[1] = j1;

                            if (selectedSoldier instanceof Pawn)
                                soldiers.add( new Pawn(selectedSoldier.imageBitmap,selectedSoldier.whitePiece,new int[]{i1,j1},selectedSoldier.validMovement,selectedSoldier.infiniteMovement,selectedSoldier.validAttack,selectedSoldier.canRunOver,selectedSoldier.canAttackOver,selectedSoldier.infiniteAttack));
                            else if (selectedSoldier instanceof Rook)
                                soldiers.add( new Rook(selectedSoldier.imageBitmap,selectedSoldier.whitePiece,new int[]{i1,j1},selectedSoldier.validMovement,selectedSoldier.infiniteMovement,selectedSoldier.validAttack,selectedSoldier.canRunOver,selectedSoldier.canAttackOver,selectedSoldier.infiniteAttack));
                            else if (selectedSoldier instanceof Knight)
                                soldiers.add( new Knight(selectedSoldier.imageBitmap,selectedSoldier.whitePiece,new int[]{i1,j1},selectedSoldier.validMovement,selectedSoldier.infiniteMovement,selectedSoldier.validAttack,selectedSoldier.canRunOver,selectedSoldier.canAttackOver,selectedSoldier.infiniteAttack));
                            else if (selectedSoldier instanceof Bishop)
                                soldiers.add( new Bishop(selectedSoldier.imageBitmap,selectedSoldier.whitePiece,new int[]{i1,j1},selectedSoldier.validMovement,selectedSoldier.infiniteMovement,selectedSoldier.validAttack,selectedSoldier.canRunOver,selectedSoldier.canAttackOver,selectedSoldier.infiniteAttack));
                            else if (selectedSoldier instanceof Queen)
                                soldiers.add( new Queen(selectedSoldier.imageBitmap,selectedSoldier.whitePiece,new int[]{i1,j1},selectedSoldier.validMovement,selectedSoldier.infiniteMovement,selectedSoldier.validAttack,selectedSoldier.canRunOver,selectedSoldier.canAttackOver,selectedSoldier.infiniteAttack));
                            else
                                soldiers.add(  new King(selectedSoldier.imageBitmap,selectedSoldier.whitePiece,new int[]{i1,j1},selectedSoldier.validMovement,selectedSoldier.infiniteMovement,selectedSoldier.validAttack,selectedSoldier.canRunOver,selectedSoldier.canAttackOver,selectedSoldier.infiniteAttack));

                            boardTileMatrix[i1][j1].currentSoldier = soldiers.get(soldiers.size()-1);

                        }
                        boardTileMatrix[i1][j1].invalidate();


                    }
                });
            }
        }

    }


    //mark the selected soldier
    public void selectSoldiers(){
        // get references to white soldiers image views
        whiteSoldiers[0] = findViewById(R.id.button1);
        whiteSoldiers[1] = findViewById(R.id.button2);
        whiteSoldiers[2] = findViewById(R.id.button3);
        whiteSoldiers[3] = findViewById(R.id.button4);
        whiteSoldiers[4] = findViewById(R.id.button5);
        whiteSoldiers[5] = findViewById(R.id.button6);

        // get references to black soldiers image views
        blackSoldiers[0] = findViewById(R.id.button7);
        blackSoldiers[1] = findViewById(R.id.button8);
        blackSoldiers[2] = findViewById(R.id.button9);
        blackSoldiers[3] = findViewById(R.id.button10);
        blackSoldiers[4] = findViewById(R.id.button11);
        blackSoldiers[5] = findViewById(R.id.button12);

        // create a common OnClickListener for all the image views
        View.OnClickListener whiteImageOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView currentImg = (ImageView) v;
                eraserActive = false;
                // remove the background image of the other image views
                for (int i =0;i<whiteSoldiers.length;i++) {
                    if (whiteSoldiers[i] != currentImg) {
                        whiteSoldiers[i].setBackground(null);
                    }else {
                        if (i==0){
                            Bitmap bitmap = ((BitmapDrawable)whiteSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new Pawn(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }else if (i==1){
                            Bitmap bitmap = ((BitmapDrawable)whiteSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new Rook(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }else if (i==2){
                            Bitmap bitmap = ((BitmapDrawable)whiteSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new Knight(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }else if(i==3){
                            Bitmap bitmap = ((BitmapDrawable)whiteSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new Bishop(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }else if (i==4){
                            Bitmap bitmap = ((BitmapDrawable)whiteSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new Queen(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }else if (i==5)
                        {
                            Bitmap bitmap = ((BitmapDrawable)whiteSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new King(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }
                    }
                }
                for (ImageView imageView : blackSoldiers) {
                    if (imageView != v) {
                        imageView.setBackground(null);
                    }
                }

                // set the clicked image view's background to #0073FF color
                currentImg.setBackgroundColor(Color.parseColor("#0073FF"));
            }
        };


        View.OnClickListener blackImageOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraserActive = false;
                ImageView currentImg = (ImageView) v;
                // remove the background image of the other image views
                for (int i =0;i<blackSoldiers.length;i++) {
                    if (blackSoldiers[i] != currentImg) {
                        blackSoldiers[i].setBackground(null);
                    }else {
                        if (i==0){
                            Bitmap bitmap = ((BitmapDrawable)blackSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new Pawn(bitmap,  false, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }else if (i==1){
                            Bitmap bitmap = ((BitmapDrawable)blackSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new Rook(bitmap,  false, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }else if (i==2){
                            Bitmap bitmap = ((BitmapDrawable)blackSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new Knight(bitmap,  false, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }else if(i==3){
                            Bitmap bitmap = ((BitmapDrawable)blackSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new Bishop(bitmap,  false, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }else if (i==4){
                            Bitmap bitmap = ((BitmapDrawable)blackSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new Queen(bitmap,  false, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }else if (i==5)
                        {
                            Bitmap bitmap = ((BitmapDrawable)blackSoldiers[i].getDrawable()).getBitmap();
                            selectedSoldier = new King(bitmap,  false, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false);
                        }
                    }
                }

                for (ImageView imageView : whiteSoldiers) {
                    if (imageView != v) {
                        imageView.setBackground(null);
                    }
                }
                // set the clicked image view's background to #0073FF color
                currentImg.setBackgroundColor(Color.parseColor("#0073FF"));
            }
        };


// set the OnClickListener to all the image views
        for (ImageView imageView : whiteSoldiers) {
            imageView.setOnClickListener(whiteImageOnClickListener);
        }
        for (ImageView imageView : blackSoldiers) {
            imageView.setOnClickListener(blackImageOnClickListener);
        }
    }

    //create the board
    public void setBoard(){
        // Define the EditTexts
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        applyButton = findViewById(R.id.applyChanges);

        boardLayout = findViewById(R.id.boardLayout);
        GameActivity gameActivity = new GameActivity();
        gameActivity.createCustomBoard(this, col,row,-1,-2, soldiers, boardLayout);
        boardTileMatrix = gameActivity.boardTileMatrix;


        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the values of the EditTexts
                String rowString = editText1.getText().toString();
                String colString = editText2.getText().toString();
                if (rowString.equals("") || colString.equals("")) return;

                // Parse the values to integers
                int newRow = Integer.parseInt(rowString);
                int newCol = Integer.parseInt(colString);

                //if(row==newRow&& col == newCol)return;

                // Update the row and col variables
                row = newRow;
                col = newCol;

                boardLayout.removeAllViews();


                //call this only when pressing apply and not when changing to diffault
                if (deleteSoldiers)
                    soldiers.clear();

                gameActivity.createCustomBoard(context, col,row,-1,-2, soldiers, boardLayout);
                boardTileMatrix = gameActivity.boardTileMatrix;
                setBoardTileMatrixOnClick();

            }
        });
    }

    //check if two kings are one tile from each other
    public boolean kingClash(){
        ArrayList<King> blackKing = new ArrayList<>();
        ArrayList<King> whiteKing = new ArrayList<>();
        for (int i = 0; i < soldiers.size(); i++) {
            if (soldiers.get(i) instanceof King){
                if (soldiers.get(i).whitePiece)
                    whiteKing.add((King) soldiers.get(i));
                else
                    blackKing.add((King) soldiers.get(i));
            }
        }
        for (int i = 0; i < whiteKing.size(); i++) {
            int row = whiteKing.get(i).currentPosition[0];
            int col = whiteKing.get(i).currentPosition[1];

            for (int j = 0; j < blackKing.size(); j++) {
                int row2 = blackKing.get(j).currentPosition[0];
                int col2 = blackKing.get(j).currentPosition[1];

                if (row == row2 && col == col2 -1||  col == col2 +1)//right or left
                //the king clash
                    return true;
                else if ((row == row2+1 && col == col2 -1)|| (row == row2+1 && col == col2 +1)) //cross
                    //the king clash
                    return true;
                else if ((row == row2-1 && col == col2 -1)|| (row == row2-1 && col == col2 +1))//cross
                    //the king clash
                    return true;
                else if ((row == row2-1 && col == col2 )|| (row == row2+1 && col == col2))//up and down
                    //the king clash
                    return true;
            }
        }
        return false;
    }
}