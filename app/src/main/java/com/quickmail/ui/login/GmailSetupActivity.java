package com.quickmail.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.quickmail.R;
import com.quickmail.storage.PreferencesManager;
import com.quickmail.ui.compose.QuickMailActivity;

public class GmailSetupActivity extends AppCompatActivity {

    private EditText etGmail, etPassword;
    private Button btnSave;
    private PreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail_setup);

        prefs = new PreferencesManager(this);
        etGmail = findViewById(R.id.etGmail);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);

        // Auto-fill if already saved
        if (prefs.getGmail() != null) {
            etGmail.setText(prefs.getGmail());
            etPassword.setText(prefs.getPassword());
        }

        btnSave.setOnClickListener(v -> {
            String email = etGmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.saveCredentials(email, pass);
            Toast.makeText(this, "Saved ✓", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, QuickMailActivity.class));
            finish();  // don't come back
        });
    }
}
