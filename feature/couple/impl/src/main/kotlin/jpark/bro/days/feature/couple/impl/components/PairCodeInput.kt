package jpark.bro.days.feature.couple.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens

/**
 * 6칸 코드 입력 (시안 couple-code-input). 칸을 누르면 그 칸이 선택되고, 입력하면 그 칸을 덮어쓴 뒤 다음 칸으로 간다.
 * 선택 칸 강조는 입력을 받을 수 있을 때(포커스가 있을 때)만 보인다.
 * 키보드 입력은 보이지 않는 필드 하나가 받는다. 필드에 늘 표시 문자 하나를 넣어 두어,
 * 빈 칸에서 누른 백스페이스도 받는다 (키보드는 지울 글자가 없으면 지우기를 보내지 않는다).
 */
@Composable
internal fun PairCodeInput(
    cells: List<Char?>,
    activeCell: Int,
    onCellClick: (Int) -> Unit,
    onType: (String) -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = TextFieldValue(SENTINEL, selection = TextRange(SENTINEL.length)),
            onValueChange = { changed ->
                val text = changed.text
                when {
                    text.isEmpty() -> onDelete()
                    text.length > SENTINEL.length -> onType(text.removePrefix(SENTINEL))
                    else -> Unit
                }
            },
            modifier =
                Modifier
                    .size(1.dp)
                    .alpha(0f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            cells.forEachIndexed { index, char ->
                val isActive = isFocused && index == activeCell
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .aspectRatio(CELL_RATIO)
                            .clip(AppShapes.default)
                            .background(if (isActive) colors.primarySoft else colors.surface)
                            .border(
                                if (isActive) 2.dp else 1.dp,
                                if (isActive) colors.primary else colors.border,
                                AppShapes.default,
                            ).clickable {
                                onCellClick(index)
                                focusRequester.requestFocus()
                                keyboard?.show()
                            }.testTag("pairing_cell_$index"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = char?.toString().orEmpty(),
                        style = AppThemeTokens.typography.headlineMd,
                        color = colors.textPrimary,
                    )
                }
            }
        }
    }
}

private const val CELL_RATIO = 0.85f

/** 보이지 않는 필드에 늘 들어 있는 표시 문자 (폭 없는 공백) */
private const val SENTINEL = "​"
