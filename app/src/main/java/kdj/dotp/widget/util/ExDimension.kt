package kdj.dotp.widget.util

import android.content.Context
import android.util.TypedValue

/**
 * Created by DJKim on 2021/10/01.
 */

@JvmOverloads
fun Int.toPx(context: Context, unit: Int = TypedValue.COMPLEX_UNIT_DIP): Float {
    return this.toFloat().toPx(context, unit)
}

@JvmOverloads
fun Float.toPx(context: Context, unit: Int = TypedValue.COMPLEX_UNIT_DIP): Float {
    return TypedValue.applyDimension(unit, this, context.resources.displayMetrics)
}