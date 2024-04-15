package com.example.jmb_bms.model.icons


enum class CodingScheme( override val character: Char): TopLevelMods2525 {
    WAR_FIGHT('S'),
    TACTICAL_GRAPH('G'),
    METOC('W'),
    INTELLIGENCE('I'),
    MAPPING('M'),
    OTHER('O')
}

enum class Affiliation(override val character: Char): TopLevelMods2525 {
    PENDING('P'),
    UNKNOWN('U'),
    ASSUMED_FRIEND('A'),
    FRIEND('F'),
    NEUTRAL('N'),
    SUSPECT('S'),
    HOSTILE('H'),
    JOKER('J'),
    FAKER('K'),
    OTHER('O')
}

enum class Cathegory(override val character: Char): TopLevelMods2525 {
    TASK('T'),
    C2('G'),
    MOBILITY('M'),
    FIRE_SUPPORT('F'),
    COMBAT_SERVICE_SUPPORT('S'),
    OTHER('O')
}

enum class BattleDimension(override val character: Char): TopLevelMods2525 {
    SPACE('P'),
    AIR('A'),
    GROUND('G'),
    SEA_SURFACE('S'),
    SEA_SUBSURFACE('U'),
    SOF('F'),
    OTHER('O'),
    NONE('-')
}

enum class Status(override val character: Char): TopLevelMods2525 {
    PRESENT('P'),
    ANTICIPATED('A')
}