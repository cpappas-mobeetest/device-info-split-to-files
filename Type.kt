package com.mobeetest.worker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// ===============================
// BASE TYPOGRAPHY (Material3)
// ===============================

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,       // Roboto
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

// ===============================
// PERMISSIONS ACTIVITY TYPOGRAPHY
// ===============================

// Top Bar
val permissionsScaffoldTopBarTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        textAlign = TextAlign.Center
    )
)

val permissionsScaffoldContentProgressMessageTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        textAlign = TextAlign.Justify,
        color = bootStrapAlertSuccessTextColor
    )
)

val permissionsScaffoldContentInfoMessageTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        color = bootStrapInfoSuccessTextColor
    )
)

// ===============================
// MAIN ACTIVITY / SCAFFOLD TYPOGRAPHY
// ===============================

val mainScaffoldTopBarTitleTextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 23.sp,
    textAlign = TextAlign.Center
)

