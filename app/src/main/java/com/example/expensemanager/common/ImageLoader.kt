package com.example.expensemanager.common

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

object ImageLoader {

    fun loadAvatar(
        imageView: ImageView,
        url: String?,
        @DrawableRes placeholderRes: Int = android.R.drawable.sym_def_app_icon
    ) {
        Glide.with(imageView.context)
            .load(url)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade(150))
            .into(imageView)
    }

    fun loadTransactionImage(
        imageView: ImageView,
        url: String?,
        sizePx: Int = 144,
        @DrawableRes placeholderRes: Int = android.R.drawable.ic_menu_gallery
    ) {
        if (url.isNullOrBlank()) {
            imageView.setImageResource(placeholderRes)
            return
        }

        Glide.with(imageView.context)
            .load(url)
            .override(sizePx, sizePx)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .into(imageView)
    }

    fun clear(imageView: ImageView) {
        Glide.with(imageView.context).clear(imageView)
    }
}
