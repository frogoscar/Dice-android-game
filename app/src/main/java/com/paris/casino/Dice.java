package com.paris.casino;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.ImageView;

import java.util.Random;

/**
 * Created by exe on 000026/4/26.
 */
public class Dice extends ImageView {
    private Context context;
    public static final int DICE_STATE_DICE = 0;
    public static final int DICE_STATE_CUP = 1;
    public static final int DICE_STATE_SHAKING = 2;
    public int state;
    private int num;

    public Dice( Context context, int theState, int theNum ) {
        super(context);
        this.context = context;
        state = theState;
        num = theNum;
    }

    public int getNum() {
        return num;
    }

    public void setNum( int theNum ) {
        num = theNum;
    }

    protected synchronized void onDraw(Canvas cv ) {
        super.onDraw( cv );
        if( state == Dice.DICE_STATE_SHAKING || state == Dice.DICE_STATE_CUP )
            return;
        Rect rectSrc, rectDst;
        Bitmap bm;
        switch( num ) {
            case 1: bm = BitmapFactory.decodeResource( context.getResources(), R.drawable.dice1 ); break;
            case 2: bm = BitmapFactory.decodeResource( context.getResources(), R.drawable.dice2 ); break;
            case 3: bm = BitmapFactory.decodeResource( context.getResources(), R.drawable.dice3 ); break;
            case 4: bm = BitmapFactory.decodeResource( context.getResources(), R.drawable.dice4 ); break;
            case 5: bm = BitmapFactory.decodeResource( context.getResources(), R.drawable.dice5 ); break;
            default: bm = BitmapFactory.decodeResource( context.getResources(), R.drawable.dice6 ); break;
        }
        rectSrc = new Rect( 0, 0, bm.getWidth(), bm.getHeight() );
        rectDst = new Rect( 0, 0, getWidth(), getHeight());
        cv.drawBitmap( bm, rectSrc, rectDst, null );
        bm.recycle();
    }
}

//public class Dice {
//
//    public Dice() {
//        number = 0;
//    }
//
//    public void lot() {
//        Random r = new Random();
//        number = r.nextInt(6)+1;
//    }
//
//    public int getNumber() {
//        return number;
//    }
//
//    private int number;
//}
