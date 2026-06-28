package com.example.canhbao.data.auth

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FirebaseTokenProvider {

    suspend fun getToken(): String {

        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Chưa đăng nhập")

        val tokenResult = withContext(Dispatchers.IO) {
            Tasks.await(user.getIdToken(false))
        }

        return tokenResult.token
            ?: throw Exception("Không lấy được token")
    }
}