package com.quickmail.storage;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class PreferencesManager {
    private static final String PREF_NAME = "quickmail_secure_prefs";
    private SharedPreferences prefs;

    public PreferencesManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            prefs = EncryptedSharedPreferences.create(
                PREF_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to init encrypted prefs", e);
        }
    }

    public void saveCredentials(String email, String password) {
        prefs.edit().putString("gmail", email).putString("password", password).apply();
    }

    public String getGmail() { return prefs.getString("gmail", null); }
    public String getPassword() { return prefs.getString("password", null); }
}
