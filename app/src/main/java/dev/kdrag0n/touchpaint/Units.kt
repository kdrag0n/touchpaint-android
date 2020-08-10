package dev.kdrag0n.touchpaint

import android.content.res.Resources
import android.util.TypedValue

fun Resources.dpToPx(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
}

fun Resources.pxToDp(px: Float): Float {
    return px / displayMetrics.density
}
