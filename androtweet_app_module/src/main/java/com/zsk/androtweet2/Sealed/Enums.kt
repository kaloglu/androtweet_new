package com.zsk.androtweet2.Sealed

/**
 * Created by kaloglu on 21/10/2017.
 */
open class Enums {
    enum class FontType {
        NORMAL {
            override fun path() = ""
        },
        FONT_ICON {
            override fun path() = "font/Fontello.ttf"
        };

        abstract fun path(): String
    }
}
