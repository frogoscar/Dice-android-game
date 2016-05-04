package com.paris.casino;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by exe on 4/05/16.
 */
public class ScoreView extends View {
    private Context context;
    private int score;
    private int fontSize;

    public ScoreView( Context context, int theFontSize ) {
        super(context);
        this.context = context;
        score = 0;
        fontSize = theFontSize;
    }

    protected synchronized void onDraw(Canvas cv ) {
        super.onDraw( cv );
        Bitmap bm = BitmapFactory.decodeResource( context.getResources(), R.drawable.win_ball );
        Paint paint = new Paint();
        Rect rectSrc, rectDst;
        rectSrc = new Rect( 0, 0, bm.getWidth(), bm.getHeight() );
        rectDst = new Rect( 0, 0, getWidth(), getHeight());
        cv.drawBitmap( bm, rectSrc, rectDst, null );

        paint.setColor( Color.rgb( 255, 255, 255 ));
        paint.setTextSize( fontSize );
        paint.setFakeBoldText( true );
        paint.setTextAlign( Paint.Align.CENTER );
        cv.drawText( Integer.toString( score ), getWidth() / 2, getHeight() / 2 + fontSize / 3, paint );

        bm.recycle();
    }

    public void addValue() {
        score ++;
        postInvalidate();
    }

    public void clearValue() {
        score = 0;
        postInvalidate();
    }
}
