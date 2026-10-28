package com.indymobile.app.activity.editor;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import p000.lg3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class WorldData implements Parcelable {
    public static final Parcelable.Creator<WorldData> CREATOR = new lg3(20);

    /* JADX INFO: renamed from: a */
    public RectF f31146a;

    /* JADX INFO: renamed from: b */
    public float f31147b;

    /* JADX INFO: renamed from: c */
    public float f31148c;

    /* JADX INFO: renamed from: d */
    public float f31149d;

    /* JADX INFO: renamed from: e */
    public float f31150e;

    /* JADX INFO: renamed from: f */
    public float f31151f = 1.0f;

    /* JADX INFO: renamed from: g */
    public float f31152g = 1.0f;

    /* JADX INFO: renamed from: h */
    public float f31153h = 5.0f;

    /* JADX INFO: renamed from: i */
    public float f31154i;

    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public final WorldData clone() {
        WorldData worldData = new WorldData();
        worldData.f31146a = new RectF(this.f31146a);
        worldData.f31147b = this.f31147b;
        worldData.f31148c = this.f31148c;
        worldData.f31149d = this.f31149d;
        worldData.f31150e = this.f31150e;
        worldData.f31151f = this.f31151f;
        worldData.f31152g = this.f31152g;
        worldData.f31153h = this.f31153h;
        worldData.f31154i = this.f31154i;
        return worldData;
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.f31146a, i);
        parcel.writeFloat(this.f31147b);
        parcel.writeFloat(this.f31148c);
        parcel.writeFloat(this.f31149d);
        parcel.writeFloat(this.f31150e);
        parcel.writeFloat(this.f31151f);
        parcel.writeFloat(this.f31152g);
        parcel.writeFloat(this.f31153h);
        parcel.writeFloat(this.f31154i);
    }
}
