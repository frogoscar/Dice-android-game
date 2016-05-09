package com.paris.casino;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

/**
 * Created by exe on 4/05/16.
 */
public class DiceActivity extends Activity implements View.OnTouchListener {
    private final static String TAG = DiceActivity.class.getName();
    private final int MAX_PERSON = 2;
    private ImageView ivHead[];
    public final static int MAX_DICE = 5;
    private Dice dice[][];
    private ScoreView yourScoreView, myScoreView;
    private SayView yourSayView, mySayView;
    private Thinker thinker;
    private SayValue mySayValue;
    private final int MAX_KEY = 15;
    private KeyView keyView[];
    private ImageView diceCup;
    private ImageView frame;
    private ImageView imageGray[], imageSide[];
    private ImageView imageOption, imageHelp, imageWar;
    private ImageView imageWin, imageLose;
    private GestureDetector gd;
    private View touchView;
    private int resultState;
    private ProgressDialog progressDialog;
    private int focusPerson;
    private int runSerialNo;
    private MediaPlayer mpClick = null;
    private int MAX_TRACE = 10;
    private SayValue sayValueTrace[];
    private int currTrace;
    private boolean isPure;
    private boolean gameRunning;
    private boolean haveSaid;

    private int warSign;
    private boolean warServer;
    private int phoneSum;
    private int serverPhoneNo;
    private char getDiceValue[] = new char[2];
    private boolean isFirstFocus;
    private SayValue yourSayValue;
    private final char CMD_SET_DICE = 0;
    private final char CMD_SAY = 1;

    private final int SUB_CMD_INTELLIGENCE_GAME_WAR_MSG = 8030;
    private final int WAR_CMD_DICE_SET_DICE = 2;
    private final int WAR_CMD_DICE_NOTIFY_DICE_SET = 3;
    private final int WAR_CMD_DICE_SAY = 4;
    private final int WAR_CMD_DICE_NOTIFY_SAY = 5;
    private int warNo;
    private int warPhoneId;
    private boolean yourDiceSetted;
    private View clickedView;

    Handler showProgressHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean dialogShouldShow = false;
            if( progressDialog == null ) {
                progressDialog = new ProgressDialog( DiceActivity.this );
                dialogShouldShow = true;
            }
            String message = msg.getData().getString("message");
            progressDialog.setMessage(message);
            if( dialogShouldShow )
                progressDialog.show();
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diceactivity);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        warSign = Integer.parseInt( getIntent().getStringExtra( "warsign" ));
        if( warSign != 0 ) {
            if( warSign == 1 ) {
                String server = getIntent().getStringExtra( "server" );
                if( server.equals( "1" ))
                    warServer = true;
                else {
                    warServer = false;
                    serverPhoneNo = Integer.parseInt( getIntent().getStringExtra( "serverphoneno" ));
                }
                phoneSum = Integer.parseInt( getIntent().getStringExtra( "phonesum" ));
                Toast.makeText( this, "Start" + Integer.toString( phoneSum + 1 ) + "-players game", Toast.LENGTH_SHORT ).show();
                if( warServer )
                    isFirstFocus = true;
                else
                    isFirstFocus = false;
                yourDiceSetted = false;
            } else if( warSign == 2 ) {
                warNo = Integer.parseInt( getIntent().getStringExtra( "warno" ));
                warPhoneId = Integer.parseInt( getIntent().getStringExtra( "warphoneid" ));
                Log.d(TAG, "warPhoneId = " + warPhoneId);
                if( warPhoneId == 0 )
                    isFirstFocus = true;
                else
                    isFirstFocus = false;
                Toast.makeText( this, "Start 2-players game", Toast.LENGTH_SHORT ).show();
                yourDiceSetted = false;
            }
        }
        gameRunning = false;
        runSerialNo = 0;
        haveSaid = false;
        getDiceValue[1] = (char)-1;
        File audioFile = new File(  Environment.getExternalStorageDirectory().toString() + "/dice/work/click.wav" );
        if( audioFile.exists()) {
            mpClick = new MediaPlayer();
            try {
                mpClick.setDataSource( Environment.getExternalStorageDirectory().toString() + "/dice/work/click.wav" );
                mpClick.prepare();
            } catch( Exception e ) {
            }
        }

        new Thread( new refreshViewThread()).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if( mpClick != null ) {
            if( mpClick.isPlaying())
                mpClick.stop();
            mpClick.release();
        }
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

    final Runnable updateViewRunnable = new Runnable() {
        public void run() {
            showActivity();
        }
    };

    private void showActivity() {
        getWindow().setBackgroundDrawable( getResources().getDrawable(R.drawable.bkcolor));
        AbsoluteLayout alo = (AbsoluteLayout)findViewById( R.id.layout_dice_panel );

        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame( rect );
        int screenWidth = rect.right - rect.left;
        int screenHeight = rect.bottom - rect.top;

        focusPerson = -1;
        AbsoluteLayout.LayoutParams lp;
        frame = new ImageView( this );
        frame.setBackgroundResource( R.drawable.frame );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.width = 13 * screenWidth / 640;
        lp.height = 50 * screenHeight / 960;
        lp.width = 615 * screenWidth / 640;
        lp.height = 165 * screenHeight / 960;
        frame.setLayoutParams(lp);
        frame.setVisibility( View.INVISIBLE );
        alo.addView( frame );

        ivHead = new ImageView[MAX_PERSON];
        ivHead[0] = new ImageView( this );
        ivHead[0].setBackgroundResource( R.drawable.head1 );
        alo.addView( ivHead[0] );

        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 507 * screenWidth / 640;
        lp.y = 69 * screenHeight / 960;
        lp.width = 105 * screenWidth / 640;
        lp.height = 118 * screenHeight / 960;
        ivHead[0].setLayoutParams(lp);

        ivHead[1] = new ImageView( this );
        ivHead[1].setBackgroundResource( R.drawable.head2 );
        alo.addView( ivHead[1] );

        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 28 * screenWidth / 640;
        lp.y = 453 * screenHeight / 960;
        lp.width = 105 * screenWidth / 640;
        lp.height = 118 * screenHeight / 960;
        ivHead[1].setLayoutParams(lp);

        dice = new Dice[MAX_PERSON][MAX_DICE];
        int x = 39;
        int i;
        for( i = 0; i < MAX_DICE; i++ ) {
            dice[0][i] = new Dice( this, Dice.DICE_STATE_CUP, (int)( Math.random() * 6 ) + 1 );
            alo.addView( dice[0][i] );
            lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
            lp.x = x * screenWidth / 640;
            lp.y = 86 * screenHeight / 960;
            lp.width = 77 * screenWidth / 640;
            lp.height = 81 * screenHeight / 960;
            dice[0][i].setLayoutParams(lp);
            x += 90;
        }

        x = 159;
        for( i = 0; i < MAX_DICE; i++ ) {
            dice[1][i] = new Dice( this, Dice.DICE_STATE_DICE, (int)( Math.random() * 6 ) + 1 );
            alo.addView( dice[1][i] );
            lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
            lp.x = x * screenWidth / 640;
            lp.y = 471 * screenHeight / 960;
            lp.width = 77 * screenWidth / 640;
            lp.height = 81 * screenHeight / 960;
            dice[1][i].setLayoutParams(lp);
            x += 90;
        }

        imageGray = new ImageView[2];
        imageGray[0] = new ImageView( this );
        imageGray[0].setBackgroundResource( R.drawable.gray );
        alo.addView( imageGray[0] );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 507 * screenWidth / 640;
        lp.y = 69 * screenHeight / 960;
        lp.width = 105 * screenWidth / 640;
        lp.height = 118 * screenHeight / 960;
        imageGray[0].setLayoutParams(lp);
        imageGray[0].setVisibility( View.INVISIBLE );

        imageGray[1] = new ImageView( this );
        imageGray[1].setBackgroundResource( R.drawable.gray );
        alo.addView( imageGray[1] );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 28 * screenWidth / 640;
        lp.y = 453 * screenHeight / 960;
        lp.width = 105 * screenWidth / 640;
        lp.height = 118 * screenHeight / 960;
        imageGray[1].setLayoutParams(lp);
        imageGray[1].setVisibility( View.INVISIBLE );

        imageSide = new ImageView[2];
        imageSide[0] = new ImageView( this );
        imageSide[0].setBackgroundResource( R.drawable.fram_y02 );
        alo.addView( imageSide[0] );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 495 * screenWidth / 640;
        lp.y = 57 * screenHeight / 960;
        lp.width = 130 * screenWidth / 640;
        lp.height = 142 * screenHeight / 960;
        imageSide[0].setLayoutParams(lp);
        imageSide[0].setVisibility( View.INVISIBLE );

        imageSide[1] = new ImageView( this );
        imageSide[1].setBackgroundResource( R.drawable.fram_y );
        alo.addView( imageSide[1] );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 16 * screenWidth / 640;
        lp.y = 441 * screenHeight / 960;
        lp.width = 130 * screenWidth / 640;
        lp.height = 142 * screenHeight / 960;
        imageSide[1].setLayoutParams(lp);
        imageSide[1].setVisibility( View.INVISIBLE );

        int showFontSize = 20;
        if( screenWidth <= 320 )
            showFontSize = 20;
        else if( screenWidth < 720 )
            showFontSize = 40;
        else
            showFontSize = 50;

        yourScoreView = new ScoreView( this, showFontSize );
        alo.addView( yourScoreView );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 577 * screenWidth / 640;
        lp.y = 161 * screenHeight / 960;
        lp.width = 61 * screenWidth / 640;
        lp.height = 60 * screenHeight / 960;
        yourScoreView.setLayoutParams(lp);

        yourSayView = new SayView( this, 1, showFontSize );
        alo.addView( yourSayView );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 183 * screenWidth / 640;
        lp.y = 227 * screenHeight / 960;
        lp.width = 286 * screenWidth / 640;
        lp.height = 157 * screenHeight / 960;
        yourSayView.setLayoutParams(lp);
        yourSayView.setVisibility( View.INVISIBLE );

        myScoreView = new ScoreView( this, showFontSize );
        alo.addView( myScoreView );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 4 * screenWidth / 640;
        lp.y = 543 * screenHeight / 960;
        lp.width = 61 * screenWidth / 640;
        lp.height = 60 * screenHeight / 960;
        myScoreView.setLayoutParams(lp);

        mySayView = new SayView( this, 0, showFontSize );
        alo.addView( mySayView );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 183 * screenWidth / 640;
        lp.y = 235 * screenHeight / 960;
        lp.width = 286 * screenWidth / 640;
        lp.height = 159 * screenHeight / 960;
        mySayView.setLayoutParams(lp);
        mySayView.setVisibility( View.INVISIBLE );

        gd = new GestureDetector( new GestureDetector.SimpleOnGestureListener() {
            public synchronized boolean onSingleTapUp( MotionEvent e ) {
                if( mySayValue != null ) {
                    int i;
                    for( i = 0; i < MAX_KEY; i++ )
                        if( touchView == keyView[i] ) {
                            if( mpClick != null )
                                mpClick.start();
                            break;
                        }

                    if( i < MAX_KEY ) {
                        keyView[i].setPress( true );
                        (new keyPressThread( i )).start();
                    }
                }
                return super.onSingleTapUp( e );
            }
        });

        keyView = new KeyView[ MAX_KEY ];
        for( i = 0; i < MAX_KEY; i++ )
            keyView[i] = new KeyView( this, i );
        for( i = 0; i < MAX_KEY; i++ ) {
            alo.addView( keyView[i] );
            keyView[i].setOnTouchListener( this );
            registerForContextMenu( keyView[i] );
            lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
            switch( i ) {
                case 0: lp.x = 138 * screenWidth / 640; lp.y = 628 * screenHeight / 960; break;
                case 1: lp.x = 217 * screenWidth / 640; lp.y = 628 * screenHeight / 960; break;
                case 2: lp.x = 296 * screenWidth / 640; lp.y = 628 * screenHeight / 960; break;
                case 3: lp.x = 377 * screenWidth / 640; lp.y = 628 * screenHeight / 960; break;
                case 4: lp.x = 457 * screenWidth / 640; lp.y = 628 * screenHeight / 960; break;
                case 5: lp.x = 138 * screenWidth / 640; lp.y = 710 * screenHeight / 960; break;
                case 6: lp.x = 217 * screenWidth / 640; lp.y = 710 * screenHeight / 960; break;
                case 7: lp.x = 296 * screenWidth / 640; lp.y = 710 * screenHeight / 960; break;
                case 8: lp.x = 377 * screenWidth / 640; lp.y = 710 * screenHeight / 960; break;
                case 9: lp.x = 457 * screenWidth / 640; lp.y = 710 * screenHeight / 960; break;
                case 10: lp.x = 138 * screenWidth / 640; lp.y = 793 * screenHeight / 960; break;
                case 11: lp.x = 217 * screenWidth / 640; lp.y = 793 * screenHeight / 960; break;
                case 12: lp.x = 296 * screenWidth / 640; lp.y = 793 * screenHeight / 960; break;
                case 13: lp.x = 377 * screenWidth / 640; lp.y = 793 * screenHeight / 960; break;
                case 14: lp.x = 457 * screenWidth / 640; lp.y = 793 * screenHeight / 960; break;
                default: break;
            }
            lp.width = 63 * screenWidth / 640;
            lp.height = 63 * screenHeight / 960;
            keyView[i].setLayoutParams(lp);
        }

        diceCup = new ImageView( this );
        diceCup.setBackgroundResource( R.drawable.cup );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 252 * screenWidth / 640;
        lp.y = 10 * screenHeight / 960;
        lp.width = 146 * screenWidth / 640;
        lp.height = 209 * screenHeight / 960;
        diceCup.setLayoutParams(lp);
        diceCup.setVisibility( View.INVISIBLE );
        alo.addView( diceCup );

        imageOption = new ImageView( this );
        imageOption.setBackgroundResource( R.drawable.select );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 252 * screenWidth / 640;
        lp.y = 918 * screenHeight / 960;
        lp.width = 63 * screenWidth / 640;
        lp.height = 22 * screenHeight / 960;
        imageOption.setLayoutParams(lp);
        alo.addView( imageOption );

        imageHelp = new ImageView( this );
        imageHelp.setBackgroundResource( R.drawable.explain );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 395 * screenWidth / 640;
        lp.y = 918 * screenHeight / 960;
        lp.width = 63 * screenWidth / 640;
        lp.height = 22 * screenHeight / 960;
        imageHelp.setLayoutParams(lp);
        alo.addView( imageHelp );

        imageWar = new ImageView( this );
        imageWar.setBackgroundResource( R.drawable.pvp_dark );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 539 * screenWidth / 640;
        lp.y = 918 * screenHeight / 960;
        lp.width = 63 * screenWidth / 640;
        lp.height = 22 * screenHeight / 960;
        imageWar.setLayoutParams(lp);
        alo.addView( imageWar );

        imageWin = new ImageView( this );
        imageWin.setBackgroundResource( R.drawable.win );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 107 * screenWidth / 640;
        lp.y = 342 * screenHeight / 960;
        lp.width = 468 * screenWidth / 640;
        lp.height = 133 * screenHeight / 960;
        imageWin.setLayoutParams(lp);
        imageWin.setVisibility( View.INVISIBLE );
        alo.addView( imageWin );

        imageLose = new ImageView( this );
        imageLose.setBackgroundResource( R.drawable.lose );
        lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
        lp.x = 107 * screenWidth / 640;
        lp.y = 342 * screenHeight / 960;
        lp.width = 468 * screenWidth / 640;
        lp.height = 133 * screenHeight / 960;
        imageLose.setLayoutParams(lp);
        imageLose.setVisibility( View.INVISIBLE );
        alo.addView( imageLose );

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                clickedView = v;
                if( v.equals( imageOption ))
                    imageOption.setBackgroundResource( R.drawable.select_dark );
                else if( v.equals( imageHelp ))
                    imageHelp.setBackgroundResource( R.drawable.explain_dark );
                else if( v.equals( imageWar ))
                    imageWar.setBackgroundResource( R.drawable.pvp );
                (new clickViewThread()).start();
            }
        };
        imageOption.setOnClickListener( buttonClickListener );
        imageHelp.setOnClickListener( buttonClickListener );
        imageWar.setOnClickListener( buttonClickListener );

        sayValueTrace = new SayValue[MAX_TRACE];
        mySayValue = null;
        new Thread( new runGame()).start();
    }

    private class clickViewThread extends Thread {
        public void run() {
            try {
                Thread.sleep( 50 );
            } catch( Exception e ) {
            }
            if( clickedView.equals( imageOption )) {
                startActivity( new Intent( DiceActivity.this, Options.class ));
            } else if( clickedView.equals( imageHelp )) {
                startActivity( new Intent( DiceActivity.this, Description.class ));
            } else if( clickedView.equals( imageWar )) {
                startActivityForResult( new Intent( DiceActivity.this, WarSelect.class ), 0 );
            }
            updateHandler.post( restoreViewRunnable );
        }
    }

    final Runnable restoreViewRunnable = new Runnable() {
        public void run() {
            if( clickedView.equals( imageOption ))
                imageOption.setBackgroundResource( R.drawable.select );
            else if( clickedView.equals( imageHelp ))
                imageHelp.setBackgroundResource( R.drawable.explain );
            else if( clickedView.equals( imageWar ))
                imageWar.setBackgroundResource( R.drawable.pvp_dark );
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode != RESULT_OK )
            return;
        if( requestCode == 0 ) {
            Bundle bundle = data.getExtras();
            String warType = bundle.getString( "wartype" );
            if( warType == null)
                Log.d(TAG, "warType is null" );
            else
                Log.d(TAG, "warType is " + warType );
            if( warType.equals( "wan" )) {
                warSign = 2;
                runSerialNo++;
                warNo = Integer.parseInt( bundle.getString( "warno" ));
                warPhoneId = Integer.parseInt( bundle.getString( "warphoneid" ));
                if( warPhoneId == 0 )
                    isFirstFocus = true;
                else
                    isFirstFocus = false;
                Toast.makeText( this, "��ʼ2�˶�ս", Toast.LENGTH_SHORT ).show();
                yourDiceSetted = false;
                new Thread( new runGame()).start();
            } else {
                warSign = 1;
                runSerialNo++;
                String server = bundle.getString( "server" );
                if( server.equals( "1" ))
                    warServer = true;
                else {
                    warServer = false;
                    serverPhoneNo = Integer.parseInt( bundle.getString( "serverphoneno" ));
                }
                phoneSum = Integer.parseInt( bundle.getString( "phonesum" ));

                Toast.makeText( this, "��ʼ" + Integer.toString( phoneSum + 1 ) + "�˶�ս��", Toast.LENGTH_SHORT ).show();
                if( warServer )
                    isFirstFocus = true;
                else
                    isFirstFocus = false;
                yourDiceSetted = false;
                new Thread( new runGame()).start();
            }
        } else if( requestCode == 1 ) {
            if(mpClick == null) {
                File audioFile = new File(  Environment.getExternalStorageDirectory().toString() + "/dice/work/click.wav" );
                if( audioFile.exists()) {
                    mpClick = new MediaPlayer();
                    try {
                        mpClick.setDataSource( Environment.getExternalStorageDirectory().toString() + "/dice/work/click.wav" );
                        mpClick.prepare();
                    } catch( Exception e ) {
                    }
                } else {
                    Toast.makeText( this, "�Բ��������ļ��������أ��´�����ʱ��������", Toast.LENGTH_LONG ).show();
                }
            }
        }
    }

    private class runGame implements Runnable {
        private int myRunSerialNo;
        public void run() {
            Log.d(TAG, "run game" );
            gameRunning = true;
            haveSaid = false;
            myRunSerialNo = runSerialNo;
            focusPerson = -1;
            if( warSign == 0 )
                thinker = new Thinker( dice[0][0].getNum(), dice[0][1].getNum(), dice[0][2].getNum(), dice[0][3].getNum(), dice[0][4].getNum());
            else
                yourSayValue = new SayValue();
            mySayValue = new SayValue();
            resultState = 0;
            isPure = false;
            currTrace = 0;
            sayValueTrace[currTrace] = mySayValue.clone();
            Log.d(TAG, "do 1" );

            int i;
            for( i = 0; i < MAX_DICE; i++ ) {
                if( warSign == 0 ) {
                    dice[0][i].state = Dice.DICE_STATE_CUP;
                    dice[0][i].setNum((int)( Math.random() * 6 ) + 1 );
                } else {
                    dice[0][i].state = Dice.DICE_STATE_SHAKING;
                    new Thread( new shakeThread()).start();
                }
                dice[1][i].state = Dice.DICE_STATE_DICE;
            }
            Log.d(TAG, "create dice" );

            int times = 0;
            while( true ){
                if( myRunSerialNo != runSerialNo )
                    return;
                for( i = 0; i < MAX_DICE; i++ )
                    dice[1][i].setNum((int)( Math.random() * 6 ) + 1 );
                updateHandler.post( refreshRunnable );
                if( times++ >= 12 )
                    break;
                try {
                    Thread.sleep( 60 );
                } catch( Exception e ) {
                }
            }
            if( warSign == 1 ) {
                int cmd = CMD_SET_DICE;
                int param0 = ( dice[1][0].getNum() - 1 ) * 36 + ( dice[1][1].getNum() - 1 ) * 6 + dice[1][2].getNum() - 1;
                int param1 = ( dice[1][3].getNum() - 1 ) * 6 + dice[1][4].getNum() - 1;
//                if( warServer )
//                    for( int phondNo = 0; phondNo < phoneSum; phondNo++ )
//                        sendMsg( phondNo, (char)cmd, (char)param0, (char)param1, (char)0 );
//                else
//                    sendMsg( serverPhoneNo, (char)cmd, (char)param0, (char)param1, (char)0 );
            } else if( warSign == 2 ) {
                Log.d(TAG, "send diceset" );
                byte[] cmd = new byte[21];
                cmd[0] = cmd[1]= 0;
                cmd[2] = (byte)( SUB_CMD_INTELLIGENCE_GAME_WAR_MSG / 256 );
                cmd[3] = (byte)( SUB_CMD_INTELLIGENCE_GAME_WAR_MSG % 256 );
                cmd[4] = (byte)( warNo / ( 256 * 256 * 256 ));
                cmd[5] = (byte)(( warNo / ( 256 * 256 )) % 256 );
                cmd[6] = (byte)(( warNo / 256 ) % 256 );
                cmd[7] = (byte)( warNo % 256 );
                cmd[8] = (byte)( warPhoneId / ( 256 * 256 * 256 ));
                cmd[9] = (byte)(( warPhoneId / ( 256 * 256 )) % 256 );
                cmd[10] = (byte)(( warPhoneId / 256 ) % 256 );
                cmd[11] = (byte)( warPhoneId % 256 );
                cmd[12] = (byte)( WAR_CMD_DICE_SET_DICE / ( 256 * 256 * 256 ));
                cmd[13] = (byte)(( WAR_CMD_DICE_SET_DICE / ( 256 * 256 )) % 256 );
                cmd[14] = (byte)(( WAR_CMD_DICE_SET_DICE / 256 ) % 256 );
                cmd[15] = (byte)( WAR_CMD_DICE_SET_DICE % 256 );
                cmd[16] = (byte)dice[1][0].getNum();
                cmd[17] = (byte)dice[1][1].getNum();
                cmd[18] = (byte)dice[1][2].getNum();
                cmd[19] = (byte)dice[1][3].getNum();
                cmd[20] = (byte)dice[1][4].getNum();
            }
            Log.d(TAG, "waiting yourDiceSet" );
            while( warSign != 0 && !yourDiceSetted ) {
                if( myRunSerialNo != runSerialNo ) {
                    Log.d(TAG, "wait other side return" );
                    return;
                }
                try {
                    Thread.sleep( 60 );
                } catch( Exception e ) {
                }
            }
            if( myRunSerialNo != runSerialNo )
                return;
            if( warSign == 1 ) {
                for( i = 0; i < MAX_DICE; i++ )
                    dice[0][i].state = Dice.DICE_STATE_CUP;
                dice[0][0].setNum((int)getDiceValue[0] / 36 + 1 );
                dice[0][1].setNum(((int)getDiceValue[0] / 6 ) % 6 + 1 );
                dice[0][2].setNum((int)getDiceValue[0] % 6 + 1 );
                dice[0][3].setNum(((int)getDiceValue[1] / 6 ) % 6 + 1 );
                dice[0][4].setNum((int)getDiceValue[1] % 6 + 1 );
                getDiceValue[1] = (char)-1;
            } else if( warSign == 2 ) {
                for( i = 0; i < MAX_DICE; i++ )
                    dice[0][i].state = Dice.DICE_STATE_CUP;
            }

            Log.d(TAG, "warSign = " + warSign + ", isFirstFocus = " + isFirstFocus );
            if( warSign != 0 ) {
                if( isFirstFocus )
                    focusPerson = 1;
                else
                    focusPerson = 0;
            } else
                focusPerson = 0;
            updateHandler.post( refreshRunnable );

            while( true ) {
                if( warSign == 0 ) {
                    updateHandler.post( refreshRunnable );
                    try{
                        Thread.sleep( 1000 );
                    } catch( Exception e ) {
                    }
                    if( myRunSerialNo != runSerialNo )
                        return;
                    thinker.say();
                    haveSaid = true;
                    if( mpClick != null )
                        mpClick.start();
                    if( thinker.sayValue.count > 100 || thinker.sayValue.num == 1 )
                        isPure = true;
                    if( thinker.sayValue.type == SayValue.SAY_VALUE_TYPE_OPEN )
                        break;
                    focusPerson = 1;
                    updateHandler.post( refreshRunnable );
                } else {
                    if( focusPerson == 0 ) {
                        yourSayValue.type = SayValue.SAY_VALUE_TYPE_SAY;
                        yourSayValue.count = yourSayValue.num = -1;
                        updateHandler.post( refreshRunnable );
                        while( myRunSerialNo == runSerialNo && yourSayValue.type == SayValue.SAY_VALUE_TYPE_SAY && yourSayValue.num < 0 ) {
                            try{
                                Thread.sleep( 500 );
                            } catch( Exception e ) {
                            }
                        }
                        if( myRunSerialNo != runSerialNo )
                            return;
                        haveSaid = true;
                        if( yourSayValue.type == SayValue.SAY_VALUE_TYPE_OPEN )
                            break;
                        if( yourSayValue.count > 100 || yourSayValue.num == 1 )
                            isPure = true;
                        focusPerson = 1;
                        updateHandler.post( refreshRunnable );
                    }
                }

                mySayValue.type = SayValue.SAY_VALUE_TYPE_SAY;
                mySayValue.count = mySayValue.num = -1;
                currTrace = 0;
                sayValueTrace[currTrace] = mySayValue.clone();

                while( myRunSerialNo == runSerialNo && mySayValue.type == SayValue.SAY_VALUE_TYPE_SAY && mySayValue.num <= 0 ) {
                    try{
                        Thread.sleep( 500 );
                    } catch( Exception e ) {
                    }
                }
                if( myRunSerialNo != runSerialNo )
                    return;
                haveSaid = true;
                if( mySayValue.count > 100 || mySayValue.num == 1 )
                    isPure = true;
                if( mySayValue.type == SayValue.SAY_VALUE_TYPE_OPEN )
                    break;

                if( warSign == 0 )
                    thinker.setBase( mySayValue.count, mySayValue.num, isPure );
                focusPerson = 0;
            }

            for( i = 0; i < MAX_DICE; i++ )
                dice[0][i].state = Dice.DICE_STATE_DICE;
            updateHandler.post( refreshRunnable );

            try{
                Thread.sleep( 1000 );
            } catch( Exception e ) {
            }
            if( myRunSerialNo != runSerialNo )
                return;

            int cnt, num;
            if( mySayValue.type == SayValue.SAY_VALUE_TYPE_OPEN ) {
                if( warSign == 0 )
                    num = thinker.sayValue.num;
                else
                    num = yourSayValue.num;
            } else
                num = mySayValue.num;

            cnt = 0;
            for( i = 0; i < MAX_DICE; i++ ) {
                if( dice[0][i].getNum() == num )
                    cnt++;
                else if( !isPure && dice[0][i].getNum() == 1 )
                    cnt++;
                if( dice[1][i].getNum() == num )
                    cnt++;
                else if( !isPure && dice[1][i].getNum() == 1 )
                    cnt++;
            }

            boolean win;
            if(( warSign == 0 && thinker.sayValue.type == SayValue.SAY_VALUE_TYPE_OPEN ) || ( warSign != 0 && yourSayValue.type == SayValue.SAY_VALUE_TYPE_OPEN )) {
                if( cnt >= mySayValue.count % 100 )
                    win = true;
                else
                    win = false;
            } else {
                if(( warSign == 0 && cnt >= thinker.sayValue.count % 100 ) || ( warSign != 0 && cnt >= yourSayValue.count % 100 ))
                    win = false;
                else
                    win = true;
            }

            if( win ) {
                resultState = 1;
                myScoreView.addValue();
            } else {
                resultState = -1;
                yourScoreView.addValue();
            }
            updateHandler.post( refreshRunnable );
            if( warSign != 0 ) {
                isFirstFocus = !isFirstFocus;
                yourDiceSetted = false;
            }
            gameRunning = false;
        }
    }

    final Handler updateHandler = new Handler();
    final Runnable refreshRunnable = new Runnable() {
        public void run() {
            int i, j;
            for( i = 0; i < MAX_PERSON; i++ )
                for( j = 0; j < MAX_DICE; j++ ) {
                    dice[i][j].invalidate();
                }

            for( i = 0; i < MAX_PERSON; i++ ) {
                if( focusPerson == i ) {
                    imageSide[i].setVisibility( View.VISIBLE );
                    imageGray[i].setVisibility( View.INVISIBLE );
                } else {
                    imageSide[i].setVisibility( View.INVISIBLE );
                    imageGray[i].setVisibility( View.VISIBLE );
                }
            }

            System.out.println( "dice[0][0].state = " + dice[0][0].state );
            if( dice[0][0].state == Dice.DICE_STATE_CUP || dice[0][0].state == Dice.DICE_STATE_SHAKING ) {
                diceCup.setVisibility( View.VISIBLE );
                frame.setVisibility( View.INVISIBLE );
            } else {
                diceCup.setVisibility( View.INVISIBLE );
                frame.setVisibility( View.VISIBLE );
            }

            for( i = 0; i < MAX_PERSON; i++ ) {
/*				if( focusPerson == i )
					ivFocus[i].setVisibility( View.VISIBLE );
				else
					ivFocus[i].setVisibility( View.INVISIBLE );*/
            }
            String s;
            if( warSign == 0 && thinker != null && thinker.sayValue != null && thinker.sayValue.type == SayValue.SAY_VALUE_TYPE_SAY ) {
                if( thinker.sayValue.count < 100 )
                    s = Integer.toString( thinker.sayValue.count ) + "��" + Integer.toString( thinker.sayValue.num );
                else
                    s = Integer.toString( thinker.sayValue.count - 100 ) + "����" + Integer.toString( thinker.sayValue.num );
            } else if( warSign != 0 && yourSayValue != null && yourSayValue.type == SayValue.SAY_VALUE_TYPE_SAY && yourSayValue.count > 0 ) {
                if( yourSayValue.count < 100 )
                    s = Integer.toString( yourSayValue.count ) + "��" + Integer.toString( yourSayValue.num );
                else
                    s = Integer.toString( yourSayValue.count - 100 ) + "����" + Integer.toString( yourSayValue.num );
            } else
                s = "";
            if( !s.equals( "" )) {
                mySayView.setVisibility( View.INVISIBLE );
                yourSayView.setVisibility( View.VISIBLE );
                yourSayView.setValue( s );
                yourSayView.invalidate();
            } else {
                yourSayView.setVisibility( View.INVISIBLE );
            }
            if( mySayValue != null && mySayValue.type == SayValue.SAY_VALUE_TYPE_SAY && mySayValue.count > 0 ) {
                s = Integer.toString( mySayValue.count % 100 );
                if( mySayValue.num > -1 ) {
                    if( mySayValue.count < 100 )
                        s += "��";
                    else
                        s += "����";
                }
                if( mySayValue.num > 0 )
                    s += Integer.toString( mySayValue.num );
            } else {
                s = "";
            }
            if( !s.equals( "" )) {
                yourSayView.setVisibility( View.INVISIBLE );
                mySayView.setVisibility( View.VISIBLE );
                mySayView.setValue( s );
                mySayView.invalidate();
            } else {
                mySayView.setVisibility( View.INVISIBLE );
            }

            switch( resultState ) {
                case 0:
                    imageWin.setVisibility( View.INVISIBLE );
                    imageLose.setVisibility( View.INVISIBLE );
                    break;
                case 1:
                    imageWin.setVisibility( View.VISIBLE );
                    imageLose.setVisibility( View.INVISIBLE );
                    break;
                case -1:
                    imageWin.setVisibility( View.INVISIBLE );
                    imageLose.setVisibility( View.VISIBLE );
                    break;
                default:
                    break;
            }
        }
    };

    private String changeToString( int num ) {
        String s;
        switch( num ) {
            case 0: s = "zero"; break;
            case 1: s = "one"; break;
            case 2: s = "two"; break;
            case 3: s = "three"; break;
            case 4: s = "four"; break;
            case 5: s = "five"; break;
            case 6: s = "six"; break;
            case 7: s = "seven"; break;
            case 8: s = "eight"; break;
            case 9: s = "nine"; break;
            case 10: s = "ten"; break;
            case 11: s = "eleven"; break;
            case 12: s = "twelve"; break;
            default: s = ""; break;
        }
        return s;
    }

    public boolean onTouch(View v, MotionEvent event) {
        touchView = v;
        return gd.onTouchEvent( event );
    }

    public void recvMsgCallback( int phoneId, char cmd, char param1, char param2, char param3 ) {
        Log.d(TAG, Integer.toString( (int)cmd ) + ", " + Integer.toString( (int)param1 ) + ", " + Integer.toString( (int)param2 ) + ", " + Integer.toString( (int)param3 ));
        int i;
        if( cmd == CMD_SET_DICE ) {
            getDiceValue[0] = param1;
            getDiceValue[1] = param2;
            yourDiceSetted = true;
        } else if( cmd == CMD_SAY ) {
            yourSayValue.type = (int)param1;
            yourSayValue.count = (int)param2;
            yourSayValue.num = (int)param3;
            if( mpClick != null )
                mpClick.start();
        }
    }

    private class shakeThread implements Runnable {
        public void run() {
            while( true ) {
                try {
                    Thread.sleep( 800 );
                } catch( Exception e ) {
                }
                if( dice[0][0].state == Dice.DICE_STATE_SHAKING ) {
					/*for( int i = 0; i < MAX_DICE; i++ )
						dice[0][i].shake();*/
                } else
                    break;
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((keyCode == KeyEvent.KEYCODE_BACK) ||
                (keyCode == KeyEvent.KEYCODE_HOME))
                && event.getRepeatCount() == 0) {
            if( warSign != 0 ) {
                dialog_Exit(this);
                return false;
            }
        }
        return super.onKeyDown( keyCode, event );
    }

    public void dialog_Exit(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage( "���Ҫ�뿪˫�˶�ս��??" );
        builder.setTitle( "��ʾ" );
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton( "ȷ��",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        builder.setNegativeButton( "ȡ��",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu( android.view.Menu menu ) {
        super.onCreateOptionsMenu( menu );
        menu.add( "ѡ��" ).setIcon( android.R.drawable.ic_menu_preferences );
        menu.add( "����" ).setIcon( android.R.drawable.ic_menu_info_details );
        return true;
    }

    public boolean onOptionsItemSelected( MenuItem item ) {
    	/*if( item.getTitle().equals( "ѡ��" ))
			startActivity( new Intent( getApplicationContext(), com.joydin.intelligencegame.Options.class ));
    	else if( item.getTitle().equals( getString( R.string.About )))
    		startActivity( new Intent( this, com.joydin.intelligencegame.ConnectUs.class ));*/
        return super.onOptionsItemSelected( item );
    }

    public void handleNotifyMsg( int subCmd, byte[]msg, int point ) {
        Log.d(TAG, "handleNotifyMsg len = " + msg.length + ", point = " + point );
        switch( subCmd ) {
            case WAR_CMD_DICE_NOTIFY_DICE_SET:
                dice[0][0].setNum((int)msg[point] );
                dice[0][1].setNum((int)msg[point + 1] );
                dice[0][2].setNum((int)msg[point + 2] );
                dice[0][3].setNum((int)msg[point + 3] );
                dice[0][4].setNum((int)msg[point + 4] );
                yourDiceSetted = true;
                Log.d(TAG, "set ok" );
                break;
            case WAR_CMD_DICE_NOTIFY_SAY:
                yourSayValue.type = (int)msg[point];
                yourSayValue.count = (int)msg[point + 1];
                yourSayValue.num = (int)msg[point + 2];
                if( mpClick != null )
                    mpClick.start();
                break;
            default:
                break;
        }
    }

    private class keyPressThread extends Thread {
        int keyNo;
        public keyPressThread( int theKeyNo ) {
            keyNo = theKeyNo;
        }
        public void run() {
            try {
                Thread.sleep( 50 );
            } catch( Exception e ) {
            }
            keyView[keyNo].setPress( false );

            if( keyNo < 10 ) {
                if( focusPerson != 1 ) {
                    Looper.prepare();
                    Toast.makeText( DiceActivity.this, "��û�ֵ���", Toast.LENGTH_SHORT ).show();
                    Looper.loop();
                    return;
                }
                if( mySayValue.count < 0 && keyNo != 0 )
                    mySayValue.count = keyNo;
                else if( mySayValue.num < 0 && mySayValue.count >= 0 && mySayValue.count < 10 )
                    mySayValue.count = mySayValue.count * 10 + keyNo;
                else if( mySayValue.num == 0 ) {
                    if( keyNo == 0 || keyNo > 6 ) {
                        Looper.prepare();
                        Toast.makeText( DiceActivity.this, "�������?", Toast.LENGTH_SHORT ).show();
                        Looper.loop();
                        return;
                    }
                    boolean checkOk = false;
                    if( warSign == 0 ) {
                        if( mySayValue.count / 100 >= thinker.sayValue.count / 100 && mySayValue.count % 100 > thinker.sayValue.count % 100 )
                            checkOk = true;
                        else if( mySayValue.count / 100 >= thinker.sayValue.count / 100 && mySayValue.count % 100 == thinker.sayValue.count % 100 && (( thinker.sayValue.num > 1 && keyNo > thinker.sayValue.num ) || ( thinker.sayValue.num != 1 && keyNo == 1 )))
                            checkOk = true;
                        else if( mySayValue.count / 100 > thinker.sayValue.count / 100 && mySayValue.count % 100 == thinker.sayValue.count % 100 && keyNo == thinker.sayValue.num && keyNo != 1 )
                            checkOk = true;
                    } else {
                        if( mySayValue.count / 100 >= yourSayValue.count / 100 && mySayValue.count % 100 > yourSayValue.count % 100 )
                            checkOk = true;
                        else if( mySayValue.count / 100 >= yourSayValue.count / 100 && mySayValue.count % 100 == yourSayValue.count % 100 && (( yourSayValue.num > 1 && keyNo > yourSayValue.num ) || ( yourSayValue.num != 1 && keyNo == 1 )))
                            checkOk = true;
                        else if( mySayValue.count / 100 > yourSayValue.count / 100 && mySayValue.count % 100 == yourSayValue.count % 100 && keyNo == yourSayValue.num && keyNo != 1 )
                            checkOk = true;
                    }
                    if( !checkOk ) {
                        Looper.prepare();
                        Toast.makeText( DiceActivity.this, "�������?", Toast.LENGTH_SHORT ).show();
                        Looper.loop();
                        return;
                    }
                    mySayValue.num = keyNo;
//                    if( warSign == 1 ) {
//                        if( warServer )
//                            for( keyNo = 0; keyNo < phoneSum; keyNo++ )
//                                sendMsg( keyNo, CMD_SAY, (char)mySayValue.type, (char)mySayValue.count, (char)mySayValue.num );
//                        else
//                            sendMsg( serverPhoneNo, CMD_SAY, (char)mySayValue.type, (char)mySayValue.count, (char)mySayValue.num );
//                    } else if( warSign == 2 ) {
                        byte[] cmd = new byte[19];
                        cmd[0] = cmd[1]= 0;
                        cmd[2] = (byte)( SUB_CMD_INTELLIGENCE_GAME_WAR_MSG / 256 );
                        cmd[3] = (byte)( SUB_CMD_INTELLIGENCE_GAME_WAR_MSG % 256 );
                        cmd[4] = (byte)( warNo / ( 256 * 256 * 256 ));
                        cmd[5] = (byte)(( warNo / ( 256 * 256 )) % 256 );
                        cmd[6] = (byte)(( warNo / 256 ) % 256 );
                        cmd[7] = (byte)( warNo % 256 );
                        cmd[8] = (byte)( warPhoneId / ( 256 * 256 * 256 ));
                        cmd[9] = (byte)(( warPhoneId / ( 256 * 256 )) % 256 );
                        cmd[10] = (byte)(( warPhoneId / 256 ) % 256 );
                        cmd[11] = (byte)( warPhoneId % 256 );
                        cmd[12] = (byte)( WAR_CMD_DICE_SAY / ( 256 * 256 * 256 ));
                        cmd[13] = (byte)(( WAR_CMD_DICE_SAY / ( 256 * 256 )) % 256 );
                        cmd[14] = (byte)(( WAR_CMD_DICE_SAY / 256 ) % 256 );
                        cmd[15] = (byte)( WAR_CMD_DICE_SAY % 256 );
                        cmd[16] = (byte)mySayValue.type;
                        cmd[17] = (byte)mySayValue.count;
                        cmd[18] = (byte)mySayValue.num;
//                    }
                } else {
                    Looper.prepare();
                    Toast.makeText( DiceActivity.this, "�������?", Toast.LENGTH_SHORT ).show();
                    Looper.loop();
                    return;
                }
                if( currTrace < MAX_TRACE - 1 ) {
                    currTrace++;
                    sayValueTrace[currTrace] = mySayValue.clone();
                }
                updateHandler.post( refreshRunnable );
            } else if( keyNo == 10 ) {
                if( mySayValue.count > 0 && mySayValue.num < 0 ) {
                    mySayValue.num = 0;
                    if( isPure )
                        mySayValue.count += 100;
                    if( currTrace < MAX_TRACE - 1 ) {
                        currTrace++;
                        sayValueTrace[currTrace] = mySayValue.clone();
                    }
                    updateHandler.post( refreshRunnable );
                } else {
                    Looper.prepare();
                    Toast.makeText( DiceActivity.this, "�������?", Toast.LENGTH_SHORT ).show();
                    Looper.loop();
                }
            } else if( keyNo == 11 ) {
                if( mySayValue.num == 0 && mySayValue.count < 100 ) {
                    mySayValue.count += 100;
                    updateHandler.post( refreshRunnable );
                } else {
                    Looper.prepare();
                    Toast.makeText( DiceActivity.this, "�������?", Toast.LENGTH_SHORT ).show();
                    Looper.loop();
                }
            } else if( keyNo == 12 ) {
                if( !haveSaid ) {
                    Looper.prepare();
                    Toast.makeText( DiceActivity.this, "��û���������ܿ���", Toast.LENGTH_SHORT ).show();
                    Looper.loop();
                    return;
                }
                mySayValue.type = SayValue.SAY_VALUE_TYPE_OPEN;
//                if( warSign == 1 ) {
//                    if( warServer )
//                        for( keyNo = 0; keyNo < phoneSum; keyNo++ )
//                            sendMsg( keyNo, CMD_SAY, (char)mySayValue.type, (char)mySayValue.count, (char)mySayValue.num );
//                    else
//                        sendMsg( serverPhoneNo, CMD_SAY, (char)mySayValue.type, (char)mySayValue.count, (char)mySayValue.num );
//                } else if( warSign == 2 ) {
                    byte[] cmd = new byte[19];
                    cmd[0] = cmd[1]= 0;
                    cmd[2] = (byte)( SUB_CMD_INTELLIGENCE_GAME_WAR_MSG / 256 );
                    cmd[3] = (byte)( SUB_CMD_INTELLIGENCE_GAME_WAR_MSG % 256 );
                    cmd[4] = (byte)( warNo / ( 256 * 256 * 256 ));
                    cmd[5] = (byte)(( warNo / ( 256 * 256 )) % 256 );
                    cmd[6] = (byte)(( warNo / 256 ) % 256 );
                    cmd[7] = (byte)( warNo % 256 );
                    cmd[8] = (byte)( warPhoneId / ( 256 * 256 * 256 ));
                    cmd[9] = (byte)(( warPhoneId / ( 256 * 256 )) % 256 );
                    cmd[10] = (byte)(( warPhoneId / 256 ) % 256 );
                    cmd[11] = (byte)( warPhoneId % 256 );
                    cmd[12] = (byte)( WAR_CMD_DICE_SAY / ( 256 * 256 * 256 ));
                    cmd[13] = (byte)(( WAR_CMD_DICE_SAY / ( 256 * 256 )) % 256 );
                    cmd[14] = (byte)(( WAR_CMD_DICE_SAY / 256 ) % 256 );
                    cmd[15] = (byte)( WAR_CMD_DICE_SAY % 256 );
                    cmd[16] = (byte)mySayValue.type;
                    cmd[17] = (byte)mySayValue.count;
                    cmd[18] = (byte)mySayValue.num;
//                }
            } else if( keyNo == 13 ) {
                if( currTrace > 0 && mySayValue.type == SayValue.SAY_VALUE_TYPE_SAY && mySayValue.num <= 0 ) {
                    currTrace--;
                    mySayValue = sayValueTrace[currTrace].clone();
                    updateHandler.post( refreshRunnable );
                } else if( !( mySayValue.type == SayValue.SAY_VALUE_TYPE_SAY && mySayValue.num <= 0 )) {
                    Looper.prepare();
                    Toast.makeText( DiceActivity.this, "�޷�����", Toast.LENGTH_SHORT ).show();
                    Looper.loop();
                } else {
                    Looper.prepare();
                    Toast.makeText( DiceActivity.this, "�Ѿ�������", Toast.LENGTH_SHORT ).show();
                    Looper.loop();
                }
            } else if( keyNo == 14 ) {
                if( !gameRunning )
                    new Thread( new runGame()).start();
                else {
                    Looper.prepare();
                    Toast.makeText( DiceActivity.this, "���ڲ���ҡ����", Toast.LENGTH_SHORT ).show();
                    Looper.loop();
                }
            }
        }
    }
}
