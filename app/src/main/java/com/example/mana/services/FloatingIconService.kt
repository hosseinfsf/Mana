package com.example.mana.services

import android.content.ClipboardManager
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import com.example.mana.R
import com.example.mana.ui.theme.ManaTheme

class FloatingIconService : LifecycleService(), ClipboardManager.OnPrimaryClipChangedListener {

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var clipboardManager: ClipboardManager

    private var isWindowShown by mutableStateOf(false)
    private var copiedText by mutableStateOf<String?>(null)

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(this)

        params = createLayoutParams(false)
        composeView = createComposeView()
        windowManager.addView(composeView, params)
    }

    private fun createComposeView(): ComposeView {
        return ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingIconService)
            setContent {
                 val settingsPrefs = getSharedPreferences("mana_settings", Context.MODE_PRIVATE)
                 val iconResId = settingsPrefs.getInt("ICON_RES_ID", R.raw.cat_icon)

                ManaTheme {
                    if (isWindowShown) {
                        FloatingWindow(
                            onClose = { hideWindow() }, 
                            copiedText = copiedText,
                            onCopiedTextConsumed = { copiedText = null }
                        )
                    } else {
                        FloatingIcon(
                            onDrag = { offset ->
                                params.x += offset.x
                                params.y += offset.y
                                windowManager.updateViewLayout(composeView, params)
                            },
                            onTap = { showWindow() },
                            resId = iconResId
                        )
                    }
                }
            }
        }
    }

    override fun onPrimaryClipChanged() {
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text
            if (text != null) {
                copiedText = text.toString()
            }
        }
    }

    private fun showWindow() {
        params = createLayoutParams(true)
        isWindowShown = true
        windowManager.updateViewLayout(composeView, params)
    }

    private fun hideWindow() {
        params = createLayoutParams(false)
        isWindowShown = false
        windowManager.updateViewLayout(composeView, params)
    }

    private fun createLayoutParams(isWindow: Boolean): WindowManager.LayoutParams {
       return if (isWindow) {
             WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                 if(::params.isInitialized) {
                    x = params.x
                    y = params.y
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clipboardManager.removePrimaryClipChangedListener(this)
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
    }
}
