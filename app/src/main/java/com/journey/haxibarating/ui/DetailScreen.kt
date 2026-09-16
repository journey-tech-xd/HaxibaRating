package com.journey.haxibarating.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.journey.haxibarating.AppAccent
import com.journey.haxibarating.AppAccentInk
import com.journey.haxibarating.AppInk
import com.journey.haxibarating.AppMuted
import com.journey.haxibarating.AppSurface
import com.journey.haxibarating.data.ReviewRepository
import com.journey.haxibarating.ui.components.AverageBarChart
import com.journey.haxibarating.ui.components.BarDatum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    repository: ReviewRepository,
    restaurantId: String,
    onRate: () -> Unit,
    onBack: () -> Unit
) {
    val restaurant by repository.observeRestaurant(restaurantId).collectAsState(initial = null)
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(restaurant?.name ?: "…", color = AppInk) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppAccent)
                    }
                },
                actions = {
                    IconButton(onClick = onRate) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit rating", tint = AppAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppSurface)
            )
        }
    ) { padding ->
        val r = restaurant
        if (r == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                Text("Loading…", color = AppMuted)
            }
            return@Scaffold
        }
        val hasData = r.ratingCount > 0
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            if (r.location.isNotBlank()) {
                Text(r.location, color = AppMuted)
            }

            Text(
                text = if (hasData) "%.1f / 10 · %s".format(r.overallAverage, ScoreScale.labelFor(r.overallAverage)) else "–",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (hasData) ScoreScale.colorFor(r.overallAverage) else AppMuted,
                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
            )
            Text(
                if (hasData)
                    "Based on ${r.ratingCount} rating${if (r.ratingCount == 1L) "" else "s"} from everyone who has this app"
                else
                    "No ratings yet",
                style = MaterialTheme.typography.bodySmall,
                color = AppMuted
            )

            AverageBarChart(
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                hasData = hasData,
                data = listOf(
                    BarDatum("Taste", r.avgTaste, Color(0xFFFF8B5E)),
                    BarDatum("Ambience", r.avgAmbience, Color(0xFF5AA9FF)),
                    BarDatum("Quality", r.avgQuality, Color(0xFF69D68B)),
                    BarDatum("Service", r.avgService, Color(0xFFF2C245))
                )
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = AppAccent, contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
            }
            if (r.mapsUrl.isNotBlank()) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.mapsUrl)))
                    },
                    border = BorderStroke(1.5.dp, Color(0x734E8CFF)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccentInk),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Open in Google Maps")
                }
            }
        }
    }
}
