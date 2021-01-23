package pt.obvionaoe.utils

import io.grpc.Status
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

object Utils {
    fun String.toUUID(): UUID = UUID.fromString(this)

    fun Logger.warn(message: String, status: Status? = null) {
        if (status != null) {
            this.log(Level.WARNING, message, status)
        } else {
            this.log(Level.WARNING, message)
        }
    }
}
