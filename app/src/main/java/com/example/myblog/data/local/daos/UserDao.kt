package com.example.myblog.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myblog.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM user_table WHERE id = :userId")
    fun getUser(userId: String): Flow<User?>

    @Query("DELETE FROM user_table WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}