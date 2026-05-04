package com.wolfpack.data.repository

import com.wolfpack.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun registerWithEmail(
        email: String,
        password: String,
        profile: UserProfile
    ): Result<FirebaseUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user!!
        firestore.collection("users")
            .document(user.uid)
            .set(profile.copy(uid = user.uid).toMap())
            .await()
        user
    }

    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user!!
        }

    suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user!!
        if (result.additionalUserInfo?.isNewUser == true) {
            val profile = UserProfile(
                uid = user.uid,
                nombre = user.displayName?.split(" ")?.firstOrNull() ?: "",
                apellido = user.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                email = user.email ?: "",
                telefono = user.phoneNumber ?: ""
            )
            firestore.collection("users")
                .document(user.uid)
                .set(profile.toMap())
                .await()
        }
        user
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getUserProfile(uid: String): Result<UserProfile> = runCatching {
        val doc = firestore.collection("users").document(uid).get().await()
        UserProfile.fromMap(uid, doc.data ?: emptyMap())
    }
}