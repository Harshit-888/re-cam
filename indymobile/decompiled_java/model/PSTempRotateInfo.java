package com.indymobile.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSTempRotateInfo implements Parcelable {
    public static final Parcelable.Creator<PSTempRotateInfo> CREATOR = new C66661();
    public int rotateDegree;

    /* JADX INFO: renamed from: com.indymobile.app.model.PSTempRotateInfo$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66661 implements Parcelable.Creator<PSTempRotateInfo> {
        @Override // android.os.Parcelable.Creator
        public final PSTempRotateInfo createFromParcel(Parcel parcel) {
            PSTempRotateInfo pSTempRotateInfo = new PSTempRotateInfo();
            pSTempRotateInfo.rotateDegree = parcel.readInt();
            return pSTempRotateInfo;
        }

        @Override // android.os.Parcelable.Creator
        public final PSTempRotateInfo[] newArray(int i) {
            return new PSTempRotateInfo[i];
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
