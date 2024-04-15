package com.example.jmb_bms.model.database.chat

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteCursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray

class ChatDBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?):
    SQLiteOpenHelper(context, DB_NAME, factory, DB_VERSION)
{

    override fun onCreate(db: SQLiteDatabase?) {
        val q = "CREATE TABLE $TABLE_NAME ($ID_COL TEXT PRIMARY KEY, $NAME_COL TEXT, $OWNER_COL TEXT, $MEMBERS_COL TEXT)"
        db?.execSQL(q)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val q = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(q)
    }

    private fun convertListToJson(list: List<Any>?): String?
    {
        if(list == null) return null

        val convList = list.map { it.toString() }
        val jsonArr = JSONArray(convList)
        return jsonArr.toString()
    }

    private fun convertJsonToList(json: String?): List<String>?
    {
        if(json == null) return null
        val jsonArr = JSONArray(json)

        val list = mutableListOf<String>()

        for(cnt in 0 until jsonArr.length())
        {
            list.add(jsonArr.getString(cnt))
        }
        return list
    }

    fun addChatRoom(chatRow: ChatRow)
    {
        val values = ContentValues().apply {
            put(ID_COL,chatRow.id)
            put(OWNER_COL, chatRow.owner)
            put(NAME_COL, chatRow.name)
            put(MEMBERS_COL, convertListToJson(chatRow.members))
        }

        writableDatabase.insert(TABLE_NAME,null,values)
    }

    private fun extractChatRow(cursor: Cursor): ChatRow
    {
        val id = cursor.getString(cursor.getColumnIndexOrThrow(ID_COL))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL))
        val owner = cursor.getString(cursor.getColumnIndexOrThrow(OWNER_COL))
        val members = convertJsonToList(cursor.getString(cursor.getColumnIndexOrThrow(MEMBERS_COL)))

        return ChatRow(id,name,owner,members!!)
    }

    private fun extractAllRows(cursor: Cursor, target: MutableList<ChatRow>)
    {
        with(cursor)
        {
            while (moveToNext())
            {
                target.add(extractChatRow(this))
            }
        }
    }

    fun getAllChatRooms(): List<ChatRow>?
    {
        val cursor = readableDatabase.query(
            TABLE_NAME, null,null,null,null,null,null,null
        )

        if(cursor.count <= 0)
        {
            cursor.close()
            return null
        }

        val target = mutableListOf<ChatRow>()
        extractAllRows(cursor,target)
        cursor.close()

        return target
    }

    fun getChatRoom(id: String): ChatRow?
    {
        val selection = "$ID_COL = ?"
        val args = arrayOf(id)

        val cursor = readableDatabase.query(
            TABLE_NAME, null, selection, args, null,null,null,null
        )

        if(cursor.count <= 0)
        {
            cursor.close()
            return null
        }
        cursor.moveToNext()
        val res = extractChatRow(cursor)
        cursor.close()
        return res
    }

    fun removeAllChatRooms(){
        writableDatabase.delete(TABLE_NAME,null,null)
    }

    fun removeChatRoom(id: String)
    {
        val selection = "$ID_COL = ?"
        val args = arrayOf(id)

        writableDatabase.delete(TABLE_NAME,selection,args)
    }

    fun updateChatRoom(chatRow: ChatRow)
    {
        val values = ContentValues().apply {
            put(NAME_COL, chatRow.name)
            put(OWNER_COL, chatRow.owner)
            put(MEMBERS_COL, convertListToJson(chatRow.members))
        }
        val selection = "$ID_COL = ?"
        val args = arrayOf(chatRow.id)

        writableDatabase.update(TABLE_NAME,values,selection,args)
    }

    companion object{
        const val DB_NAME = "JMB_BMS_CHAT_ROOMS"
        const val DB_VERSION = 1
        const val TABLE_NAME = "CHATS"
        const val NAME_COL = "NAME"
        const val ID_COL = "ID"
        const val OWNER_COL = "OWNER"
        const val MEMBERS_COL = "MEMBERS"
    }
}