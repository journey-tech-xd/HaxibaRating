package com.journey.haxibarating.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.journey.haxibarating.AppAccent
import com.journey.haxibarating.AppInk
import com.journey.haxibarating.AppMuted
import com.journey.haxibarating.AppSurface
import com.journey.haxibarating.data.ReviewRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRestaurantScreen(
    repository: ReviewRepository,
    onSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Add a restaurant", color = AppInk) },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Restaurant name") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )
            Spacer(modifier = Modifier.padding(6.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (city, neighborhood, address…)") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )
            Text(
                "We'll build a Google Maps search link from this automatically — no API key needed.",
                style = MaterialTheme.typography.bodySmall,
                color = AppMuted,
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
            )
            Button(
                onClick = {
                    if (name.isBlank() || saving) return@Button
                    saving = true
                    scope.launch {
                        val id = repository.addRestaurant(name.trim(), location.trim())
                        saving = false
                        onSaved(id)
                    }
                },
                enabled = name.isNotBlank() && !saving,
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (saving) "Saving…" else "Save and rate it")
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppInk,
    unfocusedTextColor = AppInk,
    focusedBorderColor = AppAccent,
    unfocusedBorderColor = Color(0x33FFFFFF),
    focusedLabelColor = AppAccent,
    unfocusedLabelColor = AppMuted,
    cursorColor = AppAccent
)
