package com.indymobile.app;

import android.content.Context;
import com.indymobile.app.task.PSDetectPageBorderTask$TransformDetectBorderInfo;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSOpenCV {
    public static native float calculateIOU(Context context, float[] fArr, float[] fArr2);

    public static native PSDetectPageBorderTask$TransformDetectBorderInfo detectPageBorder(Context context, long j, int i, int i2, float f);

    public static native int enhanceDocument(Context context, long j, long j2, int i, int i2);

    public static native void enhancePhoto(Context context, long j, long j2, int i, int i2, int i3);

    public static native void enhanceSignature(Context context, long j, long j2);

    public static native void perspectiveTransformRect(Context context, long j, long j2, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9);

    private static native int[] signatureFilter(Context context, int[] iArr, int i, int i2, int i3);
}
