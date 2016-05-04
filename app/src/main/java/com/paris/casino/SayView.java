package com.paris.casino;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView;

/**
 * Created by exe on 4/05/16.
 */
public class SayView extends ImageView {
    private Context context;
    private String value;
    private int sideNo;
    private int fontSize;

    public SayView( Context context, int theSideNo, int theFontSize ) {
        super(context);
        this.context = context;
        sideNo = theSideNo;
        fontSize = theFontSize;
        value = "";
    }

    protected synchronized void onDraw(Canvas cv ) {
        super.onDraw( cv );
        Bitmap bm;
        if( sideNo == 0 )
            bm = BitmapFactory.decodeResource( context.getResources(), R.drawable.speak01 );
        else
            bm = BitmapFactory.decodeResource( context.getResources(), R.drawable.speak02 );
        Paint paint = new Paint();
        Rect rectSrc, rectDst;
        rectSrc = new Rect( 0, 0, bm.getWidth(), bm.getHeight() );
        rectDst = new Rect( 0, 0, getWidth(), getHeight());
        cv.drawBitmap( bm, rectSrc, rectDst, null );

        paint.setColor( Color.rgb( 255, 255, 255 ));
        paint.setTextSize( fontSize );
        paint.setFakeBoldText( true );
        paint.setTextAlign( Paint.Align.CENTER );
        if( sideNo == 0 )
            cv.drawText( value, getWidth() / 2, getHeight() * 133 / 159 / 2 + fontSize / 3, paint );
        else
            cv.drawText( value, getWidth() / 2, getHeight() * 90 / 157 + fontSize / 3, paint );
        bm.recycle();
    }

    public void setValue( String theValue ) {
        value = theValue;
    }
}
