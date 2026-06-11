package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.sharmakhata.pqmzvk.data.model.Staff
import com.aistudio.sharmakhata.pqmzvk.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val staffRepository: StaffRepository
) : ViewModel() {

    private val _staffList = MutableStateFlow<List<Staff>>(emptyList())
    val staffList: StateFlow<List<Staff>> = _staffList.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    fun loadStaff() {
        viewModelScope.launch(Dispatchers.IO) {
            _staffList.value = staffRepository.getStaffList()
        }
    }

    fun addStaff(name: String, phone: String, role: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _operationState.value = OperationState.Loading
            when (val result = staffRepository.addStaff(name, phone, role, context)) {
                is RepoResult.Success -> {
                    _operationState.value = OperationState.Success(result.message)
                    loadStaff() // Refresh list after add
                }
                is RepoResult.Error -> {
                    _operationState.value = OperationState.Error(result.message)
                }
            }
        }
    }

    fun removeStaff(id: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _operationState.value = OperationState.Loading
            // Optimistically remove from local list
            val previous = _staffList.value
            _staffList.value = previous.filter { it.id != id }

            when (val result = staffRepository.removeStaff(id, context)) {
                is RepoResult.Success -> {
                    _operationState.value = OperationState.Success(result.message)
                }
                is RepoResult.Error -> {
                    // Roll back optimistic update on error
                    _staffList.value = previous
                    _operationState.value = OperationState.Error(result.message)
                }
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}
