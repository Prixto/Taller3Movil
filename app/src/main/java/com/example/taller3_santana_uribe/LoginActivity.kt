package com.example.taller3_santana_uribe

import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.taller3_santana_uribe.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.Loginbutton.setOnClickListener {
            if(validateForm(binding.email.text.toString(),binding.password.text.toString())){
                signIn(binding.email.text.toString(),binding.password.text.toString())
            }
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if(currentUser!=null) {
            val i = Intent(this, MapActivity::class.java)
            i.putExtra("email", currentUser.email.toString())
            startActivity(i)
        }
    }

    private fun signIn(email:String, password:String){
        if(validEmailAddress(email) && password!=null){
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if(it.isSuccessful){
                    updateUI(auth.currentUser)
                }else{
                    val message = it.exception!!.message
                    Toast.makeText(this, message, Toast.LENGTH_LONG ).show()
                    Log.w(TAG, "signInWithEmailAndPassword:failure", it.exception)
                    binding.email.text.clear()
                    binding.password.text.clear()
                }
            }
        }
    }


    private fun validateForm(email : String, password: String) : Boolean {
        var valid = false
        if (email.isEmpty()) {
            binding.email.error = "Required!"
        } else if (!validEmailAddress(email)) {
            binding.email.error = "Invalid email address"
        } else if (password.isEmpty()) {
            binding.password.error = "Required!"
        } else if (password.length < 6){
            binding.password.error = "Password should be at least 6 characters long!"
        }else {
            valid = true
        }
        return valid
    }

    private fun validEmailAddress(email:String):Boolean{
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(regex.toRegex())
    }

}