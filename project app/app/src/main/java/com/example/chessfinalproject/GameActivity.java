package com.example.chessfinalproject;

import static com.example.chessfinalproject.LoginActivity.bitmapsBlackPieces;
import static com.example.chessfinalproject.LoginActivity.bitmapsWhitePieces;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GameActivity {
    private boolean isWhiteTurn = true;//white always starts
    int[] blackTime = new int[2];
    int[] whiteTime = new int[2];
    TextView playerOneTime;
    TextView playerTwoTime;
    boolean pauseGame = false;
    Context context;
    ImageView imageView;
    boolean whiteKingChecked = false;
    boolean blackKingChecked = false;
    ImageView showNames;
    LinearLayout boardLayout;
    BoardTile[][] boardTileMatrix;
    ArrayList<Soldier> soldiers;
    ArrayList<Soldier> constSoldiers = new ArrayList<>();
    BoardTile selectedBoardTile;
    Soldier whiteKing, blackKing;
    Soldier checkMateorToBlackKing,checkMateorToWhiteKing ;// the piece which threaten the king
    float rotationCounter = 0;
    int turnsCounter = 1;
    private Toast toast;
    int changeImageCounter = 0;
    public GameActivity() {

    }

    //create board and set on clicks
    public void createCustomBoard(Context context, int cols, int rows, int colorOne, int colorTwo, ArrayList<Soldier> soldiers, LinearLayout boardLayout){
        Board board = new Board(context, cols, rows, colorOne,colorTwo, soldiers);
        boardLayout.addView(board);
        boardTileMatrix = board.boardTileMatrix;
        this.soldiers = board.soldiers;
        this.boardLayout = boardLayout;
        for (int i = 0; i < soldiers.size(); i++) {
            if (soldiers.get(i) instanceof King ){
                if(soldiers.get(i).whitePiece)
                    whiteKing = soldiers.get(i);
                else
                    blackKing = soldiers.get(i);
            }
        }

        for (int i = 0; i < soldiers.size(); i++) {
            constSoldiers.add(createNewSoldier(soldiers.get(i)));
        }
        createMove();

    }

    //set on click to the tiles
    public void createMove(){
        for (int i = 0; i < boardTileMatrix.length; i++) {
            for (int j = 0; j < boardTileMatrix[i].length; j++) {


                boardTileMatrix[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BoardTile boardTile = (BoardTile) view;
                        if ((selectedBoardTile == null && boardTile.currentSoldier ==null) || pauseGame)return;

                        if (selectedBoardTile == null){//first select Tile
                            if (!boardTile.currentSoldier.whitePiece==isWhiteTurn)
                                return;
                            selectedBoardTile = boardTile;

                            //show all moves
                             selectedBoardTile.currentSoldier.highlight(boardTileMatrix,true);
                            return;
                        }
                        else if (boardTile!=selectedBoardTile && boardTile.currentSoldier ==null && selectedBoardTile.currentSoldier != null &&selectedBoardTile.currentSoldier.IsValidMovement(boardTile, boardTileMatrix) || enPassant(boardTile, selectedBoardTile)){

                            //check castling
                            int castling = 0; // 0 no castling, 1 white small, 2 white big, 3 black small, 4 black big
                            if (selectedBoardTile.currentSoldier instanceof King && ((King) selectedBoardTile.currentSoldier).castling(boardTile, boardTileMatrix)) {
                                if ((selectedBoardTile.currentSoldier.whitePiece && whiteKingChecked) || (!selectedBoardTile.currentSoldier.whitePiece && blackKingChecked)) {
                                    selectedBoardTile = null;
                                    return;
                                }

                                //check castling type
                                if (boardTile == boardTileMatrix[0][2] && boardTileMatrix[0][0].currentSoldier!=null )
                                {
                                    if (boardTileMatrix[0][0].currentSoldier instanceof Rook && !((Rook) boardTileMatrix[0][0].currentSoldier).moved)
                                        castling = 4;
                                }
                                else if (boardTile == boardTileMatrix[0][6]&& boardTileMatrix[0][7].currentSoldier!=null) {
                                    if (boardTileMatrix[0][7].currentSoldier instanceof Rook && !((Rook) boardTileMatrix[0][7].currentSoldier).moved)
                                        castling = 3;
                                }
                                else if (boardTile == boardTileMatrix[7][2]&& boardTileMatrix[7][0].currentSoldier!=null) {
                                    if (boardTileMatrix[7][0].currentSoldier instanceof Rook && !((Rook) boardTileMatrix[7][0].currentSoldier).moved)
                                        castling = 2;
                                }
                                else if (boardTile == boardTileMatrix[7][6]&& boardTileMatrix[7][7].currentSoldier!=null) {
                                    if (boardTileMatrix[7][7].currentSoldier instanceof Rook && !((Rook) boardTileMatrix[7][7].currentSoldier).moved)
                                        castling = 1;
                                }

                                if (castling==0){
                                    selectedBoardTile = null;
                                    return;
                                }

                            }



                            //remove the highlighting
                            selectedBoardTile.currentSoldier.highlight(boardTileMatrix,false);

                            Pawn enPassantPawn = null;
                            if (enPassant(boardTile,selectedBoardTile)){
                                enPassantPawn = (Pawn) boardTileMatrix[selectedBoardTile.row][boardTile.col].currentSoldier;
                                soldiers.remove(enPassantPawn);
                                boardTileMatrix[enPassantPawn.currentPosition[0]][enPassantPawn.currentPosition[1]].currentSoldier = null;
                                boardTileMatrix[enPassantPawn.currentPosition[0]][enPassantPawn.currentPosition[1]].invalidate();
                            }

                            boardTile.currentSoldier = selectedBoardTile.currentSoldier;
                            selectedBoardTile.currentSoldier = null;
                            boardTile.currentSoldier.currentPosition = new int[]{boardTile.row, boardTile.col};
                            boardTile.invalidate();
                            selectedBoardTile.invalidate();

                            if (kingClash()){
                                //cancel move
                                selectedBoardTile.currentSoldier = boardTile.currentSoldier ;
                                boardTile.currentSoldier = null;

                                selectedBoardTile.currentSoldier.currentPosition = new int[]{selectedBoardTile.row, selectedBoardTile.col};
                                boardTile.invalidate();
                                selectedBoardTile.invalidate();
                                boardTile = null;

                                if (selectedBoardTile.currentSoldier instanceof King && selectedBoardTile.currentSoldier.whitePiece){
                                    whiteKing = selectedBoardTile.currentSoldier;
                                }else if (selectedBoardTile.currentSoldier instanceof King) {
                                    blackKing = selectedBoardTile.currentSoldier;
                                }
                                selectedBoardTile = null;
                                return;
                            }

                            int pawnMoveDate = 0;
                            if(boardTile.currentSoldier instanceof Pawn)
                            {
                                pawnMoveDate = ((Pawn) boardTile.currentSoldier).moveDate;
                                ((Pawn) boardTile.currentSoldier).moveDate = turnsCounter;

                                if((selectedBoardTile.row == 1 && boardTile.row == 3 && !boardTile.currentSoldier.whitePiece)
                                        || (selectedBoardTile.row == Soldier.rows-2 && boardTile.row == Soldier.rows-4 && boardTile.currentSoldier.whitePiece)){

                                    ((Pawn) boardTile.currentSoldier).doubleMoved = true;
                                }
                            }

                            //move the rook in case of castling
                            if (castling ==1){
                                boardTileMatrix[7][5].currentSoldier =  boardTileMatrix[7][7].currentSoldier;
                                boardTileMatrix[7][7].currentSoldier = null;
                                boardTileMatrix[7][5].currentSoldier.currentPosition = new int[]{7, 5};

                                boardTileMatrix[7][5].invalidate();
                                boardTileMatrix[7][7].invalidate();
                            }
                            else if (castling ==2){
                                boardTileMatrix[7][3].currentSoldier =  boardTileMatrix[7][0].currentSoldier;
                                boardTileMatrix[7][0].currentSoldier = null;
                                boardTileMatrix[7][3].currentSoldier.currentPosition = new int[]{7, 3};

                                boardTileMatrix[7][3].invalidate();
                                boardTileMatrix[7][0].invalidate();
                            }
                            else if (castling ==3){
                                boardTileMatrix[0][5].currentSoldier =  boardTileMatrix[0][7].currentSoldier;
                                boardTileMatrix[0][7].currentSoldier = null;
                                boardTileMatrix[0][5].currentSoldier.currentPosition = new int[]{0, 5};

                                boardTileMatrix[0][5].invalidate();
                                boardTileMatrix[0][7].invalidate();
                            }
                            else if (castling ==4){
                                boardTileMatrix[0][3].currentSoldier =  boardTileMatrix[0][0].currentSoldier;
                                boardTileMatrix[0][0].currentSoldier = null;
                                boardTileMatrix[0][3].currentSoldier.currentPosition = new int[]{0, 3};

                                boardTileMatrix[0][3].invalidate();
                                boardTileMatrix[0][0].invalidate();
                            }



                            if ( boardTile.currentSoldier instanceof King && boardTile.currentSoldier.whitePiece){
                                whiteKing = boardTile.currentSoldier;
                            }else if (boardTile.currentSoldier instanceof King) {
                                blackKing = boardTile.currentSoldier;
                            }



                            //check if the move prevent check
                            isKingChecked2();

                            if ((whiteKingChecked && isWhiteTurn) || (blackKingChecked&&!isWhiteTurn) )
                            {
                                //cancel en Passant
                                if (enPassantPawn!=null){
                                    soldiers.add(enPassantPawn);
                                    boardTileMatrix[enPassantPawn.currentPosition[0]][enPassantPawn.currentPosition[1]].currentSoldier = enPassantPawn;
                                    boardTileMatrix[enPassantPawn.currentPosition[0]][enPassantPawn.currentPosition[1]].invalidate();
                                }


                                //cancel castling
                                if (castling ==1){
                                    boardTileMatrix[7][7].currentSoldier =  boardTileMatrix[7][5].currentSoldier;
                                    boardTileMatrix[7][5].currentSoldier = null;
                                    boardTileMatrix[7][7].currentSoldier.currentPosition = new int[]{7, 7};

                                    boardTileMatrix[7][5].invalidate();
                                    boardTileMatrix[7][7].invalidate();
                                }
                                else if (castling ==2){
                                    boardTileMatrix[7][0].currentSoldier =  boardTileMatrix[7][3].currentSoldier;
                                    boardTileMatrix[7][3].currentSoldier = null;
                                    boardTileMatrix[7][0].currentSoldier.currentPosition = new int[]{7, 0};

                                    boardTileMatrix[7][3].invalidate();
                                    boardTileMatrix[7][0].invalidate();
                                }
                                else if (castling ==3){
                                    boardTileMatrix[0][7].currentSoldier =  boardTileMatrix[0][5].currentSoldier;
                                    boardTileMatrix[0][5].currentSoldier = null;
                                    boardTileMatrix[0][7].currentSoldier.currentPosition = new int[]{0, 7};

                                    boardTileMatrix[0][5].invalidate();
                                    boardTileMatrix[0][7].invalidate();
                                }
                                else if (castling ==4){
                                    boardTileMatrix[0][0].currentSoldier =  boardTileMatrix[0][3].currentSoldier;
                                    boardTileMatrix[0][3].currentSoldier = null;
                                    boardTileMatrix[0][0].currentSoldier.currentPosition = new int[]{0, 0};

                                    boardTileMatrix[0][3].invalidate();
                                    boardTileMatrix[0][0].invalidate();
                                }

                                //cancel move
                                selectedBoardTile.currentSoldier = boardTile.currentSoldier ;
                                boardTile.currentSoldier = null;

                                selectedBoardTile.currentSoldier.currentPosition = new int[]{selectedBoardTile.row, selectedBoardTile.col};
                                boardTile.invalidate();
                                selectedBoardTile.invalidate();
                                boardTile = null;

                                if(selectedBoardTile.currentSoldier instanceof Pawn)
                                    ((Pawn) selectedBoardTile.currentSoldier).moveDate = pawnMoveDate;

                                if (selectedBoardTile.currentSoldier instanceof King && selectedBoardTile.currentSoldier.whitePiece){
                                    whiteKing = selectedBoardTile.currentSoldier;
                                }else if (selectedBoardTile.currentSoldier instanceof King) {
                                    blackKing = selectedBoardTile.currentSoldier;
                                }
                            }else {
                                selectedBoardTile = null;
                                endTurnFunction();
                            }

                        }
                        else if (boardTile!=selectedBoardTile && boardTile.currentSoldier !=null && selectedBoardTile.currentSoldier != null &&selectedBoardTile.currentSoldier.IsValidAttack(boardTile,boardTileMatrix)) {
                            // check attack to soldier tile

                            Soldier tempSoldier = boardTile.currentSoldier;

                            //remove the highlighting
                            selectedBoardTile.currentSoldier.highlight(boardTileMatrix,false);


                            soldiers.remove(boardTile.currentSoldier);
                            boardTile.currentSoldier = selectedBoardTile.currentSoldier;
                            selectedBoardTile.currentSoldier = null;
                            boardTile.currentSoldier.currentPosition = new int[]{boardTile.row, boardTile.col};
                            boardTile.invalidate();
                            selectedBoardTile.invalidate();

                            if ( boardTile.currentSoldier instanceof King && boardTile.currentSoldier.whitePiece){
                                whiteKing = boardTile.currentSoldier;
                            }else if (boardTile.currentSoldier instanceof King) {
                                blackKing = boardTile.currentSoldier;
                            }


                            if (kingClash()){
                                //cancel move
                                soldiers.add(tempSoldier);
                                selectedBoardTile.currentSoldier =  boardTile.currentSoldier;
                                boardTile.currentSoldier = tempSoldier;
                                boardTile.currentSoldier.currentPosition = new int[]{boardTile.row, boardTile.col};
                                boardTile.invalidate();
                                selectedBoardTile.invalidate();

                                if (selectedBoardTile.currentSoldier instanceof King && selectedBoardTile.currentSoldier.whitePiece){
                                    whiteKing = selectedBoardTile.currentSoldier;
                                }else if (selectedBoardTile.currentSoldier instanceof King) {
                                    blackKing = selectedBoardTile.currentSoldier;
                                }
                                selectedBoardTile = null;
                                return;
                            }

                            int pawnMoveDate = 0;
                            if(boardTile.currentSoldier instanceof Pawn)
                            {
                                pawnMoveDate = ((Pawn) boardTile.currentSoldier).moveDate;
                                ((Pawn) boardTile.currentSoldier).moveDate = turnsCounter;

                            }


                            //check if the move prevent check
                            isKingChecked2();

                            if ((whiteKingChecked && isWhiteTurn) || (blackKingChecked&&!isWhiteTurn))
                            {
                                soldiers.add(tempSoldier);
                                selectedBoardTile.currentSoldier =  boardTile.currentSoldier;
                                boardTile.currentSoldier = tempSoldier;
                                boardTile.currentSoldier.currentPosition = new int[]{boardTile.row, boardTile.col};
                                boardTile.invalidate();
                                selectedBoardTile.invalidate();

                                if (selectedBoardTile.currentSoldier instanceof King && selectedBoardTile.currentSoldier.whitePiece){
                                    whiteKing = selectedBoardTile.currentSoldier;
                                }else if (selectedBoardTile.currentSoldier instanceof King) {
                                    blackKing = selectedBoardTile.currentSoldier;
                                }

                                if(selectedBoardTile.currentSoldier instanceof Pawn)
                                {
                                    ((Pawn) selectedBoardTile.currentSoldier).moveDate = pawnMoveDate;
                                }

                            }else {
                                selectedBoardTile = null;
                                endTurnFunction();
                            }

                        }

                        //remove the highlighting
                        if (boardTile!=null)
                            if (boardTile.currentSoldier!=null)
                                boardTile.currentSoldier.highlight(boardTileMatrix,false);
                        if (selectedBoardTile!=null)
                            if (selectedBoardTile.currentSoldier!=null)
                                selectedBoardTile.currentSoldier.highlight(boardTileMatrix,false);

                        selectedBoardTile = null;

                    }
                });
            }
        }
    }

    //change turn, flip board...
    public void endTurnFunction(){
        boolean dialogOpened = false;
        for (int i = 0; i < boardTileMatrix.length; i++) {
            for (int j = 0; j < boardTileMatrix[i].length; j++) {
                if(boardTileMatrix[i][j].currentSoldier != null){
                    if ( boardTileMatrix[i][j].currentSoldier.whitePiece &&  boardTileMatrix[i][j].currentSoldier instanceof King && whiteKingChecked)
                        boardTileMatrix[i][j].setRedAura(true);
                    else if ( !boardTileMatrix[i][j].currentSoldier.whitePiece &&  boardTileMatrix[i][j].currentSoldier instanceof King && blackKingChecked)
                        boardTileMatrix[i][j].setRedAura(true);
                    else
                        boardTileMatrix[i][j].setRedAura(false);

                    boardTileMatrix[i][j].invalidate();
                }

                //checks if the soldier reached the end of the board
                if(boardTileMatrix[i][j].currentSoldier != null){
                    if ( boardTileMatrix[i][j].currentSoldier.whitePiece &&  boardTileMatrix[i][j].currentSoldier instanceof Pawn && i ==  0)
                    {
                        //change soldier type
                        changeSoldierDialog(boardTileMatrix[i][j],true);
                        dialogOpened = true;
                    }
                    else if ( !boardTileMatrix[i][j].currentSoldier.whitePiece &&  boardTileMatrix[i][j].currentSoldier instanceof Pawn && i == boardTileMatrix.length-1)
                    {
                        //change soldier type
                        changeSoldierDialog(boardTileMatrix[i][j],false);
                        dialogOpened = true;
                    }

                }

            }
        }

        if (!dialogOpened){
            //to flip the board use this func
            flipBoard();


            turnsCounter++;
            changeTurnAnimation();
            isWhiteTurn = ! isWhiteTurn;
            draw3();
        }


    }

    //looks for draw. in case of draw end game
    public void draw3(){
        //if there are only two soldier (Kings)
        if (soldiers.size() ==2){
            //current player won, implement end game
            pauseGame = true;
            showToast("draw");
            endGameDialog(3);
        }
    }

    //pop a dialog that says "end game"
    public void endGameDialog(int situation){

        String winner;
        if (situation == 1)
            winner = "White won";
        else if (situation ==2)
            winner = "Black won";
        else
            winner = "draw";

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Game over")
                .setMessage(winner)
                .setCancelable(false)
                .setPositiveButton("Return home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open an intent here
                        Intent intent = new Intent(context, homeActivity.class);
                        context.startActivity(intent);
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        // Get the dialog window
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            // Set the background drawable
            dialogWindow.setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);
        }
        dialog.show();

    }

    //replace pawnTile with the selected Soldier
    public void changeSoldierDialog(BoardTile pawnTile, boolean whitePiece ){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select a piece type");
        builder.setCancelable(false);


// Create a spinner
        Spinner spinner = new Spinner(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item);
        adapter.add("Rook");
        adapter.add("Bishop");
        adapter.add("Knight");
        adapter.add("Queen");
        spinner.setAdapter(adapter);

// Set the spinner in the dialog
        builder.setView(spinner);

// Set positive button and its click listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the selected item from the spinner
                String selectedItem = spinner.getSelectedItem().toString();
                int[][] validMovement = new int[0][];
                int[][] validAttack = new int[0][];


                Soldier newSoldier = null;
                boolean changeToDefault = true;

                for (int i = 0; i < constSoldiers.size(); i++) {
                    if (selectedItem.equals("Rook")&& constSoldiers.get(i) instanceof  Rook && constSoldiers.get(i).whitePiece == whitePiece){
                        newSoldier = createNewSoldier(constSoldiers.get(i));
                        changeToDefault = false;
                    }
                    else if (selectedItem.equals("Bishop")&& constSoldiers.get(i) instanceof  Bishop && constSoldiers.get(i).whitePiece == whitePiece){
                        newSoldier = createNewSoldier(constSoldiers.get(i));
                        changeToDefault = false;
                    }
                    else if (selectedItem.equals("Knight")&& constSoldiers.get(i) instanceof  Knight && constSoldiers.get(i).whitePiece == whitePiece){
                        newSoldier = createNewSoldier(constSoldiers.get(i));
                        changeToDefault = false;
                    }
                    else if (selectedItem.equals("Queen")&& constSoldiers.get(i) instanceof  Queen && constSoldiers.get(i).whitePiece == whitePiece){
                        newSoldier = createNewSoldier(constSoldiers.get(i));
                        changeToDefault = false;
                    }
                }

                if (changeToDefault){
                    if (selectedItem.equals("Rook")){
                        validMovement = new int[][]{{1,0},{0,1},{-1,0},{0,-1}};
                        validAttack   = new int[][]{{1,0},{0,1},{-1,0},{0,-1}};
                        if (whitePiece){
                            newSoldier = new Rook(bitmapsWhitePieces[1],true, null,validMovement, true,validAttack, false,false,true);
                        }else {
                            newSoldier = new Rook(bitmapsBlackPieces[1],false, null,validMovement,true,validAttack, false,false,true);
                        }

                    }
                    else if (selectedItem.equals("Bishop")){
                        validMovement = new int[][]{ {1,1},{1,-1},{-1,1},{-1,-1} };
                        validAttack = new int[][]{ {1,1},{1,-1},{-1,1},{-1,-1} };

                        if (whitePiece){
                            newSoldier = new Bishop(bitmapsWhitePieces[3],true, null,validMovement, true,validAttack, false,false,true);
                        }else {
                            newSoldier = new Bishop(bitmapsBlackPieces[3],false, null,validMovement,true,validAttack, false,false,true);
                        }
                    }
                    else if (selectedItem.equals("Knight")){
                        validMovement= new int[][]{{-1,-2},{-1,2},{1,-2},{1,2}, {-2,-1},{-2,1},{2,-1},{2,1}};
                        validAttack =new int[][]{{-1,-2},{-1,2},{1,-2},{1,2}, {-2,-1},{-2,1},{2,-1},{2,1}};

                        if (whitePiece){
                            newSoldier = new Knight(bitmapsWhitePieces[2],true, null,validMovement, false,validAttack, true,true,false);
                        }else {
                            newSoldier = new Knight(bitmapsBlackPieces[2],false, null,validMovement,false,validAttack, true,true,false);
                        }
                    }
                    else {
                         validMovement = new int[][]{{1,0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}    };
                         validAttack = new int[][]{{1,0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}    };

                        if (whitePiece){
                            newSoldier = new Queen(bitmapsWhitePieces[4],true, null,validMovement, true,validAttack, false,false,true);
                        }else {
                            newSoldier = new Queen(bitmapsBlackPieces[4],false, null,validMovement,true,validAttack, false,false,true);
                        }
                    }
                }

                Bitmap bitmap;
               if (newSoldier instanceof Queen){
                    if (whitePiece)
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wqueen);
                    else
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bqueen);
                }
                else if (newSoldier instanceof Bishop){
                    if (whitePiece)
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wbishop);
                    else
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bbishop);
                }
                else if (newSoldier instanceof Knight){
                    if (whitePiece)
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wknight);
                    else
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bknight);
                }
                else{
                    if (whitePiece)
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wrook);
                    else
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.brook);
                }


                newSoldier.diffaultImageBitmap =bitmap;


                //set newSoldier position
                soldiers.remove( pawnTile.currentSoldier);
                newSoldier.currentPosition = new int[]{pawnTile.row,pawnTile.col};
                pawnTile.currentSoldier = newSoldier;
                soldiers.add(newSoldier);
                pawnTile.invalidate();


                //to flip the board use this func
                flipBoard();

                turnsCounter++;
                changeTurnAnimation();
                isWhiteTurn = ! isWhiteTurn;
                draw3();
            }


        });
        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Get the dialog window
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            // Set the background drawable
            dialogWindow.setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);
        }

        dialog.show();

    }

    //check if boardTile is threaten and return if so
    public boolean canBeAttacked(BoardTile boardTile ,boolean isWhite){
        //create a temp soldier if its equals to null
        boolean isTempSoldier = false;
        if (boardTile.currentSoldier == null)
        {
            boardTile.currentSoldier = new Pawn(null, isWhite, null, null, false, null, false, false, false);
            isTempSoldier = true;
        }

        for (int i = 0; i < soldiers.size(); i++) {
            if (soldiers.get(i).IsValidAttack(boardTile, boardTileMatrix)) {

                //in case of king being checked save his attacker
                if ((whiteKing.currentPosition[0] == boardTile.row && whiteKing.currentPosition[1] == boardTile.col)) {
                    checkMateorToWhiteKing = soldiers.get(i);
                } else if (blackKing.currentPosition[0] == boardTile.row && blackKing.currentPosition[1] == boardTile.col) {
                    checkMateorToBlackKing = soldiers.get(i);
                }
                if (isTempSoldier)
                    boardTile.currentSoldier = null;
                return true;
            }
        }
        if (isTempSoldier)
            boardTile.currentSoldier = null;
        return false;
    }

    //search for check and update whiteKingChecked and blackKingChecked
    public void isKingChecked2(){
        BoardTile whiteKingTile = boardTileMatrix[whiteKing.currentPosition[0]][whiteKing.currentPosition[1]];
        BoardTile blackKingTile = boardTileMatrix[blackKing.currentPosition[0]][blackKing.currentPosition[1]];

        if (canBeAttacked(whiteKingTile,true)){

            //white king is checked
            whiteKingChecked = true;

            //check for mate
            checkMate(true);
        }else {
            //white king is not checked
            whiteKingChecked = false;
        }

        if (canBeAttacked(blackKingTile,false)){

            //black king is checked
            blackKingChecked = true;

            //check for mate
            checkMate(false);
        }else {
            //black king is not checked
            blackKingChecked = false;
        }

    }

    //check if the king can attack/escape. check if the checkMateor can be attacked or blocked. in case of checkmate end the game
    public void checkMate(boolean isWhite){
        //save checkMateor and king in a new variable
        Soldier attacker,king;
        if (isWhite)
        {
            king = createNewSoldier(whiteKing);
            attacker = createNewSoldier(checkMateorToWhiteKing);
        }
        else
        {
            king = createNewSoldier(blackKing);
            attacker = createNewSoldier(checkMateorToBlackKing);
        }


        //check if the king can escape/attack and if the attacker can be attacked
        for (int i = 0; i < boardTileMatrix.length; i++) {
            for (int j = 0; j < boardTileMatrix[i].length; j++) {

                if (boardTileMatrix[i][j].currentSoldier !=null){

                    //check king's attack
                    if (king.IsValidAttack(boardTileMatrix[i][j],boardTileMatrix)) {
                        Soldier tempSoldier = createNewSoldier(boardTileMatrix[i][j].currentSoldier);
                        King tempKing = (King) createNewSoldier(king);
                        soldiers.remove(boardTileMatrix[i][j].currentSoldier);
                        boardTileMatrix[i][j].currentSoldier = king;
                        boardTileMatrix[king.currentPosition[0]][king.currentPosition[1]].currentSoldier = null;
                        boardTileMatrix[i][j].currentSoldier.currentPosition = new int[]{i, j};

                        if (!canBeAttacked(boardTileMatrix[i][j], isWhite)) {
                            //the Attack is legal and the king is safe there - there is no checkmate
                            soldiers.add(tempSoldier);
                            boardTileMatrix[i][j].currentSoldier = tempSoldier;
                            boardTileMatrix[tempKing.currentPosition[0]][tempKing.currentPosition[1]].currentSoldier = tempKing;

                            showToast("Can attack");
                            return;
                        }
                        soldiers.add(tempSoldier);
                        boardTileMatrix[i][j].currentSoldier = tempSoldier;
                        boardTileMatrix[tempKing.currentPosition[0]][tempKing.currentPosition[1]].currentSoldier = tempKing;
                    }


                    //check if some soldier can attack the mateor
                    if (!(boardTileMatrix[i][j].currentSoldier instanceof King) &&boardTileMatrix[i][j].currentSoldier.IsValidAttack(boardTileMatrix[attacker.currentPosition[0]][attacker.currentPosition[1]],boardTileMatrix)) {

                        //check if the king is safe if the attacker is eaten.
                        Soldier tempSoldier = createNewSoldier(boardTileMatrix[attacker.currentPosition[0]][attacker.currentPosition[1]].currentSoldier);
                        soldiers.remove(boardTileMatrix[attacker.currentPosition[0]][attacker.currentPosition[1]].currentSoldier);
                        boardTileMatrix[attacker.currentPosition[0]][attacker.currentPosition[1]].currentSoldier = boardTileMatrix[i][j].currentSoldier;
                        boardTileMatrix[i][j].currentSoldier = null;
                        boardTileMatrix[attacker.currentPosition[0]][attacker.currentPosition[1]].currentSoldier.currentPosition = new int[]{attacker.currentPosition[0], attacker.currentPosition[1]};

                        if (!canBeAttacked(boardTileMatrix[king.currentPosition[0]][king.currentPosition[1]], isWhite)) {
                            //the Attack is legal and the king is safe  - there is no checkmate
                            soldiers.add(tempSoldier);
                            boardTileMatrix[i][j].currentSoldier =  boardTileMatrix[attacker.currentPosition[0]][attacker.currentPosition[1]].currentSoldier;
                            boardTileMatrix[attacker.currentPosition[0]][attacker.currentPosition[1]].currentSoldier = tempSoldier;
                            boardTileMatrix[i][j].currentSoldier.currentPosition = new int[]{i, j};

                            showToast("attacker can be attacked");
                            return;
                        }
                        soldiers.add(tempSoldier);
                        boardTileMatrix[i][j].currentSoldier =  boardTileMatrix[attacker.currentPosition[0]][attacker.currentPosition[1]].currentSoldier;
                        boardTileMatrix[attacker.currentPosition[0]][attacker.currentPosition[1]].currentSoldier = tempSoldier;
                        boardTileMatrix[i][j].currentSoldier.currentPosition = new int[]{i, j};

                    }

                }
                else {
                    //check king's escape
                    if (king.IsValidMovement(boardTileMatrix[i][j],boardTileMatrix) &&!canBeAttacked(boardTileMatrix[i][j],isWhite)){
                        //the movement is legal and the king is safe there - there is no checkmate
                        showToast("Can escape");
                        return;
                    }
                }
            }
        }


        //check if someone can block the attacker path. if the attacker can attacker over and does not has infinite attack - skip.
        if (attacker.infiniteAttack && !attacker.canAttackOver) {
            BoardTile kingTile = boardTileMatrix[king.currentPosition[0]][king.currentPosition[1]];
            BoardTile attackerTile = boardTileMatrix[attacker.currentPosition[0]][attacker.currentPosition[1]];

            int[] usedAttack = getPath(attackerTile, kingTile);
            if (usedAttack == null){
                //implement game over
                showToast("checkmate");
                pauseGame = true;
                endGameDialog(isWhite ? 2:1);
                return;
            }
            int i = attackerTile.row;
            int j = attackerTile.col;
            while (i != kingTile.row && j != kingTile.col) {
                i += usedAttack[0];
                j += usedAttack[1];

                for (int k = 0; k < soldiers.size(); k++) {
                    Soldier currentSoldier = soldiers.get(k);
                    if (currentSoldier != null&& !(currentSoldier instanceof King) && currentSoldier.whitePiece == king.whitePiece && currentSoldier.IsValidMovement(boardTileMatrix[i][j], boardTileMatrix)) {
                        int row = currentSoldier.currentPosition[0];
                        int col = currentSoldier.currentPosition[1];


                        boardTileMatrix[i][j].currentSoldier = currentSoldier;
                        boardTileMatrix[row][col].currentSoldier = null;
                        boardTileMatrix[i][j].currentSoldier.currentPosition = new int[]{i, j};

                        //check if the king will be safe in the new position
                        if (!canBeAttacked(boardTileMatrix[king.currentPosition[0]][king.currentPosition[1]], isWhite)) {
                            //the Attack is legal and the king is safe  - there is no checkmate

                            boardTileMatrix[row][col].currentSoldier = currentSoldier;
                            boardTileMatrix[i][j].currentSoldier = null;
                            boardTileMatrix[row][col].currentSoldier.currentPosition = new int[]{row, col};

                            showToast("attacker can be blocked ("+row+","+col+")");
                            return;
                        }

                        //return to the previous board

                        boardTileMatrix[row][col].currentSoldier = currentSoldier;
                        boardTileMatrix[i][j].currentSoldier = null;
                        boardTileMatrix[row][col].currentSoldier.currentPosition = new int[]{row, col};

                    }
                }
            }
        }

        if ((isWhite && isWhiteTurn) || (!isWhite && !isWhiteTurn)) return;
        //implement game over
        showToast("checkmate");
        endGameDialog(isWhite ? 2:1);
        pauseGame = true;
    }

    //return the path used for attackerTile to attack attackedTile
    public int[] getPath(BoardTile attackerTile , BoardTile attackedTile){

        Soldier attackerSoldier = attackerTile.currentSoldier;
        if(attackerSoldier == null)return null;
        boolean runOver = false;
        for (int i = 0; i < attackerSoldier.validAttack.length; i++) {
            runOver = false;
            int[] currentAttack = attackerSoldier.validAttack[i];
            int row = attackerSoldier.currentPosition[0];
            int col = attackerSoldier.currentPosition[1];
            if (attackerSoldier.infiniteAttack) {
                row+= currentAttack[0];
                col+= currentAttack[1];
                while (row<boardTileMatrix.length && col< boardTileMatrix[i].length&& row>=0 && col>=0 ) {

                    //if the soldier ran over another soldier in the this movement
                    if (boardTileMatrix[row][col].currentSoldier!= null && boardTileMatrix[row][col] != attackedTile ){
                        runOver = true;
                    }
                    //if the current tile is in this current movement
                    else if(boardTileMatrix[row][col] == attackedTile && boardTileMatrix[row][col].currentSoldier!= null ){
                        if (attackedTile.currentSoldier.whitePiece != attackerSoldier.whitePiece && (attackerSoldier.canAttackOver || !runOver)) {
                            return currentAttack;
                        }
                    }
                    row += currentAttack[0];
                    col += currentAttack[1];

                }

            }else {
                //check if the movement is valid
                if (row + currentAttack[0] == attackedTile.row && col + currentAttack[1] == attackedTile.col && attackedTile.currentSoldier.whitePiece != attackerSoldier.whitePiece)
                    return currentAttack;
            }
        }
        return null;
    }

    //duplicate mateor and return the new Soldier
    public Soldier createNewSoldier(Soldier mateor){
        if (mateor instanceof King){
            return new King(mateor.imageBitmap, mateor.whitePiece, mateor.currentPosition, mateor.validMovement, mateor.infiniteMovement, mateor.validAttack, mateor.canRunOver, mateor.canAttackOver, mateor.infiniteAttack);
        }
        else if (mateor instanceof Queen){
            return new Queen(mateor.imageBitmap, mateor.whitePiece, mateor.currentPosition, mateor.validMovement, mateor.infiniteMovement, mateor.validAttack, mateor.canRunOver, mateor.canAttackOver, mateor.infiniteAttack);
        }
        else if (mateor instanceof Bishop){
            return new Bishop(mateor.imageBitmap, mateor.whitePiece, mateor.currentPosition, mateor.validMovement, mateor.infiniteMovement, mateor.validAttack, mateor.canRunOver, mateor.canAttackOver, mateor.infiniteAttack);
        }
        else if (mateor instanceof Knight){
            return new Knight(mateor.imageBitmap, mateor.whitePiece, mateor.currentPosition, mateor.validMovement, mateor.infiniteMovement, mateor.validAttack, mateor.canRunOver, mateor.canAttackOver, mateor.infiniteAttack);
        }
        else if (mateor instanceof Rook){
            return new Rook(mateor.imageBitmap, mateor.whitePiece, mateor.currentPosition, mateor.validMovement, mateor.infiniteMovement, mateor.validAttack, mateor.canRunOver, mateor.canAttackOver, mateor.infiniteAttack);
        }
        else {
            return new Pawn(mateor.imageBitmap, mateor.whitePiece, mateor.currentPosition, mateor.validMovement, mateor.infiniteMovement, mateor.validAttack, mateor.canRunOver, mateor.canAttackOver, mateor.infiniteAttack);
        }
    }

    //flips the board
    public void flipBoard(){
        //flip the board
        if (rotationCounter%2==0)
            boardLayout.setRotation(180f);
        else
            boardLayout.setRotation(0);
        rotationCounter++;

        //flip each soldier
        for (int i = 0; i < boardTileMatrix.length; i++) {
            for (int j = 0; j < boardTileMatrix[i].length; j++) {
                if (boardTileMatrix[i][j].currentSoldier == null) continue;
                Bitmap originalBitmap = boardTileMatrix[i][j].currentSoldier.imageBitmap;
                Matrix matrix = new Matrix();
                matrix.postScale(-1, 1, originalBitmap.getWidth() / 2f, originalBitmap.getHeight() / 2f);
                matrix.postRotate(180, originalBitmap.getWidth() / 2f, originalBitmap.getHeight() / 2f);
                boardTileMatrix[i][j].currentSoldier.imageBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);


                Bitmap originalBitmap2 = boardTileMatrix[i][j].currentSoldier.diffaultImageBitmap;
                Matrix matrix2 = new Matrix();
                matrix2.postScale(-1, 1, originalBitmap2.getWidth() / 2f, originalBitmap2.getHeight() / 2f);
                matrix2.postRotate(180, originalBitmap2.getWidth() / 2f, originalBitmap2.getHeight() / 2f);
                boardTileMatrix[i][j].currentSoldier.diffaultImageBitmap = Bitmap.createBitmap(originalBitmap2, 0, 0, originalBitmap2.getWidth(), originalBitmap2.getHeight(), matrix2, true);

                boardTileMatrix[i][j].invalidate();
            }
        }


    }

    //creates the rook animation
    public void changeTurnAnimation() {

        AnimatorSet set = new AnimatorSet();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);



        ObjectAnimator animator3 = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        animator3.setDuration(1500);

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f);
        animator2.setDuration(750);

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f);
        animator1.setDuration(750);

        Handler handler = new Handler();
        Thread thread = new Thread(()->{
            handler.post(()->{
            });
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                if (!isWhiteTurn)
                    imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.blackturnanim));
                else
                    imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.turnanim));
            });

        });

        set.play(animator2).with(animator3);
        set.play(animator1).after(animator2);

        if (!thread.isAlive())
            thread.start();
        set.start();
    }

    //creates the timer and its buttons
    public void createTimer(Context context,LinearLayout layout,int[] blackTime ,int[] whiteTime,int addTime,int reduceTime){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.timer, null);
        ImageView timerImage = view.findViewById(R.id.timerImage);
        playerOneTime = view.findViewById(R.id.playerOne);
        playerTwoTime = view.findViewById(R.id.playerTwo);
        this.blackTime = blackTime;
        this.whiteTime = whiteTime;
        Button pauseResumeButton = view.findViewById(R.id.pauseButton);
        Button addTimeButton = view.findViewById(R.id.plusButton);
        Button reduceTimeButton = view.findViewById(R.id.minusButton);

        String blackMin = String.valueOf(blackTime[0]) , blackSec = String.valueOf(blackTime[1]);
        String whiteMin = String.valueOf(whiteTime[0]) , whiteSec = String.valueOf(whiteTime[1]);
        if (blackTime[0]<10)
            blackMin = "0"+blackMin;
        if (blackTime[1]<10)
            blackSec = "0"+blackSec;
        if (whiteTime[0]<10)
            whiteMin = "0"+whiteMin;
        if (whiteTime[1]<10)
            whiteSec = "0"+whiteSec;

        playerTwoTime.setText( blackMin+":"+blackSec);
        playerOneTime.setText( whiteMin+":"+whiteSec);

        pauseResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseGame = !pauseGame;
            }
        });

        reduceTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pauseGame)return;
                if (isWhiteTurn){
                    if (addTime<59)
                        whiteTime[1]-=reduceTime;
                    else {
                        whiteTime[1]-=reduceTime%60;
                        whiteTime[0]-=reduceTime/60;
                    }
                    if (whiteTime[1]<0){
                        whiteTime[0]--;
                        whiteTime[1] = 60+whiteTime[1];
                    }
                    String min,sec;

                    if (whiteTime[0]<10)
                        min ="0" + String.valueOf(whiteTime[0]);
                    else
                        min =String.valueOf(whiteTime[0]);
                    if (whiteTime[1]<10)
                        sec ="0" + String.valueOf(whiteTime[1]);
                    else
                        sec =String.valueOf(whiteTime[1]);

                    String time =min+":"+sec;
                    playerOneTime.setText(time);
                }
                else {
                    if (addTime<59)
                        blackTime[1]-=reduceTime;
                    else {
                        blackTime[1]-=reduceTime%60;
                        blackTime[0]-=reduceTime/60;
                    }
                    if (blackTime[1]<0){
                        blackTime[0]--;
                        blackTime[1] = 60+blackTime[1];
                    }
                    String min,sec;

                    if (blackTime[0]<10)
                        min ="0" + String.valueOf(blackTime[0]);
                    else
                        min =String.valueOf(blackTime[0]);
                    if (blackTime[1]<10)
                        sec ="0" + String.valueOf(blackTime[1]);
                    else
                        sec =String.valueOf(blackTime[1]);


                    String time = min+":"+sec;
                    playerTwoTime.setText(time);
                }
                if(whiteTime[0]<0 || whiteTime[1]< 0){

                    playerOneTime.setText("00:00");
                    return;
                }else if (blackTime[0]< 0|| blackTime[1]< 0){

                    playerTwoTime.setText("00:00");
                    return;
                }
            }
        });

        addTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isWhiteTurn){
                    if (addTime<59)
                        whiteTime[1]+=addTime;
                    else {
                        whiteTime[1]+=addTime%60;
                        whiteTime[0]+=addTime/60;
                    }
                    if (whiteTime[1]>59){
                        whiteTime[0]++;
                        whiteTime[1] = whiteTime[1]-60;
                    }
                    String min,sec;

                    if (whiteTime[0]<10)
                        min ="0" + String.valueOf(whiteTime[0]);
                    else
                        min =String.valueOf(whiteTime[0]);
                    if (whiteTime[1]<10)
                        sec ="0" + String.valueOf(whiteTime[1]);
                    else
                        sec =String.valueOf(whiteTime[1]);

                    String time =min+":"+sec;
                    playerOneTime.setText(time);
                }
                else {
                    if (addTime<59)
                        blackTime[1]+=addTime;
                    else {
                        blackTime[1]+=addTime%60;
                        blackTime[0]+=addTime/60;
                    }
                    if (blackTime[1]>59){
                        blackTime[0]++;
                        blackTime[1] = blackTime[1]-60;
                    }
                    String min,sec;

                    if (blackTime[0]<10)
                        min ="0" + String.valueOf(blackTime[0]);
                    else
                        min =String.valueOf(blackTime[0]);
                    if (blackTime[1]<10)
                        sec ="0" + String.valueOf(blackTime[1]);
                    else
                        sec =String.valueOf(blackTime[1]);


                    String time = min+":"+sec;
                    playerTwoTime.setText(time);
                }
            }
        });


        Drawable drawable = timerImage.getDrawable();

        layout.addView(view);

        android.os.Handler handler = new Handler();
        Thread thread =  new Thread(()->{

            while (true){
                if (pauseGame){
                    if(whiteTime[0]<0 || whiteTime[1]< 0){
                        handler.post(()->{
                            playerOneTime.setText("00:00");
                        });
                    }else if (blackTime[0]< 0|| blackTime[1]< 0){
                        handler.post(()->{
                            playerTwoTime.setText("00:00");
                        });
                    }
                    continue;
                }

                if (isWhiteTurn){
                    whiteTime[1]--;
                    if (whiteTime[1]<0){
                        whiteTime[1]=59;
                        whiteTime[0]--;
                        if (whiteTime[0]<0){
                            pauseGame = true;
                            handler.post(()->{
                                endGameDialog(2);
                            });

                            return;
                        }
                    }
                    handler.post(()->{
                        timerImage.setImageDrawable(context.getResources().getDrawable(R.drawable.playerone));
                        String min,sec;

                        if (whiteTime[0]<10)
                            min ="0" + String.valueOf(whiteTime[0]);
                        else
                            min =String.valueOf(whiteTime[0]);
                        if (whiteTime[1]<10)
                            sec ="0" + String.valueOf(whiteTime[1]);
                        else
                            sec =String.valueOf(whiteTime[1]);

                        if (whiteTime[0]< 0|| whiteTime[1]< 0){
                            pauseGame = true;

                            addTimeButton.setOnClickListener(null);
                            reduceTimeButton.setOnClickListener(null);
                            pauseResumeButton.setOnClickListener(null);
                        }

                        String time =min+":"+sec;
                        playerOneTime.setText(time);
                    });
                }else {
                    blackTime[1]--;
                    if (blackTime[1] < 0) {
                        blackTime[1] = 59;
                        blackTime[0]--;
                        if (blackTime[0] < 0) {
                            pauseGame = true;
                            handler.post(()->{
                                endGameDialog(1);
                            });
                            return;
                        }
                    }
                    handler.post(()->{
                        timerImage.setImageDrawable(context.getResources().getDrawable(R.drawable.playertwo));
                        String min,sec;

                        if (blackTime[0]<10)
                            min ="0" + String.valueOf(blackTime[0]);
                        else
                            min =String.valueOf(blackTime[0]);
                        if (blackTime[1]<10)
                            sec ="0" + String.valueOf(blackTime[1]);
                        else
                            sec =String.valueOf(blackTime[1]);

                        if (blackTime[0]< 0|| blackTime[1]< 0){
                            pauseGame = true;

                            addTimeButton.setOnClickListener(null);
                            reduceTimeButton.setOnClickListener(null);
                            pauseResumeButton.setOnClickListener(null);


                        }


                        String time = min+":"+sec;
                        playerTwoTime.setText(time);
                    });
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    //set onclick to the eye btn
    public void setShowNames(ImageView showNames) {
        this.showNames = showNames;
        for (int i = 0; i < soldiers.size(); i++) {
            Bitmap bitmap;
            //= BitmapFactory.decodeResource(context.getResources(), R.drawable.my_drawable)
            if (soldiers.get(i) instanceof King){
                if (soldiers.get(i).whitePiece)
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wking);
                else
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bking);
            }
            else if (soldiers.get(i) instanceof Queen){
                if (soldiers.get(i).whitePiece)
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wqueen);
                else
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bqueen);
            }
            else if (soldiers.get(i) instanceof Bishop){
                if (soldiers.get(i).whitePiece)
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wbishop);
                else
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bbishop);
            }
            else if (soldiers.get(i) instanceof Knight){
                if (soldiers.get(i).whitePiece)
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wknight);
                else
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bknight);
            }
            else if (soldiers.get(i) instanceof Rook){
                if (soldiers.get(i).whitePiece)
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wrook);
                else
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.brook);
            }
            else {
                if (soldiers.get(i).whitePiece)
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wpawn);
                else
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bpawn);
            }

            soldiers.get(i).diffaultImageBitmap =bitmap;
        }

        this.showNames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //change images to diffault
                for (int i = 0; i < soldiers.size(); i++) {
                    Bitmap temp = soldiers.get(i).diffaultImageBitmap;
                    soldiers.get(i).diffaultImageBitmap = soldiers.get(i).imageBitmap;
                    soldiers.get(i).imageBitmap = temp;
                    boardTileMatrix[soldiers.get(i).currentPosition[0]][soldiers.get(i).currentPosition[1]].invalidate();
                }
                if (changeImageCounter%2 == 0)
                    showNames.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_visibility_off_24));
                else
                    showNames.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_remove_red_eye_24));
                changeImageCounter++;
            }
        });
    }

    //are kings one tile away from each other
    public boolean kingClash(){
        King blackKing = null;
        King whiteKing = null;
        for (int i = 0; i < soldiers.size(); i++) {
            if (soldiers.get(i) instanceof King){
                if (soldiers.get(i).whitePiece)
                    whiteKing = (King) soldiers.get(i);
                else
                    blackKing = (King) soldiers.get(i);
            }
        }

        int row = whiteKing.currentPosition[0];
        int col = whiteKing.currentPosition[1];


        int row2 = blackKing.currentPosition[0];
        int col2 = blackKing.currentPosition[1];

        if (row == row2 && (col == col2 -1 ||  col == col2 +1))//right or left
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
        else
            return false;
    }

    //check for an en passant
    public boolean enPassant(BoardTile boardTile, BoardTile selectedBoardTile){

        if (selectedBoardTile.currentSoldier instanceof Pawn && boardTile.currentSoldier==null && Soldier.rows>=5){
            if (selectedBoardTile.currentSoldier.whitePiece){
                //checks if the selected tile is suitable for en passant
                if (selectedBoardTile.row == 3 && boardTileMatrix[3][boardTile.col].currentSoldier instanceof Pawn){
                    Pawn pawn = (Pawn) boardTileMatrix[3][boardTile.col].currentSoldier;

                    //check if the pawn double moved and if he moved on the last turn
                    return pawn.doubleMoved && turnsCounter - pawn.moveDate == 1 && !pawn.whitePiece;
                }

            }
            else {
                //checks if the selected tile is suitable for en passant
                if (selectedBoardTile.row == Soldier.rows-4 && boardTileMatrix[Soldier.rows-4][boardTile.col].currentSoldier instanceof Pawn){
                    Pawn pawn = (Pawn) boardTileMatrix[Soldier.rows-4][boardTile.col].currentSoldier;

                    //check if the pawn double moved and if he moved on the last turn
                    return pawn.doubleMoved && turnsCounter - pawn.moveDate == 1 && pawn.whitePiece;
                }

            }
        }
            return false;
    }

    // Declare a member variable to hold the current Toast instance
    private Toast currentToast;

    // Method to show a new Toast
    private void showToast(String message) {
        // Cancel the current Toast if it's still visible
        if (currentToast != null) {
            currentToast.cancel();
        }

        // Create and show the new Toast
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

}
