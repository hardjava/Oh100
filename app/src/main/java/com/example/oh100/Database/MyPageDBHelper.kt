package com.example.oh100.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.oh100.Object.User

class MyPageDBHelper(context: Context) : SQLiteOpenHelper(context, "myPage.db", null, 1) {
    private val tableName = "myPage"
    private val columnMyId = "myID"

    override fun onCreate(db: SQLiteDatabase?) {
        val sql = "create table if not exists $tableName" +
                "($columnMyId text PRIMARY KEY)"
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $tableName")
        onCreate(db)
    }

    fun getMyId(): String? {
        val db = this.readableDatabase
        var myId: String? = null
        val cursor = db?.rawQuery("select * from $tableName", null)
        if (cursor != null) {
            for (index in 0 until cursor.count) {
                cursor.moveToNext()
                myId = cursor.getString(1)
            }
            cursor.close()
        }
        db.close()
        return myId
    }

    fun addMyId(myId: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(columnMyId, myId)
        }
        db.insert(tableName, null, values)
        db.close()
    }

    fun deleteMyId(myId: String) {
        val db = this.writableDatabase
        db.delete(tableName, "$columnMyId=?", arrayOf(myId))
        db.close()
    }
}