package com.geowahl.geowahl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Katrin Rudisch on 01.06.2016.
 */
public class DrawView extends View {

    Paint gray;
    Paint green;

    public DrawView(Context context) {
        super(context);

        // create the Paint and set its color
        gray = new Paint();
        gray.setColor(Color.GRAY);
        green =  new Paint();
        green.setColor(Color.GREEN);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLUE);
        canvas.drawCircle(200, 200, 100, gray);
        canvas.drawCircle(70,70,50,green);
    }

}
