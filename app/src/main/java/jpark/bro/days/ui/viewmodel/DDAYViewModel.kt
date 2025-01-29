package jpark.bro.days.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jpark.bro.days.domain.usecase.FetchAllDDayDataUseCase
import jpark.bro.days.domain.usecase.FilterDaysUseCase
import jpark.bro.days.ui.state.FilterOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DDAYViewModel @Inject constructor (
    private val fetchAllDDayDataUseCase: FetchAllDDayDataUseCase,
    private val filterDaysUseCase: FilterDaysUseCase,
) : ViewModel() {
    // D-DAY 전체 리스트
    private val _dDays = MutableStateFlow<List<Triple<String, String, String>>?>(emptyList())
    val dDays: StateFlow<List<Triple<String, String, String>>?> = _dDays

    // Dialog
    private val _selectedOption = MutableStateFlow<FilterOption>(FilterOption.All)
    val selectedOption: StateFlow<FilterOption> = _selectedOption

    // Filter 적용 리스트
    private val _filteredDDays = MutableStateFlow<List<Triple<String, String, String>>>(emptyList())
    val filteredDDays: StateFlow<List<Triple<String, String, String>>> = _filteredDDays

    // Filter 적용 리스트
    private val _previousDDays = MutableStateFlow<List<Triple<String, String, String>>>(emptyList())
    val previousDDays: StateFlow<List<Triple<String, String, String>>> = _previousDDays
    // Scroll "오늘"로 이동
    private val _scrollToIndex = MutableStateFlow<Int>(0)
    val scrollToIndex: StateFlow<Int> = _scrollToIndex

    init {
        loadDDays()
    }

    private fun loadDDays() {
        viewModelScope.launch {
            fetchAllDDayDataUseCase().collect { dDayList ->
                _dDays.value = dDayList
                applyFilter()
            }
        }
    }

    fun updateSelectedOption(option: FilterOption) {
        _selectedOption.value = option
        applyFilter()
    }

    private fun applyFilter() {
        val allDays = _dDays.value ?: emptyList()
        val option = _selectedOption.value
        val filteredList = filterDaysUseCase(allDays, option)
        _filteredDDays.value = filteredList

        val todayIndex = filteredList.indexOfFirst { it.third == "오늘" }
        if (todayIndex >= 0) {
            _scrollToIndex.value = todayIndex
        } else {
            _scrollToIndex.value = 0
        }
    }

    fun updatePreviousDDays() {
        _previousDDays.value = _filteredDDays.value
    }
}