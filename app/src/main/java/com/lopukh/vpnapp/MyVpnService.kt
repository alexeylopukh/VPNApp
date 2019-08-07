package com.lopukh.vpnapp

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.net.*
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.lang.reflect.Array.getChar
import kotlin.experimental.and


private const val TAG = "VPN"
private const val DNS_SERVER = "8.8.8.8"
private const val ROUTE_IP = "0.0.0.0"
private const val MAX_PACKET_SIZE: Int = Short.MAX_VALUE.toInt()
private val KEEPALIVE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(15)

class MyVpnService : VpnService() {

    private var mThread: Thread? = null
    private var mInterface: ParcelFileDescriptor? = null
    val builder = Builder()


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packet = ByteBuffer.allocate(MAX_PACKET_SIZE)
        mThread = Thread(Runnable {
            try {
                mInterface = builder.setSession("MyVpnService")
                    .addAddress(getLocalIpAddress()!!, 24)
                    .addRoute(ROUTE_IP, 0)
                    .establish()


                val input = FileInputStream(
                    mInterface!!.fileDescriptor
                )
                val output = FileOutputStream(
                    mInterface!!.fileDescriptor
                )
                //53 udp
                val socket = Socket()

                while (true) {
                    // Read the outgoing packet from the input stream
                    var length = input.read(packet.array())
                    if (length > 0) {
                        packet.limit(length)
                        debugPacket(packet)

                        output.write(packet.array())
//                        output.flush()
                        packet.clear()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (mInterface != null) {
                    mInterface!!.close()
                    mInterface = null
                }
            }
        })
        mThread!!.start()

        return START_STICKY
    }

    override fun onDestroy() {
        if (mThread != null) {
            mThread!!.interrupt()
        }
        super.onDestroy()
    }

    fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress() && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }

        return null
    }


    private fun debugPacket(packet: ByteBuffer) {
        var buffer = packet.get().toInt()
        val ipVersion = buffer shr 4
        var headerLength = buffer and 0x0F
        headerLength *= 4
        buffer = packet.get().toInt()      //DSCP + EN
        val totalLength = packet.char.toInt()  //Total Length
        buffer = packet.char.toInt()  //Identification
        buffer = packet.char.toInt()  //Flags + Fragment Offset
        buffer = packet.get().toInt()      //Time to Live
        val protocol = packet.get().toInt()      //Protocol
        buffer = packet.char.toInt()  //Header checksum

        var sourceIP = ""
        buffer = packet.get().toInt()  //Source IP 1st Octet
        sourceIP += buffer and 0xFF
        sourceIP += "."

        buffer = packet.get().toInt()  //Source IP 2nd Octet
        sourceIP += buffer and 0xFF
        sourceIP += "."

        buffer = packet.get().toInt()  //Source IP 3rd Octet
        sourceIP += buffer and 0xFF
        sourceIP += "."

        buffer = packet.get().toInt()  //Source IP 4th Octet
        sourceIP += buffer and 0xFF

        var destIP = ""
        buffer = packet.get().toInt()  //Destination IP 1st Octet
        destIP += buffer and 0xFF
        destIP += "."

        buffer = packet.get().toInt()  //Destination IP 2nd Octet
        destIP += buffer and 0xFF
        destIP += "."

        buffer = packet.get().toInt()  //Destination IP 3rd Octet
        destIP += buffer and 0xFF
        destIP += "."

        buffer = packet.get().toInt()  //Destination IP 4th Octet
        destIP += buffer and 0xFF

        buffer = packet.get().toInt()
        buffer = packet.get().toInt()
        buffer = packet.get().toInt()

        buffer = packet.get().toInt()
        val port = buffer and 0xFF

        var hostName: String
        try {
            val addr = InetAddress.getByName(destIP)
            hostName = addr.hostName
        } catch (e: UnknownHostException) {
            hostName = "Unresolved"
        }
        if (port == 53)
        Log.d(
            TAG,
            "Packet: IP Version=$ipVersion Destination-IP=$destIP, Port=$port Hostname=$hostName, Source-IP=$sourceIP, Protocol=$protocol"
        )
    }

}