package buscador

import java.net.URI
import java.net.http.HttpRequest
import java.lang.IllegalArgumentException

object Validator {
    fun String.isAValidUrl(): Boolean {
        return try {
            HttpRequest.newBuilder(URI(this)).build()
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }


}