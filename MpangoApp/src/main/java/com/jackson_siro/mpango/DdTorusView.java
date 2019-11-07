package com.jackson_siro.mpango;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;

import java.util.ArrayList;

public class DdTorusView extends SurfaceView implements SurfaceHolder.Callback, OnGestureListener {

    private MpangoGame game;
    private Control control;
    private UI ui;
    private Canvas canvas;
    private Paint paint;
    private final Handler mHandler = new Handler();

    private int mVisible;
    private String mScore = new String();
    private String mTime;
    //private String mText = new String();
    private final static int KEY_LEFT = 1;
    private final static int KEY_RIGHT = 2;
    private int gesture;
    private KeysDown keysDown;
    private boolean starting = false;
    private boolean hasWon = false;
    private final static int COMPLETED = 1;
    private final static int UNCOMPLETED = 2;
    private final static int UNDEFINED = 0;

    private SurfaceHolder holder;
    private GestureDetector mGestureDetector;
    private Bitmap baseBmp, bestBmp, smallBoxBmp, nextBmp, titleBmp, pauseBmp, restartBmp, quitBmp, pausedBmp, gameOverBmp, blocksBmp, mainmenuBmp;
    private Bitmap scoreBmp, backgroundBmp, tipBoxBmp, leftBmp, rightBmp, downBmp, rotateBmp, timeBmp, smallpanelBmp, smallplayBmp, grayBmp, tipBmp;

    private DdTorus ddTorus;

    private boolean paused = false, over = false, animating = false;

    private float frameInterval = 0.0f, lastMoveTime = 0.0f, animatingStartTime = 0.0f;
    private long lastFrameTime = 0, lastDrop;

    private ArrayList<Integer> animatingLines;
    private int rotated, focus = 0, theme, WIDTH = 480, HEIGHT = 800, LEFT = -150, TOP = -150, BASEY, boxId = 1, mInterval;
    private Typeface typeface;
    private String bestScore;

    float bw = WIDTH * 0.24f, bh = HEIGHT * 0.15f;

    public DdTorusView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mInterval = 40;
        mTime = "0:00";
        ddTorus = (DdTorus) context;
        holder = getHolder();
        holder.addCallback(this);

        paint = new Paint();
        keysDown = new KeysDown();
        game = new MpangoGame();
        control = new Control();
        ui = new UI();
        setFocusable(true);
        setLongClickable(true);
        mGestureDetector = new GestureDetector(this);

        blocksBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.blocks);
        nextBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.next);
        scoreBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.score);
        bestBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.best);
        pausedBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.paused);
        baseBmp = BitmapFactory.decodeResource(ddTorus.getResources(), R.drawable.base0);
        timeBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.time);

        smallBoxBmp = Mpango3D.scaleBitmap(context, R.drawable.smallbox, bw * 2, bh);
        gameOverBmp = Mpango3D.scaleBitmap(context, R.drawable.gameover, bw * 2, bh);

        leftBmp = Mpango3D.scaleBitmap(context, R.drawable.left, bw, bh);
        rightBmp = Mpango3D.scaleBitmap(context, R.drawable.right, bw, bh);
        downBmp = Mpango3D.scaleBitmap(context, R.drawable.down, bw, bh);
        rotateBmp = Mpango3D.scaleBitmap(context, R.drawable.rotate, bw, bh);

        smallplayBmp = Mpango3D.scaleBitmap(context, R.drawable.smallplay, bw * 2, bh);
        smallpanelBmp = Mpango3D.scaleBitmap(context, R.drawable.panel, bw * 2, bh);

        pauseBmp = Mpango3D.scaleBitmap(context, R.drawable.pause, WIDTH * 0.75f, bh);

        DisplayMetrics dm;
        dm = ddTorus.getApplicationContext().getResources().getDisplayMetrics();
        WIDTH = dm.widthPixels;
        HEIGHT = dm.heightPixels;
        TOP = (HEIGHT == 480 ? -138 : HEIGHT == 533 ? -183 : -213);
        BASEY = (HEIGHT == 480 ? 301 : HEIGHT == 533 ? 346 : 376);

        tipBmp = Mpango3D.scaleBitmap(context, R.drawable.tip, 150, 150);

        mainmenuBmp = Mpango3D.scaleBitmap(context, R.drawable.mainmenu, 0.65f * WIDTH, 0.14f * HEIGHT);
        restartBmp = Mpango3D.scaleBitmap(context, R.drawable.restart, 0.65f * WIDTH, 0.14f * HEIGHT);
        quitBmp = Mpango3D.scaleBitmap(context, R.drawable.quit, 0.65f * WIDTH, 0.14f * HEIGHT);
        tipBoxBmp = Mpango3D.scaleBitmap(context, R.drawable.bigbox, WIDTH * 0.85f, HEIGHT * 0.3f);

        grayBmp = Bitmap.createBitmap(WIDTH, HEIGHT, Config.ARGB_8888);
        Canvas bmpCanvas = new Canvas(grayBmp);
        paint.setColor(0xB3000000);
        bmpCanvas.drawRect(0, 0, WIDTH, HEIGHT, paint);
        AssetManager am = context.getAssets();
        typeface = Typeface.createFromAsset(am, "ComicSansMS3.ttf");
    }

    private final Runnable mDrawRunnable = new Runnable() {
        public void run() {
            drawFrame();
        }
    };

    public String getMpangoBestScore() {
        return bestScore;
    }

    public MpangoGame getMpangoGame() {
        return game;
    }

    public Control getControl() {
        return control;
    }

    public void startGame(int mode) {
        starting = true;
        switch (mode) {
            case 1:
                titleBmp = Mpango3D.scaleBitmap(ddTorus, R.drawable.level_high, WIDTH * 0.55f, bh);
                backgroundBmp = Mpango3D.scaleBitmap(ddTorus, R.drawable.nairobicity, WIDTH, HEIGHT);
                break;
            case 2:
                titleBmp = Mpango3D.scaleBitmap(ddTorus, R.drawable.level_mid, WIDTH * 0.55f, bh);
                backgroundBmp = Mpango3D.scaleBitmap(ddTorus, R.drawable.nairobicity, WIDTH, HEIGHT);
                break;
            case 3:
                titleBmp = Mpango3D.scaleBitmap(ddTorus, R.drawable.level_low, WIDTH * 0.55f, bh);
                backgroundBmp = Mpango3D.scaleBitmap(ddTorus, R.drawable.nairobicity, WIDTH, HEIGHT);
                break;
        }
        control.startGame(mode);
        int aScore = control.best.getRecords(game.mode - 1).get(0).score;
        bestScore = mode == 3 ? niceTime(aScore) : String.valueOf(aScore);
        //tempMpangoBestScore = control.best.getRecords(game.mode - 1).get(0).score;
        //bestScore = mode == 3 ? niceTime(tempMpangoBestScore) : String.valueOf(tempMpangoBestScore);
    }

    float drawTip(float space, String content, float fontHeight, float lineSpace,
                  float lineWidth, float xPos, float h, Canvas canvas, Paint paint)    //spaceä¸ºé¦–è¡Œæ¼�æŽ‰ç©ºæ ¼
    {
        int len = content.length();
        String line = "";
        float wChars = 0;
        int k = 0;
        int i = 0;
        int lineNum = 0;
        wChars += space;
        xPos += space;
        for (; i < len; i++) {
            wChars += paint.measureText(String.valueOf(content.charAt(i)));
            if (i + 1 < len && paint.measureText(String.valueOf(content.charAt(i + 1))) + wChars > lineWidth) {
                if (content.charAt(i) != ' ' && content.charAt(i + 1) != ' ') {
                    if (content.charAt(i - 1) == ' ') {
                        line = content.substring(k, i);
                        wChars = 0;
                        k = i;
                        i--;
                    } else {
                        line = content.substring(k, i) + "-";
                        wChars = 0;
                        k = i;
                        i--;
                    }
                } else {
                    line = content.substring(k, i + 1);
                    wChars = 0;
                    k = i + 1;
                }
                h += lineSpace + fontHeight;
                canvas.drawText(line, xPos, h, paint);
                if (lineNum == 1) {
                    lineNum++;
                    xPos -= space;
                } else if (lineNum == 0) {
                    wChars += space;
                    lineNum++;
                }
            }
        }
        if (wChars != 0) {
            h += lineSpace + fontHeight;
            canvas.drawText(content.substring(k, i), xPos, h, paint);
        }
        return h;
    }

    void drawFrame() {
        canvas = holder.lockCanvas();
        long t = System.currentTimeMillis();
        if (canvas != null) {
            canvas.translate(-LEFT, -TOP);
            paint.setAntiAlias(true);
            paint.setStyle(Style.FILL);
            paint.setColor(0xff000000);
            canvas.drawBitmap(backgroundBmp, null, new RectF(LEFT, TOP, WIDTH + LEFT, HEIGHT + TOP), paint);
            float baseh = TOP + HEIGHT - pauseBmp.getHeight() - (titleBmp.getHeight() * 3.2f);
            canvas.drawBitmap(baseBmp, LEFT + (WIDTH - baseBmp.getWidth()) / 2, baseh, paint);

            float lefts = LEFT + 20, tops = TOP + 20, rights = LEFT + 200, bots = TOP + 75, xPos, yPos;
            if (!over) {
                canvas.drawBitmap(nextBmp, null, new RectF(lefts, tops, rights, bots), paint);
                xPos = LEFT + WIDTH - (bestBmp.getWidth() * 1.5f);
                canvas.drawBitmap(bestBmp, null, new RectF(xPos + 20, tops, xPos + 180, bots), paint);

                canvas.drawBitmap(smallBoxBmp, LEFT, bots, paint);
                canvas.drawBitmap(smallBoxBmp, xPos - 14, bots, paint);

                if (game.mode != 3 && game.score > Integer.parseInt(bestScore)) {
                    bestScore = String.valueOf(game.score);
                }
                paint.setTypeface(typeface);
                paint.setTextSize(35);
                paint.setColor(0xffffffff);
                FontMetrics fm = paint.getFontMetrics();
                
                float fontHeight = (float) Math.ceil(fm.descent - fm.ascent);
                float fontWidth = paint.measureText(bestScore);

                bots = bots + (smallBoxBmp.getHeight() / 2);
                canvas.drawText(bestScore, xPos - 14 + (smallBoxBmp.getWidth() - fontWidth) / 2.0f, bots, paint);

                float tps = TOP + 55, ;
                canvas.save();
                canvas.clipRect(
                        LEFT + (smallBoxBmp.getWidth() - 90) / 2.0f,
                        tps + (smallBoxBmp.getHeight() - 60) / 2.0f,
                        LEFT + (smallBoxBmp.getWidth() - 90) / 2.0f + 90,
                        tps + (smallBoxBmp.getHeight() - 60) / 2.0f + 60
                );

                yPos = bots + (smallBoxBmp.getHeight() / 2);
                bots = yPos + 75;
                canvas.drawBitmap(blocksBmp, LEFT, tps + (smallBoxBmp.getHeight() - 50) / 2.0f - 3, paint);
                //canvas.drawBitmap(blocksBmp, xPos - 14, bots, paint);
                canvas.restore();

                canvas.drawBitmap(scoreBmp, null, new RectF(LEFT + 18, yPos, LEFT + 200, bots), paint);
                canvas.drawBitmap(timeBmp, null, new RectF(xPos + 10, yPos, xPos + 180, bots), paint);

                //canvas.drawBitmap(smallpanelBmp, LEFT + 10, yPos + 17, paint);
                //canvas.drawBitmap(smallpanelBmp, xPos - 14 + (smallBoxBmp.getWidth() - smallpanelBmp.getWidth()) * 0.5f, yPos + 17, paint);

                paint.setTypeface(typeface);
                paint.setTextSize(15);
                paint.setColor(0xffffffff);
                fm = paint.getFontMetrics();
                fontWidth = paint.measureText(mScore);
                fontHeight = (float) Math.ceil(fm.descent - fm.ascent);
                /*canvas.drawText(mScore, LEFT + (smallBoxBmp.getWidth() - smallpanelBmp.getWidth()) * 0.5f + (smallpanelBmp.getWidth() - fontWidth) * 0.5f,
                        yPos + 17 + (smallpanelBmp.getHeight() + fontHeight) * 0.5f, paint);
                fontWidth = paint.measureText(mTime);
                canvas.drawText(mTime, xPos - 14 + (smallBoxBmp.getWidth() - smallpanelBmp.getWidth()) * 0.5f + (smallpanelBmp.getWidth() - fontWidth) * 0.5f,
                        yPos + 17 + (smallpanelBmp.getHeight() + fontHeight) * 0.5f, paint);*/

                canvas.drawBitmap(titleBmp, LEFT + (WIDTH - titleBmp.getWidth()) * 0.5f, TOP + HEIGHT - pauseBmp.getHeight() - (titleBmp.getHeight() * 1.2f), paint);
                if (!paused) {
                    float h = TOP + HEIGHT - (pauseBmp.getHeight() * 1.2f);
                    canvas.drawBitmap(pauseBmp, LEFT + (WIDTH - pauseBmp.getWidth()) * 0.5f, h, paint);

                    if (Mpango3D.KEYS) {
                        h = TOP + HEIGHT - (leftBmp.getHeight() * 1.2f);
                        canvas.drawBitmap(leftBmp, LEFT + 10, h, paint);
                        canvas.drawBitmap(rightBmp, LEFT + WIDTH - (rightBmp.getWidth() * 1.2f), h, paint);
                        h -= rotateBmp.getHeight();
                        canvas.drawBitmap(downBmp, LEFT + 10, h, paint);
                        canvas.drawBitmap(rotateBmp, LEFT + WIDTH - (rotateBmp.getWidth() * 1.2f), h, paint);
                    }
                    if (starting) {
                        //game.drawCylinder(canvas, paint, false, false, 0, null);
                        starting = false;
                    }

                    if (Mpango3D.HELP && game.dropPositions[3] > TOP + 101) {
                        //game.drawCylinder(canvas, paint, true, false, 0, null);
                        paint.setTypeface(typeface);
                        paint.setTextSize(40);
                        paint.setColor(0xffffffff);
                        fm = paint.getFontMetrics();
                        fontHeight = (float) Math.ceil(fm.descent - fm.ascent);
                        float startPos = LEFT + WIDTH * 0.5f - 0.45f * tipBoxBmp.getWidth() + tipBmp.getWidth() * 0.6f + 5;
                        float lineSpace = 0.0125f * fontHeight;
                        float boxPosX = (WIDTH - tipBoxBmp.getWidth()) * 0.5f + LEFT;
                        float boxPosY = (HEIGHT - tipBoxBmp.getHeight()) * 0.5f + TOP;
                        canvas.drawBitmap(tipBoxBmp, boxPosX, boxPosY, paint);
                        canvas.drawBitmap(tipBmp, boxPosX + 5, boxPosY + 5, paint);
                        if (boxId < 4) {
                            String tip;
                            if (boxId == 1) tip = getContext().getString(R.string.hint1);
                            else if (boxId == 2) tip = getContext().getString(R.string.hint2);
                            else tip = getContext().getString(R.string.hint3);

                            boxPosY += 10;
                            Mpango3D.drawContent(tip, fontHeight, lineSpace, tipBoxBmp.getWidth() * 0.9f - 5 - tipBmp.getWidth() * 0.6f,
                                    startPos, boxPosY, canvas, paint);
                            if (boxId == 1 || boxId == 2) {
                                String nextTip = getContext().getString(R.string.next_hint);
                                paint.setColor(0xffffff00);
                                fontWidth = paint.measureText(nextTip);
                                boxPosY = (HEIGHT + tipBoxBmp.getHeight()) * 0.5f + TOP - 70;
                                boxPosX = 0.5f * WIDTH + 0.45f * tipBoxBmp.getWidth() + LEFT - fontWidth - 5;
                                canvas.drawText(nextTip, boxPosX, boxPosY, paint);
                            }
                        }
                    } else {
                        nextFrame(null);
                    }
                } else {
                    //canvas.drawBitmap(pausedBmp, LEFT + (WIDTH - baseBmp.getWidth()) / 2 + 37, TOP + BASEY + 30, paint);
                    //canvas.drawBitmap(grayBmp, LEFT, TOP, paint);
                    //canvas.drawBitmap(smallplayBmp, LEFT + (WIDTH - pauseBmp.getWidth()) * 0.5f, TOP + HEIGHT - pauseBmp.getHeight(), paint);
                    //canvas.drawBitmap(restartBmp, LEFT + (WIDTH - restartBmp.getWidth()) / 2, TOP + 0.5f * HEIGHT - 1.04f * quitBmp.getHeight(), paint);
                    //canvas.drawBitmap(quitBmp, LEFT + (WIDTH - quitBmp.getWidth()) * 0.5f, TOP + 0.5f * HEIGHT, paint);
                }
            } else {
                /*canvas.drawBitmap(scoreBmp, LEFT + (WIDTH - scoreBmp.getWidth()) / 2.0f, TOP + 55, paint);
                paint.setTypeface(typeface);
                paint.setTextSize(17);
                paint.setColor(0xffffffff);
                FontMetrics fm = paint.getFontMetrics();
                float fontHeight = (float) Math.ceil(fm.descent - fm.ascent);
                float fontWidth = paint.measureText(mScore);
                canvas.drawText(mScore, LEFT + (WIDTH - fontWidth) / 2.0f, TOP + 55 + scoreBmp.getHeight() + fontHeight, paint);

                yPos = TOP + 126;
                canvas.drawBitmap(gameOverBmp, LEFT + (WIDTH - gameOverBmp.getWidth()) / 2, TOP + 126, paint);
                yPos += gameOverBmp.getHeight() + restartBmp.getHeight() * 0.08f;
                canvas.drawBitmap(restartBmp, LEFT + (WIDTH - restartBmp.getWidth()) * 0.5f, yPos, paint);
                yPos += restartBmp.getHeight() * 1.04f;
                canvas.drawBitmap(mainmenuBmp, LEFT + (WIDTH - restartBmp.getWidth()) * 0.5f, yPos, paint);*/
            }
        }
        if (canvas != null) {
            holder.unlockCanvasAndPost(canvas);
        }

        mHandler.removeCallbacks(mDrawRunnable);
        t = System.currentTimeMillis() - t;
        if (mVisible == VISIBLE) {
            if (t >= mInterval) mHandler.post(mDrawRunnable);
            else mHandler.postDelayed(mDrawRunnable, mInterval - t);
        }
    }

    public static String niceTime(long t) {
        long h = t / 60;
        long m = t % 60;
        String time;
        if (m < 10) time = "0" + m;
        else time = String.valueOf(m);
        return h + ":" + time;
    }

    void afterPlace() {
        int cleared = 0;
        for (int j = 0; j < 15; j++) {
            int i = 0;
            for (; i < 15; i++) {
                if (game.field[i][j] == null) break;
            }
            if (i == 15) {
                for (i = 0; i < 15; i++) {
                    game.field[i][j] = null;
                    for (int k = j + 1; k < 15; k++) {
                        game.field[i][k - 1] = game.field[i][k];
                    }
                    game.field[i][14] = null;
                }
                j--;
                game.lines++;
                cleared++;
            }
        }
        // int scores[] = {0, 100, 250, 400, 600, 1000};
        int scores[] = {0, 10, 25, 40, 60, 100};
        game.score += scores[cleared];
        if (game.mode == 3) {
            int j = 14;
            for (; j > 2; j--) {
                int i = 0;
                for (; i < 15; i++) {
                    if (game.field[i][j] != null) break;
                }
                if (i < 15) break;
            }
            // g('score').innerHTML = '+' + (j - 2);
            mScore = "+" + (j - 2);
            if (j == 2) {
                change();
                game.drawCylinder(canvas, paint, false, false, 0, null);
                control.gameOver(COMPLETED);
                game.viewPort.setTarget(game.viewPort.mTarget + game.blockData.get(game.type).view);
                return;
            }
        } else {
            mScore = String.valueOf((int) Math.floor(game.score));
            // g('score').innerHTML = Math.floor(MpangoGame.score);
        }
        game.viewPort.setTarget(game.viewPort.mTarget + game.blockData.get(game.type).view);
        change();
    }

    void nextFrame(String keyForce) {
        long now = System.currentTimeMillis();
        float elapsed;
        elapsed = now - lastFrameTime;
        elapsed = Math.max(0, Math.min(1000, elapsed));
        game.time += elapsed;
        if (game.mode == 2 && game.time > 301 * 1000) {
            control.gameOver(UNDEFINED);
        } else {
            long t = game.mode == 2 ? (301 * 1000 - game.time) : game.time;
            //mTime = niceTime(t);
            mTime = niceTime((int) (t / 1000));
        }
        //float delay = (float) (game.mode == 3 ? 1000 :(20 + 2980 * Math.exp(- game.lines / 35)));	//æº�ç �
        float delay = (float) (game.mode == 3 ? 2000 : (20 + 2980 * Math.exp(-game.lines / 35)));
        if (keysDown.down)
        //if(keysDown.down != 0)
        {
            game.score += elapsed / 100;
            if (game.mode != 3) mScore = String.valueOf(game.score);
            delay = Math.min(delay, 30);
        }
        game.posY -= Math.max(0, Math.min(1, (float) (elapsed / delay)));
        lastFrameTime = now;
        if (animating == true) {
            elapsed = (float) ((game.time - animatingStartTime) / Math.sqrt(animatingLines.size()));
            if (elapsed > 300 || elapsed < 0) {
                animating = false;
                afterPlace();
                game.drawCylinder(canvas, paint, true, false, 0, null);    //é˜²æ­¢å‡ºçŽ°ç™½å±�
                if (Mpango3D.SOUND) {
                    try {
                        MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.explode);
                        mp.start();
                    } catch (Exception e) { }
                }
                return;
            } else {
                game.drawCylinder(canvas, paint, false, false, elapsed / 300.0f, animatingLines);
                return;
            }
        }
        if (keyForce != null || (keysDown.left ^ keysDown.right) && (now - lastMoveTime > 150)) {
            lastMoveTime = now;
            move(keyForce == "left" ? 1 : keyForce == "right" ? -1 : keysDown.left ? 1 : keysDown.right ? -1 : 0);
            if (Mpango3D.SOUND) {
                try {
                    MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.rotate);
                    mp.start();
                } catch (Exception e) { }
            }
        }
        int slotY = (int) Math.floor(game.posY);
        if (game.block.size() == 1) {
            int y = slotY;
            for (; y >= 0; y--) {
                if (game.field[(int) (((game.x + game.block.get(0).x) % 15 + 15) % 15)][y] == null) {
                    break;
                }
            }
            if (y < 0) {
                place(slotY + 1);
                game.drawCylinder(canvas, paint, true, false, 0, null);    //é˜²æ­¢å‡ºçŽ°ç™½å±�
                return;
            }
        } else {
            for (int i = game.block.size() - 1; i >= 0; i--) {
                int index = (int) (slotY + game.block.get(i).y);
                if (index < 0 || (index >= 0 && index < 15 &&
                        game.field[(int) (((game.x + game.block.get(i).x) % 15 + 15) % 15)][index] != null)) {
                    place(slotY + 1);
                    game.drawCylinder(canvas, paint, true, false, 0, null);//é˜²æ­¢å‡ºçŽ°ç™½å±�
                    if (Mpango3D.SOUND) {
                        try {
                            MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.clank);
                            mp.start();
                        } catch (Exception e) {
                        }
                    }
                    return;
                }
            }
        }
        game.drawCylinder(canvas, paint, true, false, 0, null);
    }

    public void start() {
        game.nextType = game.getRandomPieceType();
        this.prepare();
        this.pause();
        this.resume();
        game.time = 0;
        over = false;
    }

    public void resume() {
        if (!paused) return;
        paused = false;
        keysDown.left = false;
        keysDown.right = false;
        keysDown.down = false;
        lastFrameTime = System.currentTimeMillis();
        mVisible = VISIBLE;
    }

    void pause() {
        if (paused) return;
        paused = true;
    }

    void gameOver() {
        this.pause();
        over = true;
    }

    void place(int slotY) {
        keysDown.down = false;
        for (int i = game.block.size() - 1; i >= 0; i--) {
            if (slotY + game.block.get(i).y > 14) {
                game.drawCylinder(canvas, paint, false, true, 0, null);
                control.gameOver(UNDEFINED);
                return;
            }
        }
        for (int i = game.block.size() - 1; i >= 0; i--) {
            int firstKey = (int) (((game.x + game.block.get(i).x) % 15 + 15) % 15);
            int secondKey = (int) (slotY + game.block.get(i).y);
            if (game.field[firstKey][secondKey] == null) {
                game.field[firstKey][secondKey] = new int[2];
            }
            game.field[firstKey][secondKey][0] = game.type + 1;
            game.field[firstKey][secondKey][1] = game.pieceCount;
        }
        ++game.pieceCount;
        ArrayList<Integer> cleared = new ArrayList<Integer>();

        for (int j = 0; j < 15; j++) {
            int i = 0;
            for (; i < 15; i++) {
                if (game.field[i][j] == null) break;
            }
            if (i == 15) {
                for (i = 0; i < 15; i++) {
                    if (game.field[i][j] == null) {
                        game.field[i][j] = new int[2];
                    }
                    game.field[i][j][0] = game.field[i][j][0];
                    game.field[i][j][1] = 0;
                }
                cleared.add(j);
            }
        }
        if (cleared.size() != 0) {
            animating = true;
            animatingStartTime = game.time;
            animatingLines = cleared;
        } else afterPlace();
    }

    void prepare() {
        game.x = -2;
        this.change();
    }

    void change() {
        game.type = game.nextType;
        game.nextType = game.getRandomPieceType();
        theme = game.blockData.get(game.nextType).theme;
        rotated = 0;
        game.posY = 16.0f;
        game.viewPort.setTarget(game.viewPort.mTarget - game.blockData.get(game.type).view);
        game.block = game.blockData.get(game.type).coords.get(0).block;
        game.pieceSpawnTime = game.time;
    }

    boolean overlaps() {
        int X, Y;
        int slotY = (int) Math.floor(game.posY);
        int i;
        if (game.block.size() == 1) {
            for (Y = slotY; Y >= 0; Y--) {
                if (game.field[(int) (((game.x + game.block.get(0).x) % 15 + 15) % 15)][Y] == null) {
                    return false;
                }
            }
            return true;
        }
        for (i = game.block.size() - 1; i >= 0; i--) {
            X = (int) (((game.x + game.block.get(i).x) % 15 + 15) % 15);
            Y = (int) (slotY + game.block.get(i).y);
            if (Y < 0 || (X > 0 && X < 15 && Y > 0 && Y < 15 && game.field[X][Y] != null)) {
                return true;
            }
        }
        if (game.posY % 1 > 1 / 2) {
            slotY = (int) Math.ceil(game.posY);
            for (i = game.block.size() - 1; i >= 0; i--) {
                X = (int) (((game.x + game.block.get(i).x) % 15 + 15) % 15);
                Y = (int) (slotY + game.block.get(i).y);
                if (Y < 0 || (X >= 0 && X < 15 && Y >= 0 && Y < 15 && game.field[X][Y] != null)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean rotate(int dir, boolean idle) {
        if (game.type == 0) {
            return false;
        }
        rotated = (rotated + dir + 4) % game.blockData.get(game.type).coords.size();
        game.block = game.blockData.get(game.type).coords.get(rotated).block;
        if (idle) {
            return false;
        }
        if (overlaps() && !this.move(1) && !this.move(-1)) {
            this.rotate(-dir, true);
            return false;
        }
        if (Mpango3D.SOUND) {
            try {
                MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.bounce);
                mp.start();
            } catch (Exception e) {
            }
        }
        return true;
    }

    boolean move(float xDir) {
        game.x += xDir;
        if (overlaps()) {
            game.x -= xDir;
            return false;
        }
        game.viewPort.setTarget(game.viewPort.mTarget + xDir);
        return true;
    }

    void initAlertDialog(final int score, final int mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ddTorus);
        final EditText editText = new EditText(ddTorus);
        builder.setView(editText);
        builder.setCancelable(false);
        builder.setTitle(R.string.fill_name);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editText.getText().toString();
                ArrayList<MpangoBest.Record> recs = control.best.getRecords(mode - 1);
                int i = 1;
                if (mode != 3) {
                    for (; i >= 0; i--) {
                        if (recs.get(i).score < score) {
                            recs.get(i + 1).score = recs.get(i).score;
                            recs.get(i + 1).name = recs.get(i).name;
                        } else break;
                    }
                    recs.get(i + 1).score = score;
                    recs.get(i + 1).name = name;
                } else {
                    for (; i >= 0; i--) {
                        if (recs.get(i).score > score) {
                            recs.get(i + 1).score = recs.get(i).score;
                            recs.get(i + 1).name = recs.get(i).name;
                        } else break;
                    }
                    recs.get(i + 1).score = score;
                    recs.get(i + 1).name = name;
                }
                control.best.updateHighScoreFiles(mode - 1, recs);
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                goWelcome();
            }
        });
        builder.create();
        builder.show();
    }

    class KeysDown {
        boolean down = false;
        boolean left = false;
        boolean right = false;
    }

    class Control {
        Mpango3D config = new Mpango3D();
        MpangoBest best;
        String bestRecord;

        Control() {
            best = new MpangoBest();
        }

        void gameOver(int complete) {
            DdTorusView.this.gameOver();
            int score = (int) Math.floor(game.score);
            long time = game.time;
            if (complete == UNCOMPLETED) return;
            //String message = "  Your score of " + score + " did not achieve a high score .";
            hasWon = false;
            if (game.mode == 1) {
                if (score > best.getRecords(0).get(2).score) {
                    hasWon = true;
                    initAlertDialog(score, 1);
                }
            }
            if (game.mode == 2) {
                if (time > 301 * 1000) {
                    if (score > best.getRecords(1).get(2).score) {
                        hasWon = true;
                        initAlertDialog(score, 2);
                    } else {
                        //message = "  Failure . You must survive for 3 minutes to qualify for a high score in Time Attack .";
                    }
                }
            }
            if (game.mode == 3) {
                if (complete == UNDEFINED) {
                    //message = "  You must clear all but three rows to qualify for a high score in Garbage .";
                } else if (time < best.getRecords(2).get(2).score * 1000) {
                    hasWon = true;
                    initAlertDialog((int) (time / 1000), 3);
                } else {
                    //message = "  Your time of " + TorusView.niceTime((int)(time / 1000)) + " did not ahcieve a high score .";
                }
            }
            ui.gameOver();
            //mText = hasWon ? "" : message;
        }

        void restartGame() {
            gameOver(UNCOMPLETED);
            startGame(game.mode);
        }

        void startGame(int mode) {
            //ui.setGameMode(mode <= 0 ? 1 : mode);
            game.setMode(mode);
            mScore = mode == 3 ? "+6" : String.valueOf(game.score);
            start();
        }

        void pauseGame() {
            pause();
            ui.pauseGame();
        }

        void resumeGame() {
            resume();
            ui.resumeGame();
        }

    }

    class UI {
        boolean active_menu = true;
        PauseAnimation pauseAnimation;

        UI() {
            pauseAnimation = new PauseAnimation();
        }

        void menuMode() {
            game.mode = 1;
            game.clear();
            game.drawCylinder(canvas, paint, false, false, 0, null);
        }

        void init() {
            menuMode();
        }

        void pauseGame() {
            pauseAnimation.start(true);
        }

        void resumeGame() {
            pauseAnimation.start(false);
        }

        void gameOver() {
        }

        class PauseAnimation {
            CubicMotion motion = new CubicMotion(300);
            int interval = 0;

            void frame() {
                float pos = this.motion.getPosition();
                if (pos == this.motion.mTarget) {
                    mHandler.removeCallbacks(mDrawRunnable);
                    this.interval = 0;
                }
            }

            void start(boolean doPause) {
                this.motion.setTarget(doPause ? 1 : 0);
                if (this.interval == 0) {
                    mHandler.postDelayed(mDrawRunnable, 10);
                }
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawFrame();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mVisible = INVISIBLE;
        mHandler.removeCallbacks(mDrawRunnable);
    }

    boolean isInArea(ArrayList<MpangoGame.Coords> arr, float ex, float ey) {
        float minX, maxX, minY, maxY;
        MpangoGame.Coords area;
        MpangoGame.Coord pttl;
        MpangoGame.Coord ptbr;
        int areas = arr.size();
        for (int i = 0; i < areas; i++) {
            area = arr.get(i);
            pttl = area.block.get(0);
            ptbr = area.block.get(1);
            minX = pttl.x <= ptbr.x ? pttl.x : ptbr.x;
            maxX = pttl.x >= ptbr.x ? pttl.x : ptbr.x;
            minY = pttl.y <= ptbr.y ? pttl.y : ptbr.y;
            maxY = pttl.y >= ptbr.y ? pttl.y : ptbr.y;

            if (ex >= minX && ex <= maxX && ey >= minY && ey <= maxY) return true;
        }
        return false;
    }

    void goWelcome() {
        ddTorus.startActivity(new Intent(ddTorus, BbWelcome.class));
        (ddTorus).finish();
    }

    void help() {
        Mpango3D.HELP = false;
        SharedPreferences sp = ddTorus.getSharedPreferences("com.jackson_siro.mpango", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("help", false);
        editor.commit();
        drawFrame();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        float ex = e.getX() + LEFT;
        float ey = e.getY() + TOP;

        if (!over) {
            if (!paused) {
                if (Mpango3D.HELP && ex >= LEFT + (WIDTH - tipBoxBmp.getWidth()) * 0.5f && ex <= LEFT + (WIDTH + tipBoxBmp.getWidth()) * 0.5f &&
                        ey >= TOP + (HEIGHT - tipBoxBmp.getHeight()) * 0.5f && ey <= TOP + (HEIGHT + tipBoxBmp.getHeight()) * 0.5f) {
                    boxId++;
                    if (boxId > 3) {
                        Mpango3D.HELP = false;
                        help();
                    }
                    return false;
                }
                if (!Mpango3D.HELP) {
                    //if(keysDown.down == 0)
                    if (!keysDown.down) {
                        if (Mpango3D.KEYS) {
                            if (ey >= TOP + HEIGHT - leftBmp.getHeight() && ey <= TOP + HEIGHT) {
                                if (ex >= LEFT + 4 && ex <= LEFT + 4 + leftBmp.getWidth()) {
                                    if (!keysDown.left) {
                                        keysDown.left = true;
                                        nextFrame("left");
                                        gesture = KEY_LEFT;
                                    }
                                } else if (ex >= LEFT + WIDTH - rightBmp.getWidth() && ex <= LEFT + WIDTH) {
                                    if (!keysDown.right) {
                                        keysDown.right = true;
                                        nextFrame("right");
                                        gesture = KEY_RIGHT;
                                    }
                                }
                                if (gesture == KEY_LEFT) {
                                    keysDown.left = false;
                                }
                                if (gesture == KEY_RIGHT) {
                                    keysDown.right = false;
                                }
                                gesture = 0;
                            }
                            if (ey >= TOP + HEIGHT - leftBmp.getHeight() - rotateBmp.getHeight() &&
                                    ey <= TOP + HEIGHT - leftBmp.getHeight()) {
                                if (ex >= LEFT + 4 && ex <= LEFT + 4 + leftBmp.getWidth()) {
                                    //keysDown.down = 1;
                                    keysDown.down = true;
                                    nextFrame(null);
                                    return false;
                                } else if (ex >= LEFT + WIDTH - rightBmp.getWidth() && ex <= LEFT + WIDTH) {
                                    rotate(1, false);
                                }
                            }
                        }
                        if (game.dropPositions[3] < game.ghostPositions[2] && ex >= game.ghostPositions[0]
                                && ex <= game.ghostPositions[1] && ey >= game.ghostPositions[2] && ey <= game.ghostPositions[3]) {
                            //keysDown.down = 1;
                            keysDown.down = true;
                            nextFrame(null);
                            return false;
                        }
                    }
                }
            }
            if (ex >= LEFT + (WIDTH - pauseBmp.getWidth()) * 0.5f && ex <= LEFT + (WIDTH + pauseBmp.getWidth()) * 0.5f &&
                    ey >= TOP + HEIGHT - pauseBmp.getHeight() && ey <= TOP + HEIGHT) {
                drawFrame();
                if (paused) control.resumeGame();
                else control.pauseGame();
                return false;
            }
            if (paused) {
                if (ex >= LEFT + (WIDTH - restartBmp.getWidth()) / 2 && ex <= LEFT + (WIDTH + restartBmp.getWidth()) * 0.5f) {
                    if (ey >= TOP + 0.5f * HEIGHT - 1.04f * quitBmp.getHeight() && ey <= TOP + 0.5f * HEIGHT - 0.04f * quitBmp.getHeight()) {
                        boxId = 1;
                        control.restartGame();
                    } else if (ey >= TOP + 0.5f * HEIGHT && ey <= TOP + 0.5f * HEIGHT + quitBmp.getHeight()) {
                        //quit
                        mVisible = INVISIBLE;
                        drawFrame();
                        pause();
                        control.gameOver(UNCOMPLETED);
                        ui.menuMode();
                        goWelcome();
                    }
                }
            }
        } else {
            if (ex >= LEFT + (WIDTH - restartBmp.getWidth()) * 0.5f && ex <= LEFT + (WIDTH + restartBmp.getWidth()) * 0.5f) {
                if (ey >= TOP + 126 + gameOverBmp.getHeight() + 1.12f * restartBmp.getHeight() &&
                        ey <= TOP + 126 + gameOverBmp.getHeight() + 2.12f * restartBmp.getHeight()) {
                    drawFrame();
                    control.gameOver(UNCOMPLETED);
                    ui.menuMode();
                    goWelcome();
                } else if (ey >= TOP + 126 + gameOverBmp.getHeight() + restartBmp.getHeight() * 0.08f &&
                        ey <= TOP + 126 + gameOverBmp.getHeight() + 1.08f * restartBmp.getHeight()) {
                    drawFrame();
                    control.restartGame();
                }
            }
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private final static float FLING_MIN_DISTANCE = 10.0f;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (Mpango3D.HELP || keysDown.down) return false;
        float distanceX = e1.getX() - e2.getX();
        if (!over && !paused) {
            if (distanceX > FLING_MIN_DISTANCE) {
                if (!keysDown.left) {
                    keysDown.left = true;
                    nextFrame("left");
                    gesture = KEY_LEFT;
                }
            } else if ((-distanceX) > FLING_MIN_DISTANCE) {
                if (!keysDown.right) {
                    keysDown.right = true;
                    nextFrame("right");
                    gesture = KEY_RIGHT;
                }
            }
            if (gesture == KEY_LEFT) keysDown.left = false;
            if (gesture == KEY_RIGHT) keysDown.right = false;
            gesture = 0;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        float ex = e.getX() + LEFT;
        float ey = e.getY() + TOP;
        if (!Mpango3D.HELP && !over && !paused && !keysDown.down && ex >= game.dropPositions[0] && ex <= game.dropPositions[1] &&
                ey >= game.dropPositions[2] && ey <= game.dropPositions[3]) {
            rotate(1, false);
        }
        if (keysDown.down) keysDown.down = false;
        return false;
    }

    protected void onMpango3DurationChanged(Configuration config) {
    }
}