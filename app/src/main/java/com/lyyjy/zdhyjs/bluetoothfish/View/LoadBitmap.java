package com.lyyjy.zdhyjs.bluetoothfish.View;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created by Administrator on 2015/9/6.
 */
public class LoadBitmap {
    private String TAG="LoadBitmap";
    private Bitmap mBitmap;
    private int screenWidth;
    private int screenHeight;

    public Bitmap getBitmap() {
        return mBitmap;
    }

    /*以一定像素点加载图片*/
    public LoadBitmap(Resources resources, int resId, int minSideLength, int maxNumOfPixels, int screenWidth, int screenHeight){
        /*获取屏幕长宽*/
        this.screenHeight=screenHeight;
        this.screenWidth=screenWidth;

        /*解析图片*/
        mBitmap=decodeSampleBitmapFromResource(resources,resId,minSideLength,maxNumOfPixels);
//        Log.e(TAG, "pixels:" + mBitmap.getHeight() * mBitmap.getWidth());
    }

    private static Bitmap cutBitmap(Bitmap bitmap,float screenWidth,float screenHeight){
        int heightCut=0,widthCut=0;
        int height=bitmap.getHeight();
        int width=bitmap.getWidth();
        if (height < screenHeight || width < screenWidth) {
            if (screenHeight / height > screenWidth / width) {
                heightCut = height;
                widthCut = (int) (height * screenWidth / screenHeight);
            } else {
                widthCut = width;
                heightCut = (int) (width * screenHeight / screenWidth);
            }
        } else {
            if (width >= screenWidth) {
                widthCut = (int) screenWidth;
            }
            if (height >= screenHeight) {
                heightCut = (int) screenHeight;
            }
        }
        Bitmap newBitmap= Bitmap.createBitmap(bitmap, 0, 0, widthCut, heightCut);
        bitmap.recycle();
        return newBitmap;
    }

    public Bitmap decodeSampleBitmapFromResource(Resources resources,int resId,int minSideLength,int maxNumOfPixels){
        final BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeResource(resources, resId, options);

        options.inSampleSize=computeSampleSize(options,minSideLength,maxNumOfPixels);
        options.inJustDecodeBounds=false;
        Bitmap bitmap= BitmapFactory.decodeResource(resources, resId, options);
        bitmap=cutBitmap(bitmap,screenWidth,screenHeight);

        return bitmap;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :(int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
    
    
}
