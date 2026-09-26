package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 3D Thermal Printer Dispenser based on the reference design.
 * Features the realistic printer opening slot from which the crisp paper receipt feeds out.
 */
@Composable
fun ThermalPrinterDispenser(
    modifier: Modifier = Modifier,
    paperContent: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3D Thermal Printer Slot Machine Bar at the top
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(34.dp)
                .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color(0x40000000))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF22242A),
                            Color(0xFF15161A),
                            Color(0xFF0B0C0E)
                        )
                    )
                )
                .border(1.5.dp, Color(0xFF3B3E48), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Metallic glossy highlight line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .height(2.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 3.dp)
                    .background(Color(0x33FFFFFF))
            )

            // Inner 3D hollow dark dispenser slit
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(10.dp)
                    .shadow(4.dp, RoundedCornerShape(5.dp))
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF050507))
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(5.dp))
            )
        }

        // Crisp receipt paper feeding out from the 3D slot
        Box(
            modifier = Modifier
                .offset(y = (-10).dp)
                .fillMaxWidth(0.90f)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 6.dp, bottomEnd = 6.dp),
                    ambientColor = Color(0x33000000),
                    spotColor = Color(0x40000000)
                )
                .background(Color(0xFFFFFDF9))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, start = 18.dp, end = 18.dp, bottom = 12.dp)
            ) {
                paperContent()

                Spacer(modifier = Modifier.height(14.dp))

                // Serrated thermal paper tear edge at the bottom
                ThermalPaperTearEdge(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                )
            }
        }
    }
}

/**
 * Zig-zag perforated serrated cut at the bottom of thermal receipts.
 */
@Composable
fun ThermalPaperTearEdge(
    modifier: Modifier = Modifier,
    teethCount: Int = 26,
    color: Color = Color(0xFFF1F0EC)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val toothWidth = width / teethCount

        val path = Path().apply {
            moveTo(0f, 0f)
            for (i in 0 until teethCount) {
                val startX = i * toothWidth
                val midX = startX + (toothWidth / 2f)
                val endX = (i + 1) * toothWidth

                lineTo(midX, height)
                lineTo(endX, 0f)
            }
            close()
        }

        drawPath(path = path, color = color)
    }
}
