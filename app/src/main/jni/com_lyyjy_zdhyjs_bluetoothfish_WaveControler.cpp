//
// Created by Administrator on 2016/3/29.
//
#include "com_lyyjy_zdhyjs_bluetoothfish_WaveControler.h"
#include "android/bitmap.h"
#include "memory.h"
#include "Wave.h"

JNIEXPORT jint JNICALL Java_com_lyyjy_zdhyjs_bluetoothfish_WaveControler_createCClass
        (JNIEnv *env, jobject,jobject bitmap)
{
    AndroidBitmapInfo bmpInfo={0};

    if(AndroidBitmap_getInfo(env,bitmap,&bmpInfo)<0)
    {
        return -1;
    }
    int* dataFromBmp=NULL;
    if(AndroidBitmap_lockPixels(env,bitmap,(void**)&dataFromBmp))
    {
        return -1;
    }

    int length=bmpInfo.height*bmpInfo.width;

    int* pPixels=new int[length];
    memcpy(pPixels,dataFromBmp,length* sizeof(int));
    Wave* wave=new Wave(pPixels,bmpInfo.width,bmpInfo.height,bmpInfo.format);
    AndroidBitmap_unlockPixels(env,bitmap);

    return (int)wave;
}

JNIEXPORT jint JNICALL Java_com_lyyjy_zdhyjs_bluetoothfish_WaveControler_createCClassWithScale
        (JNIEnv *env, jobject,jobject bitmap,jfloat scale)
{
    AndroidBitmapInfo bmpInfo={0};

    if(AndroidBitmap_getInfo(env,bitmap,&bmpInfo)<0)
    {
        return -1;
    }
    int* dataFromBmp=NULL;
    if(AndroidBitmap_lockPixels(env,bitmap,(void**)&dataFromBmp))
    {
        return -1;
    }

    int length=bmpInfo.height*bmpInfo.width;

    int* pPixels=new int[length];
    memcpy(pPixels,dataFromBmp,length* sizeof(int));
    Wave* wave=new Wave(pPixels,bmpInfo.width,bmpInfo.height,bmpInfo.format,scale);
    AndroidBitmap_unlockPixels(env,bitmap);

    return (int)wave;
}

JNIEXPORT void JNICALL Java_com_lyyjy_zdhyjs_bluetoothfish_WaveControler_deleteCClass
        (JNIEnv *, jobject,Wave* pAddress)
{
    delete(pAddress);
}

JNIEXPORT void JNICALL Java_com_lyyjy_zdhyjs_bluetoothfish_WaveControler_rippleBitmap
        (JNIEnv *env, jobject,jobject bitmap,Wave* pAddress)
{
    int* dataFromBmp=NULL;
    if(AndroidBitmap_lockPixels(env,bitmap,(void**)&dataFromBmp))
    {
        return;
    }
//    time=getCurrentTime();
    pAddress->run();
//    LOGE("run time:%d",getCurrentTime()-time);
//    time=getCurrentTime();

    int *pNewPixels = pAddress->getNewPixels();

//    time=getCurrentTime();
    memcpy(dataFromBmp,pNewPixels, pAddress->getLength()* sizeof(int));
//    LOGE("memcpy time:%d",getCurrentTime()-time);
//    time=getCurrentTime();

    AndroidBitmap_unlockPixels(env,bitmap);
}

JNIEXPORT void JNICALL Java_com_lyyjy_zdhyjs_bluetoothfish_WaveControler_setSourcePower
        (JNIEnv *, jobject,float x ,float y, Wave* pAddress)
{
    pAddress->setSourcePower(x,y);
}

//设置波源大小
JNIEXPORT void JNICALL Java_com_lyyjy_zdhyjs_bluetoothfish_WaveControler_setSourcePowerSize
        (JNIEnv *, jobject,jint radius,jint depth,Wave* pAddress)
{
    pAddress->setSourcePowerSize(radius,depth);
}

//水面滑动
JNIEXPORT void JNICALL Java_com_lyyjy_zdhyjs_bluetoothfish_WaveControler_moveLine
        (JNIEnv *, jobject,jfloat startX,jfloat startY,jfloat endX,jfloat endY,Wave* pAddress)
{
    pAddress->moveLine(startX,startY,endX,endY);
}