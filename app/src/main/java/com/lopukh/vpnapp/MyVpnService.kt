package com.lopukh.vpnapp

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.net.*
import java.nio.ByteBuffer

private const val TAG = "VPN"
private const val DNS_SERVER = "1.1.1.1"
private const val ROUTE_IP = "0.0.0.0"
private const val MAX_MTU = 1500
private const val MAX_PACKET_SIZE = 32767

class MyVpnService : VpnService() {


    private var mThread: Thread? = null
    private lateinit var mInterface: ParcelFileDescriptor
    private var isReady = false


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mThread = Thread(
            Runnable {
                if (!isReady) {
                    buildVpnService()
                }
                if (isReady) {
                    readPackets()
                }
            })
        mThread!!.start()

        return START_STICKY
    }

    private fun readPackets() {
        val input = FileInputStream(mInterface.fileDescriptor)
        val output = FileOutputStream(mInterface.fileDescriptor)
        val packet = ByteBuffer.allocate(MAX_PACKET_SIZE)
        while (true) {
            try {
                val length = input.read(packet.array())
//                if (length > 0) {
                    val b = ByteArray(length)
                    System.arraycopy(packet.array(), 0, b, 0, length)
                    output.write(b)
                    packet.clear()
                    Thread.sleep(100)
//                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun buildVpnService() {
        mInterface = Builder()
            .setMtu(MAX_MTU)
            .addAddress(getLocalIpAddress()!!, 24)
            .addRoute(ROUTE_IP, 0)
            .establish()!!
        isReady = true
    }

    override fun onDestroy() {
        if (mThread != null) {
            mThread!!.interrupt()
        }
        super.onDestroy()
    }

    private fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }

        return null
    }


}