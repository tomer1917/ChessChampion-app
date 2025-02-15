package com.example.chessfinalproject;


import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class Board extends LinearLayout {
    int cols;
    int rows;
    BoardTile[][] boardTileMatrix;
    ArrayList<Soldier> soldiers;


    //create the board
    public Board(Context context, int cols, int rows,int colorOne, int colorTwo,  ArrayList<Soldier> soldiers){
        super(context);
        this.cols = cols;
        this.rows = rows;
        int length = getScreenWidth(context)/(this.cols);
        this.soldiers = soldiers;
        boardTileMatrix = new BoardTile[rows][cols];


        LinearLayout linearLayout1=new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams1=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout1.setLayoutParams(layoutParams1);
        linearLayout1.setOrientation(linearLayout1.VERTICAL);
        addView(linearLayout1);

        LinearLayout linearLayout2=new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams2 =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout2.setLayoutParams(layoutParams2);
        linearLayout2.setOrientation(linearLayout2.HORIZONTAL);
        linearLayout1.addView(linearLayout2);

        int counter =0;
        if ((rows%2!=0&&cols%2==0) ||(rows%2==0&&cols%2!=0)) counter=1;
        for (int i = 0; i < rows; i++) {
            LinearLayout linearLayout=new LinearLayout(context);
            LinearLayout.LayoutParams layoutParams =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setOrientation(linearLayout.HORIZONTAL);
            linearLayout1.addView(linearLayout);

            if(cols%2==0)counter++;

            for (int j = 0; j < cols; j++) {
                int color = 0;

                if (counter%2==0)
                    color = colorTwo;
                else
                    color = colorOne;

                boolean check = true;
                for (int k = 0; k < soldiers.size(); k++) {
                    if (soldiers.get(k).startingPosition[0]==i&& soldiers.get(k).startingPosition[1]==j) {
                        boardTileMatrix[i][j] = new BoardTile(context,i,j, soldiers.get(k),length,color);
                        check = false;
                        break;
                    }
                }
                if (check)
                    boardTileMatrix[i][j] = new BoardTile(context,i,j,null,length,color);

                linearLayout.addView(boardTileMatrix[i][j]);
                counter++;
            }
        }

    }

    //create a custom board (for the home screen)
    public Board(Context context, int cols, int rows,int colorOne, int colorTwo){
        super(context);
        this.cols = cols;
        this.rows = rows;
        int length = getScreenWidth(context)/(this.cols);
        boardTileMatrix = new BoardTile[rows][cols];


        LinearLayout linearLayout1=new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams1=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout1.setLayoutParams(layoutParams1);
        linearLayout1.setOrientation(linearLayout1.VERTICAL);
        addView(linearLayout1);

        LinearLayout linearLayout2=new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams2 =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout2.setLayoutParams(layoutParams2);
        linearLayout2.setOrientation(linearLayout2.HORIZONTAL);
        linearLayout1.addView(linearLayout2);

        int counter =0;
        if ((rows%2!=0&&cols%2==0) ||(rows%2==0&&cols%2!=0)) counter=1;
        for (int i = 0; i < rows; i++) {
            LinearLayout linearLayout=new LinearLayout(context);
            LinearLayout.LayoutParams layoutParams =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setOrientation(linearLayout.HORIZONTAL);
            linearLayout1.addView(linearLayout);

            if(cols%2==0)counter++;

            for (int j = 0; j < cols; j++) {
                int color = 0;

                if (counter%2==0)
                    color = colorTwo;
                else
                    color = colorOne;

                boardTileMatrix[i][j] = new BoardTile(context,i,j,null,length,color);
                linearLayout.addView(boardTileMatrix[i][j]);
                counter++;
            }
        }
    }

    //return the width of the screen using context
    private int getScreenWidth(Context context)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return width;
    }


}
