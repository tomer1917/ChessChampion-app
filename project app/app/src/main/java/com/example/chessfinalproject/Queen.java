package com.example.chessfinalproject;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Queen extends Soldier {

    public Queen(Bitmap imageBitmap, boolean whitePiece, int[] startingPosition, int[][] validMovement, boolean infiniteMovement,int[][] validAttack,boolean canRunOver,boolean canAttackOver, boolean infiniteAttack) {
        this.imageBitmap = imageBitmap;
        this.whitePiece = whitePiece;
        this.startingPosition = startingPosition;
        this.currentPosition = startingPosition;
        this.validMovement = validMovement;
        this.infiniteMovement = infiniteMovement;
        this.name = "queen";
        this.validAttack = validAttack;
        this.canRunOver = canRunOver;
        this.canAttackOver = canAttackOver;
        this.infiniteAttack = infiniteAttack;
    }


    @Override
    public void highlight(BoardTile[][] boardTileMatrix,boolean highlight){
        boolean runOver = false, attackOver = false;
        for (int i = 0; i < validMovement.length; i++) {
            runOver = false;
            int[] currentMovement = validMovement[i];
            int row = currentPosition[0];
            int col = currentPosition[1];

            if (infiniteMovement) {
                row+= currentMovement[0];
                col+= currentMovement[1];


                while (row<rows && col< cols&& row>=0 && col>=0 ) {

                    //if the soldier ran over another soldier in the this movement
                    if (boardTileMatrix[row][col].currentSoldier!= null){
                        runOver = true;
                    }

                    if (canRunOver || !runOver){
                        boardTileMatrix[row][col].setHighlight(highlight);
                        boardTileMatrix[row][col].invalidate();
                    }

                    row+= currentMovement[0];
                    col+= currentMovement[1];

                }

            }else if (row+ currentMovement[0]>=0 && col + currentMovement[1]>=0 && row+ currentMovement[0]<rows &&col + currentMovement[1]<cols){

                //check once if the movement is valid
                if(boardTileMatrix[row + currentMovement[0]][col + currentMovement[1]].currentSoldier == null) {

                    boardTileMatrix[row + currentMovement[0]][col + currentMovement[1]].setHighlight(highlight);
                    boardTileMatrix[row + currentMovement[0]][col + currentMovement[1]].invalidate();
                }
            }
        }

        for (int i = 0; i < validAttack.length; i++) {
            attackOver = false;
            int[] currentAttack = validAttack[i];
            int row2 = currentPosition[0];
            int col2 = currentPosition[1];
            if (infiniteAttack) {
                row2+= currentAttack[0];
                col2+= currentAttack[1];

                while (row2<rows && col2< cols&& row2>=0 && col2>=0 ) {

                    //if the soldier ran over another soldier in the this movement
                    if (boardTileMatrix[row2][col2].currentSoldier!= null){

                        if (!attackOver && boardTileMatrix[row2][col2].currentSoldier.whitePiece!= whitePiece){
                            boardTileMatrix[row2][col2].setHighlight(highlight);
                            boardTileMatrix[row2][col2].invalidate();
                        }else if (canAttackOver && boardTileMatrix[row2][col2].currentSoldier.whitePiece!= whitePiece){
                            boardTileMatrix[row2][col2].setHighlight(highlight);
                            boardTileMatrix[row2][col2].invalidate();
                        }
                        attackOver = true;

                    }
                    row2+= currentAttack[0];
                    col2+= currentAttack[1];

                }
            }else if (row2+ currentAttack[0]>=0 && col2+ currentAttack[1]>=0 && row2+ currentAttack[0]<rows &&col2+ currentAttack[1]<cols){

                if(boardTileMatrix[row2 + currentAttack[0]][col2 + currentAttack[1]].currentSoldier!= null){
                    if (whitePiece!= boardTileMatrix[row2 + currentAttack[0]][col2 + currentAttack[1]].currentSoldier.whitePiece){
                        boardTileMatrix[row2 + currentAttack[0]][col2 + currentAttack[1]].setHighlight(highlight);
                        boardTileMatrix[row2 + currentAttack[0]][col2 + currentAttack[1]].invalidate();
                    }
                }
            }
        }
    }

    @Override
    public boolean IsValidMovement(BoardTile currentTile, BoardTile[][] boardTileMatrix) {
        boolean runOver = false;
        for (int i = 0; i < validMovement.length; i++) {
            runOver = false;
            int[] currentMovement = validMovement[i];

            //if custom chess check if valid movement can be simplified
            if (Soldier.customChess){
                if (currentMovement[0] == currentMovement[1]) {
                    currentMovement = currentMovement[1] > 0 ? new int[]{1, 1} : new int[]{-1, -1};
                } else if (currentMovement[0] == -currentMovement[1]) {
                    currentMovement = currentMovement[1] < 0 ? new int[]{1, -1} : new int[]{-1, 1};
                }

                if (currentMovement[0] == 0) {
                    currentMovement = currentMovement[1] > 0 ? new int[]{0, 1} : new int[]{0, -1};
                } else if (currentMovement[1] == 0) {
                    currentMovement = currentMovement[0] < 0 ? new int[]{-1, 0} : new int[]{1, 0};
                }
            }

            int row = currentPosition[0];
            int col = currentPosition[1];
            if (infiniteMovement) {
                row+= currentMovement[0];
                col+= currentMovement[1];
                while (row<rows && col< cols&& row>=0 && col>=0 ) {

                    //if the soldier ran over another soldier in the this movement
                    if (boardTileMatrix[row][col].currentSoldier!= null && boardTileMatrix[row][col] != currentTile ){
                        runOver = true;
                    }
                    //if the current tile is in this current movement
                    else if(boardTileMatrix[row][col] == currentTile){
                        if (Soldier.customChess){
                            if ((currentMovement[0] == 0 && (col-startingPosition[1]) % validMovement[i][1] == 0 && (canRunOver || !runOver))
                                    || (currentMovement[1] == 0 && (row-startingPosition[0]) % validMovement[i][0] == 0 && (canRunOver || !runOver))
                                    || (currentMovement[1] != 0 && currentMovement[0] != 0 && (row-startingPosition[0]) % validMovement[i][0] == 0 && (col-startingPosition[1]) % validMovement[i][1] == 0 && (canRunOver || !runOver))) {
                                return true;
                            }
                        }
                        else
                            return (canRunOver || !runOver);
                    }
                    row+= currentMovement[0];
                    col+= currentMovement[1];
                }

            }else {
                if (Soldier.customChess){
                    row+= currentMovement[0];
                    col+= currentMovement[1];
                    while (row<rows && col< cols&& row>=0 && col>=0){

                        //if the soldier ran over another soldier in the this movement
                        if (boardTileMatrix[row][col].currentSoldier!= null && boardTileMatrix[row][col] != currentTile ){
                            runOver = true;
                        }
                        //if the current tile is in this current movement
                        else if(boardTileMatrix[row][col] == currentTile){
                            if (currentPosition[0]+validMovement[i][0] == currentTile.row && currentPosition[1]+validMovement[i][1] == currentTile.col  &&(canRunOver || !runOver)){
                                return true;
                            }
                        }
                        row+= currentMovement[0];
                        col+= currentMovement[1];
                    }
                }else {
                    //check once if the movement is valid
                    if (row + currentMovement[0] == currentTile.row && col + currentMovement[1] == currentTile.col)
                        return true;
                }
            }
        }
        return false;
    }



    @Override
    public boolean IsValidAttack(BoardTile currentTile, BoardTile[][] boardTileMatrix) {

        boolean runOver = false;
        for (int i = 0; i < validAttack.length; i++) {
            runOver = false;
            int[] currentAttack = validAttack[i];

            if (Soldier.customChess){
                if (currentAttack[0] == currentAttack[1]) {
                    currentAttack = currentAttack[1] > 0 ? new int[]{1, 1} : new int[]{-1, -1};
                } else if (currentAttack[0] == -currentAttack[1]) {
                    currentAttack = currentAttack[1] < 0 ? new int[]{1, -1} : new int[]{-1, 1};
                }

                if (currentAttack[0] == 0) {
                    currentAttack = currentAttack[1] > 0 ? new int[]{0, 1} : new int[]{0, -1};
                } else if (currentAttack[1] == 0) {
                    currentAttack = currentAttack[0] < 0 ? new int[]{-1, 0} : new int[]{1, 0};
                }
            }


            int row = currentPosition[0];
            int col = currentPosition[1];
            if (infiniteAttack) {
                row+= currentAttack[0];
                col+= currentAttack[1];
                while (row<rows && col< cols&& row>=0 && col>=0 ) {

                    //if the soldier ran over another soldier in the this movement
                    if (boardTileMatrix[row][col].currentSoldier!= null && boardTileMatrix[row][col] != currentTile ){
                        runOver = true;
                    }
                    //if the current tile is in this current movement
                    else if(boardTileMatrix[row][col] == currentTile && boardTileMatrix[row][col].currentSoldier!= null ) {
                        if (currentTile.currentSoldier.whitePiece != whitePiece)
                        {
                            if (Soldier.customChess){
                                if ((currentAttack[0] == 0 && (col-startingPosition[1]) % validAttack[i][1] == 0 && (canAttackOver || !runOver))
                                        || (currentAttack[1] == 0 && (row-startingPosition[0]) % validAttack[i][0] == 0 && (canAttackOver || !runOver))
                                        || (currentAttack[1] != 0 && currentAttack[0] != 0 && (row-startingPosition[0]) % validAttack[i][0] == 0 && (col-startingPosition[1]) % validAttack[i][1] == 0 && (canAttackOver || !runOver))) {
                                    return true;
                                }
                            }
                            else
                                return (canAttackOver || !runOver);
                        }
                    }
                    row+= currentAttack[0];
                    col+= currentAttack[1];

                }

            }else {
                if (Soldier.customChess){
                    row+= currentAttack[0];
                    col+= currentAttack[1];
                    while (row<rows && col< cols&& row>=0 && col>=0){

                        //if the soldier ran over another soldier in the this movement
                        if (boardTileMatrix[row][col].currentSoldier!= null && boardTileMatrix[row][col] != currentTile ){
                            runOver = true;
                        }
                        //if the current tile is in this current movement
                        else if(boardTileMatrix[row][col] == currentTile &&  currentTile.currentSoldier.whitePiece != whitePiece){
                            if (currentPosition[0]+validAttack[i][0] == currentTile.row && currentPosition[1]+validAttack[i][1] == currentTile.col  &&(canAttackOver || !runOver)){
                                return true;
                            }
                        }
                        row+= currentAttack[0];
                        col+= currentAttack[1];
                    }
                }
                else {
                    //check once if the movement is valid
                    if (row + currentAttack[0] == currentTile.row && col + currentAttack[1] == currentTile.col && currentTile.currentSoldier.whitePiece != whitePiece)
                        return true;
                    }
            }
        }
        return false;
    }
}
