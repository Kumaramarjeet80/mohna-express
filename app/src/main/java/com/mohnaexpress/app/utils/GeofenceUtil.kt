package com.mohnaexpress.app.utils

import com.mohnaexpress.app.data.model.ZoneCollection
import com.mohnaexpress.app.data.model.ZoneFeature

object GeofenceUtil {

    /**
     * Ray-Casting Point-in-Polygon algorithm.
     * GeoJSON polygon coordinates are in [longitude, latitude] format.
     */
    fun isPointInsidePolygon(lat: Double, lng: Double, polygonRing: List<List<Double>>): Boolean {
        if (polygonRing.size < 3) return false
        var inside = false
        val n = polygonRing.size
        var j = n - 1
        for (i in 0 until n) {
            val xi = polygonRing[i][0] // lng
            val yi = polygonRing[i][1] // lat
            val xj = polygonRing[j][0]
            val yj = polygonRing[j][1]

            val intersect = ((yi > lat) != (yj > lat)) &&
                    (lng < (xj - xi) * (lat - yi) / (yj - yi) + xi)
            if (intersect) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    /**
     * Finds matching delivery zone for given lat/lng from ZoneCollection
     */
    fun findMatchingZone(lat: Double, lng: Double, zoneCollection: ZoneCollection?): ZoneFeature? {
        if (zoneCollection == null || zoneCollection.features.isEmpty()) return null

        for (feature in zoneCollection.features) {
            val coordinates = feature.geometry.coordinates
            if (coordinates.isNotEmpty()) {
                // Outer ring of polygon
                val outerRing = coordinates[0]
                if (isPointInsidePolygon(lat, lng, outerRing)) {
                    return feature
                }
            }
        }
        return null
    }

    /**
     * Haversine distance in kilometers
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
