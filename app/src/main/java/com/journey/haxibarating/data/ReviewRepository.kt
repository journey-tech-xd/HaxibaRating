package com.journey.haxibarating.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder

/**
 * All reads/writes go through this one shared Firestore project, which is
 * what makes every phone that installs this app "cross data": everyone's
 * copy of the app points at the same Firebase project (via google-services.json),
 * so a rating typed on one phone shows up as an updated average on every
 * other phone within a second or two, with no app update and no server to run.
 */
class ReviewRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val restaurants = db.collection("restaurants")

    /** Live list of every restaurant, newest first, updating in real time. */
    fun observeRestaurants(): Flow<List<Restaurant>> = callbackFlow {
        val registration = restaurants
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toRestaurant() } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    /** Live updates for a single restaurant (used by the detail/graph screen). */
    fun observeRestaurant(id: String): Flow<Restaurant?> = callbackFlow {
        val registration = restaurants.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toRestaurant())
        }
        awaitClose { registration.remove() }
    }

    suspend fun addRestaurant(name: String, location: String): String {
        val query = URLEncoder.encode("$name $location", "UTF-8")
        val mapsUrl = "https://www.google.com/maps/search/?api=1&query=$query"
        val data = hashMapOf(
            "name" to name,
            "location" to location,
            "mapsUrl" to mapsUrl,
            "createdAt" to System.currentTimeMillis(),
            "ratingCount" to 0L,
            "sumTaste" to 0L,
            "sumAmbience" to 0L,
            "sumQuality" to 0L,
            "sumService" to 0L
        )
        val ref = restaurants.add(data).await()
        return ref.id
    }

    /** Each param is a plain 0..10 rating (see [RATING_MIN]/[RATING_MAX]). */
    suspend fun submitRating(
        restaurantId: String,
        taste: Int,
        ambience: Int,
        quality: Int,
        service: Int
    ) {
        restaurants.document(restaurantId).update(
            mapOf(
                "ratingCount" to FieldValue.increment(1),
                "sumTaste" to FieldValue.increment(taste.toLong()),
                "sumAmbience" to FieldValue.increment(ambience.toLong()),
                "sumQuality" to FieldValue.increment(quality.toLong()),
                "sumService" to FieldValue.increment(service.toLong())
            )
        ).await()
    }

    private fun DocumentSnapshot.toRestaurant(): Restaurant? {
        if (!exists()) return null
        return Restaurant(
            id = id,
            name = getString("name") ?: return null,
            location = getString("location") ?: "",
            mapsUrl = getString("mapsUrl") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            ratingCount = getLong("ratingCount") ?: 0L,
            sumTaste = getLong("sumTaste") ?: 0L,
            sumAmbience = getLong("sumAmbience") ?: 0L,
            sumQuality = getLong("sumQuality") ?: 0L,
            sumService = getLong("sumService") ?: 0L
        )
    }
}
