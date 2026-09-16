package com.journey.haxibarating

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.journey.haxibarating.data.ReviewRepository
import com.journey.haxibarating.ui.AddRestaurantScreen
import com.journey.haxibarating.ui.DetailScreen
import com.journey.haxibarating.ui.HomeScreen
import com.journey.haxibarating.ui.RateScreen
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {

    private val repository = ReviewRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HaxibaRatingApp(repository)
        }
    }
}

// Black/dark-navy palette -- the app's own fixed look, same on every phone
// regardless of the system theme (there's no light mode here, by design).
val AppBg1 = Color(0xFF04060B)
val AppBg2 = Color(0xFF070B16)
val AppSurface = Color(0xE6131829) // ~90% opaque dark navy card
val AppSurfaceSolid = Color(0xFF121729)
val AppInk = Color(0xFFF1F4FA)
val AppMuted = Color(0xFF8D96AC)
val AppLine = Color(0x1AFFFFFF)
val AppAccent = Color(0xFF4E8CFF)
val AppAccentInk = Color(0xFFBFD6FF)

private val AppDarkColors = darkColorScheme(
    primary = AppAccent,
    onPrimary = Color.White,
    secondary = AppAccent,
    background = AppBg1,
    onBackground = AppInk,
    surface = AppSurfaceSolid,
    onSurface = AppInk,
    surfaceVariant = AppSurface,
    onSurfaceVariant = AppMuted
)

@Composable
fun HaxibaRatingApp(repository: ReviewRepository) {
    MaterialTheme(colorScheme = AppDarkColors) {
        Surface(modifier = Modifier.fillMaxSize(), color = AppBg1) {
            Box(modifier = Modifier.fillMaxSize()) {
                MovingBackground()

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            repository = repository,
                            onAddRestaurant = { navController.navigate("add") },
                            onOpenRestaurant = { restaurant ->
                                // Nothing rated yet? Skip the empty detail
                                // page and go straight to rating it.
                                if (restaurant.ratingCount > 0) {
                                    navController.navigate("detail/${restaurant.id}")
                                } else {
                                    navController.navigate("rate/${restaurant.id}")
                                }
                            }
                        )
                    }
                    composable("add") {
                        AddRestaurantScreen(
                            repository = repository,
                            onSaved = { id ->
                                // Brand new restaurant: always straight to rating it.
                                navController.navigate("rate/$id") {
                                    popUpTo("home")
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = "detail/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: return@composable
                        DetailScreen(
                            repository = repository,
                            restaurantId = id,
                            onRate = { navController.navigate("rate/$id") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = "rate/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: return@composable
                        RateScreen(
                            repository = repository,
                            restaurantId = id,
                            onSubmitted = {
                                // Always land on the (now populated) detail
                                // screen, whichever way we got here.
                                navController.navigate("detail/$id") {
                                    popUpTo("home")
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

/** Three soft, slowly drifting patches of navy/near-black light -- the
 *  "keeps moving in the background" look, drawn cheaply as gradients
 *  rather than a real blur (which would need API 31+). */
@Composable
private fun MovingBackground() {
    val transition = rememberInfiniteTransition(label = "bg-drift")
    val t1 by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(32000, easing = LinearEasing), RepeatMode.Restart),
        label = "t1"
    )
    val t2 by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing), RepeatMode.Restart),
        label = "t2"
    )
    val t3 by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(48000, easing = LinearEasing), RepeatMode.Restart),
        label = "t3"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        fun blob(cx: Float, cy: Float, radius: Float, color: Color) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0f)),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )
        }

        blob(
            cx = w * 0.25f + cos(t1) * w * 0.18f,
            cy = h * 0.15f + sin(t1) * h * 0.10f,
            radius = w * 0.7f,
            color = Color(0xFF0B3A63)
        )
        blob(
            cx = w * 0.8f + cos(t2) * w * 0.15f,
            cy = h * 0.35f + sin(t2) * h * 0.12f,
            radius = w * 0.6f,
            color = Color(0xFF14264D)
        )
        blob(
            cx = w * 0.35f + cos(t3) * w * 0.12f,
            cy = h * 0.85f + sin(t3) * h * 0.10f,
            radius = w * 0.55f,
            color = Color(0xFF0A1730)
        )
    }
}
