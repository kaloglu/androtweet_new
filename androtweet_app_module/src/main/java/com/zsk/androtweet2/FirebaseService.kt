package com.zsk.androtweet2

import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

/**
 * Created by kaloglu on 11/11/2017.
 */
class FirebaseService {
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    val firebaseAuth: FirebaseAuth? = FirebaseAuth.getInstance()
    val currentUser: FirebaseUser? = firebaseAuth!!.currentUser

    val USERS: String? = "users"
    val TWITTER_ACCOUNTS: String? = "twitter_accounts"
    val PREFERENCES: String? = "preferences"
    val STATUSES: String? = "statuses"
    val LIKES: String? = "likes"
    val RETWEETS: String? = "retweets"
    val TWEET_DETAILS: String? = "tweet_details"


    fun String?.getDatabaseReference(): DatabaseReference =
            firebaseDatabase.getReference(this)

    fun String?.update(id: String, value: Any): Task<Void>? =
            this.getDatabaseReference().child(currentUser!!.uid).child(id).setValue(value)

    fun String?.remove(id: String): Task<Void>? =
            this.getDatabaseReference().child(currentUser!!.uid).child(id).removeValue()

    fun isSignedIn(): Boolean = FirebaseAuth.getInstance().currentUser != null

    fun <T>recyclerOptions(receiver: Class<T>, activity: BaseActivity, query: Query?): FirebaseRecyclerOptions<T> {
        return FirebaseRecyclerOptions.Builder<T>()
                .setQuery(query,receiver)
                .setLifecycleOwner(activity)
                .build()
    }


}
