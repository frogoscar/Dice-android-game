package com.paris.casino;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

/**
 * Created by licence on 09/05/2016.
 */

public class Options extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.options );

        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame( rect );
        int screenWidth = rect.right - rect.left;

        int showFontSize = 18;
        if( screenWidth <= 320 )
            showFontSize = 18;
        else if( screenWidth < 720 )
            showFontSize = 21;
        else
            showFontSize = 24;

        ((CheckBox)findViewById( R.id.check_voice )).setTextSize( showFontSize );
        ((Button)findViewById( R.id.button_ok )).setTextSize( showFontSize );
        ((Button)findViewById( R.id.button_cancel )).setTextSize( showFontSize );

        ((CheckBox)findViewById( R.id.check_voice )).setChecked( true );

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if( v.equals(((View)findViewById( R.id.button_ok )))) {
                    finish();
                } else if( v.equals(((View)findViewById( R.id.button_cancel )))) {
                    finish();
/*				} else if( v.equals(((View)findViewById( R.id.button_default )))) {
					new AlertDialog.Builder( Options.this )
					.setMessage( Options.this.getString( R.string.ReallyRestoreDefault ))
					.setPositiveButton( Options.this.getString( R.string.Yes ), new DialogInterface.OnClickListener() {
						public void onClick( DialogInterface dialog, int whichButton ) {
							Config.setDefaultConfig();
							Config.saveConfig();
					        if( Config.canCompute24 )
					        	((CheckBox)findViewById( R.id.check_can_compute_24 )).setChecked( true );
					        else
					        	((CheckBox)findViewById( R.id.check_can_compute_24 )).setChecked( false );
					        if( Config.allowDecimal )
					        	((CheckBox)findViewById( R.id.check_allow_decimal )).setChecked( true );
					        else
					        	((CheckBox)findViewById( R.id.check_allow_decimal )).setChecked( false );
					        if( Config.difficulty == 0 )
					        	((RadioButton)findViewById( R.id.radio_easy )).setChecked( true );
					        else
					        	((RadioButton)findViewById( R.id.radio_hard )).setChecked( true );
						}
					} )
					.setNegativeButton( Options.this.getString( R.string.No ), null )
					.show();*/
                }
            }
        };
        ((Button)findViewById( R.id.button_ok )).setOnClickListener( buttonClickListener );
        ((Button)findViewById( R.id.button_cancel )).setOnClickListener( buttonClickListener );
        //((Button)findViewById( R.id.button_default )).setOnClickListener( buttonClickListener );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
