package com.example.myapplication.fundation.db

import android.content.Context
import androidx.room.Room
import com.example.myapplication.fundation.wallet.AppDatabase
import com.example.myapplication.fundation.wallet.UserDao

class DCWalletDataBase {
    companion object {
        fun getUserDao(context: Context): UserDao? {
            // 不应该在UI线程搞 应该放在IO线程 这里只是测试
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java, "dcwallet"
            ).allowMainThreadQueries().build().userDao()
        }
    }
}