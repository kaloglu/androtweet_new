package com.zsk.androtweet2.components

import android.content.Context
import android.graphics.Typeface
import com.zsk.androtweet2.Sealed.Enums
import java.util.*

/**
 * Created by kaloglu on 21/10/2017.
 */


internal class CustomTypeFace {
    companion object {
        private val cache = Hashtable<String, Typeface>()

        fun getTypeFace(context: Context, fontType: Enums.FontType): Typeface? {
            val assetPath: String=fontType.path()

            synchronized(cache) {
                if (!cache.containsKey(assetPath)) {
                    try {
                        val typeFace = Typeface.createFromAsset(context.assets, assetPath)
                        cache.put(assetPath, typeFace)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return null
                    }

                }
                return cache[assetPath]
            }
        }
    }
}
