package com.gooey.image

import android.graphics.Bitmap
import com.facebook.common.references.CloseableReference
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory
import com.facebook.imagepipeline.postprocessors.IterativeBoxBlurPostProcessor


/**
 *@author lishihui01
 *@Date 2024/3/9
 *@Describe:
 */

class NovaBlurPostProcessor(blurRadius: Int) : IterativeBoxBlurPostProcessor(blurRadius) {
    override fun process(
        sourceBitmap: Bitmap,
        bitmapFactory: PlatformBitmapFactory
    ): CloseableReference<Bitmap>? {
        val createBitmapInternal = bitmapFactory.createBitmapInternal(
            sourceBitmap.width,
            sourceBitmap.height,
            FALLBACK_BITMAP_CONFIGURATION
        )
        return try {
            process(createBitmapInternal.get(), sourceBitmap)
            CloseableReference.cloneOrNull(createBitmapInternal)
        } finally {
            CloseableReference.closeSafely(createBitmapInternal)
        }
    }
}