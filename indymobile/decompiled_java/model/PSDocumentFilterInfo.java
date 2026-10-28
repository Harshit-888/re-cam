package com.indymobile.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSDocumentFilterInfo implements Parcelable {
    public static final Parcelable.Creator<PSDocumentFilterInfo> CREATOR = new C66571();
    public int bgColor = -1;

    /* JADX INFO: renamed from: com.indymobile.app.model.PSDocumentFilterInfo$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66571 implements Parcelable.Creator<PSDocumentFilterInfo> {
        @Override // android.os.Parcelable.Creator
        public final PSDocumentFilterInfo createFromParcel(Parcel parcel) {
            PSDocumentFilterInfo pSDocumentFilterInfo = new PSDocumentFilterInfo();
            pSDocumentFilterInfo.bgColor = -1;
            pSDocumentFilterInfo.bgColor = parcel.readInt();
            return pSDocumentFilterInfo;
        }

        @Override // android.os.Parcelable.Creator
        public final PSDocumentFilterInfo[] newArray(int i) {
            return new PSDocumentFilterInfo[i];
        }
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.bgColor);
    }
}
