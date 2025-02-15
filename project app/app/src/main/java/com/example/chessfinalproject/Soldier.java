package com.example.chessfinalproject;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public abstract class Soldier {
    protected Bitmap imageBitmap;
    protected boolean whitePiece;
    protected Bitmap diffaultImageBitmap;
    protected int[] startingPosition = new int[2]; // [row,col]
    protected int[] currentPosition = new int[2]; // [row,col]
    protected int[][] validMovement;// [[row,col],[row,col]]
    protected int[][] validAttack;// [[row,col],[row,col]]
    protected boolean infiniteMovement;
    protected String name;
    protected boolean canRunOver;
    protected boolean canAttackOver;
    protected boolean infiniteAttack;
    protected static int rows;
    protected static int cols;
    protected static boolean customChess = false;

    // return true if the soldier can move to currentTile
    public abstract boolean IsValidMovement(BoardTile currentTile, BoardTile[][] boardTileMatrix);

    // return true if the soldier can attack currentTile
    public abstract boolean IsValidAttack(BoardTile currentTile, BoardTile[][] boardTileMatrix);

    //the function highlights or removes the highlight of all the possible move the soldier can move
    public abstract void highlight(BoardTile[][] boardTileMatrix,boolean highlight);
 }
