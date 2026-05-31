package com.aistudio.sharmakhata.pqmzvk.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.Locale

object UpiUtils {

    /** Known UPI app package names for targeted intents. */
    const val PACKAGE_GPAY = "com.google.android.apps.nbu.paisa.user"
    const val PACKAGE_PHONEPE = "com.phonepe.app"
    const val PACKAGE_PAYTM = "net.one97.paytm"

    /** All known packages in priority order. */
    private val KNOWN_PACKAGES = listOf(PACKAGE_GPAY, PACKAGE_PHONEPE, PACKAGE_PAYTM)

    data class UpiRequest(
        val upiId: String,
        val merchantName: String,
        val amount: Double? = null,
        val note: String = "",
        val currency: String = "INR"
    )

    /**
     * Build a UPI deep-link URI.
     *
     * Result format:
     * `upi://pay?pa=merchant@upi&pn=ShopName&am=100.00&tn=Note&cu=INR`
     */
    fun buildUpiUri(request: UpiRequest): Uri {
        val params = mutableListOf(
            "pa=${Uri.encode(request.upiId)}",
            "pn=${Uri.encode(request.merchantName)}",
            "cu=${Uri.encode(request.currency)}"
        )
        if (request.amount != null) {
            params.add("am=${String.format(Locale.US, "%.2f", request.amount)}")
        }
        if (request.note.isNotBlank()) {
            params.add("tn=${Uri.encode(request.note)}")
        }
        return Uri.parse("upi://pay?${params.joinToString("&")}")
    }

    /**
     * Open the given UPI URI in a specific app identified by [packageName].
     *
     * @return `null` on success, or an error message string if the app is not installed.
     */
    fun openApp(context: Context, uri: Uri, packageName: String): String? {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            null // success
        } catch (_: Exception) {
            "$packageName installed nahi hai"
        }
    }

    /**
     * Open the UPI URI via the system app-chooser (lets the user pick any
     * app that can handle `upi://` intents).
     *
     * @return `null` on success, or an error message if no UPI-capable app
     *         is installed at all.
     */
    fun openWithChooser(context: Context, uri: Uri): String? {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, "UPI app chunein"))
            null
        } else {
            "Koi UPI app installed nahi hai"
        }
    }

    /**
     * Try each known UPI app (GPay -> PhonePe -> Paytm) in order and open
     * the first one that is installed.
     *
     * @return `null` on success, or an error message if none of the known
     *         apps are installed.
     */
    fun openFirstInstalled(context: Context, uri: Uri): String? {
        for (pkg in KNOWN_PACKAGES) {
            val error = openApp(context, uri, pkg)
            if (error == null) return null // opened successfully
        }
        return "Koi UPI app (GPay/PhonePe/Paytm) installed nahi hai"
    }

    // -- Convenience helpers -------------------------------------------------

    fun openGpay(context: Context, uri: Uri): String? = openApp(context, uri, PACKAGE_GPAY)

    fun openPhonePe(context: Context, uri: Uri): String? = openApp(context, uri, PACKAGE_PHONEPE)

    fun openPaytm(context: Context, uri: Uri): String? = openApp(context, uri, PACKAGE_PAYTM)

    /**
     * Build a UPI URI and immediately try to open it in the first known
     * installed app. All-in-one convenience.
     *
     * @return `null` on success, or an error message on failure.
     */
    fun pay(context: Context, request: UpiRequest): String? {
        val uri = buildUpiUri(request)
        return openFirstInstalled(context, uri)
    }
}
