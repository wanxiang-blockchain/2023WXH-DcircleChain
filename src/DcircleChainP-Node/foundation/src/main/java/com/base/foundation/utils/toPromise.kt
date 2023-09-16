package com.base.foundation.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun <T>toPromise(block:suspend () -> T):Promise<T> {
	return Promise(Promise.Pend { resolve, _ ->
		CoroutineScope(Dispatchers.Main).launch {
			resolve.run(block())
		}
	})
}