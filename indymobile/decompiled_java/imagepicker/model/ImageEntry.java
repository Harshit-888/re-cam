package com.indymobile.app.imagepicker.model;

import android.os.Parcel;
import android.os.Parcelable;
import p000.C11249h5;
import p000.g90;
import p000.k26;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class ImageEntry implements Parcelable {
    public static final Parcelable.Creator<ImageEntry> CREATOR = new C11249h5(18);

    /* JADX INFO: renamed from: a */
    public final int f31374a;

    /* JADX INFO: renamed from: b */
    public final String f31375b;

    /* JADX INFO: renamed from: c */
    public final long f31376c;

    public ImageEntry(Parcel parcel) {
        this.f31374a = parcel.readInt();
        this.f31375b = parcel.readString();
        this.f31376c = parcel.readLong();
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    public final boolean equals(Object obj) {
        return (obj instanceof ImageEntry) && ((ImageEntry) obj).f31374a == this.f31374a;
    }

    public final String toString() {
        return k26.m31678p(new StringBuilder("ImageEntry{path='"), this.f31375b, "'}");
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.f31374a);
        parcel.writeString(this.f31375b);
        parcel.writeLong(this.f31376c);
    }

    public ImageEntry(g90 g90Var) {
        this.f31375b = (String) g90Var.f49034c;
        this.f31374a = g90Var.f49032a;
        this.f31376c = g90Var.f49033b;
    }
}
