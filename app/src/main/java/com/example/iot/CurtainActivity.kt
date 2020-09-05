package com.example.iot

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.michaldrabik.classicmaterialtimepicker.CmtpDialogFragment
import com.michaldrabik.classicmaterialtimepicker.utilities.setOnTime24PickedListener
import kotlinx.android.synthetic.main.activity_curtain.*
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text
import java.lang.Math.floor
import java.sql.Time
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class CurtainActivity : AppCompatActivity() {
    private val handlers: Handler = Handler()
    val myref = FirebaseDatabase.getInstance().getReference("PI_03_CONTROL")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curtain)

        initValue()
        getLight.run()
        ToastRunnabler.run()
        buttonStart.setOnClickListener{
            btn1_timefrom()
        }
        buttonEnd.setOnClickListener{
            btn2_timefrom()
        }
        btn_set.setOnClickListener{
            checkTime.run()
        }
        checkTime

        btn_reset.setOnClickListener(){
            buttonEnd.setText("00:00")
            buttonStart.setText("00:00")
            onPause()
        }
        //checkLight.run()
        manual_switch.setOnClickListener {
            if(manual_switch.isChecked == true){
                myref.child("relay").setValue("1")//open
                Toast.makeText(this, "Curtain is Open", Toast.LENGTH_SHORT).show()
            }else if (manual_switch.isChecked == false){
                myref.child("relay").setValue("0")//open
                Toast.makeText(this, "Curtain is Close", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onPause(){
        super.onPause()
        handlers.removeCallbacks(checkTime)
    }
    fun initValue(){
        val usersettings = getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        val r = usersettings.getString("relay", "").toString().toInt()

        if(r == 0){
            imageView.setImageResource(R.drawable.close)
            statusOfCurtain.setText("CLOSE")
            statusOfCurtain.isChecked = false//close

        }else if (r == 1){
            imageView.setImageResource(R.drawable.window)
            statusOfCurtain.setText("OPEN")
            statusOfCurtain.isChecked = true//open
        }
    }

    private val getLight: Runnable = object: Runnable{
        override fun run() {
            val sdf = SimpleDateFormat("yyyyMMdd")
            val frmt = DecimalFormat("00")
            val date = sdf.format(Date())
            val ddbdate = "PI_03_" + date
            val hour = SimpleDateFormat("HH").format(Date())
            val min = SimpleDateFormat("mm").format(Date())
            val sec = SimpleDateFormat("ss").format(Date())
            val minSec = min + frmt.format((Math.floor(sec.toDouble() / 10) *10).toInt())

            val ref = FirebaseDatabase.getInstance().getReference().child(ddbdate)
            val lastQuery = ref.child(hour).child(minSec)
            lastQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    test_get_light.text = p0.child("light").value.toString() // get light
                }
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })

            handlers.postDelayed(this, 1000)
        }
    }

    fun btn1_timefrom(){

        val dialog = CmtpDialogFragment.newInstance()
        dialog.setInitialTime24(12,0 )
        dialog.setOnTime24PickedListener {
            buttonStart.setText(it.toString())
        }
        dialog.show(supportFragmentManager, "TimePicker")
    }

    fun btn2_timefrom(){

        val dialog = CmtpDialogFragment.newInstance()
        dialog.setInitialTime24(12,0 )
        dialog.setOnTime24PickedListener {
            buttonEnd.setText(it.toString())
        }
        dialog.show(supportFragmentManager, "TimePicker")
    }

    //private fun checkTime()
    private val checkTime: Runnable = object: Runnable{
        override fun run(){
            val sdf = SimpleDateFormat("yyyyMMdd")
            val frmt = DecimalFormat("00")
            val date = sdf.format(Date())
            val ddbdate = "PI_03_" + date
            val hour = SimpleDateFormat("HH").format(Date())
            val min = SimpleDateFormat("mm").format(Date())
            val sec = SimpleDateFormat("ss").format(Date())
            val minSec = min + frmt.format((Math.floor(sec.toDouble() / 10) *10).toInt())

            val time = SimpleDateFormat("HH:mm").format(Date())
            val ref = FirebaseDatabase.getInstance().getReference().child(ddbdate)
            val lastQuery = ref.child(hour).child(minSec)
            lastQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val startTime = buttonStart.text.toString()
                    val endTime = buttonEnd.text.toString()

                    if(time >= startTime && time < endTime){
                        myref.child("relay").setValue("0")//close
                    }else{
                        myref.child("relay").setValue("1")//open
                    }
                }
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
            handlers.postDelayed(this, 1000)
        }
    }

    /* private val checkLight:Runnable = object: Runnable{
         override fun run() {
                 val sdf = SimpleDateFormat("yyyyMMdd")
                 val frmt = DecimalFormat("00")
                 val date = sdf.format(Date())
                 val ddbdate = "PI_03_" + date
                 val hour = SimpleDateFormat("HH").format(Date())
                 val min = SimpleDateFormat("mm").format(Date())
                 val sec = SimpleDateFormat("ss").format(Date())
                 val minSec = min + frmt.format((floor(sec.toDouble()/10)*10).toInt())
                 val ref = FirebaseDatabase.getInstance().getReference().child(ddbdate)
                 val lastQuery = ref.child(hour).child(minSec)

                 lastQuery.addValueEventListener(object : ValueEventListener{
                     override fun onDataChange(p0: DataSnapshot) {
                         val lgt = p0.child("light").value.toString() // get light
                         test_get_light.text = lgt
                         if (test_get_light.text == "null") {
                             test_get_light.text = "0"
                             myref.child("relay").setValue("1") //open
                         } else {
                             val num = lgt.toInt()
                             if (num >= 35) {
                                 myref.child("relay").setValue("0") // close
                             } else if (num < 35)
                                 myref.child("relay").setValue("1") //open
                         }
                     }

                     override fun onCancelled(p0: DatabaseError) {
                         TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                     }
                 })

             handlers.postDelayed(this, 100)
         }
     } */

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
}