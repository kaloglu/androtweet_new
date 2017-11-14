package com.zsk.androtweet2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import java.util.*

class SplashScreen : BaseActivity() {
    var signInProviders = Arrays.asList(
            AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
//            , AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
//            AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
//            AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
//            AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        if (FirebaseService().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            val idpResponse = IdpResponse.fromResultIntent(data)
            startActivity(Intent(this, MainActivity::class.java).putExtra("my_token", idpResponse!!.idpToken))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
