package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun deleteProfile()
}

@Dao
interface HourCorrespondenceDao {
    @Query("SELECT * FROM hour_correspondences WHERE dayOfWeek = :dayOfWeek AND hourNumber = :hourNumber LIMIT 1")
    suspend fun getCorrespondence(dayOfWeek: Int, hourNumber: Int): HourCorrespondence?

    @Query("SELECT * FROM hour_correspondences ORDER BY dayOfWeek, hourNumber")
    fun getAllCorrespondencesFlow(): Flow<List<HourCorrespondence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(correspondences: List<HourCorrespondence>)
}

@Dao
interface LunarMansionDao {
    @Query("SELECT * FROM lunar_mansions WHERE `index` = :index LIMIT 1")
    suspend fun getMansion(index: Int): LunarMansion?

    @Query("SELECT * FROM lunar_mansions ORDER BY `index` ASC")
    fun getAllMansionsFlow(): Flow<List<LunarMansion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mansions: List<LunarMansion>)
}
