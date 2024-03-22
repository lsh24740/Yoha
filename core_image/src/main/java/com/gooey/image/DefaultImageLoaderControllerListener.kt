package com.gooey.image

import android.graphics.drawable.Animatable
import com.facebook.drawee.controller.ControllerListener
import com.facebook.imagepipeline.image.ImageInfo


/**
 *@author lishihui01
 *@Date 2024/3/9
 *@Describe:
 */

class DefaultImageLoaderControllerListener : ControllerListener<ImageInfo?> {
    // com.facebook.drawee.controller.ControllerListener
    override fun onFailure(str: String?, th: Throwable?) {}

    // com.facebook.drawee.controller.ControllerListener
    override fun onFinalImageSet(str: String?, imageInfo: ImageInfo?, animatable: Animatable?) {}

    // com.facebook.drawee.controller.ControllerListener
    override fun onIntermediateImageFailed(str: String?, th: Throwable?) {}

    // com.facebook.drawee.controller.ControllerListener
    override fun onIntermediateImageSet(str: String?, imageInfo: ImageInfo?) {}

    // com.facebook.drawee.controller.ControllerListener
    override fun onRelease(str: String?) {}

    // com.facebook.drawee.controller.ControllerListener
    override fun onSubmit(str: String?, obj: Any?) {}
}