package com.example.jmb_bms.model

import com.example.jmb_bms.viewModel.ServerInfoVM
import kotlin.reflect.KClass

class MenuListsHelper {

    private inline fun createBottomGroundList(low: Int, high: Int): List<OpenableMenuItem>
    {
        return GroundIcon.entries.filterIndexed { index, _ ->
            index in low..high
        }.map {
            OpenableMenuItem(it.iconCode,it.toString(),null,it){vm -> vm.finishCreatingSymbol()}
        }
    }

    private inline fun  createBottomAirList(low: Int, high: Int): List<OpenableMenuItem>
    {
        return AirSymbols.entries.filterIndexed { index, _ ->
            index in low..high
        }.map {
            OpenableMenuItem(it.iconCode,it.toString(),null,it){vm -> vm.finishCreatingSymbol()}
        }
    }

    private val spaceList = SpaceSymbols.entries.map { item ->
        OpenableMenuItem(item.iconCode,item.toString(),null,item){ vm: ServerInfoVM -> vm.finishCreatingSymbol() }
    }

    private val airFirstLevel = listOf(
        OpenableMenuItem("-","Air Track",null,AirSymbols.AIR_TRACK){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("AMF","Military - Fixed Wing",null,null,null),
        OpenableMenuItem("AMD", "Military - Drone",null,null,null),
        OpenableMenuItem("AMR", "Military - Rotary Wing", null,null,null),
        OpenableMenuItem("AW","Military - Weapon", null,AirSymbols.WEAPON){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("AWM", "Military - Missile, Bomb..." , null,null,null),
        OpenableMenuItem("AC","Civilian",null,null,null)
        )

    private fun airFixedWing() = AirSymbols.entries.filterIndexed{ index, _ ->
        index in AirSymbols.FIXED_WING.ordinal ..AirSymbols.AIRBORNE_COMMAND_POST.ordinal ||
                index in AirSymbols.ASW_CARRIER_BASED.ordinal..AirSymbols.SOF.ordinal ||
                index == AirSymbols.LIGHTER_THEN_AIR.ordinal

    }.map {
        OpenableMenuItem(it.iconCode,it.toString(),null,it){vm -> vm.finishCreatingSymbol()}
    }
    private fun airDrone() = createBottomAirList(AirSymbols.DRONE__UAV.ordinal,AirSymbols.DRONE_MEDEVAC.ordinal)
    private fun airRotaryWing() = createBottomAirList(AirSymbols.ROTARY_WING.ordinal,AirSymbols.RW_ECM.ordinal)
    private fun airMissiles() = createBottomAirList(AirSymbols.MISSILE_IN_FLIGHT.ordinal,AirSymbols.DECOY.ordinal)
    private fun airCiv() = createBottomAirList(AirSymbols.CIVIL_AIRCRAFT.ordinal,AirSymbols.C_LIGHTER_THEN_AIR.ordinal)

    private fun groundLvl1() = listOf(
        OpenableMenuItem("-","Ground Track",null,GroundIcon.GROUND_TRACK){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("GLU", "Ground Unit", null,null),
        OpenableMenuItem("GLE","Ground Equipment", null,null),
        OpenableMenuItem("GLI", "Ground Installations", null,null),
    )

    private inline fun groundUnitLvl2() = listOf(
        OpenableMenuItem("U","Unit", null, GroundIcon.UNIT){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("UC","Combat", null, GroundIcon.COMBAT){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("UAD", "Air Defence", null,GroundIcon.AIR_DEFENSE){vm -> vm.finishCreatingSymbol()}, //this might actually work
        OpenableMenuItem("UA", "Armor", null,null),
        OpenableMenuItem("UAA", "Anti Armor",null,null),
        OpenableMenuItem("UAV","Aviation",null,GroundIcon.AVIATION){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("UIE", "Infantry and Engineer", null,null),
        OpenableMenuItem("UFA", "Field artillery",null,GroundIcon.FIELD_ARTILLERY){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("UCS", "Combat support",null,GroundIcon.COMBAT_SUPPORT){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("UO","Other",null,null)
    )

    private inline fun groundUnitCSLvl3() = listOf(
        OpenableMenuItem("SN","NBC", null, GroundIcon.NBC){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("SI", "Military Intelligence",null,GroundIcon.MILITARY_INTELLIGENCE){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("SS", "Signal Unit",null,GroundIcon.SIGNAL_UNIT){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("SL","Law Enforcement",null,GroundIcon.LAW_ENFORCEMENT_UNIT){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("SCSS", "Command Service Support",null,GroundIcon.COMBAT_SERVICE_SUPPORT){vm -> vm.finishCreatingSymbol()},
        OpenableMenuItem("SO", "Other", null,null)
    )

    private inline fun groundUnitNBCLvl4() = createBottomGroundList(GroundIcon.NBC.ordinal, GroundIcon.DECONTAMINATION.ordinal)

    private inline fun groundUnitMilIntLvl4() = createBottomGroundList(GroundIcon.MILITARY_INTELLIGENCE.ordinal,GroundIcon.JOINT_INTELLIGENCE_CENTER.ordinal)

    private inline fun groundUnitLELvl4() = createBottomGroundList(GroundIcon.LAW_ENFORCEMENT_UNIT.ordinal, GroundIcon.CENTRAL_INTELLIGENCE_DIVISION_CID.ordinal)

    private inline fun groundUnitSSLvl4() = createBottomGroundList(GroundIcon.SIGNAL_UNIT.ordinal,GroundIcon.ELECTRONIC_RANGING.ordinal)

    private inline fun groundUnitOLvl4() = createBottomGroundList(GroundIcon.INFORMATION_WARFARE_UNIT.ordinal,GroundIcon.EXPLOSIVE_ORDNANCE_DISPOSAL.ordinal)
    private inline fun groundUnitCSSLvl4() = createBottomGroundList(GroundIcon.COMBAT_SERVICE_SUPPORT.ordinal, GroundIcon.ELECTROOPTICAL_CORPS.ordinal)
    private inline fun groundUnitADLvl3() = createBottomGroundList(GroundIcon.AIR_DEFENSE.ordinal,GroundIcon.THEATER_MISSILE_DEFENSE_UNIT.ordinal)
    private inline fun groundUnitArmorLvl3() = createBottomGroundList(GroundIcon.ARMOR.ordinal,GroundIcon.ARMOR_WHEELED_RECOVERY.ordinal)
    private inline fun groundUnitAArmLvl3() = createBottomGroundList(GroundIcon.ANTI_ARMOR.ordinal,GroundIcon.ANTI_ARMOR_MOTORIZED_AIR_ASSAULT.ordinal)
    private inline fun groundUnitAVLvl3() = createBottomGroundList(GroundIcon.AVIATION.ordinal,GroundIcon.UNMANNED_AERIAL_VEHICLE_ROTARY_WING.ordinal)
    private inline fun groundUnitIELvl3() = createBottomGroundList(GroundIcon.INFANTRY.ordinal,GroundIcon.ENGINEER_NAVAL_CONSTRUCTION.ordinal)
    private inline fun groundUnitFALvl3() = createBottomGroundList(GroundIcon.FIELD_ARTILLERY.ordinal,GroundIcon.MOUNTAIN_METEOROLOGICAL.ordinal)
    private inline  fun groundUnitOLvl3() = createBottomGroundList(GroundIcon.RECONNAISSANCE.ordinal,GroundIcon.INTERNAL_SEC_FORCES_AVIATION.ordinal)




    private fun getLvl0Lists(id: String) : List<OpenableMenuItem>?
    {
        return when(id) {
            "P" -> spaceList
            "A" -> airFirstLevel
            "G" -> groundLvl1()
            "S" -> null
            "U" -> null
            "F" -> null
            "I" -> null
            "X" -> null
            else -> null
        }
    }

    private fun getLvl1Lists(id: String) : List<OpenableMenuItem>?
    {
        return when(id){
            "AMF" -> airFixedWing()
            "AMD" -> airDrone()
            "AMR" -> airRotaryWing()
            "AWM" -> airMissiles()
            "AC" -> airCiv()
            "GLU" -> groundUnitLvl2()
            else -> null
        }
    }
    private fun getLvl2Lists(id: String) : List<OpenableMenuItem>?
    {
        return when(id)
        {
            "UAD" -> groundUnitADLvl3()
            "UA" -> groundUnitArmorLvl3()
            "UAA" -> groundUnitAArmLvl3()
            "UAV" -> groundUnitAVLvl3()
            "UIE" -> groundUnitIELvl3()
            "UFA" -> groundUnitFALvl3()
            "UCS" -> groundUnitCSLvl3()
            "UO" -> groundUnitOLvl3()
            else -> null
        }
    }

    private fun getLvl3Lists(id: String): List<OpenableMenuItem>?
    {
        return when(id)
        {
            "SN" -> groundUnitNBCLvl4()
            "SI" -> groundUnitMilIntLvl4()
            "SS" -> groundUnitSSLvl4()
            "SL" -> groundUnitLELvl4()
            "SCSS" -> groundUnitCSSLvl4()
            "SO" -> groundUnitOLvl4()
            else -> null
        }
    }

    fun getList( id: String, level: Int) : List<OpenableMenuItem>?
    {
        return when(level)
        {
            0 -> getLvl0Lists(id)
            1 -> getLvl1Lists(id)
            else -> return null
        }

    }
}