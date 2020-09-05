package com.example.iot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationBuilderWithBuilderAccessor
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.michaldrabik.classicmaterialtimepicker.CmtpDialogFragment
import com.michaldrabik.classicmaterialtimepicker.utilities.setOnTime24PickedListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.lang.Exception
import java.lang.Math.floor
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.widget.ImageButton


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnAir = findViewById<ImageButton>(R.id.imgBtnAir)
        val btnLight = findViewById<ImageButton>(R.id.imgBtnLight)
        val btnLock = findViewById<ImageButton>(R.id.imgBtnLock)
        val btnCurtain = findViewById<ImageButton>(R.id.imgBtnCurtain)
        btnAir.setOnClickListener{
            val intent = Intent(this@MainActivity, AirCondActivity::class.java)
            startActivity(intent)
        }
        btnLight.setOnClickListener{
            val intent = Intent(this@MainActivity, LightActivity::class.java)
            startActivity(intent)
        }
        btnLock.setOnClickListener{
            val intent = Intent(this@MainActivity, Lockv2Activity::class.java)
            startActivity(intent)
        }
        btnCurtain.setOnClickListener{
            val intent = Intent(this@MainActivity, CurtainActivity::class.java)
            startActivity(intent)
        }
    }
}





