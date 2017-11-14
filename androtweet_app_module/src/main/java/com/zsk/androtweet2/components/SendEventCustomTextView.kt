package com.zsk.androtweet2.components

import android.view.View
import com.zsk.androtweet2.AndroTweetApp

class SendEventCustomTextView(private val onClickListener: View.OnClickListener?) : View.OnClickListener {

    override fun onClick(v: View?) {
        val cButton = v as CustomTextView

        if (!cButton.getEventAction().isNullOrBlank())
            (cButton.context.applicationContext as AndroTweetApp).sendEvent(
                    cButton.getEventCategory(),
                    cButton.getEventAction(),
                    cButton.getEventLabel(),
                    cButton.getEventValue(),
                    cButton.isEventInterraction()
            )
        onClickListener?.onClick(v)
    }

}
