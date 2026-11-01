package jpark.bro.days.ui.view.coupleDate.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import jpark.bro.days.R
import jpark.bro.days.domain.model.ProfileType
import jpark.bro.days.ui.state.ProfileUiState
import jpark.bro.days.ui.util.EMOJIS
import jpark.bro.days.ui.util.ScreenUtil
import jpark.bro.days.ui.util.ToastUtil
import jpark.bro.days.ui.viewmodel.ProfileViewModel
import kotlinx.datetime.LocalDate

@Composable
fun CoupleProfile(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profileA by viewModel.profileA.collectAsState()
    val profileB by viewModel.profileB.collectAsState()

    Row(
        modifier =
            modifier
                .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Profile(
            profileData = profileA,
            onSaveProfile = { name, birthday ->
                viewModel.saveProfile(ProfileType.A, name, birthday)
            },
        )
        Text(
            text = EMOJIS.HEART,
            fontSize = 32.sp,
            modifier =
                Modifier
                    .padding(24.dp),
        )
        Profile(
            profileData = profileB,
            onSaveProfile = { name, birthday ->
                viewModel.saveProfile(ProfileType.B, name, birthday)
            },
        )
    }
}

@Composable
private fun Profile(
    profileData: ProfileUiState,
    onSaveProfile: (String, LocalDate) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { showDialog = true },
                ).widthIn(min = 140.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .border(width = 2.dp, color = Color.White, shape = CircleShape)
                    .clip(shape = CircleShape),
        ) {
            Icon(
                painter = painterResource(R.drawable.male_profile),
                contentDescription = stringResource(R.string.male_profile_icon),
                modifier =
                    Modifier
                        .size(96.dp),
            )
        }
        Text(
            text = profileData.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier =
                Modifier
                    .padding(vertical = 8.dp),
        )
        Text(
            text = profileData.ageText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier =
                Modifier
                    .padding(bottom = 16.dp),
        )
    }
    if (showDialog) {
        ProfileEditDialog(
            profileData = profileData,
            onConfirm = { updatedName, updatedBirthday ->
                onSaveProfile(updatedName, updatedBirthday)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun ProfileEditDialog(
    profileData: ProfileUiState,
    onConfirm: (String, LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    val screenWidth = ScreenUtil.getScreenWidth()
    val dialogWidth = screenWidth * 0.8f

    var name by remember { mutableStateOf(profileData.name) }
    var birthday by remember { mutableStateOf(profileData.birthday) }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier =
                modifier
                    .width(dialogWidth)
                    .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column {
                Box(
                    modifier =
                        Modifier
                            .height(dialogWidth)
                            .clickable(
                                onClick = {
                                    ToastUtil.showToast(
                                        context = context,
                                        msg = context.getString(R.string.update_toast),
                                    )
                                },
                            ),
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.account_circle_icon),
                        modifier =
                            Modifier
                                .fillMaxSize(),
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { it ->
                        if (it.length <= 8) { // 글자 수 제한
                            name = it
                        }
                    },
                    label = { Text(text = stringResource(R.string.nick_name)) },
                    singleLine = true,
                    suffix = {
                        Text(
                            text = stringResource(R.string.text_length, name.length),
                            style = TextStyle(fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.W600),
                        )
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFF79747E),
                                shape = RoundedCornerShape(4.dp),
                            ).clip(shape = RoundedCornerShape(4.dp)),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { showDatePicker = true },
                                ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "$birthday")
                        Icon(
                            painter = painterResource(R.drawable.calendar_month_24),
                            contentDescription = stringResource(R.string.calendar_icon),
                            modifier = Modifier,
                        )
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = { onConfirm(name, birthday) }) {
                        Text(stringResource(R.string.update))
                    }
                }
            }
        }
        if (showDatePicker) {
            DaysDatePicker(
                initialDate = birthday,
                onDismiss = { showDatePicker = false },
                onDateSelected = { selectedDate ->
                    birthday = selectedDate
                    showDatePicker = false
                },
            )
        }
    }
}
