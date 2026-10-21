#!/usr/bin/env bash
#
# Builds the OpenVPN binaries the :vpnprotocol:openvpn module ships and installs them into
# vpnmanager/vpnservicemanager/vpnprotocol/openvpn/src/main/jniLibs/<abi>/:
#
#   libopenvpn.so    OpenVPN itself, with main(); OpenSSL, LZO and LZ4 linked in
#   libovpnexec.so   empty launcher executable linked against libopenvpn.so
#
# from upstream OpenVPN plus the patches in ./patches, the important one raising the
# username/password and management line limits so a JWT auth-token fits as the password.
# Versions and checksums of what is installed are recorded in ./BUILD_INFO.txt.
#
# Usage: ./build-openvpn-android.sh [abi...]
#   abi  any of armeabi-v7a arm64-v8a x86 x86_64 (default: all four)
#
# Environment:
#   ANDROID_NDK_HOME  NDK to build with. Defaults to the newest NDK under
#                     $ANDROID_HOME/ndk (or ~/Library/Android/sdk/ndk). r28 or newer.
#   OPENVPN_SRC       build an existing OpenVPN source tree instead of the release tarball.
#                     It is copied, not modified; the patches must apply to it.
#   WORK_DIR          downloads and intermediate builds (default: ./build, gitignored)
#   JNI_LIBS_DIR      where the binaries are installed (default: the module's jniLibs)
#   JOBS              parallel jobs (default: number of CPUs)
#
# Requires: cmake, make, perl, python3, pkg-config, patch, curl, shasum or sha256sum.

set -euo pipefail

OPENVPN_VERSION=2.7.8
OPENVPN_SHA256=c070d1d2440b5a6fca6c2c68645c98cd492116ac36ef4f0946177115532c8e36
OPENSSL_VERSION=3.5.9
OPENSSL_SHA256=603f5602e2eef00d77fbd429d34dcd5822bb301757a1bc9cdb24c670f1eb859a
LZO_VERSION=2.10
LZO_SHA256=c0f892943208266f9b6543b3ae308fab6284c5c90e627931446fb49b4221a072
LZ4_VERSION=1.10.0
LZ4_SHA256=537512904744b35e232912055ccf8ec66d768639ff3abe5788d90d792ec5f48b

# Matches minSdkVersion of mobile-android-vpn-manager.
ANDROID_API=21
# Android 15+ devices may use 16 KB pages; every ELF segment must be aligned to that.
PAGE_SIZE_LDFLAGS="-Wl,-z,max-page-size=16384"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PATCH_DIR="$SCRIPT_DIR/patches"
WORK_DIR="$(mkdir -p "${WORK_DIR:-$SCRIPT_DIR/build}" && cd "${WORK_DIR:-$SCRIPT_DIR/build}" && pwd)"
JNI_LIBS_DIR="${JNI_LIBS_DIR:-$SCRIPT_DIR/../vpnmanager/vpnservicemanager/vpnprotocol/openvpn/src/main/jniLibs}"
JNI_LIBS_DIR="$(mkdir -p "$JNI_LIBS_DIR" && cd "$JNI_LIBS_DIR" && pwd)"
# Every ABI is built and verified here first, so a failed build never leaves jniLibs half updated.
STAGING_DIR="$WORK_DIR/jniLibs"
DOWNLOAD_DIR="$WORK_DIR/downloads"
JOBS="${JOBS:-$(getconf _NPROCESSORS_ONLN)}"

ALL_ABIS=(armeabi-v7a arm64-v8a x86 x86_64)
if [[ $# -gt 0 ]]; then ABIS=("$@"); else ABIS=("${ALL_ABIS[@]}"); fi

log() { printf '\n==> %s\n' "$*"; }
die() { printf 'error: %s\n' "$*" >&2; exit 1; }

sha256() {
    if command -v sha256sum >/dev/null; then sha256sum "$1"; else shasum -a 256 "$1"; fi | cut -d' ' -f1
}

for tool in cmake make perl python3 pkg-config patch curl; do
    command -v "$tool" >/dev/null || die "$tool is required"
done

# --- NDK ---------------------------------------------------------------------

if [[ -z "${ANDROID_NDK_HOME:-}" ]]; then
    sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}"
    ANDROID_NDK_HOME="$(ls -d "$sdk"/ndk/*/ 2>/dev/null | sort -V | tail -1)"
    ANDROID_NDK_HOME="${ANDROID_NDK_HOME%/}"
fi
[[ -f "${ANDROID_NDK_HOME:-}/source.properties" ]] || die "no NDK found; set ANDROID_NDK_HOME"
NDK_REVISION="$(sed -n 's/^Pkg.Revision *= *//p' "$ANDROID_NDK_HOME/source.properties")"
[[ "${NDK_REVISION%%.*}" -ge 28 ]] || die "NDK r28 or newer required, found $NDK_REVISION"

case "$(uname -s)" in
    Darwin) HOST_TAG=darwin-x86_64 ;;  # also the tag on Apple silicon
    Linux) HOST_TAG=linux-x86_64 ;;
    *) die "unsupported host $(uname -s)" ;;
esac
TOOLCHAIN="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$HOST_TAG"
[[ -d "$TOOLCHAIN" ]] || die "missing NDK toolchain $TOOLCHAIN"

# --- Sources -----------------------------------------------------------------

# fetch <url> <file> <sha256>
fetch() {
    local file="$DOWNLOAD_DIR/$2"
    mkdir -p "$DOWNLOAD_DIR"
    if [[ ! -f "$file" ]]; then
        curl -fsSL --retry 3 -o "$file.part" "$1"
        mv "$file.part" "$file"
    fi
    local actual
    actual="$(sha256 "$file")"
    [[ "$actual" == "$3" ]] || die "$2: sha256 $actual, expected $3"
}

# unpack <tarball> <dir>: a fresh copy, so every build starts from pristine sources.
unpack() {
    rm -rf "$2" && mkdir -p "$2"
    tar -xzf "$DOWNLOAD_DIR/$1" -C "$2" --strip-components=1
}

log "Fetching sources"
fetch "https://github.com/openssl/openssl/releases/download/openssl-$OPENSSL_VERSION/openssl-$OPENSSL_VERSION.tar.gz" \
    "openssl-$OPENSSL_VERSION.tar.gz" "$OPENSSL_SHA256"
fetch "https://www.oberhumer.com/opensource/lzo/download/lzo-$LZO_VERSION.tar.gz" \
    "lzo-$LZO_VERSION.tar.gz" "$LZO_SHA256"
fetch "https://github.com/lz4/lz4/releases/download/v$LZ4_VERSION/lz4-$LZ4_VERSION.tar.gz" \
    "lz4-$LZ4_VERSION.tar.gz" "$LZ4_SHA256"

OPENVPN_TREE="$WORK_DIR/src/openvpn"
if [[ -n "${OPENVPN_SRC:-}" ]]; then
    [[ -f "$OPENVPN_SRC/CMakeLists.txt" ]] || die "OPENVPN_SRC=$OPENVPN_SRC is not an OpenVPN source tree"
    log "Copying OpenVPN from $OPENVPN_SRC"
    rm -rf "$OPENVPN_TREE" && mkdir -p "$OPENVPN_TREE"
    (cd "$OPENVPN_SRC" && tar --exclude=.git -cf - .) | tar -xf - -C "$OPENVPN_TREE"
    OPENVPN_LABEL="$OPENVPN_SRC ($(git -C "$OPENVPN_SRC" describe --always --tags --dirty 2>/dev/null || echo 'not a git checkout'))"
else
    fetch "https://github.com/OpenVPN/openvpn/releases/download/v$OPENVPN_VERSION/openvpn-$OPENVPN_VERSION.tar.gz" \
        "openvpn-$OPENVPN_VERSION.tar.gz" "$OPENVPN_SHA256"
    unpack "openvpn-$OPENVPN_VERSION.tar.gz" "$OPENVPN_TREE"
    OPENVPN_LABEL="$OPENVPN_VERSION"
fi
[[ ! -f "$OPENVPN_TREE/config.h" ]] || die "the OpenVPN tree was configured with autotools; CMake refuses to build it"

log "Patching OpenVPN"
for p in "$PATCH_DIR"/*.patch; do
    echo "    $(basename "$p")"
    patch -d "$OPENVPN_TREE" -p1 --forward --quiet < "$p" || die "$(basename "$p") does not apply"
done

# --- Per-ABI build -----------------------------------------------------------

build_abi() {
    local abi="$1" target openssl_target
    case "$abi" in
        armeabi-v7a) target=armv7a-linux-androideabi; openssl_target=android-arm ;;
        arm64-v8a) target=aarch64-linux-android; openssl_target=android-arm64 ;;
        x86) target=i686-linux-android; openssl_target=android-x86 ;;
        x86_64) target=x86_64-linux-android; openssl_target=android-x86_64 ;;
        *) die "unknown ABI $abi (expected one of ${ALL_ABIS[*]})" ;;
    esac

    local abi_dir="$WORK_DIR/$abi"
    local prefix="$abi_dir/prefix"
    rm -rf "$abi_dir" && mkdir -p "$prefix"

    # Everything static goes into libopenvpn.so, so it must be position independent.
    local cc="$TOOLCHAIN/bin/$target$ANDROID_API-clang"
    local cflags="-O2 -fPIC"
    local -a autotools_env=(CC="$cc" AR="$TOOLCHAIN/bin/llvm-ar" RANLIB="$TOOLCHAIN/bin/llvm-ranlib"
        CFLAGS="$cflags" LDFLAGS="$PAGE_SIZE_LDFLAGS")

    log "[$abi] OpenSSL $OPENSSL_VERSION"
    unpack "openssl-$OPENSSL_VERSION.tar.gz" "$abi_dir/openssl"
    (
        cd "$abi_dir/openssl"
        export ANDROID_NDK_ROOT="$ANDROID_NDK_HOME" PATH="$TOOLCHAIN/bin:$PATH"
        ./Configure "$openssl_target" -D__ANDROID_API__="$ANDROID_API" \
            no-shared no-tests no-docs -fPIC -Wno-macro-redefined \
            --prefix="$prefix" --libdir=lib >/dev/null
        make -j"$JOBS" build_libs >/dev/null
        make install_dev >/dev/null
    )

    log "[$abi] LZO $LZO_VERSION"
    unpack "lzo-$LZO_VERSION.tar.gz" "$abi_dir/lzo"
    (
        cd "$abi_dir/lzo"
        ./configure --host="$target" --prefix="$prefix" --enable-static --disable-shared \
            "${autotools_env[@]}" >/dev/null
        make -j"$JOBS" >/dev/null
        make install >/dev/null
    )

    log "[$abi] LZ4 $LZ4_VERSION"
    unpack "lz4-$LZ4_VERSION.tar.gz" "$abi_dir/lz4"
    make -C "$abi_dir/lz4/lib" -j"$JOBS" install PREFIX="$prefix" BUILD_SHARED=no \
        "${autotools_env[@]}" >/dev/null

    log "[$abi] OpenVPN $OPENVPN_LABEL"
    # PKG_CONFIG_LIBDIR (not _PATH) so the host's lzo2/liblz4 can never be picked up.
    # USE_WERROR=OFF: upstream only builds 64-bit targets with -Werror; on armeabi-v7a/x86
    # its own sources trip -Wshorten-64-to-32 (e.g. buffer.c, st_size to size_t).
    # UNSUPPORTED_BUILDS: upstream only supports CMake on Windows, yet autotools cannot
    # target Android at all (it maps *-linux-android to TARGET_LINUX), while CMake has an
    # Android branch (TARGET_ANDROID), the code paths ics-openvpn builds too.
    PKG_CONFIG_LIBDIR="$prefix/lib/pkgconfig" cmake -S "$OPENVPN_TREE" -B "$abi_dir/openvpn" \
        -DUNSUPPORTED_BUILDS=ON \
        -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake" \
        -DANDROID_ABI="$abi" \
        -DANDROID_PLATFORM="android-$ANDROID_API" \
        -DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_SHARED_LINKER_FLAGS="$PAGE_SIZE_LDFLAGS" \
        -DCMAKE_EXE_LINKER_FLAGS="$PAGE_SIZE_LDFLAGS" \
        -DOPENSSL_ROOT_DIR="$prefix" \
        -DOPENSSL_USE_STATIC_LIBS=TRUE \
        -DENABLE_LZO=ON \
        -DENABLE_LZ4=ON \
        -DENABLE_PKCS11=OFF \
        -DENABLE_DNS_UPDOWN_BY_DEFAULT=OFF \
        -DBUILD_TESTING=OFF \
        -DUSE_WERROR=OFF >/dev/null
    PKG_CONFIG_LIBDIR="$prefix/lib/pkgconfig" cmake --build "$abi_dir/openvpn" -j"$JOBS" \
        --target openvpn ovpnexec >/dev/null

    local dest="$STAGING_DIR/$abi"
    rm -rf "$dest" && mkdir -p "$dest"
    for lib in libopenvpn.so libovpnexec.so; do
        "$TOOLCHAIN/bin/llvm-strip" --strip-unneeded -o "$dest/$lib" "$abi_dir/openvpn/$lib"
    done
    verify_abi "$abi" "$dest"
}

# Sanity checks on what Android will load: launcher → libopenvpn.so → main, 16 KB alignment.
verify_abi() {
    local abi="$1" dest="$2" readelf="$TOOLCHAIN/bin/llvm-readelf"
    "$readelf" -d "$dest/libovpnexec.so" | grep -q 'NEEDED.*\[libopenvpn.so\]' \
        || die "[$abi] libovpnexec.so does not link libopenvpn.so"
    "$readelf" -d "$dest/libopenvpn.so" | grep -q 'SONAME.*\[libopenvpn.so\]' \
        || die "[$abi] libopenvpn.so has no SONAME libopenvpn.so"
    "$readelf" --dyn-syms "$dest/libopenvpn.so" | grep -qE 'FUNC +GLOBAL +DEFAULT +[0-9]+ main$' \
        || die "[$abi] libopenvpn.so does not export main"
    if "$readelf" --dyn-syms "$dest/libopenvpn.so" | grep -qE ' (SSL_new|lzo1x_1_compress|LZ4_compress_default)$'; then
        die "[$abi] libopenvpn.so exports OpenSSL/LZO/LZ4 symbols"
    fi
    local f align
    for f in "$dest"/*.so; do
        for align in $("$readelf" -lW "$f" | awk '$1 == "LOAD" { print $NF }'); do
            (( align >= 16384 )) || die "[$abi] $(basename "$f") has a LOAD segment aligned to $align"
        done
    done
}

rm -rf "$STAGING_DIR"
for abi in "${ABIS[@]}"; do
    build_abi "$abi"
done

# --- Install -----------------------------------------------------------------

log "Installing into $JNI_LIBS_DIR"
for abi in "${ABIS[@]}"; do
    mkdir -p "$JNI_LIBS_DIR/$abi"
    for lib in libopenvpn.so libovpnexec.so; do
        cp "$STAGING_DIR/$abi/$lib" "$JNI_LIBS_DIR/$abi/$lib"
    done
    echo "    $abi"
done

# Checksums cover everything in jniLibs, so a partial build ("$0 arm64-v8a") still leaves an
# accurate record; the versions above them are those of this build.
{
    echo "OpenVPN      $OPENVPN_LABEL"
    for p in "$PATCH_DIR"/*.patch; do echo "  + patch    $(basename "$p")"; done
    echo "OpenSSL      $OPENSSL_VERSION"
    echo "LZO          $LZO_VERSION"
    echo "LZ4          $LZ4_VERSION"
    echo "NDK          $NDK_REVISION"
    echo "Android API  $ANDROID_API"
    echo "ABIs built   ${ABIS[*]}"
    echo
    (cd "$JNI_LIBS_DIR" && for f in */libopenvpn.so */libovpnexec.so; do echo "$(sha256 "$f")  $f"; done | sort -k2)
} > "$SCRIPT_DIR/BUILD_INFO.txt"

log "Done"
cat "$SCRIPT_DIR/BUILD_INFO.txt"
