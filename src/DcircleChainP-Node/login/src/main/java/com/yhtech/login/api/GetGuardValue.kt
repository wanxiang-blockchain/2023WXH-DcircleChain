package com.yhtech.login.api

import com.base.foundation.api.postJsonNoTokenSus
import com.base.foundation.api.Request
import com.base.foundation.api.Response
import com.base.foundation.getUs
import com.base.foundation.utils.Tuple
import com.google.gson.Gson

class GetGuardValueRequest {
	var address: String = ""
	var sign:String = ""
}


class GetGuardValueResponse {
	enum class Result(var int: Int) {
		InvalidSign(-1),
		NotFound(-404),
		Ok(0),
	}
	var result: Int = Result.Ok.int
	var guardValue: String = ""
}


suspend fun getGuardValue(request: GetGuardValueRequest): Tuple<GetGuardValueResponse, Error?> {
	val req = Request("", request)
	val (ret, err) = postJsonNoTokenSus<Response<GetGuardValueResponse>>("/im/user/getGuardValue",
		req, getUs().nf.get(), Response(0, GetGuardValueResponse::class.java)::class.java)
	if (err!=null) {
		return Tuple(GetGuardValueResponse(), err)
	}

	val data = Gson().fromJson(Gson().toJson(ret.data), GetGuardValueResponse::class.java)
	return Tuple(data, null)
}