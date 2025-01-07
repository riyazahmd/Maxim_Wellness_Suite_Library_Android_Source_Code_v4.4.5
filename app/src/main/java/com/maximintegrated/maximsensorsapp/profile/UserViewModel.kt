package com.maximintegrated.maximsensorsapp.profile

import androidx.lifecycle.*
import com.maximintegrated.maximsensorsapp.DeviceSettings
import com.maximintegrated.maximsensorsapp.bpt.BptSettings
import kotlinx.coroutines.launch

class UserViewModel(private val userDao: UserDao) : ViewModel() {

    val users: LiveData<List<User>> = userDao.observeUsers()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun selectUserFromSettings(userId: String) {
        if (userId != "") {
            viewModelScope.launch {
                val user = userDao.getUser(userId)
                _currentUser.value = user
            }
        }
    }

    fun selectUser(user: User) {
        _currentUser.value = user
        DeviceSettings.selectedUserId = user.userId
        BptSettings.currentUser = user.username
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            userDao.insertUser(user)
            selectUser(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            userDao.updateUser(user)
            if (user.userId == DeviceSettings.selectedUserId) {
                _currentUser.value = user
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            userDao.deleteUser(user)
            if (user.userId == DeviceSettings.selectedUserId) {
                DeviceSettings.selectedUserId = ""
                _currentUser.value = null
            }
        }
    }

    fun getCurrentUser(): User {
        return _currentUser.value ?: User()
    }
}

@Suppress("UNCHECKED_CAST")
class UserViewModelFactory(
    private val userDao: UserDao
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (UserViewModel(userDao) as T)
}