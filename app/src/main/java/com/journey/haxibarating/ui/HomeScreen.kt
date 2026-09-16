package com.journey.haxibarating.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.journey.haxibarating.AppAccent
import com.journey.haxibarating.AppInk
import com.journey.haxibarating.AppMuted
import com.journey.haxibarating.AppSurface
import com.journey.haxibarating.data.Restaurant
import com.journey.haxibarating.data.ReviewRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: ReviewRepository,
    onAddRestaurant: () -> Unit,
    onOpenRestaurant: (Restaurant) -> Unit
) {
    val restaurants by repository.observeRestaurants().collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Haxiba Rating", color = AppInk) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRestaurant, containerColor = AppAccent, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add restaurant")
            }
        }
    ) { padding ->
        if (restaurants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "No restaurants yet.\nTap + to add the first one.",
                    textAlign = TextAlign.Center,
                    color = AppMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(restaurants, key = { it.id }) { restaurant ->
                    RestaurantRow(restaurant = restaurant, onClick = { onOpenRestaurant(restaurant) })
                }
            }
        }
    }
}

@Composable
private fun RestaurantRow(restaurant: Restaurant, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        RestaurantRowContent(restaurant)
    }
}

@Composable
private fun RestaurantRowContent(restaurant: Restaurant) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(restaurant.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, color = AppInk)
            if (restaurant.location.isNotBlank()) {
                Text(restaurant.location, style = MaterialTheme.typography.bodySmall, color = AppMuted)
            }
        }
        if (restaurant.ratingCount == 0L) {
            Text("Not yet rated", style = MaterialTheme.typography.bodySmall, color = AppMuted)
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "%.1f".format(restaurant.overallAverage),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = ScoreScale.colorFor(restaurant.overallAverage)
                )
                Text(
                    "%d rating%s".format(restaurant.ratingCount, if (restaurant.ratingCount == 1L) "" else "s"),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppMuted
                )
            }
        }
    }
}
