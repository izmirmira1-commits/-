package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String,
    val birthDate: String, // YYYY-MM-DD
    val birthPlace: String,
    val country: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@Entity(tableName = "hour_correspondences", primaryKeys = ["dayOfWeek", "hourNumber"])
data class HourCorrespondence(
    val dayOfWeek: Int, // 0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday
    val hourNumber: Int, // 1 to 24 (1-12 Daytime, 13-24 Nighttime)
    val planetArabic: String,
    val planetSpiritual: String,
    val angelName: String,
    val prophetName: String,
    val divineName: String,
    val letters: String,
    val incense: String,
    val color: String,
    val temperament: String,
    val direction: String,
    val positiveWorks: String,
    val caution: String
)

@Entity(tableName = "lunar_mansions")
data class LunarMansion(
    @PrimaryKey
    val index: Int, // 1 to 28
    val name: String,
    val degreeStart: Double,
    val degreeEnd: Double,
    val zodiacSign: String,
    val element: String,
    val nature: String, // سعيد / نحس / ممتزج
    val meanings: String,
    val actions: String
)
