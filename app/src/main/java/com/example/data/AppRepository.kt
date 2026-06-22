package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val database: AppDatabase) {
    private val userProfileDao = database.userProfileDao()
    private val hourCorrespondenceDao = database.hourCorrespondenceDao()
    private val lunarMansionDao = database.lunarMansionDao()

    val profileFlow: Flow<UserProfile?> = userProfileDao.getProfileFlow()

    suspend fun getProfile(): UserProfile? {
        return userProfileDao.getProfile()
    }

    suspend fun insertOrUpdateProfile(profile: UserProfile) {
        userProfileDao.insertOrUpdate(profile)
    }

    suspend fun deleteProfile() {
        userProfileDao.deleteProfile()
    }

    suspend fun getCorrespondence(dayOfWeek: Int, hourNumber: Int): HourCorrespondence? {
        return hourCorrespondenceDao.getCorrespondence(dayOfWeek, hourNumber)
    }

    suspend fun getLunarMansion(index: Int): LunarMansion? {
        return lunarMansionDao.getMansion(index)
    }

    fun getAllMansionsFlow(): Flow<List<LunarMansion>> {
        return lunarMansionDao.getAllMansionsFlow()
    }
}
