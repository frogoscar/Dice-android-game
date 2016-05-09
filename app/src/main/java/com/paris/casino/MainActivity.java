package com.paris.casino;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getName();

    private View clickedView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View.OnClickListener clickListener = new View.OnClickListener() {
            public void onClick(View v) {
                clickedView = v;
//                if( v.equals((View)findViewById( R.id.ImageSingle )))
//                    ((ImageView)findViewById( R.id.ImageSingle )).setBackgroundResource( R.drawable.single_press );
//                else if( v.equals((View)findViewById( R.id.ImageHelp )))
//                    ((ImageView)findViewById( R.id.ImageHelp )).setBackgroundResource( R.drawable.help_press );
//                else if( v.equals((View)findViewById( R.id.ImageExit )))
//                    ((ImageView)findViewById( R.id.ImageExit )).setBackgroundResource( R.drawable.exit_press );
                (new clickViewThread()).start();
            }
        };

        findViewById(R.id.ImageSingle).setOnClickListener(clickListener);
        findViewById(R.id.ImageHelp).setOnClickListener(clickListener);
        findViewById(R.id.ImageExit).setOnClickListener(clickListener);

        File newDir = new File(Environment.getExternalStorageDirectory().toString() + "/dice/");
        if (!newDir.exists())
            newDir.mkdir();
        newDir = new File(Environment.getExternalStorageDirectory().toString() + "/dice/work/");
        if (!newDir.exists())
            newDir.mkdir();
        newDir = new File(Environment.getExternalStorageDirectory().toString() + "/dice/temp/");
        if (!newDir.exists())
            newDir.mkdir();

        File audioFile = new File(Environment.getExternalStorageDirectory().toString() + "/dice/work/click.wav");
        if (!audioFile.exists()) {
            try {
                audioFile = new File(Environment.getExternalStorageDirectory().toString() + "/dice/work/click2.wav");
                audioFile.createNewFile();
                FileOutputStream out = new FileOutputStream(audioFile);
                InputStream inputStream = getResources().openRawResource(R.raw.click);
                byte[] reader = new byte[10000];
                int len;
                while ((len = inputStream.read(reader)) != -1) {
                    out.write(reader, 0, len);
                }
                inputStream.close();
                out.close();
                audioFile.renameTo(new File(Environment.getExternalStorageDirectory().toString() + "/dice/work/click.wav"));
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        (new updateViewThread()).start();
    }

    private class clickViewThread extends Thread {
        public void run() {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
            if (clickedView.equals((View) findViewById(R.id.ImageSingle))) {
                Intent newIntent = new Intent(MainActivity.this, DiceActivity.class);
                newIntent.putExtra("warsign", "0");
                startActivity(newIntent);
            } else if (clickedView.equals((View) findViewById(R.id.ImageHelp))) {
                startActivity(new Intent(getApplicationContext(), Description.class));
            } else if (clickedView.equals((View) findViewById(R.id.ImageExit))) {
                finish();
                return;
            }
            updateHandler.post(restoreViewRunnable);
        }
    }

    private class updateViewThread extends Thread {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                if (rect.top != 0)
                    break;
            }
            updateHandler.post(updateViewRunnable);
        }
    }

    final Handler updateHandler = new Handler();
    final Runnable updateViewRunnable = new Runnable() {
        public void run() {
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenWidth = rect.right - rect.left;
            int screenHeight = rect.bottom - rect.top;

            AbsoluteLayout.LayoutParams lp;
            lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
            lp.x = screenWidth * 160 / 640;
            lp.y = screenHeight * 491 / 960;
            lp.width = screenWidth * 295 / 640;
            lp.height = screenHeight * 68 / 960;
            findViewById(R.id.ImageSingle).setLayoutParams(lp);

            lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
            lp.x = screenWidth * 160 / 640;
            lp.y = screenHeight * 595 / 960;
            lp.width = screenWidth * 295 / 640;
            lp.height = screenHeight * 68 / 960;
            findViewById(R.id.ImageHelp).setLayoutParams(lp);

            lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
            lp.x = screenWidth * 160 / 640;
            lp.y = screenHeight * 699 / 960;
            lp.width = screenWidth * 295 / 640;
            lp.height = screenHeight * 68 / 960;
            findViewById(R.id.ImageExit).setLayoutParams(lp);

//            lp = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0);
//            lp.x = screenWidth * 172 / 640;
//            lp.y = screenHeight * 804 / 960;
//            lp.width = screenWidth * 295 / 640;
//            lp.height = screenHeight * 68 / 960;
//            findViewById( R.id.ImageExit ).setLayoutParams(lp);
        }
    };

    final Runnable restoreViewRunnable = new Runnable() {
        public void run() {
            if (clickedView.equals((View) findViewById(R.id.ImageSingle)))
                ((ImageView) findViewById(R.id.ImageSingle)).setBackgroundResource(R.drawable.single);
            else if (clickedView.equals((View) findViewById(R.id.ImageHelp)))
                ((ImageView) findViewById(R.id.ImageHelp)).setBackgroundResource(R.drawable.help);
            else if (clickedView.equals((View) findViewById(R.id.ImageExit)))
                ((ImageView) findViewById(R.id.ImageExit)).setBackgroundResource(R.drawable.exit);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == 0) {
            Bundle bundle = data.getExtras();
            String warType = bundle.getString("wartype");
            if (warType == null)
                Log.d(TAG, "warType is null");
            else
                Log.d(TAG, "warType is " + warType);
            Intent newIntent = new Intent(MainActivity.this, DiceActivity.class);
            if (warType.equals("wan")) {
                newIntent.putExtra("warsign", "2");
                newIntent.putExtra("warno", bundle.getString("warno"));
                newIntent.putExtra("warphoneid", bundle.getString("warphoneid"));
                newIntent.putExtra("phonesum", bundle.getString("phonesum"));
            } else {
                newIntent.putExtra("warsign", "1");
                newIntent.putExtra("server", bundle.getString("server"));
                newIntent.putExtra("serverphoneno", bundle.getString("serverphoneno"));
                newIntent.putExtra("phonesum", bundle.getString("phonesum"));
            }
            startActivity(newIntent);
        }
    }
}
