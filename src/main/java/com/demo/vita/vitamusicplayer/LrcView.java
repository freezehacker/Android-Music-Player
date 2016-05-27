package com.demo.vita.vitamusicplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sjk on 2016/4/17.
 */
public class LrcView extends TextView {

    private float width, height, textWidth, textHeight;
    private Paint currentPaint, notCurrentPaint;
    private int index;
    private List<LyricsBean> lrcList;

    public void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);

        currentPaint = new Paint();
        currentPaint.setAntiAlias(true);
        currentPaint.setTextAlign(Paint.Align.CENTER);
        currentPaint.setTextSize(20);
        currentPaint.setColor(Color.rgb(0, 0, 250));

        notCurrentPaint = new Paint();
        notCurrentPaint.setAntiAlias(true);
        notCurrentPaint.setTextAlign(Paint.Align.CENTER);
        notCurrentPaint.setTextSize(12);
        notCurrentPaint.setColor(Color.rgb(0, 250, 0));
    }

    public void setLrcList(List<LyricsBean> lrcList) {
        this.lrcList = lrcList;
    }

    public List<LyricsBean> getLrcList() {
        return lrcList;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LrcView(Context context) {
        super(context);
        init();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null || lrcList == null) {
            return;
        }

        setText("");
        float w = width / 2;
        canvas.drawText(lrcList.get(index).getSentence(), w, height / 2, currentPaint);

        float y = height / 2;
        for (int i = index - 1; i >= 0; --i) {
            y -= textHeight;
            canvas.drawText(lrcList.get(i).getSentence(), w, y, notCurrentPaint);
        }

        y = height / 2;
        for (int i = index + 1, siz = lrcList.size(); i < siz; ++i) {
            y += textHeight;
            canvas.drawText(lrcList.get(i).getSentence(), w, y, notCurrentPaint);
        }
    }
}
