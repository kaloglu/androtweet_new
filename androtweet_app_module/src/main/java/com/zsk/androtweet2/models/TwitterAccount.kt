package com.zsk.androtweet2.models

import com.twitter.sdk.android.core.TwitterAuthToken

/**
 * Created by kaloglu on 12/11/2017.
 */
data class TwitterAccount(
        val id: Long = 0,
        val name: String? = "",
        val realname: String? = "",
        val profilePic: String? = "",
        val authToken: CustomAuthToken = CustomAuthToken()
) {
    class CustomAuthToken : TwitterAuthToken {
        constructor() : super("", "")
        constructor(authToken: TwitterAuthToken?) : super(authToken?.token, authToken?.secret)

    }
}
