#include "fastterminal.h"
#include <windows.h>
#include <stdio.h>

/**
 * @file fastterminal.cpp
 * @brief Native JNI C++ implementation for FastTerminal console hooks
 */

// ============================================================================
// DLL Entry Point
// ============================================================================
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
