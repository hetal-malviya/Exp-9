package com.example.exp_9

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var draftLoginPrefs: SharedPreferences

    private lateinit var etLoginEmail: TextInputEditText
    private lateinit var etLoginPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnRegisterRedirect: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        draftLoginPrefs = getSharedPreferences("UnsavedLoginDrafts", MODE_PRIVATE)

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("IS_LOGGED_IN", false)) {
            navigateToHome()
            return
        }

        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegisterRedirect = findViewById(R.id.btnRegisterRedirect)

        // Attach Real-Time Draft Auto-Save & Restoration
        attachDraftWatcher(etLoginEmail, "DRAFT_LOGIN_EMAIL")
        attachDraftWatcher(etLoginPassword, "DRAFT_LOGIN_PASSWORD")

        // Pre-fill email if redirected from Registration
        val registeredEmail = intent.getStringExtra("REGISTERED_EMAIL")
        if (!registeredEmail.isNullOrEmpty()) {
            etLoginEmail.setText(registeredEmail)
        }

        btnLogin.setOnClickListener {
            handleLogin()
        }

        btnRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun attachDraftWatcher(editText: EditText, prefKey: String) {
        // Restore unsaved draft text when app opens
        val savedDraft = draftLoginPrefs.getString(prefKey, "")
        if (!savedDraft.isNullOrEmpty() && editText.text.isNullOrEmpty()) {
            editText.setText(savedDraft)
        }

        // Save typed text in real-time to SharedPreferences synchronously (commit)
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                draftLoginPrefs.edit().putString(prefKey, s?.toString() ?: "").commit()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun handleLogin() {
        val email = etLoginEmail.text.toString().trim()
        val password = etLoginPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Check credentials against database
        if (dbHelper.checkUserCredentials(email, password)) {
            // Save login session in SharedPreferences
            sharedPreferences.edit().apply {
                putBoolean("IS_LOGGED_IN", true)
                putString("LOGGED_IN_EMAIL", email)
                commit()
            }

            // Clear unsaved login draft upon successful login
            draftLoginPrefs.edit().clear().commit()

            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
            navigateToHome()
        } else {
            // Give specific message if user isn't registered vs wrong password
            if (!dbHelper.checkUserExists(email)) {
                Toast.makeText(this, "Account not found. Please register first.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
