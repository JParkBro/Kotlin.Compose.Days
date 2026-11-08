package jpark.bro.days.core.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * 알림 권한 요청 (Android 13 이상). 이미 허용했거나 13 미만이면 아무것도 하지 않는다.
 * 거절해도 화면 동작은 그대로이고, 알림만 오지 않는다.
 */
@Composable
fun rememberNotificationPermissionRequest(): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    return {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

/**
 * 이 앱 알림이 기기에서 켜져 있는지(Android 13 이상은 권한 포함)를 화면이 다시 보일 때마다 알려준다.
 * 시스템 설정에서 바꾸고 돌아와도 맞게 보여주기 위해서다.
 */
@Composable
fun ObserveNotificationsEnabled(onChecked: (enabled: Boolean) -> Unit) {
    val context = LocalContext.current
    val currentOnChecked by rememberUpdatedState(onChecked)
    LifecycleResumeEffect(context) {
        currentOnChecked(NotificationManagerCompat.from(context).areNotificationsEnabled())
        onPauseOrDispose {}
    }
}

/** 시스템 설정의 이 앱 알림 화면을 연다 */
fun Context.openAppNotificationSettings() {
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
