package com.caiyu.deepseek_for_android.utils

class ApiConstants {
    companion object {
        const val BASE_URL = "https://api.deepseek.com/"
        const val BASE_URL_BETA = "https://api.deepseek.com/beta/"
        const val BASE_URL_WITH_SILICONFLOW = "https://api.siliconflow.cn/v1/"

        const val API_CHAT_COMPLETIONS = BASE_URL + "chat/completions"
        const val API_CHAT_COMPLETIONS_WITH_SILICONFLOW = BASE_URL_WITH_SILICONFLOW + "chat/completions"
        const val API_USER_BALANCE = BASE_URL + "user/balance"
    }
}
class MediaType {
    companion object {
        const val APPLICATION_JSON = "application/json"
        const val APPLICATION_XML = "application/xml"
        const val APPLICATION_OCTET_STREAM = "application/octet-stream"
        const val FORM_DATA = "multipart/form-data"
        const val TEXT_PLAIN = "text/plain"
        const val IMAGE_PNG = "image/png"
        const val IMAGE_JPG = "image/jpg"
    }
}

class Header {
    companion object {
        const val CONTENT_TYPE = "Content-Type"
        const val CONTENT_LENGTH = "Content-Length"
        // request header
        const val ACCEPT = "Accept"
        const val ACCEPT_CHARSET = "Accept-Charset"
        const val ACCEPT_ENCODING = "Accept-Encoding"
        const val ACCEPT_LANGUAGE = "Accept-Language"
        const val AUTHORIZATION = "Authorization"
        const val USER_AGENT = "User-Agent"
        const val HOST = "Host"
        const val CONNECTION = "Connection"
        const val REFERER = "Referer"
        const val ORIGIN = "Origin"
    }
}

