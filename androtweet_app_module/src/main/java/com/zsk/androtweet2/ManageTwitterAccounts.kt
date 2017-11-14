package com.zsk.androtweet2

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.Query
import com.zsk.androtweet2.models.TwitterAccount
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_manage_twitter_accounts.*
import kotlinx.android.synthetic.main.twitter_user_layout.view.*


/**
 * Created by kaloglu on 12/11/2017.
 */
class ManageTwitterAccounts : BaseActivity() {
    lateinit var toolbar: Toolbar
    var firebaseService = FirebaseService()
    lateinit var twitter_account_query: Query
    lateinit var options: FirebaseRecyclerOptions<TwitterAccount>
    lateinit var adapter: TwitterAccountAdapter

    init {
        with(firebaseService) {
            twitter_account_query = TWITTER_ACCOUNTS.getDatabaseReference().child(currentUser?.uid).orderByKey()
            options = recyclerOptions(TwitterAccount::class.java, this@ManageTwitterAccounts, twitter_account_query)
            adapter = TwitterAccountAdapter(options)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_manage_twitter_accounts)
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        super.onCreate(savedInstanceState)

    }

    override fun addEventListenerForFirebase() {
        super.addEventListenerForFirebase()
        adapter = TwitterAccountAdapter(options)

    }

    override fun initializeScreenObject() {

        // Scroll to bottom on new messages
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                twitter_accounts_rw.smoothScrollToPosition(adapter.itemCount)
                adapter.notifyItemRangeInserted(positionStart, itemCount)
            }
        })

        twitter_accounts_rw.adapter = adapter
        twitter_accounts_rw.layoutManager = LinearLayoutManager(this)

        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider))

        twitter_accounts_rw.addItemDecoration(itemDecorator)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result to the login button.
        twitterLogin.onActivityResult(requestCode, resultCode, data)
    }

    public override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    inner class TwitterAccountAdapter(options: FirebaseRecyclerOptions<TwitterAccount>)
        : FirebaseRecyclerAdapter<TwitterAccount, TwitterAccountHolder>(options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TwitterAccountHolder {
            return TwitterAccountHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.twitter_user_layout, parent, false))
        }

        override fun onBindViewHolder(holder: TwitterAccountHolder, position: Int, model: TwitterAccount) {
            holder.bind(model)
        }

        override fun onDataChanged() {
            // If there are no chat messages, show a view that invites the user to add a message.
//                mEmptyListMessage.setVisibility(if (itemCount == 0) View.VISIBLE else View.GONE)
        }
    }

    inner class TwitterAccountHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(twitterAccount: TwitterAccount) {
            itemView.let { iv ->
                iv.real_name.text = twitterAccount.realname
                iv.user_name.text = twitterAccount.name
                iv.profile_pic.loadFromUrl(this@ManageTwitterAccounts, twitterAccount.profilePic)

                iv.delete_account.setOnClickListener {
                    with(firebaseService) {
                        TWITTER_ACCOUNTS.remove(twitterAccount.id.toString())

                    }
                }

            }
        }

    }

}

