#include "fastterminal.h"
#include <windows.h>
#include <stdio.h>

/**
 * @file fastterminal.cpp
 * @brief Native JNI C++ implementation for FastTerminal console hooks.
 * 
 * Provides high-performance integration with the Win32 Console APIs to query 
 * layout, toggle input modes, walk parent focus chains, and track pointer locations.
 */

// ============================================================================
// DLL Entry Point
// ============================================================================

/**
 * @brief Dynamic-Link Library entry point function.
 * 
 * Configures optimization flags on thread attachment.
 * 
 * @param hModule Handle to the DLL module.
 * @param ul_reason_for_call Reason flag calling the entry point.
 * @param lpReserved Reserved parameter.
 * @return BOOL TRUE on success, FALSE otherwise.
 */
BOOL APIENTRY DllMain(HMODULE hModule, DWORD ul_reason_for_call, LPVOID lpReserved) {
    switch (ul_reason_for_call) {
        case DLL_PROCESS_ATTACH:
            DisableThreadLibraryCalls(hModule);
            break;
        case DLL_PROCESS_DETACH:
            break;
    }
    return TRUE;
}

// ============================================================================
// JNI Implementations
// ============================================================================

/**
 * @brief Queries current console buffer width (columns) and visible window height.
 * 
 * Implements: JNIEXPORT jintArray JNICALL Java_fastterminal_FastTerminal_getTerminalSize
 * Retrieves the screen buffer info using Win32 `GetConsoleScreenBufferInfo`.
 * 
 * @param env Pointer to the JNI environment.
 * @param clazz The calling Java class reference.
 * @return jintArray [cols, rows] console dimensions, or NULL on memory allocation failure.
 */
JNIEXPORT jintArray JNICALL Java_fastterminal_FastTerminal_getTerminalSize(JNIEnv* env, jclass clazz) {
    jintArray result = env->NewIntArray(2);
    if (result == NULL) return NULL;
    
    jint dims[2] = { 120, 30 }; // Premium default fallback geometry
    
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    if (hConsole != INVALID_HANDLE_VALUE && hConsole != NULL) {
        CONSOLE_SCREEN_BUFFER_INFO csbi;
        if (GetConsoleScreenBufferInfo(hConsole, &csbi)) {
            dims[0] = csbi.srWindow.Right - csbi.srWindow.Left + 1; // Retrieve actual visible window column width!
            dims[1] = csbi.srWindow.Bottom - csbi.srWindow.Top + 1;
        }
    }
    
    env->SetIntArrayRegion(result, 0, 2, dims);
    return result;
}

/**
 * @brief Configures standard raw console modes (echo off, direct inputs, etc.) via SetConsoleMode.
 * 
 * Implements: JNIEXPORT void JNICALL Java_fastterminal_FastTerminal_setRawMode
 * Saves the original mode on first activation to allow safe cleanup later.
 * 
 * @param env Pointer to the JNI environment.
 * @param clazz The calling Java class reference.
 * @param enableRaw True to set direct raw terminal input flags, false to restore original console state.
 */
JNIEXPORT void JNICALL Java_fastterminal_FastTerminal_setRawMode(JNIEnv* env, jclass clazz, jboolean enableRaw) {
    HANDLE hInput = GetStdHandle(STD_INPUT_HANDLE);
    if (hInput == INVALID_HANDLE_VALUE || hInput == NULL) return;
    
    static DWORD originalMode = 0;
    static bool hasOriginalMode = false;
    
    if (enableRaw) {
        if (!hasOriginalMode) {
            GetConsoleMode(hInput, &originalMode);
            hasOriginalMode = true;
        }
        DWORD rawMode = ENABLE_EXTENDED_FLAGS | ENABLE_WINDOW_INPUT | ENABLE_MOUSE_INPUT;
        SetConsoleMode(hInput, rawMode);
    } else {
        if (hasOriginalMode) {
            SetConsoleMode(hInput, originalMode);
        }
    }
}

/**
 * @brief Retrieves the console window's screen rect, client offset, and font character cell size.
 * 
 * Implements: JNIEXPORT jintArray JNICALL Java_fastterminal_FastTerminal_getConsoleWindowInfo
 * Useful for high-precision cursor positioning calculations, especially in custom UI elements.
 * 
 * @param env Pointer to the JNI environment.
 * @param clazz The calling Java class reference.
 * @return jintArray Int array holding size and coordinate offsets, or NULL on error.
 */
JNIEXPORT jintArray JNICALL Java_fastterminal_FastTerminal_getConsoleWindowInfo(JNIEnv* env, jclass clazz) {
    HWND hwnd = GetConsoleWindow();
    RECT rect = {0, 0, 0, 0};
    GetWindowRect(hwnd, &rect);
    
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    CONSOLE_FONT_INFO fontInfo;
    int fontWidth = 8;
    int fontHeight = 16;
    if (GetCurrentConsoleFont(hConsole, FALSE, &fontInfo)) {
        COORD fontSize = GetConsoleFontSize(hConsole, fontInfo.nFont);
        fontWidth = fontSize.X;
        fontHeight = fontSize.Y;
    }
    
    POINT clientOffset = {0, 0};
    ClientToScreen(hwnd, &clientOffset);
    
    jintArray result = env->NewIntArray(6);
    if (result == NULL) return NULL;
    
    jint info[6] = {
        (jint)rect.left, (jint)rect.top,
        (jint)clientOffset.x, (jint)clientOffset.y,
        (jint)fontWidth, (jint)fontHeight
    };
    
    env->SetIntArrayRegion(result, 0, 6, info);
    return result;
}

/**
 * @brief Checks if our console window is the active focused window in Windows.
 * 
 * Implements: JNIEXPORT jboolean JNICALL Java_fastterminal_FastTerminal_isTerminalFocused
 * Correctly walks root ancestor boundaries for Modern Windows Terminal (wt.exe) host environments.
 * 
 * @param env Pointer to the JNI environment.
 * @param clazz The calling Java class reference.
 * @return jboolean True if focused, False otherwise.
 */
JNIEXPORT jboolean JNICALL Java_fastterminal_FastTerminal_isTerminalFocused(JNIEnv* env, jclass clazz) {
    HWND hwndConsole = GetConsoleWindow();
    if (hwndConsole == NULL) return JNI_FALSE;
    
    HWND hwndForeground = GetForegroundWindow();
    if (hwndForeground == NULL) return JNI_FALSE;
    
    if (hwndForeground == hwndConsole) return JNI_TRUE;
    
    // Check root ancestors and owner window trees (crucial for wt.exe / Windows Terminal)
    if (GetAncestor(hwndConsole, GA_ROOT) == hwndForeground) return JNI_TRUE;
    if (GetAncestor(hwndConsole, GA_ROOTOWNER) == hwndForeground) return JNI_TRUE;
    
    // Walk parent hierarchy to handle embedded hosts
    HWND parent = hwndConsole;
    while ((parent = GetParent(parent)) != NULL) {
        if (parent == hwndForeground) return JNI_TRUE;
    }
    
    return JNI_FALSE;
}

/**
 * @brief Checks if the mouse cursor is hovering over our terminal window.
 * 
 * Implements: JNIEXPORT jboolean JNICALL Java_fastterminal_FastTerminal_isMouseOverTerminal
 * 
 * @param env Pointer to the JNI environment.
 * @param clazz The calling Java class reference.
 * @return jboolean True if mouse is hovering over terminal, False otherwise.
 */
JNIEXPORT jboolean JNICALL Java_fastterminal_FastTerminal_isMouseOverTerminal(JNIEnv* env, jclass clazz) {
    HWND hwndConsole = GetConsoleWindow();
    if (hwndConsole == NULL) return JNI_FALSE;

    POINT pt;
    if (GetCursorPos(&pt)) {
        HWND hwndUnderMouse = WindowFromPoint(pt);
        if (hwndUnderMouse == hwndConsole || IsChild(hwndConsole, hwndUnderMouse)) {
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}
