package com.jackson_siro.mpango;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.*;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.GestureDetector.OnGestureListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class GgSettings extends AppCompatActivity {

    private SettingView settingView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        settingView = new SettingView(this, null);
        root.addView(settingView);

        setContentView(root);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
    }

    private void goWelcome() {
        this.startActivity(new Intent(this, BbWelcome.class));
        this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    public class SettingView extends SurfaceView implements OnGestureListener, SurfaceHolder.Callback {
        private GestureDetector mGestureDetector;
        private SurfaceHolder holder;
        private Bitmap backgroundBmp, mainmenuBmp, boxBmp, selectionBmp, tinyboxBmp, deselectionBmp;
        private int WIDTH = 480, HEIGHT = 800, LEFT = -159, TOP = -183, BASEY;
        private Typeface typeface;

        public SettingView(Context context, AttributeSet attrs) {
            super(context, attrs);

            mGestureDetector = new GestureDetector(this);
            setFocusable(true);
            setLongClickable(true);
            holder = getHolder();
            holder.addCallback(this);
            backgroundBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.nairobicity);

            DisplayMetrics dm;
            dm = getApplicationContext().getResources().getDisplayMetrics();
            WIDTH = dm.widthPixels;
            HEIGHT = dm.heightPixels;
            mainmenuBmp = Mpango3D.scaleBitmap(context, R.drawable.mainmenu, WIDTH * 0.5f, HEIGHT * 0.1f);
            boxBmp = Mpango3D.scaleBitmap(context, R.drawable.largebox, WIDTH * 0.82f, HEIGHT * 0.58f);
            selectionBmp = Mpango3D.scaleBitmap(context, R.drawable.selection, WIDTH * 0.09f, WIDTH * 0.09f);
            deselectionBmp = Mpango3D.scaleBitmap(context, R.drawable.tick, WIDTH * 0.12f, HEIGHT * 0.07f);
            tinyboxBmp = Mpango3D.scaleBitmap(context, R.drawable.tinybox, WIDTH * 0.1f, WIDTH * 0.1f);

            AssetManager am = context.getAssets();
            typeface = Typeface.createFromAsset(am, "ComicSansMS3.ttf");
            TOP = (HEIGHT == 480 ? -138 : HEIGHT == 533 ? -183 : -213);
            BASEY = (HEIGHT == 480 ? 301 : HEIGHT == 533 ? 346 : 376);
        }

        void drawFrame() {
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.translate(-LEFT, -TOP);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Style.FILL);
                paint.setColor(0xff000000);

                canvas.drawBitmap(backgroundBmp, null, new RectF(LEFT, TOP, WIDTH + LEFT, HEIGHT + TOP), paint);
                canvas.drawBitmap(boxBmp, LEFT + (WIDTH - boxBmp.getWidth()) * 0.5f, TOP + (HEIGHT - boxBmp.getHeight()) * 0.5f, paint);
                canvas.drawBitmap(mainmenuBmp, LEFT + (WIDTH - mainmenuBmp.getWidth()) * 0.5f, TOP + HEIGHT - mainmenuBmp.getHeight() - 10, paint);

                paint.setTypeface(typeface);
                paint.setColor(0xffffffff);
                paint.setTextSize(60);
                paint.setFakeBoldText(true);
                FontMetrics fm = paint.getFontMetrics();
                float fontHeight = (float) Math.ceil(fm.descent - fm.ascent);
                float xPos = LEFT + (WIDTH - boxBmp.getWidth()) * 0.7f;
                float yPos = TOP + (HEIGHT - boxBmp.getHeight()) * 0.5f + fontHeight;
                canvas.drawText(getString(R.string.settings), xPos + 10, yPos, paint);
                yPos = TOP + 0.5f * HEIGHT - boxBmp.getHeight() * 0.35f;
                canvas.drawBitmap(tinyboxBmp, xPos + 15, yPos, paint);
                canvas.drawBitmap(tinyboxBmp, xPos + 15, yPos + 0.15f * WIDTH, paint);
                canvas.drawBitmap(tinyboxBmp, xPos + 15, yPos + 0.3f * WIDTH, paint);
                paint.setAlpha(255);

                if (Mpango3D.HELP) canvas.drawBitmap(selectionBmp, xPos + 15, yPos, paint);
                if (Mpango3D.KEYS) canvas.drawBitmap(selectionBmp, xPos + 15, yPos + 0.15f * WIDTH, paint);
                if (Mpango3D.SOUND) canvas.drawBitmap(selectionBmp, xPos + 15, yPos + 0.3f * WIDTH, paint);

                paint.setColor(0xffffffff);
                paint.setTextSize(45);
                paint.getFontMetrics(fm);
                paint.setFakeBoldText(false);
                paint.setStrokeWidth(0);
                fontHeight = (float) Math.ceil(fm.descent - fm.ascent);
                canvas.drawText(getString(R.string.settings1), xPos + 15 + WIDTH * 0.1f + 10, yPos + fontHeight, paint);
                canvas.drawText(getString(R.string.settings2), xPos + 15 + 0.1f * WIDTH + 10, yPos + 0.15f * WIDTH + fontHeight, paint);
                canvas.drawText(getString(R.string.settings3), xPos + 15 + WIDTH * 0.1f + 10, yPos + fontHeight + 0.3f * WIDTH, paint);
                /*
                 * float sFontHeight = fontHeight;
                 *
                 * yPos += (0.15f * WIDTH); paint.setTextSize(19);
                 * paint.getFontMetrics(fm); fontHeight =
                 * (float)Math.ceil(fm.descent - fm.ascent);
                 */
                // canvas.drawText("sound", xPos + 20 + 0.1f * WIDTH, yPos +
                // 0.4f * WIDTH + sFontHeight, paint);
                /*
                 * if(!Mpango3D.KEYS) { paint.setColor(Color.GRAY); }
                 */
                /*
                 * canvas.drawText("Keys' position", xPos + 15, yPos + 0.125f *
                 * WIDTH + fontHeight, paint); canvas.drawText("left", xPos + 20
                 * + 0.1f * WIDTH, yPos + 0.25f * WIDTH + sFontHeight, paint);
                 * canvas.drawText("right", LEFT + 0.55f * WIDTH + 20, yPos +
                 * 0.25f * WIDTH + sFontHeight, paint);
                 */

                /*
                 * paint.setStyle(Style.FILL); paint.setColor(0x33111111);
                 * canvas.drawCircle(xPos + 15 + 0.05f * WIDTH, yPos + 0.3f *
                 * WIDTH, 0.05f * WIDTH, paint); canvas.drawCircle(LEFT + 0.5f *
                 * WIDTH + 15, yPos + 0.3f * WIDTH, 0.05f * WIDTH, paint);
                 *
                 * if(Mpango3D.KEYS && Mpango3D.KEYSPOS) {
                 * paint.setColor(0xff00ff00); canvas.drawCircle(LEFT + 0.5f *
                 * WIDTH + 15, yPos + 0.3f * WIDTH, 0.025f * WIDTH, paint); }
                 * else { paint.setColor(0x44111111); canvas.drawCircle(LEFT +
                 * 0.5f * WIDTH + 15, yPos + 0.3f * WIDTH, 0.025f * WIDTH,
                 * paint); } if(Mpango3D.KEYS && !Mpango3D.KEYSPOS) {
                 * paint.setColor(0xff00ff00); canvas.drawCircle(xPos + 15 +
                 * 0.05f * WIDTH, yPos + 0.3f * WIDTH, 0.025f * WIDTH, paint); }
                 * else { paint.setColor(0x44111111); canvas.drawCircle(xPos +
                 * 15 + 0.05f * WIDTH, yPos + 0.3f * WIDTH, 0.025f * WIDTH,
                 * paint); }
                 */
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

            if (x >= LEFT + (WIDTH - boxBmp.getWidth()) * 0.5f
                    && x <= LEFT + 0.5f * WIDTH
                    && y >= TOP + 0.5f * HEIGHT - boxBmp.getHeight() * 0.35f
                    && y <= TOP + 0.5f * HEIGHT - boxBmp.getHeight() * 0.35f
                    + 0.1f * WIDTH) {
                Mpango3D.HELP = !Mpango3D.HELP;
                SharedPreferences sp = getSharedPreferences("com.jackson_siro.mpango", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("help", Mpango3D.HELP);
                editor.commit();
                drawFrame();
                return false;
            }
            if (x >= LEFT + (WIDTH - boxBmp.getWidth()) * 0.5f
                    && x <= LEFT + 0.5f * WIDTH - 0.16f * boxBmp.getWidth()
                    && y >= TOP + 0.5f * HEIGHT - boxBmp.getHeight() * 0.35f
                    + 0.15f * WIDTH
                    && y <= TOP + 0.5f * HEIGHT - boxBmp.getHeight() * 0.35f
                    + 0.25f * WIDTH) {
                Mpango3D.KEYS = !Mpango3D.KEYS;
                // Mpango3D.KEYSPOS = true;
                SharedPreferences sp = getSharedPreferences("com.jackson_siro.mpango", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("keys", Mpango3D.KEYS);
                // editor.putBoolean("keys' position", Mpango3D.KEYSPOS);
                editor.commit();
                drawFrame();
                return false;
            }
            //TOP + (HEIGHT - boxBmp.getHeight()) * 0.5f + fontHeight + 0.15f * WIDTH
            if (x >= LEFT + (WIDTH - boxBmp.getWidth()) * 0.5f
                    && x <= LEFT + 0.5f * WIDTH - 0.16f * boxBmp.getWidth()
                    && y >= TOP + 0.5f * HEIGHT - boxBmp.getHeight() * 0.35f + 0.3f * WIDTH
                    && y <= TOP + 0.5f * HEIGHT - boxBmp.getHeight() * 0.35f + 0.4f * WIDTH) {
                Mpango3D.SOUND = !Mpango3D.SOUND;
                SharedPreferences sp = getSharedPreferences("com.jackson_siro.mpango", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("sound", Mpango3D.SOUND);
                // editor.putBoolean("keys' position", Mpango3D.KEYSPOS);
                editor.commit();
                drawFrame();
                return false;
            }

            if (y >= TOP + HEIGHT - mainmenuBmp.getHeight() - 10
                    && y <= TOP + HEIGHT - 10
                    && x >= LEFT + (WIDTH - mainmenuBmp.getWidth()) * 0.5f
                    && x <= LEFT + (WIDTH + mainmenuBmp.getWidth()) * 0.5f) {
                // drawFrame();
                goWelcome();
                return false;
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
    }

}