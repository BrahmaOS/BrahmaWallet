#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_io_brahmaos_wallet_brahmawallet_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello BramhaOS";
    return env->NewStringUTF(hello.c_str());
}
