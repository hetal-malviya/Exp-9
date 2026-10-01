package com.example.exp_9

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var tvWelcomeName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUsersList: TextView
    private lateinit var btnLogout: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)

        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUsersList = findViewById(R.id.tvUsersList)
        btnLogout = findViewById(R.id.btnLogout)

        val userEmail = sharedPreferences.getString("LOGGED_IN_EMAIL", "Unknown") ?: "Unknown"
        val userName = dbHelper.getUserName(userEmail) ?: "User"

        tvWelcomeName.text = "Welcome, $userName!"
        tvUserEmail.text = "Logged in as: $userEmail"

        displayRegisteredUsers()

        btnLogout.setOnClickListener {
            // Clear SharedPreferences user session
            sharedPreferences.edit().apply {
                clear()
                apply()
            }
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate back to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun displayRegisteredUsers() {
        val cursor = dbHelper.getAllData()
        if (cursor.count == 0) {
            tvUsersList.text = "No registered users found."
            cursor.close()
            return
        }

        val builder = StringBuilder()
        var count = 1
        while (cursor.moveToNext()) {
            val id = cursor.getString(0)
            val name = cursor.getString(1)
            val email = cursor.getString(2)
            builder.append("$count. $name ($email) [ID: $id]\n")
            count++
        }
        cursor.close()

        tvUsersList.text = builder.toString()
    }
}
