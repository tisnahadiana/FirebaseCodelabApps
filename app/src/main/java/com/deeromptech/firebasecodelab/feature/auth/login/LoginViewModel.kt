package com.deeromptech.firebasecodelab.feature.auth.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeromptech.firebasecodelab.R
import com.deeromptech.firebasecodelab.firebase.FirebaseDb
import com.deeromptech.firebasecodelab.model.user.User
import com.deeromptech.firebasecodelab.utils.AuthResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class LoginViewModel : ViewModel() {

    private val firebaseDatabase: FirebaseDb by lazy { FirebaseDb() }

    private val _state = MutableStateFlow<SignInState>(SignInState.Nothing)
    val state = _state.asStateFlow()

    fun signIn(email: String, password: String) {
        _state.value = SignInState.Loading
        // Firebase signIn
        firebaseDatabase.loginWithEmailAndPassword(email, password) { success, error ->
            if (success) {
                _state.value = SignInState.Success
            } else {
                _state.value = SignInState.Error
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        _state.value = SignInState.Loading
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential
                if (credential is CustomCredential) {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            val googleIdTokenCredential =
                                GoogleIdTokenCredential.createFrom(credential.data)

                            val firebaseCredential = GoogleAuthProvider.getCredential(
                                googleIdTokenCredential.idToken,
                                null
                            )

                            FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        val user = it.result.user
                                        if (user != null) {
                                            firebaseDatabase.checkUserByEmail(user.email!!) { error, exists ->
                                                if (error != null) {
                                                    _state.value = SignInState.Error
                                                    return@checkUserByEmail
                                                }

                                                if (exists == true) {
                                                    _state.value = SignInState.Success
                                                } else {
                                                    // Create new user
                                                    val newUser = User(
                                                        name = user.displayName.orEmpty(),
                                                        email = user.email.orEmpty(),
                                                        phone = user.phoneNumber.orEmpty(),
                                                        imagePath = user.photoUrl?.toString().orEmpty()
                                                    )

                                                    firebaseDatabase.saveUserInformation(user.uid, newUser)
                                                        .addOnSuccessListener {
                                                            _state.value = SignInState.Success
                                                        }
                                                        .addOnFailureListener {
                                                            _state.value = SignInState.Error
                                                        }

                                                }
                                            }
                                        } else {
                                            _state.value = SignInState.Error
                                        }
                                    } else {
                                        _state.value = SignInState.Error
                                    }
                                }

                        } catch (e: GoogleIdTokenParsingException) {
                            _state.value = SignInState.Error
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = SignInState.Error
            }
        }

    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

}

sealed class SignInState {
    object Nothing : SignInState()
    object Loading : SignInState()
    object Success : SignInState()
    object Error : SignInState()
}