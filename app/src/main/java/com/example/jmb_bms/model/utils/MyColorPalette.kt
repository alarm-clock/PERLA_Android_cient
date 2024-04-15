package com.example.jmb_bms.model.utils

import androidx.compose.ui.graphics.Color

data class MyColorPalette(
    val primary: Color,
    val onPrimary: Color,
    val inversePrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val outline: Color,
    val outlineVariant: Color,

    val onSurface: Color,

    val enabledButton: Color,
    val disabledButton: Color,

    val loading: Color,
    val wrappingBox: Color,
    val darkerWrappingBox: Color,

    val pickOneBtnFromManyEn: Color,
    val pickOneBtnFromManyDis: Color,

    val pickOneBtnFromManyInsideEn: Color,
    val pickOneBtnFromManyInsideDis: Color,

    val error: Color,

    val lightCian: Color =  Color(0,126,126),

)
