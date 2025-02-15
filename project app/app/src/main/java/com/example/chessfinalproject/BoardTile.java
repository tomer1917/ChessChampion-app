package com.example.chessfinalproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;

public class BoardTile extends androidx.appcompat.widget.AppCompatImageView {
    public int row;
    public int col;
    Soldier currentSoldier = null;
    int length;
    private boolean redAura = false;
    int tileColor;//-2 for default brown -1 for default white
    boolean highlight = false;

    //regular tile
    public BoardTile(Context context, int row, int col, Soldier currentSoldier, int length, int tileColor) {
        super(context);
        this.row = row;
        this.col = col;
        this.currentSoldier = currentSoldier;
        this.length = length;
        this.tileColor = tileColor;

        LinearLayout.LayoutParams ViewLayoutParams = new LinearLayout.LayoutParams(length, length);
        ViewLayoutParams.setMargins(0,0,0,0);
        setLayoutParams(ViewLayoutParams);

    }

    //draw the tile
    @Override
    protected void onDraw(Canvas canvas) {
        Drawable imageDrawable = null;

        if (tileColor==-1||tileColor==-2){
            if (tileColor == -1)
                imageDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.brighttile, null);
            else
                imageDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.browntile, null);

            imageDrawable.setBounds(0, 0, length, length);
            imageDrawable.draw(canvas);
        } else{
            canvas.drawColor(tileColor);
        }

        if (highlight){
            imageDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.blueshade, null);
            imageDrawable.setBounds(0, 0, length, length);
            imageDrawable.draw(canvas);
        }

        if (currentSoldier != null){

            if (redAura){
                imageDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.check, null);
                imageDrawable.setBounds(0, 0, length, length);
                imageDrawable.draw(canvas);
            }

            BitmapDrawable drawable = new BitmapDrawable(getResources(), currentSoldier.imageBitmap);
            drawable.setBounds(0, 0, length, length);
            drawable.draw(canvas);
        }

    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public void setRedAura(boolean redAura) {
        this.redAura = redAura;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setWillNotDraw(false);
    }

}
