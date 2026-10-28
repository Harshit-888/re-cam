package com.indymobile.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSMarkupRawImageInfo implements Parcelable {
    public static final Parcelable.Creator<PSMarkupRawImageInfo> CREATOR = new C66611();
    public int rotateDegree;

    /* JADX INFO: renamed from: com.indymobile.app.model.PSMarkupRawImageInfo$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66611 implements Parcelable.Creator<PSMarkupRawImageInfo> {
        @Override // android.os.Parcelable.Creator
        public final PSMarkupRawImageInfo createFromParcel(Parcel parcel) {
            PSMarkupRawImageInfo pSMarkupRawImageInfo = new PSMarkupRawImageInfo();
            pSMarkupRawImageInfo.rotateDegree = parcel.readInt();
            return pSMarkupRawImageInfo;
        }

        @Override // android.os.Parcelable.Creator
        public final PSMarkupRawImageInfo[] newArray(int i) {
            return new PSMarkupRawImageInfo[i];
        }
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.rotateDegree);
    }
}
