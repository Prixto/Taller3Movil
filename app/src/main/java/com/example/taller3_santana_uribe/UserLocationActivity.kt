package com.example.taller3_santana_uribe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3_santana_uribe.databinding.ActivityUserLocationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.*

class UserLocationActivity : AppCompatActivity() {

    private val USERS = "users/"
    private lateinit var binding: ActivityUserLocationBinding
    private var myLocationOverlay: MyLocationNewOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserLocationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val db = FirebaseDatabase.getInstance()
        val userRef = db.getReference(USERS)
        val auth = FirebaseAuth.getInstance()
        val currentUserUid = auth.currentUser?.uid

        val selectedUserEmail = intent.getStringExtra("selectedUserEmail")

        val selectedUserLocationRef = selectedUserEmail?.let { userRef.child(it) }

        if (selectedUserLocationRef != null) {
            selectedUserLocationRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val selectedUserLatitude = snapshot.child("latitude").value as Double
                        val selectedUserLongitude = snapshot.child("longitude").value as Double

                        val currentUserLocationRef = userRef.child(currentUserUid ?: "")

                        currentUserLocationRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val currentUserLatitude = snapshot.child("latitude").value as Double
                                    val currentUserLongitude = snapshot.child("longitude").value as Double

                                    val currentLocation = GeoPoint(currentUserLatitude, currentUserLongitude)
                                    val selectedUserLocation = GeoPoint(selectedUserLatitude, selectedUserLongitude)

                                    val distance = calculateDistance(currentLocation, selectedUserLocation)
                                    binding.distanceTextView.text = "Distancia: ${String.format("%.2f", distance)} metros"

                                    binding.mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                    binding.mapView.setBuiltInZoomControls(true)

                                    binding.mapView.controller.setZoom(16.0)
                                    binding.mapView.controller.setCenter(selectedUserLocation)

                                    myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this@UserLocationActivity), binding.mapView)
                                    myLocationOverlay?.enableMyLocation()
                                    binding.mapView.overlays.add(myLocationOverlay)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val radius = 6371

        val lat1Rad = Math.toRadians(point1.latitude)
        val lon1Rad = Math.toRadians(point1.longitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val lon2Rad = Math.toRadians(point2.longitude)

        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radius * c * 1000
    }
}
