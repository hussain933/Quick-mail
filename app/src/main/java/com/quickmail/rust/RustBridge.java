package com.quickmail.rust;

public class RustBridge {
    static { System.loadLibrary("quickmail_engine"); }

    // Credentials are passed for every call (no global state)
    public static native boolean sendEmail(
        String to, String subject, String body,
        String[] attachmentPaths,
        String senderEmail, String senderPassword
    );
}
