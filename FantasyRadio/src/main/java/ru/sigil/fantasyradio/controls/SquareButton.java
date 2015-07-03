package ru.sigil.fantasyradio.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

/**
 * Created by namelessone
 * 06.09.14.
 */

public class SquareButton extends Button
{

    public SquareButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public SquareButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SquareButton(Context context)
    {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //Get canvas width and height
        int w = View.MeasureSpec.getSize(widthMeasureSpec);
        int h = View.MeasureSpec.getSize(heightMeasureSpec);

        w = Math.min(w, h);
        h = w;

        setMeasuredDimension(w, h);
    }


}