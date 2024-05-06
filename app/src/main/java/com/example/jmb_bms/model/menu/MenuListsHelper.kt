/**
 * @file: MenuListsHelper.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing MenuListsHelper class
 */
package com.example.jmb_bms.model.menu

import com.example.jmb_bms.model.OpenableMenuItem
import com.example.jmb_bms.model.icons.*

/**
 * Class that holds point creation menus and dynamically creates them on demand to not use whole system memory.
 * Not all methods are commented because they mostly only hold list with strings from which menu will be created.
 * How it all work is quite simple, Every submenu has its own code. When users picks some element in menu [getList]
 * method is called with level on which menu is and with picked code. Based on those parameters correct level method
 * is called that based on code returns menu that will be showed to user. If from given element symbol can be created
 * then it also has closure that is called that renders symbol.
 */
class MenuListsHelper {
    /*
    * What was hard lesson I learned while writing this class. DO NOT USE LARGE ENUMS IN KOTLIN, ESPECIALLY IF THEY HOLD STRING OR SOMETHING.
    * ENUM CLASS NEEDS TWICE AS MUCH MEMORY AS REGULAR CLASS. COMBINE IT WITH 1400 SYMBOLS AND CREATE LISTS BY TRAVERSING
    * THROUGH WHOLE ENUM USING .entries AND YOUR RAM IS EATEN ALIVE. FOR SOME F****** REASON IT LOADS ALL ENUM ELEMENTS INTO MEMORY
    * ,EVEN BEFORE ANY CALL TO .entries IS REACHED, AND COMMITS SEPPUKU. READER, PLEASE DO NOT USE ENUM UNLESS ITS LIKE 10 ENTRIES
    * OTHERWISE YOU ARE TAKING MEMORY AWAY FROM YOUR STARVING CLASS INSTANCES THAT HAVE BRIGHT FUTURE AHEAD
    */

    /**
     * Method that creates "Bottom" level list with entries from which symbol can be created
     * @param list [List]<[IconTuple]> with all tuples that are used given menu
     * @return [List]<[OpenableMenuItem]> created from [list]
     */
    private fun  createBottomList(list: List<IconTuple>): List<OpenableMenuItem>
    {
        return list.map {
            OpenableMenuItem(it.iconCode,it.iconName,null,it){vm ,context -> vm.finishCreatingSymbol(it ,context)}
        }
    }

    private val spaceList = createBottomList(listOf(
        IconTuple(IconMenuName.SPACE_TRACK, Icon.SPACE_TRACK),
        IconTuple(IconMenuName.SPACE_STATION, Icon.SPACE_STATION),
        IconTuple(IconMenuName.SPACE_LAUNCH_VEHICLE, Icon.SPACE_LAUNCH_VEHICLE),
        IconTuple(IconMenuName.CREWED_SPACE_VEHICLE,Icon.CREWED_SPACE_VEHICLE),
        IconTuple(IconMenuName.SATELLITE,Icon.SATELLITE)
    ))

    private val airFirstLevel = listOf(
        OpenableMenuItem("-","Air Track",null, IconTuple(IconMenuName.AIR_TRACK,Icon.AIR_TRACK)){ vm, context-> vm.finishCreatingSymbol(IconTuple(IconMenuName.AIR_TRACK,Icon.AIR_TRACK),context)},
        OpenableMenuItem("AMF","Military - Fixed Wing",null,null,null),
        OpenableMenuItem("AMD", "Military - Drone",null,null,null),
        OpenableMenuItem("AMR", "Military - Rotary Wing", null,null,null),
        OpenableMenuItem("AW","Military - Weapon", null, IconTuple(IconMenuName.WEAPON,Icon.WEAPON)){ vm, context -> vm.finishCreatingSymbol(
            IconTuple(IconMenuName.WEAPON,Icon.WEAPON),context)},
        OpenableMenuItem("AWM", "Military - Missile, Bomb..." , null,null,null),
        OpenableMenuItem("AC","Civilian",null,null,null)
        )

    fun airFixedWing() = createBottomList(listOf(
        IconTuple(IconMenuName.BOMBER,Icon.BOMBER),
        IconTuple(IconMenuName.FIGHTER,Icon.FIGHTER),
        IconTuple(IconMenuName.INTERCEPTOR,Icon.INTERCEPTOR),
        IconTuple(IconMenuName.TRAINER,Icon.TRAINER),
        IconTuple(IconMenuName.ATTACK,Icon.ATTACK),
        IconTuple(IconMenuName.VSTOL,Icon.VSTOL),
        IconTuple(IconMenuName.TANKER,Icon.TANKER),
        IconTuple(IconMenuName.TANKER_BOOM_ONLY,Icon.TANKER_BOOM_ONLY),
        IconTuple(IconMenuName.TANKER_DROGUE_ONLY,Icon.TANKER_DROGUE_ONLY),
        IconTuple(IconMenuName.CARGO_AIRLIFT,Icon.CARGO_AIRLIFT),
        IconTuple(IconMenuName.CARGO_AIRLIFT_LIGHT,Icon.CARGO_AIRLIFT_LIGHT),
        IconTuple(IconMenuName.CARGO_AIRLIFT_MEDIUM,Icon.CARGO_AIRLIFT_MEDIUM),
        IconTuple(IconMenuName.CARGO_AIRLIFT_HEAVY,Icon.CARGO_AIRLIFT_HEAVY),
        IconTuple(IconMenuName.ECM,Icon.ECM),
        IconTuple(IconMenuName.MEDEVAC,Icon.MEDEVAC),
        IconTuple(IconMenuName.RECONNAISSANCE,Icon.RECONNAISSANCE),
        IconTuple(IconMenuName.AIR_EARLY_WARN,Icon.AIR_EARLY_WARN),
        IconTuple(IconMenuName.ELECTRONIC_SURVEILLANCE_MES,Icon.ELECTRONIC_SURVEILLANCE_MES),
        IconTuple(IconMenuName.PHOTOGRAPHIC,Icon.PHOTOGRAPHIC),
        IconTuple(IconMenuName.PATROL,Icon.PATROL),
        IconTuple(IconMenuName.ANTI_SURFACE_WARFARE,Icon.ANTI_SURFACE_WARFARE),
        IconTuple(IconMenuName.MINE_COUNTERMEASURES,Icon.MINE_COUNTERMEASURES),
        IconTuple(IconMenuName.UTILITY,Icon.UTILITY),
        IconTuple(IconMenuName.UTILITY_LIGHT,Icon.UTILITY_LIGHT),
        IconTuple(IconMenuName.UTILITY_MEDIUM,Icon.UTILITY_MEDIUM),
        IconTuple(IconMenuName.UTILITY_HEAVY,Icon.UTILITY_HEAVY),
        IconTuple(IconMenuName.COMMUNICATIONS,Icon.COMMUNICATIONS),
        IconTuple(IconMenuName.CSAR,Icon.CSAR),
        IconTuple(IconMenuName.AIRBORNE_COMMAND_POST,Icon.AIRBORNE_COMMAND_POST),
        IconTuple(IconMenuName.ASW_CARRIER_BASED,Icon.ASW_CARRIER_BASED),
        IconTuple(IconMenuName.SOF,Icon.SOF),
        IconTuple(IconMenuName.LIGHTER_THEN_AIR,Icon.LIGHTER_THEN_AIR),
    ))


    private fun airDrone() = createBottomList(listOf(
        IconTuple(IconMenuName.DRONE__UAV,Icon.DRONE__UAV),
        IconTuple(IconMenuName.DRONE_ATTACK,Icon.DRONE_ATTACK),
        IconTuple(IconMenuName.DRONE_BOMBER,Icon.DRONE_BOMBER),
        IconTuple(IconMenuName.DRONE_CARGO,Icon.DRONE_CARGO),
        IconTuple(IconMenuName.DRONE_COMMAND_POST,Icon.DRONE_COMMAND_POST),
        IconTuple(IconMenuName.DRONE_FIGHTER,Icon.DRONE_FIGHTER),
        IconTuple(IconMenuName.DRONE_CSAR,Icon.DRONE_CSAR),
        IconTuple(IconMenuName.DRONE_ELECTRONIC_COUNTERMEASURES,Icon.DRONE_ELECTRONIC_COUNTERMEASURES),
        IconTuple(IconMenuName.DRONE_TANKER,Icon.DRONE_TANKER),
        IconTuple(IconMenuName.DRONE_VSTOL,Icon.DRONE_VSTOL),
        IconTuple(IconMenuName.DRONE_SOF,Icon.DRONE_SOF),
        IconTuple(IconMenuName.DRONE_MINE_COUNTERMEASURES,Icon.DRONE_MINE_COUNTERMEASURES),
        IconTuple(IconMenuName.DRONE_ASUW,Icon.DRONE_ASUW),
        IconTuple(IconMenuName.DRONE_PATROL,Icon.DRONE_PATROL),
        IconTuple(IconMenuName.DRONE_RECONNAISSANCE,Icon.DRONE_RECONNAISSANCE),
        IconTuple(IconMenuName.DRONE_EARLY_WARINNG,Icon.DRONE_EARLY_WARINNG),
        IconTuple(IconMenuName.DRONE_ELECTRONIC_SURVEILLANCE_MEASURES,Icon.DRONE_ELECTRONIC_SURVEILLANCE_MEASURES),
        IconTuple(IconMenuName.DRONE_PHOTOGRAPHIC,Icon.DRONE_PHOTOGRAPHIC),
        IconTuple(IconMenuName.DRONE_ASW,Icon.DRONE_ASW),
        IconTuple(IconMenuName.DRONE_TRAINER,Icon.DRONE_TRAINER),
        IconTuple(IconMenuName.DRONE_UTILITY,Icon.DRONE_UTILITY),
        IconTuple(IconMenuName.DRONE_COMMUNICATIONS,Icon.DRONE_COMMUNICATIONS),
        IconTuple(IconMenuName.DRONE_MEDEVAC,Icon.DRONE_MEDEVAC),
    ))
    private fun airRotaryWing() = createBottomList(listOf(
        IconTuple(IconMenuName.ROTARY_WING,Icon.ROTARY_WING),
        IconTuple(IconMenuName.ROTARY_W_ATTACK,Icon.ROTARY_W_ATTACK),
        IconTuple(IconMenuName.ANTI_SUBMARINE_WARFARE,Icon.ANTI_SUBMARINE_WARFARE),
        IconTuple(IconMenuName.RW_UTILITY,Icon.RW_UTILITY),
        IconTuple(IconMenuName.RW_UTILITY_LIGHT,Icon.RW_UTILITY_LIGHT),
        IconTuple(IconMenuName.RW_UTILITY_MEDIUM,Icon.RW_UTILITY_MEDIUM),
        IconTuple(IconMenuName.RW_UTILITY_HEAVY,Icon.RW_UTILITY_HEAVY),
        IconTuple(IconMenuName.RW_MINE_COUNTERMEASURES,Icon.RW_MINE_COUNTERMEASURES),
        IconTuple(IconMenuName.RW_CSAR,Icon.RW_CSAR),
        IconTuple(IconMenuName.RW_RECON,Icon.RW_RECON),
        IconTuple(IconMenuName.RW_DRONE__UAV,Icon.RW_DRONE__UAV),
        IconTuple(IconMenuName.RW_CARGO_AIRLIFT,Icon.RW_CARGO_AIRLIFT),
        IconTuple(IconMenuName.RW_CARGO_LIGHT,Icon.RW_CARGO_LIGHT),
        IconTuple(IconMenuName.RW_CARGO_MEDIUM,Icon.RW_CARGO_MEDIUM),
        IconTuple(IconMenuName.RW_CARGO_HEAVY,Icon.RW_CARGO_HEAVY),
        IconTuple(IconMenuName.RW_MEDEVAC,Icon.RW_MEDEVAC),
        IconTuple(IconMenuName.RW_SOF,Icon.RW_SOF),
        IconTuple(IconMenuName.RW_AIRBONE_COMMAND,Icon.RW_AIRBONE_COMMAND),
        IconTuple(IconMenuName.RW_TANKER,Icon.RW_TANKER),
        IconTuple(IconMenuName.RW_ECM,Icon.RW_ECM),

        ))

    private fun airMissiles() = createBottomList(listOf(
        IconTuple(IconMenuName.MISSILE_IN_FLIGHT,Icon.MISSILE_IN_FLIGHT),
        IconTuple(IconMenuName.SURFACE_LAUNCHED_MISSILE,Icon.SURFACE_LAUNCHED_MISSILE),
        IconTuple(IconMenuName.SSM,Icon.SSM),
        IconTuple(IconMenuName.SAM,Icon.SAM),
        IconTuple(IconMenuName.SSSM,Icon.SSSM),
        IconTuple(IconMenuName.ABM,Icon.ABM),
        IconTuple(IconMenuName.AIR_LAUNCHED_MISSILE,Icon.AIR_LAUNCHED_MISSILE),
        IconTuple(IconMenuName.ASM,Icon.ASM),
        IconTuple(IconMenuName.AAM,Icon.AAM),
        IconTuple(IconMenuName.AIR_2_SPACE_MISSILE,Icon.AIR_2_SPACE_MISSILE),
        IconTuple(IconMenuName.S_SSM,Icon.S_SSM),
        IconTuple(IconMenuName.CRUISE_MISSILE,Icon.CRUISE_MISSILE),
        IconTuple(IconMenuName.BALLISTIC_MISSILE,Icon.BALLISTIC_MISSILE),
        IconTuple(IconMenuName.BOMB,Icon.BOMB),
        IconTuple(IconMenuName.DECOY,Icon.DECOY)

        ))
    private fun airCiv() = createBottomList(listOf(
        IconTuple(IconMenuName.CIVIL_AIRCRAFT,Icon.CIVIL_AIRCRAFT),
        IconTuple(IconMenuName.C_FW,Icon.C_FW),
        IconTuple(IconMenuName.C_RW,Icon.C_RW),
        IconTuple(IconMenuName.C_LIGHTER_THEN_AIR,Icon.C_LIGHTER_THEN_AIR)
    ))

    private fun seaSurfLvl1() = listOf(
        OpenableMenuItem("-", "Sea Surface Track",null, IconTuple(IconMenuName.SEA_SURFACE_TRACK,Icon.SEA_SURFACE_TRACK)){vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.SEA_SURFACE_TRACK,Icon.SEA_SURFACE_TRACK),context)
        },
        OpenableMenuItem("SSCL","Combatant line",null,IconTuple(IconMenuName.COMBATANT,Icon.COMBATANT)){vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.COMBATANT,Icon.COMBATANT),context)
        },
        OpenableMenuItem("SSCO","Combatant other",null,IconTuple(IconMenuName.AMPHIBIOUS_WARFARE_SHIP,Icon.AMPHIBIOUS_WARFARE_SHIP)){vm,context->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.AMPHIBIOUS_WARFARE_SHIP,Icon.AMPHIBIOUS_WARFARE_SHIP),context)
        },
        OpenableMenuItem("SSNC","Non combatant",null,IconTuple(IconMenuName.NONCOMBATANT,Icon.NONCOMBATANT)){vm, ctx ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.NONCOMBATANT,Icon.NONCOMBATANT),ctx)
        },
        OpenableMenuItem("SSNM","Non-military",null,IconTuple(IconMenuName.NONMILITARY,Icon.NONMILITARY)){vm, ctx->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.NONMILITARY,Icon.NONMILITARY),ctx)
        }
    )

    private fun seaSurfSSNMLvl2() = createBottomList(listOf(
        IconTuple(IconMenuName.NONMILITARY,Icon.NONMILITARY),
        IconTuple(IconMenuName.MERCHANT,Icon.MERCHANT),
        IconTuple(IconMenuName.CARGO,Icon.CARGO),
        IconTuple(IconMenuName.ROLL_ON_ROLL_OFF,Icon.ROLL_ON_ROLL_OFF),
        IconTuple(IconMenuName.OILER_TANKER,Icon.OILER_TANKER),
        IconTuple(IconMenuName.TUG,Icon.TUG),
        IconTuple(IconMenuName.FERRY,Icon.FERRY),
        IconTuple(IconMenuName.PASSENGER,Icon.PASSENGER),
        IconTuple(IconMenuName.HAZARDOUS_MATERIALS_HAZMAT,Icon.HAZARDOUS_MATERIALS_HAZMAT),
        IconTuple(IconMenuName.TOWING_VESSEL,Icon.TOWING_VESSEL),
        IconTuple(IconMenuName.FISHING,Icon.FISHING),
        IconTuple(IconMenuName.DRIFTER,Icon.DRIFTER),
        IconTuple(IconMenuName.DREDGE,Icon.DREDGE),
        IconTuple(IconMenuName.TRAWLER,Icon.TRAWLER),
        IconTuple(IconMenuName.LEISURE_CRAFT,Icon.LEISURE_CRAFT),
        IconTuple(IconMenuName.LAW_ENFORCEMENT_VESSEL,Icon.LAW_ENFORCEMENT_VESSEL),
        IconTuple(IconMenuName.NON_MILITARY_HOVERCRAFT,Icon.NON_MILITARY_HOVERCRAFT),
        IconTuple(IconMenuName.OWN_TRACK,Icon.OWN_TRACK)
    ))

    private fun seaSurfSSNCLvl2() = createBottomList(listOf(
        IconTuple(IconMenuName.NONCOMBATANT,Icon.NONCOMBATANT),
        IconTuple(IconMenuName.UNDERWAY_REPLENISHMENT,Icon.UNDERWAY_REPLENISHMENT),
        IconTuple(IconMenuName.FLEET_SUPPORT,Icon.FLEET_SUPPORT),
        IconTuple(IconMenuName.INTELLIGENCE,Icon.INTELLIGENCE),
        IconTuple(IconMenuName.SERVICE__SUPPORT_HARBOR,Icon.SERVICE__SUPPORT_HARBOR),
        IconTuple(IconMenuName.HOSPITAL_SHIP,Icon.HOSPITAL_SHIP),
        IconTuple(IconMenuName.NONCOMBATANT_HOVERCRAFT,Icon.NONCOMBATANT_HOVERCRAFT),
        IconTuple(IconMenuName.NONCOMBATANT_STATION,Icon.NONCOMBATANT_STATION),
        IconTuple(IconMenuName.RESCUE,Icon.RESCUE),
    ))

    private fun seaSurfSSCLLvl2() = createBottomList(listOf(
        IconTuple(IconMenuName.SEA_SURFACE_TRACK,Icon.SEA_SURFACE_TRACK),
        IconTuple(IconMenuName.COMBATANT,Icon.COMBATANT),
        IconTuple(IconMenuName.LINE,Icon.LINE),
        IconTuple(IconMenuName.CARRIER,Icon.CARRIER),
        IconTuple(IconMenuName.BATTLESHIP,Icon.BATTLESHIP),
        IconTuple(IconMenuName.CRUISER,Icon.CRUISER),
        IconTuple(IconMenuName.DESTROYER,Icon.DESTROYER),
        IconTuple(IconMenuName.FRIGATE_CORVETTE,Icon.FRIGATE_CORVETTE),
        IconTuple(IconMenuName.LITTORAL_COMBATANT,Icon.LITTORAL_COMBATANT),
        IconTuple(IconMenuName.ANTISUBMARINE_WARFARE_MISSION_PACKAGE,Icon.ANTISUBMARINE_WARFARE_MISSION_PACKAGE),
        IconTuple(IconMenuName.MINE_WARFARE_MISSION_PACKAGE,Icon.MINE_WARFARE_MISSION_PACKAGE),
        IconTuple(IconMenuName.SURFACE_WARFARE_SUW_MISSION_PACKAGE,Icon.SURFACE_WARFARE_SUW_MISSION_PACKAGE),
    ))

    private fun seaSurfSSCOLvl2() = createBottomList(listOf(
        IconTuple(IconMenuName.AMPHIBIOUS_WARFARE_SHIP,Icon.AMPHIBIOUS_WARFARE_SHIP),
        IconTuple(IconMenuName.ASSAULT_VESSEL,Icon.ASSAULT_VESSEL),
        IconTuple(IconMenuName.LANDING_SHIP,Icon.LANDING_SHIP),
        IconTuple(IconMenuName.LANDING_SHIP_MEDIUM,Icon.LANDING_SHIP_MEDIUM),
        IconTuple(IconMenuName.LANDING_SHIP_TANK,Icon.LANDING_SHIP_TANK),
        IconTuple(IconMenuName.LANDING_CRAFT,Icon.LANDING_CRAFT),
        IconTuple(IconMenuName.MINE_WARFARE_VESSEL,Icon.MINE_WARFARE_VESSEL),
        IconTuple(IconMenuName.MINELAYER,Icon.MINELAYER),
        IconTuple(IconMenuName.MINESWEEPER,Icon.MINESWEEPER),
        IconTuple(IconMenuName.MINEHUNTER,Icon.MINEHUNTER),
        IconTuple(IconMenuName.MCM_SUPPORT,Icon.MCM_SUPPORT),
        IconTuple(IconMenuName.SEA_SURF_PATROL,Icon.SEA_SURF_PATROL),
        IconTuple(IconMenuName.ANTISUBMARINE_WARFARE,Icon.ANTISUBMARINE_WARFARE),
        IconTuple(IconMenuName.ANTISURFACE_WARFARE,Icon.ANTISURFACE_WARFARE),
        IconTuple(IconMenuName.ANTISHIP_MISSILE_PATROL_CRAFT,Icon.ANTISHIP_MISSILE_PATROL_CRAFT),
        IconTuple(IconMenuName.TORPEDO_PATROL_CRAFT,Icon.TORPEDO_PATROL_CRAFT),
        IconTuple(IconMenuName.GUN_PATROL_CRAFT,Icon.GUN_PATROL_CRAFT),
        IconTuple(IconMenuName.HOVERCRAFT,Icon.HOVERCRAFT),
        IconTuple(IconMenuName.STATION,Icon.STATION),
        IconTuple(IconMenuName.PICKET,Icon.PICKET),
        IconTuple(IconMenuName.ASW_SHIP,Icon.ASW_SHIP),
        IconTuple(IconMenuName.NAVY_GROUP,Icon.NAVY_GROUP),
        IconTuple(IconMenuName.NAVY_TASK_FORCE,Icon.NAVY_TASK_FORCE),
        IconTuple(IconMenuName.NAVY_TASK_GROUP,Icon.NAVY_TASK_GROUP),
        IconTuple(IconMenuName.NAVY_TASK_UNIT,Icon.NAVY_TASK_UNIT),
        IconTuple(IconMenuName.CONVOY,Icon.CONVOY),
        IconTuple(IconMenuName.SURFACE_DECOY,Icon.SURFACE_DECOY),
        IconTuple(IconMenuName.UNMANNED_SURFACE_VEHICLE,Icon.UNMANNED_SURFACE_VEHICLE),
        IconTuple(IconMenuName.MINE_COUNTERMEASURES_SURFACE_DRONE,Icon.MINE_COUNTERMEASURES_SURFACE_DRONE),
        IconTuple(IconMenuName.ANTISUBMARINE_WARFARE_SURFACE_DRONE,Icon.ANTISUBMARINE_WARFARE_SURFACE_DRONE),
        IconTuple(IconMenuName.ANTISURFACE_WARFARE_SURFACE_DRONE,Icon.ANTISURFACE_WARFARE_SURFACE_DRONE),
    ))


    private fun groundLvl1() = listOf(
        OpenableMenuItem("-","Ground Track",null, IconTuple(IconMenuName.GROUND_TRACK,Icon.GROUND_TRACK)){ vm, context -> vm.finishCreatingSymbol(
            IconTuple(IconMenuName.GROUND_TRACK,Icon.GROUND_TRACK),context)},
        OpenableMenuItem("GLU", "Ground Unit", null,null),
        OpenableMenuItem("GLE","Ground Equipment", null, IconTuple(IconMenuName.GROUND_TRACK_EQUIPMENT,Icon.GROUND_TRACK_EQUIPMENT)){ vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.GROUND_TRACK_EQUIPMENT,Icon.GROUND_TRACK_EQUIPMENT),context)
        },
        OpenableMenuItem("GLI", "Ground Installations", null,IconTuple(IconMenuName.INSTALLATION,Icon.INSTALLATION)){vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.INSTALLATION,Icon.INSTALLATION),context)
        },
    )

    private fun groundInstalationLvl2() = createBottomList(listOf(
        IconTuple(IconMenuName.INSTALLATION,Icon.INSTALLATION),
        IconTuple(IconMenuName.RAW_MATERIAL_PRODUCTION_STORAGE,Icon.RAW_MATERIAL_PRODUCTION_STORAGE),
        IconTuple(IconMenuName.MINE,Icon.MINE),
        IconTuple(IconMenuName.PETROLEUM_GAS_OIL,Icon.PETROLEUM_GAS_OIL),
        IconTuple(IconMenuName.NBC,Icon.NBC),
        IconTuple(IconMenuName.NBC_BIOLOGICAL,Icon.NBC_BIOLOGICAL),
        IconTuple(IconMenuName.NBC_CHEMICAL,Icon.NBC_CHEMICAL),
        IconTuple(IconMenuName.NBC_NUCLEAR,Icon.NBC_NUCLEAR),
        IconTuple(IconMenuName.PROCESSING_FACILITY,Icon.PROCESSING_FACILITY),
        IconTuple(IconMenuName.PROCESSING_FACILITY_DECONTAMINATION,Icon.PROCESSING_FACILITY_DECONTAMINATION),
        IconTuple(IconMenuName.EQUIPMENT_MANUFACTURE,Icon.EQUIPMENT_MANUFACTURE),
        IconTuple(IconMenuName.SERVICE_RESEARCH_UTILITY_FACILITY,Icon.SERVICE_RESEARCH_UTILITY_FACILITY),
        IconTuple(IconMenuName.TECHNOLOGICAL_RESEARCH_FACILITY,Icon.TECHNOLOGICAL_RESEARCH_FACILITY),
        IconTuple(IconMenuName.TELECOMMUNICATIONS_FACILITY,Icon.TELECOMMUNICATIONS_FACILITY),
        IconTuple(IconMenuName.ELECTRIC_POWER_FACILITY,Icon.ELECTRIC_POWER_FACILITY),
        IconTuple(IconMenuName.NUCLEAR_PLANT,Icon.NUCLEAR_PLANT),
        IconTuple(IconMenuName.DAM,Icon.DAM),
        IconTuple(IconMenuName.FOSSIL_FUEL,Icon.FOSSIL_FUEL),
        IconTuple(IconMenuName.PUBLIC_WATER_SERVICES,Icon.PUBLIC_WATER_SERVICES),
        IconTuple(IconMenuName.MILITARY_MATERIEL_FACILITY,Icon.MILITARY_MATERIEL_FACILITY),
        IconTuple(IconMenuName.NUCLEAR_ENERGY,Icon.NUCLEAR_ENERGY),
        IconTuple(IconMenuName.ATOMIC_ENERGY_REACTOR,Icon.ATOMIC_ENERGY_REACTOR),
        IconTuple(IconMenuName.NUCLEAR_MATERIAL_PRODUCTION,Icon.NUCLEAR_MATERIAL_PRODUCTION),
        IconTuple(IconMenuName.WEAPONS_GRADE,Icon.WEAPONS_GRADE),
        IconTuple(IconMenuName.NUCLEAR_MATERIAL_STORAGE,Icon.NUCLEAR_MATERIAL_STORAGE),
        IconTuple(IconMenuName.AIRCRAFT_PRODUCTION_ASSEMBLY,Icon.AIRCRAFT_PRODUCTION_ASSEMBLY),
        IconTuple(IconMenuName.AMMUNITION_AND_EXPLOSIVES_PRODUCTION,Icon.AMMUNITION_AND_EXPLOSIVES_PRODUCTION),
        IconTuple(IconMenuName.ARMAMENT_PRODUCTION,Icon.ARMAMENT_PRODUCTION),
        IconTuple(IconMenuName.MILITARY_VEHICLE_PRODUCTION,Icon.MILITARY_VEHICLE_PRODUCTION),
        IconTuple(IconMenuName.ENGINEERING_EQUIPMENT_PRODUCTION,Icon.ENGINEERING_EQUIPMENT_PRODUCTION),
        IconTuple(IconMenuName.ENGINEERING_EQUIPMENT_PRODUCTION_BRIDGE,Icon.ENGINEERING_EQUIPMENT_PRODUCTION_BRIDGE),
        IconTuple(IconMenuName.CHEMICAL_BIOLOGICAL_WARFARE_PRODUCTION,Icon.CHEMICAL_BIOLOGICAL_WARFARE_PRODUCTION),
        IconTuple(IconMenuName.SHIP_CONSTRUCTION,Icon.SHIP_CONSTRUCTION),
        IconTuple(IconMenuName.MISSILE_SPACE_SYSTEM_PRODUCTION,Icon.MISSILE_SPACE_SYSTEM_PRODUCTION),
        IconTuple(IconMenuName.GOVERNMENT_LEADERSHIP,Icon.GOVERNMENT_LEADERSHIP),
        IconTuple(IconMenuName.MILITARY_BASE_FACILITY,Icon.MILITARY_BASE_FACILITY),
        IconTuple(IconMenuName.AIRPORT_AIRBASE,Icon.AIRPORT_AIRBASE),
        IconTuple(IconMenuName.SEAPORT_NAVAL_BASE,Icon.SEAPORT_NAVAL_BASE),
        IconTuple(IconMenuName.TRANSPORT_FACILITY,Icon.TRANSPORT_FACILITY),
        IconTuple(IconMenuName.MEDICAL_FACILITY,Icon.MEDICAL_FACILITY),
        IconTuple(IconMenuName.HOSPITAL,Icon.HOSPITAL),
    ))



    private fun groundEquipmentLvl2() = listOf(
        OpenableMenuItem("EW","Weapon",null, IconTuple(IconMenuName.WEAPON,Icon.WEAPON)){vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.WEAPON,Icon.WEAPON),context)
        },
        OpenableMenuItem("EWML","Missile and rocket Launchers",null, IconTuple(IconMenuName.MISSILE_LAUNCHER,Icon.MISSILE_LAUNCHER)){vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.MISSILE_LAUNCHER,Icon.MISSILE_LAUNCHER),context)
        },
        OpenableMenuItem("EWRG","Rifles and grenade launchers",null, IconTuple(IconMenuName.RIFLE,Icon.RIFLE)){vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.RIFLE,Icon.RIFLE),context)
        },
        OpenableMenuItem("EWMH","Mortars, Howitzers, AT, AD, Direct fire", null, IconTuple(IconMenuName.MORTAR,Icon.MORTAR)){vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.MORTAR,Icon.MORTAR),context)
        },
        OpenableMenuItem("EV","Vehicle",null, IconTuple(IconMenuName.GROUND_VEHICLE,Icon.GROUND_VEHICLE)){vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.GROUND_VEHICLE,Icon.GROUND_VEHICLE),context)
        },
        //TODO to armored vehicle add missile support vehicles
        OpenableMenuItem("EVA", "Armored vehicle", null, IconTuple(IconMenuName.ARMORED_VEHICLE,Icon.ARMORED_VEHICLE)){ vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.ARMORED_VEHICLE,Icon.ARMORED_VEHICLE),context)
        },
        OpenableMenuItem("EVUE","Utility and Engineer vehicle", null, IconTuple(IconMenuName.UTILITY_VEHICLE,Icon.UTILITY_VEHICLE)){vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.UTILITY_VEHICLE,Icon.UTILITY_VEHICLE),context)
        },
        OpenableMenuItem("EVC","Civilian Vehicle",null, IconTuple(IconMenuName.CIVILIAN_VEHICLE,Icon.CIVILIAN_VEHICLE)){vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.CIVILIAN_VEHICLE,Icon.CIVILIAN_VEHICLE),context)
        },
        OpenableMenuItem("ESE","Special Equipment",null, IconTuple(IconMenuName.SPECIAL_EQUIPMENT,Icon.SPECIAL_EQUIPMENT)){ vm, context ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.SPECIAL_EQUIPMENT,Icon.SPECIAL_EQUIPMENT),context)
        }
    )

    private fun groundEquipmentESELvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.EQUIPMENT_SENSOR,Icon.EQUIPMENT_SENSOR),
        IconTuple(IconMenuName.RADAR,Icon.RADAR),
        IconTuple(IconMenuName.EMPLACED_SENSOR,Icon.EMPLACED_SENSOR),
        IconTuple(IconMenuName.SPECIAL_EQUIPMENT,Icon.SPECIAL_EQUIPMENT),
        IconTuple(IconMenuName.LASER,Icon.LASER),
        IconTuple(IconMenuName.NBC_EQUIPMENT,Icon.NBC_EQUIPMENT),
        IconTuple(IconMenuName.FLAME_THROWER,Icon.FLAME_THROWER),
        IconTuple(IconMenuName.LAND_MINES,Icon.LAND_MINES),
        IconTuple(IconMenuName.CLAYMORE,Icon.CLAYMORE),
        IconTuple(IconMenuName.LESS_THAN_LETHAL,Icon.LESS_THAN_LETHAL),
    ))


    private fun groundEquipmentEVCLvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.CIVILIAN_VEHICLE,Icon.CIVILIAN_VEHICLE),
        IconTuple(IconMenuName.AUTOMOBILE,Icon.AUTOMOBILE),
        IconTuple(IconMenuName.COMPACT_AUTOMOBILE,Icon.COMPACT_AUTOMOBILE),
        IconTuple(IconMenuName.MIDSIZE_AUTOMOBILE,Icon.MIDSIZE_AUTOMOBILE),
        IconTuple(IconMenuName.SEDAN_AUTOMOBILE,Icon.SEDAN_AUTOMOBILE),
        IconTuple(IconMenuName.OPENBED_TRUCK,Icon.OPENBED_TRUCK),
        IconTuple(IconMenuName.PICKUP_OPENBED_TRUCK,Icon.PICKUP_OPENBED_TRUCK),
        IconTuple(IconMenuName.SMALL_OPENBED_TRUCK,Icon.SMALL_OPENBED_TRUCK),
        IconTuple(IconMenuName.LARGE_OPENBED_TRUCK,Icon.LARGE_OPENBED_TRUCK),
        IconTuple(IconMenuName.MULTIPASSENGER_VEHICLE,Icon.MULTIPASSENGER_VEHICLE),
        IconTuple(IconMenuName.VAN_MULTIPASSENGER_VEHICLE,Icon.VAN_MULTIPASSENGER_VEHICLE),
        IconTuple(IconMenuName.SMALL_BUS_MULTIPASSENGER_VEHICLE,Icon.SMALL_BUS_MULTIPASSENGER_VEHICLE),
        IconTuple(IconMenuName.LARGE_BUS_MULTIPASSENGER_VEHICLE,Icon.LARGE_BUS_MULTIPASSENGER_VEHICLE),
        IconTuple(IconMenuName.CIVILIAN_UTILITY_VEHICLE,Icon.CIVILIAN_UTILITY_VEHICLE),
        IconTuple(IconMenuName.SPORT_UTILITY_VEHICLE_SUV_UTILITY_VEHICLE,Icon.SPORT_UTILITY_VEHICLE_SUV_UTILITY_VEHICLE),
        IconTuple(IconMenuName.SMALL_BOX_TRUCK_UTILITY_VEHICLE,Icon.SMALL_BOX_TRUCK_UTILITY_VEHICLE),
        IconTuple(IconMenuName.LARGE_BOX_TRUCK_UTILITY_VEHICLE,Icon.LARGE_BOX_TRUCK_UTILITY_VEHICLE),
        IconTuple(IconMenuName.JEEP_TYPE_VEHICLE,Icon.JEEP_TYPE_VEHICLE),
        IconTuple(IconMenuName.SMALL_LIGHT_JEEP_TYPE_VEHICLE,Icon.SMALL_LIGHT_JEEP_TYPE_VEHICLE),
        IconTuple(IconMenuName.MEDIUM_JEEP_TYPE_VEHICLE,Icon.MEDIUM_JEEP_TYPE_VEHICLE),
        IconTuple(IconMenuName.LARGE_HEAVY_JEEP_TYPE_VEHICLE,Icon.LARGE_HEAVY_JEEP_TYPE_VEHICLE),
        IconTuple(IconMenuName.TRACTOR_TRAILER_TRUCK_WITH_BOX_TRAILER,Icon.TRACTOR_TRAILER_TRUCK_WITH_BOX_TRAILER),
        IconTuple(IconMenuName.SMALL_LIGHT_BOX_TRAILER_TRACTOR_TRAILER_TRUCK,Icon.SMALL_LIGHT_BOX_TRAILER_TRACTOR_TRAILER_TRUCK),
        IconTuple(IconMenuName.MEDIUM_BOX_TRAILER_TRACTOR_TRAILER_TRUCK,Icon.MEDIUM_BOX_TRAILER_TRACTOR_TRAILER_TRUCK),
        IconTuple(IconMenuName.LARGE_HEAVY_BOX_TRAILER_TRACTOR_TRAILER_TRUCK,Icon.LARGE_HEAVY_BOX_TRAILER_TRACTOR_TRAILER_TRUCK),
        IconTuple(IconMenuName.TRACTOR_TRAILER_TRUCK_WITH_FLATBED_TRAILER,Icon.TRACTOR_TRAILER_TRUCK_WITH_FLATBED_TRAILER),
        IconTuple(IconMenuName.SMALL_LIGHT_FLATBED_TRAILER_TRACTOR_TRAILER_TRUCK,Icon.SMALL_LIGHT_FLATBED_TRAILER_TRACTOR_TRAILER_TRUCK),
        IconTuple(IconMenuName.MEDIUM_FLATBED_TRAILER_TRACTOR_TRAILER_TRUCK,Icon.MEDIUM_FLATBED_TRAILER_TRACTOR_TRAILER_TRUCK),
        IconTuple(IconMenuName.LARGE_HEAVY_FLATBED_TRAILER_TRACTOR_TRAILER_TRUCK,Icon.LARGE_HEAVY_FLATBED_TRAILER_TRACTOR_TRAILER_TRUCK),
        IconTuple(IconMenuName.PACK_ANIMALS,Icon.PACK_ANIMALS),
    ))

    private fun groundEquipmentEVUELvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.UTILITY_VEHICLE,Icon.UTILITY_VEHICLE),
        IconTuple(IconMenuName.BUS,Icon.BUS),
        IconTuple(IconMenuName.SEMI,Icon.SEMI),
        IconTuple(IconMenuName.SEMI_LIGHT,Icon.SEMI_LIGHT),
        IconTuple(IconMenuName.SEMI_MEDIUM,Icon.SEMI_MEDIUM),
        IconTuple(IconMenuName.SEMI_HEAVY,Icon.SEMI_HEAVY),
        IconTuple(IconMenuName.LIMITED_CROSSCOUNTRY_TRUCK,Icon.LIMITED_CROSSCOUNTRY_TRUCK),
        IconTuple(IconMenuName.CROSSCOUNTRY_TRUCK,Icon.CROSSCOUNTRY_TRUCK),
        IconTuple(IconMenuName.WATER_CRAFT,Icon.WATER_CRAFT),
        IconTuple(IconMenuName.TOW_TRUCK,Icon.TOW_TRUCK),
        IconTuple(IconMenuName.TOW_TRUCK_LIGHT,Icon.TOW_TRUCK_LIGHT),
        IconTuple(IconMenuName.TOW_TRUCK_HEAVY,Icon.TOW_TRUCK_HEAVY),
        IconTuple(IconMenuName.AMBULANCE,Icon.AMBULANCE),
        IconTuple(IconMenuName.ARMORED_AMBULANCE,Icon.ARMORED_AMBULANCE),
        IconTuple(IconMenuName.ENGINEER_VEHICLE,Icon.ENGINEER_VEHICLE),
        IconTuple(IconMenuName.BRIDGE,Icon.BRIDGE),
        IconTuple(IconMenuName.EARTHMOVER,Icon.EARTHMOVER),
        IconTuple(IconMenuName.CONSTRUCTION_VEHICLE,Icon.CONSTRUCTION_VEHICLE),
        IconTuple(IconMenuName.MINE_LAYING_VEHICLE,Icon.MINE_LAYING_VEHICLE),
        IconTuple(IconMenuName.ARMORED_CARRIER_WITH_VOLCANO,Icon.ARMORED_CARRIER_WITH_VOLCANO),
        IconTuple(IconMenuName.TRUCK_MOUNTED_WITH_VOLCANO,Icon.TRUCK_MOUNTED_WITH_VOLCANO),
        IconTuple(IconMenuName.MINE_CLEARING_VEHICLE,Icon.MINE_CLEARING_VEHICLE),
        IconTuple(IconMenuName.ARMORED_MOUNTED_MINE_CLEARING_VEHICLE,Icon.ARMORED_MOUNTED_MINE_CLEARING_VEHICLE),
        IconTuple(IconMenuName.TRAILER_MOUNTED_MINE_CLEARING_VEHICLE,Icon.TRAILER_MOUNTED_MINE_CLEARING_VEHICLE),
        IconTuple(IconMenuName.DOZER,Icon.DOZER),
        IconTuple(IconMenuName.ARMORED_DOZER,Icon.ARMORED_DOZER),
        IconTuple(IconMenuName.ARMORED_ASSAULT,Icon.ARMORED_ASSAULT),
        IconTuple(IconMenuName.ARMORED_ENGINEER_RECON_VEHICLE_AERV,Icon.ARMORED_ENGINEER_RECON_VEHICLE_AERV),
        IconTuple(IconMenuName.BACKHOE,Icon.BACKHOE),
        IconTuple(IconMenuName.FERRY_TRANSPORTER,Icon.FERRY_TRANSPORTER),
        IconTuple(IconMenuName.TRAIN_LOCOMOTIVE,Icon.TRAIN_LOCOMOTIVE),
    ))

    private fun groundEquipmentEVALvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.ARMORED_VEHICLE,Icon.ARMORED_VEHICLE),
        IconTuple(IconMenuName.TANK,Icon.TANK),
        IconTuple(IconMenuName.TANK_LIGHT,Icon.TANK_LIGHT),
        IconTuple(IconMenuName.TANK_LIGHT_RECOVERY,Icon.TANK_LIGHT_RECOVERY),
        IconTuple(IconMenuName.TANK_MEDIUM,Icon.TANK_MEDIUM),
        IconTuple(IconMenuName.TANK_MEDIUM_RECOVERY,Icon.TANK_MEDIUM_RECOVERY),
        IconTuple(IconMenuName.TANK_HEAVY,Icon.TANK_HEAVY),
        IconTuple(IconMenuName.TANK_HEAVY_RECOVERY,Icon.TANK_HEAVY_RECOVERY),
        IconTuple(IconMenuName.ARMORED_PERSONNEL_CARRIER,Icon.ARMORED_PERSONNEL_CARRIER),
        IconTuple(IconMenuName.ARMORED_PERSONNEL_CARRIER_RECOVERY,Icon.ARMORED_PERSONNEL_CARRIER_RECOVERY),
        IconTuple(IconMenuName.ARMORED_INFANTRY,Icon.ARMORED_INFANTRY),
        IconTuple(IconMenuName.C2V_ACV,Icon.C2V_ACV),
        IconTuple(IconMenuName.COMBAT_SERVICE_SUPPORT_VEHICLE,Icon.COMBAT_SERVICE_SUPPORT_VEHICLE),
        IconTuple(IconMenuName.LIGHT_ARMORED_VEHICLE,Icon.LIGHT_ARMORED_VEHICLE),
        IconTuple(IconMenuName.MISSILE_SUPPORT_VEHICLE,Icon.MISSILE_SUPPORT_VEHICLE),
        IconTuple(IconMenuName.MISSILE_SUPPORT_VEHICLE_TRANSLOADER,Icon.MISSILE_SUPPORT_VEHICLE_TRANSLOADER),
        IconTuple(IconMenuName.MISSILE_SUPPORT_VEHICLE_TRANSPORTER,Icon.MISSILE_SUPPORT_VEHICLE_TRANSPORTER),
        IconTuple(IconMenuName.MISSILE_SUPPORT_VEHICLE_CRANE_LOADING_DEVICE,Icon.MISSILE_SUPPORT_VEHICLE_CRANE_LOADING_DEVICE),
        IconTuple(IconMenuName.MISSILE_SUPPORT_VEHICLE_PROPELLANT_TRANSPORTER,Icon.MISSILE_SUPPORT_VEHICLE_PROPELLANT_TRANSPORTER),
        IconTuple(IconMenuName.MISSILE_SUPPORT_VEHICLE_WARHEAD_TRANSPORTER,Icon.MISSILE_SUPPORT_VEHICLE_WARHEAD_TRANSPORTER),
    ))


    private fun groundEquipmentEWMHLvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.EQUIPMENT_MORTAR,Icon.EQUIPMENT_MORTAR),
        IconTuple(IconMenuName.EQUIPMENT_MORTAR_LIGHT,Icon.EQUIPMENT_MORTAR_LIGHT),
        IconTuple(IconMenuName.EQUIPMENT_MORTAR_MEDIUM,Icon.EQUIPMENT_MORTAR_MEDIUM),
        IconTuple(IconMenuName.EQUIPMENT_MORTAR_HEAVY,Icon.EQUIPMENT_MORTAR_HEAVY),
        IconTuple(IconMenuName.HOWITZER,Icon.HOWITZER),
        IconTuple(IconMenuName.HOWITZER_LIGHT,Icon.HOWITZER_LIGHT),
        IconTuple(IconMenuName.HOWITZER_LIGHT_SELFPROPELLED,Icon.HOWITZER_LIGHT_SELFPROPELLED),
        IconTuple(IconMenuName.HOWITZER_MEDIUM,Icon.HOWITZER_MEDIUM),
        IconTuple(IconMenuName.HOWITZER_MEDIUM_SELFPROPELLED,Icon.HOWITZER_MEDIUM_SELFPROPELLED),
        IconTuple(IconMenuName.HOWITZER_HEAVY,Icon.HOWITZER_HEAVY),
        IconTuple(IconMenuName.HOWITZER_HEAVY_SELFPROPELLED,Icon.HOWITZER_HEAVY_SELFPROPELLED),
        IconTuple(IconMenuName.ANTITANK_GUN,Icon.ANTITANK_GUN),
        IconTuple(IconMenuName.ANTITANK_GUN_LIGHT,Icon.ANTITANK_GUN_LIGHT),
        IconTuple(IconMenuName.ANTITANK_GUN_MEDIUM,Icon.ANTITANK_GUN_MEDIUM),
        IconTuple(IconMenuName.ANTITANK_GUN_HEAVY,Icon.ANTITANK_GUN_HEAVY),
        IconTuple(IconMenuName.ANTITANK_GUN_RECOILLESS,Icon.ANTITANK_GUN_RECOILLESS),
        IconTuple(IconMenuName.DIRECT_FIRE_GUN,Icon.DIRECT_FIRE_GUN),
        IconTuple(IconMenuName.DIRECT_FIRE_GUN_LIGHT,Icon.DIRECT_FIRE_GUN_LIGHT),
        IconTuple(IconMenuName.DIRECT_FIRE_GUN_LIGHT_SELFPROPELLED,Icon.DIRECT_FIRE_GUN_LIGHT_SELFPROPELLED),
        IconTuple(IconMenuName.DIRECT_FIRE_GUN_MEDIUM,Icon.DIRECT_FIRE_GUN_MEDIUM),
        IconTuple(IconMenuName.DIRECT_FIRE_GUN_MEDIUM_SELFPROPELLED,Icon.DIRECT_FIRE_GUN_MEDIUM_SELFPROPELLED),
        IconTuple(IconMenuName.DIRECT_FIRE_GUN_HEAVY,Icon.DIRECT_FIRE_GUN_HEAVY),
        IconTuple(IconMenuName.DIRECT_FIRE_GUN_HEAVY_SELFPROPELLED,Icon.DIRECT_FIRE_GUN_HEAVY_SELFPROPELLED),
        IconTuple(IconMenuName.AIR_DEFENSE_GUN,Icon.AIR_DEFENSE_GUN),
        IconTuple(IconMenuName.AIR_DEFENSE_GUN_LIGHT,Icon.AIR_DEFENSE_GUN_LIGHT),
        IconTuple(IconMenuName.AIR_DEFENSE_GUN_MEDIUM,Icon.AIR_DEFENSE_GUN_MEDIUM),
        IconTuple(IconMenuName.AIR_DEFENSE_GUN_HEAVY,Icon.AIR_DEFENSE_GUN_HEAVY),
    ))


    private fun groundEquipmentEWRGLvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.RIFLE_AUTOMATIC_WEAPON,Icon.RIFLE_AUTOMATIC_WEAPON),
        IconTuple(IconMenuName.RIFLE,Icon.RIFLE),
        IconTuple(IconMenuName.LIGHT_MACHINE_GUN,Icon.LIGHT_MACHINE_GUN),
        IconTuple(IconMenuName.HEAVY_MACHINE_GUN,Icon.HEAVY_MACHINE_GUN),
        IconTuple(IconMenuName.GRENADE_LAUNCHER,Icon.GRENADE_LAUNCHER),
        IconTuple(IconMenuName.GRENADE_LAUNCHER_LIGHT,Icon.GRENADE_LAUNCHER_LIGHT),
        IconTuple(IconMenuName.GRENADE_LAUNCHER_MEDIUM,Icon.GRENADE_LAUNCHER_MEDIUM),
        IconTuple(IconMenuName.GRENADE_LAUNCHER_HEAVY,Icon.GRENADE_LAUNCHER_HEAVY),
    ))


    private fun groundEquipmentEWMLLvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.MISSILE_LAUNCHER,Icon.MISSILE_LAUNCHER),
        IconTuple(IconMenuName.AIR_DEFENSE_AD_MISSILE_LAUNCHER,Icon.AIR_DEFENSE_AD_MISSILE_LAUNCHER),
        IconTuple(IconMenuName.SHORT_RANGE_AD_MISSILE_LAUNCHER,Icon.SHORT_RANGE_AD_MISSILE_LAUNCHER),
        IconTuple(IconMenuName.SHORT_RANGE_AD_TRANSPORTER_LAUNCHER_AND_RADAR_TLAR,Icon.SHORT_RANGE_AD_TRANSPORTER_LAUNCHER_AND_RADAR_TLAR),
        IconTuple(IconMenuName.SHORT_RANGE_AD_TRANSPORTER_ERECTOR_LAUNCHER_AND_RADAR_TELAR,Icon.SHORT_RANGE_AD_TRANSPORTER_ERECTOR_LAUNCHER_AND_RADAR_TELAR),
        IconTuple(IconMenuName.INTERMEDIATE_RANGE_AD_MISSILE_LAUNCHER,Icon.INTERMEDIATE_RANGE_AD_MISSILE_LAUNCHER),
        IconTuple(IconMenuName.INTERMEDIATE_RANGE_AD_TRANSPORTER_LAUNCHER_AND_RADAR_TLAR,Icon.INTERMEDIATE_RANGE_AD_TRANSPORTER_LAUNCHER_AND_RADAR_TLAR),
        IconTuple(IconMenuName.INTERMEDIATE_RANGE_AD_TRANSPORTER_ERECTOR_LAUNCHER_AND_RADAR_TELAR,Icon.INTERMEDIATE_RANGE_AD_TRANSPORTER_ERECTOR_LAUNCHER_AND_RADAR_TELAR),
        IconTuple(IconMenuName.LONG_RANGE_AD_MISSILE_LAUNCHER,Icon.LONG_RANGE_AD_MISSILE_LAUNCHER),
        IconTuple(IconMenuName.LONG_RANGE_AD_TRANSPORTER_LAUNCHER_AND_RADAR_TLAR,Icon.LONG_RANGE_AD_TRANSPORTER_LAUNCHER_AND_RADAR_TLAR),
        IconTuple(IconMenuName.LONG_RANGE_AD_TRANSPORTER_ERECTOR_LAUNCHER_AND_RADAR_TELAR,Icon.LONG_RANGE_AD_TRANSPORTER_ERECTOR_LAUNCHER_AND_RADAR_TELAR),
        IconTuple(IconMenuName.AD_MISSILE_LAUNCHER_THEATER,Icon.AD_MISSILE_LAUNCHER_THEATER),
        IconTuple(IconMenuName.AD_MISSILE_LAUNCHER_THEATER_TRANSPORTER_LAUNCHER_AND_RADAR_TLAR,Icon.AD_MISSILE_LAUNCHER_THEATER_TRANSPORTER_LAUNCHER_AND_RADAR_TLAR),
        IconTuple(IconMenuName.AD_MISSILE_LAUNCHER_THEATER_TRANSPORTER_ERECTOR_LAUNCHER_AND_RADAR_TELAR,Icon.AD_MISSILE_LAUNCHER_THEATER_TRANSPORTER_ERECTOR_LAUNCHER_AND_RADAR_TELAR),
        IconTuple(IconMenuName.SURFSURF_SS_MISSILE_LAUNCHER,Icon.SURFSURF_SS_MISSILE_LAUNCHER),
        IconTuple(IconMenuName.SHORT_RANGE_SS_MISSILE_LAUNCHER,Icon.SHORT_RANGE_SS_MISSILE_LAUNCHER),
        IconTuple(IconMenuName.INTERMEDIATE_RANGE_SS_MISSILE_LAUNCHER,Icon.INTERMEDIATE_RANGE_SS_MISSILE_LAUNCHER),
        IconTuple(IconMenuName.LONG_RANGE_SS_MISSILE_LAUNCHER,Icon.LONG_RANGE_SS_MISSILE_LAUNCHER),
        IconTuple(IconMenuName.MISSILE_LAUNCHER_ANTITANK_AT,Icon.MISSILE_LAUNCHER_ANTITANK_AT),
        IconTuple(IconMenuName.MISSILE_LAUNCHER_AT_LIGHT,Icon.MISSILE_LAUNCHER_AT_LIGHT),
        IconTuple(IconMenuName.MISSILE_LAUNCHER_AT_MEDIUM,Icon.MISSILE_LAUNCHER_AT_MEDIUM),
        IconTuple(IconMenuName.MISSILE_LAUNCHER_AT_HEAVY,Icon.MISSILE_LAUNCHER_AT_HEAVY),
        IconTuple(IconMenuName.EQUIPMENT_SINGLE_ROCKET_LAUNCHER,Icon.EQUIPMENT_SINGLE_ROCKET_LAUNCHER),
        IconTuple(IconMenuName.EQUIPMENT_SINGLE_ROCKET_LAUNCHER_LIGHT,Icon.EQUIPMENT_SINGLE_ROCKET_LAUNCHER_LIGHT),
        IconTuple(IconMenuName.EQUIPMENT_SINGLE_ROCKET_LAUNCHER_MEDIUM,Icon.EQUIPMENT_SINGLE_ROCKET_LAUNCHER_MEDIUM),
        IconTuple(IconMenuName.EQUIPMENT_SINGLE_ROCKET_LAUNCHER_HEAVY,Icon.EQUIPMENT_SINGLE_ROCKET_LAUNCHER_HEAVY),
        IconTuple(IconMenuName.MULTIPLE_ROCKET_LAUNCHER,Icon.MULTIPLE_ROCKET_LAUNCHER),
        IconTuple(IconMenuName.MULTIPLE_ROCKET_LAUNCHER_LIGHT,Icon.MULTIPLE_ROCKET_LAUNCHER_LIGHT),
        IconTuple(IconMenuName.MULTIPLE_ROCKET_LAUNCHER_MEDIUM,Icon.MULTIPLE_ROCKET_LAUNCHER_MEDIUM),
        IconTuple(IconMenuName.MULTIPLE_ROCKET_LAUNCHER_HEAVY,Icon.MULTIPLE_ROCKET_LAUNCHER_HEAVY),
        IconTuple(IconMenuName.ANTITANK_ROCKET_LAUNCHER,Icon.ANTITANK_ROCKET_LAUNCHER),
        IconTuple(IconMenuName.ANTITANK_ROCKET_LAUNCHER_LIGHT,Icon.ANTITANK_ROCKET_LAUNCHER_LIGHT),
        IconTuple(IconMenuName.ANTITANK_ROCKET_LAUNCHER_MEDIUM,Icon.ANTITANK_ROCKET_LAUNCHER_MEDIUM),
        IconTuple(IconMenuName.ANTITANK_ROCKET_LAUNCHER_HEAVY,Icon.ANTITANK_ROCKET_LAUNCHER_HEAVY),
    ))

    private inline fun groundUnitLvl2() = listOf(
        OpenableMenuItem("U","Unit", null, IconTuple(IconMenuName.UNIT,Icon.UNIT)){ vm, context-> vm.finishCreatingSymbol(IconTuple(IconMenuName.UNIT,Icon.UNIT),context)},
        OpenableMenuItem("UC","Combat", null, IconTuple(IconMenuName.COMBAT,Icon.COMBAT)){ vm, context-> vm.finishCreatingSymbol(IconTuple(IconMenuName.COMBAT,Icon.COMBAT),context)},
        OpenableMenuItem("UAD", "Air Defence", null, IconTuple(IconMenuName.AIR_DEFENSE,Icon.AIR_DEFENSE)){ vm, context-> vm.finishCreatingSymbol(
            IconTuple(IconMenuName.AIR_DEFENSE,Icon.AIR_DEFENSE),context)}, //this might actually work
        OpenableMenuItem("UA", "Armor", null,null),
        OpenableMenuItem("UAA", "Anti Armor",null,null),
        OpenableMenuItem("UAV","Aviation",null, IconTuple(IconMenuName.AVIATION,Icon.AVIATION)){ vm, context-> vm.finishCreatingSymbol(IconTuple(IconMenuName.AVIATION,Icon.AVIATION),context)},
        OpenableMenuItem("UIE", "Infantry and Engineer", null,null),
        OpenableMenuItem("UFA", "Field artillery",null, IconTuple(IconMenuName.FIELD_ARTILLERY,Icon.FIELD_ARTILLERY)){ vm, context-> vm.finishCreatingSymbol(
            IconTuple(IconMenuName.FIELD_ARTILLERY,Icon.FIELD_ARTILLERY),context)},
        OpenableMenuItem("UCS", "Combat support",null, IconTuple(IconMenuName.COMBAT_SUPPORT,Icon.COMBAT_SUPPORT)){ vm, context-> vm.finishCreatingSymbol(
            IconTuple(IconMenuName.COMBAT_SUPPORT,Icon.COMBAT_SUPPORT),context)},
        OpenableMenuItem("UO","Other",null,null)
    )

    private inline fun groundUnitCSLvl3() = listOf(
        OpenableMenuItem("SN","NBC", null, IconTuple(IconMenuName.NBC,Icon.NBC)){ vm, context-> vm.finishCreatingSymbol(IconTuple(IconMenuName.NBC,Icon.NBC),context)},
        OpenableMenuItem("SI", "Military Intelligence",null, IconTuple(IconMenuName.MILITARY_INTELLIGENCE,Icon.MILITARY_INTELLIGENCE)){ vm, context -> vm.finishCreatingSymbol(
            IconTuple(IconMenuName.MILITARY_INTELLIGENCE,Icon.MILITARY_INTELLIGENCE),context)},
        OpenableMenuItem("SS", "Signal Unit",null, IconTuple(IconMenuName.SIGNAL_UNIT,Icon.SIGNAL_UNIT)){ vm, context-> vm.finishCreatingSymbol(
            IconTuple(IconMenuName.SIGNAL_UNIT,Icon.SIGNAL_UNIT),context)},
        OpenableMenuItem("SL","Law Enforcement",null, IconTuple(IconMenuName.LAW_ENFORCEMENT_UNIT,Icon.LAW_ENFORCEMENT_UNIT)){ vm, context -> vm.finishCreatingSymbol(
            IconTuple(IconMenuName.LAW_ENFORCEMENT_UNIT,Icon.LAW_ENFORCEMENT_UNIT),context)},
        OpenableMenuItem("SCSS", "Command Service Support",null, IconTuple(IconMenuName.COMBAT_SERVICE_SUPPORT,Icon.COMBAT_SERVICE_SUPPORT)){ vm, context -> vm.finishCreatingSymbol(
            IconTuple(IconMenuName.COMBAT_SERVICE_SUPPORT,Icon.COMBAT_SERVICE_SUPPORT),context)},
        OpenableMenuItem("SO", "Other", null,null)
    )

    private fun groundUnitNBCLvl4() = createBottomList(listOf(
        IconTuple(IconMenuName.COMBAT_SUPPORT_NBC,Icon.COMBAT_SUPPORT_NBC),
        IconTuple(IconMenuName.CHEMICAL,Icon.CHEMICAL),
        IconTuple(IconMenuName.SMOKE_DECON,Icon.SMOKE_DECON),
        IconTuple(IconMenuName.MECHANIZED_SMOKE_DECON,Icon.MECHANIZED_SMOKE_DECON),
        IconTuple(IconMenuName.MOTORIZED_SMOKE_DECON,Icon.MOTORIZED_SMOKE_DECON),
        IconTuple(IconMenuName.SMOKE,Icon.SMOKE),
        IconTuple(IconMenuName.MOTORIZED_SMOKE,Icon.MOTORIZED_SMOKE),
        IconTuple(IconMenuName.ARMOR_SMOKE,Icon.ARMOR_SMOKE),
        IconTuple(IconMenuName.CHEMICAL_RECON,Icon.CHEMICAL_RECON),
        IconTuple(IconMenuName.CHEMICAL_WHEELED_ARMORED_VEHICLE,Icon.CHEMICAL_WHEELED_ARMORED_VEHICLE),
        IconTuple(IconMenuName.CHEMICAL_WHEELED_ARMORED_VEHICLE_RECONNAISSANCE_SURVEILLANCE,Icon.CHEMICAL_WHEELED_ARMORED_VEHICLE_RECONNAISSANCE_SURVEILLANCE),
        IconTuple(IconMenuName.NUCLEAR,Icon.NUCLEAR),
        IconTuple(IconMenuName.BIOLOGICAL,Icon.BIOLOGICAL),
        IconTuple(IconMenuName.RECON_EQUIPPED,Icon.RECON_EQUIPPED),
        IconTuple(IconMenuName.DECONTAMINATION,Icon.DECONTAMINATION),
    ))

    private fun groundUnitMilIntLvl4() = createBottomList(listOf(
            IconTuple(IconMenuName.MILITARY_INTELLIGENCE,Icon.MILITARY_INTELLIGENCE),
            IconTuple(IconMenuName.AERIAL_EXPLOITATION,Icon.AERIAL_EXPLOITATION),
            IconTuple(IconMenuName.SIGNAL_INTELLIGENCE_SIGINT,Icon.SIGNAL_INTELLIGENCE_SIGINT),
            IconTuple(IconMenuName.ELECTRONIC_WARFARE,Icon.ELECTRONIC_WARFARE),
            IconTuple(IconMenuName.ARMORED_WHEELED_VEHICLE,Icon.ARMORED_WHEELED_VEHICLE),
            IconTuple(IconMenuName.DIRECTION_FINDING,Icon.DIRECTION_FINDING),
            IconTuple(IconMenuName.INTERCEPT,Icon.INTERCEPT),
            IconTuple(IconMenuName.JAMMING,Icon.JAMMING),
            IconTuple(IconMenuName.THEATER,Icon.THEATER),
            IconTuple(IconMenuName.CORPS,Icon.CORPS),
            IconTuple(IconMenuName.COUNTER_INTELLIGENCE,Icon.COUNTER_INTELLIGENCE),
            IconTuple(IconMenuName.SURVEILLANCE,Icon.SURVEILLANCE),
            IconTuple(IconMenuName.GROUND_SURVEILLANCE_RADAR,Icon.GROUND_SURVEILLANCE_RADAR),
            IconTuple(IconMenuName.SENSOR,Icon.SENSOR),
            IconTuple(IconMenuName.SENSOR_SCM,Icon.SENSOR_SCM),
            IconTuple(IconMenuName.GROUND_STATION_MODULE,Icon.GROUND_STATION_MODULE),
            IconTuple(IconMenuName.SURVEILLANCE_METEOROLOGICAL,Icon.SURVEILLANCE_METEOROLOGICAL),
            IconTuple(IconMenuName.OPERATIONS,Icon.OPERATIONS),
            IconTuple(IconMenuName.TACTICAL_EXPLOIT,Icon.TACTICAL_EXPLOIT),
            IconTuple(IconMenuName.INTERROGATION,Icon.INTERROGATION),
            IconTuple(IconMenuName.JOINT_INTELLIGENCE_CENTER,Icon.JOINT_INTELLIGENCE_CENTER),
    ))
    private fun groundUnitLELvl4() = createBottomList(listOf(
        IconTuple(IconMenuName.LAW_ENFORCEMENT_UNIT,Icon.LAW_ENFORCEMENT_UNIT),
        IconTuple(IconMenuName.SHORE_PATROL,Icon.SHORE_PATROL),
        IconTuple(IconMenuName.MILITARY_POLICE,Icon.MILITARY_POLICE),
        IconTuple(IconMenuName.CIVILIAN_LAW_ENFORCEMENT,Icon.CIVILIAN_LAW_ENFORCEMENT),
        IconTuple(IconMenuName.SECURITY_POLICE_AIR,Icon.SECURITY_POLICE_AIR),
        IconTuple(IconMenuName.CENTRAL_INTELLIGENCE_DIVISION_CID,Icon.CENTRAL_INTELLIGENCE_DIVISION_CID),
    ))
    private fun groundUnitSSLvl4() = createBottomList(listOf(
        IconTuple(IconMenuName.SIGNAL_UNIT,Icon.SIGNAL_UNIT),
        IconTuple(IconMenuName.AREA,Icon.AREA),
        IconTuple(IconMenuName.COMMUNICATION_CONFIGURED_PACKAGE,Icon.COMMUNICATION_CONFIGURED_PACKAGE),
        IconTuple(IconMenuName.LARGE_COMMUNICATION_CONFIGURED_PACKAGE_LCCP,Icon.LARGE_COMMUNICATION_CONFIGURED_PACKAGE_LCCP),
        IconTuple(IconMenuName.COMMAND_OPERATIONS,Icon.COMMAND_OPERATIONS),
        IconTuple(IconMenuName.FORWARD_COMMUNICATIONS,Icon.FORWARD_COMMUNICATIONS),
        IconTuple(IconMenuName.MULTIPLE_SUBSCRIBER_ELEMENT,Icon.MULTIPLE_SUBSCRIBER_ELEMENT),
        IconTuple(IconMenuName.SMALL_EXTENSION_NODE,Icon.SMALL_EXTENSION_NODE),
        IconTuple(IconMenuName.LARGE_EXTENSION_NODE,Icon.LARGE_EXTENSION_NODE),
        IconTuple(IconMenuName.NODE_CENTER,Icon.NODE_CENTER),
        IconTuple(IconMenuName.RADIO_UNIT,Icon.RADIO_UNIT),
        IconTuple(IconMenuName.TACTICAL_SATELLITE,Icon.TACTICAL_SATELLITE),
        IconTuple(IconMenuName.TELETYPE_CENTER,Icon.TELETYPE_CENTER),
        IconTuple(IconMenuName.RELAY,Icon.RELAY),
        IconTuple(IconMenuName.SIGNAL_SUPPORT,Icon.SIGNAL_SUPPORT),
        IconTuple(IconMenuName.TELEPHONE_SWITCH,Icon.TELEPHONE_SWITCH),
        IconTuple(IconMenuName.ELECTRONIC_RANGING,Icon.ELECTRONIC_RANGING),
    ))
    private fun groundUnitOLvl4() = createBottomList(listOf(
        IconTuple(IconMenuName.INFORMATION_WARFARE_UNIT,Icon.INFORMATION_WARFARE_UNIT),
        IconTuple(IconMenuName.LANDING_SUPPORT,Icon.LANDING_SUPPORT),
        IconTuple(IconMenuName.EXPLOSIVE_ORDNANCE_DISPOSAL,Icon.EXPLOSIVE_ORDNANCE_DISPOSAL),
    ))
    private fun groundUnitCSSLvl4() = createBottomList(listOf(
        IconTuple(IconMenuName.COMBAT_SERVICE_SUPPORT,Icon.COMBAT_SERVICE_SUPPORT),
        IconTuple(IconMenuName.ADMINISTRATIVE_ADMIN,Icon.ADMINISTRATIVE_ADMIN),
        IconTuple(IconMenuName.ADMIN_THEATER,Icon.ADMIN_THEATER),
        IconTuple(IconMenuName.ADMIN_CORPS,Icon.ADMIN_CORPS),
        IconTuple(IconMenuName.JUDGE_ADVOCATE_GENERAL_JAG,Icon.JUDGE_ADVOCATE_GENERAL_JAG),
        IconTuple(IconMenuName.JAG_THEATER,Icon.JAG_THEATER),
        IconTuple(IconMenuName.JAG_CORPS,Icon.JAG_CORPS),
        IconTuple(IconMenuName.POSTAL,Icon.POSTAL),
        IconTuple(IconMenuName.POSTAL_THEATER,Icon.POSTAL_THEATER),
        IconTuple(IconMenuName.POSTAL_CORPS,Icon.POSTAL_CORPS),
        IconTuple(IconMenuName.FINANCE,Icon.FINANCE),
        IconTuple(IconMenuName.FINANCE_THEATER,Icon.FINANCE_THEATER),
        IconTuple(IconMenuName.FINANCE_CORPS,Icon.FINANCE_CORPS),
        IconTuple(IconMenuName.PERSONNEL_SERVICES,Icon.PERSONNEL_SERVICES),
        IconTuple(IconMenuName.PERSONNEL_THEATER,Icon.PERSONNEL_THEATER),
        IconTuple(IconMenuName.PERSONNEL_CORPS,Icon.PERSONNEL_CORPS),
        IconTuple(IconMenuName.MORTUARY_GRAVES_REGISTRY,Icon.MORTUARY_GRAVES_REGISTRY),
        IconTuple(IconMenuName.MORTUARY_GRAVES_REGISTRY_THEATER,Icon.MORTUARY_GRAVES_REGISTRY_THEATER),
        IconTuple(IconMenuName.MORTUARY_GRAVES_REGISTRY_CORPS,Icon.MORTUARY_GRAVES_REGISTRY_CORPS),
        IconTuple(IconMenuName.RELIGIOUS_CHAPLAIN,Icon.RELIGIOUS_CHAPLAIN),
        IconTuple(IconMenuName.RELIGIOUS_CHAPLAIN_THEATER,Icon.RELIGIOUS_CHAPLAIN_THEATER),
        IconTuple(IconMenuName.RELIGIOUS_CHAPLAIN_CORPS,Icon.RELIGIOUS_CHAPLAIN_CORPS),
        IconTuple(IconMenuName.PUBLIC_AFFAIRS,Icon.PUBLIC_AFFAIRS),
        IconTuple(IconMenuName.PUBLIC_AFFAIRS_THEATER,Icon.PUBLIC_AFFAIRS_THEATER),
        IconTuple(IconMenuName.PUBLIC_AFFAIRS_CORPS,Icon.PUBLIC_AFFAIRS_CORPS),
        IconTuple(IconMenuName.PUBLIC_AFFAIRS_BROADCAST,Icon.PUBLIC_AFFAIRS_BROADCAST),
        IconTuple(IconMenuName.PUBLIC_AFFAIRS_BROADCAST_THEATER,Icon.PUBLIC_AFFAIRS_BROADCAST_THEATER),
        IconTuple(IconMenuName.PUBLIC_AFFAIRS_BROADCAST_CORPS,Icon.PUBLIC_AFFAIRS_BROADCAST_CORPS),
        IconTuple(IconMenuName.PUBLIC_AFFAIRS_JOINT_INFORMATION_BUREAU_JIB,Icon.PUBLIC_AFFAIRS_JOINT_INFORMATION_BUREAU_JIB),
        IconTuple(IconMenuName.PUBLIC_AFFAIRS_JIB_THEATER,Icon.PUBLIC_AFFAIRS_JIB_THEATER),
        IconTuple(IconMenuName.PUBLIC_AFFAIRS_JIB_CORPS,Icon.PUBLIC_AFFAIRS_JIB_CORPS),
        IconTuple(IconMenuName.REPLACEMENT_HOLDING_UNIT_RHU,Icon.REPLACEMENT_HOLDING_UNIT_RHU),
        IconTuple(IconMenuName.RHU_THEATER,Icon.RHU_THEATER),
        IconTuple(IconMenuName.RHU_CORPS,Icon.RHU_CORPS),
        IconTuple(IconMenuName.LABOR,Icon.LABOR),
        IconTuple(IconMenuName.LABOR_THEATER,Icon.LABOR_THEATER),
        IconTuple(IconMenuName.LABOR_CORPS,Icon.LABOR_CORPS),
        IconTuple(IconMenuName.MORALE_WELFARE_RECREATION_MWR,Icon.MORALE_WELFARE_RECREATION_MWR),
        IconTuple(IconMenuName.MWR_THEATER,Icon.MWR_THEATER),
        IconTuple(IconMenuName.MWR_CORPS,Icon.MWR_CORPS),
        IconTuple(IconMenuName.QUARTERMASTER_SUPPLY,Icon.QUARTERMASTER_SUPPLY),
        IconTuple(IconMenuName.QUARTERMASTER_SUPPLY_THEATER,Icon.QUARTERMASTER_SUPPLY_THEATER),
        IconTuple(IconMenuName.QUARTERMASTER_SUPPLY_CORPS,Icon.QUARTERMASTER_SUPPLY_CORPS),
        IconTuple(IconMenuName.MEDICAL,Icon.MEDICAL),
        IconTuple(IconMenuName.MEDICAL_THEATER,Icon.MEDICAL_THEATER),
        IconTuple(IconMenuName.MEDICAL_CORPS,Icon.MEDICAL_CORPS),
        IconTuple(IconMenuName.MEDICAL_TREATMENT_FACILITY,Icon.MEDICAL_TREATMENT_FACILITY),
        IconTuple(IconMenuName.MEDICAL_TREATMENT_FACILITY_THEATER,Icon.MEDICAL_TREATMENT_FACILITY_THEATER),
        IconTuple(IconMenuName.MEDICAL_TREATMENT_FACILITY_CORPS,Icon.MEDICAL_TREATMENT_FACILITY_CORPS),
        IconTuple(IconMenuName.MEDICAL_VETERINARY,Icon.MEDICAL_VETERINARY),
        IconTuple(IconMenuName.MEDICAL_VETERINARY_THEATER,Icon.MEDICAL_VETERINARY_THEATER),
        IconTuple(IconMenuName.MEDICAL_VETERINARY_CORPS,Icon.MEDICAL_VETERINARY_CORPS),
        IconTuple(IconMenuName.MEDICAL_DENTAL,Icon.MEDICAL_DENTAL),
        IconTuple(IconMenuName.MEDICAL_DENTAL_THEATER,Icon.MEDICAL_DENTAL_THEATER),
        IconTuple(IconMenuName.MEDICAL_DENTAL_CORPS,Icon.MEDICAL_DENTAL_CORPS),
        IconTuple(IconMenuName.MEDICAL_PSYCHOLOGICAL,Icon.MEDICAL_PSYCHOLOGICAL),
        IconTuple(IconMenuName.MEDICAL_PSYCHOLOGICAL_THEATER,Icon.MEDICAL_PSYCHOLOGICAL_THEATER),
        IconTuple(IconMenuName.MEDICAL_PSYCHOLOGICAL_CORPS,Icon.MEDICAL_PSYCHOLOGICAL_CORPS),
        IconTuple(IconMenuName.SUPPLY,Icon.SUPPLY),
        IconTuple(IconMenuName.SUPPLY_THEATER,Icon.SUPPLY_THEATER),
        IconTuple(IconMenuName.SUPPLY_CORPS,Icon.SUPPLY_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_I,Icon.SUPPLY_CLASS_I),
        IconTuple(IconMenuName.SUPPLY_CLASS_I_THEATER,Icon.SUPPLY_CLASS_I_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_I_CORPS,Icon.SUPPLY_CLASS_I_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_II,Icon.SUPPLY_CLASS_II),
        IconTuple(IconMenuName.SUPPLY_CLASS_II_THEATER,Icon.SUPPLY_CLASS_II_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_II_CORPS,Icon.SUPPLY_CLASS_II_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_III,Icon.SUPPLY_CLASS_III),
        IconTuple(IconMenuName.SUPPLY_CLASS_III_THEATER,Icon.SUPPLY_CLASS_III_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_III_CORPS,Icon.SUPPLY_CLASS_III_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_III_AVIATION,Icon.SUPPLY_CLASS_III_AVIATION),
        IconTuple(IconMenuName.SUPPLY_CLASS_III_AVIATION_THEATER,Icon.SUPPLY_CLASS_III_AVIATION_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_III_AVIATION_CORPS,Icon.SUPPLY_CLASS_III_AVIATION_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_IV,Icon.SUPPLY_CLASS_IV),
        IconTuple(IconMenuName.SUPPLY_CLASS_IV_THEATER,Icon.SUPPLY_CLASS_IV_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_IV_CORPS,Icon.SUPPLY_CLASS_IV_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_V,Icon.SUPPLY_CLASS_V),
        IconTuple(IconMenuName.SUPPLY_CLASS_V_THEATER,Icon.SUPPLY_CLASS_V_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_V_CORPS,Icon.SUPPLY_CLASS_V_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_VI,Icon.SUPPLY_CLASS_VI),
        IconTuple(IconMenuName.SUPPLY_CLASS_VI_THEATER,Icon.SUPPLY_CLASS_VI_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_VI_CORPS,Icon.SUPPLY_CLASS_VI_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_VII,Icon.SUPPLY_CLASS_VII),
        IconTuple(IconMenuName.SUPPLY_CLASS_VII_THEATER,Icon.SUPPLY_CLASS_VII_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_VII_CORPS,Icon.SUPPLY_CLASS_VII_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_VIII,Icon.SUPPLY_CLASS_VIII),
        IconTuple(IconMenuName.SUPPLY_CLASS_VIII_THEATER,Icon.SUPPLY_CLASS_VIII_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_VIII_CORPS,Icon.SUPPLY_CLASS_VIII_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_IX,Icon.SUPPLY_CLASS_IX),
        IconTuple(IconMenuName.SUPPLY_CLASS_IX_THEATER,Icon.SUPPLY_CLASS_IX_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_IX_CORPS,Icon.SUPPLY_CLASS_IX_CORPS),
        IconTuple(IconMenuName.SUPPLY_CLASS_X,Icon.SUPPLY_CLASS_X),
        IconTuple(IconMenuName.SUPPLY_CLASS_X_THEATER,Icon.SUPPLY_CLASS_X_THEATER),
        IconTuple(IconMenuName.SUPPLY_CLASS_X_CORPS,Icon.SUPPLY_CLASS_X_CORPS),
        IconTuple(IconMenuName.SUPPLY_LAUNDRY_BATH,Icon.SUPPLY_LAUNDRY_BATH),
        IconTuple(IconMenuName.SUPPLY_LAUNDRY_BATH_THEATER,Icon.SUPPLY_LAUNDRY_BATH_THEATER),
        IconTuple(IconMenuName.SUPPLY_LAUNDRY_BATH_CORPS,Icon.SUPPLY_LAUNDRY_BATH_CORPS),
        IconTuple(IconMenuName.SUPPLY_WATER,Icon.SUPPLY_WATER),
        IconTuple(IconMenuName.SUPPLY_WATER_THEATER,Icon.SUPPLY_WATER_THEATER),
        IconTuple(IconMenuName.SUPPLY_WATER_CORPS,Icon.SUPPLY_WATER_CORPS),
        IconTuple(IconMenuName.SUPPLY_WATER_PURIFICATION,Icon.SUPPLY_WATER_PURIFICATION),
        IconTuple(IconMenuName.SUPPLY_WATER_PURIFICATION_THEATER,Icon.SUPPLY_WATER_PURIFICATION_THEATER),
        IconTuple(IconMenuName.SUPPLY_WATER_PURIFICATION_CORPS,Icon.SUPPLY_WATER_PURIFICATION_CORPS),
        IconTuple(IconMenuName.TRANSPORTATION,Icon.TRANSPORTATION),
        IconTuple(IconMenuName.TRANSPORTATION_THEATER,Icon.TRANSPORTATION_THEATER),
        IconTuple(IconMenuName.TRANSPORTATION_CORPS,Icon.TRANSPORTATION_CORPS),
        IconTuple(IconMenuName.MOVEMENT_CONTROL_CENTER_MCC,Icon.MOVEMENT_CONTROL_CENTER_MCC),
        IconTuple(IconMenuName.MCC_THEATER,Icon.MCC_THEATER),
        IconTuple(IconMenuName.MCC_CORPS,Icon.MCC_CORPS),
        IconTuple(IconMenuName.RAILHEAD,Icon.RAILHEAD),
        IconTuple(IconMenuName.RAILHEAD_THEATER,Icon.RAILHEAD_THEATER),
        IconTuple(IconMenuName.RAILHEAD_CORPS,Icon.RAILHEAD_CORPS),
        IconTuple(IconMenuName.SPOD_SPOE,Icon.SPOD_SPOE),
        IconTuple(IconMenuName.SPOD_SPOE_THEATER,Icon.SPOD_SPOE_THEATER),
        IconTuple(IconMenuName.SPOD_SPOE_CORPS,Icon.SPOD_SPOE_CORPS),
        IconTuple(IconMenuName.APOD_APOE,Icon.APOD_APOE),
        IconTuple(IconMenuName.APOD_APOE_THEATER,Icon.APOD_APOE_THEATER),
        IconTuple(IconMenuName.APOD_APOE_CORPS,Icon.APOD_APOE_CORPS),
        IconTuple(IconMenuName.MISSILE,Icon.MISSILE),
        IconTuple(IconMenuName.MISSILE_THEATER,Icon.MISSILE_THEATER),
        IconTuple(IconMenuName.MISSILE_CORPS,Icon.MISSILE_CORPS),
        IconTuple(IconMenuName.MAINTENANCE,Icon.MAINTENANCE),
        IconTuple(IconMenuName.MAINTENANCE_THEATER,Icon.MAINTENANCE_THEATER),
        IconTuple(IconMenuName.MAINTENANCE_CORPS,Icon.MAINTENANCE_CORPS),
        IconTuple(IconMenuName.MAINTENANCE_HEAVY,Icon.MAINTENANCE_HEAVY),
        IconTuple(IconMenuName.MAINTENANCE_HEAVY_THEATER,Icon.MAINTENANCE_HEAVY_THEATER),
        IconTuple(IconMenuName.MAINTENANCE_HEAVY_CORPS,Icon.MAINTENANCE_HEAVY_CORPS),
        IconTuple(IconMenuName.MAINTENANCE_RECOVERY,Icon.MAINTENANCE_RECOVERY),
        IconTuple(IconMenuName.MAINTENANCE_RECOVERY_THEATER,Icon.MAINTENANCE_RECOVERY_THEATER),
        IconTuple(IconMenuName.MAINTENANCE_RECOVERY_CORPS,Icon.MAINTENANCE_RECOVERY_CORPS),
        IconTuple(IconMenuName.ORDNANCE,Icon.ORDNANCE),
        IconTuple(IconMenuName.ORDNANCE_THEATER,Icon.ORDNANCE_THEATER),
        IconTuple(IconMenuName.ORDNANCE_CORPS,Icon.ORDNANCE_CORPS),
        IconTuple(IconMenuName.ORDNANCE_MISSILE,Icon.ORDNANCE_MISSILE),
        IconTuple(IconMenuName.ORDNANCE_MISSILE_THEATER,Icon.ORDNANCE_MISSILE_THEATER),
        IconTuple(IconMenuName.ORDNANCE_MISSILE_CORPS,Icon.ORDNANCE_MISSILE_CORPS),
        IconTuple(IconMenuName.ELECTROOPTICAL,Icon.ELECTROOPTICAL),
        IconTuple(IconMenuName.ELECTROOPTICAL_THEATER,Icon.ELECTROOPTICAL_THEATER),
        IconTuple(IconMenuName.ELECTROOPTICAL_CORPS,Icon.ELECTROOPTICAL_CORPS),
    ))
    private fun groundUnitOLvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.RECONNAISSANCE,Icon.RECONNAISSANCE),
        IconTuple(IconMenuName.RECONNAISSANCE_HORSE,Icon.RECONNAISSANCE_HORSE),
        IconTuple(IconMenuName.RECONNAISSANCE_CAVALRY,Icon.RECONNAISSANCE_CAVALRY),
        IconTuple(IconMenuName.RECONNAISSANCE_CAVALRY_ARMORED,Icon.RECONNAISSANCE_CAVALRY_ARMORED),
        IconTuple(IconMenuName.RECONNAISSANCE_CAVALRY_MOTORIZED,Icon.RECONNAISSANCE_CAVALRY_MOTORIZED),
        IconTuple(IconMenuName.RECONNAISSANCE_CAVALRY_GROUND,Icon.RECONNAISSANCE_CAVALRY_GROUND),
        IconTuple(IconMenuName.RECONNAISSANCE_CAVALRY_AIR,Icon.RECONNAISSANCE_CAVALRY_AIR),
        IconTuple(IconMenuName.RECONNAISSANCE_ARCTIC,Icon.RECONNAISSANCE_ARCTIC),
        IconTuple(IconMenuName.RECONNAISSANCE_AIR_ASSAULT,Icon.RECONNAISSANCE_AIR_ASSAULT),
        IconTuple(IconMenuName.RECONNAISSANCE_AIRBORNE,Icon.RECONNAISSANCE_AIRBORNE),
        IconTuple(IconMenuName.RECONNAISSANCE_MOUNTAIN,Icon.RECONNAISSANCE_MOUNTAIN),
        IconTuple(IconMenuName.RECONNAISSANCE_LIGHT,Icon.RECONNAISSANCE_LIGHT),
        IconTuple(IconMenuName.RECONNAISSANCE_MARINE,Icon.RECONNAISSANCE_MARINE),
        IconTuple(IconMenuName.RECONNAISSANCE_MARINE_DIVISION,Icon.RECONNAISSANCE_MARINE_DIVISION),
        IconTuple(IconMenuName.RECONNAISSANCE_MARINE_FORCE,Icon.RECONNAISSANCE_MARINE_FORCE),
        IconTuple(IconMenuName.RECONNAISSANCE_MARINE_LIGHT_ARMORED_RECONNAISSNACE_LAR,Icon.RECONNAISSANCE_MARINE_LIGHT_ARMORED_RECONNAISSNACE_LAR),
        IconTuple(IconMenuName.RECONNAISSANCE_LONG_RANGE_SURVEILLANCE_LRS,Icon.RECONNAISSANCE_LONG_RANGE_SURVEILLANCE_LRS),
        IconTuple(IconMenuName.MISSILE_SURFSURF,Icon.MISSILE_SURFSURF),
        IconTuple(IconMenuName.MISSILE_SURFSURF_TACTICAL,Icon.MISSILE_SURFSURF_TACTICAL),
        IconTuple(IconMenuName.MISSILE_SURFSURF_STRATEGIC,Icon.MISSILE_SURFSURF_STRATEGIC),
        IconTuple(IconMenuName.INTERNAL_SECURITY_FORCES,Icon.INTERNAL_SECURITY_FORCES),
        IconTuple(IconMenuName.RIVERINE,Icon.RIVERINE),
        IconTuple(IconMenuName.GROUND,Icon.GROUND),
        IconTuple(IconMenuName.DISMOUNTED_GROUND,Icon.DISMOUNTED_GROUND),
        IconTuple(IconMenuName.MOTORIZED_GROUND,Icon.MOTORIZED_GROUND),
        IconTuple(IconMenuName.MECHANIZED_GROUND,Icon.MECHANIZED_GROUND),
        IconTuple(IconMenuName.WHEELED_MECHANIZED,Icon.WHEELED_MECHANIZED),
        IconTuple(IconMenuName.RAILROAD,Icon.RAILROAD),
        IconTuple(IconMenuName.INTERNAL_SEC_FORCES_AVIATION,Icon.INTERNAL_SEC_FORCES_AVIATION)
    ))
    private fun groundUnitFALvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.FIELD_ARTILLERY,Icon.FIELD_ARTILLERY),
        IconTuple(IconMenuName.HOWITZER_GUN,Icon.HOWITZER_GUN),
        IconTuple(IconMenuName.SELFPROPELLED,Icon.SELFPROPELLED),
        IconTuple(IconMenuName.GUN_AIR_ASSAULT,Icon.GUN_AIR_ASSAULT),
        IconTuple(IconMenuName.GUN_AIRBORNE,Icon.GUN_AIRBORNE),
        IconTuple(IconMenuName.ARCTIC,Icon.ARCTIC),
        IconTuple(IconMenuName.GUN_MOUNTAIN,Icon.GUN_MOUNTAIN),
        IconTuple(IconMenuName.GUN_LIGHT,Icon.GUN_LIGHT),
        IconTuple(IconMenuName.MEDIUM,Icon.MEDIUM),
        IconTuple(IconMenuName.HEAVY,Icon.HEAVY),
        IconTuple(IconMenuName.AMPHIBIOUS,Icon.AMPHIBIOUS),
        IconTuple(IconMenuName.ROCKET,Icon.ROCKET),
        IconTuple(IconMenuName.SINGLE_ROCKET_LAUNCHER,Icon.SINGLE_ROCKET_LAUNCHER),
        IconTuple(IconMenuName.SINGLE_ROCKET_SELFPROPELLED,Icon.SINGLE_ROCKET_SELFPROPELLED),
        IconTuple(IconMenuName.SINGLE_ROCKET_TRUCK,Icon.SINGLE_ROCKET_TRUCK),
        IconTuple(IconMenuName.SINGLE_ROCKET_TOWED,Icon.SINGLE_ROCKET_TOWED),
        IconTuple(IconMenuName.MULTI_ROCKET_LAUNCHER,Icon.MULTI_ROCKET_LAUNCHER),
        IconTuple(IconMenuName.MULTI_ROCKET_SELFPROPELLED,Icon.MULTI_ROCKET_SELFPROPELLED),
        IconTuple(IconMenuName.MULTI_ROCKET_TRUCK,Icon.MULTI_ROCKET_TRUCK),
        IconTuple(IconMenuName.MULTI_ROCKET_TOWED,Icon.MULTI_ROCKET_TOWED),
        IconTuple(IconMenuName.TARGET_ACQUISITION,Icon.TARGET_ACQUISITION),
        IconTuple(IconMenuName.TARGET_AQUSITION_RADAR,Icon.TARGET_AQUSITION_RADAR),
        IconTuple(IconMenuName.SOUND,Icon.SOUND),
        IconTuple(IconMenuName.FLASH_OPTICAL,Icon.FLASH_OPTICAL),
        IconTuple(IconMenuName.COLT_FIST,Icon.COLT_FIST),
        IconTuple(IconMenuName.DISMOUNTED_COLT_FIST,Icon.DISMOUNTED_COLT_FIST),
        IconTuple(IconMenuName.TRACKED_COLT_FIST,Icon.TRACKED_COLT_FIST),
        IconTuple(IconMenuName.ANGLICO,Icon.ANGLICO),
        IconTuple(IconMenuName.MORTAR,Icon.MORTAR),
        IconTuple(IconMenuName.SELFPROPELLED_SP_TRACKED_MORTAR,Icon.SELFPROPELLED_SP_TRACKED_MORTAR),
        IconTuple(IconMenuName.SP_WHEELED_MORTAR,Icon.SP_WHEELED_MORTAR),
        IconTuple(IconMenuName.TOWED_MORTAR,Icon.TOWED_MORTAR),
        IconTuple(IconMenuName.TOWED_AIRBORNE_MORTAR,Icon.TOWED_AIRBORNE_MORTAR),
        IconTuple(IconMenuName.TOWED_AIR_ASSAULT_MORTAR,Icon.TOWED_AIR_ASSAULT_MORTAR),
        IconTuple(IconMenuName.TOWED_ARCTIC_MORTAR,Icon.TOWED_ARCTIC_MORTAR),
        IconTuple(IconMenuName.TOWED_MOUNTAIN_MORTAR,Icon.TOWED_MOUNTAIN_MORTAR),
        IconTuple(IconMenuName.AMPHIBIOUS_MORTAR,Icon.AMPHIBIOUS_MORTAR),
        IconTuple(IconMenuName.ARTILLERY_SURVEY,Icon.ARTILLERY_SURVEY),
        IconTuple(IconMenuName.AIR_ASSAULT,Icon.AIR_ASSAULT),
        IconTuple(IconMenuName.AIRBORNE,Icon.AIRBORNE),
        IconTuple(IconMenuName.LIGHT,Icon.LIGHT),
        IconTuple(IconMenuName.MOUNTAIN,Icon.MOUNTAIN),
        IconTuple(IconMenuName.METEOROLOGICAL,Icon.METEOROLOGICAL),
        IconTuple(IconMenuName.AIR_ASSAULT_METEOROLOGICAL,Icon.AIR_ASSAULT_METEOROLOGICAL),
        IconTuple(IconMenuName.AIRBORNE_METEOROLOGICAL,Icon.AIRBORNE_METEOROLOGICAL),
        IconTuple(IconMenuName.LIGHT_METEOROLOGICAL,Icon.LIGHT_METEOROLOGICAL),
        IconTuple(IconMenuName.MOUNTAIN_METEOROLOGICAL,Icon.MOUNTAIN_METEOROLOGICAL),
    ))
    private fun groundUnitIELvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.INFANTRY,Icon.INFANTRY),
        IconTuple(IconMenuName.INFANTRY_LIGHT,Icon.INFANTRY_LIGHT),
        IconTuple(IconMenuName.INFANTRY_MOTORIZED,Icon.INFANTRY_MOTORIZED),
        IconTuple(IconMenuName.INFANTRY_MOUNTAIN,Icon.INFANTRY_MOUNTAIN),
        IconTuple(IconMenuName.INFANTRY_AIRBORNE,Icon.INFANTRY_AIRBORNE),
        IconTuple(IconMenuName.INFANTRY_AIR_ASSAULT,Icon.INFANTRY_AIR_ASSAULT),
        IconTuple(IconMenuName.INFANTRY_MECHANIZED,Icon.INFANTRY_MECHANIZED),
        IconTuple(IconMenuName.INFANTRY_NAVAL,Icon.INFANTRY_NAVAL),
        IconTuple(IconMenuName.INFANTRY_FIGHTING_VEHICLE,Icon.INFANTRY_FIGHTING_VEHICLE),
        IconTuple(IconMenuName.INFANTRY_ARCTIC,Icon.INFANTRY_ARCTIC),
        IconTuple(IconMenuName.ENGINEER,Icon.ENGINEER),
        IconTuple(IconMenuName.ENGINEER_COMBAT,Icon.ENGINEER_COMBAT),
        IconTuple(IconMenuName.ENGINEER_COMBAT_AIR_ASSAULT,Icon.ENGINEER_COMBAT_AIR_ASSAULT),
        IconTuple(IconMenuName.ENGINEER_COMBAT_AIRBORNE,Icon.ENGINEER_COMBAT_AIRBORNE),
        IconTuple(IconMenuName.ENGINEER_COMBAT_ARCTIC,Icon.ENGINEER_COMBAT_ARCTIC),
        IconTuple(IconMenuName.ENGINEER_COMBAT_LIGHT_SAPPER,Icon.ENGINEER_COMBAT_LIGHT_SAPPER),
        IconTuple(IconMenuName.ENGINEER_COMBAT_MEDIUM,Icon.ENGINEER_COMBAT_MEDIUM),
        IconTuple(IconMenuName.ENGINEER_COMBAT_HEAVY,Icon.ENGINEER_COMBAT_HEAVY),
        IconTuple(IconMenuName.ENGINEER_COMBAT_MECHANIZED_TRACK,Icon.ENGINEER_COMBAT_MECHANIZED_TRACK),
        IconTuple(IconMenuName.ENGINEER_COMBAT_MOTORIZED,Icon.ENGINEER_COMBAT_MOTORIZED),
        IconTuple(IconMenuName.ENGINEER_COMBAT_MOUNTAIN,Icon.ENGINEER_COMBAT_MOUNTAIN),
        IconTuple(IconMenuName.ENGINEER_COMBAT_RECON,Icon.ENGINEER_COMBAT_RECON),
        IconTuple(IconMenuName.ENGINEER_CONSTRUCTION,Icon.ENGINEER_CONSTRUCTION),
        IconTuple(IconMenuName.ENGINEER_NAVAL_CONSTRUCTION,Icon.ENGINEER_NAVAL_CONSTRUCTION),
    ))
        private fun groundUnitAVLvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.AVIATION,Icon.AVIATION),
        IconTuple(IconMenuName.FIXED_WING,Icon.FIXED_WING),
        IconTuple(IconMenuName.UTILITY_FIXED_WING,Icon.UTILITY_FIXED_WING),
        IconTuple(IconMenuName.ATTACK_FIXED_WING,Icon.ATTACK_FIXED_WING),
        IconTuple(IconMenuName.RECON_FIXED_WING,Icon.RECON_FIXED_WING),
        IconTuple(IconMenuName.ROTARY_WING,Icon.ROTARY_WING),
        IconTuple(IconMenuName.ATTACK_ROTARY_WING,Icon.ATTACK_ROTARY_WING),
        IconTuple(IconMenuName.SCOUT_ROTARY_WING,Icon.SCOUT_ROTARY_WING),
        IconTuple(IconMenuName.ANTISUBMARINE_WARFARE_ROTARY_WING,Icon.ANTISUBMARINE_WARFARE_ROTARY_WING),
        IconTuple(IconMenuName.UTILITY_ROTARY_WING,Icon.UTILITY_ROTARY_WING),
        IconTuple(IconMenuName.LIGHT_UTILITY_ROTARY_WING,Icon.LIGHT_UTILITY_ROTARY_WING),
        IconTuple(IconMenuName.MEDIUM_UTILITY_ROTARY_WING,Icon.MEDIUM_UTILITY_ROTARY_WING),
        IconTuple(IconMenuName.HEAVY_UTILITY_ROTARY_WING,Icon.HEAVY_UTILITY_ROTARY_WING),
        IconTuple(IconMenuName.C2_ROTARY_WING,Icon.C2_ROTARY_WING),
        IconTuple(IconMenuName.MEDEVAC_ROTARY_WING,Icon.MEDEVAC_ROTARY_WING),
        IconTuple(IconMenuName.MINE_COUNTERMEASURE_ROTARY_WING,Icon.MINE_COUNTERMEASURE_ROTARY_WING),
        IconTuple(IconMenuName.SEARCH_AND_RESCUE,Icon.SEARCH_AND_RESCUE),
        IconTuple(IconMenuName.COMPOSITE,Icon.COMPOSITE),
        IconTuple(IconMenuName.VERTICAL_SHORT_TAKEOFF_AND_LANDING_V_STOL,Icon.VERTICAL_SHORT_TAKEOFF_AND_LANDING_V_STOL),
        IconTuple(IconMenuName.UNMANNED_AERIAL_VEHICLE,Icon.UNMANNED_AERIAL_VEHICLE),
        IconTuple(IconMenuName.UNMANNED_AERIAL_VEHICLE_FIXED_WING,Icon.UNMANNED_AERIAL_VEHICLE_FIXED_WING),
        IconTuple(IconMenuName.UNMANNED_AERIAL_VEHICLE_ROTARY_WING,Icon.UNMANNED_AERIAL_VEHICLE_ROTARY_WING),
    ))
    private inline fun groundUnitAArmLvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.ANTI_ARMOR,Icon.ANTI_ARMOR),
        IconTuple(IconMenuName.ANTI_ARMOR_DISMOUNTED,Icon.ANTI_ARMOR_DISMOUNTED),
        IconTuple(IconMenuName.ANTI_ARMOR_LIGHT,Icon.ANTI_ARMOR_LIGHT),
        IconTuple(IconMenuName.ANTI_ARMOR_AIRBORNE,Icon.ANTI_ARMOR_AIRBORNE),
        IconTuple(IconMenuName.ANTI_ARMOR_AIR_ASSAULT,Icon.ANTI_ARMOR_AIR_ASSAULT),
        IconTuple(IconMenuName.ANTI_ARMOR_MOUNTAIN,Icon.ANTI_ARMOR_MOUNTAIN),
        IconTuple(IconMenuName.ANTI_ARMOR_ARCTIC,Icon.ANTI_ARMOR_ARCTIC),
        IconTuple(IconMenuName.ANTI_ARMOR_ARMORED,Icon.ANTI_ARMOR_ARMORED),
        IconTuple(IconMenuName.ANTI_ARMOR_ARMORED_TRACKED,Icon.ANTI_ARMOR_ARMORED_TRACKED),
        IconTuple(IconMenuName.ANTI_ARMOR_ARMORED_WHEELED,Icon.ANTI_ARMOR_ARMORED_WHEELED),
        IconTuple(IconMenuName.ANTI_ARMOR_ARMORED_AIR_ASSAULT,Icon.ANTI_ARMOR_ARMORED_AIR_ASSAULT),
        IconTuple(IconMenuName.ANTI_ARMOR_MOTORIZED,Icon.ANTI_ARMOR_MOTORIZED),
        IconTuple(IconMenuName.ANTI_ARMOR_MOTORIZED_AIR_ASSAULT,Icon.ANTI_ARMOR_MOTORIZED_AIR_ASSAULT),
    ))
    private fun groundUnitArmorLvl3() = createBottomList(listOf(
      IconTuple(IconMenuName.ARMOR,Icon.ARMOR),
      IconTuple(IconMenuName.ARMOR_TRACK,Icon.ARMOR_TRACK),
      IconTuple(IconMenuName.ARMOR_TRACK_AIRBORNE,Icon.ARMOR_TRACK_AIRBORNE),
      IconTuple(IconMenuName.ARMOR_TRACK_AMPHIBIOUS,Icon.ARMOR_TRACK_AMPHIBIOUS),
      IconTuple(IconMenuName.ARMOR_TRACK_AMPHIBIOUS_RECOVERY,Icon.ARMOR_TRACK_AMPHIBIOUS_RECOVERY),
      IconTuple(IconMenuName.ARMOR_TRACK_LIGHT,Icon.ARMOR_TRACK_LIGHT),
      IconTuple(IconMenuName.ARMOR_TRACK_MEDIUM,Icon.ARMOR_TRACK_MEDIUM),
      IconTuple(IconMenuName.ARMOR_TRACK_HEAVY,Icon.ARMOR_TRACK_HEAVY),
      IconTuple(IconMenuName.ARMOR_TRACK_RECOVERY,Icon.ARMOR_TRACK_RECOVERY),
      IconTuple(IconMenuName.ARMOR_WHEELED,Icon.ARMOR_WHEELED),
      IconTuple(IconMenuName.ARMOR_WHEELED_AIR_ASSAULT,Icon.ARMOR_WHEELED_AIR_ASSAULT),
      IconTuple(IconMenuName.ARMOR_WHEELED_AIRBORNE,Icon.ARMOR_WHEELED_AIRBORNE),
      IconTuple(IconMenuName.ARMOR_WHEELED_AMPHIBIOUS,Icon.ARMOR_WHEELED_AMPHIBIOUS),
      IconTuple(IconMenuName.ARMOR_WHEELED_AMPHIBIOUS_RECOVERY,Icon.ARMOR_WHEELED_AMPHIBIOUS_RECOVERY),
      IconTuple(IconMenuName.ARMOR_WHEELED_LIGHT,Icon.ARMOR_WHEELED_LIGHT),
      IconTuple(IconMenuName.ARMOR_WHEELED_MEDIUM,Icon.ARMOR_WHEELED_MEDIUM),
      IconTuple(IconMenuName.ARMOR_WHEELED_HEAVY,Icon.ARMOR_WHEELED_HEAVY),
      IconTuple(IconMenuName.ARMOR_WHEELED_RECOVERY,Icon.ARMOR_WHEELED_RECOVERY),
    ))


    private fun groundUnitADLvl3() = createBottomList(listOf(
        IconTuple(IconMenuName.AIR_DEFENSE,Icon.AIR_DEFENSE),
        IconTuple(IconMenuName.SHORT_RANGE,Icon.SHORT_RANGE),
        IconTuple(IconMenuName.CHAPARRAL,Icon.CHAPARRAL),
        IconTuple(IconMenuName.STINGER,Icon.STINGER),
        IconTuple(IconMenuName.VULCAN,Icon.VULCAN),
        IconTuple(IconMenuName.AIR_DEFENSE_MISSILE,Icon.AIR_DEFENSE_MISSILE),
        IconTuple(IconMenuName.AIR_DEFENSE_MISSILE_LIGHT,Icon.AIR_DEFENSE_MISSILE_LIGHT),
        IconTuple(IconMenuName.AIR_DEFENSE_MISSILE_MOTORIZED_AVENGER,Icon.AIR_DEFENSE_MISSILE_MOTORIZED_AVENGER),
        IconTuple(IconMenuName.AIR_DEFENSE_MISSILE_MEDIUM,Icon.AIR_DEFENSE_MISSILE_MEDIUM),
        IconTuple(IconMenuName.AIR_DEFENSE_MISSILE_HEAVY,Icon.AIR_DEFENSE_MISSILE_HEAVY),
        IconTuple(IconMenuName.H_MAD,Icon.H_MAD),
        IconTuple(IconMenuName.HAWK,Icon.HAWK),
        IconTuple(IconMenuName.PATRIOT,Icon.PATRIOT),
        IconTuple(IconMenuName.GUN_UNIT,Icon.GUN_UNIT),
        IconTuple(IconMenuName.AIR_DEFENCE_COMPOSITE,Icon.AIR_DEFENCE_COMPOSITE),
        IconTuple(IconMenuName.TARGETING_UNIT,Icon.TARGETING_UNIT),
        IconTuple(IconMenuName.THEATER_MISSILE_DEFENSE_UNIT,Icon.THEATER_MISSILE_DEFENSE_UNIT),
    ))

    private fun subSurfaceLvl1() = listOf(
        OpenableMenuItem("-", "Subsurface track", null, IconTuple(IconMenuName.SUBSURFACE_TRACK,Icon.SUBSURFACE_TRACK)){vm, ctx ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.SUBSURFACE_TRACK,Icon.SUBSURFACE_TRACK),ctx)
        },
        OpenableMenuItem("US", "Submarine", null, IconTuple(IconMenuName.SUBMARINE,Icon.SUBMARINE)){vm, ctx ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.SUBMARINE,Icon.SUBMARINE),ctx)
        },
        OpenableMenuItem("UW", "Underwater weapon and deacoy",null,IconTuple(IconMenuName.UNDERWATER_WEAPON,Icon.UNDERWATER_WEAPON)){vm, ctx ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.UNDERWATER_WEAPON,Icon.UNDERWATER_WEAPON),ctx)
        },
        OpenableMenuItem("UNS", "Undawater non-submarine", null, IconTuple(IconMenuName.NONSUBMARINE,Icon.NONSUBMARINE)){ vm, ctx ->
            vm.finishCreatingSymbol(IconTuple(IconMenuName.NONSUBMARINE,Icon.NONSUBMARINE),ctx)
        }
    )

    private fun subSurfaceUNSLvl2() = createBottomList(listOf(
        IconTuple(IconMenuName.NONSUBMARINE,Icon.NONSUBMARINE),
        IconTuple(IconMenuName.DIVER,Icon.DIVER),
    ))


    private fun subSurfaceUWLvl2() = createBottomList(listOf(
        IconTuple(IconMenuName.UNDERWATER_WEAPON,Icon.UNDERWATER_WEAPON),
        IconTuple(IconMenuName.TORPEDO,Icon.TORPEDO),
        IconTuple(IconMenuName.SEA_MINE,Icon.SEA_MINE),
        IconTuple(IconMenuName.SEA_MINE_DEALT,Icon.SEA_MINE_DEALT),
        IconTuple(IconMenuName.SEA_MINE_GROUND,Icon.SEA_MINE_GROUND),
        IconTuple(IconMenuName.SEA_MINE_GROUND_DEALT,Icon.SEA_MINE_GROUND_DEALT),
        IconTuple(IconMenuName.SEA_MINE_MOORED,Icon.SEA_MINE_MOORED),
        IconTuple(IconMenuName.SEA_MINE_MOORED_DEALT,Icon.SEA_MINE_MOORED_DEALT),
        IconTuple(IconMenuName.SEA_MINE_FLOATING,Icon.SEA_MINE_FLOATING),
        IconTuple(IconMenuName.SEA_MINE_FLOATING_DEALT,Icon.SEA_MINE_FLOATING_DEALT),
        IconTuple(IconMenuName.SEA_MINE_OTHER_POSITION,Icon.SEA_MINE_OTHER_POSITION),
        IconTuple(IconMenuName.SEA_MINE_OTHER_POSITION_DEALT,Icon.SEA_MINE_OTHER_POSITION_DEALT),
        IconTuple(IconMenuName.UNDERWATER_DECOY,Icon.UNDERWATER_DECOY),
        IconTuple(IconMenuName.SEA_MINE_DECOY,Icon.SEA_MINE_DECOY),
    ))


    private fun subSurfaceUSLvl2() = createBottomList(listOf(
        IconTuple(IconMenuName.SUBMARINE,Icon.SUBMARINE),
        IconTuple(IconMenuName.SURFACED_SUBMARINE,Icon.SURFACED_SUBMARINE),
        IconTuple(IconMenuName.NUCLEAR_PROPULSION,Icon.NUCLEAR_PROPULSION),
        IconTuple(IconMenuName.SURFACED_NUCLEAR_PROPULSION_SUBMARINE,Icon.SURFACED_NUCLEAR_PROPULSION_SUBMARINE),
        IconTuple(IconMenuName.ATTACK_SUBMARINE_SSN,Icon.ATTACK_SUBMARINE_SSN),
        IconTuple(IconMenuName.MISSILE_SUBMARINE_TYPE_UNKNOWN,Icon.MISSILE_SUBMARINE_TYPE_UNKNOWN),
        IconTuple(IconMenuName.GUIDED_MISSILE_SUBMARINE_SSGN,Icon.GUIDED_MISSILE_SUBMARINE_SSGN),
        IconTuple(IconMenuName.BALLISTIC_MISSILE_SUBMARINE_SSBN,Icon.BALLISTIC_MISSILE_SUBMARINE_SSBN),
        IconTuple(IconMenuName.CONVENTIONAL_PROPULSION,Icon.CONVENTIONAL_PROPULSION),
        IconTuple(IconMenuName.SURFACED_CONVENTIONAL_PROPULSION_SUBMARINE,Icon.SURFACED_CONVENTIONAL_PROPULSION_SUBMARINE),
        IconTuple(IconMenuName.ATTACK_SUBMARINE_SS,Icon.ATTACK_SUBMARINE_SS),
        IconTuple(IconMenuName.CONVENTIONAL_PROPULSION_MISSILE_SUBMARINE_TYPE_UNKNOWN,Icon.CONVENTIONAL_PROPULSION_MISSILE_SUBMARINE_TYPE_UNKNOWN),
        IconTuple(IconMenuName.GUIDED_MISSILE_SUBMARINE_SSG,Icon.GUIDED_MISSILE_SUBMARINE_SSG),
        IconTuple(IconMenuName.BALLISTIC_MISSILE_SUBMARINE_SSB,Icon.BALLISTIC_MISSILE_SUBMARINE_SSB),
        IconTuple(IconMenuName.OTHER_SUBMERSIBLE,Icon.OTHER_SUBMERSIBLE),
        IconTuple(IconMenuName.SURFACED_OTHER_SUBMERSIBLE,Icon.SURFACED_OTHER_SUBMERSIBLE),
        IconTuple(IconMenuName.SUBSURF_STATION,Icon.SUBSURF_STATION),
        IconTuple(IconMenuName.ASW_SUBMARINE,Icon.ASW_SUBMARINE),
        IconTuple(IconMenuName.UNMANNED_UNDERWATER_VEHICLE_UUV,Icon.UNMANNED_UNDERWATER_VEHICLE_UUV),
        IconTuple(IconMenuName.MINE_WARFARE_SUBSURFACE_DRONE,Icon.MINE_WARFARE_SUBSURFACE_DRONE),
        IconTuple(IconMenuName.ANTISUBMARINE_WARFARE_SUBSURFACE_DRONE,Icon.ANTISUBMARINE_WARFARE_SUBSURFACE_DRONE),
        IconTuple(IconMenuName.ANTISURFACE_WARFARE_SUBSURFACE_DRONE,Icon.ANTISURFACE_WARFARE_SUBSURFACE_DRONE),
    ))

    private fun sofLvl1(): List<OpenableMenuItem>
    {
       val list = createBottomList(listOf(
           IconTuple(IconMenuName.SOF_UNIT_AVIATION,Icon.SOF_UNIT_AVIATION),
           IconTuple(IconMenuName.SOF_UNIT_FIXED_WING,Icon.SOF_UNIT_FIXED_WING),
           IconTuple(IconMenuName.SOF_UNIT_ATTACK,Icon.SOF_UNIT_ATTACK),
           IconTuple(IconMenuName.SOF_UNIT_REFUEL,Icon.SOF_UNIT_REFUEL),
           IconTuple(IconMenuName.SOF_UNIT_UTILITY,Icon.SOF_UNIT_UTILITY),
           IconTuple(IconMenuName.SOF_UNIT_UTILITY_LIGHT,Icon.SOF_UNIT_UTILITY_LIGHT),
           IconTuple(IconMenuName.SOF_UNIT_UTILITY_MEDIUM,Icon.SOF_UNIT_UTILITY_MEDIUM),
           IconTuple(IconMenuName.SOF_UNIT_UTILITY_HEAVY,Icon.SOF_UNIT_UTILITY_HEAVY),
           IconTuple(IconMenuName.SOF_UNIT_V_STOL,Icon.SOF_UNIT_V_STOL),
           IconTuple(IconMenuName.SOF_UNIT_ROTARY_WING,Icon.SOF_UNIT_ROTARY_WING),
           IconTuple(IconMenuName.RW_SOF_UNIT_COMBAT_SEARCH_AND_RESCUE,Icon.RW_SOF_UNIT_COMBAT_SEARCH_AND_RESCUE),
           IconTuple(IconMenuName.RW_SOF_UNIT_ATTACK,Icon.RW_SOF_UNIT_ATTACK),
           IconTuple(IconMenuName.RW_SOF_UNIT_UTILITY,Icon.RW_SOF_UNIT_UTILITY),
           IconTuple(IconMenuName.RW_SOF_UNIT_UTILITY_LIGHT,Icon.RW_SOF_UNIT_UTILITY_LIGHT),
           IconTuple(IconMenuName.RW_SOF_UNIT_UTILITY_MEDIUM,Icon.RW_SOF_UNIT_UTILITY_MEDIUM),
           IconTuple(IconMenuName.RW_SOF_UNIT_UTILITY_HEAVY,Icon.RW_SOF_UNIT_UTILITY_HEAVY),
           IconTuple(IconMenuName.SOF_UNIT_SOF_UNIT_NAVAL,Icon.SOF_UNIT_SOF_UNIT_NAVAL),
           IconTuple(IconMenuName.SOF_UNIT_SEAL,Icon.SOF_UNIT_SEAL),
           IconTuple(IconMenuName.SOF_UNIT_UNDERWATER_DEMOLITION_TEAM,Icon.SOF_UNIT_UNDERWATER_DEMOLITION_TEAM),
           IconTuple(IconMenuName.SOF_UNIT_SPECIAL_BOAT,Icon.SOF_UNIT_SPECIAL_BOAT),
           IconTuple(IconMenuName.SOF_UNIT_SPECIAL_SSNR,Icon.SOF_UNIT_SPECIAL_SSNR),
           IconTuple(IconMenuName.SOF_UNIT_GROUND,Icon.SOF_UNIT_GROUND),
           IconTuple(IconMenuName.SOF_UNIT_SPECIAL_FORCES,Icon.SOF_UNIT_SPECIAL_FORCES),
           IconTuple(IconMenuName.SOF_UNIT_RANGER,Icon.SOF_UNIT_RANGER),
           IconTuple(IconMenuName.SOF_UNIT_PSYCHOLOGICAL_OPERATIONS_PSYOP,Icon.SOF_UNIT_PSYCHOLOGICAL_OPERATIONS_PSYOP),
           IconTuple(IconMenuName.SOF_UNIT_FIXED_WING_AVIATION,Icon.SOF_UNIT_FIXED_WING_AVIATION),
           IconTuple(IconMenuName.SOF_UNIT_CIVIL_AFFAIRS,Icon.SOF_UNIT_CIVIL_AFFAIRS),
           IconTuple(IconMenuName.SOF_UNIT_SUPPORT,Icon.SOF_UNIT_SUPPORT),
       )).toMutableList()

       list.add(0,
           OpenableMenuItem("-","Special operations forces", null, IconTuple(IconMenuName.SPECIAL_OPERATIONS_FORCES_SOF_UNIT,Icon.SPECIAL_OPERATIONS_FORCES_SOF_UNIT)){
                   vm, context -> vm.finishCreatingSymbol(IconTuple(IconMenuName.SPECIAL_OPERATIONS_FORCES_SOF_UNIT,Icon.SPECIAL_OPERATIONS_FORCES_SOF_UNIT),context)
           }
       )

       return list
    }


    /**
     * Method that gets level 1 lists based on [id]. Level 0 is basically battlefield dimension.
     * @param id Picked submenu id
     * @return [List]<[OpenableMenuItem]> with all elements of picked submenu or null if known code or level is passed
     */
    private fun getLvl0Lists(id: String) : List<OpenableMenuItem>?
    {
        return when(id) {
            "P" -> spaceList
            "A" -> airFirstLevel
            "G" -> groundLvl1()
            "S" -> seaSurfLvl1()
            "U" -> subSurfaceLvl1()
            "F" -> sofLvl1()
            "I" -> null
            "X" -> null
            else -> null
        }
    }

    /**
     * Method that gets level 2 lists based on [id].
     * @param id Picked submenu id
     * @return [List]<[OpenableMenuItem]> with all elements of picked submenu or null if known code or level is passed
     */
    private fun getLvl1Lists(id: String) : List<OpenableMenuItem>?
    {
        return when(id){
            "AMF" -> airFixedWing()
            "AMD" -> airDrone()
            "AMR" -> airRotaryWing()
            "AWM" -> airMissiles()
            "AC" -> airCiv()
            "GLU" -> groundUnitLvl2()
            "GLE" -> groundEquipmentLvl2()
            "GLI" -> groundInstalationLvl2()
            "SSCL" -> seaSurfSSCLLvl2()
            "SSCO" -> seaSurfSSCOLvl2()
            "SSNC" -> seaSurfSSNCLvl2()
            "SSNM" -> seaSurfSSNMLvl2()
            "US" -> subSurfaceUSLvl2()
            "UW" -> subSurfaceUWLvl2()
            "UNS" -> subSurfaceUNSLvl2()
            else -> null
        }
    }

    /**
     * Method that gets level 3 lists based on [id].
     * @param id Picked submenu id
     * @return [List]<[OpenableMenuItem]> with all elements of picked submenu or null if known code or level is passed
     */
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
            "EWML" -> groundEquipmentEWMLLvl3()
            "EWRG" -> groundEquipmentEWRGLvl3()
            "EWMH" -> groundEquipmentEWMHLvl3()
            "EVA" -> groundEquipmentEVALvl3()
            "EVUE" -> groundEquipmentEVUELvl3()
            "EVC" -> groundEquipmentEVCLvl3()
            "ESE" -> groundEquipmentESELvl3()
            else -> null
        }
    }

    /**
     * Method that gets level 4 lists based on [id].
     * @param id Picked submenu id
     * @return [List]<[OpenableMenuItem]> with all elements of picked submenu or null if known code or level is passed
     */
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

    /**
     * Method that gets correct submenu based on [level] depth and submenus [id]
     * @param id Submenu ID
     * @param level Depth of submenu
     * @return [List]<[OpenableMenuItem]> with all elements of submenu or null if known code or level is passed
     */
    fun getList( id: String, level: Int) : List<OpenableMenuItem>?
    {
        return when(level)
        {
            0 -> getLvl0Lists(id)
            1 -> getLvl1Lists(id)
            2 -> getLvl2Lists(id)
            3 -> getLvl3Lists(id)
            else -> return null
        }
    }
}