package com.deeromptech.firebasecodelab.firebase

import com.deeromptech.firebasecodelab.model.user.User
import com.deeromptech.firebasecodelab.utils.Constant.USERS_COLLECTION
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage

class FirebaseDb {
    private val firebaseAuth = Firebase.auth
    private val firebaseStorage = Firebase.storage.reference
    private val usersCollectionRef = Firebase.firestore.collection(USERS_COLLECTION)

    val userUid = FirebaseAuth.getInstance().currentUser?.uid

    fun loginWithEmailAndPassword(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result.user?.let {
                        onResult(true, null)
                    } ?: onResult(false, "User not found")
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun createNewUser(
        email: String, password: String
    ) = firebaseAuth.createUserWithEmailAndPassword(email, password)

    fun saveUserInformation(
        userUid: String,
        user: User
    ) = usersCollectionRef.document(userUid).set(user)

    fun checkUserByEmail(email: String, onResult: (String?, Boolean?) -> Unit) {
        usersCollectionRef.whereEqualTo("email", email).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = it.result.toObjects(User::class.java)
                    if (user.isEmpty())
                        onResult(null, false)
                    else
                        onResult(null, true)
                } else
                    onResult(it.exception.toString(), null)
            }
    }

    fun resetPassword(email: String) = firebaseAuth.sendPasswordResetEmail(email)

    fun logout() = Firebase.auth.signOut()

    fun getUser() = usersCollectionRef
        .document(FirebaseAuth.getInstance().currentUser!!.uid)

    fun uploadUserProfileImage(image: ByteArray, imageName: String): UploadTask {
        deleteAllProfileImages(firebaseAuth.currentUser!!.uid)
        val imageRef = firebaseStorage.child("profileImages")
            .child(firebaseAuth.currentUser!!.uid)
            .child(imageName)
        return imageRef.putBytes(image)
    }

    private fun deleteAllProfileImages(userId: String) {
        val profileImagesRef = firebaseStorage.child("profileImages").child(userId)

        profileImagesRef.listAll()
            .addOnSuccessListener { result ->
                result.items.forEach { imageRef ->
                    imageRef.delete()
                        .addOnSuccessListener {
                            // Image deleted successfully
                            // You can add additional logging or handling here if needed
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors that occurred during deletion
                            // You may want to log the error or take appropriate action
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred while listing items
                // You may want to log the error or take appropriate action
            }
    }

    fun getImageUrl(
        name: String,
        email: String,
        phone: String,
        imagePath: String,
        onResult: (User?, String?) -> Unit,
    ) {
        if (imagePath.isNotEmpty())
            firebaseStorage.child("profileImages")
                .child(firebaseAuth.currentUser!!.uid)
                .child(imagePath).downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val imageUrl = it.result.toString()
                        val user = User(
                            name,
                            email,
                            phone,
                            imageUrl,
                        )
                        onResult(user, null)
                    } else
                        onResult(null, it.exception.toString())

                } else {
            val user = User(
                name,
                email,
                phone,
                "",
            )
            onResult(user, null)
        }
    }

    fun updateUserInformation(user: User) =
        Firebase.firestore.runTransaction { transaction ->
            val userPath = usersCollectionRef.document(Firebase.auth.currentUser!!.uid)
            if (user.imagePath.isNotEmpty()) {
                transaction.set(userPath, user)
            } else {
                val imagePath = transaction.get(userPath)["imagePath"] as String
                user.imagePath = imagePath
                transaction.set(userPath, user)
            }
        }
}