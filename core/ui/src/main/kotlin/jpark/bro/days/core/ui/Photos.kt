package jpark.bro.days.core.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppThemeTokens
import java.io.File
import jpark.bro.days.core.designsystem.R as DesignR

/**
 * 기기에 저장된 사진 (ImageRepository가 준 파일). [file]이 없으면 아무것도 그리지 않는다.
 * 영역을 채우도록 잘라서 보여준다.
 */
@Composable
fun DaysPhoto(
    file: File?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    if (file == null) return
    AsyncImage(
        model = file,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

/** 프로필 원: 사진 → 이름 첫 글자 → 사람 아이콘 순 */
@Composable
fun ProfileAvatar(
    file: File?,
    name: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    val colors = AppThemeTokens.colors
    Box(
        modifier =
            modifier
                .size(size)
                .clip(AppShapes.full)
                .background(colors.surface)
                .border(2.dp, colors.primarySoft, AppShapes.full),
        contentAlignment = Alignment.Center,
    ) {
        val initial = name?.firstOrNull()?.toString()
        when {
            file != null -> {
                DaysPhoto(file = file, contentDescription = name, modifier = Modifier.fillMaxSize())
            }

            initial != null -> {
                Text(text = initial, style = AppThemeTokens.typography.headlineSm, color = colors.primary)
            }

            else -> {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_person),
                    contentDescription = null,
                    tint = colors.primary,
                )
            }
        }
    }
}

/** 시스템 사진 선택기 (권한 필요 없음). 고르면 content Uri 문자열을 넘긴다. 반환값을 부르면 연다 */
@Composable
fun rememberPhotoPicker(onPicked: (uri: String) -> Unit): () -> Unit {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { onPicked(it.toString()) }
        }
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}
