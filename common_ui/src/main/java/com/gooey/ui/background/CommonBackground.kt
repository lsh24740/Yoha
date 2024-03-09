package com.gooey.ui.background

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.Utils
import com.gooey.common.utils.DimensionUtils

/**
 *@author lishihui01
 *@Date 2023/9/23
 *@Describe:
 */

interface IDrawableBuilder {
    fun build(): Drawable?
}

sealed class Builder : IDrawableBuilder

class QR {
    companion object {
        @JvmStatic
        fun combine(builder: Builder, corner: Corner): Background {
            return when (builder) {
                is Background -> {
                    builder.corner = corner
                    builder
                }
                is Solid -> {
                    Background.of(builder, corner)
                }
                is Stroke -> {
                    Background.of(builder, corner)
                }
            }
        }

        @JvmStatic
        fun combine(one: Builder, other: Builder): Background {
            return when (one) {
                is Background -> {
                    when (other) {
                        is Stroke -> {
                            one.stroke = other
                            one
                        }
                        is Solid -> {
                            Overlay(one, Background.of(other, one.corner))
                        }
                        is Background -> {
                            Overlay(one, other)
                        }
                    }
                }
                is Stroke -> {
                    when (other) {
                        is Background -> {
                            other.stroke = one
                            other
                        }
                        is Solid -> {
                            Background.of(other, one)
                        }
                        else -> {
                            Background.of(one)
                        }
                    }
                }
                is Solid -> {
                    when (other) {
                        is Background -> {
                            Overlay(other, Background.of(one))
                        }
                        is Stroke -> {
                            Background.of(one, other)
                        }
                        is Solid -> {
                            Overlay(Background.of(one), Background.of(other))
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun solid(color: ColorStateList): Solid {
            return Solid.of(color)
        }

        @JvmStatic
        fun solid(startColor: Int): Solid {
            return Solid.of(startColor)
        }

        @JvmStatic
        fun solid(startColor: Int, endColor: Int): Solid {
            return Solid.of(startColor, endColor)
        }

        @JvmStatic
        fun solid(startColor: Int, endColor: Int, orientation: Int): Solid {
            return Solid.of(startColor, endColor, orientation)
        }

        @JvmStatic
        fun stroke(color: Int): Stroke {
            return Stroke.of(color)
        }

        @JvmStatic
        fun stroke(color: Int, width: Float): Stroke {
            return Stroke.of(color, width)
        }

        @JvmStatic
        fun stroke(color: ColorStateList): Stroke {
            return Stroke.of(color)
        }

        @JvmStatic
        fun stroke(color: ColorStateList, width: Float): Stroke {
            return Stroke.of(color, width)
        }

        @JvmStatic
        fun corner(radius: Float): Corner {
            return Corner.only(radius)
        }

        @JvmStatic
        fun corner(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float): Corner {
            return Corner.of(topLeft, topRight, bottomRight, bottomLeft)
        }

        @JvmStatic
        fun of(solid: Solid, stroke: Stroke): Background {
            return Background.of(solid, stroke)
        }

        @JvmStatic
        fun of(solid: Solid, corner: Corner): Background {
            return Background.of(solid, corner)
        }

        @JvmStatic
        fun of(stroke: Stroke, corner: Corner): Background {
            return Background.of(stroke, corner)
        }

        @JvmStatic
        fun of(solid: Solid, stroke: Stroke, corner: Corner): Background {
            return Background.of(solid, stroke, corner)
        }

        @JvmStatic
        fun stateful(
                normal: Builder? = null,
                pressed: Builder? = null,
                disable: Builder? = null,
                overlay: Builder? = null,
                selected: Builder? = null,
                cornerAll: Corner? = null
        ): IDrawableBuilder {
            return Stateful().apply {
                this.normal = normal
                this.pressed = pressed
                this.disable = disable
                this.overlay = overlay
                this.selected = selected
                this.corner = cornerAll
            }
        }

        internal fun getColor(@ColorRes color: Int): Int {
            return ContextCompat.getColor(Utils.getApp(), color)
        }

        const val TOP_BOTTOM = 1
        const val TR_BL = 2
        const val RIGHT_LEFT = 3
        const val BR_TL = 4
        const val BOTTOM_TOP = 5
        const val BL_TR = 6
        const val LEFT_RIGHT = 7
        const val TL_BR = 8
    }
}

class Overlay(private var base: Background, private var over: Background) : Background() {
    override var solid: Solid? = null
        get() = base.solid
        set(value) {
            over.solid = value
            field = value
        }

    override var stroke: Stroke? = null
        get() = base.stroke
        set(value) {
            over.stroke = value
            field = value
        }

    override var corner: Corner? = null
        get() = base.corner
        set(value) {
            base.corner = value
            over.corner = value
            field = value
        }


    override fun build(): Drawable? {
        return LayerDrawable(arrayOf(base.build(), over.build()))
    }
}

open class Background : Builder() {
    open var corner: Corner? = null
    open var stroke: Stroke? = null
    open var solid: Solid? = null

    override fun build(): Drawable? {
        val result = GradientDrawable()
        corner?.apply {
            if (radiusArray != null) {
                result.cornerRadii = radiusArray
            } else {
                result.cornerRadius = radius
            }
        }
        solid?.apply {
            when {
                colorStateList != null -> {
                    result.color = colorStateList
                }
                colors != null -> {
                    result.colors = colors
                    result.orientation = orientation
                }
                else -> {
                    result.setColor(color)
                }
            }
        }
        stroke?.apply {
            if (colorStateList != null) {
                result.setStroke(width, colorStateList)
            } else {
                result.setStroke(width, color)
            }
        }
        return result
    }

    open operator fun plus(solid: Solid): Background {
        return QR.combine(this, solid)
    }

    open operator fun plus(stroke: Stroke): Background {
        return QR.combine(this, stroke)
    }

    open operator fun plus(corner: Corner): Background {
        return QR.combine(this, corner)
    }

    open operator fun plus(other: Background): Background {
        return QR.combine(this, other)
    }

    fun stateful(
            pressed: Builder? = null,
            disable: Builder? = null,
            overlay: Builder? = null,
            selected: Builder? = null,
            cornerAll: Corner? = null
    ): IDrawableBuilder {
        return if (pressed == null
                && disable == null
                && overlay == null
                && selected == null
                && cornerAll == null
        ) {
            this
        } else {
            QR.stateful(this, pressed, disable, overlay, selected, cornerAll ?: corner)
        }
    }

    companion object {
        @JvmStatic
        fun of(solid: Solid): Background {
            return Background().apply {
                this.solid = solid
            }
        }

        @JvmStatic
        fun of(stroke: Stroke): Background {
            return Background().apply {
                this.stroke = stroke
            }
        }

        @JvmStatic
        fun of(solid: Solid, stroke: Stroke): Background {
            return Background().apply {
                this.solid = solid
                this.stroke = stroke
            }
        }

        @JvmStatic
        fun of(solid: Solid, corner: Corner?): Background {
            return Background().apply {
                this.solid = solid
                this.corner = corner
            }
        }

        @JvmStatic
        fun of(stroke: Stroke, corner: Corner): Background {
            return Background().apply {
                this.stroke = stroke
                this.corner = corner
            }
        }

        @JvmStatic
        fun of(solid: Solid, stroke: Stroke, corner: Corner): Background {
            return Background().apply {
                this.solid = solid
                this.stroke = stroke
                this.corner = corner
            }
        }
    }
}

class Corner {
    var radius = 0.0f
    var radiusArray: FloatArray? = null

    operator fun plus(solid: Solid): Background {
        return QR.combine(solid, this)
    }

    operator fun plus(stroke: Stroke): Background {
        return QR.combine(stroke, this)
    }

    operator fun plus(background: Background): Background {
        return QR.combine(background, this)
    }

    companion object {
        @JvmStatic
        fun only(radius: Float): Corner {
            return Corner().apply {
                this.radius = DimensionUtils.dpToPxF(radius)
            }
        }

        @JvmStatic
        fun of(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float): Corner {
            return Corner().apply {
                this.radiusArray = floatArrayOf(
                        DimensionUtils.dpToPxF(topLeft),
                        DimensionUtils.dpToPxF(topLeft),
                        DimensionUtils.dpToPxF(topRight),
                        DimensionUtils.dpToPxF(topRight),
                        DimensionUtils.dpToPxF(bottomRight),
                        DimensionUtils.dpToPxF(bottomRight),
                        DimensionUtils.dpToPxF(bottomLeft),
                        DimensionUtils.dpToPxF(bottomLeft)
                )
            }
        }
    }
}

class Stroke : Builder() {
    var color = Color.WHITE
    var width = DimensionUtils.dpToPx(0.5f)
    var colorStateList: ColorStateList? = null

    operator fun plus(solid: Solid): Background {
        return QR.combine(this, solid)
    }

    operator fun plus(background: Background): Background {
        return QR.combine(this, background)
    }

    operator fun plus(corner: Corner): Background {
        return QR.combine(this, corner)
    }

    fun stateful(
            pressed: Builder? = null,
            disable: Builder? = null,
            overlay: Builder? = null,
            selected: Builder? = null,
            cornerAll: Corner? = null
    ): IDrawableBuilder {
        return if (pressed == null
                && disable == null
                && overlay == null
                && selected == null
                && cornerAll == null
        ) {
            this
        } else {
            QR.stateful(this, pressed, disable, overlay, selected, cornerAll)
        }
    }

    override fun build(): Drawable? {
        return if (colorStateList != null) {
            GradientDrawable().apply {
                setStroke(width, this@Stroke.colorStateList)
            }
        } else {
            GradientDrawable().apply {
                setStroke(width, this@Stroke.color)
            }
        }
    }

    companion object {
        @JvmStatic
        fun of(color: Int): Stroke {
            return Stroke().apply {
                this.color = color
            }
        }

        @JvmStatic
        fun or(@ColorRes color: Int): Stroke {
            return Stroke().apply {
                this.color = QR.getColor(color)
            }
        }

        @JvmStatic
        fun of(color: Int, width: Float): Stroke {
            return Stroke().apply {
                this.color = color
                this.width = DimensionUtils.dpToPx(width)
            }
        }

        @JvmStatic
        fun of(color: ColorStateList): Stroke {
            return Stroke().apply {
                this.colorStateList = color
            }
        }

        @JvmStatic
        fun of(color: ColorStateList, width: Float): Stroke {
            return Stroke().apply {
                this.colorStateList = color
                this.width = DimensionUtils.dpToPx(width)
            }
        }
    }
}

class Solid : Builder() {
    var orientation = GradientDrawable.Orientation.LEFT_RIGHT
    var color = Color.TRANSPARENT
    var colors: IntArray? = null
    var colorStateList: ColorStateList? = null

    operator fun plus(solid: Solid): Background {
        return QR.combine(this, solid)
    }

    operator fun plus(stroke: Stroke): Background {
        return QR.combine(this, stroke)
    }

    operator fun plus(background: Background): Background {
        return QR.combine(this, background)
    }

    operator fun plus(corner: Corner): Background {
        return QR.combine(this, corner)
    }

    fun stateful(
            pressed: Builder? = null,
            disable: Builder? = null,
            overlay: Builder? = null,
            selected: Builder? = null,
            cornerAll: Corner? = null
    ): IDrawableBuilder {
        return if (pressed == null
                && disable == null
                && overlay == null
                && selected == null
                && cornerAll == null
        ) {
            this
        } else {
            QR.stateful(this, pressed, disable, overlay, selected, cornerAll)
        }
    }

    override fun build(): Drawable? {
        return when {
            colorStateList != null -> {
                GradientDrawable().apply { color = colorStateList }
            }
            colors != null -> {
                GradientDrawable(
                        orientation,
                        colors
                )
            }
            else -> {
                ColorDrawable(color)
            }
        }
    }

    companion object {
        @JvmStatic
        fun of(color: ColorStateList): Solid {
            return Solid().apply {
                this.colorStateList = color
            }
        }

        @JvmStatic
        fun of(color: Int): Solid {
            return Solid().apply {
                this.color = color
            }
        }

        @JvmStatic
        fun or(color: Int): Solid {
            return Solid().apply {
                this.color = QR.getColor(color)
            }
        }

        @JvmStatic
        fun of(startColor: Int, endColor: Int): Solid {
            return Solid().apply {
                this.colors = intArrayOf(startColor, endColor)
            }
        }

        @JvmStatic
        fun or(startColor: Int, endColor: Int): Solid {
            return Solid().apply {
                this.colors = intArrayOf(QR.getColor(startColor), QR.getColor(endColor))
            }
        }

        @JvmStatic
        fun of(startColor: Int, endColor: Int, orientation: Int): Solid {
            return Solid().apply {
                this.colors = intArrayOf(startColor, endColor)
                this.orientation = forAngle(orientation)
            }
        }

        @JvmStatic
        fun or(startColor: Int, endColor: Int, orientation: Int): Solid {
            return Solid().apply {
                this.colors = intArrayOf(QR.getColor(startColor), QR.getColor(endColor))
                this.orientation = forAngle(orientation)
            }
        }

        @JvmStatic
        fun forAngle(input: Int): GradientDrawable.Orientation {
            return when (input) {
                TOP_BOTTOM -> GradientDrawable.Orientation.TOP_BOTTOM
                TR_BL -> GradientDrawable.Orientation.TR_BL
                RIGHT_LEFT -> GradientDrawable.Orientation.RIGHT_LEFT
                BR_TL -> GradientDrawable.Orientation.BR_TL
                BOTTOM_TOP -> GradientDrawable.Orientation.BOTTOM_TOP
                BL_TR -> GradientDrawable.Orientation.BL_TR
                LEFT_RIGHT -> GradientDrawable.Orientation.LEFT_RIGHT
                else -> GradientDrawable.Orientation.TL_BR
            }
        }

        /** draw the gradient from the top to the bottom  */
        const val TOP_BOTTOM = 1

        /** draw the gradient from the top-right to the bottom-left  */
        const val TR_BL = 2

        /** draw the gradient from the right to the left  */
        const val RIGHT_LEFT = 3

        /** draw the gradient from the bottom-right to the top-left  */
        const val BR_TL = 4

        /** draw the gradient from the bottom to the top  */
        const val BOTTOM_TOP = 5

        /** draw the gradient from the bottom-left to the top-right  */
        const val BL_TR = 6

        /** draw the gradient from the left to the right  */
        const val LEFT_RIGHT = 7

        /** draw the gradient from the top-left to the bottom-right  */
        const val TL_BR = 8
    }
}