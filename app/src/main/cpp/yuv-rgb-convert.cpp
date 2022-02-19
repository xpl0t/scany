#include <jni.h>

#define u_char unsigned char

short clamp(short val, short lo, short hi)
{
    return val < lo ? lo : val > hi ? hi : val;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xpl0t_scany_extensions_ImageProxyKt_yuvToRgb(
        JNIEnv *env, jclass clazz,
        jint width, jint height,
        jint uvRowStride, jint uvPixelStride,
        jobject y, jobject u, jobject v,
        jobject rgb) {

    auto* yBuf = (u_char*) env->GetDirectBufferAddress(y);
    auto* uBuf = (char*) env->GetDirectBufferAddress(u);
    auto* vBuf = (char*) env->GetDirectBufferAddress(v);
    auto* rgbBuf = (u_char*) env->GetDirectBufferAddress(rgb);

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int yIdx = (y * width) + x;
            auto yVal = (float) yBuf[yIdx];

            int uvx = x / 2;
            int uvy = y / 2;
            int uvIdx = (uvy * uvRowStride) + (uvx * uvPixelStride);
            auto uVal = (float) (uBuf[uvIdx] - 128);
            auto vVal = (float) (vBuf[uvIdx] - 128);

            auto r = (short) (yVal + 1.370705 * vVal);
            auto g = (short) (yVal - (0.698001 * vVal) - (0.337633 * uVal));
            auto b = (short) (yVal + 1.732446 * uVal);

            r = clamp(r, 0, 255);
            g = clamp(g, 0, 255);
            b = clamp(b, 0, 255);

            int rgbIdx = yIdx * 3;
            rgbBuf[rgbIdx] = (u_char) r;
            rgbBuf[rgbIdx + 1] = (u_char) g;
            rgbBuf[rgbIdx + 2] = (u_char) b;
        }
    }
}
