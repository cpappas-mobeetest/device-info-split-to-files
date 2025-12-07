package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import com.mobeetest.worker.ui.theme.deviceInfoLinkColor

@Composable
fun CpuInfoSpecialThanksRow() {
    val annotatedText = buildAnnotatedString {
        append("Special thanks: ")

        withLink(
            LinkAnnotation.Url(
                url = "https://github.com/pytorch/cpuinfo",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = deviceInfoLinkColor,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append("pytorch")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.heart),
            contentDescription = "Love",
            modifier = Modifier
                .size(18.dp)
                .padding(end = 6.dp),
            tint = Color.Unspecified
        )

        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
