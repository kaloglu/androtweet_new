/*
 * Copyright (C) 2015 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package zao.kaloglu.com.socialpurge.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.core.models.TweetBuilder
import com.twitter.sdk.android.tweetui.Timeline
import com.twitter.sdk.android.tweetui.TimelineResult
import zao.kaloglu.com.socialpurge.components.twitter.TimelineDelegate
import zao.kaloglu.com.socialpurge.fragments.BaseFragment
import zao.kaloglu.com.socialpurge.helpers.AppSettings
import zao.kaloglu.com.socialpurge.helpers.utils.twitter.components.views.CompactTweetView

@Suppress("UNCHECKED_CAST")
/**
 * TimelineAdapter is a RecyclerView adapter which can provide Timeline Tweets to
 * RecyclerViews.
 */
class TimelineAdapter private constructor(
        private val context: Context,
        val timelineDelegate: TimelineDelegate
) : RecyclerView.Adapter<TimelineAdapter.ViewHolder>() {

//    private var previousCount: Int = 0

    // A menu item view type.
    private val STANDART_VIEW_TYPE = 0

    // The Native Express ad view type.
    private val NATIVE_EXPRESS_AD_VIEW_TYPE = 1

    fun checkNewItems(onFinished: () -> Unit) {
        timelineDelegate.next(object : Callback<TimelineResult<Tweet>>() {
            override fun success(result: Result<TimelineResult<Tweet>>?) {
                onFinished()

            }

            override fun failure(exception: TwitterException?) {
                onFinished()
            }

        })
    }

    fun selectAll(checked: Boolean) {
        timelineDelegate.selectAll(checked)
    }

    fun addAll() {
        timelineDelegate.addAll()
    }

    constructor(
            context: Context,
            timeline: Timeline<Tweet>,
            toggleSheetMenuListener: BaseFragment.ToggleSheetMenuListener? = null
    ) : this(context, TimelineDelegate(context, timeline, toggleSheetMenuListener = toggleSheetMenuListener))


    init {
        MobileAds.initialize(context, AppSettings.ADMOB_APP_ID)
        timelineDelegate.refresh(this)

//        getAdsSettings()?.getInt("ads_items_per_ad", 8)
    }

    fun getItemList(): MutableList<Tweet> = this.timelineDelegate.tweetList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        when (viewType) {
            STANDART_VIEW_TYPE -> {
                val tweet = TweetBuilder().build() as Tweet
                val compactTweetView = CompactTweetView(context, tweet, timelineDelegate)
                return TweetViewHolder(compactTweetView)
            }

            NATIVE_EXPRESS_AD_VIEW_TYPE -> {
                val nativeExpressLayoutView = AdView(context)
                nativeExpressLayoutView.adSize = AdSize(-1, -2)
                nativeExpressLayoutView.adUnitId = AppSettings.ADMOB_SMARTBANNER_UNIT_ID
                nativeExpressLayoutView.loadAd(AdRequest.Builder().build())
                return NativeExpressAdViewHolder(nativeExpressLayoutView)
            }

            else -> {
                return null
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            STANDART_VIEW_TYPE -> {
                val compactTweetView = holder.itemView as CompactTweetView
                compactTweetView.position = position
            }
        }

    }

    override fun getItemCount(): Int = timelineDelegate.tweetList.size

    /**
     * Determines the view type for the given position.
     */

    override fun getItemViewType(position: Int): Int {
        return when (getItemList()[position].id == Tweet.INVALID_ID) { //TODO: will add removeAds parameter.
//        return when (position % ITEMS_PER_AD == 0) { //TODO: will add removeAds parameter.
            true -> NATIVE_EXPRESS_AD_VIEW_TYPE
            else -> STANDART_VIEW_TYPE
        }
    }


    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class TweetViewHolder(itemView: CompactTweetView) : ViewHolder(itemView)

    /**
     * The [NativeExpressAdViewHolder] class.
     */
    inner class NativeExpressAdViewHolder internal constructor(view: View) : ViewHolder(view)
}

