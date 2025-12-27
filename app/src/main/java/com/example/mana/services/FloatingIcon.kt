package com.example.mana.services

import androidx.annotation.RawRes
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import kotlin.math.roundToInt

@Composable
fun FloatingIcon(
    onDrag: (IntOffset) -> Unit,
    onTap: () -> Unit,
    @RawRes resId: Int
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Raw(resId))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = Modifier
            .size(72.dp) // A bit larger for animations
            .pointerInput(Unit) { detectTapGestures(onTap = { onTap() }) }
            .pointerInput(Unit) { detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(IntOffset(dragAmount.x.roundToInt(), dragAmount.y.roundToInt()))
                }
            }
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
        )
    }
}
