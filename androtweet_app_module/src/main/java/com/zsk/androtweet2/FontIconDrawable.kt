package com.zsk.androtweet2

/**
 * Created by kaloglu on 04/11/2017.
 */

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.*
import android.support.annotation.Dimension.DP
import android.support.annotation.Dimension.PX
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import com.zsk.androtweet2.Sealed.Enums
import com.zsk.androtweet2.components.CustomTypeFace

/**
 * A custom [Drawable] which can display icons from icon fonts.
 */
class FontIconDrawable : Drawable {
    private var mContext: Context? = null
    private var mSizeX = -1
    private var mSizeY = -1

    private var mRespectFontBounds = false

    /**
     * Returns the icon color
     */
    var color: Int = 0
        private set
    private var mIconPaint: Paint? = null
    /**
     * Returns the icon contour color
     */
    var contourColor: Int = 0
        private set
    private var mContourPaint: Paint? = null
    /**
     * Returns the icon background color
     */
    var backgroundColor: Int = 0
        private set
    private var mBackgroundPaint: Paint? = null

    private var mRoundedCornerRx = -1
    private var mRoundedCornerRy = -1

    private var mPaddingBounds: Rect? = null
    private var mPathBounds: RectF? = null

    private var mPath: Path? = null

    private var mIconPadding: Int = 0
    private var mContourWidth: Int = 0

    private var mIconOffsetX = 0
    private var mIconOffsetY = 0

    /**
     * just a helper method to get the alpha value
     *
     * @return
     */
    var compatAlpha = 255
        private set

    private var mDrawContour: Boolean = false

    /**
     * @return the PlainIcon which is used inside this FontIconDrawable
     */
    var plainIcon: String? = null
        private set

    private var mTint: ColorStateList? = null
    private var mTintMode: PorterDuff.Mode? = PorterDuff.Mode.SRC_IN
    private var mTintFilter: ColorFilter? = null
    private var mColorFilter: ColorFilter? = null
    private var DEFAULT_SIZE = 0


    @JvmOverloads constructor(context: Context, icon: String = "", isResourceName: Boolean = context.resources.getIdentifier("ic_" + icon, "string", context.packageName) != 0) {
        var iconstr = icon
        mContext = context
        color = ContextCompat.getColor(context, R.color.colorPrimary)

        prepare()
        CustomTypeFace.getTypeFace(context, Enums.FontType.FONT_ICON)?.let { typeface(it) }
        if (isResourceName)
            iconstr = mContext!!.getString(context.resources.getIdentifier("ic_" + iconstr, "string", context.packageName))

        icon(iconstr)

    }

    constructor(context: Context, @StringRes icon: Int) {
        mContext = context
        color = ContextCompat.getColor(context, R.color.colorPrimary);

        prepare()
        icon(mContext!!.getString(icon))
    }

    private fun prepare() {
        DEFAULT_SIZE = 40// buraya ekran boyua göre özel hesaplama gelecek.
        mIconPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        mIconPaint!!.style = Paint.Style.FILL
        mIconPaint!!.textAlign = Paint.Align.CENTER
        mIconPaint!!.isUnderlineText = false
        mIconPaint!!.isAntiAlias = true
        mIconPaint!!.color = color

        mBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        mContourPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mContourPaint!!.style = Paint.Style.STROKE

        mPath = Path()

        mPathBounds = RectF()
        mPaddingBounds = Rect()
    }

    /**
     * Loads and draws given.
     *
     * @param icon
     * @return The current IconExtDrawable for chaining.
     */
    fun icon(icon: String): FontIconDrawable {
        return iconText(icon)
    }

    /**
     * Loads and draws given text
     *
     * @param icon
     * @return The current IconExtDrawable for chaining.
     */
    fun iconText(icon: String?): FontIconDrawable {
        plainIcon = icon
        //        mIconPaint.setTypeface(Typeface.DEFAULT);
        invalidateSelf()
        return this
    }

    /**
     * Set if it should respect the original bounds of the icon. (DEFAULT is false)
     * This will break the "padding" functionality, but keep the padding defined by the font itself
     * Check it out with the oct_arrow_down and oct_arrow_small_down of the Octicons font
     *
     * @param respectBounds set to true if it should respect the original bounds
     */
    fun respectFontBounds(respectBounds: Boolean): FontIconDrawable {
        this.mRespectFontBounds = respectBounds
        invalidateSelf()
        return this
    }

    /**
     * Set the color of the drawable.
     *
     * @param color The color, usually from android.graphics.Color or 0xFF012345.
     * @return The current IconExtDrawable for chaining.
     */
    fun color(@ColorInt color: Int): FontIconDrawable {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        mIconPaint!!.color = Color.rgb(red, green, blue)
        this.color = color
        alpha = Color.alpha(color)
        invalidateSelf()
        return this
    }

    /*
    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color));
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
    */

    /**
     * Set the color of the drawable.
     *
     * @param colorRes The color resource, from your R file.
     * @return The current IconExtDrawable for chaining.
     */
    fun colorRes(@ColorRes colorRes: Int): FontIconDrawable {
        return color(ContextCompat.getColor(mContext!!, colorRes))
    }

    /**
     * set the icon offset for X from resource
     *
     * @param iconOffsetXRes
     * @return
     */
    fun iconOffsetXRes(@DimenRes iconOffsetXRes: Int): FontIconDrawable {
        return iconOffsetXPx(mContext!!.resources.getDimensionPixelSize(iconOffsetXRes))
    }

    /**
     * set the icon offset for X as dp
     *
     * @param iconOffsetXDp
     * @return
     */
    fun iconOffsetXDp(@Dimension(unit = DP) iconOffsetXDp: Int): FontIconDrawable {
        return iconOffsetXPx(convertDpToPx(mContext, iconOffsetXDp.toFloat()))
    }

    /**
     * set the icon offset for X
     *
     * @param iconOffsetX
     * @return
     */
    fun iconOffsetXPx(@Dimension(unit = PX) iconOffsetX: Int): FontIconDrawable {
        this.mIconOffsetX = iconOffsetX
        return this
    }

    /**
     * set the icon offset for Y from resource
     *
     * @param iconOffsetYRes
     * @return
     */
    fun iconOffsetYRes(@DimenRes iconOffsetYRes: Int): FontIconDrawable {
        return iconOffsetYPx(mContext!!.resources.getDimensionPixelSize(iconOffsetYRes))
    }

    /**
     * set the icon offset for Y as dp
     *
     * @param iconOffsetYDp
     * @return
     */
    fun iconOffsetYDp(@Dimension(unit = DP) iconOffsetYDp: Int): FontIconDrawable {
        return iconOffsetYPx(convertDpToPx(mContext, iconOffsetYDp.toFloat()))
    }

    /**
     * set the icon offset for Y
     *
     * @param iconOffsetY
     * @return
     */
    fun iconOffsetYPx(@Dimension(unit = PX) iconOffsetY: Int): FontIconDrawable {
        this.mIconOffsetY = iconOffsetY
        return this
    }

    /**
     * Set the padding of the drawable from res
     *
     * @param dimenRes
     * @return The current IconExtDrawable for chaining.
     */
    fun paddingRes(@DimenRes dimenRes: Int): FontIconDrawable {
        return paddingPx(mContext!!.resources.getDimensionPixelSize(dimenRes))
    }


    /**
     * Set the padding in dp for the drawable
     *
     * @param iconPadding
     * @return The current IconExtDrawable for chaining.
     */
    fun paddingDp(@Dimension(unit = DP) iconPadding: Int): FontIconDrawable {
        return paddingPx(convertDpToPx(mContext, iconPadding.toFloat()))
    }

    /**
     * Set a padding for the.
     *
     * @param iconPadding
     * @return The current IconExtDrawable for chaining.
     */
    fun paddingPx(@Dimension(unit = PX) iconPadding: Int): FontIconDrawable {
        if (mIconPadding != iconPadding) {
            mIconPadding = iconPadding
            if (mDrawContour) {
                mIconPadding += mContourWidth
            }

            invalidateSelf()
        }
        return this
    }

    /**
     * Set the size of this icon to the standard Android ActionBar.
     *
     * @return The current IconExtDrawable for chaining.
     */
    @Deprecated("use actionBar() instead")
    fun actionBarSize(): FontIconDrawable {
        return sizeDp(ANDROID_ACTIONBAR_ICON_SIZE_DP.toFloat())
    }

    /**
     * Sets the size and the Padding to the correct values to be used for the actionBar / toolBar
     *
     * @return
     */
    fun actionBar(): FontIconDrawable {
        sizeDp(ANDROID_ACTIONBAR_ICON_SIZE_DP.toFloat())
        paddingDp(ANDROID_ACTIONBAR_ICON_SIZE_PADDING_DP)
        return this
    }

    /**
     * Set the size of the drawable.
     *
     * @param dimenRes The dimension resource.
     * @return The current IconExtDrawable for chaining.
     */
    fun sizeRes(@DimenRes dimenRes: Int): FontIconDrawable {
        return sizePx(mContext!!.resources.getDimensionPixelSize(dimenRes))
    }


    /**
     * Set the size of the drawable.
     *
     * @param size The size in density-independent pixels (dp).
     * @return The current IconExtDrawable for chaining.
     */
    fun sizeDp(@Dimension(unit = DP) size: Float): FontIconDrawable {
        return sizePx(convertDpToPx(mContext, size))
    }

    /**
     * Set the size of the drawable.
     *
     * @param size The size in pixels (px).
     * @return The current IconExtDrawable for chaining.
     */
    fun sizePx(@Dimension(unit = PX) size: Int): FontIconDrawable {
        this.mSizeX = size
        this.mSizeY = size
        setBounds(0, 0, size, size)
        invalidateSelf()
        return this
    }

    /**
     * Set the size of the drawable.
     *
     * @param dimenResX The dimension resource.
     * @return The current IconExtDrawable for chaining.
     */
    fun sizeResX(@DimenRes dimenResX: Int): FontIconDrawable {
        return sizePxX(mContext!!.resources.getDimensionPixelSize(dimenResX))
    }


    /**
     * Set the size of the drawable.
     *
     * @param sizeX The size in density-independent pixels (dp).
     * @return The current IconExtDrawable for chaining.
     */
    fun sizeDpX(@Dimension(unit = DP) sizeX: Int): FontIconDrawable {
        return sizePxX(convertDpToPx(mContext, sizeX.toFloat()))
    }

    /**
     * Set the size of the drawable.
     *
     * @param sizeX The size in pixels (px).
     * @return The current IconExtDrawable for chaining.
     */
    fun sizePxX(@Dimension(unit = PX) sizeX: Int): FontIconDrawable {
        this.mSizeX = sizeX
        setBounds(0, 0, mSizeX, mSizeY)
        invalidateSelf()
        return this
    }

    /**
     * Set the size of the drawable.
     *
     * @param dimenResY The dimension resource.
     * @return The current IconExtDrawable for chaining.
     */
    fun sizeResY(@DimenRes dimenResY: Int): FontIconDrawable {
        return sizePxY(mContext!!.resources.getDimensionPixelSize(dimenResY))
    }


    /**
     * Set the size of the drawable.
     *
     * @param sizeY The size in density-independent pixels (dp).
     * @return The current IconExtDrawable for chaining.
     */
    fun sizeDpY(@Dimension(unit = DP) sizeY: Int): FontIconDrawable {
        return sizePxY(convertDpToPx(mContext, sizeY.toFloat()))
    }

    /**
     * Set the size of the drawable.
     *
     * @param sizeY The size in pixels (px).
     * @return The current IconExtDrawable for chaining.
     */
    fun sizePxY(@Dimension(unit = PX) sizeY: Int): FontIconDrawable {
        this.mSizeY = sizeY
        setBounds(0, 0, mSizeX, mSizeY)
        invalidateSelf()
        return this
    }


    /**
     * Set contour color for the.
     *
     * @param contourColor
     * @return The current IconExtDrawable for chaining.
     */
    fun contourColor(@ColorInt contourColor: Int): FontIconDrawable {
        val red = Color.red(contourColor)
        val green = Color.green(contourColor)
        val blue = Color.blue(contourColor)
        mContourPaint!!.color = Color.rgb(red, green, blue)
        mContourPaint!!.alpha = Color.alpha(contourColor)
        this.contourColor = contourColor
        invalidateSelf()
        return this
    }

    /**
     * Set contour color from color res.
     *
     * @param contourColorRes
     * @return The current IconExtDrawable for chaining.
     */
    fun contourColorRes(@ColorRes contourColorRes: Int): FontIconDrawable {
        return contourColor(ContextCompat.getColor(mContext!!, contourColorRes))
    }

    /**
     * set background color
     *
     * @param backgroundColor
     * @return
     */
    fun backgroundColor(@ColorInt backgroundColor: Int): FontIconDrawable {
        this.mBackgroundPaint!!.color = backgroundColor
        this.backgroundColor = backgroundColor
        this.mRoundedCornerRx = 0
        this.mRoundedCornerRy = 0
        return this
    }

    /**
     * set background color from res
     *
     * @param backgroundColorRes
     * @return
     */
    fun backgroundColorRes(@ColorRes backgroundColorRes: Int): FontIconDrawable {
        return backgroundColor(ContextCompat.getColor(mContext!!, backgroundColorRes))
    }

    /**
     * set rounded corner from res
     *
     * @param roundedCornerRxRes
     * @return
     */
    fun roundedCornersRxRes(@DimenRes roundedCornerRxRes: Int): FontIconDrawable {
        this.mRoundedCornerRx = mContext!!.resources.getDimensionPixelSize(roundedCornerRxRes)
        return this
    }

    /**
     * set rounded corner from dp
     *
     * @param roundedCornerRxDp
     * @return
     */
    fun roundedCornersRxDp(@Dimension(unit = DP) roundedCornerRxDp: Int): FontIconDrawable {
        this.mRoundedCornerRx = convertDpToPx(mContext, roundedCornerRxDp.toFloat())
        return this
    }

    /**
     * set rounded corner from px
     *
     * @param roundedCornerRxPx
     * @return
     */
    fun roundedCornersRxPx(@Dimension(unit = PX) roundedCornerRxPx: Int): FontIconDrawable {
        this.mRoundedCornerRx = roundedCornerRxPx
        return this
    }

    /**
     * set rounded corner from res
     *
     * @param roundedCornerRyRes
     * @return
     */
    fun roundedCornersRyRes(@DimenRes roundedCornerRyRes: Int): FontIconDrawable {
        this.mRoundedCornerRy = mContext!!.resources.getDimensionPixelSize(roundedCornerRyRes)
        return this
    }

    /**
     * set rounded corner from dp
     *
     * @param roundedCornerRyDp
     * @return
     */
    fun roundedCornersRyDp(@Dimension(unit = DP) roundedCornerRyDp: Int): FontIconDrawable {
        this.mRoundedCornerRy = convertDpToPx(mContext, roundedCornerRyDp.toFloat())
        return this
    }

    /**
     * set rounded corner from px
     *
     * @param roundedCornerRyPx
     * @return
     */
    fun roundedCornersRyPx(@Dimension(unit = PX) roundedCornerRyPx: Int): FontIconDrawable {
        this.mRoundedCornerRy = roundedCornerRyPx
        return this
    }

    /**
     * set rounded corner from res
     *
     * @param roundedCornerRes
     * @return
     */
    fun roundedCornersRes(@DimenRes roundedCornerRes: Int): FontIconDrawable {
        this.mRoundedCornerRx = mContext!!.resources.getDimensionPixelSize(roundedCornerRes)
        this.mRoundedCornerRy = this.mRoundedCornerRx
        return this
    }

    /**
     * set rounded corner from dp
     *
     * @param roundedCornerDp
     * @return
     */
    fun roundedCornersDp(@Dimension(unit = DP) roundedCornerDp: Int): FontIconDrawable {
        this.mRoundedCornerRx = convertDpToPx(mContext, roundedCornerDp.toFloat())
        this.mRoundedCornerRy = this.mRoundedCornerRx
        return this
    }

    /**
     * set rounded corner from px
     *
     * @param roundedCornerPx
     * @return
     */
    fun roundedCornersPx(@Dimension(unit = PX) roundedCornerPx: Int): FontIconDrawable {
        this.mRoundedCornerRx = roundedCornerPx
        this.mRoundedCornerRy = this.mRoundedCornerRx
        return this
    }

    /**
     * Set contour width from an dimen res for the icon
     *
     * @param contourWidthRes
     * @return The current IconExtDrawable for chaining.
     */
    fun contourWidthRes(@DimenRes contourWidthRes: Int): FontIconDrawable {
        return contourWidthPx(mContext!!.resources.getDimensionPixelSize(contourWidthRes))
    }

    /**
     * Set contour width from dp for the icon
     *
     * @param contourWidthDp
     * @return The current IconExtDrawable for chaining.
     */
    fun contourWidthDp(@Dimension(unit = DP) contourWidthDp: Int): FontIconDrawable {
        return contourWidthPx(convertDpToPx(mContext, contourWidthDp.toFloat()))
    }

    /**
     * Set contour width for the icon.
     *
     * @param contourWidth
     * @return The current IconExtDrawable for chaining.
     */
    fun contourWidthPx(@Dimension(unit = PX) contourWidth: Int): FontIconDrawable {
        mContourWidth = contourWidth
        mContourPaint!!.strokeWidth = mContourWidth.toFloat()
        drawContour(true)
        invalidateSelf()
        return this
    }

    /**
     * Enable/disable contour drawing.
     *
     * @param drawContour
     * @return The current IconExtDrawable for chaining.
     */
    fun drawContour(drawContour: Boolean): FontIconDrawable {
        if (mDrawContour != drawContour) {
            mDrawContour = drawContour

            if (mDrawContour) {
                mIconPadding += mContourWidth
            } else {
                mIconPadding -= mContourWidth
            }

            invalidateSelf()
        }
        return this
    }

    /**
     * Set the colorFilter
     *
     * @param cf
     * @return The current IconExtDrawable for chaining.
     */
    fun colorFilter(cf: ColorFilter): FontIconDrawable {
        setColorFilter(cf)
        return this
    }

    /**
     * Sets the opacity
     *
     * @param alpha
     * @return The current IconExtDrawable for chaining.
     */
    fun alpha(alpha: Int): FontIconDrawable {
        setAlpha(alpha)
        return this
    }

    /**
     * Sets the style
     *
     * @param style
     * @return The current IconExtDrawable for chaining.
     */
    fun style(style: Paint.Style): FontIconDrawable {
        mIconPaint!!.style = style
        return this
    }

    /**
     * sets the typeface of the drawable
     * NOTE THIS WILL OVERWRITE THE ICONFONT!
     *
     * @param typeface
     * @return
     */
    fun typeface(typeface: Typeface): FontIconDrawable {
        mIconPaint?.typeface = typeface
        return this
    }

    override fun draw(canvas: Canvas) {
        if (plainIcon != null) {
            val viewBounds = bounds

            updatePaddingBounds(viewBounds)
            updateTextSize(viewBounds)
            offsetIcon(viewBounds)

            if (mBackgroundPaint != null && mRoundedCornerRy > -1 && mRoundedCornerRx > -1) {
                canvas.drawRoundRect(RectF(0f, 0f, viewBounds.width().toFloat(), viewBounds.height().toFloat()), mRoundedCornerRx.toFloat(), mRoundedCornerRy.toFloat(), mBackgroundPaint!!)
            }

            mPath!!.close()

            if (mDrawContour) {
                canvas.drawPath(mPath!!, mContourPaint!!)
            }

            mIconPaint!!.alpha = compatAlpha
            mIconPaint!!.colorFilter = if (mColorFilter == null) mTintFilter else mColorFilter

            canvas.drawPath(mPath!!, mIconPaint!!)
        }
    }

    override fun setTint(tintColor: Int) {
        setTintList(ColorStateList.valueOf(tintColor))
    }

    override fun setTintList(tint: ColorStateList?) {
        mTint = tint
        mTintFilter = updateTintFilter(tint, mTintMode)
        invalidateSelf()
    }

    override fun setTintMode(tintMode: PorterDuff.Mode) {
        mTintMode = tintMode
        mTintFilter = updateTintFilter(mTint, tintMode)
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect) {
        offsetIcon(bounds)
        mPath!!.close()
        super.onBoundsChange(bounds)
    }

    override fun isStateful(): Boolean {
        return true
    }

    override fun setState(stateSet: IntArray): Boolean {
        alpha = compatAlpha
        return super.setState(stateSet)
    }

    override fun onStateChange(stateSet: IntArray): Boolean {
        if (mTint != null && mTintMode != null) {
            mTintFilter = updateTintFilter(mTint, mTintMode)
            invalidateSelf()
            return true
        }
        return false
    }

    override fun getIntrinsicWidth(): Int {
        if (mSizeX <= 0)
            mSizeX = mIconPaint!!.measureText(plainIcon).toInt()
        return mSizeX
    }

    override fun getIntrinsicHeight(): Int {
        if (mSizeY <= 0)
            mSizeX = (mIconPaint!!.fontMetrics.bottom - mIconPaint!!.fontMetrics.top).toInt()
        return mSizeY
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }


    override fun setAlpha(alpha: Int) {
        mIconPaint!!.alpha = alpha
        compatAlpha = alpha
        invalidateSelf()
    }

    override fun getAlpha(): Int {
        return compatAlpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mColorFilter = cf
        invalidateSelf()
    }

    override fun clearColorFilter() {
        mColorFilter = null
        invalidateSelf()
    }

    /**
     * Creates a BitMap to use in Widgets or anywhere else
     *
     * @return bitmap to set
     */
    fun toBitmap(): Bitmap {
        if (mSizeX == -1 || mSizeY == -1) {
            this.actionBar()
        }

        val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)

        this.style(Paint.Style.FILL)

        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, canvas.width, canvas.height)
        this.draw(canvas)

        return bitmap
    }

    //------------------------------------------
    // PRIVATE HELPER METHODS
    //------------------------------------------

    /**
     * Update the Padding Bounds
     *
     * @param viewBounds
     */
    private fun updatePaddingBounds(viewBounds: Rect) {
        if (mIconPadding >= 0
                && mIconPadding * 2 <= viewBounds.width()
                && mIconPadding * 2 <= viewBounds.height()) {
            mPaddingBounds!!.set(
                    viewBounds.left + mIconPadding,
                    viewBounds.top + mIconPadding,
                    viewBounds.right - mIconPadding,
                    viewBounds.bottom - mIconPadding)
        }
    }

    /**
     * Update the TextSize
     *
     * @param viewBounds
     */
    private fun updateTextSize(viewBounds: Rect) {
        var textSize = viewBounds.height().toFloat() * if (mRespectFontBounds) 1 else 2
        mIconPaint!!.textSize = textSize

        val textValue = plainIcon.toString()
        mIconPaint!!.getTextPath(textValue, 0, textValue.length, 0f, viewBounds.height().toFloat(), mPath)
        mPath!!.computeBounds(mPathBounds, true)

        if (!mRespectFontBounds) {
            val deltaWidth = mPaddingBounds!!.width().toFloat() / mPathBounds!!.width()
            val deltaHeight = mPaddingBounds!!.height().toFloat() / mPathBounds!!.height()
            val delta = if (deltaWidth < deltaHeight) deltaWidth else deltaHeight
            textSize *= delta

            mIconPaint!!.textSize = textSize

            mIconPaint!!.getTextPath(textValue, 0, textValue.length, 0f, viewBounds.height().toFloat(), mPath)
            mPath!!.computeBounds(mPathBounds, true)
        }
    }

    /**
     * Set the icon offset
     *
     * @param viewBounds
     */
    private fun offsetIcon(viewBounds: Rect) {
        val startX = viewBounds.centerX() - mPathBounds!!.width() / 2
        val offsetX = startX - mPathBounds!!.left

        val startY = viewBounds.centerY() - mPathBounds!!.height() / 2
        val offsetY = startY - mPathBounds!!.top

        mPath!!.offset(offsetX + mIconOffsetX, offsetY + mIconOffsetY)
    }


    /**
     * Ensures the tint filter is consistent with the current tint color and
     * mode.
     */
    private fun updateTintFilter(tint: ColorStateList?, tintMode: PorterDuff.Mode?): PorterDuffColorFilter? {
        if (tint == null || tintMode == null) {
            return null
        }
        // setMode, setColor of PorterDuffColorFilter are not public method in SDK v7. (Thanks @Google still not accessible in API v24)
        // Therefore we create a new one all the time here. Don't expect this is called often.
        val color = tint.getColorForState(state, Color.TRANSPARENT)
        return PorterDuffColorFilter(color, tintMode)
    }


    companion object {
        val ANDROID_ACTIONBAR_ICON_SIZE_DP = 24
        val ANDROID_ACTIONBAR_ICON_SIZE_PADDING_DP = 1

        fun convertDpToPx(context: Context?, dp: Float): Int {
            return applyDimension(COMPLEX_UNIT_DIP, dp, context!!.resources.displayMetrics).toInt()
        }
    }
}
