package com.example.chessfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class setMovementActivity extends Activity {

    ArrayList<ImageView> imageViewList = new ArrayList<>();
    ArrayList<Soldier> soldiers = new ArrayList<>();
    BoardTile[][] boardTileMatrix;
    int row,col;
    LinearLayout boardLayout, whiteSoldiers;
    TextView movementValueTextView, attackValueTextView;
    Soldier selectedSoldier = null;
    boolean[] isWhite;
    int[][] position;
    int[] type;
    BoardTile selectedBoardTile = null;
    Button addButton, removeBtn, finishBtn, continueBtn, changeToDefault;

    ArrayList<int[]> validMovement = new ArrayList<>();
    ArrayList<int[]> validAttack = new ArrayList<>();

    ArrayList<Soldier> soldierPreference = new ArrayList<>();// the setting of every soldier
    ArrayList<Soldier> allSoldiers = new ArrayList<>();// the last array with all the soldiers set
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_movement);

        Intent intent = getIntent();
        isWhite = intent.getBooleanArrayExtra("isWhite");
        position = (int[][]) intent.getSerializableExtra("position");
        type = intent.getIntArrayExtra("type");// 0 pawn 1 rook 2 knight 3 bishop 4 queen 5 king

        row = intent.getIntExtra("row",8);
        col = intent.getIntExtra("col",8);
        boardLayout = findViewById(R.id.boardLayout);
        whiteSoldiers = findViewById(R.id.whiteSoldiers);

        //create the board
        GameActivity gameActivity = new GameActivity();
        gameActivity.createCustomBoard(this, col,row,-1,-2, soldiers, boardLayout);
        boardTileMatrix = gameActivity.boardTileMatrix;



         movementValueTextView = findViewById(R.id.movement_value);
         attackValueTextView = findViewById(R.id.attack_value);


        addImageViewsToLinearLayout();
        setBoardTileMatrixOnClick();


        Switch movementSwitch = findViewById(R.id.movementSwitch);
        CheckBox infiniteMovementCheckBox = findViewById(R.id.checkbox_infinite_movement);
        CheckBox canMoveOverCheckBox = findViewById(R.id.checkbox_can_move_over);
        CheckBox infiniteAttackCheckBox = findViewById(R.id.checkbox_infinite_attack);
        CheckBox canAttackOverCheckBox = findViewById(R.id.checkbox_can_attack_over);




        addButton = findViewById(R.id.addButton);
        //add the selected matrix
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedBoardTile == null) return;
                Boolean infinite, runOver,movement;

                movement = movementSwitch.isChecked();


                int[] valid = new int[2];

                valid[0] = selectedBoardTile.row- row/2;
                valid[1] = selectedBoardTile.col- col/2;

                if (!movement){

                    for (int[] arr : validMovement) {
                        if (Arrays.equals(arr, valid)) {
                            Toast.makeText(getApplicationContext(), "movement already picked", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    String movementText;
                    if (validMovement.isEmpty())
                        movementText = "{"+valid[0]+","+valid[1]+"}";
                    else
                        movementText = movementValueTextView.getText().toString() +" ,{"+valid[0]+","+valid[1]+"}";
                    movementValueTextView.setText(movementText);
                    validMovement.add(valid);
                }
                else {
                    for (int[] arr : validAttack) {
                        if (Arrays.equals(arr, valid)) {
                            Toast.makeText(getApplicationContext(), "attack already picked", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    String attackText;
                    if (validAttack.isEmpty())
                         attackText = "{"+valid[0]+","+valid[1]+"}";
                    else
                         attackText = attackValueTextView.getText().toString() +" ,{"+valid[0]+","+valid[1]+"}";
                    attackValueTextView.setText(attackText);
                    validAttack.add(valid);
                }
            }
        });

        removeBtn = findViewById(R.id.remove);
        //remove the selected matrix
        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (! movementSwitch.isChecked() && validMovement.size()>0){
                    validMovement.remove(validMovement.size()-1);
                    if (validMovement.size()==0){
                        movementValueTextView.setText("");
                        return;
                    }
                    String movementText =  "{"+ validMovement.get(0)[0]+","+validMovement.get(0)[1]+"}";

                    for (int i = 1; i < validMovement.size(); i++) {
                        movementText += " ,{"+validMovement.get(i)[0]+","+validMovement.get(i)[1]+"}";
                    }
                    movementValueTextView.setText(movementText);
                }
                else if (movementSwitch.isChecked()&&validAttack.size()>0){
                    validAttack.remove(validAttack.size()-1);
                    if (validAttack.size()==0){
                        attackValueTextView.setText("");
                        return;
                    }

                    String attackText =  "{"+ validAttack.get(0)[0]+","+validAttack.get(0)[1]+"}";

                    for (int i = 1; i < validAttack.size(); i++) {
                        attackText += " ,{"+validAttack.get(i)[0]+","+validAttack.get(i)[1]+"}";
                    }
                    attackValueTextView.setText(attackText);
                }
            }
        });

        finishBtn = findViewById(R.id.finishBtn);
        //finish editing the selected soldier
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validAttack.isEmpty() || validMovement.isEmpty()) return;

                boolean infiniteMovement = infiniteMovementCheckBox.isChecked();
                boolean canRunOver = canMoveOverCheckBox.isChecked();

                boolean infiniteAttack = infiniteAttackCheckBox.isChecked();
                boolean canAttackOver = canAttackOverCheckBox.isChecked();

                int[][] validMovementArray = validMovement.toArray(new int[validMovement.size()][]);
                int[][] validAttackArray = validAttack.toArray(new int[validAttack.size()][]);


                selectedSoldier.infiniteAttack = infiniteAttack;
                selectedSoldier.infiniteMovement = infiniteMovement;
                selectedSoldier.canRunOver = canRunOver;
                selectedSoldier.canAttackOver = canAttackOver;
                selectedSoldier.validAttack = validAttackArray;
                selectedSoldier.validMovement = validMovementArray;


                for (int i = 0; i < soldierPreference.size(); i++) {
                    if (selectedSoldier.getClass() == soldierPreference.get(i).getClass()){
                        soldierPreference.set(i, selectedSoldier);
                        return;
                    }
                }
                soldierPreference.add(selectedSoldier);
                validMovement.clear();
                validAttack.clear();

                for (int i = 0; i < imageViewList.size(); i++) {
                    if (imageViewList.get(i).getBackground() != null){
                        imageViewList.get(i).setBackground(null);
                        imageViewList.get(i).setOnClickListener(null);
                        Toast.makeText(getApplicationContext(), "uploaded!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        continueBtn = findViewById(R.id.continueBtn);
        //move to the next activity
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (soldierPreference.size() != imageViewList.size())
                    return;

                ImageView imageView = new ImageView(getApplicationContext());
                for (int i = 0; i < type.length; i++) {
                    if(type[i]==0)
                    {
                        for (int j = 0; j < soldierPreference.size(); j++) {
                            if (soldierPreference.get(j) instanceof Pawn){
                                if (isWhite[i])
                                    imageView.setImageResource(R.drawable.wpawn);
                                else
                                    imageView.setImageResource(R.drawable.bpawn);


                                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                                allSoldiers.add(new Pawn(bitmap,  isWhite[i], position[i],  soldierPreference.get(j).validMovement
                                        , soldierPreference.get(j).infiniteMovement,soldierPreference.get(j).validAttack, soldierPreference.get(j).canRunOver,
                                        soldierPreference.get(j).canAttackOver,soldierPreference.get(j).infiniteAttack));
                            }
                        }
                    }
                    else if(type[i]==1)
                    {
                        for (int j = 0; j < soldierPreference.size(); j++) {
                            if (soldierPreference.get(j) instanceof Rook){
                                if (isWhite[i])
                                    imageView.setImageResource(R.drawable.wrook);
                                else
                                    imageView.setImageResource(R.drawable.brook);
                                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                                allSoldiers.add(new Rook(bitmap,  isWhite[i], position[i],  soldierPreference.get(j).validMovement
                                        , soldierPreference.get(j).infiniteMovement,soldierPreference.get(j).validAttack, soldierPreference.get(j).canRunOver,
                                        soldierPreference.get(j).canAttackOver,soldierPreference.get(j).infiniteAttack));
                            }
                        }
                    }
                    else if(type[i]==2)
                    {
                        for (int j = 0; j < soldierPreference.size(); j++) {
                            if (soldierPreference.get(j) instanceof Knight){
                                if (isWhite[i])
                                    imageView.setImageResource(R.drawable.wknight);
                                else
                                    imageView.setImageResource(R.drawable.bknight);
                                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                                allSoldiers.add(new Knight(bitmap,  isWhite[i], position[i],  soldierPreference.get(j).validMovement
                                        , soldierPreference.get(j).infiniteMovement,soldierPreference.get(j).validAttack, soldierPreference.get(j).canRunOver,
                                        soldierPreference.get(j).canAttackOver,soldierPreference.get(j).infiniteAttack));
                            }
                        }

                    }
                    else if(type[i]==3)
                    {
                        for (int j = 0; j < soldierPreference.size(); j++) {
                            if (soldierPreference.get(j) instanceof Bishop){
                                if (isWhite[i])
                                    imageView.setImageResource(R.drawable.wbishop);
                                else
                                    imageView.setImageResource(R.drawable.bbishop);
                                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                                allSoldiers.add(new Bishop(bitmap,  isWhite[i], position[i],  soldierPreference.get(j).validMovement
                                        , soldierPreference.get(j).infiniteMovement,soldierPreference.get(j).validAttack, soldierPreference.get(j).canRunOver,
                                        soldierPreference.get(j).canAttackOver,soldierPreference.get(j).infiniteAttack));
                            }
                        }
                    }
                    else if(type[i]==4)
                    {
                        for (int j = 0; j < soldierPreference.size(); j++) {
                            if (soldierPreference.get(j) instanceof Queen){
                                if (isWhite[i])
                                    imageView.setImageResource(R.drawable.wqueen);
                                else
                                    imageView.setImageResource(R.drawable.bqueen);
                                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                                allSoldiers.add(new Queen(bitmap,  isWhite[i], position[i],  soldierPreference.get(j).validMovement
                                        , soldierPreference.get(j).infiniteMovement,soldierPreference.get(j).validAttack, soldierPreference.get(j).canRunOver,
                                        soldierPreference.get(j).canAttackOver,soldierPreference.get(j).infiniteAttack));
                            }
                        }

                    }
                    else if(type[i]==5)
                    {
                        for (int j = 0; j < soldierPreference.size(); j++) {
                            if (soldierPreference.get(j) instanceof King){
                                if (isWhite[i])
                                    imageView.setImageResource(R.drawable.wking);
                                else
                                    imageView.setImageResource(R.drawable.bking);
                                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                                allSoldiers.add(new King(bitmap,  isWhite[i], position[i],  soldierPreference.get(j).validMovement
                                        , soldierPreference.get(j).infiniteMovement,soldierPreference.get(j).validAttack, soldierPreference.get(j).canRunOver,
                                        soldierPreference.get(j).canAttackOver,soldierPreference.get(j).infiniteAttack));
                            }
                        }
                    }
                }


/*
                boardLayout.removeAllViews();
                attackValueTextView.setText("");
                movementValueTextView.setText("");

                GameActivity gameActivity = new GameActivity();
                gameActivity.createCustomBoard(context, col,row,-1,-2, allSoldiers, boardLayout);
                boardTileMatrix = gameActivity.boardTileMatrix;
*/




                //open game board activity and send soldiers via intent
                Intent intent = getIntent();
                int[] blackTime = intent.getIntArrayExtra("blackTime");
                int[] whiteTime = intent.getIntArrayExtra("whiteTime");
                int addTime = intent.getIntExtra("addTime", 15);
                int reduceTime = intent.getIntExtra("reduceTime", 15);
                boolean isWhitePlayer = intent.getBooleanExtra("isWhitePlayer", true);



                Intent intent2 = new Intent(context, gameBoardActivity.class);
                intent2.putExtra("type", type);
                intent2.putExtra("row", row);
                intent2.putExtra("col", col);


                intent2.putExtra("customGame", true);
                intent2.putExtra("blackTime", blackTime);
                intent2.putExtra("whiteTime", whiteTime);
                intent2.putExtra("addTime", addTime);
                intent2.putExtra("reduceTime", reduceTime);
                intent2.putExtra("isWhitePlayer", isWhitePlayer);

                for (int i = 0; i < allSoldiers.size(); i++) {
                    Soldier soldier = allSoldiers.get(i);
                    intent2.putExtra("soldier" + i + "_whitePiece", soldier.whitePiece);
                    intent2.putExtra("soldier" + i + "_startingPosition", soldier.startingPosition);
                    intent2.putExtra("soldier" + i + "_validMovement", soldier.validMovement);
                    intent2.putExtra("soldier" + i + "_infiniteMovement", soldier.infiniteMovement);
                    intent2.putExtra("soldier" + i + "_validAttack", soldier.validAttack);
                    intent2.putExtra("soldier" + i + "_canRunOver", soldier.canRunOver);
                    intent2.putExtra("soldier" + i + "_canAttackOver", soldier.canAttackOver);
                    intent2.putExtra("soldier" + i + "_infiniteAttack", soldier.infiniteAttack);
                }


                startActivity(intent2);


            }
        });

        changeToDefault = findViewById(R.id.changeToDefault);
        //change the movement and attack to default
        changeToDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectedSoldier == null)
                    return;

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

                attackValueTextView.setText("");
                movementValueTextView.setText("");

                validAttack.clear();
                validMovement.clear();

                if (selectedSoldier instanceof Pawn){
                    validAttack.add(pawnAttack[0]);
                    validAttack.add(pawnAttack[1]);
                    validMovement.add(pawnMovement[0]);

                    String movTxt = "{"+pawnMovement[0][0]+","+pawnMovement[0][1]+"}";
                    String attackTxt = "{"+pawnAttack[0][0]+","+pawnAttack[0][1]+"} ,{" +pawnAttack[1][0]+","+pawnAttack[1][1]+"}";

                    infiniteAttackCheckBox.setChecked(false);
                    infiniteMovementCheckBox.setChecked(false);
                    canAttackOverCheckBox.setChecked(false);
                    canMoveOverCheckBox.setChecked(false);

                    movementValueTextView.setText( movTxt);
                    attackValueTextView.setText(attackTxt);

                }
                else if (selectedSoldier instanceof Rook){
                    validAttack.add(rookAttack[0]);
                    validMovement.add(rookMovement[0]);

                    String movTxt = "{"+rookMovement[0][0]+","+rookMovement[0][1]+"}";
                    String attackTxt = "{"+rookAttack[0][0]+","+rookAttack[0][1]+"}";


                    for (int i = 1; i < rookMovement.length; i++) {
                        validMovement.add(rookMovement[i]);
                        movTxt += " ,{"+rookMovement[i][0]+","+rookMovement[i][1]+"}";
                    }
                    for (int i = 1; i < rookAttack.length; i++) {
                        validAttack.add(rookAttack[i]);
                        attackTxt += " ,{"+rookAttack[i][0]+","+rookAttack[i][1]+"}";
                    }
                    infiniteAttackCheckBox.setChecked(true);
                    infiniteMovementCheckBox.setChecked(true);
                    canAttackOverCheckBox.setChecked(false);
                    canMoveOverCheckBox.setChecked(false);

                    movementValueTextView.setText( movTxt);
                    attackValueTextView.setText(attackTxt);

                }
                else if (selectedSoldier instanceof Bishop){
                    validAttack.add(bishopAttack[0]);
                    validMovement.add(bishopMovement[0]);

                    String movTxt = "{"+bishopMovement[0][0]+","+bishopMovement[0][1]+"}";
                    String attackTxt = "{"+bishopAttack[0][0]+","+bishopAttack[0][1]+"}";


                    for (int i = 1; i < bishopMovement.length; i++) {
                        validMovement.add(bishopMovement[i]);
                        movTxt += " ,{"+bishopMovement[i][0]+","+bishopMovement[i][1]+"}";
                    }
                    for (int i = 1; i < bishopAttack.length; i++) {
                        validAttack.add(bishopAttack[i]);
                        attackTxt += " ,{"+bishopAttack[i][0]+","+bishopAttack[i][1]+"}";
                    }
                    infiniteAttackCheckBox.setChecked(true);
                    infiniteMovementCheckBox.setChecked(true);
                    canAttackOverCheckBox.setChecked(false);
                    canMoveOverCheckBox.setChecked(false);

                    movementValueTextView.setText( movTxt);
                    attackValueTextView.setText(attackTxt);

                }

                else if (selectedSoldier instanceof Knight){
                    validAttack.add(knightAttack[0]);
                    validMovement.add(knightMovement[0]);

                    String movTxt = "{"+knightMovement[0][0]+","+knightMovement[0][1]+"}";
                    String attackTxt = "{"+knightAttack[0][0]+","+knightAttack[0][1]+"}";


                    for (int i = 1; i < knightMovement.length; i++) {
                        validMovement.add(knightMovement[i]);
                        movTxt += " ,{"+knightMovement[i][0]+","+knightMovement[i][1]+"}";
                    }
                    for (int i = 1; i < knightAttack.length; i++) {
                        validAttack.add(knightAttack[i]);
                        attackTxt += " ,{"+knightAttack[i][0]+","+knightAttack[i][1]+"}";
                    }
                    infiniteAttackCheckBox.setChecked(false);
                    infiniteMovementCheckBox.setChecked(false);
                    canAttackOverCheckBox.setChecked(true);
                    canMoveOverCheckBox.setChecked(true);

                    movementValueTextView.setText( movTxt);
                    attackValueTextView.setText(attackTxt);

                }
                else if (selectedSoldier instanceof Queen){
                    validAttack.add(queenAttack[0]);
                    validMovement.add(queenMovement[0]);

                    String movTxt = "{"+queenMovement[0][0]+","+queenMovement[0][1]+"}";
                    String attackTxt = "{"+queenAttack[0][0]+","+queenAttack[0][1]+"}";


                    for (int i = 1; i < queenMovement.length; i++) {
                        validMovement.add(queenMovement[i]);
                        movTxt += " ,{"+queenMovement[i][0]+","+queenMovement[i][1]+"}";
                    }
                    for (int i = 1; i < queenAttack.length; i++) {
                        validAttack.add(queenAttack[i]);
                        attackTxt += " ,{"+queenAttack[i][0]+","+queenAttack[i][1]+"}";
                    }
                    infiniteAttackCheckBox.setChecked(true);
                    infiniteMovementCheckBox.setChecked(true);
                    canAttackOverCheckBox.setChecked(false);
                    canMoveOverCheckBox.setChecked(false);

                    movementValueTextView.setText( movTxt);
                    attackValueTextView.setText(attackTxt);

                }
                else if (selectedSoldier instanceof King){
                    validAttack.add(kingAttack[0]);
                    validMovement.add(kingMovement[0]);

                    String movTxt = "{"+kingMovement[0][0]+","+kingMovement[0][1]+"}";
                    String attackTxt = "{"+kingAttack[0][0]+","+kingAttack[0][1]+"}";


                    for (int i = 1; i < kingMovement.length; i++) {
                        validMovement.add(kingMovement[i]);
                        movTxt += " ,{"+kingMovement[i][0]+","+kingMovement[i][1]+"}";
                    }
                    for (int i = 1; i < kingAttack.length; i++) {
                        validAttack.add(kingAttack[i]);
                        attackTxt += " ,{"+kingAttack[i][0]+","+kingAttack[i][1]+"}";
                    }
                    infiniteAttackCheckBox.setChecked(false);
                    infiniteMovementCheckBox.setChecked(false);
                    canAttackOverCheckBox.setChecked(false);
                    canMoveOverCheckBox.setChecked(false);

                    movementValueTextView.setText( movTxt);
                    attackValueTextView.setText(attackTxt);

                }
                finishBtn.callOnClick();
                selectedSoldier = null;

            }
        });

    }

    //assign onclick to the board tile
    public void setBoardTileMatrixOnClick(){
        for (int i = 0; i < boardTileMatrix.length; i++) {
            for (int j = 0; j < boardTileMatrix[i].length; j++) {
                int i1 = i;
                int j1 = j;
                boardTileMatrix[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // handle click event for boardTile

                        if (boardTileMatrix[i1][j1].currentSoldier != null) return;

                        if ( boardTileMatrix[i1][j1].highlight)
                        {
                            //change the tile to "not selected"
                            boardTileMatrix[i1][j1].setHighlight(false);
                            selectedBoardTile = null;
                        }
                        else if(selectedBoardTile == null)
                        {
                            boardTileMatrix[i1][j1].setHighlight(true);
                            selectedBoardTile = boardTileMatrix[i1][j1];
                        }
                        boardTileMatrix[i1][j1].invalidate();

                    }
                });
            }
        }

    }

    //create a linearlayout with the images of the used soldiers
    public void addImageViewsToLinearLayout() {
        int[] tempType = new int[type.length];
        for (int i = 0; i < type.length; i++) {
            tempType[i] =  type[i];
        }

        for (int i = 0; i < tempType.length; i++) {
            int currentType = tempType[i];
            for (int j = 0; j < tempType.length; j++) {
                if (i!=j && tempType[i]==tempType[j])
                    tempType[j] = -1;
            }
        }


        for (int i = 0; i < tempType.length; i++) {
            ImageView imageView = new ImageView(this);
            if (tempType[i] == -1)continue;
            //imageView.setImageResource(R.drawable.your_image_resource);

            if(tempType[i]==0)
            {
                imageView.setImageResource(R.drawable.wpawn);
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                soldiers.add(new Pawn(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false));
            }
            else if(tempType[i]==1)
            {
                imageView.setImageResource(R.drawable.wrook);
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                soldiers.add(new Rook(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false));
            }
            else if(tempType[i]==2)
            {
                imageView.setImageResource(R.drawable.wknight);
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                soldiers.add(new Knight(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false));
            }
            else if(tempType[i]==3)
            {
                imageView.setImageResource(R.drawable.wbishop);
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                soldiers.add(new Bishop(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false));
            }
            else if(tempType[i]==4)
            {
                imageView.setImageResource(R.drawable.wqueen);
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                soldiers.add(new Queen(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false));
            }
            else if(tempType[i]==5)
            {
                imageView.setImageResource(R.drawable.wking);
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                soldiers.add(new King(bitmap,  true, new int[]{0,0},  new int[][]{{0,0}}, false,new int[][]{{0,0}}, false,false,false));
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    100,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 0, 0);

            imageView.setLayoutParams(layoutParams);

            imageViewList.add(imageView);
            whiteSoldiers.addView(imageView);


            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!validMovement.isEmpty()|| !validAttack.isEmpty()){
                        Toast.makeText(getApplicationContext(), "finish setting current soldier", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (int j = 0; j < imageViewList.size(); j++) {
                        if (imageViewList.get(j)!=view)
                            imageViewList.get(j).setBackground(null);
                        else
                        {
                            imageViewList.get(j).setBackgroundColor(Color.parseColor("#0073FF"));
                            selectedSoldier = soldiers.get(j);

                            if (selectedBoardTile !=null)
                            {
                                selectedBoardTile.setHighlight(false);
                                selectedBoardTile.invalidate();
                                selectedBoardTile = null;
                            }
                            boardTileMatrix[row/2][col/2].currentSoldier = selectedSoldier;
                            boardTileMatrix[row/2][col/2].invalidate();
                        }
                    }
                    attackValueTextView.setText("");
                    movementValueTextView.setText("");
                    validAttack.clear();
                    validMovement.clear();
                }
            });

        }
    }


}