package com.indymobile.app.activity.camera.camerax;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import kotlin.Metadata;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
@Metadata(m31965d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\u0018\u00002\u00020\u0001B'\b\u0007\u0012\u0006\u0010\u0003\u001a\u00020\u0002\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0004\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006¢\u0006\u0004\b\b\u0010\t¨\u0006\n"}, m31966d2 = {"Lcom/indymobile/app/activity/camera/camerax/FocusView;", "Landroid/widget/FrameLayout;", "Landroid/content/Context;", "context", "Landroid/util/AttributeSet;", "attrs", "", "defStyleAttr", "<init>", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "app_release"}, m31967k = 1, m31968mv = {2, 2, 0}, m31970xi = 48)
public final class FocusView extends FrameLayout {

    /* JADX INFO: renamed from: a */
    public final FeedbackCircleView f31145a;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public FocusView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        context.getClass();
        FeedbackCircleView feedbackCircleView = new FeedbackCircleView(context, attributeSet, i);
        this.f31145a = feedbackCircleView;
        setClipToPadding(false);
        setClipChildren(false);
        addView(feedbackCircleView);
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public FocusView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        context.getClass();
    }
}
