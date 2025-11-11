package es.hgg.sharexp.server.util

import java.security.SecureRandom
import java.util.*

object UUIDv7 {
    private val generator = SecureRandom()

    private val random: ULong
        get() = generator.nextLong().toULong()
    private val millis: ULong
        get() = System.currentTimeMillis().toULong()

    fun generate(): UUID {
        return UUID(msb(), lsb())
    }

    private fun msb(): Long {
        val time: ULong = (millis shl 16)
        val version = (7UL shl 12)
        val randA = (random and 0x0FFF_UL)

        return (time or version or randA).toLong()
    }

    private fun lsb(): Long {
        val rand = (random and 0x3FFF_FFFF_FFFF_FFFF_UL)
        val variant = (0b10_UL shl 62)
        return (rand or variant).toLong()
    }
}
