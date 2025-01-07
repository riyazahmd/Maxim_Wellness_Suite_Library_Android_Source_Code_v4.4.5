package com.maximintegrated.maximsensorsapp

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.maximsensorsapp.exts.dpToPx
import com.maximintegrated.maximsensorsapp.exts.parseTypedArray
import com.maximintegrated.maximsensorsapp.exts.setForegroundSelectable
import kotlinx.android.synthetic.main.view_menu_item.view.*

class MenuItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    var title: String? = null
        set(value) {
            field = value
            if (value != null) {
                titleTextView.text = value
            }
        }

    init {
        inflate(context, R.layout.view_menu_item, this)

        val contentPadding = dpToPx(8).toInt()
        setContentPadding(contentPadding, contentPadding, contentPadding, contentPadding)
        setCardBackgroundColor(ContextCompat.getColorStateList(context, R.color.menu_item_bg))

        isClickable = true
        isFocusable = true

        setForegroundSelectable()

        attrs?.let {
            parseTypedArray(attrs, R.styleable.MenuItemView) {
                getString(R.styleable.MenuItemView_miv_title)?.let { title = it }
                val iconDrawableRes = getResourceId(R.styleable.MenuItemView_miv_icon, 0)
                if (iconDrawableRes != 0) {
                    iconImageView.setImageResource(iconDrawableRes)
                }
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        alpha = if (enabled) 1f else 0.5f
    }
}