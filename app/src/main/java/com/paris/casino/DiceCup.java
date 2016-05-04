package com.paris.casino;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.ImageView;

/**
 * Created by exe on 4/05/16.
 */
public class DiceCup extends ImageView {
    private Context context;

    public DiceCup( Context context ) {
        super(context);
        this.context = context;
    }

    protected synchronized void onDraw(Canvas cv ) {
        super.onDraw( cv );
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.cup);
        Rect rectSrc, rectDst;
        rectSrc = new Rect( 0, 0, bm.getWidth(), bm.getHeight() );
        rectDst = new Rect( 0, 0, getWidth(), getHeight());
        cv.drawBitmap( bm, rectSrc, rectDst, null );
        bm.recycle();
    }

}
