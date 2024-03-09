package com.gooey.ui.background

import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable

/**
 *@author lishihui01
 *@Date 2023/9/23
 *@Describe:
 */
class Stateful : IDrawableBuilder {
    var normal: Builder? = null
    var pressed: Builder? = null
    var disable: Builder? = null
    var overlay: Builder? = null
    var selected: Builder? = null
    var corner: Corner? = null

    private fun combine(b: Builder?, corner: Corner?): Builder? {
        return if (b != null && corner != null) {
            QR.combine(b, corner)
        } else {
            b
        }
    }

    override fun build(): Drawable? {
        val normal = this.normal ?: return null
        val disable = this.disable
        val overlay = this.overlay
        val selected = this.selected
        val corner = this.corner

        val p = this.pressed ?: when {
            overlay != null -> QR.combine(normal, overlay)
            else -> normal
        }
        val drawable = StateListDrawable()
        if (selected != null) {
            val ps = this.pressed ?: when {
                overlay != null -> QR.combine(selected, overlay)
                else -> selected
            }
            drawable.addState(
                    intArrayOf(
                            android.R.attr.state_enabled,
                            android.R.attr.state_selected,
                            android.R.attr.state_pressed
                    ), combine(ps, corner)?.build()
            )
            drawable.addState(
                    intArrayOf(
                            android.R.attr.state_enabled,
                            android.R.attr.state_selected
                    ), combine(selected, corner)?.build()
            )
            drawable.addState(
                    intArrayOf(
                            android.R.attr.state_enabled,
                            android.R.attr.state_pressed
                    ), combine(p, corner)?.build()
            )
            drawable.addState(
                    intArrayOf(-android.R.attr.state_enabled),
                    combine(disable, corner)?.build()
            )
            drawable.addState(intArrayOf(), combine(normal, corner)?.build())
        } else {
            drawable.addState(
                    intArrayOf(
                            android.R.attr.state_enabled,
                            android.R.attr.state_pressed
                    ), combine(p, corner)?.build()
            )
            drawable.addState(
                    intArrayOf(-android.R.attr.state_enabled),
                    combine(disable, corner)?.build()
            )
            drawable.addState(intArrayOf(), combine(normal, corner)?.build())
        }
        return drawable
    }
}