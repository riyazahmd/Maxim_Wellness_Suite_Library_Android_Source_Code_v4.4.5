package com.maximintegrated.maximsensorsapp

import android.app.Application
import com.chibatching.kotpref.Kotpref
import com.maximintegrated.maximsensorsapp.profile.UserDao
import com.maximintegrated.maximsensorsapp.profile.UserDatabase
import com.rohitss.uceh.UCEHandler

class MaximSensorsApp : Application(){

    lateinit var userDao: UserDao
        private set

    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
        UCEHandler.Builder(applicationContext).build()
        val userDatabase = UserDatabase.getInstance(this)
        userDao = userDatabase.userDao()
    }
}