/**
 * @file: Symbol.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing Symbol class
 */
package com.example.jmb_bms.model.icons

import android.content.Context
import android.content.SharedPreferences
import android.util.SparseArray
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import armyc2.c2sd.renderer.MilStdIconRenderer
import armyc2.c2sd.renderer.SinglePointRenderer
import armyc2.c2sd.renderer.utilities.MilStdAttributes
import armyc2.c2sd.renderer.utilities.RendererSettings

/**
 * Class that holds all data and method required to create symbol. Every symbol is created from string code with
 * length 15 characters. To understand what every character means you must learn how 2525 symbology works. Characters
 * like coding scheme, affiliation, or icon code are stored in attributes that can be accessed directly. Some setters also
 * have checks if compatible combination are used. After that point is rendered by invoking method [createIcon]. If
 * symbol is not created from menu but from whole icon string then just use constructor tha takes symbol code as parameter
 * and symbol will be rendered right away. Rendered symbol can be then accessed in [imageBitmap] attribute and its validity
 * in [invalidIcon] attribute. Invalid icon is icon that can not be rendered.
 */
class Symbol {

    private var symbolCode: String = "---------------"

    var cScheme: CodingScheme = CodingScheme.OTHER
        set(value) {
            somethingChanged = true
            field = value
        }
    var affiliation: Affiliation = Affiliation.UNKNOWN
        set(value) {
            somethingChanged = true
            field = value
        }

    var dimension: BattleDimension = BattleDimension.OTHER
        set(value) {
            val oldVal = dimension
            field = value
            if(!checkDimensionAndScheme())
            {
                field = oldVal
            }
            somethingChanged = true
        }
    var cathegory: Cathegory = Cathegory.OTHER
        set(value) {
            field = value
            somethingChanged = true
        }
    var status: Status = Status.ANTICIPATED
        set(value) {
            somethingChanged = true
            field = value
        }

    var iconCode : String = Icon.GROUND
    var imageBitmap: ImageBitmap? = null
        private set

    private var somethingChanged = false
    var invalidIcon = false

    /**
     * Constructor that renders symbol by invokes [createIcon] method right away. It also sets all attribute to represent
     * rendered symbol. If rendering fails [invalidIcon] will be set to true
     * @param symbolCode Code of symbol that will be rendered
     * @param context Context used for rendering symbols
     * @param size Symbols size, it must be string because lib takes it as a string
     */
    constructor( symbolCode: String, context: Context, size: String = "150")
    {
        this.symbolCode = symbolCode

        editEnumsBasedOnString()
        somethingChanged = true
        try {
            //Log.d("Symbol","Invalid icon: $invalidIcon")
            if(!invalidIcon) createIcon(context, size)

        } catch (_: Exception)
        {
            invalidIcon = true
        }
    }

    /**
     * Constructor that renders symbol from [SharedPreferences] It also sets all attribute to represent
     * rendered symbol. If rendering fails [invalidIcon] will be set to true.
     * @param shPref [SharedPreferences] where symbol code is stored under "jmb_bms_user_symbol" key
     * @param context Context used for rendering symbol
     * @param size Symbols size, it must be string because lib takes it as a string
     */
    constructor( shPref: SharedPreferences, context: Context, size: String = "150")
    {
        this.symbolCode = shPref.getString("jmb_bms_user_symbol", "---------------").toString()

        editEnumsBasedOnString()
        try {

            if(!invalidIcon) createIcon(context,size)

        } catch (_: Exception)
        {
            invalidIcon = true
        }
    }

    /**
     * Constructor that does not set anything and initializes instance by default value.
     */
    constructor(context: Context)
    {
        invalidIcon = true
        //editEnumsBasedOnString()
        //createIcon(context)
    }

    /**
     * Method that removes '-' character from [string] and returns it
     * @param string String from which all '-' characters will be removed
     * @return String without '-' character
     */
    private fun removeMinus(string: String): String
    {
        val regex = "-+".toRegex()
        return  string.replace(regex,"")
    }

    /**
     * Method that stores scheme and also checks if it is correct scheme
     * @param string String with scheme
     * @return True if scheme was stored and correct else false
     */
    private fun checkAndStoreScheme(string: String): Boolean
    {
        val possibleVals = enumValues<CodingScheme>()

        possibleVals.forEach { value ->
            if( value.character == string[0] )
            {
                cScheme = value
                return true
            }
        }
        return false
    }

    /**
     * Method that stores affiliation and also checks if it is correct scheme
     * @param string String with affiliation
     * @return True if affiliation was stored and correct else false
     */
    private fun checkAndStoretAffiliation(string: String): Boolean
    {
        val possibleVals = enumValues<Affiliation>()

        possibleVals.forEach { value ->
            if( value.character == string[1])
            {
                affiliation = value
                return true
            }
        }
        return false
    }

    /**
     * Method that stores battle dimension or category and also checks if it is correct scheme
     * @param string String with battle dimension or category
     * @return True if battle dimension or category was stored and correct else false
     */
    private fun checkAndStoreDimensionOrCathegory(string: String): Boolean
    {
        if(cScheme == CodingScheme.TACTICAL_GRAPH) {
            val possibleVals = enumValues<Cathegory>()

            possibleVals.forEach { value ->
                if (value.character == string[2]) {
                    cathegory = value
                    return true
                }
            }
            return false
        }
        else
        {
            val possibleVals = enumValues<BattleDimension>()
            possibleVals.forEach { value ->
                if (value.character == string[2]) {
                    dimension = value
                    return true
                }
            }
            return false
        }
    }

    /**
     * Method that stores status and also checks if it is correct scheme
     * @param string String with status
     * @return True if status was stored and correct else false
     */
    private fun checkAndStoreStatus(string: String) : Boolean
    {
        if(string[3] == Status.PRESENT.character )
        {
            status = Status.PRESENT
            return true

        } else if( string[3] == Status.ANTICIPATED.character)
        {
            status = Status.ANTICIPATED
            return true
        }
        return false
    }

    //no way I can check that now
    /**
     * Method that only stores icon code
     * @param string icon code
     * @return true
     */
    fun checkAndStoreIconCode(string: String ) : Boolean
    {
        iconCode = string
        return true
    }

    /**
     * Getter for symbol code
     * @return String with symbol code
     */
    fun getSymbolCode() = symbolCode

    /**
     * Method that initializes attributes from icon string and checks if [symbolCode] string is correct
     *
     */
    private fun editEnumsBasedOnString()
    {
        val workinCode = removeMinus(symbolCode)
        if(workinCode.isEmpty())
        {
            invalidIcon = true
            return
        }

        if( !checkAndStoreScheme(workinCode)) invalidIcon = true
        if( !checkAndStoretAffiliation(workinCode)) invalidIcon = true
        if( !checkAndStoreDimensionOrCathegory(workinCode)) invalidIcon = true
        if( !checkAndStoreStatus(workinCode)) invalidIcon = true

        if( !checkAndStoreIconCode(if( workinCode.length < 4 ) "" else workinCode.substring(4))) invalidIcon = true

    }

    fun getBitmap() = imageBitmap

    /*
    private fun checkIfDimensionIsCorrectWithIcon(code: IconCode2525): Boolean
    {
        var returnVal: Boolean = when(code::class) {
            GroundIcon::class -> dimension == BattleDimension.GROUND
            SpaceSymbols::class -> dimension == BattleDimension.SPACE
            AirSymbols::class -> dimension == BattleDimension.AIR
            SeaSurfaceIcon::class -> dimension == BattleDimension.SEA_SURFACE
            SubSeaSurfaceIcon::class -> dimension == BattleDimension.SEA_SUBSURFACE
            SOFicon::class -> dimension == BattleDimension.SOF
            else -> true
        }

        return  returnVal
    }

     */
    private fun checkDimensionAndScheme(): Boolean
    {
        if(cScheme == CodingScheme.INTELLIGENCE)
            return !(dimension == BattleDimension.SOF || dimension == BattleDimension.OTHER)

        return true
    }
/*
    fun editIconCode( code: IconCode2525) : Boolean
    {
        if( !checkIfDimensionIsCorrectWithIcon(code) ) return false

        iconCode = code
        somethingChanged = true

        return true
    }


 */
    /**
     * Method that fills [string] with '-' characters until length of 15
     * @param string String that will be filled
     * @return [string] that has filled remaining length with '-' characters
     */
    fun fillWithMinus( string: String) : String
    {
        val suffLen = 15 - string.length
        var newSuffix = ""
        for( cnt in 0 until suffLen)
        {
            newSuffix += "-"
        }
        return string + newSuffix
    }

    /**
     * Method that creates symbol code from stored attributes like [affiliation] or [dimension] and stores it in
     * [symbolCode] attribute
     * @return True if code was built else false, [invalidIcon] attribute is set to negation of returned value
     */
    private fun constructCodeFromEnums() : Boolean
    {
        if(!checkDimensionAndScheme()) return false

        var newStr = cScheme.character.toString() + affiliation.character.toString()

        if( cScheme == CodingScheme.TACTICAL_GRAPH) newStr += cathegory.character.toString()
        else newStr += dimension.character.toString()

        newStr += (status.character.toString() + iconCode)

        newStr = fillWithMinus(newStr)

        symbolCode = newStr

        invalidIcon = false
        return true
    }


    /**
     * Method that renders 2525 symbol from attributes stored in instance. Rendered image is returned and stored in
     * [imageBitmap] parameter. Byproduct is also symbol code stored in [symbolCode] attribute.
     * @param context Context for rendering symbol
     * @param size Rendered symbol size (for example "150" or "25")
     * @return [ImageBitmap] on success or null if symbol could not be rendered
     */
    fun createIcon(context: Context, size: String) : ImageBitmap?
    {
        //if(invalidIcon && !somethingChanged) return null
        if(somethingChanged)
        {
            constructCodeFromEnums()
            somethingChanged = false
        } else {
            return imageBitmap
        }

        val mir = MilStdIconRenderer.getInstance()
        if( mir == null) {

            println("--------------------------mir is null----------------------------")
        }
        val cacheDir: String = context.cacheDir.getAbsoluteFile().absolutePath
        mir.init(context, cacheDir)

        val modifiers =  SparseArray<String>()
        /* modifiers.put(ModifiersUnits.C_QUANTITY,"10");
         modifiers.put(ModifiersUnits.H_ADDITIONAL_INFO_1,"H");
         modifiers.put(ModifiersUnits.H1_ADDITIONAL_INFO_2,"H1")
 */
        val attributes = SparseArray<String>()



        attributes.put(MilStdAttributes.PixelSize,size);
        attributes.put(MilStdAttributes.KeepUnitRatio,"true");
        attributes.put(MilStdAttributes.SymbologyStandard, RendererSettings.Symbology_2525C.toString() )

        //modifiers.put(ModifiersTG.H2_ADDITIONAL_INFO_3,"H2")
        val e = SinglePointRenderer.getInstance()
        val bitmap = if(cScheme == CodingScheme.WAR_FIGHT) e.RenderUnit(symbolCode,modifiers,attributes)
            else e.RenderSP(symbolCode,modifiers,attributes)


        if(bitmap == null)
        {
            imageBitmap = null
            return null
        }
        imageBitmap = bitmap.image.asImageBitmap()
        return bitmap.image.asImageBitmap()
    }
}