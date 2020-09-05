package com.example.iot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Switch
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.michaldrabik.classicmaterialtimepicker.CmtpDialogFragment
import com.michaldrabik.classicmaterialtimepicker.utilities.setOnTime24PickedListener
import kotlinx.android.synthetic.main.activity_lockv2.*
import java.lang.Math.floor
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class Lockv2Activity : AppCompatActivity() {

    var value: String = ""
    private val handlers: Handler = Handler()
    private val handled: Handler = Handler()
    private val handleee: Handler = Handler()
    val db = FirebaseDatabase.getInstance()
    val myref = db.getReference("PI_03_CONTROL")
    private val chanelID = "1234"
    private val notifiID = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lockv2)
        initValue()

        ToastRunnabler.run()
        switch2.setOnClickListener {
            ToastRunnables.run()
        }
        TimeRunnable.run()
        val switch1 = findViewById<Switch>(R.id.switch1)
        val switch2 = findViewById<Switch>(R.id.switch2)

        switch1.setOnClickListener {

            if (switch1.isChecked) {
                Toast.makeText(this, "Locking...", Toast.LENGTH_SHORT).show()
                myref.child("relay").setValue("1")
                imageView1.setImageResource(R.drawable.ic_white_lock)

            } else if (switch1.isChecked == false) {
                Toast.makeText(this, "Unlocking...", Toast.LENGTH_SHORT).show()
                myref.child("relay").setValue("0")
                imageView1.setImageResource(R.drawable.ic_unlockwhite)
            }
        }

        switch2.setOnClickListener {

            if (switch2.isChecked) {
                switch2.isChecked = true
                btn_time1.isEnabled = false
                btn_time2.isEnabled = false
                Toast.makeText(this, "Alert is on", Toast.LENGTH_SHORT)
            } else if (switch2.isChecked == false) {
                switch2.isChecked = false
                btn_time1.isEnabled = true
                btn_time2.isEnabled = true
                Toast.makeText(this, "Alert is off", Toast.LENGTH_SHORT)
                    .show()
            }
        }



        btn_time1.setOnClickListener { btn1_timefrom() }
        btn_time2.setOnClickListener { btn2_timefrom() }


    }

    fun initValue() {

        val userSettings = getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        val r = userSettings.getString("relay", "").toString().toInt()
        if (r == 1) {
            switch1.isChecked = true
            imageView1.setImageResource(R.drawable.ic_white_lock)
        } else if (r == 0) {
            switch1.isChecked = false
            imageView1.setImageResource(R.drawable.ic_unlockwhite)
        }
    }


    fun btn1_timefrom(){

        val dialog = CmtpDialogFragment.newInstance()
        dialog.setInitialTime24(9,0 )
        dialog.setOnTime24PickedListener {
            btn_time1.setText(it.toString())
        }
        dialog.show(supportFragmentManager, "TimePicker")
    }

    fun btn2_timefrom(){

        val dialog = CmtpDialogFragment.newInstance()
        dialog.setInitialTime24(9,0 )
        dialog.setOnTime24PickedListener {
            btn_time2.setText(it.toString())
        }
        dialog.show(supportFragmentManager, "TimePicker")
    }

    private val ToastRunnabler: Runnable = object : Runnable {
        override fun run() {
            val userSettings = getSharedPreferences("Preferences", Context.MODE_PRIVATE)
            val prefEditor = userSettings.edit()

            val ref = FirebaseDatabase.getInstance().getReference("PI_03_CONTROL")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val relay = snapshot.child("relay").value
                    prefEditor.putString("relay",relay.toString())
                    prefEditor.apply()
                }
            })
            initValue()
            handlers.postDelayed(this, 100)
        }
    }

    private val ToastRunnables: Runnable = object : Runnable {

        override fun run() {

            val sdf = SimpleDateFormat("yyyyMMdd")
            val frmt = DecimalFormat("00")
            val date = sdf.format(Date())
            val ddbdate = "PI_03_" + date
            val hour = SimpleDateFormat("HH").format(Date())
            val min = SimpleDateFormat("mm").format(Date())
            val sec = SimpleDateFormat("ss").format(Date())
            val minSec = min + frmt.format((floor(sec.toDouble() / 10) * 10).toInt())

            val ref = FirebaseDatabase.getInstance().getReference().child(ddbdate)
            val lastQuery = ref.child(hour).child(minSec)
            lastQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    try {
                        val time= SimpleDateFormat("HH:mm").format(Date())

                        val onTime = btn_time1.text.toString()
                        val offTime = btn_time2.text.toString()

                        if(switch2.isChecked) {
                            if(onTime > offTime) {
                                if (time >= onTime || time <= offTime) {
                                    val ult = p0.child("ultra2").value.toString().toDoubleOrNull()
                                    if (ult == null) {
                                        myref.child("buzzer").setValue("0")
                                    } else if (ult < 50.0) {
                                        myref.child("buzzer").setValue("1")
                                        createnotify()
                                        sendNotify()
                                    } else if (ult >= 50.0) {
                                        myref.child("buzzer").setValue("0")
                                    } else {
                                        myref.child("buzzer").setValue("0")
                                    }
                                } else {
                                    myref.child("buzzer").setValue("0")
                                }
                            }else if (offTime > onTime) {
                                if (time >= onTime && time <= offTime) {
                                    val ult = p0.child("ultra2").value.toString().toDoubleOrNull()
                                    if (ult == null) {
                                        myref.child("buzzer").setValue("0")
                                    } else if (ult < 50.0) {
                                        myref.child("buzzer").setValue("1")
                                        createnotify()
                                        sendNotify()
                                    } else if (ult >= 50.0) {
                                        myref.child("buzzer").setValue("0")
                                    } else {
                                        myref.child("buzzer").setValue("0")
                                    }
                                } else {
                                    myref.child("buzzer").setValue("0")
                                }
                            }
                        }else if(switch2.isChecked == false) {

                            myref.child("buzzer").setValue("0")
                        }
                    }
                    catch(e: Exception){
                        onPause()
                        onResume()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented")
                }
            })

            handled.postDelayed(this, 10000)
        }

    }

    private val TimeRunnable: Runnable = object : Runnable {

        override fun run() {

            val sdf = SimpleDateFormat("yyyyMMdd")
            val frmt = DecimalFormat("00")
            val date = sdf.format(Date())
            val ddbdate = "PI_03_" + date
            val hour = SimpleDateFormat("HH").format(Date())
            val min = SimpleDateFormat("mm").format(Date())
            val sec = SimpleDateFormat("ss").format(Date())
            val minSec = min + frmt.format((floor(sec.toDouble() / 10) * 10).toInt())

            val ref = FirebaseDatabase.getInstance().getReference().child(ddbdate)
            val lastQuery = ref.child(hour).child(minSec)
            lastQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val time= SimpleDateFormat("HH:mm").format(Date())

                    val onTime = "20:00"
                    val offTime = "07:00"

                    if(switch1.isChecked == false) {
                        if (time >= onTime || time <= offTime) {
                            createnotify1()
                            sendNotify1()
                        }
                    }

                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented")
                }
            })

            handleee.postDelayed(this, 60000)//1 min alert 1 times
        }

    }

    override fun onPause() {
        super.onPause()
        handled.removeCallbacks(ToastRunnables)
    }

    override fun onResume() {
        super.onResume()
        ToastRunnables.run()
    }

    private fun createnotify(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Alert"
            val desc = "Intruder detected"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(chanelID,name,importance).apply{
                description = desc
            }
            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createnotify1(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Alert"
            val desc = "Door is not locked"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(chanelID,name,importance).apply{
                description = desc
            }
            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun sendNotify(){
        val intent = Intent(this,MainActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending : PendingIntent = PendingIntent.getActivity(this,0,intent,0)
        val time= SimpleDateFormat("HH:mm").format(Date())
        val builder =   NotificationCompat.Builder(this,chanelID)
            .setSmallIcon(R.drawable.warning)
            .setContentTitle("Alert")
            .setContentText("Intruder detected at " + time.toString())
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            notify(notifiID,builder.build())
        }
    }

    private fun sendNotify1(){
        val intent = Intent(this,MainActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending : PendingIntent = PendingIntent.getActivity(this,0,intent,0)

        val builder =   NotificationCompat.Builder(this,chanelID)
            .setSmallIcon(R.drawable.warning)
            .setContentTitle("Alert")
            .setContentText("Door is not locked")
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            notify(notifiID,builder.build())
        }
    }

}
