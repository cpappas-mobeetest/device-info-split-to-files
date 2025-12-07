package com.mobeetest.worker.ui.activities.main.pages.composables.device

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal fun PlayGifAtLeastWhileInProgress(
    @DrawableRes resId: Int,
    @Suppress("SameParameterValue") minPlays: Int,
    inProgress: Boolean,
    modifier: Modifier = Modifier,
    @Suppress("SameParameterValue") contentDescription: String? = null,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val onFinishedState = rememberUpdatedState(onFinished)
    val inProgressState = rememberUpdatedState(inProgress)
    val minPlaysState = rememberUpdatedState(minPlays)

    var playsDone by remember(resId) { mutableIntStateOf(0) }

    val drawable = remember(resId) {
        val source = ImageDecoder.createSource(context.resources, resId)
        ImageDecoder.decodeDrawable(source) as? AnimatedImageDrawable
    }

    // When a new update starts, reset the counter
    LaunchedEffect(inProgress) {
        if (inProgress) playsDone = 0
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageDrawable(drawable)
                contentDescription?.let { this.contentDescription = it }
            }
        },
        update = { imageView ->
            imageView.setImageDrawable(drawable)
        }
    )

    DisposableEffect(drawable) {
        if (drawable == null) {
            onFinishedState.value()
            onDispose { }
        } else {
            // Play ONCE per start and at the end decide if we restart
            drawable.repeatCount = 0

            val callback = object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(d: Drawable?) {
                    playsDone++

                    val shouldContinue =
                        inProgressState.value || playsDone < minPlaysState.value

                    if (shouldContinue) {
                        drawable.start() // another full loop
                    } else {
                        onFinishedState.value()
                    }
                }
            }

            drawable.registerAnimationCallback(callback)
            drawable.start()

            onDispose {
                drawable.unregisterAnimationCallback(callback)
                drawable.stop()
            }
        }
    }
}
