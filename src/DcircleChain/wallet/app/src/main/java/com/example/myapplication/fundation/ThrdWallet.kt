package com.example.myapplication.fundation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast


enum class ThirdWallet{
    MetaMask,
    TrustWallet,
    CoinbaseWallet
}

class ThirdWalletHelper {
    companion object {
        var Packages:Map<String,String> = hashMapOf<String,String>(
            ThirdWallet.MetaMask.name to "io.metamask",
            ThirdWallet.TrustWallet.name to "com.wallet.crypto.trustapp",
            ThirdWallet.CoinbaseWallet.name to "org.toshi",
        )

        fun getWalletPackage(appName:ThirdWallet):String {
            val packageName = Packages.get(appName.name)
            if (packageName != null)  {
                return packageName
            }
            Log.e("ThirdWalletHelper","${appName.name} is not support!")
            throw Exception("${appName.name} is not support!")
        }

        private fun getWalletIntentPackage(appName:ThirdWallet):String {
            val packageName = Packages.get(appName.name)
            if (packageName != null)  {
                return packageName+".intent.action.MAIN"
            }
            Log.e("ThirdWalletHelper","${appName.name} is not support!")
            throw Exception("${appName.name} is not support!")
        }

        // 判断是否安装某个应用
        fun hasInstalled(context: Context, appName: ThirdWallet): Boolean {
            return try {
                val packageManager = context.packageManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val intent = Intent(getWalletIntentPackage(appName))
                    val resolveInfo = packageManager.queryIntentActivities(intent,
                        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
                    resolveInfo.forEach{
                        Toast.makeText(context,it.activityInfo.name,Toast.LENGTH_SHORT).show()
                    }
                    return resolveInfo.isNotEmpty()
                } else {
                    val intent = Intent()
                    intent.setPackage(getWalletPackage(appName))
                    val resolveInfo = packageManager.queryIntentActivities(intent,0)
                    resolveInfo.forEach{
                        Toast.makeText(context,it.activityInfo.name,Toast.LENGTH_SHORT).show()
                    }
                    return resolveInfo.isNotEmpty()
                }
                true
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("ThirdWalletHelper",e.message.toString())
                false
            }
        }
    }
}
