package com.ngse.synctime

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ngse.synctime.databinding.ActivityMainBinding
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone


class MainActivity : AppCompatActivity() {
    var devicePolicyManager: DevicePolicyManager? = null
    var demoDeviceAdmin: ComponentName? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager?;
        demoDeviceAdmin = getComponentName(this)

        binding.btnAddAdmin.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, demoDeviceAdmin)
            startActivityForResult(intent, 1234)
        }
        binding.btnStartSync.setOnClickListener {
            SNTPClient.getDate(
                TimeZone.getDefault()
            ) { rawDate, date, ex ->
                if (devicePolicyManager?.isProfileOwnerApp(applicationContext.packageName)==true) {
                    devicePolicyManager?.setTime(demoDeviceAdmin!!, date.time)
                } else {
                    Toast.makeText(this,"You are not Admin Device",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==1234 && resultCode==Activity.RESULT_OK){
            enableProfile()
        }
    }


    private fun enableProfile() {
        val manager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName: ComponentName = getComponentName(this)
        val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        if (devicePolicyManager?.isProfileOwnerApp(applicationContext.packageName)==true) {
            // This is the name for the newly created managed profile.
            manager.setProfileName(componentName, "test_profile")
            // We enable the profile here.
            manager.setProfileEnabled(componentName)
        }
    }

    fun getComponentName(context: Context): ComponentName {
        return ComponentName(context.applicationContext, MyDeviceAdminReceiver::class.java)
    }
}
