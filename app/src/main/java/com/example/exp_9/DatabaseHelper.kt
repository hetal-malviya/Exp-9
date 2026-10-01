package com.example.exp_9

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDB.db"
        private const val DATABASE_VERSION = 2
        const val TABLE_NAME = "users"
        const val COL_1 = "ID"
        const val COL_2 = "NAME"
        const val COL_3 = "EMAIL"
        const val COL_4 = "PASSWORD"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE " + TABLE_NAME + " ("
                + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_2 + " TEXT, "
                + COL_3 + " TEXT UNIQUE, "
                + COL_4 + " TEXT)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun checkUserExists(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COL_3 = ?", arrayOf(email.trim().lowercase()))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun registerUser(name: String, email: String, password: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        if (checkUserExists(cleanEmail)) {
            return false // User already registered
        }
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_2, name.trim())
            put(COL_3, cleanEmail)
            put(COL_4, password)
        }
        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L
    }

    fun checkUserCredentials(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COL_3 = ? AND $COL_4 = ?",
            arrayOf(email.trim().lowercase(), password)
        )
        val valid = cursor.count > 0
        cursor.close()
        return valid
    }

    fun getUserName(email: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COL_2 FROM $TABLE_NAME WHERE $COL_3 = ?", arrayOf(email.trim().lowercase()))
        var name: String? = null
        if (cursor.moveToFirst()) {
            name = cursor.getString(0)
        }
        cursor.close()
        return name
    }

    fun insertData(name: String, email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_2, name.trim())
            put(COL_3, cleanEmail)
            put(COL_4, "")
        }
        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L
    }

    fun updateData(id: String, name: String, email: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_1, id)
            put(COL_2, name.trim())
            put(COL_3, email.trim().lowercase())
        }
        val result = db.update(TABLE_NAME, contentValues, "ID = ?", arrayOf(id))
        return result > 0
    }

    fun getAllData(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun deleteData(id: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "ID = ?", arrayOf(id))
    }
}
