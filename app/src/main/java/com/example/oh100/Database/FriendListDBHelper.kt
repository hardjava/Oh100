package com.example.oh100.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.oh100.Object.User

class FriendListDBHelper(context: Context) : SQLiteOpenHelper(context, "friendList.db", null, 1) {
    private val tableName = "FriendList"
    private val columnId = "id"
    private val columnUserId = "userId"
    private val columnSolvedCount = "solved_count"

    override fun onCreate(db: SQLiteDatabase?) {
        val sql = "create table if not exists $tableName" +
                "($columnId integer PRIMARY KEY autoincrement, " +
                "$columnUserId text, " +
                "$columnSolvedCount integer)"
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $tableName")
        onCreate(db)
    }

    fun getAllFriends(): ArrayList<User> {
        val friendList = ArrayList<User>()
        val db = this.readableDatabase
        val cursor = db?.rawQuery("select * from $tableName", null)

        if (cursor != null) {
            for (index in 0 until cursor.count) {
                cursor.moveToNext()
                val id = cursor.getInt(0)
                val userId = cursor.getString(1)
                val solvedCount = cursor.getInt(2)
                friendList.add(User(userId, solvedCount))
            }
            cursor.close()
        }
        db.close()
        return friendList
    }

    fun addFriend(id: String, solvedCount: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(columnUserId, id)
            put(columnSolvedCount, solvedCount)
        }
        db.insert(tableName, null, values)
        db.close()
    }

    fun deleteFriend(userId: String) {
        val db = this.writableDatabase
        db.delete(tableName, "$columnUserId=?", arrayOf(userId))
        db.close()
    }
}