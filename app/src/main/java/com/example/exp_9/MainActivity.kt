package com.example.exp_9

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var draftMainPrefs: SharedPreferences

    // UI Elements - SharedPreferences
    private lateinit var etPrefKey: TextInputEditText
    private lateinit var etPrefValue: TextInputEditText
    private lateinit var btnSavePref: MaterialButton
    private lateinit var btnReadPref: MaterialButton
    private lateinit var btnClearPref: MaterialButton
    private lateinit var tvPrefOutput: TextView

    // UI Elements - SQLite Database
    private lateinit var etDbId: TextInputEditText
    private lateinit var etDbName: TextInputEditText
    private lateinit var etDbEmail: TextInputEditText
    private lateinit var btnDbInsert: MaterialButton
    private lateinit var btnDbView: MaterialButton
    private lateinit var btnDbUpdate: MaterialButton
    private lateinit var btnDbDelete: MaterialButton
    private lateinit var tvDbOutput: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Persistence Helpers
        dbHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        draftMainPrefs = getSharedPreferences("UnsavedMainDrafts", MODE_PRIVATE)

        // Bind Views
        initViews()

        // Setup SharedPreferences Click Listeners
        setupSharedPreferencesListeners()

        // Setup SQLite Database Click Listeners
        setupDatabaseListeners()
    }

    private fun initViews() {
        // SharedPreferences
        etPrefKey = findViewById(R.id.etPrefKey)
        etPrefValue = findViewById(R.id.etPrefValue)
        btnSavePref = findViewById(R.id.btnSavePref)
        btnReadPref = findViewById(R.id.btnReadPref)
        btnClearPref = findViewById(R.id.btnClearPref)
        tvPrefOutput = findViewById(R.id.tvPrefOutput)

        // SQLite Database
        etDbId = findViewById(R.id.etDbId)
        etDbName = findViewById(R.id.etDbName)
        etDbEmail = findViewById(R.id.etDbEmail)
        btnDbInsert = findViewById(R.id.btnDbInsert)
        btnDbView = findViewById(R.id.btnDbView)
        btnDbUpdate = findViewById(R.id.btnDbUpdate)
        btnDbDelete = findViewById(R.id.btnDbDelete)
        tvDbOutput = findViewById(R.id.tvDbOutput)

        // Attach Real-Time Draft Auto-Save & Restoration
        attachDraftWatcher(etPrefKey, "DRAFT_PREF_KEY")
        attachDraftWatcher(etPrefValue, "DRAFT_PREF_VALUE")
        attachDraftWatcher(etDbId, "DRAFT_DB_ID")
        attachDraftWatcher(etDbName, "DRAFT_DB_NAME")
        attachDraftWatcher(etDbEmail, "DRAFT_DB_EMAIL")
    }

    private fun attachDraftWatcher(editText: EditText, prefKey: String) {
        // Restore unsaved draft text when app opens
        val savedDraft = draftMainPrefs.getString(prefKey, "")
        if (!savedDraft.isNullOrEmpty() && editText.text.isNullOrEmpty()) {
            editText.setText(savedDraft)
        }

        // Save typed text in real-time to SharedPreferences synchronously (commit)
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                draftMainPrefs.edit().putString(prefKey, s?.toString() ?: "").commit()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSharedPreferencesListeners() {
        btnSavePref.setOnClickListener {
            val key = etPrefKey.text.toString().trim()
            val value = etPrefValue.text.toString().trim()

            if (key.isEmpty()) {
                Toast.makeText(this, "Please enter a key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedPreferences.edit().putString(key, value).apply()
            Toast.makeText(this, "Saved to SharedPreferences!", Toast.LENGTH_SHORT).show()
            tvPrefOutput.text = "Saved:\nKey: $key\nValue: $value"
            tvPrefOutput.setTextColor(getColor(android.R.color.black))
        }

        btnReadPref.setOnClickListener {
            val key = etPrefKey.text.toString().trim()

            if (key.isEmpty()) {
                Toast.makeText(this, "Please enter a key to read", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val value = sharedPreferences.getString(key, null)
            if (value != null) {
                etPrefValue.setText(value)
                tvPrefOutput.text = "Retrieved:\nKey: $key\nValue: $value"
                tvPrefOutput.setTextColor(getColor(android.R.color.black))
                Toast.makeText(this, "Data loaded!", Toast.LENGTH_SHORT).show()
            } else {
                tvPrefOutput.text = "No value found for key '$key'"
                tvPrefOutput.setTextColor(getColor(android.R.color.holo_red_dark))
                Toast.makeText(this, "Key not found", Toast.LENGTH_SHORT).show()
            }
        }

        btnClearPref.setOnClickListener {
            val key = etPrefKey.text.toString().trim()

            if (key.isEmpty()) {
                sharedPreferences.edit().clear().apply()
                etPrefValue.text?.clear()
                tvPrefOutput.text = "All SharedPreferences cleared."
                tvPrefOutput.setTextColor(getColor(android.R.color.darker_gray))
                Toast.makeText(this, "Cleared all preferences", Toast.LENGTH_SHORT).show()
            } else {
                sharedPreferences.edit().remove(key).apply()
                etPrefValue.text?.clear()
                tvPrefOutput.text = "Removed key '$key' from SharedPreferences."
                tvPrefOutput.setTextColor(getColor(android.R.color.darker_gray))
                Toast.makeText(this, "Removed key: $key", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDatabaseListeners() {
        btnDbInsert.setOnClickListener {
            val name = etDbName.text.toString().trim()
            val email = etDbEmail.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please enter Name and Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isInserted = dbHelper.insertData(name, email)
            if (isInserted) {
                Toast.makeText(this, "Record Inserted Successfully", Toast.LENGTH_SHORT).show()
                clearDbInputs()
                viewAllRecords()
            } else {
                Toast.makeText(this, "Failed to Insert Record", Toast.LENGTH_SHORT).show()
            }
        }

        btnDbView.setOnClickListener {
            viewAllRecords()
        }

        btnDbUpdate.setOnClickListener {
            val id = etDbId.text.toString().trim()
            val name = etDbName.text.toString().trim()
            val email = etDbEmail.text.toString().trim()

            if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please enter ID, Name, and Email to update", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isUpdated = dbHelper.updateData(id, name, email)
            if (isUpdated) {
                Toast.makeText(this, "Record Updated Successfully", Toast.LENGTH_SHORT).show()
                clearDbInputs()
                viewAllRecords()
            } else {
                Toast.makeText(this, "Update Failed! Record ID not found", Toast.LENGTH_SHORT).show()
            }
        }

        btnDbDelete.setOnClickListener {
            val id = etDbId.text.toString().trim()

            if (id.isEmpty()) {
                Toast.makeText(this, "Please enter ID to delete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val deletedRows = dbHelper.deleteData(id)
            if (deletedRows > 0) {
                Toast.makeText(this, "Record Deleted Successfully", Toast.LENGTH_SHORT).show()
                clearDbInputs()
                viewAllRecords()
            } else {
                Toast.makeText(this, "Delete Failed! Record ID not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun viewAllRecords() {
        val cursor = dbHelper.getAllData()
        if (cursor.count == 0) {
            tvDbOutput.text = "No records found in database."
            tvDbOutput.setTextColor(getColor(android.R.color.darker_gray))
            cursor.close()
            return
        }

        val builder = StringBuilder()
        while (cursor.moveToNext()) {
            val id = cursor.getString(0)
            val name = cursor.getString(1)
            val email = cursor.getString(2)
            builder.append("ID: $id | Name: $name | Email: $email\n")
        }
        cursor.close()

        tvDbOutput.text = builder.toString()
        tvDbOutput.setTextColor(getColor(android.R.color.black))
    }

    private fun clearDbInputs() {
        etDbId.text?.clear()
        etDbName.text?.clear()
        etDbEmail.text?.clear()
    }
}
