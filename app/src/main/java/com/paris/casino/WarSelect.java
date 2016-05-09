package com.paris.casino;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by licence on 09/05/2016.
 */

public class WarSelect extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.warselect );
        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if(((RadioButton)findViewById( R.id.func_wan )).isChecked()) {
                    //startActivityForResult( new Intent( getApplicationContext(), WanWar.class ), 0 );
                } else if(((RadioButton)findViewById( R.id.func_lan )).isChecked()) {
                    //startActivityForResult( new Intent( getApplicationContext(), SearchDevice.class ), 0 );
                }
            }
        };
        (findViewById( R.id.func_wan )).setOnClickListener( buttonClickListener );
        (findViewById( R.id.func_lan )).setOnClickListener( buttonClickListener );
        new Thread( new refreshViewThread()).start();
    }

    private class refreshViewThread extends Thread {
        public void run() {
            while( true ) {
                try {
                    Thread.sleep( 50 );
                } catch( Exception e ) {
                }
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame( rect );
                if( rect.top != 0 )
                    break;
            }
            updateHandler.post( updateViewRunnable );
        }
    }

    final Handler updateHandler = new Handler();
    final Runnable updateViewRunnable = new Runnable() {
        public void run() {

            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame( rect );
            int screenWidth = rect.right - rect.left;
            int screenHeight = rect.bottom - rect.top;

            int showFontSize = 18;
            if( screenWidth <= 320 )
                showFontSize = 18;
            else if( screenWidth < 720 )
                showFontSize = 21;
            else
                showFontSize = 24;

            ((TextView)findViewById( R.id.text_1 )).setTextSize( showFontSize );
            ((TextView)findViewById( R.id.text_2 )).setTextSize( showFontSize );
            ((TextView)findViewById( R.id.text_3 )).setTextSize( showFontSize );
            ((RadioButton)findViewById( R.id.func_wan )).setTextSize( showFontSize );
            ((RadioButton)findViewById( R.id.func_lan )).setTextSize( showFontSize );

            AbsoluteLayout.LayoutParams lp;
            lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
            lp.x = 43 * screenWidth / 640;
            lp.y = 180 * screenHeight / 960;
            lp.width = 543 * screenWidth / 640;
            lp.height = 580 * screenHeight / 960;
            findViewById( R.id.layout_main ).setLayoutParams(lp);
            findViewById( R.id.layout_main ).setVisibility( View.VISIBLE );
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == 0 && resultCode == RESULT_OK ) {
            Bundle bundle = data.getExtras();
            String warType = bundle.getString( "wartype" );
            if( warType.equals( "wan" )) {
                String warNo = bundle.getString( "warno" );
                String warPhoneId = bundle.getString( "warphoneid" );
                bundle = new Bundle();
                bundle.putString( "wartype", warType );
                bundle.putString( "warno", warNo );
                bundle.putString( "warphoneid", warPhoneId );
                setResult( RESULT_OK, getIntent().putExtras( bundle ));
            } else {
                String isServer = bundle.getString( "server" );
                String phoneSum = bundle.getString( "phonesum" );
                String serverPhoneNo = bundle.getString( "serverphoneno" );
                bundle = new Bundle();
                bundle.putString( "wartype", warType );
                bundle.putString( "server", isServer );
                bundle.putString( "phonesum", phoneSum );
                bundle.putString( "serverphoneno", serverPhoneNo );
                setResult( RESULT_OK, getIntent().putExtras( bundle ));
            }
            finish();
        }
    }
}
