package com.indymobile.app.activity.camera.camerax;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.indymobileapp.document.scanner.R;
import kotlin.Metadata;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
@Metadata(m31965d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\b\u0000\u0018\u00002\u00020\u0001B'\b\u0007\u0012\u0006\u0010\u0003\u001a\u00020\u0002\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0004\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006¢\u0006\u0004\b\b\u0010\t¨\u0006\n"}, m31966d2 = {"Lcom/indymobile/app/activity/camera/camerax/FeedbackCircleView;", "Landroid/widget/FrameLayout;", "Landroid/content/Context;", "context", "Landroid/util/AttributeSet;", "attrs", "", "defStyleAttr", "<init>", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "app_release"}, m31967k = 1, m31968mv = {2, 2, 0}, m31970xi = 48)
public final class FeedbackCircleView extends FrameLayout {

    /* JADX INFO: renamed from: a */
    public final AnimatorSet f31144a;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public FeedbackCircleView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        context.getClass();
        ImageView imageView = new ImageView(context, attributeSet, i);
        imageView.setImageResource(R.drawable.camera_focus_inner);
        context.getResources();
        imageView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        imageView.setAlpha(0.0f);
        ImageView imageView2 = new ImageView(context, attributeSet, i);
        imageView2.setImageResource(R.drawable.camera_focus_outer);
        context.getResources();
        imageView2.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        imageView2.setAlpha(0.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        Animator animatorLoadAnimator = AnimatorInflater.loadAnimator(getContext(), R.animator.camera_focus_outer);
        animatorLoadAnimator.setTarget(imageView2);
        Animator animatorLoadAnimator2 = AnimatorInflater.loadAnimator(getContext(), R.animator.camera_focus_inner);
        animatorLoadAnimator2.setTarget(imageView);
        animatorSet.playTogether(animatorLoadAnimator, animatorLoadAnimator2);
        this.f31144a = animatorSet;
        Resources resources = context.getResources();
        setLayoutParams(new ViewGroup.LayoutParams(resources.getDimensionPixelSize(R.dimen.camera_focus_diameter), resources.getDimensionPixelSize(R.dimen.camera_focus_diameter)));
        setClipToPadding(false);
        setClipChildren(false);
        addView(imageView2);
        addView(imageView);
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public FeedbackCircleView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        context.getClass();
    }
}
