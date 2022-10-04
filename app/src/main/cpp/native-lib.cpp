#include <jni.h>
//#include "Empatica_inferencing.h"
#include <string>
#include <cstdlib>
//#include "C:/Users/adriv/AndroidStudioProjects/e4link-sample-project-android-main/app/libnode/include/node/node.h"
int debug_nn = 0;

extern "C"
JNIEXPORT jint JNICALL
Java_com_empatica_sample_MainActivity_startNodeWithArguments(JNIEnv *env, jobject thiz,
                                                             jintArray arguments) {
    // TODO: implement startNodeWithArguments()
    return 10;
}
