#include"Wave.h"
#include<cstring>

#include "android/log.h"
#include "math.h"

#define   LOG_TAG    "LOG_TEST"
#define   LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define   LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

Wave::Wave(int* pixels, int width,int height,int pixelFormat){
	init(pixels,width,height,pixelFormat);
	this->m_fScale = 1;
}

Wave::Wave(int* pixels, int width, int height, int pixelFormat, float scale){
	init(pixels, width, height, pixelFormat);
	this->m_fScale = scale;
}

void Wave::init(int* pixels, int width, int height, int pixelFormat){
	this->m_pOldPixels = pixels;
	this->m_nWidth = width;
	this->m_nHeight = height;
	this->m_nPixelFormat = pixelFormat;
	this->m_nLength = width*height;

	this->m_pNewPixels = new int[m_nLength]{};

	memcpy(m_pNewPixels, pixels, m_nLength*sizeof(int));

	m_pBuf1 = new short[m_nLength]{};
	m_pBuf2 = new short[m_nLength]{};

	this->m_nPowerRate = 3;

	setSourcePowerSize(m_nSourceRadius,m_nSourceDepth);
}

Wave::~Wave(){
	delete(this->m_pBuf1);
	delete(this->m_pBuf2);
	delete(this->m_pNewPixels);
	delete(this->m_pOldPixels);
	delete(this->m_pSourcePower);
	delete(this->m_nSourcePosition);

	LOGE("~Wave");
}

int Wave::getPowerRate(){
	return this->m_nPowerRate;
}

void Wave::setPowerRate(int rate){
	this->m_nPowerRate = rate;
}

int* Wave::getNewPixels()
{
	return this->m_pNewPixels;
}

int* Wave::getOldPixels()
{
	return this->m_pOldPixels;
}

int Wave::getWidth(){
	return this->m_nWidth;
}

int Wave::getHeight(){
	return this->m_nHeight;
}

int Wave::getLength(){
	return this->m_nLength;
}

int Wave::getPixelFormat(){
	return this->m_nPixelFormat;
}

void Wave::setSourcePowerSize(int radius, int depth){

	this->m_nSourceRadius = radius/m_fScale;
	this->m_nSourceDepth = depth;

	initSourcePower();
}

//初始化波能
void Wave::initSourcePower(){
//	LOGE("init source power first");
	int value = m_nSourceRadius*m_nSourceRadius;
	int diameter = m_nSourceRadius << 1;
	double rate = m_nSourceDepth / m_nSourceRadius;	//波源能量分布

	delete(m_pSourcePower);
	delete(m_nSourcePosition);

	m_pSourcePower = new int[value << 2]{};			//初始化波源能量为0
	m_nSourcePosition = new int[value << 2]{};		//初始化波源位置为0

	for (int x = 0; x <= m_nSourceRadius; ++x){
		for (int y = 0; y <= m_nSourceRadius; ++y){
			double distanceSquare = sqrt((m_nSourceRadius - x)*(m_nSourceRadius - x) + (m_nSourceRadius - y)*(m_nSourceRadius - y));

			if (distanceSquare <= m_nSourceRadius){
				int depth = m_nSourceDepth - distanceSquare*rate;

				m_nSourcePosition[y*diameter + x] = y*m_nWidth + x;
				m_pSourcePower[y*diameter + x] = depth;

				m_nSourcePosition[y*diameter + diameter - x] = y*m_nWidth + diameter - x;
				m_pSourcePower[y*diameter + diameter - x] = depth;

				m_nSourcePosition[(diameter - y)*diameter + x] = (diameter - y)*m_nWidth + x;
				m_pSourcePower[(diameter - y)*diameter + x] = depth;

				m_nSourcePosition[(diameter - y)*diameter + diameter - x] = (diameter - y)*m_nWidth + diameter - x;
				m_pSourcePower[(diameter - y)*diameter + diameter - x] = depth;

			}
		}
	}
}


void Wave::setSourcePower(float x, float y){
//	LOGE("setSourcePower first");

	int sourceX = x/m_fScale;
	int sourceY = y/m_fScale;
	// 判断坐标是否在屏幕范围内
	if ((sourceX + m_nSourceRadius) >= m_nWidth || (sourceY + m_nSourceRadius) >= m_nHeight
		|| (sourceX - m_nSourceRadius) <= 0 || (sourceY - m_nSourceRadius) <= 0) {
		return;
	}

	//设置波源
	int distance = (sourceY - m_nSourceRadius)*m_nWidth + sourceX - m_nSourceRadius;
	int size = (m_nSourceRadius*m_nSourceRadius) << 2;
	for (int i = 0; i < size; ++i){
		m_pBuf1[distance+m_nSourcePosition[i]] = m_pSourcePower[i];
	}
}

void Wave::setLinePower(int x, int y) {
	int mx = 0, my = 0;
	for (int posx = x - m_nSourceRadius; posx < x + m_nSourceRadius; ++posx) {
		for (int posy = y - m_nSourceRadius; posy < y + m_nSourceRadius; ++posy) {
			my = posy;
			mx = posx;
			m_pBuf1[m_nWidth * my + mx] = m_nSourceDepth>>1;
		}
	}
}

void Wave::moveLine(int startX, int startY, int endX, int endY){
	int dx = endX - startX;
	int dy = endY - startY;
	dx = (dx >= 0) ? dx : -dx;
	dy = (dy >= 0) ? dy : -dy;

	int currentX=startX;
	int currentY=startY;

	if (dx == 0 && dy == 0) {
		setSourcePower(currentX, currentY);
	}
	else if (dx == 0) {
		int yinc = (endY - startY > 0) ? 1 : -1;
		for (int i = 0; i < dy; ++i) {
			setSourcePower(currentX, currentY);
			currentY += yinc;
		}
	}
	else if (dy == 0) {
		int xinc = (endX - startX > 0) ? 1 : -1;
		for (int i = 0; i < dx; ++i) {
			setSourcePower(currentX,currentY);
			currentX += xinc;
		}
	}
	else if (dx > dy) {
		int p = (dy << 1) - dx;
		int inc1 = (dy << 1);
		int inc2 = ((dy - dx) << 1);
		int xinc = (endX - startX > 0) ? 1 : -1;
		int yinc = (endY - startY > 0) ? 1 : -1;

		for (int i = 0; i < dx; ++i) {
			setSourcePower(currentX, currentY);
			currentX += xinc;
			if (p < 0) {
				p += inc1;
			}
			else {
				currentY += yinc;
				p += inc2;
			}
		}
	}
	else if(dx>dy){
		int p = (dx << 1) - dy;
		int inc1 = (dx << 1);
		int inc2 = ((dx - dy) << 1);
		int xinc = (endX - startX > 0) ? 1 : -1;
		int yinc = (endY - startY > 0) ? 1 : -1;

		for (int i = 0; i < dy; ++i) {
			setSourcePower(currentX, currentY);
			currentY += yinc;
			if (p < 0) {
				p += inc1;
			}
			else {
				startX += xinc;
				p += inc2;
			}
		}
	}
	else{
		int xinc = (endX - startX > 0) ? 1 : -1;
		int yinc = (endY - startY > 0) ? 1 : -1;

		for (int i = 0; i < dx; ++i){
			setSourcePower(currentX, currentY);
			currentX += xinc;
			currentY += yinc;
		}
	}
}

void Wave::spreedRipple(){

	int length = m_nWidth*(m_nHeight -1);
	for (int i = m_nWidth; i < length; ++i){
		m_pBuf2[i] = ((m_pBuf1[i - 1] + m_pBuf1[i - m_nWidth] + m_pBuf1[i + 1] + m_pBuf1[i + m_nWidth]) >> 1) - m_pBuf2[i];
		m_pBuf2[i] -= m_pBuf2[i] >> m_nPowerRate;//波能衰减率
	}
	short* temp = m_pBuf1;
	m_pBuf1 = m_pBuf2;
	m_pBuf2 = temp;
}

void Wave::renderRipple(){

	int offset;
	int i = m_nWidth;
	for (int y = 1; y < m_nHeight - 1; ++y) {
		for (int x = 0; x < m_nWidth; ++x, ++i) {
			// 计算出偏移像素和原始像素的内存地址偏移量 :
			//offset = width * yoffset + xoffset
			offset = (m_nWidth * (m_pBuf1[i - m_nWidth] - m_pBuf1[i +
				m_nWidth])) + (m_pBuf1[i + 1] - m_pBuf1[i - 1]);
			// 判断坐标是否在范围内
			if (i + offset > 0 && i + offset < m_nLength) {
				m_pNewPixels[i] = m_pOldPixels[i + offset];
			}
			else {
				m_pNewPixels[i] = m_pOldPixels[i];
			}
		}
	}
}

void Wave::run(){
	spreedRipple();
	renderRipple();
}

void Wave::setPointPower(int x, int y, int depth){
	int sourceX = x / m_fScale;
	int sourceY = y / m_fScale;

	m_pBuf1[y*m_nWidth + x] = depth;
}
