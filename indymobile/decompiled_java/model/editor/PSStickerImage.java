package com.indymobile.app.model.editor;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.ads.mediation.inmobi.InMobiNetworkValues;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.UUID;
import p000.fu4;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSStickerImage implements Parcelable {
    public static final Parcelable.Creator<PSStickerImage> CREATOR = new C66761();
    public static final int TYPE_IMAGE = 3;
    public static final int TYPE_PAD = 0;
    public static final int TYPE_SIGNATURE = 1;

    /* JADX INFO: renamed from: a */
    @fu4("image_id")
    private String f31426a;

    /* JADX INFO: renamed from: b */
    @fu4(InMobiNetworkValues.WIDTH)
    private int f31427b;

    /* JADX INFO: renamed from: c */
    @fu4(InMobiNetworkValues.HEIGHT)
    private int f31428c;

    /* JADX INFO: renamed from: d */
    @fu4("type")
    private int f31429d;

    /* JADX INFO: renamed from: e */
    @fu4("created")
    private Date f31430e;

    /* JADX INFO: renamed from: com.indymobile.app.model.editor.PSStickerImage$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66761 implements Parcelable.Creator<PSStickerImage> {
        @Override // android.os.Parcelable.Creator
        public final PSStickerImage createFromParcel(Parcel parcel) {
            return new PSStickerImage(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public final PSStickerImage[] newArray(int i) {
            return new PSStickerImage[i];
        }
    }

    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    public PSStickerImage(Parcel parcel) {
        this.f31426a = parcel.readString();
        this.f31427b = parcel.readInt();
        this.f31428c = parcel.readInt();
        this.f31429d = parcel.readInt();
        long j = parcel.readLong();
        this.f31430e = j == -1 ? null : new Date(j);
    }

    /* JADX INFO: renamed from: a */
    public final int m15948a() {
        return this.f31428c;
    }

    /* JADX INFO: renamed from: b */
    public final String m15949b() {
        return this.f31426a;
    }

    /* JADX INFO: renamed from: c */
    public final int m15950c() {
        return this.f31429d;
    }

    /* JADX INFO: renamed from: d */
    public final int m15951d() {
        return this.f31427b;
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PSStickerImage) {
            return this.f31426a.equals(((PSStickerImage) obj).f31426a);
        }
        return false;
    }

    public final int hashCode() {
        return this.f31426a.hashCode();
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.f31426a);
        parcel.writeInt(this.f31427b);
        parcel.writeInt(this.f31428c);
        parcel.writeInt(this.f31429d);
        Date date = this.f31430e;
        parcel.writeLong(date != null ? date.getTime() : -1L);
    }

    public PSStickerImage(int i, int i2, int i3) {
        this.f31426a = UUID.randomUUID().toString();
        this.f31427b = i;
        this.f31428c = i2;
        this.f31429d = i3;
        this.f31430e = new Date();
    }

    public PSStickerImage() {
    }
}
