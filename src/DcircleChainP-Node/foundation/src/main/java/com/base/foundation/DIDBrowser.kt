package com.base.foundation

import android.os.Build
import com.blankj.utilcode.util.ActivityUtils
import java.util.Locale

class DIDBrowser(var baseUrl: String) {

	fun GetUserUrl(address:String):String {
		return "${baseUrl}/#/user/${address}/${getLanguage()}"
	}

	
	fun GetArticleUrl(address:String):String {
		return "${baseUrl}/#/article/${address}/${getLanguage()}"
	}
}


fun getLanguage():String{
	val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		ActivityUtils.getTopActivity().resources.configuration.locales[0]
	} else {
		ActivityUtils.getTopActivity().resources.configuration.locale
	}

	return when(locale.toLanguageTag()){
		Locale.ENGLISH.toLanguageTag() -> {
			 LangType.US.lang
		}
		Locale.TRADITIONAL_CHINESE.toLanguageTag() -> {
			 LangType.TW.lang
		}
		else -> LangType.CN.lang
	}
}

enum class LangType(val lang:String){
	US("en"),
	CN("zhHans"),
	TW("zhHant"),
}

var browser_:DIDBrowser = DIDBrowser(DCircleScanUrl)
fun GetDIDBrowser(): DIDBrowser {
	return browser_
}
