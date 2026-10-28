package com.indymobile.app.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;
import p000.cv3;
import p000.yu3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSDocumentProcessInfo implements Parcelable {
    public static final Parcelable.Creator<PSDocumentProcessInfo> CREATOR = new C66581();
    public PSRect autoDetectRect;
    public List<PSLine> bottomLines;
    public yu3 colorType;
    public cv3 enhanceType = cv3.kPSEnhanceTypeUndefined;
    public boolean fixToFitImage;
    public float height;
    public List<PSLine> leftLines;
    public List<PSLine> rightLines;
    public List<PSLine> topLines;
    public float userRotateDegree;
    public PSRect userSelectRect;
    public float width;

    /* JADX INFO: renamed from: com.indymobile.app.model.PSDocumentProcessInfo$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66581 implements Parcelable.Creator<PSDocumentProcessInfo> {
        @Override // android.os.Parcelable.Creator
        public final PSDocumentProcessInfo createFromParcel(Parcel parcel) {
            PSDocumentProcessInfo pSDocumentProcessInfo = new PSDocumentProcessInfo();
            pSDocumentProcessInfo.enhanceType = cv3.kPSEnhanceTypeUndefined;
            pSDocumentProcessInfo.width = parcel.readFloat();
            pSDocumentProcessInfo.height = parcel.readFloat();
            pSDocumentProcessInfo.fixToFitImage = parcel.readByte() != 0;
            pSDocumentProcessInfo.autoDetectRect = (PSRect) parcel.readParcelable(PSRect.class.getClassLoader());
            pSDocumentProcessInfo.userSelectRect = (PSRect) parcel.readParcelable(PSRect.class.getClassLoader());
            Parcelable.Creator<PSLine> creator = PSLine.CREATOR;
            pSDocumentProcessInfo.leftLines = parcel.createTypedArrayList(creator);
            pSDocumentProcessInfo.topLines = parcel.createTypedArrayList(creator);
            pSDocumentProcessInfo.bottomLines = parcel.createTypedArrayList(creator);
            pSDocumentProcessInfo.rightLines = parcel.createTypedArrayList(creator);
            pSDocumentProcessInfo.userRotateDegree = parcel.readFloat();
            int i = parcel.readInt();
            pSDocumentProcessInfo.enhanceType = i == -1 ? null : cv3.values()[i];
            int i2 = parcel.readInt();
            pSDocumentProcessInfo.colorType = i2 != -1 ? yu3.values()[i2] : null;
            return pSDocumentProcessInfo;
        }

        @Override // android.os.Parcelable.Creator
        public final PSDocumentProcessInfo[] newArray(int i) {
            return new PSDocumentProcessInfo[i];
        }
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(this.width);
        parcel.writeFloat(this.height);
        parcel.writeByte(this.fixToFitImage ? (byte) 1 : (byte) 0);
        parcel.writeParcelable(this.autoDetectRect, i);
        parcel.writeParcelable(this.userSelectRect, i);
        parcel.writeTypedList(this.leftLines);
        parcel.writeTypedList(this.topLines);
        parcel.writeTypedList(this.bottomLines);
        parcel.writeTypedList(this.rightLines);
        parcel.writeFloat(this.userRotateDegree);
        cv3 cv3Var = this.enhanceType;
        parcel.writeInt(cv3Var == null ? -1 : cv3Var.ordinal());
        yu3 yu3Var = this.colorType;
        parcel.writeInt(yu3Var != null ? yu3Var.ordinal() : -1);
    }
}
