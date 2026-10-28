package com.indymobile.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSPoint implements Parcelable {
    public static final Parcelable.Creator<PSPoint> CREATOR = new C66631();

    /* JADX INFO: renamed from: x */
    public float f31389x;

    /* JADX INFO: renamed from: y */
    public float f31390y;

    /* JADX INFO: renamed from: com.indymobile.app.model.PSPoint$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66631 implements Parcelable.Creator<PSPoint> {
        @Override // android.os.Parcelable.Creator
        public final PSPoint createFromParcel(Parcel parcel) {
            PSPoint pSPoint = new PSPoint();
            pSPoint.f31389x = parcel.readFloat();
            pSPoint.f31390y = parcel.readFloat();
            return pSPoint;
        }

        @Override // android.os.Parcelable.Creator
        public final PSPoint[] newArray(int i) {
            return new PSPoint[i];
        }
    }

    public PSPoint(float f, float f2) {
        this.f31389x = f;
        this.f31390y = f2;
    }

    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public final PSPoint clone() {
        return new PSPoint(this.f31389x, this.f31390y);
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PSPoint)) {
            return false;
        }
        PSPoint pSPoint = (PSPoint) obj;
        return this.f31389x == pSPoint.f31389x && this.f31390y == pSPoint.f31390y;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(this.f31389x);
        parcel.writeFloat(this.f31390y);
    }

    public PSPoint() {
        this(0.0f, 0.0f);
    }
}
