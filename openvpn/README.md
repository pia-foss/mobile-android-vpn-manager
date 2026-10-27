# OpenVPN binaries

The `:vpnprotocol:openvpn` module runs OpenVPN as a separate process. It ships two prebuilt
binaries per ABI in `vpnmanager/vpnservicemanager/vpnprotocol/openvpn/src/main/jniLibs/<abi>/`:

- `libopenvpn.so` is OpenVPN itself, built as a shared library that defines `main()`. OpenSSL,
  LZO and LZ4 are linked in statically.
- `libovpnexec.so` is an empty launcher executable linked against `libopenvpn.so`. `FilePath`
  starts it from the app's native library directory, with `LD_LIBRARY_PATH` pointing there.

This folder rebuilds both binaries from upstream OpenVPN plus the patches in `patches/`. The patches
raise the limits that stopped a JWT auth-token from being used as the OpenVPN password.

```bash
openvpn/build-openvpn-android.sh               # all ABIs: armeabi-v7a arm64-v8a x86 x86_64
openvpn/build-openvpn-android.sh arm64-v8a     # just one
```

The script builds and checks every requested ABI first, then copies the binaries into `jniLibs`, so
a failed build leaves the committed binaries untouched. It also rewrites
[`BUILD_INFO.txt`](BUILD_INFO.txt) with the versions used and the SHA-256 of every binary in
`jniLibs`. Commit both together. A full four-ABI build takes about 10 minutes on an M-series Mac.
Downloads and intermediate builds go to `openvpn/build/` (about 1 GB, gitignored).

Requirements: Android NDK r28+, plus `cmake`, `make`, `perl`, `python3`, `pkg-config`, `patch` and
`curl`. The script runs on macOS and Linux. The script header lists the environment overrides
(`ANDROID_NDK_HOME`, `OPENVPN_SRC`, `WORK_DIR`, `JNI_LIBS_DIR`, `JOBS`).

The output is not bit-for-bit reproducible, because OpenSSL embeds its build date. A rebuild
therefore always changes the checksums, even when nothing else changed.

## What is built

| Component | Version | Notes |
|-----------|---------|-------|
| OpenVPN   | 2.7.8   | upstream release tarball, plus `patches/` |
| OpenSSL   | 3.5.9   | 3.5 LTS, static |
| LZO       | 2.10    | static |
| LZ4       | 1.10.0  | static |

Every download is checked against a pinned SHA-256. The binaries target API 21, the `minSdk`
here, and are linked for 16 KB pages. The script checks each ABI for:

- the launcher depending on `libopenvpn.so`;
- an exported `main`;
- hidden OpenSSL/LZO/LZ4 symbols, so they cannot clash with another copy in the app;
- 16 KB `LOAD` alignment.

The build uses OpenVPN's CMake build with `CMAKE_SYSTEM_NAME=Android`, which defines
`TARGET_ANDROID`. That enables the management-interface hooks this module relies on:
`PROTECTFD`, `OPENTUN`, `IFCONFIG`, `ROUTE`, `PERSIST_TUN_ACTION`, and so on. Upstream marks CMake
as unsupported outside Windows, hence `-DUNSUPPORTED_BUILDS=ON`. Autotools cannot target Android
at all.

To move to a newer OpenVPN, update `OPENVPN_VERSION` and `OPENVPN_SHA256` in the script and
rebuild. The build stops if a patch no longer applies.

## Patches

1. **`0001-increase-credential-and-management-limits.patch`**. `HandleOpenVpnPasswordOutput`
   sends the token to OpenVPN as `password 'Auth' <token>` on the management socket. Three
   limits got in its way:

   | Limit | Was | Now | Effect on a long token |
   |-------|-----|-----|------------------------|
   | `OPTION_PARM_SIZE` / `OPTION_LINE_SIZE` (`options.h`) | 256 | 2048 | `Options error: Parameter at TCP:0 is too long (256 chars max)`, so the password is never set |
   | management input line (`manage.c`) | 1024 | 4096 | the whole line is silently dropped |
   | `USER_PASS_LEN` (`misc.h`) | 128 | 4096 | the password is silently truncated |

   Upstream sets `USER_PASS_LEN` to 4096 only when built with PKCS#11, and it never raises the
   other two limits. So enabling PKCS#11 alone is not enough. The patch sets all three directly,
   and PKCS#11 stays off.

2. **`0002-android-shared-library-and-launcher.patch`**. On Android, upstream CMake builds a
   plain `openvpn` executable. Android only installs `lib*.so` files into an app's native
   library directory, and an app can only execute files from there. So the patch builds
   `libopenvpn.so` plus the `libovpnexec.so` launcher instead.

### Remaining ceiling: about 1.2 KB

The client also sends the password inside the TLS key-exchange message. That message lives in a
fixed `TLS_CHANNEL_BUF_SIZE` (2048-byte) buffer, shared with key material, the options string and
peer info. The server has the same buffer, so raising it on the client alone would not help.
A token much over about 1.2 KB still fails.

## Differences from the previous binaries

The binaries before this build came from ics-openvpn v0.7.60 (`2.7-icsopenvpn`, OpenSSL 3.5.0).
That is Arne Schwabe's OpenVPN fork, an upstream `master` snapshot from April 2025 plus two
commits. This build is upstream 2.7.8, 607 commits later, including the 2.7.x security fixes.

- One of the fork's two commits, "socket: don't transfer bind family to socket in case of ANY
  address", has since landed upstream.
- The other, "always use a `0.0.0.0/0` default on Android instead of `def1`", has not. With
  `redirect-gateway def1`, OpenVPN now reports two `/1` routes instead of a `/0`. That makes no
  difference here: `HandleOpenVpnNeedOkOutput` answers every `ROUTE` with `ok` without reading it,
  and `Service` builds its own routes.
- Build flags: upstream CMake also enables `ENABLE_PLUGIN` (accepts `--plugin`) and `ENABLE_NTLM`
  (NTLM proxy auth), which ics-openvpn left off. Neither is used here.

Neither the old nor the new binary supports `--scramble` (XOR obfuscation is a separate,
non-upstream patch). OpenVPN endpoints that need it will fail with either binary.

