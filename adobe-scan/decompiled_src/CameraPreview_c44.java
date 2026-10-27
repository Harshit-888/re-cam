package defpackage;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Size;
import android.view.Display;
import android.view.View;
import androidx.camera.core.impl.l;
import androidx.camera.core.impl.m;
import androidx.camera.core.impl.o;
import androidx.camera.core.impl.q;
import androidx.camera.core.impl.r;
import androidx.camera.core.impl.s;
import androidx.compose.ui.node.e;
import androidx.compose.ui.platform.AndroidCompositionLocals_androidKt;
import com.adobe.dcmscan.y;
import com.adobe.t5.pdf.Document;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: compiled from: CameraPreview.kt */
/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes.dex */
public final class c44 {
    public static final ExecutorService a;

    /* JADX INFO: compiled from: CameraPreview.kt */
    public static final class a implements zne, e6a {
        public final /* synthetic */ z34 a;

        public a(z34 z34Var) {
            this.a = z34Var;
        }

        @Override // defpackage.zne
        public final /* synthetic */ void a(Object obj) {
            this.a.invoke(obj);
        }

        @Override // defpackage.e6a
        public final v5a<?> b() {
            return this.a;
        }

        public final boolean equals(Object obj) {
            if ((obj instanceof zne) && (obj instanceof e6a)) {
                return this.a == ((e6a) obj).b();
            }
            return false;
        }

        public final int hashCode() {
            return this.a.hashCode();
        }
    }

    static {
        ExecutorService executorServiceNewSingleThreadExecutor = Executors.newSingleThreadExecutor();
        executorServiceNewSingleThreadExecutor.getClass();
        a = executorServiceNewSingleThreadExecutor;
    }

    public static final void a(final float f, final Size size, final jjk jjkVar, final y.a aVar, final jjk jjkVar2, final jjk jjkVar3, p34 p34Var, fr5 fr5Var, final int i) {
        pr5 pr5Var;
        final p34 p34Var2 = p34Var;
        pr5 pr5VarJ = fr5Var.j(506542253);
        int i2 = i | (pr5VarJ.d(f) ? 4 : 2) | (pr5VarJ.z(size) ? 32 : 16) | (pr5VarJ.O(jjkVar) ? Document.PERMITTED_OPERATION_FORM_ENTRY : 128) | (pr5VarJ.f(aVar == null ? -1 : aVar.ordinal()) ? Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING : Document.PERMITTED_OPERATION_PAGE_OPERATION) | (pr5VarJ.O(jjkVar3) ? 131072 : 65536) | (pr5VarJ.z(p34Var2) ? 1048576 : 524288);
        if (pr5VarJ.q(i2 & 1, (599187 & i2) != 599186)) {
            pr5VarJ.z0();
            if ((i & 1) != 0 && !pr5VarJ.g0()) {
                pr5VarJ.G();
            }
            pr5VarJ.Z();
            Object objX = pr5VarJ.x();
            fr5.a.C0051a c0051a = fr5.a.a;
            if (objX == c0051a) {
                objX = Integer.valueOf(y.a.CAMERA_FACING_FRONT == aVar ? 0 : 1);
                pr5VarJ.r(objX);
            }
            int iIntValue = ((Number) objX).intValue();
            txd txdVarA = rk2.a(qhc.b(txd.a.a, "CameraPreview"), f, false);
            boolean z = pr5VarJ.z(p34Var2);
            Object objX2 = pr5VarJ.x();
            if (z || objX2 == c0051a) {
                objX2 = new g5a() { // from class: q34
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj) {
                        jhc jhcVar = (jhc) obj;
                        jhcVar.getClass();
                        p34Var2.c.invoke(gw9.c(jhcVar).q(jhcVar, true));
                        return d6m.a;
                    }
                };
                pr5VarJ.r(objX2);
            }
            txd txdVarA2 = nqe.a(txdVarA, (g5a) objX2);
            bmd bmdVarD = si3.d(um1.a.a, false);
            int iHashCode = Long.hashCode(pr5VarJ.T);
            xif xifVarT = pr5VarJ.T();
            txd txdVarC = er5.c(pr5VarJ, txdVarA2);
            cr5.lo.getClass();
            e.a aVar2 = cr5.a.b;
            pr5VarJ.D();
            if (pr5VarJ.S) {
                pr5VarJ.A(aVar2);
            } else {
                pr5VarJ.p();
            }
            pi9.e(pr5VarJ, cr5.a.g, bmdVarD);
            pi9.e(pr5VarJ, cr5.a.f, xifVarT);
            pi9.c(pr5VarJ, Integer.valueOf(iHashCode), cr5.a.j);
            pi9.d(pr5VarJ, cr5.a.k);
            pi9.e(pr5VarJ, cr5.a.d, txdVarC);
            c(f, size, jjkVar, iIntValue, jjkVar2, jjkVar3, p34Var2, pr5VarJ, (3670016 & i2) | (i2 & 14) | 3072 | (i2 & 112) | (i2 & 896) | 24576 | (458752 & i2));
            pr5Var = pr5VarJ;
            p34Var2 = p34Var2;
            b(p34Var2, pr5Var, (i2 >> 18) & 14);
            pr5Var.Y(true);
        } else {
            pr5Var = pr5VarJ;
            pr5Var.G();
        }
        kzg kzgVarA0 = pr5Var.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a(f, size, jjkVar, aVar, jjkVar2, jjkVar3, p34Var2, i) { // from class: r34
                public final /* synthetic */ float a;
                public final /* synthetic */ Size b;
                public final /* synthetic */ jjk c;
                public final /* synthetic */ y.a d;
                public final /* synthetic */ jjk e;
                public final /* synthetic */ jjk f;
                public final /* synthetic */ p34 g;

                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    int iB = dae.b(24577);
                    c44.a(this.a, this.b, this.c, this.d, this.e, this.f, this.g, (fr5) obj, iB);
                    return d6m.a;
                }
            };
        }
    }

    public static final void b(final p34 p34Var, fr5 fr5Var, final int i) {
        int i2;
        pr5 pr5VarJ = fr5Var.j(-668922735);
        if ((i & 6) == 0) {
            i2 = (pr5VarJ.z(p34Var) ? 4 : 2) | i;
        } else {
            i2 = i;
        }
        if (pr5VarJ.q(i2 & 1, (i2 & 3) != 2)) {
            pr5VarJ.z0();
            if ((i & 1) != 0 && !pr5VarJ.g0()) {
                pr5VarJ.G();
            }
            pr5VarJ.Z();
            boolean z = pr5VarJ.z(p34Var);
            Object objX = pr5VarJ.x();
            if (z || objX == fr5.a.a) {
                objX = new g5a() { // from class: s34
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj) {
                        Context context = (Context) obj;
                        context.getClass();
                        View view = new View(context);
                        view.setOnTouchListener(p34Var.g);
                        return view;
                    }
                };
                pr5VarJ.r(objX);
            }
            i62.a(48, 4, pr5VarJ, (g5a) objX, null, i6k.d(txd.a.a, 1.0f));
        } else {
            pr5VarJ.G();
        }
        kzg kzgVarA0 = pr5VarJ.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a() { // from class: t34
                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    int iB = dae.b(i | 1);
                    c44.b(p34Var, (fr5) obj, iB);
                    return d6m.a;
                }
            };
        }
    }

    public static final void c(final float f, final Size size, final jjk jjkVar, final int i, final jjk jjkVar2, jjk jjkVar3, final p34 p34Var, fr5 fr5Var, final int i2) {
        int i3;
        final jjk jjkVar4;
        pr5 pr5Var;
        int i4;
        Object a44Var;
        Object[] objArr;
        pr5 pr5Var2;
        bzc bzcVarD;
        pr5 pr5VarJ = fr5Var.j(580202914);
        if ((i2 & 6) == 0) {
            i3 = (pr5VarJ.d(f) ? 4 : 2) | i2;
        } else {
            i3 = i2;
        }
        if ((i2 & 48) == 0) {
            i3 |= pr5VarJ.z(size) ? 32 : 16;
        }
        if ((i2 & 384) == 0) {
            i3 |= pr5VarJ.O(jjkVar) ? Document.PERMITTED_OPERATION_FORM_ENTRY : 128;
        }
        if ((i2 & 3072) == 0) {
            i3 |= pr5VarJ.f(i) ? Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING : Document.PERMITTED_OPERATION_PAGE_OPERATION;
        }
        if ((i2 & 24576) == 0) {
            i3 |= pr5VarJ.O(jjkVar2) ? 16384 : 8192;
        }
        if ((196608 & i2) == 0) {
            i3 |= pr5VarJ.O(jjkVar3) ? 131072 : 65536;
        }
        if ((1572864 & i2) == 0) {
            i3 |= pr5VarJ.z(p34Var) ? 1048576 : 524288;
        }
        if (pr5VarJ.q(i3 & 1, (599187 & i3) != 599186)) {
            pr5VarJ.z0();
            if ((i2 & 1) != 0 && !pr5VarJ.g0()) {
                pr5VarJ.G();
            }
            pr5VarJ.Z();
            final jvc jvcVar = (jvc) pr5VarJ.L(AndroidCompositionLocals_androidKt.getLocalLifecycleOwner());
            Context context = (Context) pr5VarJ.L(AndroidCompositionLocals_androidKt.b);
            Object objX = pr5VarJ.x();
            fr5.a.C0051a c0051a = fr5.a.a;
            if (objX == c0051a) {
                x9g x9gVar = x9g.b;
                context.getClass();
                duc ducVar = x9g.b.a;
                synchronized (ducVar.a) {
                    Object obj = r36.a;
                    ducVar.e = euc.a(Build.VERSION.SDK_INT >= 34 ? r36$a.b(context) : 0);
                    bzcVarD = ducVar.b;
                    if (bzcVarD == null) {
                        w54 w54Var = new w54(context, null);
                        wv4 wv4VarF = o6a.f(o6a.f(m6a.a(ducVar.c), new xtc(new wtc(w54Var)), eig.b()), new n6a(new ztc(new ytc(ducVar, w54Var, context))), eig.b());
                        ducVar.b = wv4VarF;
                        wv4VarF.r(new o6a.b(wv4VarF, new cuc(ducVar)), eig.b());
                        bzcVarD = o6a.d(wv4VarF);
                    }
                }
                i4 = 0;
                new w9g(i4);
                objX = o6a.f(bzcVarD, new n6a(new qx3()), eig.b());
                pr5VarJ.r(objX);
            } else {
                i4 = 0;
            }
            bzc bzcVar = (bzc) objX;
            boolean zO = pr5VarJ.O((Configuration) pr5VarJ.L(AndroidCompositionLocals_androidKt.a)) | pr5VarJ.O(context);
            Object objX2 = pr5VarJ.x();
            if (zO || objX2 == c0051a) {
                Display display = context.getDisplay();
                Integer numValueOf = display != null ? Integer.valueOf(display.getRotation()) : null;
                objX2 = Integer.valueOf(numValueOf != null ? numValueOf.intValue() : i4);
                pr5VarJ.r(objX2);
            }
            int iIntValue = ((Number) objX2).intValue();
            int i5 = (i3 & 14) == 4 ? 1 : i4;
            Object objX3 = pr5VarJ.x();
            if (i5 != 0 || objX3 == c0051a) {
                objX3 = Integer.valueOf(0.56f == f ? 1 : i4);
                pr5VarJ.r(objX3);
            }
            int iIntValue2 = ((Number) objX3).intValue();
            boolean zF = pr5VarJ.f(iIntValue2) | pr5VarJ.f(iIntValue);
            Object objX4 = pr5VarJ.x();
            Object obj2 = objX4;
            if (zF || objX4 == c0051a) {
                hyf.a aVar = new hyf.a();
                aVar.a.n(o.o, Integer.valueOf(iIntValue2 == -1 ? i4 : iIntValue2));
                q qVar = aVar.a;
                qVar.n(o.p, Integer.valueOf(iIntValue));
                qVar.n(o.q, Integer.valueOf(iIntValue));
                s sVar = new s(r.X(aVar.a));
                o.s(sVar);
                hyf hyfVar = new hyf(sVar);
                hyfVar.t = hyf.A;
                pr5VarJ.r(hyfVar);
                obj2 = hyfVar;
            }
            hyf hyfVar2 = (hyf) obj2;
            boolean zF2 = pr5VarJ.f(iIntValue2) | pr5VarJ.f(iIntValue);
            Object objX5 = pr5VarJ.x();
            if (zF2 || objX5 == c0051a) {
                pdb.b bVar = new pdb.b();
                bVar.a.n(m.Q, 0);
                bVar.a.n(o.o, Integer.valueOf(iIntValue2 == -1 ? 0 : iIntValue2));
                bVar.a.n(o.p, Integer.valueOf(iIntValue));
                objX5 = bVar.e();
                pr5VarJ.r(objX5);
            }
            final pdb pdbVar = (pdb) objX5;
            boolean zA = pr5VarJ.a(((Boolean) jjkVar.getValue()).booleanValue()) | pr5VarJ.f(iIntValue2) | pr5VarJ.f(iIntValue);
            Object objX6 = pr5VarJ.x();
            if (zA || objX6 == c0051a) {
                boolean zBooleanValue = ((Boolean) jjkVar.getValue()).booleanValue();
                g5a<androidx.camera.core.e, d6m> g5aVar = p34Var.b;
                ddb.c cVar = new ddb.c();
                q qVar2 = cVar.a;
                if (zBooleanValue) {
                    qVar2.n(l.Q, 1);
                    cVar.a.n(l.R, 20);
                } else {
                    qVar2.n(l.Q, 0);
                }
                if (size != null) {
                    cVar.a.n(o.s, size);
                } else {
                    if (iIntValue2 == -1) {
                        iIntValue2 = 0;
                    }
                    cVar.a.n(o.o, Integer.valueOf(iIntValue2));
                }
                cVar.a.n(o.p, Integer.valueOf(iIntValue));
                l lVar = new l(r.X(cVar.a));
                o.s(lVar);
                ddb ddbVar = new ddb(lVar);
                ExecutorService executorService = a;
                final y34 y34Var = new y34(g5aVar);
                synchronized (ddbVar.s) {
                    try {
                        fdb fdbVar = ddbVar.t;
                        if (fdbVar != null) {
                            ddb.a aVar2 = new ddb.a() { // from class: cdb
                                public final void a(qqj qqjVar) {
                                    y34Var.a(qqjVar);
                                }
                            };
                            synchronized (fdbVar.t) {
                                fdbVar.a = aVar2;
                                fdbVar.g = executorService;
                            }
                        }
                        if (ddbVar.v == null) {
                            ddbVar.s();
                        }
                        ddbVar.u = executorService;
                        ddbVar.v = y34Var;
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                pr5VarJ.r(ddbVar);
                objX6 = ddbVar;
            } else {
                iIntValue = iIntValue;
            }
            ddb ddbVar2 = (ddb) objX6;
            boolean z = (i3 & 7168) == 2048;
            Object objX7 = pr5VarJ.x();
            if (z || objX7 == c0051a) {
                LinkedHashSet linkedHashSet = new LinkedHashSet();
                isf.e("The specified lens facing is invalid.", i != -1);
                linkedHashSet.add(new jtc(i));
                objX7 = new s44(linkedHashSet);
                pr5VarJ.r(objX7);
            }
            s44 s44Var = (s44) objX7;
            s44Var.getClass();
            boolean z2 = pr5VarJ.z(p34Var) | pr5VarJ.z(pdbVar);
            Object objX8 = pr5VarJ.x();
            if (z2 || objX8 == c0051a) {
                objX8 = new g5a() { // from class: u34
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj3) {
                        ((c18) obj3).getClass();
                        p34 p34Var2 = p34Var;
                        p34Var2.a.invoke(pdbVar);
                        return new b44(p34Var2);
                    }
                };
                pr5VarJ.r(objX8);
            }
            oi8.a(pdbVar, (g5a) objX8, pr5VarJ);
            Object objX9 = pr5VarJ.x();
            Object obj3 = objX9;
            if (objX9 == c0051a) {
                q7g q7gVar = new q7g(context);
                q7gVar.setImplementationMode(q7g.c.COMPATIBLE);
                pr5VarJ.r(q7gVar);
                obj3 = q7gVar;
            }
            final q7g q7gVar2 = (q7g) obj3;
            boolean z3 = pr5VarJ.z(q7gVar2) | pr5VarJ.z(jvcVar) | pr5VarJ.z(p34Var);
            Object objX10 = pr5VarJ.x();
            if (z3 || objX10 == c0051a) {
                objX10 = new g5a() { // from class: v34
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj4) {
                        ((Context) obj4).getClass();
                        q7g q7gVar3 = q7gVar2;
                        q7gVar3.getPreviewStreamState().e(jvcVar, new c44.a(new z34(0, p34Var)));
                        return q7gVar3;
                    }
                };
                pr5VarJ.r(objX10);
            }
            g5a g5aVar2 = (g5a) objX10;
            txd txdVarD = i6k.d(txd.a.a, 1.0f);
            int i6 = 57344 & i3;
            boolean z4 = ((458752 & i3) == 131072) | (i6 == 16384);
            Object objX11 = pr5VarJ.x();
            if (z4 || objX11 == c0051a) {
                jjkVar4 = jjkVar3;
                objX11 = new g5a() { // from class: w34
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj4) {
                        q7g q7gVar3 = (q7g) obj4;
                        q7gVar3.getClass();
                        q7gVar3.setVisibility(((Boolean) jjkVar4.getValue()).booleanValue() ? 0 : 8);
                        q7gVar3.setKeepScreenOn(((Boolean) jjkVar2.getValue()).booleanValue());
                        return d6m.a;
                    }
                };
                pr5VarJ.r(objX11);
            } else {
                jjkVar4 = jjkVar3;
            }
            i62.a(48, 0, pr5VarJ, g5aVar2, (g5a) objX11, txdVarD);
            Object[] objArr2 = {bzcVar, jjkVar2.getValue(), Integer.valueOf(iIntValue), hyfVar2, pdbVar, ddbVar2};
            int i7 = iIntValue;
            boolean z5 = pr5VarJ.z(bzcVar) | (i6 == 16384) | pr5VarJ.z(hyfVar2) | pr5VarJ.z(q7gVar2) | pr5VarJ.z(jvcVar) | pr5VarJ.z(s44Var) | pr5VarJ.z(pdbVar) | pr5VarJ.z(ddbVar2) | pr5VarJ.z(p34Var) | pr5VarJ.f(i7);
            Object objX12 = pr5VarJ.x();
            if (z5 || objX12 == c0051a) {
                objArr = objArr2;
                pr5 pr5Var3 = pr5VarJ;
                a44Var = new a44(bzcVar, jjkVar2, hyfVar2, q7gVar2, jvcVar, s44Var, pdbVar, ddbVar2, p34Var, i7, null);
                pr5Var3.r(a44Var);
                pr5Var2 = pr5Var3;
            } else {
                objArr = objArr2;
                a44Var = objX12;
                pr5Var2 = pr5VarJ;
            }
            oi8.g(objArr, (u5a) a44Var, pr5Var2);
            pr5Var = pr5Var2;
        } else {
            jjkVar4 = jjkVar3;
            pr5 pr5Var4 = pr5VarJ;
            pr5Var4.G();
            pr5Var = pr5Var4;
        }
        kzg kzgVarA0 = pr5Var.a0();
        if (kzgVarA0 != null) {
            final jjk jjkVar5 = jjkVar4;
            kzgVarA0.d = new u5a() { // from class: x34
                @Override // defpackage.u5a
                public final Object invoke(Object obj4, Object obj5) {
                    ((Integer) obj5).getClass();
                    c44.c(f, size, jjkVar, i, jjkVar2, jjkVar5, p34Var, (fr5) obj4, dae.b(i2 | 1));
                    return d6m.a;
                }
            };
        }
    }
}
