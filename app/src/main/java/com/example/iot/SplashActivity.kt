package com.example.iot

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Contacts.SettingsColumns.KEY
import android.util.Log
import android.widget.Button
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class SplashActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        initValue()

        Handler().postDelayed(
            {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                finish()
            }, 1000)
    }


    fun initValue(){

        val userSettings = getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        val prefEditor = userSettings.edit()

        val ref = FirebaseDatabase.getInstance().getReference("PI_03_CONTROL")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {



                val relay = snapshot.child("relay").value

                prefEditor.putString("relay",relay.toString())
                prefEditor.apply()

            }
        })



    }




}
