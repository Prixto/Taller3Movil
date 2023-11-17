package com.example.taller3_santana_uribe

import UserListAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.taller3_santana_uribe.databinding.ActivityUserListBinding
import com.example.taller3_santana_uribe.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class UserListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserListBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private var vel: ValueEventListener? = null
    private var currentUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        myRef = db.reference.child("users")
        userRef = db.reference.child("users").child(auth.currentUser?.uid ?: "")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseOne", "Failed to read user data.", error.toException())
            }
        })
    }

    override fun onStart() {
        super.onStart()
        readUsersSubscribe()
    }

    override fun onStop() {
        super.onStop()
        if (vel != null) {
            myRef.removeEventListener(vel!!)
        }
    }

    private fun readUsersSubscribe() {
        vel = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()

                for (single in snapshot.children) {
                    val myUser = single.getValue(User::class.java)
                    if (myUser != null && myUser.activo) {
                        userList.add(myUser)
                    }
                }

                val adapter = UserListAdapter(this@UserListActivity, userList)
                binding.listUsers.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseOne", "Failed to read value.", error.toException())
            }
        })
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
            R.id.change_status ->{

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

    private fun toggleUserStatus() {
        if (currentUser != null) {
            val newStatus = !currentUser!!.activo
            currentUser!!.activo = newStatus
            userRef.setValue(currentUser) // Actualizar el estado en Firebase
        }
    }
}
