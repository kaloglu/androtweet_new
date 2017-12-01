package com.zsk.androtweet2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import org.jetbrains.anko.alert
import org.jetbrains.anko.email
import java.util.*


class SplashScreen : BaseActivity() {
    var signInProviders = Arrays.asList(
            AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
            , AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
//            , AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()
//            AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
//            , AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build()
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        getRemoteSettings()
    }

    override fun onResume() {
        super.onResume()
        if (firebaseService.currentUser != null) {
            onActivityResult(-1, -1, intent)
        } else {
            val build = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(signInProviders)
                    .setIsSmartLockEnabled(true, true)
                    .setAllowNewEmailAccounts(true)
                    .build()

            startActivityForResult(
                    build,
                    RC_SIGN_IN)
        }
    }

    private fun getRemoteSettings() {
        with(firebaseService) {
            var cacheSize = 3600L

            if (config.info.configSettings.isDeveloperModeEnabled)
                cacheSize = 0

            config.fetch(cacheSize).addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    config.getKeysByPrefix("twitter_").asSequence().forEach { configKey ->
                        getTwitterSettings()?.let { twitterSettings ->
                            when {
                                twitterSettings.getString(configKey, "") != config.getString(configKey) -> {
                                    twitterSettings.edit().putString(configKey, config.getString(configKey)).apply()
                                }
                            }

                        }
                    }
                    config.activateFetched()
                } else {
                    alert("Maintenance Time! plase try again few later...") {
                        negativeButton("Close App", {
                            finish()
                        })
                        neutralPressed("Report", {
                            email(
                                    "support@androtweet.net",
                                    "App Working Issue[SplashScreen]",
                                    "App doesn't open I do not know why!"
                            )
                        })
                    }
                }

            }.addOnFailureListener { exception ->
                handleException(exception.message)
            }


        }
    }

    private fun handleException(errorMessage: String?) {
        alert("Something goes Wrong") {
            negativeButton("Close App", {
                finish()
            })
            positiveButton("Try Again", {
                initTwitter(intent)
            })
            neutralPressed("Report", {
                email(
                        "support@androtweet.net",
                        "App Working Issue[SplashScreen]",
                        "App doesn't open I do not know why!\n Reason[$errorMessage]"
                )
            })
        }
    }

    private fun twitterImplementation(intent: Intent) {
        val twitterSettings = getTwitterSettings()
        val consumerKey = twitterSettings?.getString("twitter_consumer_key", "")
        val consumerSecret = twitterSettings?.getString("twitter_consumer_secret", "")

        if (consumerKey == "" || consumerSecret == "")
            handleException("does not get consumer keys")

        val twitterAuthConfig = TwitterAuthConfig(consumerKey, consumerSecret)
        val twitterConfig = TwitterConfig.Builder(this@SplashScreen)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(twitterAuthConfig)
                .debug(BuildConfig.DEBUG)
                .build()
        Twitter.initialize(twitterConfig)

        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var idpResponse: IdpResponse? = null
        val intent = Intent(this, MainActivity::class.java)
        when (requestCode) {
            RC_SIGN_IN -> idpResponse = IdpResponse.fromResultIntent(data)
        }
        idpResponse?.let { intent.putExtra("my_token", idpResponse!!.idpToken) }

        if (resultCode == Activity.RESULT_OK) {
            twitterImplementation(intent)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}


