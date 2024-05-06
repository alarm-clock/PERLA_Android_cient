/**
 * @file: ServerInfo.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ServerInfo class
 */
package com.example.jmb_bms.model

import android.content.Context
import android.content.SharedPreferences
import com.example.jmb_bms.model.icons.Symbol
import com.example.jmb_bms.model.icons.SymbolModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Class that is model for ServerInfo screen and holds all data user entered.
 * @param context Context for rendering symbol
 * @param shPref [SharedPreferences] that hold all stored data
 * @constructor Initializes instance with values stored in [SharedPreferences]
 */
class ServerInfo(context: Context, private val shPref: SharedPreferences): SymbolModel {

    var ipV4: String = ""
        /**
         * Setter for [ipV4] that checks if attribute is empty and stores [value] in shared preferences
         */
        set(value) {
            ipV4WasEntered = value != ""
            field = value
            shPref.editPreferences { editor ->
                editor.putString("ServerInfo_IP", value)
                editor.putBoolean("ServerInfo_IPent",ipV4WasEntered)
                editor.apply()
            }
        }

    var ipV4WasEntered: Boolean = false
        private set


    private var _port: Int = -1
    val port : Int get() = _port

    var portString: String = ""
        /**
         * Setter for [portString] that checks if attribute is empty and stores [value] in shared preferences and sets [_port] attribute
         */
        set(value) {
            portWasEntered = value != ""
            if(portWasEntered)
            {
                _port = try {
                    value.toInt()
                } catch (_: Exception)
                {
                    -1 // invalid value
                }
                portWasEntered = _port != -1

            } else {
                _port = -1
            }
            field = value
            shPref.editPreferences { editor ->
                editor.putString("ServerInfo_Port",if(_port != -1) value else "")
                editor.putBoolean("ServerInfo_PortEnt", portWasEntered)
                editor.apply()
            }
        }

    var portWasEntered: Boolean = false
        private set

    var userName: String = ""
        /**
         * Setter for [userName] that checks if attribute is empty and stores [value] in shared preferences
         */
        set(value) {
            userNameWasEntered = value != ""
            field = value

            shPref.editPreferences { editor ->
                editor.putString("ServerInfo_User", value)
                editor.putBoolean("ServerInfo_UserEnt",userNameWasEntered)
                editor.apply()
            }
        }
    var userNameWasEntered: Boolean = false
        private set

    /**
     * Method that returns true if all values were entered
     * @return True if all values were entered otherwise false
     */
    override fun everyThingEntered() = (userNameWasEntered && portWasEntered && ipV4WasEntered && symbolIsValid())

    /**
     * Method that returns if rendered symbol is valid
     * @return True if rendered symbol is valid otherwise false
     */
    override fun symbolIsValid() = (symbol.imageBitmap != null) //&& !symbol.invalidIcon

    override var symbolString = ""
        /**
         * Setter for [symbolString] that stores [value] in shared preferences
         */
        set(value)
        {
            shPref.editPreferences { editor ->
                editor.putString("ServerInfo_Symbol",value)
                editor.apply()
            }
            field = value
        }

    override var symbol : Symbol

    override var menuIdsString = ""
        /**
         * Setter for [menuIdsString] that stores [value] in shared preferences
         */
        set(value) {
            shPref.editPreferences {editor ->
                editor.putString("ServerInfo_IconMenuLists",value)
                editor.apply()
            }
            field = value
        }

    init{
        ipV4 = shPref.getString("ServerInfo_IP", "") ?: ""
        ipV4WasEntered = shPref.getBoolean("ServerInfo_IPent", false)

        portString = shPref.getString("ServerInfo_Port", "") ?: ""
        portWasEntered = shPref.getBoolean("ServerInfo_PortEnt", false)

        userName = shPref.getString("ServerInfo_User", "") ?: ""
        userNameWasEntered = shPref.getBoolean("ServerInfo_UserEnt", false)

        menuIdsString = shPref.getString("ServerInfo_IconMenuLists","") ?: ""

        symbolString = shPref.getString("ServerInfo_Symbol", "") ?: ""

        symbol = if (symbolString == "") Symbol(context)
                     else Symbol(symbolString, context)

        symbol.createIcon(context,"250")

    }

    /**
     * Extension method for [SharedPreferences] that does [operation] in IO coroutine scope
     * @param operation Closure with commands that happen inside shared preferences
     */
    private fun SharedPreferences.editPreferences(operation: (SharedPreferences.Editor) -> Unit)
    {
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            withContext(Dispatchers.IO)
            {
                val ed = edit()
                operation(ed)
                ed.apply()
            }
        }
    }
}
