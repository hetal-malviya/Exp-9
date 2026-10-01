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

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var draftPrefs: SharedPreferences

    private lateinit var etRegisterName: TextInputEditText
    private lateinit var etRegisterEmail: TextInputEditText
    private lateinit var etRegisterPassword: TextInputEditText
    private lateinit var etRegisterConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnLoginRedirect: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)
        draftPrefs = getSharedPreferences("UnsavedRegisterDrafts", MODE_PRIVATE)

        etRegisterName = findViewById(R.id.etRegisterName)
        etRegisterEmail = findViewById(R.id.etRegisterEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        etRegisterConfirmPassword = findViewById(R.id.etRegisterConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnLoginRedirect = findViewById(R.id.btnLoginRedirect)

        // Attach Real-Time Draft Auto-Save & Restoration
        attachDraftWatcher(etRegisterName, "DRAFT_NAME")
        attachDraftWatcher(etRegisterEmail, "DRAFT_EMAIL")
        attachDraftWatcher(etRegisterPassword, "DRAFT_PASSWORD")
        attachDraftWatcher(etRegisterConfirmPassword, "DRAFT_CONFIRM_PASSWORD")

        btnRegister.setOnClickListener {
            handleRegistration()
        }

        btnLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attachDraftWatcher(editText: EditText, prefKey: String) {
        // Restore unsaved draft text when app opens
        val savedDraft = draftPrefs.getString(prefKey, "")
        if (!savedDraft.isNullOrEmpty() && editText.text.isNullOrEmpty()) {
            editText.setText(savedDraft)
        }

        // Save typed text in real-time to SharedPreferences
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                draftPrefs.edit().putString(prefKey, s?.toString() ?: "").commit()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun handleRegistration() {
        val name = etRegisterName.text.toString().trim()
        val email = etRegisterEmail.text.toString().trim()
        val password = etRegisterPassword.text.toString().trim()
        val confirmPassword = etRegisterConfirmPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if user already exists
        if (dbHelper.checkUserExists(email)) {
            Toast.makeText(this, "User already registered! Please login instead.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                putExtra("REGISTERED_EMAIL", email)
            })
            finish()
            return
        }

        // Register new user in database
        val isRegistered = dbHelper.registerUser(name, email, password)
        if (isRegistered) {
            // Clear unsaved draft text upon successful registration
            draftPrefs.edit().clear().apply()

            Toast.makeText(this, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, LoginActivity::class.java).apply {
                putExtra("REGISTERED_EMAIL", email)
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
