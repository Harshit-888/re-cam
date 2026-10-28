package com.indymobile.app.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSRect implements Parcelable {
    public static final Parcelable.Creator<PSRect> CREATOR = new C66652();
    public PSPoint bottomLeft;
    public PSPoint bottomRight;
    public PSPoint topLeft;
    public PSPoint topRight;

    /* JADX INFO: renamed from: com.indymobile.app.model.PSRect$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    class C66641 implements Comparator<PSPoint> {
        @Override // java.util.Comparator
        public final int compare(PSPoint pSPoint, PSPoint pSPoint2) {
            return pSPoint.f31390y - pSPoint2.f31390y > 0.0f ? 1 : -1;
        }
    }

    /* JADX INFO: renamed from: com.indymobile.app.model.PSRect$2 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66652 implements Parcelable.Creator<PSRect> {
        @Override // android.os.Parcelable.Creator
        public final PSRect createFromParcel(Parcel parcel) {
            PSRect pSRect = new PSRect();
            pSRect.topLeft = (PSPoint) parcel.readParcelable(PSPoint.class.getClassLoader());
            pSRect.topRight = (PSPoint) parcel.readParcelable(PSPoint.class.getClassLoader());
            pSRect.bottomLeft = (PSPoint) parcel.readParcelable(PSPoint.class.getClassLoader());
            pSRect.bottomRight = (PSPoint) parcel.readParcelable(PSPoint.class.getClassLoader());
            return pSRect;
        }

        @Override // android.os.Parcelable.Creator
        public final PSRect[] newArray(int i) {
            return new PSRect[i];
        }
    }

    public PSRect(PSPoint pSPoint, PSPoint pSPoint2, PSPoint pSPoint3, PSPoint pSPoint4) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(pSPoint);
        arrayList.add(pSPoint2);
        arrayList.add(pSPoint3);
        arrayList.add(pSPoint4);
        Collections.sort(arrayList, new C66641());
        PSPoint pSPoint5 = (PSPoint) arrayList.get(0);
        PSPoint pSPoint6 = (PSPoint) arrayList.get(1);
        PSPoint pSPoint7 = (PSPoint) arrayList.get(2);
        PSPoint pSPoint8 = (PSPoint) arrayList.get(3);
        if (pSPoint5.f31389x < pSPoint6.f31389x) {
            this.topLeft = pSPoint5;
            this.topRight = pSPoint6;
        } else {
            this.topLeft = pSPoint6;
            this.topRight = pSPoint5;
        }
        if (pSPoint7.f31389x < pSPoint8.f31389x) {
            this.bottomLeft = pSPoint7;
            this.bottomRight = pSPoint8;
        } else {
            this.bottomLeft = pSPoint8;
            this.bottomRight = pSPoint7;
        }
    }

    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public final PSRect clone() {
        return new PSRect(this.topLeft.clone(), this.topRight.clone(), this.bottomLeft.clone(), this.bottomRight.clone());
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.topLeft, i);
        parcel.writeParcelable(this.topRight, i);
        parcel.writeParcelable(this.bottomLeft, i);
        parcel.writeParcelable(this.bottomRight, i);
    }

    public PSRect() {
    }
}
