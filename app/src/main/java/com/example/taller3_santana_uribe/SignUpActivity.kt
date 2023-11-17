package com.example.taller3_santana_uribe

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.example.taller3_santana_uribe.databinding.ActivitySignUpBinding
import com.example.taller3_santana_uribe.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var storage :FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        myRef = db.reference.child("users")

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val name =binding.nameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()

            if(validateForm(email, password, name, lastName)){
                registerDB(email, password, name, lastName)
            }
        }

        binding.buscarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }

        binding.tomarFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 2)
        }
    }

    private fun validateForm(email : String, password: String, name : String,
                             lastName: String) : Boolean {
        var valid = false
        if (email.isEmpty()) {
            binding.emailEditText.error = "Required!"
        } else if (!validEmailAddress(email)) {
            binding.emailEditText.error = "Invalid email address"
        } else if(name.isEmpty()){
            binding.nameEditText.error = "Required!"
        } else if(lastName.isEmpty()){
            binding.lastNameEditText.error = "Required!"
        } else if (password.isEmpty()) {
            binding.passwordEditText.error = "Required!"
        } else if (password.length < 6){
            binding.passwordEditText.error = "Password should be at least 6 characters long!"
        }else {
            val image = binding.userImage.drawable
            if (image == null) {
                Toast.makeText(this, "Seleccione una foto", Toast.LENGTH_SHORT).show()
            } else {
                valid = true
            }
        }
        return valid
    }

    private fun validEmailAddress(email:String):Boolean{
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(regex.toRegex())
    }

    private fun registerDB(email: String,password: String,name: String,lastName: String){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this
        ) { task ->
            if (task.isSuccessful) {
                updateUI(auth.currentUser)
                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)

                val uid = task.result!!.user?.uid

                storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                val imageRef = storageRef.child("images/${uid}.jpg")

                val bitmap = binding.userImage.drawable.toBitmap()
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val bytes = baos.toByteArray()

                imageRef.putBytes(bytes).addOnCompleteListener { uploadTask ->
                    if (uploadTask.isSuccessful) {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            val imageUrl = downloadUrl.toString()
                            val user = User(name,lastName,email,password,true,imageUrl,0.0,0.0)
                            if (uid != null) {
                                myRef.child(uid).setValue(user)
                            }

                            Toast.makeText(this@SignUpActivity, "SignUp successful", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignUpActivity, "Error uploading image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (!task.isSuccessful) {
                Toast.makeText(
                    this@SignUpActivity, "Error: " + task.exception.toString(),
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, task.exception!!.message!!)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uri = data?.data
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri!!))
            binding.userImage.setImageBitmap(bitmap)
        }

        if (requestCode == 2 && resultCode == RESULT_OK) {
            val extras = data?.extras
            val imageBitmap = extras?.get("data") as Bitmap
            binding.userImage.setImageBitmap(imageBitmap)
        }
    }
}
