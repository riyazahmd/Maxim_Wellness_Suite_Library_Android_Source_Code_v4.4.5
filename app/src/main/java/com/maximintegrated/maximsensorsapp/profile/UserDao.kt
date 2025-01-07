package com.maximintegrated.maximsensorsapp.profile

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun observeUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getUsers(): List<User>

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUser(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}