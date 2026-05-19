#ifndef FASTTERMINAL_H
#define FASTTERMINAL_H

#include <jni.h>

/**
 * @file fastterminal.h
 * @brief JNI Exported Method Declarations for the FastTerminal Windows native hooks library.
 * 
 * Provides native Windows console querying capabilities (dimensions, raw input, active focus,
 * cell font dimensions, and mouse telemetry) to Java wrapper classes.
 */

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Queries the visible terminal window size in characters.
 * 
 * @param env Pointer to the JNI environment.
 * @param clazz The calling Java class reference.
 * @return jintArray An array containing [columns, rows], or NULL if query fails.
 */
JNIEXPORT jintArray JNICALL Java_fastterminal_FastTerminal_getTerminalSize(JNIEnv* env, jclass clazz);

/**
 * @brief Configures standard console mode input/output flags (raw mode toggle).
 * 
 * Sets ENABLE_WINDOW_INPUT and ENABLE_MOUSE_INPUT, and disables line editing/echoing
 * for smooth real-time keypresses and mouse telemetry.
 * 
 * @param env Pointer to the JNI environment.
 * @param clazz The calling Java class reference.
 * @param enableRaw jboolean flag to enable (true) or disable/restore (false) raw mode.
 */
JNIEXPORT void JNICALL Java_fastterminal_FastTerminal_setRawMode(JNIEnv* env, jclass clazz, jboolean enableRaw);

/**
 * @brief Retrieves hardware console window dimensions, font sizes, and layout.
 * 
 * Returns an array containing the screen rect bounding dimensions, client area offsets,
 * and current console font width/height cell properties.
 * 
 * @param env Pointer to the JNI environment.
 * @param clazz The calling Java class reference.
 * @return jintArray An array containing [left, top, clientX, clientY, fontWidth, fontHeight], or NULL on error.
 */
JNIEXPORT jintArray JNICALL Java_fastterminal_FastTerminal_getConsoleWindowInfo(JNIEnv* env, jclass clazz);

/**
 * @brief Determines if the host terminal window currently holds active OS focus.
 * 
 * Walks parent and root owner window structures to correctly identify focus when
 * running embedded inside modern terminal hosts like wt.exe (Windows Terminal).
 * 
 * @param env Pointer to the JNI environment.
 * @param clazz The calling Java class reference.
 * @return jboolean JNI_TRUE if focused, JNI_FALSE otherwise.
 */
JNIEXPORT jboolean JNICALL Java_fastterminal_FastTerminal_isTerminalFocused(JNIEnv* env, jclass clazz);

/**
 * @brief Checks if the mouse cursor is hovering over the active terminal window boundary.
 * 
 * Uses screen space coordinate translations to check mouse bounds dynamically.
 * 
 * @param env Pointer to the JNI environment.
 * @param clazz The calling Java class reference.
 * @return jboolean JNI_TRUE if mouse is hovering over the terminal, JNI_FALSE otherwise.
 */
JNIEXPORT jboolean JNICALL Java_fastterminal_FastTerminal_isMouseOverTerminal(JNIEnv* env, jclass clazz);

#ifdef __cplusplus
}
#endif

#endif // FASTTERMINAL_H
