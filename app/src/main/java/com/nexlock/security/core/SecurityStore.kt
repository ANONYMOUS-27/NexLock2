package com.nexlock.security.core

import android.content.Context
import java.security.MessageDigest

class SecurityStore(context: Context) {
    private val prefs = context.getSharedPreferences("nexlock_secure", Context.MODE_PRIVATE)

    fun hasPin(): Boolean = prefs.contains("pin_hash")

    fun setPin(pin: String) {
        prefs.edit().putString("pin_hash", hash(pin)).apply()
    }

    fun verifyPin(pin: String): Boolean = prefs.getString("pin_hash", null) == hash(pin)

    fun protectedPackages(): Set<String> = prefs.getStringSet("protected_packages", emptySet()) ?: emptySet()

    fun setProtected(packageName: String, enabled: Boolean) {
        val updated = protectedPackages().toMutableSet()
        if (enabled) updated += packageName else updated -= packageName
        prefs.edit().putStringSet("protected_packages", updated).apply()
    }

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
}
