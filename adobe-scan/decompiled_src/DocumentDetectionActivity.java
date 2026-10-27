package com.adobe.dcmscan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.adobe.dcmscan.DocumentDetectionActivity;
import com.adobe.pdfeditclient.analytics.DCMScanEditAnalytics;
import defpackage.a38;
import defpackage.amm;
import defpackage.b31;
import defpackage.cie;
import defpackage.d6m;
import defpackage.dmm;
import defpackage.e38;
import defpackage.e5a;
import defpackage.f2b;
import defpackage.fdj;
import defpackage.fr5;
import defpackage.g2h;
import defpackage.g5a;
import defpackage.h31;
import defpackage.hcf;
import defpackage.i6a;
import defpackage.kzg;
import defpackage.l7f;
import defpackage.m59;
import defpackage.nck;
import defpackage.pr5;
import defpackage.qii;
import defpackage.r44;
import defpackage.s28;
import defpackage.sw6;
import defpackage.t56;
import defpackage.tf5;
import defpackage.u5a;
import defpackage.ug5;
import defpackage.ujk;
import defpackage.v28;
import defpackage.w21;
import defpackage.wck;
import defpackage.wtl;
import defpackage.x75;
import defpackage.yc6;
import defpackage.z28;
import defpackage.zvi;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

/* JADX INFO: compiled from: DocumentDetectionActivity.kt */
/* JADX INFO: loaded from: classes2.dex */
public final class DocumentDetectionActivity extends com.adobe.dcmscan.a {
    public static final /* synthetic */ int Y = 0;
    public final amm P = new amm(g2h.a(s.class), new f(), new s28(0), new g());
    public ClipData Q;
    public int R;
    public final hcf S;
    public final hcf T;
    public final hcf U;
    public final hcf V;
    public final wtl W;
    public final h31 X;

    /* JADX INFO: compiled from: DocumentDetectionActivity.kt */
    public static final class a implements c {
        public static final a a = new a();
        public static final Parcelable.Creator<a> CREATOR = new C0072a();

        /* JADX INFO: renamed from: com.adobe.dcmscan.DocumentDetectionActivity$a$a, reason: collision with other inner class name */
        /* JADX INFO: compiled from: DocumentDetectionActivity.kt */
        public static final class C0072a implements Parcelable.Creator<a> {
            @Override // android.os.Parcelable.Creator
            public final a createFromParcel(Parcel parcel) {
                parcel.getClass();
                parcel.readInt();
                return a.a;
            }

            @Override // android.os.Parcelable.Creator
            public final a[] newArray(int i) {
                return new a[i];
            }
        }

        @Override // android.os.Parcelable
        public final int describeContents() {
            return 0;
        }

        public final boolean equals(Object obj) {
            return this == obj || (obj instanceof a);
        }

        public final int hashCode() {
            return -617957271;
        }

        public final String toString() {
            return "NoPhotosSelectedDialog";
        }

        @Override // android.os.Parcelable
        public final void writeToParcel(Parcel parcel, int i) {
            parcel.getClass();
            parcel.writeInt(1);
        }
    }

    /* JADX INFO: compiled from: DocumentDetectionActivity.kt */
    public static final class b implements c {
        public static final b a = new b();
        public static final Parcelable.Creator<b> CREATOR = new a();

        /* JADX INFO: compiled from: DocumentDetectionActivity.kt */
        public static final class a implements Parcelable.Creator<b> {
            @Override // android.os.Parcelable.Creator
            public final b createFromParcel(Parcel parcel) {
                parcel.getClass();
                parcel.readInt();
                return b.a;
            }

            @Override // android.os.Parcelable.Creator
            public final b[] newArray(int i) {
                return new b[i];
            }
        }

        @Override // android.os.Parcelable
        public final int describeContents() {
            return 0;
        }

        public final boolean equals(Object obj) {
            return this == obj || (obj instanceof b);
        }

        public final int hashCode() {
            return -1067683253;
        }

        public final String toString() {
            return "PageImportLimitDialog";
        }

        @Override // android.os.Parcelable
        public final void writeToParcel(Parcel parcel, int i) {
            parcel.getClass();
            parcel.writeInt(1);
        }
    }

    /* JADX INFO: compiled from: DocumentDetectionActivity.kt */
    public interface c extends Parcelable {
    }

    /* JADX INFO: compiled from: DocumentDetectionActivity.kt */
    public static final /* synthetic */ class d extends i6a implements g5a<Boolean, d6m> {
        @Override // defpackage.g5a
        public final d6m invoke(Boolean bool) {
            ((DocumentDetectionActivity) this.receiver).E0(bool.booleanValue());
            return d6m.a;
        }
    }

    /* JADX INFO: compiled from: DocumentDetectionActivity.kt */
    public static final /* synthetic */ class e extends i6a implements g5a<Boolean, d6m> {
        @Override // defpackage.g5a
        public final d6m invoke(Boolean bool) {
            Boolean bool2 = bool;
            bool2.getClass();
            ((DocumentDetectionActivity) this.receiver).T.setValue(bool2);
            t56.a.getClass();
            t56.g.setValue(bool2);
            return d6m.a;
        }
    }

    /* JADX INFO: compiled from: ActivityViewModelLazy.kt */
    public static final class f implements e5a<dmm> {
        public f() {
        }

        @Override // defpackage.e5a
        public final dmm invoke() {
            return DocumentDetectionActivity.this.getViewModelStore();
        }
    }

    /* JADX INFO: compiled from: ActivityViewModelLazy.kt */
    public static final class g implements e5a<yc6> {
        public g() {
        }

        @Override // defpackage.e5a
        public final yc6 invoke() {
            return DocumentDetectionActivity.this.getDefaultViewModelCreationExtras();
        }
    }

    /* JADX INFO: compiled from: DocumentDetectionActivity.kt */
    public static final /* synthetic */ class h extends i6a implements e5a<d6m> {
        @Override // defpackage.e5a
        public final d6m invoke() {
            DocumentDetectionActivity documentDetectionActivity = (DocumentDetectionActivity) this.receiver;
            int i = DocumentDetectionActivity.Y;
            documentDetectionActivity.finish();
            return d6m.a;
        }
    }

    /* JADX INFO: compiled from: DocumentDetectionActivity.kt */
    public static final /* synthetic */ class i extends i6a implements e5a<d6m> {
        @Override // defpackage.e5a
        public final d6m invoke() {
            DocumentDetectionActivity documentDetectionActivity = (DocumentDetectionActivity) this.receiver;
            int i = DocumentDetectionActivity.Y;
            documentDetectionActivity.getClass();
            sw6 sw6VarP = sw6.f.p();
            f2b.a.getClass();
            HashMap<String, Object> map = new HashMap<>();
            t56.a.getClass();
            map.put("adb.event.context.edges_detected", t56.g.getValue().booleanValue() ? DCMScanEditAnalytics.VALUE_YES : DCMScanEditAnalytics.VALUE_NO);
            sw6VarP.f("DCMScan:Operation:Doc Detect Import", map);
            wck<fdj> wckVar = documentDetectionActivity.C0().g;
            ListIterator<fdj> listIterator = wckVar.listIterator();
            ClipData clipData = null;
            while (true) {
                ujk ujkVar = (ujk) listIterator;
                if (!ujkVar.hasNext()) {
                    break;
                }
                fdj fdjVar = (fdj) ujkVar.next();
                ClipData.Item item = new ClipData.Item(fdjVar.b.a);
                if (fdjVar.equals(x75.M(wckVar))) {
                    clipData = new ClipData(new ClipDescription("Uri", new String[]{"text/uri-list"}), item);
                } else if (clipData != null) {
                    clipData.addItem(item);
                }
            }
            documentDetectionActivity.Q = clipData;
            if (clipData == null || clipData.getItemCount() <= 0) {
                documentDetectionActivity.D0(a.a);
            } else {
                if (documentDetectionActivity.Q != null) {
                    Intent intent = new Intent();
                    intent.setClipData(documentDetectionActivity.Q);
                    intent.putExtra("importFrom", 1);
                    sw6.a aVar = sw6.f;
                    HashMap mapU = s.u(documentDetectionActivity.C0());
                    HashMap map2 = new HashMap();
                    for (Map.Entry entry : mapU.entrySet()) {
                        String str = (String) entry.getKey();
                        Object value = entry.getValue();
                        if (value instanceof Serializable) {
                            map2.put(str, value);
                        } else {
                            Log.e("Non Serializable Object: ", str + " " + value);
                        }
                    }
                    intent.putExtra("docDetectContextData", map2);
                    CaptureActivity.y yVar = documentDetectionActivity.C0().i;
                    if (yVar != null) {
                        intent.putExtra("RetakeOperation", yVar);
                    }
                    documentDetectionActivity.setResult(-1, intent);
                } else {
                    documentDetectionActivity.setResult(0);
                }
                documentDetectionActivity.finish();
            }
            return d6m.a;
        }
    }

    public DocumentDetectionActivity() {
        f2b f2bVar = f2b.a;
        f2bVar.getClass();
        Boolean bool = (Boolean) f2b.j.getValue(f2bVar, f2b.b[4]);
        bool.getClass();
        this.S = nck.g(bool);
        t56.a.getClass();
        Boolean value = t56.g.getValue();
        value.getClass();
        this.T = nck.g(value);
        this.U = nck.g(Boolean.TRUE);
        this.V = nck.g(null);
        this.W = new wtl(new h(0, this, DocumentDetectionActivity.class, "onClose", "onClose()V", 0), new i(0, this, DocumentDetectionActivity.class, "onDone", "onDone()V", 0), (e5a) null, (e5a) null, 28);
        this.X = (h31) V(new b31(), new w21() { // from class: u28
            @Override // defpackage.w21
            public final void a(Object obj) {
                v21 v21Var = (v21) obj;
                int i2 = DocumentDetectionActivity.Y;
                v21Var.getClass();
                int i3 = v21Var.a;
                DocumentDetectionActivity documentDetectionActivity = this.a;
                if (i3 != -1) {
                    documentDetectionActivity.U.setValue(Boolean.TRUE);
                    return;
                }
                Intent intent = v21Var.b;
                if (intent != null) {
                    intent.putExtra("importFrom", 2);
                    documentDetectionActivity.setResult(-1, intent);
                } else {
                    documentDetectionActivity.setResult(0);
                }
                documentDetectionActivity.finish();
            }
        });
    }

    public final void A0(fr5 fr5Var, final int i2) {
        pr5 pr5VarJ = fr5Var.j(-176085635);
        int i3 = (pr5VarJ.z(this) ? 4 : 2) | i2;
        int i4 = 0;
        if (pr5VarJ.q(i3 & 1, (i3 & 3) != 2)) {
            c cVarB0 = B0();
            boolean z = cVarB0 instanceof b;
            fr5.a.C0249a c0249a = fr5.a.a;
            if (z) {
                pr5VarJ.P(-1611945483);
                int iS = f2b.a.s();
                boolean z2 = pr5VarJ.z(this);
                Object objX = pr5VarJ.x();
                if (z2 || objX == c0249a) {
                    objX = new z28(i4, this);
                    pr5VarJ.r(objX);
                }
                l7f.a(iS, (e5a) objX, pr5VarJ, 0);
                pr5VarJ.Y(false);
            } else if (cVarB0 instanceof a) {
                pr5VarJ.P(-1611667010);
                boolean z3 = pr5VarJ.z(this);
                Object objX2 = pr5VarJ.x();
                if (z3 || objX2 == c0249a) {
                    objX2 = new a38(i4, this);
                    pr5VarJ.r(objX2);
                }
                cie.a((e5a) objX2, pr5VarJ, 0);
                pr5VarJ.Y(false);
            } else {
                pr5VarJ.P(-1611454877);
                pr5VarJ.Y(false);
            }
        } else {
            pr5VarJ.G();
        }
        kzg kzgVarA0 = pr5VarJ.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a(i2) { // from class: b38
                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    int i5 = DocumentDetectionActivity.Y;
                    int iB = dae.b(1);
                    this.a.A0((fr5) obj, iB);
                    return d6m.a;
                }
            };
        }
    }

    public final c B0() {
        return (c) this.V.getValue();
    }

    public final s C0() {
        return (s) this.P.getValue();
    }

    public final void D0(c cVar) {
        cVar.getClass();
        if (B0() == null) {
            this.V.setValue(cVar);
            return;
        }
        String strE = g2h.a(cVar.getClass()).e();
        c cVarB0 = B0();
        String strA = r44.a("Couldn't show ", strE, ", already showing ", cVarB0 != null ? g2h.a(cVarB0.getClass()).e() : null);
        if (zvi.a) {
            Log.d("DocumentDetectionActivity", strA);
        }
    }

    public final void E0(boolean z) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("adb.event.context.toggle_on", z ? DCMScanEditAnalytics.VALUE_YES : DCMScanEditAnalytics.VALUE_NO);
        sw6.f.p().f("DCMScan:Workflow:Toggle Documents Only", map);
        this.S.setValue(Boolean.valueOf(z));
        C0().b.setValue(Boolean.valueOf(z));
        f2b f2bVar = f2b.a;
        f2bVar.getClass();
        f2b.j.setValue(f2bVar, f2b.b[4], Boolean.valueOf(z));
    }

    @Override // com.adobe.dcmscan.a
    public final m59 h0() {
        return null;
    }

    @Override // com.adobe.dcmscan.a, androidx.fragment.app.f, defpackage.of5, defpackage.sf5, android.app.Activity
    @SuppressLint({"InflateParams"})
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        C0().c.setValue(Boolean.valueOf(o0().H));
        f2b.a.getClass();
        f2b.E(this);
        Intent intent = getIntent();
        int i2 = 0;
        this.R = intent.getIntExtra("NumberOfPages", 0);
        C0().i = (CaptureActivity.y) intent.getParcelableExtra("RetakeOperation");
        s sVarC0 = C0();
        CaptureActivity.y yVar = C0().i;
        sVarC0.h.setValue(Boolean.valueOf(yVar != null && yVar.a));
        tf5.a(this, new ug5(-2060503285, true, new v28(i2, this)));
        C0().t();
    }

    @Override // com.adobe.dcmscan.a, androidx.fragment.app.f, android.app.Activity
    public final void onPause() {
        s sVarC0 = C0();
        e38 e38Var = sVarC0.d;
        if (e38Var != null) {
            e38Var.c();
        }
        sVarC0.m.setValue(Boolean.TRUE);
        hcf hcfVar = sVarC0.k;
        Boolean bool = Boolean.FALSE;
        hcfVar.setValue(bool);
        sVarC0.l.setValue(bool);
        sVarC0.w();
        super.onPause();
    }

    @Override // com.adobe.dcmscan.a, androidx.fragment.app.f, android.app.Activity
    public final void onResume() {
        super.onResume();
        C0().t();
    }

    @Override // com.adobe.dcmscan.a, defpackage.of5, defpackage.sf5, android.app.Activity
    public final void onSaveInstanceState(Bundle bundle) {
        bundle.getClass();
        super.onSaveInstanceState(bundle);
    }

    @Override // com.adobe.dcmscan.a
    public final void g0(Activity activity, qii qiiVar) {
    }
}
