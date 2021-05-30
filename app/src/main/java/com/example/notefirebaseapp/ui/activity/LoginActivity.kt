package com.example.notefirebaseapp.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.notefirebaseapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class LoginActivity : AppCompatActivity() {
    private lateinit var authRef: FirebaseAuth
    private var currentUserId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authRef = FirebaseAuth.getInstance()
        checkInitialState()

    }

    private fun checkInitialState() {
        if (authRef.currentUser == null) {
            login_btn_regester.setOnClickListener {
                registerNewUser()
            }
            login_btn_login.setOnClickListener {
                loginUser()
            }
        } else {
            authRef.currentUser?.uid?.also { userId ->
                currentUserId = userId
                Intent(this, AllNotesActivity::class.java).also {
                    it.putExtra("userId", currentUserId)
                    startActivity(it)
                }
            }
        }
    }

    private fun registerNewUser() {
        val email = login_et_email.text.toString()
        val password = login_et_password.text.toString()

        if (email.isNotBlank() && password.isNotBlank()) {
            CoroutineScope(IO).launch {
                try {
                    authRef.createUserWithEmailAndPassword(email, password).await()
                    withContext(Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Regstered successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        authRef.currentUser?.uid?.also {
                            currentUserId = it
                            Intent(this@LoginActivity, AllNotesActivity::class.java).also {
                                it.putExtra("userId", currentUserId)
                                startActivity(it)
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Main) {
                        Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_LONG).show()
        }
    }

    private fun loginUser() {
        val email = login_et_email.text.toString()
        val password = login_et_password.text.toString()

        if (email.isNotBlank() && password.isNotBlank()) {
            CoroutineScope(IO).launch {
                try {
                    authRef.signInWithEmailAndPassword(email, password).await()
                    withContext(Main) {
                        Toast.makeText(this@LoginActivity, "login successfully", Toast.LENGTH_SHORT)
                            .show()
                        authRef.currentUser?.uid?.also { userId ->
                            currentUserId = userId
                            Intent(this@LoginActivity, AllNotesActivity::class.java).also {
                                it.putExtra("userId", currentUserId)
                                startActivity(it)
                            }
                        }

                    }
                } catch (e: Exception) {
                    withContext(Main) {
                        Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
        }
    }
}