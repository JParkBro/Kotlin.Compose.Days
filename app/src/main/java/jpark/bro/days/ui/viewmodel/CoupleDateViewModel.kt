package jpark.bro.days.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jpark.bro.days.core.util.DateUtil
import jpark.bro.days.domain.usecase.LoadCoupleDateUseCase
import jpark.bro.days.domain.usecase.SaveCoupleDateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import javax.inject.Inject

@HiltViewModel
class CoupleDateViewModel @Inject constructor(
    private val loadCoupleDateUseCase: LoadCoupleDateUseCase,
    private val saveCoupleDateUseCase: SaveCoupleDateUseCase,
) : ViewModel() {

    private val _coupleDate = MutableStateFlow<LocalDate?>(null)
    val coupleDate: StateFlow<LocalDate?> = _coupleDate

    private val _formattedCoupleDate = MutableStateFlow<String?>(null)
    val formattedCoupleDate: StateFlow<String?> = _formattedCoupleDate

    private val _daysSinceStart = MutableStateFlow<Int>(0)
    val daysSinceStart: StateFlow<Int> = _daysSinceStart

    init {
        loadCoupleDate()
    }

    private fun loadCoupleDate() {
        viewModelScope.launch {
            loadCoupleDateUseCase().collect { dateMillis ->
                dateMillis?.let { updateState(it) }
            }
        }
    }

    fun saveCoupleDate(selectedDate: LocalDate) {
        viewModelScope.launch {
            val dateMillis = selectedDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            saveCoupleDateUseCase(dateMillis)
            updateState(dateMillis)
        }
    }

    private fun updateState(dateMillis: Long) {
        val date = DateUtil.formatDateToLocalDate(dateMillis)
        _coupleDate.value = date
        _formattedCoupleDate.value = DateUtil.formatDateToString(date)
        _daysSinceStart.value = DateUtil.calculateDaysSinceStart(date)
    }
}