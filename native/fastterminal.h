#ifndef FASTTERMINAL_H
#define FASTTERMINAL_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

// Exported native method declarations for fastterminal.FastTerminal
JNIEXPORT jintArray JNICALL Java_fastterminal_FastTerminal_getTerminalSize(JNIEnv* env, jclass clazz);
JNIEXPORT void JNICALL Java_fastterminal_FastTerminal_setRawMode(JNIEnv* env, jclass clazz, jboolean enableRaw);
JNIEXPORT jintArray JNICALL Java_fastterminal_FastTerminal_getConsoleWindowInfo(JNIEnv* env, jclass clazz);
JNIEXPORT jboolean JNICALL Java_fastterminal_FastTerminal_isTerminalFocused(JNIEnv* env, jclass clazz);
JNIEXPORT jboolean JNICALL Java_fastterminal_FastTerminal_isMouseOverTerminal(JNIEnv* env, jclass clazz);

#ifdef __cplusplus
}
#endif

#endif // FASTTERMINAL_H
