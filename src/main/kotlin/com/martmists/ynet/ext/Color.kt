package com.martmists.ynet.ext

import com.github.vini2003.blade.common.data.Color

fun Color.Companion.ofRGB(v: Int): Color {
    return Color(((v shr 16) and 0xFF) / 255.0f,
              ((v shr 8) and 0xFF) / 255.0f,
              ((v shr 0) and 0xFF) / 255.0f,
              0f)
}
