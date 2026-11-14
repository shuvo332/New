package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanVibrant
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.MintGlow
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TealDeep
import com.example.ui.theme.TealOcean

val BrandTealGreenGradient: Brush
    get() = Brush.linearGradient(
        colors = listOf(TealDeep, TealOcean, EmeraldVibrant)
    )

val BrandHeroGradient: Brush
    get() = Brush.linearGradient(
        colors = listOf(CyanVibrant, TealOcean, EmeraldDeep)
    )

val BrandButtonGradient: Brush
    get() = Brush.horizontalGradient(
        colors = listOf(TealOcean, EmeraldVibrant)
    )

@Composable
fun FluidTealGreenBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        TealDeep,
                        TealOcean,
                        EmeraldDeep
                    )
                )
            )
            .drawBehind {
                // Organic fluid shapes echoing the requested teal/cyan/green atmosphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CyanVibrant.copy(alpha = 0.34f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.10f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.85f, size.height * 0.10f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MintGlow.copy(alpha = 0.24f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.12f, size.height * 0.34f),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.12f, size.height * 0.34f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmeraldVibrant.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.78f, size.height * 0.82f),
                        radius = size.width * 0.72f
                    ),
                    radius = size.width * 0.72f,
                    center = Offset(size.width * 0.78f, size.height * 0.82f)
                )
            },
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    containerColor: Color = GlassWhite,
    borderColor: Color = GlassBorder,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        contentColor = PureWhite,
        content = content
    )
}
