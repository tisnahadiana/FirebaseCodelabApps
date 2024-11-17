package com.deeromptech.firebasecodelab.feature.auth.signup

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeromptech.firebasecodelab.R
import com.deeromptech.firebasecodelab.feature.auth.login.SignInState
import com.deeromptech.firebasecodelab.firebase.FirebaseDb
import com.deeromptech.firebasecodelab.model.user.User
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

class RegisterViewModel : ViewModel() {

    private val firebaseDatabase: FirebaseDb by lazy { FirebaseDb() }

    private val _state = MutableStateFlow<SignUpState>(SignUpState.Nothing)
    val state = _state.asStateFlow()

    fun signUp(name: String, email: String, password: String) {
        _state.value = SignUpState.Loading
        // Firebase signIn
        firebaseDatabase.createNewUser(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // After user is created, update profile with the display name
                    task.result.user?.let {
                        it.updateProfile(
                            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                        ).addOnCompleteListener {
                            if (it.isSuccessful) {
                                // Save additional user information to Firestore
                                val newUser = User(
                                    name = name,
                                    email = email,
                                    phone = "",
                                    imagePath = ""
                                )
                                val userUid = FirebaseAuth.getInstance().currentUser?.uid
                                firebaseDatabase.saveUserInformation(userUid.toString(), newUser)
                                    .addOnSuccessListener {
                                        _state.value = SignUpState.Success
                                    }
                                    .addOnFailureListener {
                                        _state.value = SignUpState.Error
                                    }
                            } else {
                                _state.value = SignUpState.Error
                            }
                        }
                    }
                } else {
                    _state.value = SignUpState.Error
                }
            }
    }

    fun signUpWithGoogle(context: Context) {
        _state.value = SignUpState.Loading
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
                                                    _state.value = SignUpState.Error
                                                    return@checkUserByEmail
                                                }

                                                if (exists == true) {
                                                    _state.value = SignUpState.SuccessGoogle
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
                                                            _state.value = SignUpState.SuccessGoogle
                                                        }
                                                        .addOnFailureListener {
                                                            _state.value = SignUpState.Error
                                                        }

                                                }
                                            }
                                        } else {
                                            _state.value = SignUpState.Error
                                        }
                                    } else {
                                        _state.value = SignUpState.Error
                                    }
                                }

                        } catch (e: GoogleIdTokenParsingException) {
                            _state.value = SignUpState.Error
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = SignUpState.Error
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

sealed class SignUpState {
    object Nothing : SignUpState()
    object Loading : SignUpState()
    object Success : SignUpState()
    object SuccessGoogle : SignUpState()
    object Error : SignUpState()
}