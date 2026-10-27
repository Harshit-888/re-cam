package defpackage;

import androidx.compose.ui.input.pointer.PointerInputEventHandler;
import androidx.compose.ui.node.e;

/* JADX INFO: compiled from: CameraPreviewOverlay.kt */
/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes.dex */
public final class g44 {
    public static final void a(final u5a u5aVar, fr5 fr5Var, final int i) {
        pr5 pr5VarJ = fr5Var.j(1686532740);
        if (pr5VarJ.q(i & 1, (i & 3) != 2)) {
            txd txdVarD = i6k.d(qhc.b(txd.a.a, "CameraPreviewOverlay"), 1.0f);
            bmd bmdVarD = si3.d(um1.a.e, false);
            int iHashCode = Long.hashCode(pr5VarJ.T);
            xif xifVarT = pr5VarJ.T();
            txd txdVarC = er5.c(pr5VarJ, txdVarD);
            cr5.lo.getClass();
            e.a aVar = cr5.a.b;
            pr5VarJ.D();
            if (pr5VarJ.S) {
                pr5VarJ.A(aVar);
            } else {
                pr5VarJ.p();
            }
            pi9.e(pr5VarJ, cr5.a.g, bmdVarD);
            pi9.e(pr5VarJ, cr5.a.f, xifVarT);
            pi9.c(pr5VarJ, Integer.valueOf(iHashCode), cr5.a.j);
            pi9.d(pr5VarJ, cr5.a.k);
            pi9.e(pr5VarJ, cr5.a.d, txdVarC);
            u5aVar.invoke(pr5VarJ, 6);
            pr5VarJ.Y(true);
        } else {
            pr5VarJ.G();
        }
        kzg kzgVarA0 = pr5VarJ.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a(i, u5aVar) { // from class: d44
                public final /* synthetic */ u5a a;

                {
                    this.a = u5aVar;
                }

                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    int iB = dae.b(7);
                    g44.a(this.a, (fr5) obj, iB);
                    return d6m.a;
                }
            };
        }
    }

    public static final void b(final jjk<? extends f4g> jjkVar, final e5a<d6m> e5aVar, fr5 fr5Var, final int i) {
        kzg kzgVarA0;
        u5a<? super fr5, ? super Integer, d6m> u5aVar;
        boolean z;
        jjkVar.getClass();
        e5aVar.getClass();
        pr5 pr5VarJ = fr5Var.j(-1818607680);
        int i2 = (pr5VarJ.O(jjkVar) ? 4 : 2) | i | (pr5VarJ.z(e5aVar) ? 32 : 16);
        if (pr5VarJ.q(i2 & 1, (i2 & 19) != 18)) {
            f4g value = jjkVar.getValue();
            if (cub.b(value, f4g.d.b)) {
                kzgVarA0 = pr5VarJ.a0();
                if (kzgVarA0 == null) {
                    return;
                } else {
                    u5aVar = new u5a(e5aVar, i) { // from class: e44
                        public final /* synthetic */ e5a b;

                        @Override // defpackage.u5a
                        public final Object invoke(Object obj, Object obj2) {
                            ((Integer) obj2).getClass();
                            int iB = dae.b(1);
                            g44.b(this.a, this.b, (fr5) obj, iB);
                            return d6m.a;
                        }
                    };
                }
            } else {
                txd.a aVar = txd.a.a;
                txd txdVarD = i6k.d(aVar, 1.0f);
                tkk tkkVar = l1j.g;
                txd txdVarC = z23.c(txdVarD, ((j1j) pr5VarJ.L(tkkVar)).w.k, u0h.a);
                boolean zO = ((i2 & 112) == 32) | pr5VarJ.O(value);
                Object objX = pr5VarJ.x();
                if (zO || objX == fr5.a.a) {
                    objX = new a(value, e5aVar);
                    pr5VarJ.r(objX);
                }
                txd txdVarB = r0l.b(txdVarC, value, (PointerInputEventHandler) objX);
                db5 db5VarA = bb5.a(kj2.e, um1.a.n, pr5VarJ, 54);
                int iHashCode = Long.hashCode(pr5VarJ.T);
                xif xifVarT = pr5VarJ.T();
                txd txdVarC2 = er5.c(pr5VarJ, txdVarB);
                cr5.lo.getClass();
                e.a aVar2 = cr5.a.b;
                pr5VarJ.D();
                if (pr5VarJ.S) {
                    pr5VarJ.A(aVar2);
                } else {
                    pr5VarJ.p();
                }
                pi9.e(pr5VarJ, cr5.a.g, db5VarA);
                pi9.e(pr5VarJ, cr5.a.f, xifVarT);
                pi9.c(pr5VarJ, Integer.valueOf(iHashCode), cr5.a.j);
                pi9.d(pr5VarJ, cr5.a.k);
                pi9.e(pr5VarJ, cr5.a.d, txdVarC2);
                lil.b(ebc.d(pr5VarJ, value.a), r3f.g(aVar, 48.0f, 16.0f), ((j1j) pr5VarJ.L(tkkVar)).w.i, rkl.e(18), null, null, vp5.a(), 0L, new aal(3), 0L, 0, false, 0, 0, null, null, pr5VarJ, 3120, 0, 130480);
                if (value.equals(f4g.a.b)) {
                    pr5VarJ.P(-1838648912);
                    gek.a(pr5VarJ, i6k.h(aVar, 16.0f));
                    z = true;
                    hlh.a(ebc.d(pr5VarJ, 2132025588), false, ilh.Filled, null, null, e5aVar, null, null, null, pr5VarJ, ((i2 << 15) & 3670016) | 384, 954);
                    pr5VarJ.Y(false);
                } else {
                    z = true;
                    pr5VarJ.P(-1838375988);
                    pr5VarJ.Y(false);
                }
                pr5VarJ.Y(z);
            }
            kzgVarA0.d = u5aVar;
        }
        pr5VarJ.G();
        kzgVarA0 = pr5VarJ.a0();
        if (kzgVarA0 != null) {
            u5aVar = new u5a(e5aVar, i) { // from class: f44
                public final /* synthetic */ e5a b;

                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    int iB = dae.b(1);
                    g44.b(this.a, this.b, (fr5) obj, iB);
                    return d6m.a;
                }
            };
            kzgVarA0.d = u5aVar;
        }
    }
}
