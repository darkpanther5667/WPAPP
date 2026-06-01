package com.aistudio.sharmakhata.pqmzvk.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BiometricAuthHelper {

    private const val PREFS_BIOMETRIC = "grahbook_biometric"
    private const val KEY_AUTHENTICATED = "authenticated"

    private val _authState = MutableStateFlow(false)
    val authState: StateFlow<Boolean> = _authState.asStateFlow()

    fun isAvailable(context: Context): Boolean {
        // Only lock if we are running inside a FragmentActivity — otherwise
        // BiometricPrompt cannot attach and the user gets permanently stuck.
        if (context !is androidx.fragment.app.FragmentActivity) return false
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun isDeviceCredentialAvailable(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun isAuthenticated(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_BIOMETRIC, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTHENTICATED, false)
    }

    fun setAuthenticated(context: Context, authenticated: Boolean) {
        context.getSharedPreferences(PREFS_BIOMETRIC, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUTHENTICATED, authenticated)
            .apply()
        _authState.value = authenticated
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_BIOMETRIC, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        _authState.value = false
    }

    fun showPrompt(activity: FragmentActivity) {
        try {
            if (isAuthenticated(activity)) return
            // Guard: if neither biometric nor device credential is available, skip
            if (!isAvailable(activity)) return
            // Use BIOMETRIC_STRONG | DEVICE_CREDENTIAL so the user can also
            // fall back to PIN/pattern. This also eliminates the need for
            // setNegativeButtonText which conflicts with BIOMETRIC_STRONG alone.
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify your identity")
                .setSubtitle("Use fingerprint, face, or device PIN")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .setConfirmationRequired(false)
                .build()

            val executor = getMainExecutor(activity)
            @Suppress("DEPRECATION")
            val prompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        setAuthenticated(activity, true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                            android.util.Log.e("BiometricAuth", "Auth error: $errString")
                        }
                    }

                    override fun onAuthenticationFailed() {
                        android.util.Log.d("BiometricAuth", "Auth failed — retry available")
                    }
                }
            )

            prompt.authenticate(promptInfo)
        } catch (e: Exception) {
            android.util.Log.w("BiometricAuth", "showPrompt failed, skipping biometric", e)
            // Fall through — user proceeds to app without biometric lock
        }
    }

    private fun getMainExecutor(context: Context) =
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            context.mainExecutor
        } else {
            android.os.Handler(context.mainLooper).let { handler ->
                java.util.concurrent.Executor { r -> handler.post(r) }
            }
        }
}
