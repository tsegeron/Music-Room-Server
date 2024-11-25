package com.laru.data.repo

import com.laru.data.model.User
import com.laru.db.user.UserDao

interface UserRepository {
    suspend fun addUser(user: User): Int?
    suspend fun updateUser(user: User)
    suspend fun deleteUser(email: String): Boolean
    suspend fun getUserById(id: Int): User?
    suspend fun getUserByEmail(email: String): User?
}
