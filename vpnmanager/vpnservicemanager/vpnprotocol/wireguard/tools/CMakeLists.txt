# SPDX-License-Identifier: Apache-2.0
#
# Copyright © 2017-2021 WireGuard LLC. All Rights Reserved.

cmake_minimum_required(VERSION 3.4.1)
project("WireGuard")

# Output directory settings
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
add_link_options(LINKER:--build-id=none)

# General warning settings
add_compile_options(-Wall -Werror)

# --- Fortification & hardening flags (list form so CMake passes them correctly) ---
set(FORTIFY_CFLAGS
        -O2
        -fPIC
        -D_FORTIFY_SOURCE=2
        -fstack-protector-all
        -Wformat
        -Werror=format-security
)

# Apply compile-time fortification globally
add_compile_options(${FORTIFY_CFLAGS})

# Linker hardening flags for shared libraries
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,relro,-z,now -Wl,-z,noexecstack")

# --- Build dummy.so as a fortified shared object ---
add_library(dummy SHARED dummy/dummy.c)

# Name output exactly dummy.so (no "lib" prefix)
set_target_properties(dummy PROPERTIES
        PREFIX ""
        SUFFIX ".so"
        POSITION_INDEPENDENT_CODE ON
        LINK_FLAGS "${CMAKE_SHARED_LINKER_FLAGS}"
)

# Ensure fortify flags are applied to dummy target
target_compile_options(dummy PRIVATE ${FORTIFY_CFLAGS})

# Prevent builtins from optimizing out fortifiable calls in dummy.c
target_compile_options(dummy PRIVATE
        -fno-builtin-strcpy
        -fno-builtin-memcpy
        -fno-builtin-snprintf
)

# --- Optional commented-out targets from original ---
# add_executable(libwg-quick.so wireguard-tools/src/wg-quick/android.c ndk-compat/compat.c)
# target_compile_options(libwg-quick.so PUBLIC -O3 -std=gnu11 -Wall -include ${CMAKE_CURRENT_SOURCE_DIR}/ndk-compat/compat.h -DWG_PACKAGE_NAME=\"${ANDROID_PACKAGE_NAME}\")
# target_link_libraries(libwg-quick.so -ldl)
#
# file(GLOB WG_SOURCES wireguard-tools/src/*.c ndk-compat/compat.c)
# add_executable(libwg.so ${WG_SOURCES})
# target_include_directories(libwg.so PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/wireguard-tools/src/uapi/linux/" "${CMAKE_CURRENT_SOURCE_DIR}/wireguard-tools/src/")
# target_compile_options(libwg.so PUBLIC -O3 -std=gnu11 -D_GNU_SOURCE -include ${CMAKE_CURRENT_SOURCE_DIR}/ndk-compat/compat.h -DHAVE_VISIBILITY_HIDDEN -DRUNSTATEDIR=\"/data/data/${ANDROID_PACKAGE_NAME}/cache\")

# --- Custom target to build libwg-go.so ---
add_custom_target(libwg-go.so
        WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/libwg-go"
        COMMENT "Building wireguard-go"
        VERBATIM
        COMMAND "${ANDROID_HOST_PREBUILTS}/bin/make"
        ANDROID_ARCH_NAME=${ANDROID_ARCH_NAME}
        ANDROID_PACKAGE_NAME=${ANDROID_PACKAGE_NAME}
        GRADLE_USER_HOME=${GRADLE_USER_HOME}
        CC=${CMAKE_C_COMPILER}
        CFLAGS=${CMAKE_C_FLAGS}
        LDFLAGS=${CMAKE_SHARED_LINKER_FLAGS}
        SYSROOT=${CMAKE_SYSROOT}
        TARGET=${CMAKE_C_COMPILER_TARGET}
        DESTDIR=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        BUILDDIR=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/../generated-src
)

# Ensure dummy builds after libwg-go.so
add_dependencies(dummy libwg-go.so)
