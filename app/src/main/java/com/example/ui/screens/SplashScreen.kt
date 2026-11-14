package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.FluidTealGreenBackdrop
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.MintGlow
import com.example.ui.theme.PureWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val scale = remember { Animatable(0.82f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450)
            )
        }
        delay(1150L)
        onFinished()
    }

    FluidTealGreenBackdrop {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            ) {
                Surface(
                    shape = CircleShape,
                    color = GlassWhite,
                    border = BorderStroke(2.dp, PureWhite.copy(alpha = 0.45f)),
                    modifier = Modifier.size(116.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(14.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon_fg),
                            contentDescription = stringResource(R.string.app_full_title),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.app_full_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = PureWhite,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = PureWhite.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = stringResource(R.string.app_tagline),
                        style = MaterialTheme.typography.labelLarge,
                        color = MintGlow,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MintGlow,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}
