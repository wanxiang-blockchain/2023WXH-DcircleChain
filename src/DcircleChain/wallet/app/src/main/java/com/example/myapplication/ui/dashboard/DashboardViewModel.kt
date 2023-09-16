package com.example.myapplication.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {
    val text:MutableLiveData<String> = MutableLiveData<String>()
    init {
        // 在初始化ViewModel时，设置MutableLiveData的初始值
        text.value = "余额\nMATIC: NAN \n DcChain Coin(DCC): NAN \n DCTB: NAN"
    }
    // 创建一个公共方法来改变MutableLiveData中的值
    fun changeText(newText: String) {
        text.postValue(newText)
    }
}