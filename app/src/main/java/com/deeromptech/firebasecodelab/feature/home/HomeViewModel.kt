package com.deeromptech.firebasecodelab.feature.home

import androidx.lifecycle.ViewModel
import com.deeromptech.firebasecodelab.firebase.FirebaseDb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val firebaseDatabase: FirebaseDb by lazy { FirebaseDb() }

    private val _state = MutableStateFlow<HomeState>(HomeState.Nothing)
    val state = _state.asStateFlow()



}

sealed class HomeState {
    object Nothing : HomeState()
    object Loading : HomeState()
    object Success : HomeState()
    object Error : HomeState()
}