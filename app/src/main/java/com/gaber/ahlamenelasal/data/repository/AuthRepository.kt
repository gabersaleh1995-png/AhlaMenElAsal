package com.gaber.ahlamenelasal.data.repository

import android.app.Activity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    fun checkAdminStatus(uid: String): Flow<Boolean>
    fun login(email: String, pass: String): Flow<Result<FirebaseUser>>
    fun signUp(name: String, email: String, pass: String, isAdmin: Boolean): Flow<Result<FirebaseUser>>
    fun signInWithCredential(credential: AuthCredential): Flow<Result<FirebaseUser>>
    fun resetPassword(email: String): Flow<Result<String>>
    fun logout()
    fun saveUserData(uid: String, data: Map<String, Any>): Flow<Result<Unit>>
    fun verifyPhoneNumber(options: PhoneAuthOptions)
}
