package com.indymobile.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSLine implements Parcelable {
    public static final Parcelable.Creator<PSLine> CREATOR = new C66601();

    /* JADX INFO: renamed from: p1 */
    public PSPoint f31385p1;

    /* JADX INFO: renamed from: p2 */
    public PSPoint f31386p2;

    /* JADX INFO: renamed from: com.indymobile.app.model.PSLine$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66601 implements Parcelable.Creator<PSLine> {
        @Override // android.os.Parcelable.Creator
        public final PSLine createFromParcel(Parcel parcel) {
            PSLine pSLine = new PSLine();
            pSLine.f31385p1 = (PSPoint) parcel.readParcelable(PSPoint.class.getClassLoader());
            pSLine.f31386p2 = (PSPoint) parcel.readParcelable(PSPoint.class.getClassLoader());
            return pSLine;
        }

        @Override // android.os.Parcelable.Creator
        public final PSLine[] newArray(int i) {
            return new PSLine[i];
        }
    }

    public PSLine(PSPoint pSPoint, PSPoint pSPoint2) {
        this.f31385p1 = pSPoint;
        this.f31386p2 = pSPoint2;
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.f31385p1, i);
        parcel.writeParcelable(this.f31386p2, i);
    }
}
