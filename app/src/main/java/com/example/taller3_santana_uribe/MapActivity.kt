package com.example.taller3_santana_uribe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3_santana_uribe.databinding.ActivityMapBinding
import com.example.taller3_santana_uribe.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask

class MapActivity : AppCompatActivity() {
    val USERS = "users/"
    private lateinit var binding: ActivityMapBinding
    private lateinit var auth: FirebaseAuth
    lateinit var map: MapView
    private val timer = Timer()
    private var userGeoPoint: GeoPoint? = null
    private lateinit var db : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var userRef: DatabaseReference
    private var currentUser: User? = null



    private val updateLocationTask = object : TimerTask() {
        override fun run() {
            runOnUiThread {
                updateLocation()
            }
        }
    }

    private val myLocationOverlay: MyLocationNewOverlay by lazy {
        MyLocationNewOverlay(GpsMyLocationProvider(this), map)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        myRef = db.reference.child("users")
        userRef = db.reference.child("users").child(auth.currentUser?.uid ?: "")


        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Obtener los datos del usuario actual
                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseOne", "Failed to read user data.", error.toException())
            }
        })

        Configuration.getInstance().load(this, androidx.preference.PreferenceManager
            .getDefaultSharedPreferences(this))
        map = binding.osmMap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // Esto añade la ubicación al bitmap.
        map.overlays.add(myLocationOverlay)

        // Este es el temporizador que actualiza la ubicación del usuario cada 3 segundos.
        timer.schedule(updateLocationTask, 0, 3000)

        val locationsRef = db.reference.child("locations")
        locationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (locationSnapshot in snapshot.children) {
                    val latitude = locationSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = locationSnapshot.child("longitude").getValue(Double::class.java)
                    val name = locationSnapshot.child("name").getValue(String::class.java)

                    if (latitude != null && longitude != null) {
                        val geoPoint = GeoPoint(latitude, longitude)
                        val marker = Marker(map)
                        marker.position = geoPoint
                        marker.title = name ?: ""
                        map.overlays.add(marker)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseOne", "Error al leer las ubicaciones", databaseError.toException())
            }
        })


        //  Acá verifica si se encuentra permito el permiso de ubicación.
        checkLocationPermission()

        // Acá verifica si la ubicación esta activa.
        checkLocationServices()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        map.controller.setZoom(18.0)
        map.controller.animateTo(userGeoPoint)

        myLocationOverlay.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()

        myLocationOverlay.disableMyLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.signout -> {
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
                return true
            }
            R.id.change_status -> {
                toggleUserStatus()
                return true
            }
            R.id.users -> {
                val intent = Intent(this, UserListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateLocation() {
        myLocationOverlay.enableFollowLocation()

        val location = myLocationOverlay.myLocation
        if (location != null) {
            userGeoPoint = GeoPoint(location)

            if (currentUser != null) {
                currentUser!!.latitude = userGeoPoint!!.latitude
                currentUser!!.longitude = userGeoPoint!!.longitude
                userRef.setValue(currentUser) // Actualiza la ubicación en Firebase
            }
        }

        val punteroBitmap = BitmapFactory.decodeResource(resources, R.drawable.pointer)
        myLocationOverlay.setPersonIcon(punteroBitmap)
        myLocationOverlay.isDrawAccuracyEnabled = false
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun checkLocationServices() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun toggleUserStatus() {
        if (currentUser != null) {
            val newStatus = !currentUser!!.activo
            currentUser!!.activo = newStatus
            userRef.setValue(currentUser) // Actualizar el estado en Firebase
        }
    }
}
