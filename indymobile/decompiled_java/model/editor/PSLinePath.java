package com.indymobile.app.model.editor;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.indymobile.app.model.PSPoint;
import com.mbridge.msdk.playercommon.exoplayer2.text.ttml.TtmlNode;
import java.util.ArrayList;
import java.util.Vector;
import p000.fu4;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSLinePath implements Parcelable {
    public static final Parcelable.Creator<PSLinePath> CREATOR = new C66731();

    /* JADX INFO: renamed from: a */
    @fu4(alternate = {"brushSize"}, value = "brush_size")
    private float f31398a;

    /* JADX INFO: renamed from: b */
    @fu4(TtmlNode.ATTR_TTS_COLOR)
    private int f31399b;

    /* JADX INFO: renamed from: c */
    @fu4("opacity100")
    private int f31400c;

    /* JADX INFO: renamed from: d */
    @fu4("eraser")
    private boolean f31401d;

    /* JADX INFO: renamed from: e */
    @NonNull
    @fu4("points")
    private ArrayList<PointF> f31402e;

    /* JADX INFO: renamed from: com.indymobile.app.model.editor.PSLinePath$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66731 implements Parcelable.Creator<PSLinePath> {
        @Override // android.os.Parcelable.Creator
        public final PSLinePath createFromParcel(Parcel parcel) {
            return new PSLinePath(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public final PSLinePath[] newArray(int i) {
            return new PSLinePath[i];
        }
    }

    public PSLinePath(Parcel parcel) {
        this.f31402e = new ArrayList<>();
        this.f31398a = parcel.readFloat();
        this.f31399b = parcel.readInt();
        this.f31400c = parcel.readInt();
        this.f31401d = parcel.readByte() != 0;
        parcel.readList(this.f31402e, PSPoint.class.getClassLoader());
    }

    /* JADX INFO: renamed from: a */
    public final Paint m15888a() {
        if (this.f31402e.isEmpty()) {
            return null;
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(this.f31398a);
        if (this.f31401d) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            return paint;
        }
        paint.setColor(this.f31399b);
        paint.setAlpha((this.f31400c * 255) / 100);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        return paint;
    }

    /* JADX INFO: renamed from: b */
    public final Path m15889b(float f, float f2) {
        int size = this.f31402e.size();
        if (size < 2) {
            return null;
        }
        Path path = new Path();
        PointF pointF = this.f31402e.get(0);
        float f3 = pointF.x * f;
        float f4 = pointF.y * f2;
        path.moveTo(f3, f4);
        if (size > 3) {
            int i = 1;
            while (i < size - 1) {
                PointF pointF2 = this.f31402e.get(i);
                float f5 = pointF2.x * f;
                float f6 = pointF2.y * f2;
                path.quadTo(f3, f4, (f5 + f3) / 2.0f, (f6 + f4) / 2.0f);
                i++;
                f4 = f6;
                f3 = f5;
            }
        }
        PointF pointF3 = this.f31402e.get(size - 1);
        path.lineTo(pointF3.x * f, pointF3.y * f2);
        return path;
    }

    /* JADX INFO: renamed from: c */
    public final void m15890c(float f) {
        double radians = Math.toRadians(f % 360.0f);
        ArrayList<PointF> arrayList = this.f31402e;
        int size = arrayList.size();
        int i = 0;
        while (i < size) {
            PointF pointF = arrayList.get(i);
            i++;
            PointF pointF2 = pointF;
            float f2 = pointF2.x - 0.5f;
            float f3 = pointF2.y - 0.5f;
            pointF2.x = ((((float) Math.cos(radians)) * f2) - (((float) Math.sin(radians)) * f3)) + 0.5f;
            pointF2.y = (f3 * ((float) Math.cos(radians))) + (f2 * ((float) Math.sin(radians))) + 0.5f;
        }
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(this.f31398a);
        parcel.writeInt(this.f31399b);
        parcel.writeInt(this.f31400c);
        parcel.writeByte(this.f31401d ? (byte) 1 : (byte) 0);
        parcel.writeList(this.f31402e);
    }

    public PSLinePath(float f, int i, int i2, boolean z, Vector vector) {
        ArrayList<PointF> arrayList = new ArrayList<>();
        this.f31402e = arrayList;
        this.f31398a = f;
        this.f31399b = i;
        this.f31400c = i2;
        this.f31401d = z;
        arrayList.addAll(vector);
    }

    public PSLinePath() {
        this.f31402e = new ArrayList<>();
        this.f31398a = 25.0f;
        this.f31399b = -16777216;
        this.f31400c = 100;
        this.f31401d = false;
    }
}
