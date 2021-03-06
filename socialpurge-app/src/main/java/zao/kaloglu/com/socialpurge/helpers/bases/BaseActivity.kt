package zao.kaloglu.com.socialpurge.helpers.bases

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.LayoutInflaterCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.mikepenz.iconics.context.IconicsContextWrapper
import com.mikepenz.iconics.context.IconicsLayoutInflater2
import com.squareup.picasso.Picasso
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterAuthException
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.email
import zao.kaloglu.com.socialpurge.R
import zao.kaloglu.com.socialpurge.fragments.BaseFragment
import zao.kaloglu.com.socialpurge.fragments.TwitterTimelineFragment
import zao.kaloglu.com.socialpurge.helpers.AppSettings
import zao.kaloglu.com.socialpurge.helpers.utils.FirebaseService

open class BaseActivity : AppCompatActivity() {
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mRewardedAd: RewardedVideoAd

    open fun loadAds() = loadRewardedAd()

    internal fun loadRewardedAd() =
            mRewardedAd.loadAd(AppSettings.ADMOB_REWARDED_VIDEO_UNIT_ID, AdRequest.Builder().build())

    private fun loadInterstitialAd() = mInterstitialAd.loadAd(AdRequest.Builder().build())

    fun showMobileAd() {
        mRewardedAd.show()
    }

    val TAG = this.javaClass.simpleName
    val TOKEN_ERROR = "Failed to get request token"
    val CANCEL_LOGIN = "Failed to get authorization, bundle incomplete"

    companion object {

        var firebaseService = FirebaseService()
        val socialPurgeApp = zao.kaloglu.com.socialpurge.SocialPurgeApp.instance

    }

    fun getTwitterSettings(): SharedPreferences? =
            getSharedPreferences("twitter_settings", Context.MODE_PRIVATE)

    fun getAdsSettings(): SharedPreferences? =
            getSharedPreferences("ads_settings", Context.MODE_PRIVATE)

    fun getAppSettings(): SharedPreferences? =
            getSharedPreferences("app_settings", Context.MODE_PRIVATE)


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory2(layoutInflater, IconicsLayoutInflater2(delegate))
        initializeScreenObject()
        initFirebase()

        initAds()
        super.onCreate(savedInstanceState)
    }

    lateinit var progresDialog: Dialog
    fun showProgressDialog() {
        progresDialog = createSpinnerProgress(this)
        progresDialog.show()
    }


    fun hideProgressDialog() {
        progresDialog.hide()
    }

    private var mInterstitialAdShowCount = 0

    protected open fun initAds() {
        MobileAds.initialize(this)

        //Rewarded
        mRewardedAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedAd.rewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onRewardedVideoAdClosed() {
                Log.e("Admob", "RewardedAdLoaded")
            }

            override fun onRewardedVideoAdLeftApplication() {
                Log.e("Admob", "RewardedVideoAdLeftApplication")
            }

            override fun onRewardedVideoAdLoaded() {
                alert(
                        "Do you want get select more items?",
                        "Freemium limit (${socialPurgeApp.maxSelectionCount} items)"
                ) {
                    negativeButton("No, That is enough!", {
                        loadInterstitialAd()
                    })
                    positiveButton("Yes, Show Video.", {
                        showMobileAd()
                    })
                }.show()
            }

            override fun onRewardedVideoAdOpened() {
                hideProgressDialog()
                Log.e("Admob", "RewardedVideoAdOpened")
            }

            override fun onRewarded(rewardedItem: RewardItem?) {
                //        Log.e("Admob", "Rewarded " + rewardedItem?.amount + " " + rewardedItem?.type)
                socialPurgeApp.rewardCount += rewardedItem?.amount ?: 0
                alert { "Congratulations! You can select ${socialPurgeApp.maxSelectionCount} items now!" }
            }

            override fun onRewardedVideoStarted() {
                Log.e("Admob", "RewardedVideoStarted")
            }

            override fun onRewardedVideoAdFailedToLoad(p0: Int) {
                Log.e("Admob", "RewardedAdFailedLoad => " + p0)
                mInterstitialAdShowCount+=1
                if (mInterstitialAdShowCount % 5 == 0)
                    loadInterstitialAd()
                else {
                    hideProgressDialog()
                    getSelectLimitAlert()
                }
            }
        }

        //Interstitial
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = AppSettings.ADMOB_INTERSTITIAL_UNIT_ID
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.e("Admob", "AdLoaded")
                mInterstitialAd.show()
                mInterstitialAdShowCount++
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                Log.e("Admob", "onAdFailedToLoad: " + errorCode)
                getSelectLimitAlert()
                hideProgressDialog()
            }

            override fun onAdOpened() {
                Log.e("Admob", "AdOpened")
                hideProgressDialog()
            }

            override fun onAdLeftApplication() {
                Log.e("Admob", "AdLeftApplication")
            }

            override fun onAdClosed() {
                getSelectLimitAlert()
            }
        }
    }

    private fun getSelectLimitAlert() {
        alert(
                "You can select only ${socialPurgeApp.maxSelectionCount} for a while. ",
                "Sorry! :("
        ) {
            positiveButton("ok", {})
        }.show()
    }

    open fun initFirebase() {
        addEventListenerForFirebase()
    }

    open fun addEventListenerForFirebase() {
    }

    open fun initializeScreenObject() {}

    open fun ImageView.loadFromUrl(context: Context, profilePic: String?) {
        if (!profilePic.isNullOrEmpty())
            Picasso.with(context).load(profilePic).into(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE) {
            handleTwitterAuthError(data!!)
        }
    }

    private fun handleTwitterAuthError(data: Intent) = data.let {
        val hasError = data.hasExtra("auth_error")
        if (hasError) {
            val authException = data.getSerializableExtra("auth_error") as TwitterAuthException
            val errorMessage = authException.message
            twitterLogin?.let { view ->
                when (errorMessage) {
                    TOKEN_ERROR -> handleTokenError(view, errorMessage)
                    CANCEL_LOGIN -> handleIncompleteLogin(view)
                    else -> handleTokenError(view, errorMessage)
                }
            }
        }
    }

    private fun handleIncompleteLogin(view: View): Snackbar {
        return longSnackbar(
                view,
                "Twitter login process incomplete",
                "Try again"
        ) {
            when (view) {
                is TwitterLoginButton -> {
                    view.callOnClick()
                }
                else -> {
                }
            }
        }
    }

    private fun handleTokenError(view: View, errorMessage: String?): Snackbar {
        return longSnackbar(
                view,
                "Temporarily unable to support.\n Contact: socialpurge@kaloglu.com",
                "Report"
        ) {
            email(
                    "socialpurge@kaloglu.com",
                    "App Working Issue[SplashScreen]",
                    "Can not add Twitter account!\n Reason[$errorMessage]"
            )
        }
    }

    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit, final: () -> Unit) {
        val frgTrx = beginTransaction()
        frgTrx.func()
        frgTrx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        frgTrx.commit()
        final()
    }

    fun AppCompatActivity.startFragment(fragment: Fragment, containerViewId: Int = fragment_container.id, isReplace: Boolean = true) =
            supportFragmentManager.let({ frgMngr ->
                var frgTag = "undefinedContentType"
                fragment.arguments?.let { args -> frgTag = args[zao.kaloglu.com.socialpurge.helpers.utils.Enums.FragmentArguments.CONTENT_TYPE].toString() }

                frgMngr.inTransaction({
                    fragment.let { frg ->
                        when {
                            isReplace -> replace(containerViewId, frg, frgTag)
                            else -> add(containerViewId, frg, frgTag).addToBackStack(frgTag)
                        }

                    }
                }, {
                    frgMngr.executePendingTransactions()
                })
            })

    /** use Long with {@code @Enum.FragmentContentTypes} {@link FragmentContentTypes zao.kaloglu.com.socialpurge.helpers.utils.Enums.FragmentContentTypes}*/
    fun Long.twitterTimeline(): BaseFragment = TwitterTimelineFragment().getInstance(this)

    internal fun SharedPreferences.put(key: String, value: Any) {

        val editor = this.edit()

        when (value) {
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is String -> editor.putString(key, value)
        }

        editor.apply()
    }

    internal fun SharedPreferences.get(key: String, default: Any): Any = when (default) {
        is Long -> this.getLong(key, default)
        is Float -> this.getFloat(key, default)
        is Boolean -> this.getBoolean(key, default)
        is Int -> this.getInt(key, default)
        is String -> this.getString(key, default)
        else -> ""
    }


    fun createSpinnerProgress(context: Context): Dialog {

        val dialog = Dialog(context, R.style.spinnerTheme)
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

}
