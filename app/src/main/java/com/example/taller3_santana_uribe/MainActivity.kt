package com.example.taller3_santana_uribe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taller3_santana_uribe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(baseContext,LoginActivity::class.java))
        }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(baseContext,SignUpActivity::class.java))
        }
    }
}