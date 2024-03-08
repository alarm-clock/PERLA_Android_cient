package com.example.jmb_bms.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.SparseArray
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import armyc2.c2sd.renderer.MilStdIconRenderer
import armyc2.c2sd.renderer.ModifierRenderer
import armyc2.c2sd.renderer.SinglePointRenderer
import armyc2.c2sd.renderer.utilities.MilStdAttributes
import armyc2.c2sd.renderer.utilities.ModifiersTG
import armyc2.c2sd.renderer.utilities.ModifiersUnits
import armyc2.c2sd.renderer.utilities.RendererSettings
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


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

    var iconCode : IconCode2525 = GroundIcon.UNIT
    var imageBitmap: ImageBitmap? = null
        private set

    private var somethingChanged = false
    var invalidIcon = false


    constructor( symbolCode: String, context: Context, size: String = "150")
    {
        this.symbolCode = symbolCode

        editEnumsBasedOnString()
        somethingChanged = true
        try {
            Log.d("Symbol","Invalid icon: $invalidIcon")
            if(!invalidIcon) createIcon(context, size)

        } catch (_: Exception)
        {
            invalidIcon = true
        }
    }

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
    constructor(context: Context)
    {
        invalidIcon = true
        //editEnumsBasedOnString()
        //createIcon(context)
    }

    private fun removeMinus(string: String): String
    {
        val regex = "-+".toRegex()
        return  string.replace(regex,"")
    }

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

    private fun getWarValues(): Array<IconCode2525>?
    {
        when(dimension)
        {
            BattleDimension.SPACE -> {
                val spaceSymbols = enumValues<SpaceSymbols>()
                return spaceSymbols.map { it }.toTypedArray()
            }
            BattleDimension.AIR -> {
                val airSymbols = enumValues<AirSymbols>()
                return airSymbols.map { it }.toTypedArray()
            }
            BattleDimension.GROUND -> {
                val groundSymbols = enumValues<GroundIcon>()
                return groundSymbols.map { it }.toTypedArray()
            }
            BattleDimension.SEA_SURFACE -> {
                val seaSymbols = enumValues<SeaSurfaceIcon>()
                return seaSymbols.map { it }.toTypedArray()
            }
            BattleDimension.SEA_SUBSURFACE -> {
                val subSurfaceSymbols = enumValues<SubSeaSurfaceIcon>()
                return subSurfaceSymbols.map { it }.toTypedArray()
            }
            BattleDimension.SOF -> {
                val sofSymbols = enumValues<SOFicon>()
                return sofSymbols.map { it }.toTypedArray()
            }
            //this value is most likely not supported ath this time
            else -> return null
        }
    }
    fun checkAndStoreIconCode(string: String ) : Boolean
    {
        var values : Array<IconCode2525>?

        if( cScheme == CodingScheme.WAR_FIGHT)
        {
            values = getWarValues()
            if ( values == null) return false

        } else if( cScheme == CodingScheme.TACTICAL_GRAPH) {
            val graphics = enumValues<TacticalGraphicsIconCode>()
            values = graphics.map { it }.toTypedArray()
        } else return false

        values.forEach { value ->
            if(value.iconCode == string)
            {
                iconCode = value
                return true
            }
        }
        return false
    }

    fun getSymbolCode() = symbolCode
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
    private fun checkDimensionAndScheme(): Boolean
    {
        if(cScheme == CodingScheme.INTELLIGENCE)
            return !(dimension == BattleDimension.SOF || dimension == BattleDimension.OTHER)

        return true
    }

    fun editIconCode( code: IconCode2525 ) : Boolean
    {
        if( !checkIfDimensionIsCorrectWithIcon(code) ) return false

        iconCode = code
        somethingChanged = true

        return true
    }

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

    private fun constructCodeFromEnums() : Boolean
    {
        if(!checkDimensionAndScheme()) return false

        var newStr = cScheme.character.toString() + affiliation.character.toString()

        if( cScheme == CodingScheme.TACTICAL_GRAPH) newStr += cathegory.character.toString()
        else newStr += dimension.character.toString()

        newStr += (status.character.toString() + iconCode.iconCode)

        newStr = fillWithMinus(newStr)

        symbolCode = newStr

        invalidIcon = false
        return true
    }

    //TODO refactor this function so it looks better and most importantly it checks for errors
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