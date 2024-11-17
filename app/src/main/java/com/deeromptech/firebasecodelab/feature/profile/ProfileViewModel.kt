package com.deeromptech.firebasecodelab.feature.profile

import androidx.lifecycle.ViewModel
import com.deeromptech.firebasecodelab.firebase.FirebaseDb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {

    private val firebaseDatabase: FirebaseDb by lazy { FirebaseDb() }

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Nothing)
    val state = _state.asStateFlow()



}

sealed class ProfileState {
    object Nothing : ProfileState()
    object Loading : ProfileState()
    object SuccessLogout : ProfileState()
    object Error : ProfileState()
}