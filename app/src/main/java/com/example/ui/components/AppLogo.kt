package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InkDark
import com.example.ui.theme.ParchiPurplePrimary
import com.example.ui.theme.ParchiPurpleSupporting

private val fillPathData = "M4 2.99674C4 2.73153 4.10536 2.47717 4.29289 2.28964C4.48043 2.1021 4.73478 1.99674 5 1.99674C5.24762 1.99538 5.49048 2.06477 5.7 2.19674L6.633 2.79674C6.84204 2.93032 7.08493 3.0013 7.333 3.0013C7.58107 3.0013 7.82396 2.93032 8.033 2.79674L8.967 2.19674C9.17604 2.06317 9.41893 1.99219 9.667 1.99219C9.91507 1.99219 10.158 2.06317 10.367 2.19674L11.3 2.79674C11.509 2.93032 11.7519 3.0013 12 3.0013C12.2481 3.0013 12.491 2.93032 12.7 2.79674L13.633 2.19674C13.842 2.06317 14.0849 1.99219 14.333 1.99219C14.5811 1.99219 14.824 2.06317 15.033 2.19674L15.967 2.79674C16.176 2.93032 16.4189 3.0013 16.667 3.0013C16.9151 3.0013 17.158 2.93032 17.367 2.79674L18.3 2.19674C18.5095 2.06477 18.7524 1.99538 19 1.99674C19.2652 1.99674 19.5196 2.1021 19.7071 2.28964C19.8946 2.47717 20 2.73153 20 2.99674V20.9967C20 21.262 19.8946 21.5163 19.7071 21.7038C19.5196 21.8914 19.2652 21.9967 19 21.9967C18.7524 21.9981 18.5095 21.9287 18.3 21.7967L17.367 21.1967C17.158 21.0632 16.9151 20.9922 16.667 20.9922C16.4189 20.9922 16.176 21.0632 15.967 21.1967L15.033 21.7967C14.824 21.9303 14.5811 22.0013 14.333 22.0013C14.0849 22.0013 9.17604 21.9303 8.967 21.7967L8.033 21.1967C7.82396 21.0632 7.58107 20.9922 7.333 20.9922C7.08493 20.9922 6.84204 21.0632 6.633 21.1967L5.7 21.7967C5.49048 21.9287 5.24762 21.9981 5 21.9967C4.73478 21.9967 4.48043 21.8914 4.29289 21.7038C4.10536 21.5163 4 21.262 4 20.9967V2.99674Z"
private val strokePathData = "M13 16H8M14 8H8M16 12H8"

val AppLogoVector: ImageVector by lazy {
    val fillNodes = PathParser().parsePathString(fillPathData).toNodes()
    val strokeNodes = PathParser().parsePathString(strokePathData).toNodes()
    ImageVector.Builder(
        name = "AppLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = fillNodes,
        fill = SolidColor(ParchiPurplePrimary) // #E7BBFF
    ).addPath(
        pathData = strokeNodes,
        stroke = SolidColor(ParchiPurpleSupporting), // #A500FF
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ).build()
}

/**
 * App Logo rendering the exact thermal receipt vector SVG provided by the user
 */
@Composable
fun AppLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    fillColor: Color = ParchiPurplePrimary,
    strokeColor: Color = ParchiPurpleSupporting
) {
    Icon(
        imageVector = AppLogoVector,
        contentDescription = "App Logo",
        tint = Color.Unspecified,
        modifier = modifier.size(size)
    )
}

/**
 * App Header Logo with Title
 */
@Composable
fun AppHeaderBrand(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLogoIcon(size = 28.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.1.sp,
            color = InkDark
        )
    }
}
