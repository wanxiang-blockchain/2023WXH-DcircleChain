package com.base.foundation

import android.app.Activity
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.base.foundation.db.Account
import com.base.foundation.db.findByAddress
import com.base.foundation.utils.MakeToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun ShowTouchID(fragmentActivity:FragmentActivity, fromEthAddress:String): Aes? = withContext(Dispatchers.Main) {
	val account = Account.findByAddress(fromEthAddress)?:return@withContext null

	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
		return@withContext null
	}

	val biometricManager = BiometricManager.from(fragmentActivity)
	val auth = biometricManager.canAuthenticate()
	if (auth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
		return@withContext null
	}

	if (auth == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE) {
		return@withContext null
	}

	if (auth == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
		return@withContext null
	}

	val promptInfo = BiometricPrompt.PromptInfo.Builder()
		.setTitle(fragmentActivity.getString(R.string.touchId))
		.setSubtitle(fragmentActivity.getString(R.string.fingerprint_auth_login))
		.setNegativeButtonText(fragmentActivity.getString(R.string.cancel))
		.build()

	val executor = ContextCompat.getMainExecutor(fragmentActivity)
	val wait: Channel<Aes?> = Channel(1)

	val callback = object : BiometricPrompt.AuthenticationCallback() {
		override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
			super.onAuthenticationError(errorCode, errString)
			//取消操作
			if (errorCode == 13 || errorCode == 5 || errorCode == 10){
				return
			}
			//失败过多被锁定
			if (errorCode == 7){
				MakeToast.showShort(errString.toString())
				return
			}
			DCircleScope.launch {
				wait.send(null)
			}
		}

		override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
			super.onAuthenticationSucceeded(result)

			DCircleScope.launch {
				wait.send(Aes())
				return@launch
			}

		}
	}

	val biometricPrompt = BiometricPrompt(fragmentActivity, executor, callback)
	try {
		biometricPrompt.authenticate(promptInfo)
	} catch (e:Exception) {
		wait.send(null)
	}
	return@withContext wait.receive()
}

suspend fun ShowTouchID(activity:Activity, fromEthAddress:String): Aes? = withContext(Dispatchers.Main) {
	if (activity is FragmentActivity) {
		return@withContext ShowTouchID(activity, fromEthAddress)
	}

	return@withContext null
}