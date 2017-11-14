package com.zsk.androtweet2

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.squareup.picasso.Picasso
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.models.User
import com.zsk.androtweet2.models.TwitterAccount
import kotlinx.android.synthetic.main.activity_main.*


open class MainActivity : BaseActivity(), Drawer.OnDrawerItemClickListener, AccountHeader.OnAccountHeaderListener {

    var firebaseService = FirebaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar!!)
        DrawerImageLoader.init(PicassoLoader())
        createNavigationDrawer(savedInstanceState, toolbar)

    }

    override fun addEventListenerForFirebase() {
        super.addEventListenerForFirebase()
        with(firebaseService) {
            TWITTER_ACCOUNTS.getDatabaseReference().child(currentUser?.uid).addChildEventListener(object : ChildEventListener {
                override fun onCancelled(databaseError: DatabaseError?) {
                }

                override fun onChildMoved(dataSnapShot: DataSnapshot?, p1: String?) {
                }

                override fun onChildChanged(dataSnapShot: DataSnapshot?, p1: String?) {

                    dataSnapShot?.getValue<TwitterAccount>(TwitterAccount::class.java)
                            ?.let { account ->
                                androTweetApp.accountHeader.updateProfile(
                                        getProfileDrawerItem(account)
                                )
                            }
                }

                override fun onChildAdded(dataSnapShot: DataSnapshot?, p1: String?) {
                    dataSnapShot?.getValue<TwitterAccount>(TwitterAccount::class.java)
                            ?.let { account ->
                                var index = 0
                                with(androTweetApp.accountHeader.profiles) {
                                    if (count() >= 2)
                                        index = count() - 2
                                }
                                androTweetApp.accountHeader.addProfile(
                                        getProfileDrawerItem(account),
                                        index
                                )
                            }
                }

                override fun onChildRemoved(dataSnapShot: DataSnapshot?) {
                    dataSnapShot?.getValue<TwitterAccount>(TwitterAccount::class.java)
                            ?.let { account ->
                                androTweetApp.accountHeader.removeProfileByIdentifier(account.id)
                            }
                }
            })
        }

    }

    private fun getProfileDrawerItem(account: TwitterAccount): IProfile<*> {
        return ProfileDrawerItem().withIdentifier(account.id)
                .withName(account.name)
                .withEmail(account.realname)
                .withIcon(account.profilePic)
    }


    override fun initializeScreenObject() {

        twitterLogin.callback = object : TwitterLoginCallBack(firebaseService) {}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result to the login button.
        twitterLogin.onActivityResult(requestCode, resultCode, data)
    }

    private fun createNavigationDrawer(savedInstanceState: Bundle?, toolbar: Toolbar) {
        androTweetApp.accountHeader = createAccountHeader(savedInstanceState)
        androTweetApp.navigationDrawer = createDrawer(toolbar, androTweetApp.accountHeader, savedInstanceState)
    }

    private fun createAccountHeader(savedInstanceState: Bundle?): AccountHeader {
        return AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.md_red_500)
                .addProfiles(
//                        Settings for Account
                        ProfileSettingDrawerItem().withName("Add Account")
                                .withIdentifier(baseActivity.ADD_TWITTER_ACCOUNT)
                                .withIcon(FontIconDrawable(this, getString(R.string.ic_user_plus))),
                        ProfileSettingDrawerItem().withName("Manage Account")
                                .withIdentifier(baseActivity.MANAGE_TWITTER_ACCOUNT)
                                .withDescription("Add / Remove your accounts")
                                .withIcon(FontIconDrawable(this, getString(R.string.ic_cog)))
                )
                .withOnAccountHeaderListener(this)
                .withAlternativeProfileHeaderSwitching(true)
                .withCurrentProfileHiddenInList(true)
                .withThreeSmallProfileImages(true)
                .withSavedInstance(savedInstanceState)
                .build()
    }

    open fun signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener({ _ ->
                    startActivity(Intent(this, SplashScreen::class.java))
                })
    }

    private fun createDrawer(toolbar: Toolbar, headerResult: AccountHeader, savedInstanceState: Bundle?): Drawer {
        return DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult, true)
                .inflateMenu(R.menu.activity_main_drawer)
                .addStickyDrawerItems(
                        PrimaryDrawerItem().withName("Logout").withIdentifier(LOGOUT)
                )
                .withOnDrawerItemClickListener(this)
                .withSavedInstance(savedInstanceState)
                .withCloseOnClick(true)
                .withFullscreen(false)
                .build()
    }

    override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*, *>?): Boolean {
        when (drawerItem?.identifier) {
            LOGOUT -> signOut()
            ADD_TWITTER_ACCOUNT -> twitterLogin.callOnClick()
            MANAGE_TWITTER_ACCOUNT -> getManageActivity()
        }
        return false
    }

    private fun getManageActivity() {
        startActivity(Intent(this, ManageTwitterAccounts::class.java))
    }

    override fun onProfileChanged(view: View?, profile: IProfile<*>?, current: Boolean): Boolean {
        if (profile is IDrawerItem<*, *>) {
            return onItemClick(view, -1, profile)
        }

        return false
    }

    open class TwitterLoginCallBack(firebaseServ: FirebaseService) : Callback<TwitterSession>() {
        var firebaseService = firebaseServ
        override fun success(result: Result<TwitterSession>?) {
            if (result?.data == null)
                return

            val session = result.data
            val authToken: TwitterAccount.CustomAuthToken = TwitterAccount.CustomAuthToken(session.authToken)

            TwitterApiClient(session).accountService.verifyCredentials(true, true, true)
                    .enqueue(object : Callback<User>() {
                        override fun success(result: Result<User>?) {
                            val user: User? = result?.data
                            if (user != null) {
                                with(firebaseService) {
                                    TWITTER_ACCOUNTS?.update(user.id.toString(),
                                            TwitterAccount(
                                                    user.id,
                                                    "@" + user.screenName,
                                                    user.name,
                                                    user.profileImageUrl,
                                                    authToken
                                            )
                                    )
                                }
                            }

                        }

                        override fun failure(exception: TwitterException?) {
                            Log.e(baseActivity.TAG, "exception", exception)
                        }
                    })

        }

        override fun failure(exception: TwitterException?) {
        }

    }

    class PicassoLoader : AbstractDrawerImageLoader() {

        override fun set(imageView: ImageView?, uri: Uri?, placeholder: Drawable?, tag: String?) {
            Picasso.with(imageView?.context).load(uri).placeholder(placeholder).into(imageView)
        }

        override fun cancel(imageView: ImageView?) {
            Picasso.with(imageView?.context).cancelRequest(imageView)
        }
        /*
        @Override
        public Drawable placeholder(Context ctx) {
            return super.placeholder(ctx);
        }

        @Override
        public Drawable placeholder(Context ctx, String tag) {
            return super.placeholder(ctx, tag);
        }
        */
    }
}


