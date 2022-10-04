// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("empalinksample");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("empalinksample")
//      }
//    }
#include "jni.h"
#include "iostream"
//#include "Empatica_inferencing.h"

int8_t example[1][2] = {{1,2}};


extern "C"
JNIEXPORT void JNICALL
Java_com_empatica_sample_MainActivity_setSampleArray(JNIEnv *env, jobject thiz,
                                                     jfloatArray data_arr2_c) {
    // TODO: implement setSampleArray()


}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_empatica_sample_MainActivity_getSampleArray(JNIEnv *env, jobject thiz) {
    // TODO: implement getSampleArray()

    return NULL;
}

