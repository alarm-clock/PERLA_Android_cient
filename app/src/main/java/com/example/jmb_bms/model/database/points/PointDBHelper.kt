package com.example.jmb_bms.model.database.points

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import androidx.compose.ui.graphics.asAndroidBitmap
import com.example.jmb_bms.model.icons.Symbol
import locus.api.android.ActionDisplayPoints
import locus.api.android.objects.PackPoints
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Point
import locus.api.objects.geoData.addAttachmentPhoto
import locus.api.objects.geoData.parameterDescription
import org.json.JSONArray
import kotlin.concurrent.thread

class PointDBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?):
    SQLiteOpenHelper(context, DB_NAME, factory, DB_VERSION)
{

    override fun onCreate(db: SQLiteDatabase) {
        var q = "CREATE TABLE $TABLE_NAME ($ID_COL INTEGER PRIMARY KEY, $NAME_COL TEXT, $ONLINE_COL INTEGER, $OWNER_COL TEXT, $OWNER_NAME_COL TEXT, $LOC_COL TEXT, $SYMBOL_COL TEXT, $DESCR_COL TEXT, $DOC_URIS_COL TEXT, $VISIBLE_COL INTEGER, $SYMBOL_MENU_STRING TEXT, $SERVER_ID_COL TEXT, $POSTED_TO_SERV_COL INTEGER )"
        db.execSQL(q)
        q = "CREATE TABLE $TMP_POINTS_TABLE ($ID_COL INTEGER PRIMARY KEY, $NAME_COL TEXT, $ONLINE_COL INTEGER, $OWNER_COL TEXT, $OWNER_NAME_COL TEXT, $LOC_COL TEXT, $SYMBOL_COL TEXT, $DESCR_COL TEXT, $DOC_URIS_COL TEXT, $VISIBLE_COL INTEGER, $SYMBOL_MENU_STRING TEXT, $SERVER_ID_COL TEXT, $POSTED_TO_SERV_COL INTEGER )"
        db.execSQL(q)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TMP_POINTS_TABLE")
        onCreate(db)
    }

    private fun convertLocation(location: Location): String = "${location.latitude}|${location.longitude}"
    private fun convertLocation(location: String): Location
    {
        val list = location.split('|')
        return Location(list[0].toDouble(), list[1].toDouble())
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

    fun addPoint(pointRow: PointRow): Long {
        val values = ContentValues().apply {
            put(NAME_COL, pointRow.name)
            put(ONLINE_COL, if (pointRow.online) 1 else 0)
            if (pointRow.online && pointRow.ownerId != null) {
                put(OWNER_COL, pointRow.ownerId)
                put(OWNER_NAME_COL, pointRow.ownerName)
            }
            put(LOC_COL, convertLocation(pointRow.location))
            put(SYMBOL_COL, pointRow.symbol)
            put(DESCR_COL, pointRow.descr)
            put(DOC_URIS_COL, convertListToJson(pointRow.uris?.toList()))
            put(VISIBLE_COL, 1)
            put(SYMBOL_MENU_STRING,pointRow.menuString)
            put(SERVER_ID_COL,pointRow.serverId)
            put(POSTED_TO_SERV_COL,0)
        }
        val db = writableDatabase

        val res = db.insert(TABLE_NAME, null, values)

     //   db.close()

        return res
    }

    fun removePoint(name: String){
        val selection = "$NAME_COL = ?"
        val args = arrayOf(name)

        val db = writableDatabase
        writableDatabase.delete(TABLE_NAME,selection,args)
    }

    fun removePoint(id: Long)
    {
        val selection = "$ID_COL = ?"
        val args = arrayOf(id.toString())

        val db = writableDatabase
        db.delete(TABLE_NAME,selection,args)
     //   db.close()
    }

    fun removeOwnerPoints(ownerId: String)
    {
        val selection = "$OWNER_COL = ?"
        val args = arrayOf(ownerId)

        writableDatabase.delete(TABLE_NAME,selection,args)
    }

    fun removeOnlinePoints()
    {
        val selection = "$ONLINE_COL != ?"
        val args = arrayOf("0")

        writableDatabase.delete(TABLE_NAME,selection,args)
    }

    private fun updatePoint(point: PointRow, selection: String, args: Array<String>)
    {
        val values = ContentValues().apply {
            put(NAME_COL, point.name)
            put(ONLINE_COL, if(point.online) 1 else 0 )
            put(OWNER_COL, if(point.online && point.ownerId != null) point.ownerId else "")
            put(OWNER_NAME_COL, if(point.online && point.ownerId != null) point.ownerName else "")
            put(LOC_COL,convertLocation(point.location))
            put(SYMBOL_COL, point.symbol)
            put(DESCR_COL, point.descr)
            put(DOC_URIS_COL, convertListToJson(point.uris?.toList()))
            put(SYMBOL_MENU_STRING,point.menuString)
            put(SERVER_ID_COL,point.serverId)
            put(POSTED_TO_SERV_COL,if(point.postedToServer) 1 else 0)
        }
        val db = writableDatabase
        db.update(TABLE_NAME,values,selection,args)
      //  db.close()
    }

    fun updatePointsVisibility(visible: Boolean, id: Long)
    {
        val selection = "$ID_COL = ?"
        val args = arrayOf(id.toString())
        val value = ContentValues().apply {
            put(VISIBLE_COL,if(visible) 1 else 0)
        }
        val db = writableDatabase
        db.update(TABLE_NAME,value,selection,args)
      //  db.close()

    }

    fun updatePointIdentById(point: PointRow){
        val selection = "$ID_COL = ?"
        val args = arrayOf(point.id.toString())

        updatePoint(point, selection, args)
    }

    fun updatePointIdentByName(point: PointRow){
        val selection = "$NAME_COL = ?"
        val args = arrayOf(point.name)

        updatePoint(point, selection, args)
    }

    private fun extractPointRow(cursor: Cursor): PointRow
    {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(ID_COL)) //not catching
        val name = cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL))
        val online = cursor.getInt(cursor.getColumnIndexOrThrow(ONLINE_COL)) != 0
        val ownerId = if(online) cursor.getString(cursor.getColumnIndexOrThrow(OWNER_COL)) else null
        val ownerName = if(online) cursor.getString(cursor.getColumnIndexOrThrow(OWNER_NAME_COL)) else null
        val location = convertLocation(cursor.getString(cursor.getColumnIndexOrThrow(LOC_COL)))
        val symbol = cursor.getString(cursor.getColumnIndexOrThrow(SYMBOL_COL))
        val descr = cursor.getString(cursor.getColumnIndexOrThrow(DESCR_COL))
        val uris = convertJsonToList( cursor.getString(cursor.getColumnIndexOrThrow(DOC_URIS_COL)))?.map { Uri.parse(it) }?.toMutableList()
        val visible = cursor.getInt(cursor.getColumnIndexOrThrow(VISIBLE_COL)) != 0
        val menuString = cursor.getString(cursor.getColumnIndexOrThrow(SYMBOL_MENU_STRING))
        val posted = cursor.getInt(cursor.getColumnIndexOrThrow(POSTED_TO_SERV_COL)) != 0
        val serverId = cursor.getString(cursor.getColumnIndexOrThrow(SERVER_ID_COL))

        return PointRow(id,name,online,ownerId,ownerName,serverId,posted,location,symbol,descr,visible,menuString,uris)
    }

    private fun extractMenuRow(cursor: Cursor): MenuRow
    {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(ID_COL)) //not catching
        val name = cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL))
        val visible = cursor.getInt(cursor.getColumnIndexOrThrow(VISIBLE_COL)) != 0
        val symbol = cursor.getString(cursor.getColumnIndexOrThrow(SYMBOL_COL))
        val online = cursor.getInt(cursor.getColumnIndexOrThrow(ONLINE_COL)) != 0
        val ownerString = cursor.getString(cursor.getColumnIndexOrThrow(OWNER_COL))
        val owner = (ownerString == "Me") || (ownerString == "All")

        return MenuRow(id, name, visible, symbol, !online || owner, online)
    }

    private fun extractAllRows(cursor: Cursor, targetList: MutableList<PointRow>)
    {
        with(cursor)
        {
            while (moveToNext())
            {
                targetList.add(extractPointRow(this))
            }
        }
    }

    private fun extractAllMenuRows(cursor: Cursor, targetList: MutableList<MenuRow>)
    {
        with(cursor)
        {
            while (moveToNext())
            {
                targetList.add(extractMenuRow(cursor))
            }
        }
    }

    fun getPointByServerId(serverId: String): PointRow?
    {
        val selection = "$SERVER_ID_COL = ?"
        val args = arrayOf(serverId)

        val db = readableDatabase

        val cursor = db.query(
            TABLE_NAME,
            null,
            selection,
            args,
            null,
            null,
            null,
            null
        )
        if(cursor.count <= 0)
        {
            cursor.close()
            return null
        }

        cursor.moveToNext()
        val row = extractPointRow(cursor)
        cursor.close()

        return row
    }

    fun getPoint(id: Long): PointRow?
    {
        val selection = "$ID_COL = ?"
        val args = arrayOf(id.toString())

        val db = readableDatabase

        val cursor = db.query(
            TABLE_NAME,
            null,
            selection,
            args,
            null,
            null,
            null,
            null
        )
        if(cursor.count <= 0)
        {
            cursor.close()
            return null
        }

        cursor.moveToNext()
        val row = extractPointRow(cursor)
        cursor.close()

        return row

    }

    fun getAllUnsentPoints(): List<PointRow>?
    {
        val selection = "($ONLINE_COL = ?) AND ($POSTED_TO_SERV_COL = ?) AND (($OWNER_COL = ?) OR ($ONLINE_COL = ?))"
        val args = arrayOf("1","0","Me","All")

        val cursor = readableDatabase.query(
            TABLE_NAME,
            null,
            selection,
            args,
            null,
            null,
            null
        )
        if(cursor.count <= 0)
        {
            cursor.close()
            return null
        }

        val points = mutableListOf<PointRow>()
        extractAllRows(cursor,points)
        cursor.close()

        return points
    }

    fun getUserPointMenuRow(users: Boolean): List<MenuRow>?
    {
        val selection = if(users) "($ONLINE_COL = ?) OR ($OWNER_COL = ?) OR ($OWNER_COL = ?) OR ($OWNER_COL = ?)" else "($ONLINE_COL = ?) AND ($OWNER_COL != ?)"
        val args = if(users) arrayOf( "0" , "Me", "","All") else arrayOf("1", "Me")
        val columns = arrayOf(ID_COL, NAME_COL, VISIBLE_COL, SYMBOL_COL, ONLINE_COL, OWNER_COL)

        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            columns,
            selection,
            args,
            null,null,null,null
        )

        if(cursor.count <= 0)
        {
            cursor.close()
   //         db.close()
            return null
        }
        val res = mutableListOf<MenuRow>()
        extractAllMenuRows(cursor,res)
        cursor.close()
       // db.close()

        return res
    }

    fun getMenuRowByIdWithUsers(id: Long, users: Boolean): MenuRow?
    {
        val selection = "($ID_COL = ?) AND  " + (if(users) "($ONLINE_COL = ?) OR ($OWNER_COL = ?) OR ($OWNER_COL = ?) OR ($OWNER_COL = ?)" else "($ONLINE_COL = ?) AND ($OWNER_COL != ?)")
        val args = if(users) arrayOf( id.toString() , "0" , "Me", "","All") else arrayOf(id.toString(),"1", "Me")
        val columns = arrayOf(ID_COL, NAME_COL, VISIBLE_COL, SYMBOL_COL, ONLINE_COL, OWNER_COL)

        val cursor = readableDatabase.query(
            TABLE_NAME,
            columns,
            selection,
            args,
            null,null,null,null
        )
        if(cursor.count <= 0)
        {
            cursor.close()
            return null
        }
        cursor.moveToNext()
        val menuRow = extractMenuRow(cursor)
        cursor.close()
        return menuRow
    }

    fun getMenuRowById(id: Long): MenuRow?
    {
        val selection = "$ID_COL = ?"
        val args = arrayOf(id.toString())
        val columns = arrayOf(ID_COL, NAME_COL, VISIBLE_COL, SYMBOL_COL, ONLINE_COL, OWNER_COL)

        val cursor = readableDatabase.query(
            TABLE_NAME,
            columns,
            selection,
            args,
            null,null,null,null
        )
        if(cursor.count <= 0)
        {
            cursor.close()
            return null
        }
        cursor.moveToNext()
        val menuRow = extractMenuRow(cursor)
        cursor.close()
        return menuRow
    }

    fun getMenuRowByServerId(serverId: String): MenuRow?
    {
        val selection = "$SERVER_ID_COL = ?"
        val args = arrayOf(serverId)
        val columns = arrayOf(ID_COL, NAME_COL, VISIBLE_COL, SYMBOL_COL, ONLINE_COL, OWNER_COL)

        val cursor = readableDatabase.query(
            TABLE_NAME,
            columns,
            selection,
            args,
            null,null,null,null
        )
        if(cursor.count <= 0)
        {
            cursor.close()
            return null
        }
        cursor.moveToNext()
        val menuRow = extractMenuRow(cursor)
        cursor.close()
        return menuRow
    }

    fun getAllPointMenuRow(): List<MenuRow>?
    {
        val columns = arrayOf(ID_COL, NAME_COL, VISIBLE_COL, SYMBOL_COL, ONLINE_COL, OWNER_COL)

        val cursor = readableDatabase.query(
            TABLE_NAME, columns, null, null, null,null,null,null
        )

        if(cursor.count <= 0) {
            cursor.close()
            //    db.close()
            return null
        }

        val res = mutableListOf<MenuRow>()
        extractAllMenuRows(cursor,res)
        cursor.close()
       // db.close()

        return res
    }

    fun getPoint(name: String): PointRow
    {
        val selection = "$NAME_COL = ?"
        val args = arrayOf(name)

        val db = readableDatabase

        val cursor = db.query(
            TABLE_NAME,
            null,
            selection,
            args,
            null,
            null,
            null,
            null
        )
        cursor.moveToNext()
        val row = extractPointRow(cursor)
        cursor.close()

        return row
    }

    fun getOffOrOnPoints(online: Boolean): List<PointRow>
    {
        val selection = "$ONLINE_COL = ?"
        val args = arrayOf( if(online) "1" else "0")

        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, selection, args, null,null,null,null)

        val rows = mutableListOf<PointRow>()
        extractAllRows(cursor,rows)
        cursor.close()
   ///     db.close()

        return rows
    }

    fun getAllPoints(): List<PointRow>
    {
        val cursor = readableDatabase.query(TABLE_NAME, null, null, null, null, null, null, null)

        val allRows = mutableListOf<PointRow>()
        extractAllRows(cursor,allRows)
        cursor.close()

        return  allRows
    }

    fun getIdServIdByOwner(myPoints: Boolean): List<Pair<Long, String>>
    {
        val selection ="($ONLINE_COL = ?) AND " + if(myPoints) "($OWNER_COL = ?)" else "($OWNER_COL != ?)"
        val args= arrayOf("1","Me")
        val cols = arrayOf(ID_COL, SERVER_ID_COL)

        val cursor = readableDatabase.query(
            TABLE_NAME,
            cols,
            selection,
            args,
            null,null,null
        )

        val list = mutableListOf<Pair<Long, String>>()

        if(cursor.count <= 0)
        {
            cursor.close()
            return list
        }
        while (cursor.moveToNext())
        {
            val id =  cursor.getLong(cursor.getColumnIndexOrThrow(ID_COL))
            val serverId = cursor.getString(cursor.getColumnIndexOrThrow(SERVER_ID_COL))
            list.add(Pair( id , serverId ))
        }
        cursor.close()
        return list

    }

    fun getUserPoints(ownerId: String): List<PointRow>
    {
        val selection = "$OWNER_COL = ?"
        val args = arrayOf(ownerId)

        val cursor = readableDatabase.query(TABLE_NAME, null, selection, args, null,null,null,null)

        val userRows = mutableListOf<PointRow>()
        extractAllRows(cursor,userRows)
        cursor.close()

        return userRows
    }

    fun getPointsLocation(id: Long): Location?
    {
        val selection = "$ID_COL = ?"
        val args = arrayOf(id.toString())
        val columns = arrayOf(LOC_COL)

        val db = readableDatabase

        val cursor = db.query(
            TABLE_NAME,
            columns,
            selection,
            args,
            null,null,null
        )
        if(cursor.count <= 0)
        {
            cursor.close()
 ////           db.close()
            return null
        }
        cursor.moveToNext()

        val location = convertLocation(cursor.getString(cursor.getColumnIndexOrThrow(LOC_COL)))
        cursor.close()
      //  db.close()

        return location
    }

    fun storeTmpRow(pointRow: PointRow)
    {
        val values = ContentValues().apply {
            put(ID_COL,pointRow.id)
            put(NAME_COL, pointRow.name)
            put(ONLINE_COL, if (pointRow.online) 1 else 0)
            if (pointRow.online && pointRow.ownerId != null) {
                put(OWNER_COL, pointRow.ownerId)
                put(OWNER_NAME_COL, pointRow.ownerName)
            }
            put(LOC_COL, convertLocation(pointRow.location))
            put(SYMBOL_COL, pointRow.symbol)
            put(DESCR_COL, pointRow.descr)
            put(DOC_URIS_COL, convertListToJson(pointRow.uris?.toList()))
            put(VISIBLE_COL, if(pointRow.visible) 1 else 0)
            put(SYMBOL_MENU_STRING,pointRow.menuString)
            put(SERVER_ID_COL, pointRow.serverId)
            put(POSTED_TO_SERV_COL, pointRow.postedToServer )
        }
        val db = writableDatabase

        db.insert(TMP_POINTS_TABLE, null, values)
    //    db.close()
    }

    fun getTmpRow(): PointRow
    {
        val cursor = readableDatabase.query(
            TMP_POINTS_TABLE,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        cursor.moveToNext()
        val row = extractPointRow(cursor)

        val db = writableDatabase

        db.delete(TMP_POINTS_TABLE,null,null)
        cursor.close()
    //    db.close()

        return row
    }

    fun createAndSendPoint(pointRow: PointRow, ctx: Context)
    {
        val point = Point(pointRow.name,pointRow.location)
        point.extraData = GeoDataExtra()
        point.extraData!!.addParameter(1,"jmb_bms")
        point.parameterDescription = pointRow.descr
        point.protected = false
        point.isSelected = false
        point.extraData!!.addParameter(2,pointRow.id.toString())

        point.setExtraOnDisplay(
            "com.example.jmb_bms",
            "com.example.jmb_bms.activities.DummyActivity",
            "op",
            "e"
        )

        pointRow.uris?.forEach{uri ->
            point.addAttachmentPhoto(uri.toString(),"photo")
        }
        val packPoints = PackPoints(pointRow.id.toString())
        packPoints.bitmap = Symbol(pointRow.symbol,ctx).imageBitmap!!.asAndroidBitmap()
        packPoints.addPoint(point)
        ActionDisplayPoints.sendPackSilent(ctx,packPoints,false)
    }

    fun sendAllPointsToLoc(ctx: Context)
    {
        thread {
            val cursor = readableDatabase.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )


            val list = mutableListOf<PointRow>()
            extractAllRows(cursor,list)

            list.forEach {
                if(it.visible) {
                    createAndSendPoint(it,ctx)
                }
            }
            cursor.close()
        }
    }

    companion object{
        private const val DB_NAME = "JMB_BMS_POINTS"
        private const val DB_VERSION = 12
        const val TMP_POINTS_TABLE = "TMP_POINTS_TABLE"
        const val TABLE_NAME = "POINTS"
        const val ID_COL = "ID"
        const val NAME_COL = "NAME"
        const val OWNER_COL = "OWNER"
        const val OWNER_NAME_COL = "OWNER_NAME"
        const val ONLINE_COL = "ONLINE"
        const val LOC_COL = "LOCATION"
        const val SYMBOL_COL = "SYMBOL"
        const val DESCR_COL = "DESCRIPTION"
        const val DOC_URIS_COL = "DOCUMENT_URIS"
        const val VISIBLE_COL = "VISIBLE"
        const val SYMBOL_MENU_STRING = "MENUS_STRING"
        const val SERVER_ID_COL = "SERVER_ID"
        const val POSTED_TO_SERV_COL = "POSTED_TO_SERV"

    }
}