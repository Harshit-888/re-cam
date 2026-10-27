package defpackage;

import com.adobe.t5.pdf.Document;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;

/* JADX INFO: compiled from: CropMagnifier.kt */
/* JADX INFO: loaded from: classes2.dex */
public final class qj6 {
    /* JADX WARN: Multi-variable type inference failed */
    public static final void a(final kdb kdbVar, final long j, final float f, final txd txdVar, float f2, final long j2, final g5a g5aVar, fr5 fr5Var, final int i) {
        final float f3;
        float f4;
        int i2;
        final float f5;
        kdbVar.getClass();
        pr5 pr5VarJ = fr5Var.j(-1080719897);
        int i3 = i | (pr5VarJ.z(kdbVar) ? 4 : 2) | (pr5VarJ.g(j) ? 32 : 16) | (pr5VarJ.d(f) ? Document.PERMITTED_OPERATION_FORM_ENTRY : 128) | (pr5VarJ.O(txdVar) ? Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING : 1024) | 196608 | (pr5VarJ.g(j2) ? 1048576 : 524288) | (pr5VarJ.z(g5aVar) ? 8388608 : 4194304);
        if (pr5VarJ.q(i3 & 1, (4793491 & i3) != 4793490)) {
            pr5VarJ.z0();
            if ((i & 1) == 0 || pr5VarJ.g0()) {
                f4 = 2.5f;
            } else {
                pr5VarJ.G();
                f4 = f2;
            }
            pr5VarJ.Z();
            final long j3 = ((j1j) pr5VarJ.L(l1j.g)).G.a;
            boolean zO = pr5VarJ.O(kdbVar);
            Object objX = pr5VarJ.x();
            fr5.a.C0249a c0249a = fr5.a.a;
            if (zO || objX == c0249a) {
                objX = new z5k((((long) Float.floatToRawIntBits(kdbVar.getHeight())) & 4294967295L) | (((long) Float.floatToRawIntBits(kdbVar.getWidth())) << 32));
                pr5VarJ.r(objX);
            }
            final long j4 = ((z5k) objX).a;
            txd txdVarQ = i6k.q(txdVar, 80.0f);
            Object objX2 = pr5VarJ.x();
            if (objX2 == c0249a) {
                i2 = 0;
                objX2 = new nj6(i2);
                pr5VarJ.r(objX2);
            } else {
                i2 = 0;
            }
            txd txdVarA = vra.a(txdVarQ, (g5a) objX2);
            boolean z = ((((((((((3670016 & i3) ^ 1572864) <= 1048576 || !pr5VarJ.g(j2)) && (i3 & 1572864) != 1048576) ? i2 : 1) | ((29360128 & i3) == 8388608 ? 1 : i2)) | ((i3 & 896) == 256 ? 1 : i2)) | ((i3 & 112) != 32 ? i2 : 1)) | (pr5VarJ.g(j4) ? 1 : 0)) == true ? 1 : 0) | (pr5VarJ.z(kdbVar) ? 1 : 0) | (pr5VarJ.g(j3) ? 1 : 0);
            Object objX3 = pr5VarJ.x();
            if (z != 0 || objX3 == c0249a) {
                f5 = f4;
                g5a g5aVar2 = new g5a() { // from class: oj6
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj) throws Throwable {
                        long j5;
                        g5a g5aVar3 = g5aVar;
                        float f6 = f;
                        float f7 = f5;
                        long j6 = j;
                        long j7 = j4;
                        kdb kdbVar2 = kdbVar;
                        da8 da8Var = (da8) obj;
                        da8Var.getClass();
                        float fQ1 = da8Var.q1(40.0f);
                        da8.a0(da8Var, j2, fQ1, 0L, 0.0f, null, 124);
                        z64.b bVarU1 = da8Var.u1();
                        long jD = bVarU1.d();
                        bVarU1.a().c();
                        try {
                            a74 a74Var = bVarU1.a;
                            z64.b bVar = a74Var.a;
                            try {
                                a74Var.b(Float.intBitsToFloat((int) (bVar.d() >> 32)) / 2.0f, Float.intBitsToFloat((int) (bVar.d() & 4294967295L)) / 2.0f);
                                float fFloatValue = ((Number) g5aVar3.invoke(Float.valueOf(f6))).floatValue() * f7;
                                a74Var.c(fFloatValue, fFloatValue, 0L);
                                a74Var.e(0L, f6);
                                long jD2 = soe.d(j6, j7);
                                a74Var.b(-Float.intBitsToFloat((int) (jD2 >> 32)), -Float.intBitsToFloat((int) (jD2 & 4294967295L)));
                                da8.I0(da8Var, kdbVar2, 0.0f, null, 62);
                                bVarU1.a().k();
                                bVarU1.h(jD);
                                float fQ2 = da8Var.q1(3.0f);
                                float f8 = fQ2 / 2.0f;
                                yok yokVar = new yok(fQ2, 0.0f, 0, 0, null, 30);
                                long j8 = j3;
                                da8.a0(da8Var, j8, fQ1 - f8, 0L, 0.0f, yokVar, FacebookMediationAdapter.ERROR_MAPPING_NATIVE_ASSETS);
                                float f9 = -fQ1;
                                da8Var.x(j8, soe.b(da8Var.G1(), f9), soe.b(da8Var.G1(), fQ1), f8, (496 & 16) != 0 ? 0 : 0, (496 & 32) != 0 ? null : null);
                                da8Var.x(j8, soe.a(da8Var.G1(), f9), soe.a(da8Var.G1(), fQ1), f8, (496 & 16) != 0 ? 0 : 0, (496 & 32) != 0 ? null : null);
                                return d6m.a;
                            } catch (Throwable th) {
                                th = th;
                                j5 = jD;
                                hh3.c(bVarU1, j5);
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            j5 = jD;
                        }
                    }
                };
                pr5VarJ.r(g5aVar2);
                objX3 = g5aVar2;
            } else {
                f5 = f4;
            }
            e74.a(txdVarA, (g5a) objX3, pr5VarJ, i2);
            f3 = f5;
        } else {
            pr5VarJ.G();
            f3 = f2;
        }
        kzg kzgVarA0 = pr5VarJ.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a(j, f, txdVar, f3, j2, g5aVar, i) { // from class: pj6
                public final /* synthetic */ long b;
                public final /* synthetic */ float c;
                public final /* synthetic */ txd d;
                public final /* synthetic */ float e;
                public final /* synthetic */ long f;
                public final /* synthetic */ g5a g;

                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    int iB = dae.b(24577);
                    qj6.a(this.a, this.b, this.c, this.d, this.e, this.f, this.g, (fr5) obj, iB);
                    return d6m.a;
                }
            };
        }
    }
}
