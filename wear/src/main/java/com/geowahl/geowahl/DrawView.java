package com.geowahl.geowahl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;


public class DrawView extends View {

    //Paint gray = new Paint();
    //Paint green = new Paint();
    //Paint red = new Paint();
    Paint black = new Paint();
    //Paint blue = new Paint();
    Rect rectangle;
    RectF rect;

    ArrayList<String> colorList;
    ArrayList<String> partyList;
    ArrayList<String> nameList;
    ArrayList<String> percentList;


    public DrawView(Context context, ArrayList<String> colorList, ArrayList<String> partyList, ArrayList<String> nameList, ArrayList<String> votesList, ArrayList<String> percentList) {
        super(context);

        this.colorList = colorList;
        this.partyList = partyList;
        this.nameList = nameList;
        this.percentList = percentList;

        // create the Paint and set its color
        //gray.setColor(Color.GRAY);
        //green.setColor(Color.GREEN);
        //red.setColor(Color.RED);
        black.setColor(Color.BLACK);

        //rectangle = new Rect(x, y, sideLength, sideLength);
        rectangle = new Rect(50, 50, 100, 100);
        rect = new RectF(50,50,100,100);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        float width = (float)getWidth();
        float height = (float)getHeight();
        float radius;

        if (width > height){
            radius = height/3;
        }else{
            radius = width/3;
        }

        Path path = new Path();
        path.addCircle(width/2,
               height/2, radius,
                Path.Direction.CW);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        float center_x, center_y;
        center_x = width/2;
        center_y = height/2;

        final RectF oval = new RectF();
        oval.set(center_x - radius,
                center_y - radius,
                center_x + radius,
                center_y + radius);

        //drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint)
        /*float spoe = (float) 30.22;
        float oevp = (float) 20.78;
        float fpoe = (float) 39.50;
        float lugner = (float)9.50;

        float spoeW = spoe * 360 /100;
        float oevpW = oevp * 360/100;
        float fpoeW = fpoe * 360/100;
        float lugnerW = lugner * 360/100;

        Log.d("spö" , String.valueOf(spoeW));
        Log.d("övp" , String.valueOf(oevpW));
        Log.d("fpö" , String.valueOf(fpoeW));
        Log.d("lugner" , String.valueOf(lugnerW));

        canvas.drawArc(oval, 0, spoeW, true, paint);
        canvas.drawArc(oval, spoeW, oevpW, true, gray);
        canvas.drawArc(oval, spoeW+oevpW, fpoeW, true, green);
        canvas.drawArc(oval,  spoeW+oevpW+fpoeW, lugnerW, true, red );*/

        if(nameList != null || partyList != null || percentList != null || colorList != null) {
            float tmp = 0;
            for (int i = 0; i < nameList.size(); i++) {
                for (int j = 0; j < partyList.size(); j++) {
                    if (partyList.get(j).equalsIgnoreCase(nameList.get(i))) {
                        float value = Float.parseFloat(percentList.get(i)) * 360 / 100;
                        Paint color = new Paint();
                        color.setColor(Color.parseColor(colorList.get(j)));
                        color.setStyle(Paint.Style.FILL);
                        canvas.drawArc(oval, tmp, value, true, color);
                        tmp += value;
                        //ZEICHNE Donutteil mit der Farbe colorList(j) -->weil für colorList und partyList ja die Indizes zusammenpassen
                        //Farben sind übrigens HEX Werte
                    }
                }
            }
        }




        canvas.drawCircle(center_x,
                center_y, 50,black);


    }

}
