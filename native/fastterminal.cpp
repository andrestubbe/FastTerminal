#include "fastXXX.h"
#include <windows.h>
#include <stdio.h>

/**
 * @file fastXXX.cpp
 * @brief Native JNI implementation for FastXXX
 */

// ============================================================================
// DLL Entry Point
// ============================================================================
BOOL APIENTRY DllMain(HMODULE hModule, DWORD ul_reason_for_call, LPVOID lpReserved) {
    switch (ul_reason_for_call) {
        case DLL_PROCESS_ATTACH:
            // Disable thread library calls for optimization if not needed
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
 * @brief Native method implementation example
 * 
 * @param env JNI environment pointer
 * @param obj Java instance object (or clazz for static methods)
 */
JNIEXPORT void JNICALL Java_fastXXX_FastXXX_doSomethingNative(JNIEnv* env, jobject obj) {
    // TODO: Implement native logic here
    printf("[DEBUG C++] doSomethingNative called!\n");
    fflush(stdout); // Ensure printf output is flushed to Java console
}
