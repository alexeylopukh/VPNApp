package com.lopukh.vpnapp

import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer

class InternetPacket (packet: ByteBuffer){
    lateinit var destination: InetAddress
    var hostName: String
    var port: Int = -1
    var protocol: Int = -1
    var protocolName: String
    init {
        var buffer = packet.get().toInt()
        val ipVersion = buffer shr 4
        var headerLength = buffer and 0x0F
        headerLength *= 4
        buffer = packet.get().toInt()      //DSCP + EN
        val totalLength = packet.char.toInt()  //Total Length
        buffer = packet.char.toInt()  //Identification
        buffer = packet.char.toInt()  //Flags + Fragment Offset
        buffer = packet.get().toInt()      //Time to Live
        protocol = packet.get().toInt()      //Protocol
        protocolName = getProtocolName(protocol)
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
        port = buffer and 0xFF


        try {
            destination = InetAddress.getByName(destIP)
            hostName = destination.hostName
        } catch (e: UnknownHostException) {
            hostName = "Unresolved"
        }
    }

    private fun getProtocolName(protocol: Int): String{
        when (protocol){
            6 -> return "TCP"
            17 -> return "UDP"
        }
        return "Unknown"
    }
}