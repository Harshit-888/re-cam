package com.indymobile.app.imagepicker.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import p000.C11249h5;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class AlbumEntry implements Parcelable {
    public static final Parcelable.Creator<AlbumEntry> CREATOR = new C11249h5(1);

    /* JADX INFO: renamed from: a */
    public int f31367a;

    /* JADX INFO: renamed from: b */
    public String f31368b;

    /* JADX INFO: renamed from: d */
    public ImageEntry f31370d;

    /* JADX INFO: renamed from: c */
    public ArrayList f31369c = new ArrayList();

    /* JADX INFO: renamed from: e */
    public long f31371e = 0;

    /* JADX INFO: renamed from: f */
    public boolean f31372f = false;

    /* JADX INFO: renamed from: g */
    public boolean f31373g = false;

    public AlbumEntry(int i, String str) {
        this.f31367a = i;
        this.f31368b = str == null ? "" : str;
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    public final boolean equals(Object obj) {
        return (obj instanceof AlbumEntry) && ((AlbumEntry) obj).f31367a == this.f31367a;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.f31367a);
        parcel.writeString(this.f31368b);
        parcel.writeTypedList(this.f31369c);
        parcel.writeParcelable(this.f31370d, i);
        parcel.writeLong(this.f31371e);
        parcel.writeByte(this.f31372f ? (byte) 1 : (byte) 0);
        parcel.writeByte(this.f31373g ? (byte) 1 : (byte) 0);
    }
}
