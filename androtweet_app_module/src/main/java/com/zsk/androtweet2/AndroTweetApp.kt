package com.zsk.androtweet2

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.google.firebase.FirebaseApp
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.Drawer
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig


/**
 * Created by kaloglu on 22/10/2017.
 */
class AndroTweetApp : Application() {
    private var mTracker: Tracker? = null
    private object Holder {
        val INSTANCE = AndroTweetApp()
    }

    lateinit var accountHeader: AccountHeader
    lateinit var navigationDrawer: Drawer

    companion object {
        val instance: AndroTweetApp by lazy { Holder.INSTANCE }
        var daysAgo: Int = 0
        var userName: String? = null
        var tweetId: String? = null
        private val tweetID: Any? = null
    }

    private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate() {
        super.onCreate()
        initTwitter()
        FirebaseApp.initializeApp(this)
    }

    private fun initTwitter() {
        val twitterAuthConfig = TwitterAuthConfig(getString(R.string.com_twitter_sdk_android_CONSUMER_KEY), getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET))
        val config = TwitterConfig.Builder(this)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(twitterAuthConfig)
                .debug(true)
                .build()
        Twitter.initialize(config)
    }

    /**
     * Gets the default [Tracker] for this [Application].
     *
     * @return tracker
     */
    @Synchronized
    fun getDefaultTracker(): Tracker {
        if (mTracker == null) {
            val analytics = GoogleAnalytics.getInstance(this)
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker("123456")
        }
        return this.mTracker!!
    }

    fun verifyCameraPermissions(activity: Activity) {
        val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 1)
        }
    }

    fun sendScreenName2GA(mTracker: Tracker, screenName: String) {
        Log.i(screenName, "Setting screen name: " + screenName)
        mTracker.setScreenName(screenName)
        mTracker.send(HitBuilders.ScreenViewBuilder().build())
        Log.d("Tuttur3x GA ScreenName", "send ScreenName: \nScreenName: [$screenName]")
    }

    fun sendEvent(mTracker: Tracker, category: String, action: String) {
        sendEvent(mTracker, category, action, "")
    }

    fun sendEvent(mTracker: Tracker, category: String, action: String, label: String) {
        sendEvent(mTracker, category, action, label, 0, true)
    }

    fun sendEvent(category: String, action: String, label: String) {
        sendEvent(category, action, label, 0)
    }

    private fun sendEvent(category: String, action: String, label: String, value: Int) {
        sendEvent(category, action, label, value.toLong(), true)
    }

    fun sendEvent(category: String?, action: String?, label: String?, value: Long?, interraction: Boolean) {
        sendEvent(mTracker, category, action, label, value, interraction)
    }

    fun sendEvent(mTracker: Tracker?, category: String?, action: String?, label: String?, value: Long?, interraction: Boolean) {
        if (mTracker == null || category.isNullOrBlank() || action.isNullOrBlank())
            return

        Log.d("Tuttur3x GA Events", "Waiting for Sending GA events...")
        val eventBuilder = HitBuilders.EventBuilder().setCategory(category).setAction(action) //Google Analytics Event
        val customEvent = CustomEvent(category).putCustomAttribute("Action", action) //Crashlytics Event


        if (label != null && !label.isEmpty()) {
            eventBuilder.setLabel(label)
            customEvent.putCustomAttribute("Label", label)
        }
        if (value != Integer.MIN_VALUE.toLong()) {
            value?.let { eventBuilder.setValue(it) }
            customEvent.putCustomAttribute("Value", value)
        }
        if (!interraction)
            eventBuilder.setNonInteraction(false)

        mTracker.send(eventBuilder.build()) //Google Analytics Sender
        Answers.getInstance().logCustom(customEvent) //Crashlytics Sender

        Log.d("Tuttur3x GA Events", "sendEvent: \nCategory: [" + category + "] "
                + "\nAction: [" + action + "] "
                + (if (label != null && !label.isEmpty()) "\nLabel: [$label]" else "")
                + (if (value != Integer.MIN_VALUE.toLong()) "\nValue: [$value]" else "") +
                "\nNonInterraction: [" + !interraction + "]")

    }

}
