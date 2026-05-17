#ifndef FASTTERMINAL_H
#define FASTTERMINAL_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

// Exported native method declarations for fastterminal.FastTerminal
JNIEXPORT jintArray JNICALL Java_fastterminal_FastTerminal_getTerminalSize(JNIEnv* env, jclass clazz);
JNIEXPORT void JNICALL Java_fastterminal_FastTerminal_setRawMode(JNIEnv* env, jclass clazz, jboolean enableRaw);

#ifdef __cplusplus
}
#endif

#endif // FASTTERMINAL_H
