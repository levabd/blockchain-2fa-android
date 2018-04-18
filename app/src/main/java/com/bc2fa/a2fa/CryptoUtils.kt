package com.bc2fa.a2fa

import android.util.Log
import java.security.MessageDigest
import java.util.zip.CRC32

/**
* Created by Oleg Levitsky on 04.03.2018.
*/

class CryptoUtils {
    companion object {

        private fun byteArrayToHexString( array: Array<Byte> ): String {

            val result = StringBuilder(array.size * 2)

            for ( byte in array ) {

                val toAppend =
                        String.format("%2x", byte).replace(" ", "0") // hexadecimal
                result.append(toAppend)
            }

            return result.toString()
        }

        private fun toMD5Hash( text: String ): String {

            val result: String

            result = try {
                val md5 = MessageDigest.getInstance("MD5")
                val md5HashBytes = md5.digest(text.toByteArray()).toTypedArray()

                byteArrayToHexString(md5HashBytes)
            } catch ( e: Exception ) {
                "error: ${e.message}"
            }

            return result
        }

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

            val firstStr = "$path::body::$bodyCrc32::key::$salt::phone_number::$phoneNumber"

            // Log.d("Retrofit", "bodyCrc32: $bodyCrc32")
            // Log.d("Retrofit", "salt: $salt")
            // Log.d("Retrofit", "firstStr: $firstStr")

            val md5str = toMD5Hash(firstStr)
            // Log.d("Retrofit", "md5str: $md5str")

            return "$md5str$rhex"
        }
    }
}
