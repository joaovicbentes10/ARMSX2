package com.armsx2.nevoa.intro

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

/** A VideoView that fills its parent instead of letterboxing. The library background maps the
 *  whole window, so the intro has to as well for its last frame to line up with it exactly. */
class FillVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : VideoView(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec))
    }
}
