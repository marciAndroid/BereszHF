package com.example.mate_pc.game1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

public class Platform extends GameObject {

    private Bitmap image;

    private GameSurface gameSurface;

    public Platform(GameSurface gameSurface, Bitmap image, int x, int y, int height) {
        super(image, 1, 1, x, y, height);

        this.gameSurface = gameSurface;
        this.image = createSubImageAt(0,0);
    }

    private Bitmap getCurrentMoveBitmap(){
        return image;
    }

    void draw(Canvas canvas) {
        Bitmap bitmap = getCurrentMoveBitmap();
        canvas.drawBitmap(bitmap, x, y, null);
    }

    boolean isBelow(Character character) {
        if(character.getX() + (double)character.getWidth()*4/5 < getX() || getX()+getWidth() < character.getX() + (double)character.getWidth()/5) {
            return false;
        }
        else {
            if (getY() >= character.getBottomHeight()) {
                return true;
            }
        }
        return false;
    }


}
