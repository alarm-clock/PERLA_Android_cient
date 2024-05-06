/**
 * @file: ChatDBHelper.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ChatDBHelper class
 */
package com.example.jmb_bms.model.database.chat

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteCursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.jmb_bms.model.database.JSONListConverters
import org.json.JSONArray

/**
 * Class that is used to do database queries for chats SQLite database. All chats are stored in this database
 * @param context Context used to initialize [SQLiteOpenHelper]
 * @param factory Custom [SQLiteDatabase.CursorFactory]
 */
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

    /**
     * Method that inserts new chat room into table
     * @param chatRow Chat room that will be inserted
     */
    fun addChatRoom(chatRow: ChatRow)
    {
        val values = ContentValues().apply {
            put(ID_COL,chatRow.id)
            put(OWNER_COL, chatRow.owner)
            put(NAME_COL, chatRow.name)
            put(MEMBERS_COL, JSONListConverters.convertListToJson(chatRow.members))
        }

        writableDatabase.insert(TABLE_NAME,null,values)
    }

    /**
     * Method that initializes new [ChatRow] instance with values in row referenced by [cursor]
     * @param cursor Cursor referencing row in table
     * @return Initialized [ChatRow] instance
     */
    private fun extractChatRow(cursor: Cursor): ChatRow
    {
        val id = cursor.getString(cursor.getColumnIndexOrThrow(ID_COL))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL))
        val owner = cursor.getString(cursor.getColumnIndexOrThrow(OWNER_COL))
        val members = JSONListConverters.convertJsonToList(cursor.getString(cursor.getColumnIndexOrThrow(MEMBERS_COL)))

        return ChatRow(id,name,owner,members!!)
    }

    /**
     * Method that adds all [ChatRow]s into [target] list extracted from [cursor]
     * @param cursor Cursor with table rows
     * @param target [MutableList]<[ChatRow]> into which extracted rows will be added
     */
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

    /**
     * Method that returns all chat rooms stored in DB
     * @return [List]<[ChatRow]> with all chat rooms stored in DB or null if there is no chat stored
     */
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

    /**
     * Method that gets row from database with same [id]
     * @param id ID of chat room
     * @return [ChatRow] or null if no chat room with [id] exists
     */
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

    /**
     * Method that removes all rows from table
     */
    fun removeAllChatRooms(){
        writableDatabase.delete(TABLE_NAME,null,null)
    }

    /**
     * Method that removes chat room identified by [id] from table
     * @param id ID of chat room that will be deleted
     */
    fun removeChatRoom(id: String)
    {
        val selection = "$ID_COL = ?"
        val args = arrayOf(id)

        writableDatabase.delete(TABLE_NAME,selection,args)
    }

    /**
     * Method that updates chat rooms database entry with values stored in [chatRow] and identified by [ChatRow.id] attribute.
     * @param chatRow [ChatRow] instance with updated values
     */
    fun updateChatRoom(chatRow: ChatRow)
    {
        val values = ContentValues().apply {
            put(NAME_COL, chatRow.name)
            put(OWNER_COL, chatRow.owner)
            put(MEMBERS_COL, JSONListConverters.convertListToJson(chatRow.members))
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