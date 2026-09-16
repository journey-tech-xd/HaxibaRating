package com.journey.haxibarating.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.journey.haxibarating.AppAccent
import com.journey.haxibarating.AppInk
import com.journey.haxibarating.AppSurface
import com.journey.haxibarating.data.Category
import com.journey.haxibarating.data.RATING_MAX
import com.journey.haxibarating.data.RATING_MIN
import com.journey.haxibarating.data.ReviewRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateScreen(
    repository: ReviewRepository,
    restaurantId: String,
    onSubmitted: () -> Unit,
    onBack: () -> Unit
) {
    val restaurant by repository.observeRestaurant(restaurantId).collectAsState(initial = null)

    var taste by remember { mutableIntStateOf(5) }
    var ambience by remember { mutableIntStateOf(5) }
    var quality by remember { mutableIntStateOf(5) }
    var service by remember { mutableIntStateOf(5) }
    var submitting by remember { mutableIntStateOf(0) } // 0 = idle, 1 = submitting
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Rate ${restaurant?.name ?: "…"}", color = AppInk) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            RatingSlider(Category.TASTE.label, taste) { taste = it }
            RatingSlider(Category.AMBIENCE.label, ambience) { ambience = it }
            RatingSlider(Category.QUALITY.label, quality) { quality = it }
            RatingSlider(Category.SERVICE.label, service) { service = it }

            Button(
                onClick = {
                    if (submitting == 1) return@Button
                    submitting = 1
                    scope.launch {
                        repository.submitRating(restaurantId, taste, ambience, quality, service)
                        submitting = 0
                        onSubmitted()
                    }
                },
                enabled = submitting == 0,
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(if (submitting == 1) "Submitting…" else "Submit rating")
            }
        }
    }
}

@Composable
private fun RatingSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    val color = ScoreScale.colorForInt(value)
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = AppInk)
            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = color
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = RATING_MIN.toFloat()..RATING_MAX.toFloat(),
            steps = RATING_MAX - RATING_MIN - 1,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0x1AFFFFFF)
            )
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
            Text("0", style = MaterialTheme.typography.labelSmall, color = com.journey.haxibarating.AppMuted)
            Text("10", style = MaterialTheme.typography.labelSmall, color = com.journey.haxibarating.AppMuted)
        }
    }
}
