package com.jackson_siro.mpango;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.GestureDetector.OnGestureListener;

public class BbWelcome extends AppCompatActivity {
    private WelcomeView welcomeView;
    private boolean end = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        welcomeView = new WelcomeView(this, null);
        setContentView(welcomeView);
    }

    private void doPlay() {
        this.startActivity(new Intent(this, CcStart.class));
        end = false;
        this.finish();
    }

    private void doSetting() {
        this.startActivity(new Intent(this, GgSettings.class));
        end = false;
        this.finish();
    }

    private void doHelp() {
        Intent startPlay = new Intent(this, FfHelp.class);
        this.startActivity(startPlay);
        end = false;
        this.finish();
    }

    private void showHighScores() {
        Intent startPlay = new Intent(this, EeScores.class);
        this.startActivity(startPlay);
        end = false;
        this.finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_MENU == keyCode) {
            return false;
        } else if (KeyEvent.KEYCODE_BACK == keyCode) {
            end = true;
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (end) {
            System.exit(0);
        }
    }

    public class WelcomeView extends SurfaceView implements OnGestureListener, SurfaceHolder.Callback {
        private GestureDetector mGestureDetector;
        private SurfaceHolder holder;

        private Bitmap backgroundBmp;
        private Bitmap playBmp;
        private Bitmap settingBmp;
        private Bitmap highBmp;
        private Bitmap helpBmp;
        private Bitmap logoBmp;

        private int WIDTH = 480;
        private int HEIGHT = 854;

        private int LEFT = 0;
        private int TOP = 0;

        public WelcomeView(Context context, AttributeSet attrs) {
            super(context, attrs);
            mGestureDetector = new GestureDetector(this);
            setFocusable(true);
            setLongClickable(true);
            holder = getHolder();
            holder.addCallback(this);

            DisplayMetrics dm = new DisplayMetrics();
            dm = getApplicationContext().getResources().getDisplayMetrics();
            WIDTH = dm.widthPixels;
            HEIGHT = dm.heightPixels;
            backgroundBmp = Mpango3D.scaleBitmap(context, R.drawable.nairobicity, WIDTH, HEIGHT);
            logoBmp = Mpango3D.scaleBitmap(context, R.drawable.appicon, 0.7f * WIDTH, 0.3f * HEIGHT);
            playBmp = Mpango3D.scaleBitmap(context, R.drawable.play, 0.65f * WIDTH, 0.14f * HEIGHT);
            settingBmp = Mpango3D.scaleBitmap(context, R.drawable.settings, 0.65f * WIDTH, 0.14f * HEIGHT);
            highBmp = Mpango3D.scaleBitmap(context, R.drawable.highscores, 0.65f * WIDTH, 0.14f * HEIGHT);
            helpBmp = Mpango3D.scaleBitmap(context, R.drawable.help, 0.65f * WIDTH, 0.14f * HEIGHT);

            SharedPreferences sp = getSharedPreferences(  "com.jackson_siro.mpango_preferences", Context.MODE_PRIVATE);
            Mpango3D.HELP = sp.getBoolean("help", true);
            Mpango3D.KEYS = sp.getBoolean("keys", true);
            Mpango3D.SOUND = sp.getBoolean("sound", true);
            Mpango3D.KEYSPOS = sp.getBoolean("keys' position", true);
            Mpango3D.SOUND = sp.getBoolean("sound", true);
        }

        void drawFrame() {
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.translate(-LEFT, -TOP);
                Paint paint = new Paint();
                paint.setStyle(Style.FILL);
                canvas.drawBitmap(backgroundBmp, null, new RectF(LEFT, TOP, WIDTH + LEFT, HEIGHT + TOP), paint);
                float yPos = 41 + TOP;
                canvas.drawBitmap(logoBmp, (WIDTH - logoBmp.getWidth()) * 0.5f + LEFT, 41 + TOP, paint);
                float xPos = (WIDTH - playBmp.getWidth()) * 0.5f + LEFT;
                yPos = logoBmp.getHeight() + playBmp.getHeight() * 0.04f + 41 + TOP;
                canvas.drawBitmap(playBmp, xPos, yPos, paint);
                yPos += settingBmp.getHeight() * 1.04f;
                canvas.drawBitmap(settingBmp, xPos, yPos, paint);
                yPos += settingBmp.getHeight() * 1.04f;
                canvas.drawBitmap(highBmp, xPos, yPos, paint);
                yPos += settingBmp.getHeight() * 1.04f;
                canvas.drawBitmap(helpBmp, xPos, yPos, paint);
                holder.unlockCanvasAndPost(canvas);
            }
        }

        public boolean onTouchEvent(MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return super.onTouchEvent(event);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            float x = e.getX() + LEFT;
            float y = e.getY() + TOP;
            if (x >= (WIDTH - playBmp.getWidth()) * 0.5f + LEFT && x <= (WIDTH + playBmp.getWidth()) * 0.5f + LEFT) {
                if (y >= logoBmp.getHeight() + playBmp.getHeight() * 0.04f + 41 + TOP && y <= logoBmp.getHeight() + playBmp.getHeight() * 1.04f + 41 + TOP) {
                    drawFrame();
                    doPlay();
                    return false;
                } else if (y >= logoBmp.getHeight() + playBmp.getHeight() * 1.08f + 41 + TOP && y <= logoBmp.getHeight() + playBmp.getHeight() * 2.08f + 41 + TOP) {
                    drawFrame();
                    doSetting();
                } else if (y >= logoBmp.getHeight() + playBmp.getHeight() * 2.12f + 41 + TOP && y <= logoBmp.getHeight() + playBmp.getHeight() * 3.12f + 41 + TOP) {
                    drawFrame();
                    showHighScores();
                } else if (y >= logoBmp.getHeight() + playBmp.getHeight() * 3.16f + 41 + TOP && y <= logoBmp.getHeight() + playBmp.getHeight() * 4.16f + 41 + TOP) {
                    drawFrame();
                    doHelp();
                }
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            drawFrame();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }

        protected void onMpango3DurationChanged(Configuration config) {
        }
    }
}
