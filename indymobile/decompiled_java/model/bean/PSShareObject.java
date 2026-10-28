package com.indymobile.app.model.bean;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.indymobile.app.model.PSPage;
import com.indymobile.app.model.watermark.PSWatermark;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import p000.dy3;
import p000.sv3;
import p000.tv3;
import p000.ud2;
import p000.uv3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSShareObject implements Parcelable {
    public static final Parcelable.Creator<PSShareObject> CREATOR = new C66701();
    public boolean justFileName;

    @Nullable
    public String password;
    public ArrayList<PSShareDocumentBean> shareDocumentBeanList;
    public Intent shareIntent;
    public uv3 shareType;
    public File tempShareFolder;

    @Nullable
    public PSWatermark watermark;
    public tv3 shareFileType = dy3.m28727f().f46840A;
    public sv3 shareFileSize = dy3.m28727f().f46841B;
    public boolean overwriteExistingFile = dy3.m28727f().f46846G;

    /* JADX INFO: renamed from: com.indymobile.app.model.bean.PSShareObject$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66701 implements Parcelable.Creator<PSShareObject> {
        @Override // android.os.Parcelable.Creator
        public final PSShareObject createFromParcel(Parcel parcel) {
            PSShareObject pSShareObject = new PSShareObject();
            int i = parcel.readInt();
            pSShareObject.shareType = i == -1 ? null : uv3.values()[i];
            int i2 = parcel.readInt();
            pSShareObject.shareFileType = i2 == -1 ? null : tv3.values()[i2];
            int i3 = parcel.readInt();
            pSShareObject.shareFileSize = i3 != -1 ? sv3.values()[i3] : null;
            pSShareObject.shareDocumentBeanList = parcel.createTypedArrayList(PSShareDocumentBean.CREATOR);
            pSShareObject.password = parcel.readString();
            pSShareObject.watermark = (PSWatermark) parcel.readParcelable(PSWatermark.class.getClassLoader());
            pSShareObject.tempShareFolder = (File) parcel.readSerializable();
            pSShareObject.overwriteExistingFile = parcel.readByte() != 0;
            pSShareObject.justFileName = parcel.readByte() != 0;
            pSShareObject.shareIntent = (Intent) parcel.readParcelable(Intent.class.getClassLoader());
            return pSShareObject;
        }

        @Override // android.os.Parcelable.Creator
        public final PSShareObject[] newArray(int i) {
            return new PSShareObject[i];
        }
    }

    /* JADX INFO: renamed from: a */
    public final int m15877a() {
        ArrayList<PSShareDocumentBean> arrayList = this.shareDocumentBeanList;
        int size = arrayList.size();
        int size2 = 0;
        int i = 0;
        while (i < size) {
            PSShareDocumentBean pSShareDocumentBean = arrayList.get(i);
            i++;
            size2 += pSShareDocumentBean.pageList.size();
        }
        return size2;
    }

    /* JADX INFO: renamed from: b */
    public final long m15878b() {
        ArrayList<PSShareDocumentBean> arrayList = this.shareDocumentBeanList;
        long j = 0;
        if (arrayList != null) {
            int size = arrayList.size();
            int i = 0;
            while (i < size) {
                PSShareDocumentBean pSShareDocumentBean = arrayList.get(i);
                i++;
                Iterator<PSPage> it = pSShareDocumentBean.pageList.iterator();
                while (it.hasNext()) {
                    j += it.next().resultFileSize;
                }
            }
        }
        return j;
    }

    /* JADX INFO: renamed from: c */
    public final String m15879c() {
        StringBuffer stringBuffer = new StringBuffer("");
        ArrayList arrayListM15880d = m15880d();
        int size = arrayListM15880d.size();
        int i = 0;
        while (i < size) {
            Object obj = arrayListM15880d.get(i);
            i++;
            stringBuffer.append(((PSShareDocumentBean) obj).m15870a());
            stringBuffer.append("\n");
        }
        return stringBuffer.toString().trim();
    }

    /* JADX INFO: renamed from: d */
    public final ArrayList m15880d() {
        ArrayList arrayList = new ArrayList();
        ArrayList<PSShareDocumentBean> arrayList2 = this.shareDocumentBeanList;
        int size = arrayList2.size();
        int i = 0;
        while (i < size) {
            PSShareDocumentBean pSShareDocumentBean = arrayList2.get(i);
            i++;
            PSShareDocumentBean pSShareDocumentBean2 = pSShareDocumentBean;
            if (!ud2.m36446u(pSShareDocumentBean2.m15870a())) {
                arrayList.add(pSShareDocumentBean2);
            }
        }
        return arrayList;
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        uv3 uv3Var = this.shareType;
        parcel.writeInt(uv3Var == null ? -1 : uv3Var.ordinal());
        tv3 tv3Var = this.shareFileType;
        parcel.writeInt(tv3Var == null ? -1 : tv3Var.ordinal());
        sv3 sv3Var = this.shareFileSize;
        parcel.writeInt(sv3Var != null ? sv3Var.ordinal() : -1);
        parcel.writeTypedList(this.shareDocumentBeanList);
        parcel.writeString(this.password);
        parcel.writeParcelable(this.watermark, i);
        parcel.writeSerializable(this.tempShareFolder);
        parcel.writeByte(this.overwriteExistingFile ? (byte) 1 : (byte) 0);
        parcel.writeByte(this.justFileName ? (byte) 1 : (byte) 0);
        parcel.writeParcelable(this.shareIntent, i);
    }
}
