package com.android.citybus.ext

import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionManager

fun View.visible() {
    if (this.visibility != View.VISIBLE) {
        if (parent is ViewGroup) TransitionManager.beginDelayedTransition(parent as ViewGroup)
        visibility = View.VISIBLE
    }
}

fun View.gone() {
    if (this.visibility != View.GONE) {
        if (parent is ViewGroup) TransitionManager.beginDelayedTransition(parent as ViewGroup)
        visibility = View.GONE
    }
}