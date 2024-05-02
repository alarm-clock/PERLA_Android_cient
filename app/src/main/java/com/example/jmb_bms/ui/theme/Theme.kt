package com.example.jmb_bms.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.jmb_bms.model.utils.MyColorPalette

val darkCianColor = Color(0,90,90)
val darkGrey20 =  Color(20,20,20)
val darkRed25 = Color(25,0,0)
val darkOrange = Color(130,20,0)

val DarkColorScheme = MyColorPalette(
    primary = Color.Black,
    onPrimary = darkCianColor,
    inversePrimary = Color(10,10,10),
    secondary = Color.Black,//Color(0,129,126),
    onSecondary = darkCianColor,
    outline = darkCianColor,
    outlineVariant = darkCianColor,
    tertiary = Color.Black,
    onTertiary = Color.DarkGray,
    onSurface = Color.Black,
    loading = darkCianColor,
    enabledButton = Color.Black,
    disabledButton = Color.Black,
    wrappingBox = darkGrey20,
    darkerWrappingBox = Color.Black,
    pickOneBtnFromManyEn = Color.Black,
    pickOneBtnFromManyDis = darkCianColor,
    pickOneBtnFromManyInsideEn = darkCianColor,
    pickOneBtnFromManyInsideDis = Color.Black,
    error = darkRed25
)

val LightColorScheme = MyColorPalette(
    primary = Color.White,
    onPrimary = Color.Black,
    inversePrimary = Color.LightGray,
    secondary = Color.Blue,
    onSecondary = Color.White,
    outline = Color.Blue,
    outlineVariant = Color.LightGray,
    tertiary = Color.Cyan,
    onTertiary = Color.Black,
    onSurface = Color.Black,
    loading = Purple80,
    enabledButton = Purple80,
    disabledButton = Color.Gray,
    wrappingBox = Color.LightGray,
    darkerWrappingBox = Color.LightGray,
    pickOneBtnFromManyEn = Color(47,141,254),
    pickOneBtnFromManyDis = Color(52,204,254),
    pickOneBtnFromManyInsideEn = Color.White,
    pickOneBtnFromManyInsideDis = Color.White,
    error = Color.Red
)

val DarkMenuItemsColors = MenuItemColors(
    textColor = darkCianColor,
    leadingIconColor = darkCianColor,
    trailingIconColor = darkCianColor,
    disabledTextColor = darkGrey20,
    disabledLeadingIconColor = darkGrey20,
    disabledTrailingIconColor = darkGrey20
)

val LocalTheme = compositionLocalOf { DarkColorScheme }
val LocalTextfieldTheme = compositionLocalOf<TextFieldColors?>{ null }
val LocalCheckBoxTheme = compositionLocalOf<CheckboxColors?> { null }
val LocalSwitchTheme = compositionLocalOf<SwitchColors?> { null }
val LocalMenuItemsTheme = compositionLocalOf { DarkMenuItemsColors }
@Composable
fun TestTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit){
    val colors = if(darkTheme) DarkColorScheme else LightColorScheme
    val textFieldColors = if(darkTheme)
        TextFieldDefaults.colors(
            focusedIndicatorColor = Color(0,100,100),
            focusedContainerColor = Color.Black,
            focusedLabelColor = Color(0,100,100),
            focusedTextColor = darkCianColor,

            unfocusedIndicatorColor = darkCianColor,
            unfocusedContainerColor = Color.Black,
            unfocusedLabelColor = darkCianColor,
            unfocusedTextColor = darkCianColor,

            disabledTextColor = darkCianColor,
            disabledContainerColor = Color.Black,
            disabledIndicatorColor = darkCianColor,
            disabledLabelColor = darkCianColor,

            cursorColor = darkCianColor
        )
    else TextFieldDefaults.colors()

    val checkboxColors = if(darkTheme)
        CheckboxDefaults.colors(
            checkedColor = darkCianColor,
            uncheckedColor = darkGrey20,
            checkmarkColor = Color.Black,
            disabledCheckedColor = darkGrey20,
            disabledUncheckedColor = darkGrey20,
            disabledIndeterminateColor = darkGrey20
        )
    else
        CheckboxDefaults.colors()

    val menuItemColors = if (darkTheme) DarkMenuItemsColors else MenuDefaults.itemColors()

    val switchColors = if(darkTheme)
        SwitchDefaults.colors(
            checkedThumbColor = darkGrey20,
            checkedTrackColor = darkCianColor,
            checkedBorderColor = Color(10,10,10),
            checkedIconColor = darkCianColor,
            uncheckedThumbColor = darkGrey20,
            uncheckedTrackColor = Color.Black,
            uncheckedBorderColor = darkCianColor,
            uncheckedIconColor = darkGrey20,
            disabledCheckedBorderColor = darkGrey20,
            disabledCheckedThumbColor = darkGrey20,
            disabledCheckedTrackColor = Color.Black,
            disabledCheckedIconColor = Color.Black,
            disabledUncheckedBorderColor = darkGrey20,
            disabledUncheckedThumbColor = Color(10,10,10),
            disabledUncheckedTrackColor = Color.Black,
            disabledUncheckedIconColor = Color.Black
        )
    else SwitchDefaults.colors()

    CompositionLocalProvider(
        LocalTheme provides colors,
        LocalTextfieldTheme provides textFieldColors,
        LocalMenuItemsTheme provides menuItemColors,
        LocalCheckBoxTheme provides checkboxColors,
        LocalSwitchTheme provides switchColors
    ){
        content()
    }

}
