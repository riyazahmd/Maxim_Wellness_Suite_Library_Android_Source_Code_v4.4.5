package com.maximintegrated.maximsensorsapp.profile

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maximintegrated.algorithms.AlgorithmUser
import java.util.*

const val DEFAULT_USER_NAME = "unknown"

@Entity(tableName = "users")
data class User @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "userId")
    val userId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "username")
    var username: String = DEFAULT_USER_NAME,
    @ColumnInfo(name = "male")
    var isMale: Boolean = true,
    @ColumnInfo(name = "birthYear")
    var birthYear: Int = 1970,
    @ColumnInfo(name = "height")
    var height: Int = 170,
    @ColumnInfo(name = "weight")
    var weight: Int = 75,
    @ColumnInfo(name = "initialHr")
    var initialHr: Int = 70,
    @ColumnInfo(name = "sleepRestingHr")
    var sleepRestingHr: Int = 0,
    @ColumnInfo(name = "metric")
    var isMetric: Boolean = true
) {
    val heightInCm: Int
        get() = if (isMetric) height else (height * 2.54f).toInt()
    val weightInKg: Int
        get() = if (isMetric) weight else (weight * 0.453f).toInt()
    val age: Int
        get() = Calendar.getInstance().get(Calendar.YEAR) - birthYear

    fun update(
        username: String,
        isMale: Boolean,
        birthYear: Int,
        height: Int,
        weight: Int,
        initialHr: Int,
        sleepRestingHr: Int,
        isMetric: Boolean
    ) {
        this.username = username
        this.isMale = isMale
        this.birthYear = birthYear
        this.height = height
        this.weight = weight
        this.initialHr = initialHr
        this.sleepRestingHr = sleepRestingHr
        this.isMetric = isMetric
    }
}

fun User.toAlgorithmUser(): AlgorithmUser {
    val gender = if (isMale) AlgorithmUser.Gender.MALE else AlgorithmUser.Gender.FEMALE
    return AlgorithmUser(username, gender, birthYear, heightInCm, weightInKg, initialHr.toFloat(), sleepRestingHr.toFloat())
}
