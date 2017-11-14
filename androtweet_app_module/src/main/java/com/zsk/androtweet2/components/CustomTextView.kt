package com.zsk.androtweet2.components

import android.content.Context
import android.content.res.TypedArray
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.TextView
import com.zsk.androtweet2.R
import com.zsk.androtweet2.Sealed.Enums

/**
 * Created by kaloglu on 21/10/2017.
 */
class CustomTextView : TextView {

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        this.eventValue = Integer.MIN_VALUE.toLong()
        packageName = getContext().packageName
        this.attrs = attrs
        val a = getContext().theme.obtainStyledAttributes(attrs, R.styleable.View, 0, 0)
        val fontTypeId = a.getInteger(R.styleable.View_textType, 0)
        val fontType = Enums.FontType.values()[fontTypeId]
        if (fontTypeId > 0)
            setCustomTypeFace(getContext(), fontType)
        init_ga_event_tags(a)
        a.recycle()
    }

    private var eventCategory: String? = ""
    private var eventAction: String? = ""
    private var eventActionError: String? = ""
    private var eventLabel: String? = ""
    private var eventValue: Long
    private var eventInterraction = true
    private var attrs: AttributeSet? = null
    internal var packageName: String = "";

    fun setCustomTypeFace(context: Context, fontType: Enums.FontType) {
        typeface = CustomTypeFace.getTypeFace(context, fontType)
    }

    private fun init_ga_event_tags(gaEvents: TypedArray) {

        eventCategory = gaEvents.getString(R.styleable.View_gaEventCategory)
        eventAction = gaEvents.getString(R.styleable.View_gaEventAction)
        eventActionError = gaEvents.getString(R.styleable.View_gaEventActionError)
        eventLabel = gaEvents.getString(R.styleable.View_gaEventLabel)
        eventValue = gaEvents.getInteger(R.styleable.View_gaEventValue, 0).toLong()
        eventInterraction = gaEvents.getBoolean(R.styleable.View_gaEventInterraction, true)

    }


    override fun setOnClickListener(l: OnClickListener) {

        super.setOnClickListener(SendEventCustomTextView(l))
    }

    fun getEventCategory(): String? = eventCategory

    fun getEventAction(): String? = eventAction

    fun getEventActionError(): String? = eventActionError

    fun getEventLabel(): String? = eventLabel

    fun getEventValue(): Long = eventValue

    fun isEventInterraction(): Boolean = eventInterraction

//    fun setText(doubleText: Double) {
//        setText(decimalFormat.format(doubleText).toDouble())
//    }


//    fun setFontDrawableColor(fontDrawableColor: Int) {
//        CustomLayoutInflaterFactory.getBaseActivity(context).setupTextViewCustomAttributes(this, fontDrawableColor)
//    }


    fun setFontIcon(fontIcon: String) {
        setFontIcon(fontIcon, 0)
    }

    fun setFontIcon(value: String?, color: Int) {
        var fontIcon = value
        var fontIconColor = color

        if (fontIcon == null)
            return

        if (fontIconColor == 0) {
            try {
                fontIconColor = ContextCompat.getColor(context, getColorRes(fontIcon))
                setTextColor(fontIconColor)
            } catch (ignored: Exception) {
            }

        } else {
            setTextColor(ContextCompat.getColor(context, fontIconColor))
        }

        if (!fontIcon.contains("ic_"))
            fontIcon = "ic_" + fontIcon

        try {
            fontIcon = context.getString(getStringRes(fontIcon))
        } catch (ignored: Exception) {
            if (fontIcon.contains("notify_")) {
                fontIcon = context.getString(getStringRes("ic_notify_default"))
            }
        }

        text = fontIcon


    }

    private fun getColorRes(fontIcon: String): Int =
            context.resources.getIdentifier(fontIcon, "color", packageName)

    private fun getStringRes(fontIcon: String): Int =
            context.resources.getIdentifier(fontIcon, "string", packageName)
}

