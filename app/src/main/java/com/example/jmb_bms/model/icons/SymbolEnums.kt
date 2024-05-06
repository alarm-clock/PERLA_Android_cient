/**
 * @file: SymbolEnums.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing enum classes for top level symbol modifiers
 */
package com.example.jmb_bms.model.icons

/**
 * Enum Class for coding scheme (first character)
 * @param character [Char] representing coding scheme
 */
enum class CodingScheme( override val character: Char): TopLevelMods2525 {
    WAR_FIGHT('S'),
    TACTICAL_GRAPH('G'),
    METOC('W'),
    INTELLIGENCE('I'),
    MAPPING('M'),
    OTHER('O')
}

/**
 * Enum Class for affiliation (second character)
 * @param character [Char] representing affiliation
 */
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

/**
 * Enum Class for category (second character if [CodingScheme.TACTICAL_GRAPH] is picked)
 * @param character [Char] representing category scheme
 */
enum class Cathegory(override val character: Char): TopLevelMods2525 {
    TASK('T'),
    C2('G'),
    MOBILITY('M'),
    FIRE_SUPPORT('F'),
    COMBAT_SERVICE_SUPPORT('S'),
    OTHER('O')
}

/**
 * Enum Class for battle dimension (third character)
 * @param character [Char] representing battle dimension
 */
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

/**
 * Enum Class for status (fourth character)
 * @param character [Char] representing status
 */
enum class Status(override val character: Char): TopLevelMods2525 {
    PRESENT('P'),
    ANTICIPATED('A')
}