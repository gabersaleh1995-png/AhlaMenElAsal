package com.gaber.ahlamenelasal.data.repository

import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override fun checkAdminStatus(uid: String): Flow<Boolean> = callbackFlow {
        val listener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val isAdmin = snapshot?.getBoolean("isAdmin") ?: false
                trySend(isAdmin)
            }
        awaitClose { listener.remove() }
    }

    override fun login(email: String, pass: String): Flow<Result<FirebaseUser>> = callbackFlow {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(auth.currentUser!!))
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Unknown login error")))
                }
            }
        awaitClose { }
    }

    override fun signUp(name: String, email: String, pass: String, isAdmin: Boolean): Flow<Result<FirebaseUser>> = callbackFlow {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates).addOnCompleteListener {
                        val userData = mapOf(
                            "uid" to user.uid,
                            "name" to name,
                            "email" to email,
                            "isAdmin" to isAdmin
                        )
                        db.collection("users").document(user.uid).set(userData, SetOptions.merge())
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    trySend(Result.success(user))
                                } else {
                                    trySend(Result.failure(dbTask.exception ?: Exception("Failed to save user data")))
                                }
                            }
                    }
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Sign up failed")))
                }
            }
        awaitClose { }
    }

    override fun signInWithCredential(credential: AuthCredential): Flow<Result<FirebaseUser>> = callbackFlow {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(auth.currentUser!!))
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Credential sign in failed")))
                }
            }
        awaitClose { }
    }

    override fun resetPassword(email: String): Flow<Result<String>> = callbackFlow {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    trySend(Result.failure(Exception("هذا البريد غير مسجل")))
                } else {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                trySend(Result.success("تم إرسال رابط إعادة التعيين"))
                            } else {
                                trySend(Result.failure(task.exception ?: Exception("Error resetting password")))
                            }
                        }
                }
            }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun saveUserData(uid: String, data: Map<String, Any>): Flow<Result<Unit>> = callbackFlow {
        db.collection("users").document(uid).set(data, SetOptions.merge())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) trySend(Result.success(Unit))
                else trySend(Result.failure(task.exception ?: Exception("Failed to save user data")))
            }
        awaitClose { }
    }

    override fun verifyPhoneNumber(options: PhoneAuthOptions) {
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
