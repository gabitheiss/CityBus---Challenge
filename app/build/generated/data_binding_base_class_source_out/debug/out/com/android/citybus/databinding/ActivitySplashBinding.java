// Generated by view binder compiler. Do not edit!
package com.android.citybus.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.airbnb.lottie.LottieAnimationView;
import com.android.citybus.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivitySplashBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final LottieAnimationView lottieBusAnimatorView;

  @NonNull
  public final TextView nameAppSubtitle;

  @NonNull
  public final TextView nameAppTitle;

  private ActivitySplashBinding(@NonNull ConstraintLayout rootView,
      @NonNull LottieAnimationView lottieBusAnimatorView, @NonNull TextView nameAppSubtitle,
      @NonNull TextView nameAppTitle) {
    this.rootView = rootView;
    this.lottieBusAnimatorView = lottieBusAnimatorView;
    this.nameAppSubtitle = nameAppSubtitle;
    this.nameAppTitle = nameAppTitle;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivitySplashBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivitySplashBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_splash, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivitySplashBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.lottieBusAnimatorView;
      LottieAnimationView lottieBusAnimatorView = ViewBindings.findChildViewById(rootView, id);
      if (lottieBusAnimatorView == null) {
        break missingId;
      }

      id = R.id.nameAppSubtitle;
      TextView nameAppSubtitle = ViewBindings.findChildViewById(rootView, id);
      if (nameAppSubtitle == null) {
        break missingId;
      }

      id = R.id.nameAppTitle;
      TextView nameAppTitle = ViewBindings.findChildViewById(rootView, id);
      if (nameAppTitle == null) {
        break missingId;
      }

      return new ActivitySplashBinding((ConstraintLayout) rootView, lottieBusAnimatorView,
          nameAppSubtitle, nameAppTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
