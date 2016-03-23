package com.sgj.john.mytimber.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.Random;

/**
 * Created by John on 2016/3/9.
 * 音乐播放时，在条目上添加播放标识 --- > 律动
 *
 * a music visualizer sort of animation (with random data)
 *
 */
public class MusicVisualizer extends View {

    Random random = new Random();

    Paint paint = new Paint();

    public MusicVisualizer(Context context) {
        super(context);
        new MusicVisualizer(context, null);
    }

    public MusicVisualizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        //start runnable
        removeCallbacks(animateView);
        post(animateView);
    }

    public MusicVisualizer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private Runnable animateView = new Runnable() {
        @Override
        public void run() {
            //run every 150 ms
            postDelayed(this, 150);

            invalidate();
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //set paint style, Style.FILL will fill the color, Style.STROKE will stroke the color
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(getDimensionInPixel(0), getHeight() - (20 + random.nextInt((int) (getHeight() / 1.5f) - 19)), getDimensionInPixel(7), getHeight(), paint);
        canvas.drawRect(getDimensionInPixel(10), getHeight() - (20 + random.nextInt((int) (getHeight() / 1.5f) - 19)), getDimensionInPixel(17), getHeight(), paint);
        canvas.drawRect(getDimensionInPixel(20), getHeight() - (20 + random.nextInt((int) (getHeight() / 1.5f) - 19)), getDimensionInPixel(27), getHeight(), paint);

    }

    //get all dimensions in dp so that views behaves properly on different screen resolutions
    private float getDimensionInPixel(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void setColor(int color){
        paint.setColor(color);
        invalidate();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(visibility == View.VISIBLE){
            removeCallbacks(animateView);
            post(animateView);
        }else if (visibility == GONE){
            removeCallbacks(animateView);
        }
    }
}
