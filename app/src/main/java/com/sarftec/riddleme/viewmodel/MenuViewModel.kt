package com.sarftec.riddleme.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class MenuNotification {
    object ShowAboutDialog : MenuNotification()
    object NeutralNotification : MenuNotification()
}

@HiltViewModel
class MenuViwModel @Inject constructor() : ViewModel() {

    private val _menuNotification = MutableLiveData<MenuNotification>()
    val menuNotification
    get() = _menuNotification

    fun showAboutDialog() {
        _menuNotification.value = MenuNotification.ShowAboutDialog
    }

    fun neutralMenuNotification() {
        _menuNotification.value = MenuNotification.NeutralNotification
    }
}