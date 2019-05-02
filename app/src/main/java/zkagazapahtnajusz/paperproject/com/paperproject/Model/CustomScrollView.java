package zkagazapahtnajusz.paperproject.com.paperproject.Model;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView {
    private CustomOnScrollChangeListener customOnScrollChangeListener;
    private CustomOnTapChangeListener customOnTapChangeListener;

    long tapTime =0;
    boolean firstTouch = false;
    int tapMove = 0;

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (customOnScrollChangeListener!=null)
        {
            if (t>oldt)
            {
                customOnScrollChangeListener.onScrollDown();
            }
            else if (t<oldt){
                customOnScrollChangeListener.onScrollUp();
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            if(customOnTapChangeListener != null && !firstTouch) {
                tapTime = System.currentTimeMillis();
                firstTouch = true;
                tapMove = 0;
            }
        }
        else if(ev.getAction() == MotionEvent.ACTION_UP){
            if(customOnTapChangeListener != null && firstTouch) {
                if (System.currentTimeMillis() - tapTime <= 300 && tapMove < 5) {
                    tapTime = 0;
                    customOnTapChangeListener.onTap();
                }
                firstTouch =false;
                tapMove =0;
            }
        }
        else if(ev.getAction() == MotionEvent.ACTION_MOVE){
            tapMove ++;
        }
        return super.onTouchEvent(ev);
    }

    public void setCustomOnScrollChangeListener(CustomOnScrollChangeListener customOnScrollChangeListener) {
        this.customOnScrollChangeListener = customOnScrollChangeListener;
    }

    public CustomOnScrollChangeListener getCustomOnScrollChangeListener() {
        return customOnScrollChangeListener;
    }

    public CustomOnTapChangeListener getCustomOnTapChangeListener() {
        return customOnTapChangeListener;
    }

    public void setCustomOnTapChangeListener(CustomOnTapChangeListener customOnTapChangeListener) {
        this.customOnTapChangeListener = customOnTapChangeListener;
    }

    public interface CustomOnScrollChangeListener{
        void onScrollUp();
        void onScrollDown();
    }

    public interface CustomOnTapChangeListener{
        void onTap();
    }
}
