package com.calisthenics.coach.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.calisthenics.coach.data.Exercise
import java.net.URLEncoder

/**
 * The only online feature: opens a video search in the phone's browser/YouTube app when the user taps the button.
 * The app itself never connects to the internet (it has no INTERNET permission).
 */
fun openVideo(context: Context, e: Exercise) {
    val q = URLEncoder.encode("${e.en} exercise proper form", "UTF-8")
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$q"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (ex: Exception) {
        Toast.makeText(context, "تعذر فتح الفيديو. تحتاج إنترنت ومتصفحًا.", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun VideoButton(e: Exercise, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    OutlinedButton(onClick = { openVideo(context, e) }, modifier = modifier) {
        Text("▶ شاهد فيديو توضيحي (يحتاج إنترنت)")
    }
}
