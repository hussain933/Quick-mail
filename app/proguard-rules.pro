-keep class com.quickmail.rust.RustBridge { *; }
# Disable logging in release
-assumenosideeffects class android.util.Log { *; }
