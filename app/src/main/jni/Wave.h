#ifndef _WAVE_H
#define _WAVE_H

#include <stddef.h>

class Wave
{
private:
	int* m_pOldPixels= NULL;	//原始像素点
	int* m_pNewPixels= NULL; //渲染后的像素点
	int m_nWidth;		//图片宽度
	int m_nHeight;	//图片高度
	int m_nLength;	//像素点总数
	int m_nPixelFormat;	//图片格式

	int m_nPowerRate=1;	//波能衰减率(power-=power>>m_powerRater)

	float m_fScale=1;	//图片伸缩比例

	int m_nSourceRadius=50;	//波源半径
	int m_nSourceDepth=50;	//波源深度

	short* m_pBuf1= NULL;		//波能缓冲区1
	short* m_pBuf2= NULL;		//波能缓冲区2

	int* m_pSourcePower= NULL;	//波源数据
	int* m_nSourcePosition= NULL;	//波源位置
public:
	void setPixels(int*);
	void setWidth(int);
	void setHeight(int);
	void setPixelFormat(int);
	void setPowerRate(int rate);

	int* getNewPixels();
	int* getOldPixels();
	int getWidth();
	int getHeight();
	int getLength();
	int getPowerRate();
	int getPixelFormat();

	Wave(int* pixels,int width,int height,int pixelFormat);
	Wave(int* pixels, int width, int height, int pixelFormat,float scale);
	~Wave();

	void setSourcePowerSize(int radius,int depth);		//设置波源大小
	void setPointPower(int x, int y, int depth);		//设置任一点波源大小
	void setSourcePower(float x, float y);	//设置波源
	void moveLine(int startX,int startY,int endX,int endY);

	void run();			//开始渲染


private:
	void spreedRipple();		//水波扩散
	void renderRipple();		//水波渲染
	void init(int* pixels, int width, int height, int pixelFormat);	//初始化
	void initSourcePower();	//以（0,0）点为正方形左上角，初始化波源大小
	void setLinePower(int x,int y);
};

#endif