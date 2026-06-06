#!/bin/bash
cd "$(dirname "$0")"

TARGETS=(aarch64-linux-android armv7-linux-androideabi x86_64-linux-android)
JNILIBS_DIR="../app/src/main/jniLibs"

for target in "${TARGETS[@]}"; do
    echo "Building for $target..."
    cargo ndk --target "$target" --platform 24 -- build --release
done

mkdir -p "$JNILIBS_DIR/arm64-v8a" "$JNILIBS_DIR/armeabi-v7a" "$JNILIBS_DIR/x86_64"
cp target/aarch64-linux-android/release/libquickmail_engine.so "$JNILIBS_DIR/arm64-v8a/"
cp target/armv7-linux-androideabi/release/libquickmail_engine.so "$JNILIBS_DIR/armeabi-v7a/"
cp target/x86_64-linux-android/release/libquickmail_engine.so "$JNILIBS_DIR/x86_64/"
echo "✅ All .so files copied to jniLibs."
