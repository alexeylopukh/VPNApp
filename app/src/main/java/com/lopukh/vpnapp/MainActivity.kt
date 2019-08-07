package com.lopukh.vpnapp

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment

class MainActivity : AppCompatActivity() {

    val VPN_REQUEST_CODE = 13

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startVpnService(v: View){
        val intent: Intent? = VpnService.prepare(baseContext)
        if (intent != null){
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode){
            VPN_REQUEST_CODE -> {
                if (resultCode == RESULT_OK){
                    val intent = Intent(this, MyVpnService::class.java)
                    startService(intent)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
