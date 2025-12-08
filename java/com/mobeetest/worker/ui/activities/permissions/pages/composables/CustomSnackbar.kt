package com.mobeetest.worker.ui.activities.permissions.pages.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import com.mobeetest.worker.R

@Composable
fun CustomSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .shadow(elevation = 4.dp, shape = shape)
            .background(
                color = androidx.compose.ui.graphics.Color.White,
                shape = shape
            )
            .border(
                width = 2.dp,
                color = androidx.compose.ui.graphics.Color(0xFF1E90FF), // Μπλε ηλεκτρίκ
                shape = shape
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Αριστερά: Το λογότυπο (λίγο πιο compact)
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(28.dp)
                .padding(end = 4.dp)
        )

        // Κέντρο: Το κείμενο με justify και λίγο μικρότερο padding
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )

        // Δεξιά: Κουμπί κλεισίματος (ελαφρώς μικρότερο)
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Close"
            )
        }
    }
}

/*
@Composable
fun ShowCustomSnackbar(message: String) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    SnackbarHost(hostState = snackbarHostState) { data ->
        CustomSnackbar(
            message = data.visuals.message,
            onDismiss = { data.dismiss() }
        )
    }

    LaunchedEffect(Unit) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
}

 */

@Preview(showBackground = true, widthDp = 360)
@Composable
fun CustomSnackbarPreview() {
    MaterialTheme {
        CustomSnackbar(
            message = "Αυτό είναι ένα δοκιμαστικό μήνυμα πολύ μεγάλης διάρκειας ώστε " +
                    "να σπάσει αυτόματα σε πολλές γραμμές και να δεις ότι κάνει wrap χωρίς να γράψεις \\n.",
            onDismiss = {}
        )
    }
}
