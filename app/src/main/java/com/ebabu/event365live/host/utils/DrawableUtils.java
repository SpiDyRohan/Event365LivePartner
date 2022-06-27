package com.ebabu.event365live.host.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import androidx.core.content.res.ResourcesCompat;

import com.ebabu.event365live.host.R;

public class DrawableUtils {

    public static Bitmap getTempProfilePic(Context context,String fullName){
        StringBuilder shortName=new StringBuilder();
        if(fullName==null)return null;

        String[] nameArray=fullName.split(" ");
        shortName.append(nameArray[0].charAt(0));

        if(nameArray.length>1)
            shortName.append(nameArray[1].charAt(0));

        Bitmap myBitmap = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.ellipse);

        Bitmap mutableBitmap = myBitmap.copy(Bitmap.Config.ARGB_8888, true);

        int w=mutableBitmap.getWidth();
        int h=mutableBitmap.getHeight();

        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE); // Text Color
        paint.setTextSize(h/2); // Text Size
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(ResourcesCompat.getFont(context,R.font.helvetica_medium));
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern


        canvas.drawBitmap(myBitmap, 0, 0, paint);
        canvas.drawText(shortName.toString().toUpperCase(), w/2, h-h/3, paint);
        return mutableBitmap;
    }
}
