package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}

data class DroppedOperation(
    val type: String,
    val payload: String,
    val error: String,
    val droppedAt: Long = System.currentTimeMillis()
)

sealed class RepoResult {
    data class Success(val message: String) : RepoResult()
    data class Error(val message: String) : RepoResult()
}
