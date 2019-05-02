package zkagazapahtnajusz.paperproject.com.paperproject.Model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine.Reader_EPUB;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class CustomRelativeLayout extends RelativeLayout {
    private static final String TAG = "CustomRelativeLayout";

    private CustomOnTapChangeListener customOnTapChangeListener;
    private CustomOnSwipeListener customOnSwipeListener;
    long tapTime =0;
    boolean firstTouch = false;
    int tapMove = 0;
    private static final int MIN_DISTANCE = 200;
    private float downX, downY;

    //curling
    int curlingOffSet = 5;
    boolean curlEvent = false; int curlCounter =0;
    private boolean isCurling = false;
    private Paint mPaint;
    float deviceWidth;
    private Point mLastTouchPoint;
    boolean drawEnable;
    boolean touchDisable;
    boolean touchFromLeft;
    boolean touchFromRight;
    private Handler mHandler = new Handler();

    //Swiping acceleration and position
    float speed;
    int multiplySpeed = 30;
    long initTime, finalTIme;

    //isText Selection
    boolean isTextSelection =false;

    //swiping moving
    boolean isSwipingMoving = false;

    public CustomRelativeLayout(Context context) {
        super(context);
        init();
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public interface CustomOnTapChangeListener{
        void onStart(float x, float y);
        void onTap();
    }

    public interface CustomOnSwipeListener{
        void onSwipeLeft();
        void onSwipeRight();
        void onSwipeUp();
        void onSwipeDown();
    }

    public CustomOnTapChangeListener getCustomOnTapChangeListener() {
        return customOnTapChangeListener;
    }

    public void setCustomOnTapChangeListener(CustomOnTapChangeListener customOnTapChangeListener) {
        this.customOnTapChangeListener = customOnTapChangeListener;
    }

    public CustomOnSwipeListener getCustomOnSwipeListener() {
        return customOnSwipeListener;
    }

    public void setCustomOnSwipeListener(CustomOnSwipeListener customOnSwipeListener) {
        this.customOnSwipeListener = customOnSwipeListener;
    }

    public void setCurling(boolean curling) {
        isCurling = curling;
    }

    public boolean isCurling() {
        return isCurling;
    }

    public boolean isTextSelection() {
        return isTextSelection;
    }

    public void setTextSelection(boolean textSelection) {
        isTextSelection = textSelection;
    }

    //Curling System
    private void init() {
        mPaint = new Paint();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceWidth = displayMetrics.widthPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e(TAG, "----------->" + drawEnable + ":" + isCurling + ":" + isTextSelection);

        if(drawEnable &&  isCurling && !isTextSelection && isSwipingMoving){
            flipDraw(canvas);
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        Log.e(TAG, "CLICK,,,,,,,,,,,,..........");
        return true;
    }

    private void flipDraw(Canvas canvas){
        try {
            int height = getMeasuredHeight();
            int width = getMeasuredWidth();

            int halfWidth = (int) (width * .5);

            int distanceToEnd = width - mLastTouchPoint.x;
            int backOfPageWidth = Math.min(halfWidth, distanceToEnd / 2);
            int shadowLength = Math.max(5, backOfPageWidth / 20);

            // The rect that represents the backofthepage
            Rect backOfPageRect = new Rect(mLastTouchPoint.x, 0, mLastTouchPoint.x + backOfPageWidth, height);
            // The along the crease of the turning page
            Rect shadowRect = new Rect(mLastTouchPoint.x - shadowLength, 0, mLastTouchPoint.x, height);
            // The shadow cast onto the next page by teh turning page
            Rect backShadowRect = new Rect(backOfPageRect.right, 0, backOfPageRect.right + (backOfPageWidth / 2), height);

            // clip and draw the first shadow
            canvas.save();
            canvas.clipRect(shadowRect);
            mPaint.setShader(new LinearGradient(shadowRect.left, shadowRect.top, shadowRect.right, shadowRect.top, 0x00000000, 0x44000000,
                    Shader.TileMode.REPEAT));
            canvas.drawPaint(mPaint);
            canvas.restore();

            mPaint.setShader(null);

            // clip and draw the gradient that makes the page look bent
            canvas.save();
            canvas.clipRect(backOfPageRect);
            mPaint.setShadowLayer(0, 0, 0, 0x00000000);
            mPaint.setShader(new LinearGradient(backOfPageRect.left, backOfPageRect.top, backOfPageRect.right, backOfPageRect.top, new int[]{0xFFEEEEEE,
                    0xFFDDDDDD, 0xFFEEEEEE, 0xFFD6D6D6}, new float[]{.35f, .73f, 9f, 1.0f}, Shader.TileMode.REPEAT));
            canvas.drawPaint(mPaint);
            canvas.restore();

            if (backShadowRect.left > 0) {
                canvas.save();
                canvas.clipRect(backShadowRect);
                mPaint.setShader(new LinearGradient(backShadowRect.left, backShadowRect.top, backShadowRect.right, backShadowRect.top, 0x44000000, 0x00000000,
                        Shader.TileMode.REPEAT));
                canvas.drawPaint(mPaint);
                // canvas.drawColor(0xFF000000);
                canvas.restore();
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private interface CurlingAnimationFinish{
        void finish();
    }

    private void animationTouchForLeft(final CurlingAnimationFinish curlingAnimationFinish){
        try {
            final int halfWidth = getMeasuredWidth() / 2;
            if (mLastTouchPoint.x + 330 > halfWidth) {
                final Runnable animationRunnable = new Runnable() {
                    public void run() {
                        if (mLastTouchPoint != null) {
                            mLastTouchPoint.x += speed;
                            invalidate();
                            if (mLastTouchPoint.x < getMeasuredWidth()) {
                                mHandler.post(this);
                            } else {
                                touchDisable = false;
                                touchFromRight = false;
                                touchFromLeft = false;
                            }

                            //page swipe event
                            //play left side
                            curlSwipeCheck_Left(mLastTouchPoint.x, halfWidth, curlingAnimationFinish);
                        }
                    }
                };
                mHandler.post(animationRunnable);
            }
            else {
                //Just Go back
                final Runnable animationRunnable = new Runnable() {
                    public void run() {
                        if(mLastTouchPoint !=null) {
                            mLastTouchPoint.x -= speed;
                            invalidate();
                            if (mLastTouchPoint.x > -(getMeasuredWidth() / 2)) {
                                mHandler.post(this);
                            } else {
                                touchDisable = false;
                                touchFromRight = false;
                                touchFromLeft = false;
                            }
                        }
                    }
                };
                mHandler.post(animationRunnable);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void animationTouchForRight(final CurlingAnimationFinish curlingAnimationFinish){
        try {
            final int halfWidth = getMeasuredWidth() / 2;
            if (mLastTouchPoint.x > halfWidth) {
                //Just Go Back
                final Runnable animationRunnable = new Runnable() {
                    public void run() {
                        if(mLastTouchPoint !=null) {
                            mLastTouchPoint.x += speed;
                            invalidate();
                            if (mLastTouchPoint.x < getMeasuredWidth())
                                mHandler.post(this);
                            else {
                                touchDisable = false;
                                touchFromRight = false;
                                touchFromLeft = false;
                            }
                        }
                    }
                };
                mHandler.post(animationRunnable);
            } else {
                final Runnable animationRunnable = new Runnable() {
                    public void run() {
                        if (mLastTouchPoint != null) {
                            mLastTouchPoint.x -= speed;
                            invalidate();
                            if (mLastTouchPoint.x > -halfWidth) {
                                mHandler.post(this);
                            } else {
                                touchDisable = false;
                                touchFromRight = false;
                                touchFromLeft = false;
                            }

                            //page swipe event
                            //play right side
                            curlSwipeCheck_Right(mLastTouchPoint.x, halfWidth, curlingAnimationFinish);
                        }
                    }
                };

                mHandler.post(animationRunnable);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void curlSwipeCheck_Left(float x, float halfWidth, CurlingAnimationFinish curlingAnimationFinish){
        //make swipe
        if(x >= halfWidth && !curlEvent){
            curlEvent = true;
            if(curlCounter <=0) {
                curlingAnimationFinish.finish();
            }
        }
        //clear Swipe Event
        if(x >= (halfWidth * 2) - Utils.px2Dp(curlingOffSet, getResources())){
            curlEvent = false;
            curlCounter++;
        }
    }

    private void curlSwipeCheck_Right(float x, float halfWidth, CurlingAnimationFinish curlingAnimationFinish){
        //make swipe
        if(x <= halfWidth && !curlEvent){
            curlEvent = true;
            if(curlCounter <=0) {
                curlingAnimationFinish.finish();
            }
        }
        //clear Swipe Event
        float xVal = -(halfWidth/2);
        if(x <= xVal){
            curlEvent = false;
            curlCounter++;
        }
    }

    private void curlingGoBackRight(){
        final Runnable animationRunnable = new Runnable() {
            public void run() {
                if(mLastTouchPoint != null) {
                    mLastTouchPoint.x += 100;
                    invalidate();
                    if (mLastTouchPoint.x < getMeasuredWidth())
                        mHandler.post(this);
                    else {
                        touchDisable = false;
                        touchFromRight = false;
                        touchFromLeft = false;
                    }
                }
            }
        };
        mHandler.post(animationRunnable);
    }

    private void curlingGoBackLeft(){
        final Runnable animationRunnable = new Runnable() {
            public void run() {
                if (mLastTouchPoint != null) {
                    mLastTouchPoint.x -= 100;
                    invalidate();
                    if (mLastTouchPoint.x > -(getMeasuredWidth() / 2)) {
                        mHandler.post(this);
                    } else {
                        touchDisable = false;
                        touchFromRight = false;
                        touchFromLeft = false;
                    }
                }
            }
        };
        mHandler.post(animationRunnable);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        //region Single Tap
        if(customOnTapChangeListener !=null) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!firstTouch) {
                    tapTime = System.currentTimeMillis();
                    firstTouch = true;
                    tapMove = 0;
                    customOnTapChangeListener.onStart(event.getX(), event.getY());
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (firstTouch) {
                    if (System.currentTimeMillis() - tapTime <= 300 && tapMove < 3) {
                        tapTime = 0;
                        customOnTapChangeListener.onTap();
                    }
                    firstTouch = false;
                    tapMove = 0;
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                tapMove++;
            }
        }
        //endregion
        //region Swipe
        if(customOnSwipeListener !=null) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isSwipingMoving = false;
                downX = event.getX();
                downY = event.getY();
                initTime = System.currentTimeMillis();// Init Time

                //region curling effects
                if(isCurling){
                    drawEnable = true;
                    float pageVal = Float.valueOf(Reader_EPUB.currentRawPageStatic);
                    //Log.e(TAG, pageVal + " --- " + Reader_EPUB.currentRawPageStatic);
                    //InOrder To Change mLastPoint position from left side
                    if (event.getX() > (deviceWidth/2 + Utils.px2Dp(curlingOffSet, getResources()))) {
                        //if(pageVal >=100){
                        //    touchDisable = true;
                        //    Log.e(TAG, "NO PAGE FOUND Right: " + pageVal);
                        //   Toast.makeText(getContext(), "no page available", Toast.LENGTH_SHORT).show();
                        //}
                        //else {
                        curlCounter = 0;
                        touchFromRight = true;
                        touchFromLeft = false;
                        touchDisable = false;
                        //}

                    } else if (event.getX() < (deviceWidth/2 - Utils.px2Dp(curlingOffSet, getResources()))) {
                        //if(pageVal < 0){
                        //    touchDisable = true;
                        //    Log.e(TAG, "NO PAGE FOUND LEFT: " + pageVal);
                        //   Toast.makeText(getContext(), "no page available", Toast.LENGTH_SHORT).show();
                        //}
                        //else {
                        touchDisable = false;
                        curlCounter = 0;
                        touchFromRight = false;
                        touchFromLeft = true;
                        //}
                    }
                }
                //endregion
            }
            else if(event.getAction() == MotionEvent.ACTION_MOVE){
                //region Curling effects
                if(isCurling) {
                    if (!touchDisable) {
                        //check if it moves like +-20 x
                        float xPos = event.getX() - downX;if(xPos > Utils.px2Dp(5, getResources())){isSwipingMoving = true;}else if(xPos < -Utils.px2Dp(5, getResources())){isSwipingMoving = true;}
                        if (touchFromLeft) {
                            mLastTouchPoint = new Point((int) event.getX() - 330, (int) event.getY());
                            curlSwipeCheck_Left(mLastTouchPoint.x, getMeasuredWidth() / 2, new CurlingAnimationFinish() {
                                @Override
                                public void finish() {customOnSwipeListener.onSwipeRight();curlEvent = true;}
                            });
                            invalidate();
                        } else if (touchFromRight) {
                            mLastTouchPoint = new Point((int) event.getX(), (int) event.getY());
                            invalidate();
                        }
                    }
                }
                //endregion
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                float upX, upY;
                upX = event.getX();
                upY = event.getY();

                //Speed Calculation
                finalTIme = System.currentTimeMillis();
                long time = finalTIme - initTime;
                speed = (Math.abs(upX - downX)/ time) * multiplySpeed; if(speed< 80){speed =80;}

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                if(!isCurling){
                    //region Swipe System
                    //Swipe Horizontal
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        //Curling Effects Adding
                        if (deltaX < 0) {customOnSwipeListener.onSwipeRight();}
                        if (deltaX > 0) {customOnSwipeListener.onSwipeLeft();}
                    }
                    //Swipe Vertical
                    if (Math.abs(deltaY) > MIN_DISTANCE) {
                        if (deltaY < 0) {customOnSwipeListener.onSwipeDown();}
                        if (deltaY > 0) {customOnSwipeListener.onSwipeUp();}
                    }
                    //endregion
                }
                else{
                    //region Curling System
                    if(touchFromLeft){
                        touchDisable = true;
                        animationTouchForLeft(new CurlingAnimationFinish() {
                            @Override
                            public void finish() {
                                customOnSwipeListener.onSwipeRight();
                            }
                        });
                    }else if(touchFromRight){
                        touchDisable = true;
                        animationTouchForRight(new CurlingAnimationFinish() {
                            @Override
                            public void finish() {
                                customOnSwipeListener.onSwipeLeft();
                            }
                        });
                    }
                    //endregion
                }
            }
        }
        //endregion
        return super.onTouchEvent(event);
    }

    public void textSelectionChange(){

        if(touchFromRight){
            //From Right
            //Just Go Back
            curlingGoBackRight();
        }

        if(touchFromLeft){
            //From Left
            //Just Go back
            curlingGoBackLeft();
        }
    }
}
