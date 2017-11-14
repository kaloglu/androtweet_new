package com.zsk.androtweet2

import android.content.Context
import android.os.Bundle
import android.support.v4.view.LayoutInflaterCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.mikepenz.iconics.context.IconicsContextWrapper
import com.mikepenz.iconics.context.IconicsLayoutInflater2
import com.squareup.picasso.Picasso

open class BaseActivity : AppCompatActivity() {
    private object Holder {val INSTANCE = BaseActivity()}

    val RC_SIGN_IN: Int = 100
    val LOGOUT: Long = -99999
    val ADD_TWITTER_ACCOUNT: Long = -99998
    val MANAGE_TWITTER_ACCOUNT: Long = -99997
    val androTweetApp = AndroTweetApp.instance
    val TAG = "AndroTweet"

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory2(layoutInflater, IconicsLayoutInflater2(getDelegate()));
        initializeScreenObject()
        initFirebase()
        super.onCreate(savedInstanceState)
    }

    open fun initFirebase() {
        addEventListenerForFirebase()
    }

    open fun addEventListenerForFirebase() {
    }

    companion object {
        val baseActivity: BaseActivity by lazy { BaseActivity.Holder.INSTANCE }
    }

    open fun initializeScreenObject() {}

    open fun String?.getOrDefault(): String {
        if (this == null)
            return ""

        return this
    }

    open fun ImageView.loadFromUrl(context: Context, profilePic: String?) {
        if (!profilePic.isNullOrEmpty())
            Picasso.with(context).load(profilePic).into(this)
    }
}
