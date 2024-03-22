package com.gooey.image

import android.graphics.Bitmap
import android.net.Uri
import android.util.Pair
import com.blankj.utilcode.util.Utils
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.common.util.UriUtil
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.DraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder


/**
 *@author lishihui01
 *@Date 2024/3/9
 *@Describe:
 */
object ImageLoader {
    fun init() {
        Fresco.initialize(
            Utils.getApp(),
            ImagePipelineConfig.newBuilder(Utils.getApp()).setDownsampleEnabled(true).build()
        )
    }

    fun loadImage(view: DraweeView<*>, uri: String?) {
        val parse = Uri.parse(uri)
        val viewSize = getViewSize(view, 0, 0)
        val newBuilderWithSource = ImageRequestBuilder.newBuilderWithSource(parse)
        val resizeOptions =
            newBuilderWithSource.setResizeOptions(ResizeOptions(viewSize.first, viewSize.second))
        if (!UriUtil.isNetworkUri(parse)) {
            resizeOptions.setLocalThumbnailPreviewsEnabled(true)
        }
        view.controller = Fresco.newDraweeControllerBuilder().setOldController(view.controller)
            .setImageRequest(resizeOptions.build()).build()
    }


    fun loadImage(uri: String?, imageLoaderControllerListener: ImageLoaderControllerListener?) {
        val parse = Uri.parse(uri)
        val newBuilderWithSource = ImageRequestBuilder.newBuilderWithSource(parse)
        if (!UriUtil.isNetworkUri(parse)) {
            newBuilderWithSource.setLocalThumbnailPreviewsEnabled(true)
        }
        val fetchDecodedImage: DataSource<CloseableReference<CloseableImage>> =
            ImagePipelineFactory.getInstance().imagePipeline.fetchDecodedImage(
                newBuilderWithSource.build(),
                null
            )
        fetchDecodedImage.subscribe(object : BaseBitmapDataSubscriber() {
            override fun onNewResultImpl(bitmap: Bitmap?) {
                imageLoaderControllerListener?.onNewResultImpl(bitmap)
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                imageLoaderControllerListener?.onFailureImpl()
            }

        }, UiThreadImmediateExecutorService.getInstance())
    }

    fun loadRoundImage(view: DraweeView<GenericDraweeHierarchy>, uri: String?, radius: Float) {
        val viewSize = getViewSize(view, 0, 0)
        view.hierarchy = GenericDraweeHierarchyBuilder.newInstance(view.resources)
            .setRoundingParams(RoundingParams.fromCornersRadius(radius)).build()
        val newBuilderWithSource = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
        view.controller = Fresco.newDraweeControllerBuilder().setOldController(view.controller)
            .setImageRequest(
                newBuilderWithSource.setResizeOptions(
                    ResizeOptions(
                        viewSize.first,
                        viewSize.second
                    )
                ).build()
            ).build()
    }

    fun loadRoundImage(view: DraweeView<GenericDraweeHierarchy>, uri: String?, radii: FloatArray?) {
        val viewSize = getViewSize(view, 0, 0)
        view.hierarchy = GenericDraweeHierarchyBuilder.newInstance(view.resources)
            .setRoundingParams(RoundingParams.fromCornersRadii(radii)).build()
        val newBuilderWithSource = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
        view.controller = Fresco.newDraweeControllerBuilder().setOldController(view.controller)
            .setImageRequest(
                newBuilderWithSource.setResizeOptions(
                    ResizeOptions(
                        viewSize.first,
                        viewSize.second
                    )
                ).build()
            ).build()
    }

    fun loadAnimatedImage(
        view: DraweeView<*>,
        uri: String?,
        defaultImageLoaderControllerListener: DefaultImageLoaderControllerListener
    ) {
        val parse = Uri.parse(uri)
        val newBuilderWithSource = ImageRequestBuilder.newBuilderWithSource(parse)
        val viewSize = getViewSize(view, 0, 0)
        val width = viewSize.first
        val height = viewSize.second
        val resizeOptions =
            newBuilderWithSource.setResizeOptions(ResizeOptions(width, height))
        if (!UriUtil.isNetworkUri(parse)) {
            resizeOptions.setLocalThumbnailPreviewsEnabled(true)
        }
        view.controller = Fresco.newDraweeControllerBuilder()
            .setOldController(view.controller)
            .setImageRequest(resizeOptions.build())
            .setControllerListener(defaultImageLoaderControllerListener).build()
    }

    fun loadBlurImage(view: DraweeView<*>, uri: String?, blurRadius: Int) {
        val viewSize = getViewSize(view, 0, 0)
        val newBuilderWithSource = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
        val resizeOptions =
            newBuilderWithSource.setResizeOptions(ResizeOptions(viewSize.first, viewSize.second))
        if (blurRadius > 0) {
            resizeOptions.setPostprocessor(NovaBlurPostProcessor(blurRadius))
        }
        view.controller = Fresco.newDraweeControllerBuilder()
            .setOldController(view.controller)
            .setImageRequest(resizeOptions.build()).build()
    }

    fun loadCircleImage(
        view: DraweeView<GenericDraweeHierarchy>,
        uri: String?
    ) {
        loadCircleImageWithBorder(view, uri, 0, 0f)
    }

    fun loadCircleImageWithBorder(
        view: DraweeView<GenericDraweeHierarchy>,
        uri: String?,
        borderColor: Int,
        borderWidth: Float
    ) {
        val viewSize = getViewSize(view, 0, 0)
        val asCircle = RoundingParams.asCircle()
        if (borderWidth > 0.0f) {
            asCircle.setBorder(borderColor, borderWidth)
        }
        view.hierarchy =
            GenericDraweeHierarchyBuilder.newInstance(view.resources).setRoundingParams(asCircle)
                .build()
        val newBuilderWithSource = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
        view.controller = Fresco.newDraweeControllerBuilder().setOldController(view.controller)
            .setImageRequest(
                newBuilderWithSource.setResizeOptions(
                    ResizeOptions(
                        viewSize.first,
                        viewSize.second
                    )
                ).build()
            ).build()
    }

    private fun getViewSize(
        draweeView: DraweeView<*>?,
        defaultWidth: Int,
        defaultHeight: Int
    ): Pair<Int, Int> {
        var width = defaultWidth
        var height = defaultHeight
        if (draweeView != null) {
            val layoutParams = draweeView.layoutParams
            if (layoutParams != null && layoutParams.width > 0 && layoutParams.height > 0) {
                width = layoutParams.width
                height = layoutParams.height
            }
            if (width == 0) {
                width = draweeView.maxWidth
                if (width < 0 || width >= Int.MAX_VALUE) {
                    width = 0
                }
            }
            if (height == 0) {
                height = draweeView.maxHeight
                if (height < 0 || height >= Int.MAX_VALUE) {
                    height = 0
                }
            }
        }
        return Pair(width, height)
    }
}