package com.mobeetest.worker.ui.activities.main.pages.composables

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.mobeetest.worker.sharedPreferences.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun FileCreatedDialog(
    fileName: String,
    iconRes: Int,
    mimeType: String,
    uri: Uri,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsKey = booleanPreferencesKey("file_dialog_auto_open")

    var dontShowAgain by remember { mutableStateOf(false) }
    var userPreferenceChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        if (prefs[prefsKey] == true) {
            val openIntent = if (mimeType.equals("image/png", true)) {
                buildChooserExcludingPhotos(context, uri, mimeType, "Open with")
            } else {
                Intent.createChooser(
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "Open with"
                )
            }
            context.startActivity(openIntent)
            onDismiss()
        }
    }


    if (userPreferenceChecked) {
        Toast.makeText(context, "File created", Toast.LENGTH_SHORT).show()
        return
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .widthIn(min = 300.dp, max = 500.dp)
                .padding(24.dp)
                .border(1.dp, Color.Black, shape = RoundedCornerShape(12.dp))
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 6.dp)
                            .graphicsLayer { alpha = 0.7f }
                    )
                    Text("File Created", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "The file \"$fileName\" has been created successfully. What would you like to do next?",
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.toggleable(
                        value = dontShowAgain,
                        onValueChange = { dontShowAgain = it }
                    )
                ) {
                    Checkbox(checked = dontShowAgain, onCheckedChange = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Don't show again")
                }

                Spacer(modifier = Modifier.height(10.dp))


                val modifier = Modifier.fillMaxWidth().height(48.dp)

                Button(
                    onClick = {
                        if (dontShowAgain) {
                            scope.launch { context.dataStore.edit { it[prefsKey] = true } }
                        }
                        val chooser = if (mimeType.equals("image/png", true)) {
                            buildChooserExcludingPhotos(context, uri, mimeType, "Open with")
                        } else {
                            Intent.createChooser(
                                Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, mimeType)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                },
                                "Open with"
                            )
                        }
                        context.startActivity(chooser)
                        onDismiss()



                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = modifier
                ) {
                    Text("Open", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = mimeType
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share with"))
                        //Toast.makeText(context, "Share dialog shown", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = modifier
                ) {
                    Text("Share", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        //Toast.makeText(context, "Action cancelled", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = modifier
                ) {
                    Text("Continue", color = Color.White)
                }
            }
        }
    }
}



private fun Intent.withChooserExcluding(
    context: android.content.Context,
    excludePackages: Set<String>,
    title: String
): Intent {
    val pm = context.packageManager
    val matches = pm.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY)

    // Κράτα όλα τα components που ΔΕΝ ανήκουν στα excluded packages
    val allowed = matches.filter { it.activityInfo?.packageName !in excludePackages }

    // Αν δεν έμεινε τίποτα, γύρνα απλό chooser (fallback)
    if (allowed.isEmpty()) {
        return Intent.createChooser(this, title)
    }

    // Εξαίρεσε όλα τα components από τα excluded packages
    val toExclude = matches
        .filter { it.activityInfo?.packageName in excludePackages }
        .map { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }
        .toTypedArray()

    return Intent.createChooser(this, title).apply {
        putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, toExclude)
    }
}


private fun Intent.withChooserExcludingStrict(
    context: android.content.Context,
    excludePackages: Set<String>,
    title: String,
    minChoices: Int = 3 // αν μείνουν λιγότερες από 3, γύρνα σε default chooser
): Intent {
    val pm = context.packageManager
    val base = this

    // Πάρε όλους τους υποψήφιους viewers για το συγκεκριμένο data+type
    val matches = pm.queryIntentActivities(base, 0)

    // Κράτα όσους ΔΕΝ είναι στο exclude set
    val allowed = matches.filter { it.activityInfo?.packageName !in excludePackages }

    // Αν λίγοι, ΜΗΝ περιορίσεις τις επιλογές του χρήστη
    if (allowed.size < minChoices) {
        // Best-effort exclude (σε κάποιες ROMs αγνοείται, αλλά δεν κόβουμε επιλογές)
        val toExclude = matches
            .filter { it.activityInfo?.packageName in excludePackages }
            .map { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }
            .toTypedArray()

        return Intent.createChooser(base, title).apply {
            putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, toExclude)
        }
    }

    // Strict whitelist: μόνο οι επιτρεπόμενοι
    val targetIntents = allowed.map { ri ->
        Intent(base).apply {
            component = ComponentName(ri.activityInfo.packageName, ri.activityInfo.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }.toMutableList()

    val primary = targetIntents.removeAt(0)
    return Intent.createChooser(primary, title).apply {
        if (targetIntents.isNotEmpty()) {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray())
        }
    }
}


private fun collectImageViewers(
    context: android.content.Context,
    uri: Uri,
    mimeType: String
): List<android.content.pm.ResolveInfo> {
    val pm = context.packageManager

    // Δοκίμασε διαφορετικά σενάρια match για να βρούμε περισσότερα apps
    val probes = listOf(
        Intent(Intent.ACTION_VIEW).setDataAndType(uri, mimeType),
        Intent(Intent.ACTION_VIEW).setType(mimeType),
        Intent(Intent.ACTION_VIEW).setType("image/*"),
        // generic content uri probe βοηθάει apps με δηλώσεις σε image/* + content scheme
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(Uri.parse("content://media/external/images/media/1"), "image/*")
    ).map { it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }

    val out = LinkedHashMap<String, android.content.pm.ResolveInfo>()
    for (probe in probes) {
        val matches = pm.queryIntentActivities(probe, 0)
        for (ri in matches) {
            val key = "${ri.activityInfo.packageName}/${ri.activityInfo.name}"
            out.putIfAbsent(key, ri)
        }
    }
    return out.values.toList()
}



private fun buildChooserExcludingPhotos(
    context: android.content.Context,
    uri: Uri,
    mimeType: String,
    title: String,
    minChoices: Int = 3
): Intent {
    val base = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val exclude = setOf("com.google.android.apps.photos") // + "com.google.android.apps.photosgo" αν θέλεις

    // Μάζεψε υποψήφιους από πολλά queries
    val all = collectImageViewers(context, uri, mimeType)

    // Φίλτρο: βγάλε Photos, κράτα μοναδικούς
    val allowed = all.filter { it.activityInfo?.packageName !in exclude }

    // Αν μετά το exclude μείναμε με λίγους, γύρνα στον default chooser (χωρίς σκληρό exclude)
    if (allowed.size < minChoices) {
        return Intent.createChooser(base, title)
    }

    // Χτίσε explicit intents μόνο για τους επιτρεπόμενους
    val targets = allowed.map { ri ->
        Intent(base).apply {
            component = ComponentName(ri.activityInfo.packageName, ri.activityInfo.name)
        }
    }.toMutableList()

    val primary = targets.removeAt(0)
    return Intent.createChooser(primary, title).apply {
        if (targets.isNotEmpty()) {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, targets.toTypedArray())
        }
    }
}

