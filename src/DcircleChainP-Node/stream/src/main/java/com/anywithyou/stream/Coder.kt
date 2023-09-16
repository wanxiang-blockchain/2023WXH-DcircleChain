package com.anywithyou.stream

interface ConstructorJsonDecoder {
    fun decodeJson(json: String): Pair<Any, Error?>
}

interface ConstructorJsonEncoder {
    fun <T> encodeJson(instance: T): String
}

interface JsonDecoder {
    fun decodeJson(json: String): Error?
}

interface JsonEncode {
    fun encodeJson(): String?
}

class RawJson: JsonDecoder, JsonEncode {
    private var raw: String? = null

    override fun decodeJson(json: String): Error? {
        raw = json
        return null
    }

    override fun encodeJson(): String? {
        return raw
    }
}

