package com.quickmail.ui.compose;

import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.quickmail.R;
import com.quickmail.rust.RustBridge;
import com.quickmail.storage.PreferencesManager;
import com.quickmail.util.InternetChecker;
import com.quickmail.util.SizeFormatter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class QuickMailActivity extends AppCompatActivity {

    private EditText etTo, etSubject, etBody;
    private Button btnAttach, btnSend;
    private TextView tvSize;
    private List<Uri> attachmentUris = new ArrayList<>();
    private long totalSize = 0;
    private final long MAX_SIZE = 25 * 1024 * 1024;
    private PreferencesManager prefs;

    // Modern file picker launcher
    private ActivityResultLauncher<String[]> pickFilesLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_mail);

        etTo = findViewById(R.id.etTo);
        etSubject = findViewById(R.id.etSubject);
        etBody = findViewById(R.id.etBody);
        btnAttach = findViewById(R.id.btnAttach);
        btnSend = findViewById(R.id.btnSend);
        tvSize = findViewById(R.id.tvSize);
        prefs = new PreferencesManager(this);

        pickFilesLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(),
            uris -> {
                if (uris != null) for (Uri uri : uris) addAttachment(uri);
                updateSizeDisplay();
            }
        );

        btnAttach.setOnClickListener(v -> pickFilesLauncher.launch(new String[]{
            "application/pdf", "text/plain",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip", "image/png", "image/jpeg"
        }));

        btnSend.setOnClickListener(v -> {
            if (!InternetChecker.isConnected(this)) {
                Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
                return;
            }
            sendEmail();
        });

        // Clean old cache files
        cleanCache();
    }

    private void addAttachment(Uri uri) {
        long fileSize = getFileSizeFromUri(uri);
        if (totalSize + fileSize > MAX_SIZE) {
            Toast.makeText(this, "Cannot add: exceeds 25 MB limit", Toast.LENGTH_SHORT).show();
            return;
        }
        attachmentUris.add(uri);
        totalSize += fileSize;
    }

    private long getFileSizeFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            if (!cursor.isAfterLast() && sizeIndex >= 0) {
                long size = cursor.getLong(sizeIndex);
                cursor.close();
                return size;
            }
            cursor.close();
        }
        return 0;
    }

    private void updateSizeDisplay() {
        tvSize.setText("Size: " + SizeFormatter.formatMB(totalSize) + " / 25 MB");
    }

    private void sendEmail() {
        String to = etTo.getText().toString().trim();
        if (to.isEmpty()) {
            Toast.makeText(this, "Recipient required", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = prefs.getGmail();
        String password = prefs.getPassword();
        if (email == null || password == null) {
            Toast.makeText(this, "Credentials missing. Please set up again.", Toast.LENGTH_LONG).show();
            return;
        }

        // Convert content:// URIs to cache file paths
        String[] filePaths = copyUrisToCache(attachmentUris);

        boolean success = RustBridge.sendEmail(
            to,
            etSubject.getText().toString().trim(),
            etBody.getText().toString().trim(),
            filePaths,
            email,
            password
        );

        // Clean up cache files
        deleteCacheFiles(filePaths);

        if (success) {
            Toast.makeText(this, "✓ Sent", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Sending failed. Check network or credentials.", Toast.LENGTH_LONG).show();
        }
    }

    private String[] copyUrisToCache(List<Uri> uris) {
        List<String> paths = new ArrayList<>();
        for (Uri uri : uris) {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                String filename = getFileName(uri);
                File cacheFile = new File(getCacheDir(), filename);
                FileOutputStream fos = new FileOutputStream(cacheFile);
                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
                fos.close(); is.close();
                paths.add(cacheFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return paths.toArray(new String[0]);
    }

    private String getFileName(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            String name = cursor.getString(nameIndex);
            cursor.close();
            return name;
        }
        return "attachment";
    }

    private void deleteCacheFiles(String[] paths) {
        for (String p : paths) {
            new File(p).delete();
        }
    }

    private void cleanCache() {
        File[] files = getCacheDir().listFiles();
        if (files != null) for (File f : files) f.delete();
    }
}
