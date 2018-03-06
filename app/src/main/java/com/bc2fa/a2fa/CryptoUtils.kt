package com.bc2fa.a2fa

import java.security.MessageDigest
import java.util.zip.CRC32

/**
* Created by Oleg Levitsky on 04.03.2018.
*/

class CryptoUtils {
    companion object {

        @JvmStatic fun calculateApiKey(path: String, body: String, phoneNumber: String): String {

            val salt = String(charArrayOf(
                    97.toChar(), 77.toChar(), 105.toChar(), 83.toChar(), 77.toChar(), 85.toChar(),
                    118.toChar(), 89.toChar(), 109.toChar(), 80.toChar(), 71.toChar(), 122.toChar(),
                    57.toChar(), 114.toChar()))

            val source = "abcdef0123456789"
            var rhex = ""
            for (i in 0..16) rhex += source[Math.floor(Math.random() * source.length).toInt()]

            val crc = CRC32()
            crc.update(body.toByteArray())
            val bodyCrc32 = "%x".format(crc.value)

            val firstStr = "'$path'::body::'$bodyCrc32'::key::'$salt'::phone-number::'$phoneNumber'"

            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(firstStr.toByteArray())

            var md5str = ""
            for (byte in digest) md5str += "%02x".format(byte)

            return "'$md5str''$rhex'"
        }
    }
}
