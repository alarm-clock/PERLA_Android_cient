/**
 * @file: JSONListConverters.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing JSONListConverters static object
 */
package com.example.jmb_bms.model.database

import org.json.JSONArray

/**
 * Static object with JSON <-> List methods
 */
object JSONListConverters {

    /**
     * Method that converts [List]<[Any]>? into JSON string that can be stored in DB. Method uses [Any.toString] method
     * to convert object into string.
     * @param list List that will be converted into JSON string
     * @return JSON string created from [list] parameter or null if [list] was null
     */
     fun convertListToJson(list: List<Any>?): String?
    {
        if(list == null) return null

        val convList = list.map { it.toString() }
        val jsonArr = JSONArray(convList)
        return jsonArr.toString()
    }

    /**
     * Method that converts JSON string into [List]<[String]>.
     * @param json JSON string containing list that will be converted into [List].
     * @return [List]<[String]> from JSON string or null if [json] is null
     */
    fun convertJsonToList(json: String?): List<String>?
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
}