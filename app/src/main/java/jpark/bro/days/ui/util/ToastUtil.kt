package jpark.bro.days.ui.util

import android.content.Context
import android.widget.Toast

object ToastUtil {
    private var toast: Toast? = null

    fun showToast(context: Context, msg: String) {
        toast?.cancel()
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toast?.show()
    }
}