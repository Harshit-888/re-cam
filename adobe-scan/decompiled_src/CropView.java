package defpackage;

import android.view.KeyEvent;
import androidx.compose.ui.input.pointer.PointerInputEventHandler;
import androidx.compose.ui.node.e;
import com.adobe.t5.pdf.Document;
import com.inmobi.commons.core.configs.AdConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: compiled from: CropView.kt */
/* JADX INFO: loaded from: classes2.dex */
public final class ok6 {

    /* JADX INFO: compiled from: CropView.kt */
    public static final class a implements PointerInputEventHandler {
        public final /* synthetic */ ak6 a;
        public final /* synthetic */ z4e b;
        public final /* synthetic */ long c;
        public final /* synthetic */ long d;

        public a(ak6 ak6Var, z4e z4eVar, long j, long j2) {
            this.a = ak6Var;
            this.b = z4eVar;
            this.c = j;
            this.d = j2;
        }

        @Override // androidx.compose.ui.input.pointer.PointerInputEventHandler
        public final Object invoke(final ypf ypfVar, u36<? super d6m> u36Var) {
            final z4e z4eVar = this.b;
            ko4 ko4Var = new ko4(1, z4eVar);
            final long j = this.c;
            final long j2 = this.d;
            Object objB = this.a.b(ypfVar, ko4Var, new g5a() { // from class: nk6
                /* JADX WARN: Multi-variable type inference failed */
                @Override // defpackage.g5a
                public final Object invoke(Object obj) {
                    tk6 tk6Var = (tk6) z4eVar.getValue();
                    long jI = ioe.i(soe.c(ioe.h(ioe.h(((ioe) obj).a, j), j2), -tk6Var.a), wb9.b(tk6Var.d));
                    List<ioe> list = tk6Var.c;
                    float fQ1 = ypfVar.q1(28.0f);
                    float f = fQ1 * fQ1;
                    int i = 0;
                    for (Object obj2 : list) {
                        int i2 = i + 1;
                        if (i < 0) {
                            r75.n();
                            throw null;
                        }
                        long j3 = ((ioe) obj2).a;
                        int i3 = i2 % 4;
                        if (ioe.e(ioe.h(jI, ioe.j(ioe.i(j3, list.get(i3).a), 0.5f))) < f) {
                            return r75.h(Integer.valueOf(i), Integer.valueOf(i3));
                        }
                        i = i2;
                    }
                    return el8.a;
                }
            }, u36Var);
            return objB == u86.COROUTINE_SUSPENDED ? objB : d6m.a;
        }
    }

    /* JADX INFO: compiled from: CropView.kt */
    public static final class b implements g5a<zac, Boolean> {
        public final /* synthetic */ ak6 a;
        public final /* synthetic */ z4e b;
        public final /* synthetic */ int c;

        public b(ak6 ak6Var, z4e z4eVar, int i) {
            this.a = ak6Var;
            this.b = z4eVar;
            this.c = i;
        }

        /* JADX WARN: Code duplicated, block: B:21:0x007b  */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // defpackage.g5a
        public final Boolean invoke(zac zacVar) {
            long j;
            boolean z;
            long jFloatToRawIntBits;
            int iFloatToRawIntBits;
            KeyEvent keyEvent = zacVar.a;
            keyEvent.getClass();
            if (bbc.b(keyEvent) != 2) {
                j = 0;
            } else {
                float f = keyEvent.isShiftPressed() ? 50.0f : 10.0f;
                long jC = lje.c(keyEvent.getKeyCode());
                if (tac.a(jC, tac.d)) {
                    jFloatToRawIntBits = Float.floatToRawIntBits(0.0f);
                    iFloatToRawIntBits = Float.floatToRawIntBits(-f);
                } else if (tac.a(jC, tac.g)) {
                    jFloatToRawIntBits = Float.floatToRawIntBits(f);
                    iFloatToRawIntBits = Float.floatToRawIntBits(0.0f);
                } else if (tac.a(jC, tac.e)) {
                    jFloatToRawIntBits = Float.floatToRawIntBits(0.0f);
                    iFloatToRawIntBits = Float.floatToRawIntBits(f);
                } else if (tac.a(jC, tac.f)) {
                    jFloatToRawIntBits = Float.floatToRawIntBits(-f);
                    iFloatToRawIntBits = Float.floatToRawIntBits(0.0f);
                } else {
                    j = 0;
                }
                j = (jFloatToRawIntBits << 32) | (((long) iFloatToRawIntBits) & 4294967295L);
            }
            if (ioe.c(j, 0L)) {
                z = false;
            } else {
                tk6 tk6Var = (tk6) this.b.getValue();
                List<Integer> listG = r75.g(Integer.valueOf(this.c));
                ak6 ak6Var = this.a;
                ak6Var.getClass();
                tk6Var.getClass();
                qp5 qp5VarC = tk6Var.b.c(ak6Var.a(tk6Var.c, soe.c(j, -tk6Var.a), tk6Var.d, listG), listG);
                tj6 tj6Var = ak6Var.b;
                if (tj6Var.a.invoke(qp5VarC).booleanValue()) {
                    tj6Var.b.invoke(qp5VarC);
                }
                z = true;
            }
            return Boolean.valueOf(z);
        }
    }

    /* JADX INFO: compiled from: CropView.kt */
    public static final class c implements PointerInputEventHandler {
        public final /* synthetic */ ak6 a;
        public final /* synthetic */ z4e b;
        public final /* synthetic */ int c;

        public c(ak6 ak6Var, z4e z4eVar, int i) {
            this.a = ak6Var;
            this.b = z4eVar;
            this.c = i;
        }

        @Override // androidx.compose.ui.input.pointer.PointerInputEventHandler
        public final Object invoke(ypf ypfVar, u36<? super d6m> u36Var) {
            pk6 pk6Var = new pk6(0, this.b);
            final int i = this.c;
            Object objB = this.a.b(ypfVar, pk6Var, new g5a() { // from class: qk6
                @Override // defpackage.g5a
                public final Object invoke(Object obj) {
                    return r75.g(Integer.valueOf(i));
                }
            }, u36Var);
            return objB == u86.COROUTINE_SUSPENDED ? objB : d6m.a;
        }
    }

    /* JADX WARN: Code duplicated, block: B:103:0x02f7  */
    /* JADX WARN: Code duplicated, block: B:104:0x02fb  */
    /* JADX WARN: Code duplicated, block: B:108:0x032c  */
    /* JADX WARN: Code duplicated, block: B:110:0x0334  */
    /* JADX WARN: Code duplicated, block: B:112:0x033e  */
    /* JADX WARN: Code duplicated, block: B:113:0x0357  */
    /* JADX WARN: Code duplicated, block: B:116:0x0395  */
    /* JADX WARN: Code duplicated, block: B:117:0x0398  */
    /* JADX WARN: Code duplicated, block: B:121:0x03ae  */
    /* JADX WARN: Code duplicated, block: B:129:0x03f8  */
    /* JADX WARN: Code duplicated, block: B:133:0x041f  */
    /* JADX WARN: Code duplicated, block: B:137:0x0443  */
    /* JADX WARN: Code duplicated, block: B:141:0x046e  */
    /* JADX WARN: Code duplicated, block: B:153:0x049e A[SYNTHETIC] */
    public static final void a(final qp5 qp5Var, final float f, final n5i n5iVar, final float f2, final int i, final ak6 ak6Var, fr5 fr5Var, final int i2) {
        pr5 pr5Var;
        Object z5kVar;
        Object obj;
        Object aVar;
        long j;
        long j2;
        char c2;
        z4e z4eVar;
        pr5 pr5Var2;
        fr5.a.C0249a c0249a;
        fr5.a.C0249a c0249a2;
        final long j3;
        z4e z4eVar2;
        long j4;
        txd.a aVar2;
        z4e z4eVar3;
        e.a aVar3;
        Iterator it;
        final int i3;
        Object next;
        int i4;
        final long j5;
        String strC;
        boolean z;
        boolean zG;
        Object obj2;
        long j6;
        float f3;
        long j7;
        long j8;
        boolean zO;
        boolean z2;
        Object obj3;
        z4e z4eVar4;
        boolean z3;
        Object obj4;
        boolean z4;
        Object obj5;
        final long j9;
        boolean z5;
        Object objX;
        final ak6 ak6Var2 = ak6Var;
        pr5 pr5VarJ = fr5Var.j(-68633525);
        int i5 = i2 | (pr5VarJ.z(qp5Var) ? 4 : 2) | (pr5VarJ.d(f) ? 32 : 16) | (pr5VarJ.O(n5iVar) ? 256 : 128) | (pr5VarJ.d(f2) ? 2048 : 1024) | (pr5VarJ.f(i) ? 16384 : 8192) | (pr5VarJ.z(ak6Var2) ? 131072 : 65536);
        if (!pr5VarJ.q(i5 & 1, (74899 & i5) != 74898)) {
            pr5 pr5Var3 = pr5VarJ;
            pr5Var3.G();
            pr5Var = pr5Var3;
        } else if (cub.b(qp5Var, qp5.h)) {
            pr5 pr5Var4 = pr5VarJ;
            pr5Var4.P(-248087529);
            pr5Var4.Y(false);
            pr5Var = pr5Var4;
        } else {
            pr5VarJ.P(-256388647);
            int i6 = i5 & 7168;
            boolean z6 = ((i5 & 896) == 256) | (i6 == 2048);
            Object objX2 = pr5VarJ.x();
            fr5.a.C0249a c0249a3 = fr5.a.a;
            if (z6 || objX2 == c0249a3) {
                z5kVar = new z5k(z5k.g(n5iVar.b, n5iVar.a(f2, true)));
                pr5VarJ.r(z5kVar);
            } else {
                z5kVar = objX2;
            }
            long j10 = ((z5k) z5kVar).a;
            tkk tkkVar = l1j.g;
            final long j11 = ((j1j) pr5VarJ.L(tkkVar)).G.a;
            long j12 = ((j1j) pr5VarJ.L(tkkVar)).G.b;
            boolean zO2 = pr5VarJ.O(ak6Var2);
            Object objX3 = pr5VarJ.x();
            Object obj6 = objX3;
            if (zO2 || objX3 == c0249a3) {
                ioe ioeVar = new ioe(ak6Var2.c);
                pr5VarJ.r(ioeVar);
                obj6 = ioeVar;
            }
            long j13 = ((ioe) obj6).a;
            boolean zG2 = pr5VarJ.g(j10);
            Object objX4 = pr5VarJ.x();
            if (zG2 || objX4 == c0249a3) {
                ioe ioeVar2 = new ioe(wb9.b(j10));
                pr5VarJ.r(ioeVar2);
                objX4 = ioeVar2;
            }
            final long j14 = ((ioe) objX4).a;
            boolean zO3 = pr5VarJ.O(qp5Var) | pr5VarJ.g(j10);
            int i7 = i6;
            Object objX5 = pr5VarJ.x();
            if (zO3 || objX5 == c0249a3) {
                ArrayList arrayList = new ArrayList(s75.o(qp5Var, 10));
                int i8 = 0;
                while (true) {
                    if (!(i8 < 4)) {
                        break;
                    }
                    arrayList.add(new ioe(soe.d(new ioe(qp5Var.b(i8)).a, j10)));
                    i8++;
                }
                pr5VarJ.r(arrayList);
                obj = arrayList;
            } else {
                obj = objX5;
            }
            final List list = (List) obj;
            z4e z4eVarJ = nck.j(new tk6(f2, qp5Var, list, j10), pr5VarJ);
            sg7 sg7Var = (sg7) pr5VarJ.L(ys5.h);
            boolean zO4 = pr5VarJ.O(sg7Var);
            Object objX6 = pr5VarJ.x();
            Object obj7 = objX6;
            if (zO4 || objX6 == c0249a3) {
                Float fValueOf = Float.valueOf(-(sg7Var.q1(16.0f) + (sg7Var.q1(21.0f) / 2.0f)));
                pr5VarJ.r(fValueOf);
                obj7 = fValueOf;
            }
            float fFloatValue = ((Number) obj7).floatValue();
            boolean zO5 = pr5VarJ.O(sg7Var) | ((i5 & 112) == 32);
            Object objX7 = pr5VarJ.x();
            if (zO5 || objX7 == c0249a3) {
                ioe ioeVar3 = new ioe((((long) Float.floatToRawIntBits(sg7Var.q1(f))) << 32) | (((long) Float.floatToRawIntBits(sg7Var.q1(f))) & 4294967295L));
                pr5VarJ.r(ioeVar3);
                objX7 = ioeVar3;
            }
            long j15 = ((ioe) objX7).a;
            txd.a aVar4 = txd.a.a;
            float f4 = fFloatValue;
            txd txdVarD = i6k.d(aVar4, 1.0f);
            boolean z7 = pr5VarJ.z(ak6Var2) | pr5VarJ.O(z4eVarJ) | pr5VarJ.g(j15) | pr5VarJ.g(j13);
            Object objX8 = pr5VarJ.x();
            if (z7 || objX8 == c0249a3) {
                pr5 pr5Var5 = pr5VarJ;
                j = j13;
                j2 = j15;
                c2 = 2048;
                z4eVar = z4eVarJ;
                aVar = new a(ak6Var2, z4eVar, j2, j);
                pr5Var5.r(aVar);
                pr5Var2 = pr5Var5;
            } else {
                aVar = objX8;
                pr5Var2 = pr5VarJ;
                j = j13;
                j2 = j15;
                c2 = 2048;
                z4eVar = z4eVarJ;
            }
            txd txdVarB = r0l.b(txdVarD, ak6Var2, (PointerInputEventHandler) aVar);
            final long j16 = j10;
            boolean zG3 = pr5Var2.g(j11) | pr5Var2.g(j) | pr5Var2.g(j2) | (i7 == c2) | pr5Var2.g(j14) | pr5Var2.z(ak6Var2) | pr5Var2.z(list) | pr5Var2.g(j10) | pr5Var2.g(j12);
            Object objX9 = pr5Var2.x();
            if (zG3) {
                c0249a = c0249a3;
            } else {
                c0249a = c0249a3;
                if (objX9 != c0249a) {
                    j3 = j12;
                    c0249a2 = c0249a;
                    j4 = j14;
                    z4eVar2 = z4eVar;
                }
                e74.a(txdVarB, (g5a) objX9, pr5Var2, 0);
                aVar2 = aVar4;
                txd txdVarF = r3f.f(i6k.d(aVar2, 1.0f), f);
                bmd bmdVarD = si3.d(um1.a.a, false);
                z4eVar3 = z4eVar2;
                int iHashCode = Long.hashCode(pr5Var2.T);
                xif xifVarT = pr5Var2.T();
                txd txdVarC = er5.c(pr5Var2, txdVarF);
                cr5.lo.getClass();
                aVar3 = cr5.a.b;
                pr5Var2.D();
                if (pr5Var2.S) {
                    pr5Var2.A(aVar3);
                } else {
                    pr5Var2.p();
                }
                pi9.e(pr5Var2, cr5.a.g, bmdVarD);
                pi9.e(pr5Var2, cr5.a.f, xifVarT);
                pi9.c(pr5Var2, Integer.valueOf(iHashCode), cr5.a.j);
                pi9.d(pr5Var2, cr5.a.k);
                pi9.e(pr5Var2, cr5.a.d, txdVarC);
                pr5Var2.P(334677032);
                it = list.iterator();
                i3 = 0;
                while (it.hasNext()) {
                    next = it.next();
                    i4 = i3 + 1;
                    if (i3 >= 0) {
                        r75.n();
                        throw null;
                    }
                    txd.a aVar5 = aVar2;
                    j5 = ((ioe) next).a;
                    if (i != 0) {
                        pr5Var2.P(913693966);
                        strC = ebc.c(i, new Object[]{Integer.valueOf(i4)}, pr5Var2);
                        pr5Var2.Y(false);
                    } else {
                        pr5Var2.P(913797382);
                        pr5Var2.Y(false);
                        strC = null;
                    }
                    txd txdVarQ = i6k.q(aVar5, 53.0f);
                    boolean zD = pr5Var2.d(f4) | pr5Var2.z(ak6Var2) | pr5Var2.f(i3) | pr5Var2.g(j16) | pr5Var2.g(j5) | pr5Var2.g(j4);
                    if (i7 == 2048) {
                        z = true;
                    } else {
                        z = false;
                    }
                    zG = zD | z | pr5Var2.g(j);
                    Object objX10 = pr5Var2.x();
                    int i9 = i7;
                    fr5.a.C0249a c0249a4 = c0249a2;
                    if (!zG || objX10 == c0249a4) {
                        final long j17 = j;
                        final long j18 = j4;
                        j6 = j11;
                        final float f5 = f4;
                        obj2 = new g5a() { // from class: jk6
                            /* JADX WARN: Multi-variable type inference failed */
                            @Override // defpackage.g5a
                            public final Object invoke(Object obj8) {
                                ((sg7) obj8).getClass();
                                float f6 = f5;
                                long jFloatToRawIntBits = (((long) Float.floatToRawIntBits(f6)) << 32) | (((long) Float.floatToRawIntBits(f6)) & 4294967295L);
                                ak6 ak6Var3 = ak6Var2;
                                long jI = ioe.i(soe.c(ioe.h(ioe.i(j5, ((List) ak6Var3.d.getValue()).contains(Integer.valueOf(i3)) ? soe.d(((ioe) ak6Var3.f.getValue()).a, j16) : 0L), j18), f2), ioe.i(j17, jFloatToRawIntBits));
                                return new irb((((long) tld.b(Float.intBitsToFloat((int) (jI >> 32)))) << 32) | (((long) tld.b(Float.intBitsToFloat((int) (jI & 4294967295L)))) & 4294967295L));
                            }
                        };
                        f3 = f5;
                        j7 = j18;
                        j8 = j17;
                        pr5Var2.r(obj2);
                    } else {
                        obj2 = objX10;
                        j7 = j4;
                        j6 = j11;
                        f3 = f4;
                        j8 = j;
                    }
                    txd txdVarA = i55.a(dmc.a(txdVarQ, (g5a) obj2), rlh.a);
                    zO = pr5Var2.O(strC);
                    Object objX11 = pr5Var2.x();
                    if (!zO || objX11 == c0249a4) {
                        z2 = false;
                        kk6 kk6Var = new kk6(false ? 1 : 0, strC);
                        pr5Var2.r(kk6Var);
                        obj3 = kk6Var;
                    } else {
                        z2 = false;
                        obj3 = objX11;
                    }
                    txd txdVarB2 = dhj.b(txdVarA, z2, (g5a) obj3);
                    z4eVar4 = z4eVar3;
                    z3 = pr5Var2.z(ak6Var2) | pr5Var2.O(z4eVar4) | pr5Var2.f(i3);
                    Object objX12 = pr5Var2.x();
                    obj4 = objX12;
                    if (z3 || objX12 == c0249a4) {
                        b bVar = new b(ak6Var2, z4eVar4, i3);
                        pr5Var2.r(bVar);
                        obj4 = bVar;
                    }
                    txd txdVarA2 = ebc.a(txdVarB2, (g5a) obj4);
                    z4 = pr5Var2.z(ak6Var2) | pr5Var2.O(z4eVar4) | pr5Var2.f(i3);
                    Object objX13 = pr5Var2.x();
                    obj5 = objX13;
                    if (z4 || objX13 == c0249a4) {
                        c cVar = new c(ak6Var2, z4eVar4, i3);
                        pr5Var2.r(cVar);
                        obj5 = cVar;
                    }
                    txd txdVarB3 = r0l.b(txdVarA2, ak6Var2, (PointerInputEventHandler) obj5);
                    j9 = j3;
                    z5 = pr5Var2.z(ak6Var2) | pr5Var2.f(i3) | pr5Var2.g(j6) | pr5Var2.g(j9);
                    objX = pr5Var2.x();
                    if (z5 || objX == c0249a4) {
                        final ak6 ak6Var3 = ak6Var2;
                        final int i10 = i3;
                        final long j19 = j6;
                        objX = new g5a() { // from class: lk6
                            @Override // defpackage.g5a
                            public final Object invoke(Object obj8) {
                                da8 da8Var = (da8) obj8;
                                da8Var.getClass();
                                float fQ1 = da8Var.q1(9.0f);
                                yok yokVar = new yok(da8Var.q1(3.0f), 0.0f, 0, 0, null, 30);
                                ((List) ak6Var3.d.getValue()).contains(Integer.valueOf(i10));
                                da8.a0(da8Var, j9, fQ1, da8Var.G1(), 0.0f, null, AdConfig.DEFAULT_PING_V2_INTERVAL_NORMAL);
                                da8.a0(da8Var, j19, fQ1, da8Var.G1(), 0.0f, yokVar, 104);
                                return d6m.a;
                            }
                        };
                        pr5Var2.r(objX);
                    }
                    e74.a(txdVarB3, (g5a) objX, pr5Var2, 0);
                    ak6Var2 = ak6Var;
                    c0249a2 = c0249a4;
                    f4 = f3;
                    i3 = i4;
                    z4eVar3 = z4eVar4;
                    aVar2 = aVar5;
                    it = it;
                    i7 = i9;
                    long j20 = j6;
                    i = i;
                    j4 = j7;
                    j3 = j9;
                    j16 = j16;
                    j = j8;
                    j11 = j20;
                }
                pr5Var2.Y(false);
                pr5Var2.P(1788612822);
                pr5Var2.Y(false);
                pr5Var2.Y(true);
                pr5Var2.Y(false);
                pr5Var = pr5Var2;
            }
            final long j21 = j;
            j3 = j12;
            z4e z4eVar5 = z4eVar;
            final long j22 = j2;
            g5a g5aVar = new g5a() { // from class: ik6
                /* JADX WARN: Code duplicated, block: B:44:0x016c A[Catch: all -> 0x0246, TryCatch #2 {all -> 0x0246, blocks: (B:28:0x013f, B:30:0x014c, B:42:0x0167, B:47:0x0175, B:44:0x016c, B:45:0x016f, B:46:0x0172), top: B:70:0x013f }] */
                /* JADX WARN: Code duplicated, block: B:45:0x016f A[Catch: all -> 0x0246, TryCatch #2 {all -> 0x0246, blocks: (B:28:0x013f, B:30:0x014c, B:42:0x0167, B:47:0x0175, B:44:0x016c, B:45:0x016f, B:46:0x0172), top: B:70:0x013f }] */
                /* JADX WARN: Code duplicated, block: B:46:0x0172 A[Catch: all -> 0x0246, TryCatch #2 {all -> 0x0246, blocks: (B:28:0x013f, B:30:0x014c, B:42:0x0167, B:47:0x0175, B:44:0x016c, B:45:0x016f, B:46:0x0172), top: B:70:0x013f }] */
                /* JADX WARN: Multi-variable type inference failed */
                @Override // defpackage.g5a
                public final Object invoke(Object obj8) throws Throwable {
                    z64.b bVar2;
                    long j23;
                    long j24;
                    thm thmVar;
                    long j25 = j21;
                    long j26 = j22;
                    float f6 = f2;
                    long j27 = j14;
                    ak6 ak6Var4 = ak6Var2;
                    List list2 = list;
                    long j28 = j16;
                    long j29 = j11;
                    long j30 = j3;
                    da8 da8Var = (da8) obj8;
                    da8Var.getClass();
                    z64.b bVarU1 = da8Var.u1();
                    long jD = bVarU1.d();
                    bVarU1.a().c();
                    long j31 = j30;
                    try {
                        a74 a74Var = bVarU1.a;
                        try {
                            a74Var.b(Float.intBitsToFloat((int) (j25 >> 32)) + Float.intBitsToFloat((int) (j26 >> 32)), Float.intBitsToFloat((int) (j25 & 4294967295L)) + Float.intBitsToFloat((int) (j26 & 4294967295L)));
                            a74Var.e(0L, f6);
                            a74Var.b(-Float.intBitsToFloat((int) (j27 >> 32)), -Float.intBitsToFloat((int) (j27 & 4294967295L)));
                            float fQ1 = da8Var.q1(9.0f);
                            float fQ2 = da8Var.q1(25.0f);
                            long jFloatToRawIntBits = (((long) Float.floatToRawIntBits(fQ2)) << 32) | (((long) Float.floatToRawIntBits(da8Var.q1(10.0f))) & 4294967295L);
                            float fQ3 = da8Var.q1(3.0f);
                            yok yokVar = new yok(fQ3, 0.0f, 0, 0, null, 30);
                            hcf hcfVar = ak6Var4.d;
                            hcf hcfVar2 = ak6Var4.f;
                            long j32 = jFloatToRawIntBits;
                            ArrayList arrayList2 = new ArrayList(s75.o(list2, 10));
                            Iterator it2 = list2.iterator();
                            int i11 = 0;
                            while (it2.hasNext()) {
                                Object next2 = it2.next();
                                int i12 = i11 + 1;
                                if (i11 < 0) {
                                    r75.n();
                                    throw null;
                                }
                                ioe ioeVar4 = (ioe) next2;
                                Iterator it3 = it2;
                                yok yokVar2 = yokVar;
                                arrayList2.add(new ioe(ioe.i(ioeVar4.a, ((List) hcfVar.getValue()).contains(Integer.valueOf(i11)) ? soe.d(((ioe) hcfVar2.getValue()).a, j28) : 0L)));
                                hcfVar = hcfVar;
                                it2 = it3;
                                i11 = i12;
                                yokVar = yokVar2;
                            }
                            yok yokVar3 = yokVar;
                            qp5 qp5VarA = rp5.a(arrayList2);
                            wj6 wj6Var = (wj6) qp5VarA.e.getValue();
                            int i13 = 0;
                            int i14 = 0;
                            while (true) {
                                if (!(i14 < 4)) {
                                    hh3.c(bVarU1, jD);
                                    return d6m.a;
                                }
                                int i15 = i14 + 1;
                                try {
                                    ioe ioeVar5 = new ioe(qp5VarA.b(i14));
                                    int i16 = i13 + 1;
                                    if (i13 < 0) {
                                        r75.n();
                                        throw null;
                                    }
                                    long j33 = ioeVar5.a;
                                    wj6Var.getClass();
                                    int i17 = i13 % 4;
                                    if (i17 == -3) {
                                        thmVar = wj6Var.b;
                                    } else if (i17 == -2) {
                                        thmVar = wj6Var.c;
                                    } else if (i17 == -1) {
                                        thmVar = wj6Var.d;
                                    } else if (i17 == 1) {
                                        thmVar = wj6Var.b;
                                    } else if (i17 == 2) {
                                        thmVar = wj6Var.c;
                                    } else if (i17 != 3) {
                                        thmVar = wj6Var.a;
                                    } else {
                                        thmVar = wj6Var.d;
                                    }
                                    qp5 qp5Var2 = qp5VarA;
                                    wj6 wj6Var2 = wj6Var;
                                    long j34 = thmVar.a;
                                    long j35 = thmVar.b;
                                    da8 da8Var2 = da8Var;
                                    long jI = ioe.i(j33, ioe.b(j34, 2.0f));
                                    j24 = jD;
                                    try {
                                        long j36 = ioe.j(j35, fQ1);
                                        long jB = ioe.b(ioe.j(j35, fQ2), 2.0f);
                                        long jI2 = ioe.i(j33, j36);
                                        long jH = ioe.h(jI, jB);
                                        da8Var = da8Var2;
                                        float f7 = fQ2;
                                        da8Var.x(j29, jI2, jH, fQ3, (496 & 16) != 0 ? 0 : 0, (496 & 32) != 0 ? null : null);
                                        da8Var.x(j29, ioe.i(jI, jB), ioe.h(ioe.i(j33, j34), j36), fQ3, (496 & 16) != 0 ? 0 : 0, (496 & 32) != 0 ? null : null);
                                        float f8 = fQ3;
                                        float f9 = thmVar.c;
                                        z64.b bVarU2 = da8Var.u1();
                                        long jD2 = bVarU2.d();
                                        bVarU2.a().c();
                                        try {
                                            bVarU2.a.e(jI, f9);
                                            long jH2 = ioe.h(jI, wb9.b(j32));
                                            long j37 = j31;
                                            long j38 = j32;
                                            da8.B0(da8Var, j37, jH2, j38, 0.0f, null, null, AdConfig.DEFAULT_PING_V2_INTERVAL_NORMAL);
                                            j31 = j37;
                                            yok yokVar4 = yokVar3;
                                            da8.B0(da8Var, j29, jH2, j38, 0.0f, yokVar4, null, 104);
                                            bVarU2.a().k();
                                            bVarU2.h(jD2);
                                            fQ3 = f8;
                                            yokVar3 = yokVar4;
                                            fQ2 = f7;
                                            j32 = j38;
                                            i13 = i16;
                                            i14 = i15;
                                            qp5VarA = qp5Var2;
                                            wj6Var = wj6Var2;
                                            jD = j24;
                                        } catch (Throwable th) {
                                            bVarU2.a().k();
                                            bVarU2.h(jD2);
                                            throw th;
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        bVar2 = bVarU1;
                                        j23 = j24;
                                        hh3.c(bVar2, j23);
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    j24 = jD;
                                }
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            bVar2 = bVarU1;
                            j23 = jD;
                            hh3.c(bVar2, j23);
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        bVar2 = bVarU1;
                    }
                }
            };
            z4eVar2 = z4eVar5;
            c0249a2 = c0249a;
            j4 = j14;
            j = j21;
            ak6Var2 = ak6Var2;
            pr5Var2.r(g5aVar);
            objX9 = g5aVar;
            e74.a(txdVarB, (g5a) objX9, pr5Var2, 0);
            aVar2 = aVar4;
            txd txdVarF2 = r3f.f(i6k.d(aVar2, 1.0f), f);
            bmd bmdVarD2 = si3.d(um1.a.a, false);
            z4eVar3 = z4eVar2;
            int iHashCode2 = Long.hashCode(pr5Var2.T);
            xif xifVarT2 = pr5Var2.T();
            txd txdVarC2 = er5.c(pr5Var2, txdVarF2);
            cr5.lo.getClass();
            aVar3 = cr5.a.b;
            pr5Var2.D();
            if (pr5Var2.S) {
                pr5Var2.A(aVar3);
            } else {
                pr5Var2.p();
            }
            pi9.e(pr5Var2, cr5.a.g, bmdVarD2);
            pi9.e(pr5Var2, cr5.a.f, xifVarT2);
            pi9.c(pr5Var2, Integer.valueOf(iHashCode2), cr5.a.j);
            pi9.d(pr5Var2, cr5.a.k);
            pi9.e(pr5Var2, cr5.a.d, txdVarC2);
            pr5Var2.P(334677032);
            it = list.iterator();
            i3 = 0;
            while (it.hasNext()) {
                next = it.next();
                i4 = i3 + 1;
                if (i3 >= 0) {
                    r75.n();
                    throw null;
                }
                txd.a aVar6 = aVar2;
                j5 = ((ioe) next).a;
                if (i != 0) {
                    pr5Var2.P(913693966);
                    strC = ebc.c(i, new Object[]{Integer.valueOf(i4)}, pr5Var2);
                    pr5Var2.Y(false);
                } else {
                    pr5Var2.P(913797382);
                    pr5Var2.Y(false);
                    strC = null;
                }
                txd txdVarQ2 = i6k.q(aVar6, 53.0f);
                boolean zD2 = pr5Var2.d(f4) | pr5Var2.z(ak6Var2) | pr5Var2.f(i3) | pr5Var2.g(j16) | pr5Var2.g(j5) | pr5Var2.g(j4);
                if (i7 == 2048) {
                    z = true;
                } else {
                    z = false;
                }
                zG = zD2 | z | pr5Var2.g(j);
                Object objX14 = pr5Var2.x();
                int i11 = i7;
                fr5.a.C0249a c0249a5 = c0249a2;
                if (zG) {
                    final long j110 = j;
                    final long j111 = j4;
                    j6 = j11;
                    final float f6 = f4;
                    obj2 = new g5a() { // from class: jk6
                        /* JADX WARN: Multi-variable type inference failed */
                        @Override // defpackage.g5a
                        public final Object invoke(Object obj8) {
                            ((sg7) obj8).getClass();
                            float f7 = f6;
                            long jFloatToRawIntBits = (((long) Float.floatToRawIntBits(f7)) << 32) | (((long) Float.floatToRawIntBits(f7)) & 4294967295L);
                            ak6 ak6Var4 = ak6Var2;
                            long jI = ioe.i(soe.c(ioe.h(ioe.i(j5, ((List) ak6Var4.d.getValue()).contains(Integer.valueOf(i3)) ? soe.d(((ioe) ak6Var4.f.getValue()).a, j16) : 0L), j111), f2), ioe.i(j110, jFloatToRawIntBits));
                            return new irb((((long) tld.b(Float.intBitsToFloat((int) (jI >> 32)))) << 32) | (((long) tld.b(Float.intBitsToFloat((int) (jI & 4294967295L)))) & 4294967295L));
                        }
                    };
                    f3 = f6;
                    j7 = j111;
                    j8 = j110;
                    pr5Var2.r(obj2);
                } else {
                    final long j112 = j;
                    final long j113 = j4;
                    j6 = j11;
                    final float f7 = f4;
                    obj2 = new g5a() { // from class: jk6
                        /* JADX WARN: Multi-variable type inference failed */
                        @Override // defpackage.g5a
                        public final Object invoke(Object obj8) {
                            ((sg7) obj8).getClass();
                            float f8 = f7;
                            long jFloatToRawIntBits = (((long) Float.floatToRawIntBits(f8)) << 32) | (((long) Float.floatToRawIntBits(f8)) & 4294967295L);
                            ak6 ak6Var4 = ak6Var2;
                            long jI = ioe.i(soe.c(ioe.h(ioe.i(j5, ((List) ak6Var4.d.getValue()).contains(Integer.valueOf(i3)) ? soe.d(((ioe) ak6Var4.f.getValue()).a, j16) : 0L), j113), f2), ioe.i(j112, jFloatToRawIntBits));
                            return new irb((((long) tld.b(Float.intBitsToFloat((int) (jI >> 32)))) << 32) | (((long) tld.b(Float.intBitsToFloat((int) (jI & 4294967295L)))) & 4294967295L));
                        }
                    };
                    f3 = f7;
                    j7 = j113;
                    j8 = j112;
                    pr5Var2.r(obj2);
                }
                txd txdVarA3 = i55.a(dmc.a(txdVarQ2, (g5a) obj2), rlh.a);
                zO = pr5Var2.O(strC);
                Object objX15 = pr5Var2.x();
                if (zO) {
                    z2 = false;
                    kk6 kk6Var2 = new kk6(false ? 1 : 0, strC);
                    pr5Var2.r(kk6Var2);
                    obj3 = kk6Var2;
                } else {
                    z2 = false;
                    kk6 kk6Var3 = new kk6(false ? 1 : 0, strC);
                    pr5Var2.r(kk6Var3);
                    obj3 = kk6Var3;
                }
                txd txdVarB4 = dhj.b(txdVarA3, z2, (g5a) obj3);
                z4eVar4 = z4eVar3;
                z3 = pr5Var2.z(ak6Var2) | pr5Var2.O(z4eVar4) | pr5Var2.f(i3);
                Object objX16 = pr5Var2.x();
                obj4 = objX16;
                if (z3) {
                    b bVar2 = new b(ak6Var2, z4eVar4, i3);
                    pr5Var2.r(bVar2);
                    obj4 = bVar2;
                } else {
                    b bVar3 = new b(ak6Var2, z4eVar4, i3);
                    pr5Var2.r(bVar3);
                    obj4 = bVar3;
                }
                txd txdVarA4 = ebc.a(txdVarB4, (g5a) obj4);
                z4 = pr5Var2.z(ak6Var2) | pr5Var2.O(z4eVar4) | pr5Var2.f(i3);
                Object objX17 = pr5Var2.x();
                obj5 = objX17;
                if (z4) {
                    c cVar2 = new c(ak6Var2, z4eVar4, i3);
                    pr5Var2.r(cVar2);
                    obj5 = cVar2;
                } else {
                    c cVar3 = new c(ak6Var2, z4eVar4, i3);
                    pr5Var2.r(cVar3);
                    obj5 = cVar3;
                }
                txd txdVarB5 = r0l.b(txdVarA4, ak6Var2, (PointerInputEventHandler) obj5);
                j9 = j3;
                z5 = pr5Var2.z(ak6Var2) | pr5Var2.f(i3) | pr5Var2.g(j6) | pr5Var2.g(j9);
                objX = pr5Var2.x();
                if (z5) {
                    final ak6 ak6Var4 = ak6Var2;
                    final int i12 = i3;
                    final long j114 = j6;
                    objX = new g5a() { // from class: lk6
                        @Override // defpackage.g5a
                        public final Object invoke(Object obj8) {
                            da8 da8Var = (da8) obj8;
                            da8Var.getClass();
                            float fQ1 = da8Var.q1(9.0f);
                            yok yokVar = new yok(da8Var.q1(3.0f), 0.0f, 0, 0, null, 30);
                            ((List) ak6Var4.d.getValue()).contains(Integer.valueOf(i12));
                            da8.a0(da8Var, j9, fQ1, da8Var.G1(), 0.0f, null, AdConfig.DEFAULT_PING_V2_INTERVAL_NORMAL);
                            da8.a0(da8Var, j114, fQ1, da8Var.G1(), 0.0f, yokVar, 104);
                            return d6m.a;
                        }
                    };
                    pr5Var2.r(objX);
                } else {
                    final ak6 ak6Var5 = ak6Var2;
                    final int i13 = i3;
                    final long j115 = j6;
                    objX = new g5a() { // from class: lk6
                        @Override // defpackage.g5a
                        public final Object invoke(Object obj8) {
                            da8 da8Var = (da8) obj8;
                            da8Var.getClass();
                            float fQ1 = da8Var.q1(9.0f);
                            yok yokVar = new yok(da8Var.q1(3.0f), 0.0f, 0, 0, null, 30);
                            ((List) ak6Var5.d.getValue()).contains(Integer.valueOf(i13));
                            da8.a0(da8Var, j9, fQ1, da8Var.G1(), 0.0f, null, AdConfig.DEFAULT_PING_V2_INTERVAL_NORMAL);
                            da8.a0(da8Var, j115, fQ1, da8Var.G1(), 0.0f, yokVar, 104);
                            return d6m.a;
                        }
                    };
                    pr5Var2.r(objX);
                }
                e74.a(txdVarB5, (g5a) objX, pr5Var2, 0);
                ak6Var2 = ak6Var;
                c0249a2 = c0249a5;
                f4 = f3;
                i3 = i4;
                z4eVar3 = z4eVar4;
                aVar2 = aVar6;
                it = it;
                i7 = i11;
                long j23 = j6;
                i = i;
                j4 = j7;
                j3 = j9;
                j16 = j16;
                j = j8;
                j11 = j23;
            }
            pr5Var2.Y(false);
            pr5Var2.P(1788612822);
            pr5Var2.Y(false);
            pr5Var2.Y(true);
            pr5Var2.Y(false);
            pr5Var = pr5Var2;
        }
        kzg kzgVarA0 = pr5Var.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a(f, n5iVar, f2, i, ak6Var, i2) { // from class: mk6
                public final /* synthetic */ float b;
                public final /* synthetic */ n5i c;
                public final /* synthetic */ float d;
                public final /* synthetic */ int e;
                public final /* synthetic */ ak6 f;

                @Override // defpackage.u5a
                public final Object invoke(Object obj8, Object obj9) {
                    ((Integer) obj9).getClass();
                    int iB = dae.b(1);
                    ok6.a(this.a, this.b, this.c, this.d, this.e, this.f, (fr5) obj8, iB);
                    return d6m.a;
                }
            };
        }
    }

    public static final void b(final kdb kdbVar, final qp5 qp5Var, float f, final float f2, long j, final int i, final g5a g5aVar, fr5 fr5Var, final int i2) {
        final float f3;
        final long j2;
        qp5Var.getClass();
        pr5 pr5VarJ = fr5Var.j(1241661446);
        int i3 = i2 | (pr5VarJ.z(kdbVar) ? 4 : 2) | (pr5VarJ.z(qp5Var) ? 32 : 16) | 384 | (pr5VarJ.d(f2) ? Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING : 1024) | 8192 | (pr5VarJ.f(i) ? 131072 : 65536) | (pr5VarJ.z(g5aVar) ? 1048576 : 524288);
        if (pr5VarJ.q(i3 & 1, (599187 & i3) != 599186)) {
            pr5VarJ.z0();
            if ((i2 & 1) == 0 || pr5VarJ.g0()) {
                j2 = ((j1j) pr5VarJ.L(l1j.g)).f;
                f3 = 16.0f;
            } else {
                pr5VarJ.G();
                f3 = f;
                j2 = j;
            }
            pr5VarJ.Z();
            bnf.a(khc.Ltr, vg5.c(805674793, pr5VarJ, new u5a() { // from class: ck6
                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    fr5 fr5Var2 = (fr5) obj;
                    int iIntValue = ((Integer) obj2).intValue();
                    if (fr5Var2.q(iIntValue & 1, (iIntValue & 3) != 2)) {
                        Object objX = fr5Var2.x();
                        qp5 qp5Var2 = qp5Var;
                        fr5.a.C0249a c0249a = fr5.a.a;
                        if (objX == c0249a) {
                            objX = nck.g(qp5Var2);
                            fr5Var2.r(objX);
                        }
                        final z4e z4eVar = (z4e) objX;
                        Object objX2 = fr5Var2.x();
                        if (objX2 == c0249a) {
                            objX2 = w62.a(cub.b(qp5Var2, qp5.h) ? 0.0f : 1.0f);
                            fr5Var2.r(objX2);
                        }
                        final o62 o62Var = (o62) objX2;
                        Object objX3 = fr5Var2.x();
                        if (objX3 == c0249a) {
                            c18 c18Var = oi8.a;
                            e4h e4hVar = new e4h(fr5Var2.n());
                            fr5Var2.r(e4hVar);
                            objX3 = e4hVar;
                        }
                        s86 s86Var = (s86) objX3;
                        boolean z = fr5Var2.z(qp5Var2) | fr5Var2.z(o62Var) | fr5Var2.z(s86Var);
                        Object objX4 = fr5Var2.x();
                        if (z || objX4 == c0249a) {
                            objX4 = new sk6(qp5Var2, o62Var, s86Var, z4eVar, null);
                            fr5Var2.r(objX4);
                        }
                        oi8.d(fr5Var2, (u5a) objX4, qp5Var2);
                        final kdb kdbVar2 = kdbVar;
                        if (kdbVar2 != null) {
                            fr5Var2.P(1581551863);
                            boolean zO = fr5Var2.O(kdbVar2);
                            Object objX5 = fr5Var2.x();
                            if (zO || objX5 == c0249a) {
                                objX5 = new z5k((((long) Float.floatToRawIntBits(kdbVar2.getHeight())) & 4294967295L) | (((long) Float.floatToRawIntBits(kdbVar2.getWidth())) << 32));
                                fr5Var2.r(objX5);
                            }
                            final long j3 = ((z5k) objX5).a;
                            txd txdVarD = i6k.d(txd.a.a, 1.0f);
                            final float f4 = f3;
                            final float f5 = f2;
                            final long j4 = j2;
                            final g5a g5aVar2 = g5aVar;
                            final int i4 = i;
                            cj3.a(txdVarD, null, vg5.c(1529272282, fr5Var2, new w5a() { // from class: ek6
                                /* JADX WARN: Code duplicated, block: B:68:0x0271  */
                                /* JADX WARN: Code duplicated, block: B:72:0x0292  */
                                /* JADX WARN: Code duplicated, block: B:76:0x02bb  */
                                /* JADX WARN: Multi-variable type inference failed */
                                /* JADX WARN: Type inference failed for: r8v12, types: [fk6] */
                                @Override // defpackage.w5a
                                public final Object invoke(Object obj3, Object obj4, Object obj5) {
                                    dj3 dj3Var;
                                    long j5;
                                    jjk jjkVarA;
                                    boolean zO2;
                                    Object objX6;
                                    boolean z2;
                                    Object objX7;
                                    dj3 dj3Var2 = (dj3) obj3;
                                    fr5 fr5Var3 = (fr5) obj4;
                                    int iIntValue2 = ((Integer) obj5).intValue();
                                    dj3Var2.getClass();
                                    if ((iIntValue2 & 6) == 0) {
                                        iIntValue2 |= fr5Var3.O(dj3Var2) ? 4 : 2;
                                    }
                                    int i5 = 0;
                                    if (fr5Var3.q(iIntValue2 & 1, (iIntValue2 & 19) != 18)) {
                                        float fD = dj3Var2.d();
                                        float f6 = f4;
                                        float f7 = 2.0f * f6;
                                        float f8 = fD - f7;
                                        float fE = dj3Var2.e() - f7;
                                        sg7 sg7Var = (sg7) fr5Var3.L(ys5.h);
                                        boolean zD = fr5Var3.d(f8) | fr5Var3.d(fE) | fr5Var3.O(sg7Var);
                                        Object objX8 = fr5Var3.x();
                                        fr5.a.C0249a c0249a2 = fr5.a.a;
                                        if (zD || objX8 == c0249a2) {
                                            objX8 = new z5k((((long) Float.floatToRawIntBits(sg7Var.q1(fE))) & 4294967295L) | (((long) Float.floatToRawIntBits(sg7Var.q1(f8))) << 32));
                                            fr5Var3.r(objX8);
                                        }
                                        long j6 = ((z5k) objX8).a;
                                        boolean zG = fr5Var3.g(j6);
                                        long j7 = j3;
                                        boolean zG2 = zG | fr5Var3.g(j7);
                                        Object objX9 = fr5Var3.x();
                                        if (zG2 || objX9 == c0249a2) {
                                            objX9 = ba8.a(j6, j7);
                                            fr5Var3.r(objX9);
                                        }
                                        final n5i n5iVar = (n5i) objX9;
                                        txd txdVarF = r3f.f(dj3Var2.a(), f6);
                                        boolean zO3 = fr5Var3.O(n5iVar);
                                        final float f9 = f5;
                                        boolean zD2 = zO3 | fr5Var3.d(f9);
                                        final kdb kdbVar3 = kdbVar2;
                                        boolean z3 = zD2 | fr5Var3.z(kdbVar3);
                                        Object objX10 = fr5Var3.x();
                                        if (z3 || objX10 == c0249a2) {
                                            objX10 = new g5a() { // from class: bk6
                                                @Override // defpackage.g5a
                                                public final Object invoke(Object obj6) {
                                                    kdb kdbVar4 = kdbVar3;
                                                    da8 da8Var = (da8) obj6;
                                                    da8Var.getClass();
                                                    n5i n5iVar2 = n5iVar;
                                                    n5iVar2.getClass();
                                                    float f10 = f9;
                                                    z98 z98Var = new z98(n5iVar2, n5iVar2.a(f10, true), f10, true);
                                                    z64.b bVarU1 = da8Var.u1();
                                                    long jD = bVarU1.d();
                                                    bVarU1.a().c();
                                                    try {
                                                        z98Var.invoke(bVarU1.a);
                                                        da8.I0(da8Var, kdbVar4, 0.0f, null, 62);
                                                        return d6m.a;
                                                    } finally {
                                                        hh3.c(bVarU1, jD);
                                                    }
                                                }
                                            };
                                            fr5Var3.r(objX10);
                                        }
                                        e74.a(txdVarF, (g5a) objX10, fr5Var3, 0);
                                        boolean zO4 = fr5Var3.O(n5iVar);
                                        Object objX11 = fr5Var3.x();
                                        final z4e z4eVar2 = z4eVar;
                                        if (zO4 || objX11 == c0249a2) {
                                            final g5a g5aVar3 = g5aVar2;
                                            objX11 = new ak6(n5iVar, new tj6(new g5a() { // from class: fk6
                                                @Override // defpackage.g5a
                                                public final Object invoke(Object obj6) {
                                                    qp5 qp5Var3 = (qp5) obj6;
                                                    qp5Var3.getClass();
                                                    z4eVar2.setValue(qp5Var3);
                                                    g5aVar3.invoke(qp5Var3);
                                                    return d6m.a;
                                                }
                                            }, 1));
                                            fr5Var3.r(objX11);
                                        }
                                        ak6 ak6Var = (ak6) objX11;
                                        o62 o62Var2 = o62Var;
                                        boolean z4 = fr5Var3.z(o62Var2);
                                        Object objX12 = fr5Var3.x();
                                        if (z4 || objX12 == c0249a2) {
                                            objX12 = new gk6(i5, o62Var2);
                                            fr5Var3.r(objX12);
                                        }
                                        txd.a aVar = txd.a.a;
                                        txd txdVarA = vra.a(aVar, (g5a) objX12);
                                        bmd bmdVarD = si3.d(um1.a.a, false);
                                        int iHashCode = Long.hashCode(fr5Var3.m());
                                        xif xifVarO = fr5Var3.o();
                                        txd txdVarC = er5.c(fr5Var3, txdVarA);
                                        cr5.lo.getClass();
                                        e.a aVar2 = cr5.a.b;
                                        if (fr5Var3.l() == null) {
                                            lp5.b();
                                            throw null;
                                        }
                                        fr5Var3.D();
                                        if (fr5Var3.h()) {
                                            fr5Var3.A(aVar2);
                                        } else {
                                            fr5Var3.p();
                                        }
                                        pi9.e(fr5Var3, cr5.a.g, bmdVarD);
                                        pi9.e(fr5Var3, cr5.a.f, xifVarO);
                                        pi9.c(fr5Var3, Integer.valueOf(iHashCode), cr5.a.j);
                                        pi9.d(fr5Var3, cr5.a.k);
                                        pi9.e(fr5Var3, cr5.a.d, txdVarC);
                                        ok6.a((qp5) z4eVar2.getValue(), f6, n5iVar, f9, i4, ak6Var, fr5Var3, 0);
                                        fr5Var3.s();
                                        hcf hcfVar = ak6Var.d;
                                        jjk jjkVarB = o72.b(((List) hcfVar.getValue()).size() == 1 ? 1.0f : 0.0f, ea2.f(300, 0, null, 6), "Crop Magnifier fade in", null, fr5Var3, 3120, 20);
                                        if (((List) hcfVar.getValue()).size() == 1) {
                                            fr5Var3.P(1834700479);
                                            long jI = ioe.i(((qp5) z4eVar2.getValue()).b(((Number) x75.M((List) hcfVar.getValue())).intValue()), ((ioe) ak6Var.f.getValue()).a);
                                            long j8 = qp5.i;
                                            long jI2 = ioe.i(soe.c(ioe.h(jI, j8), f9), j8);
                                            Object objX13 = fr5Var3.x();
                                            if (objX13 == c0249a2) {
                                                objX13 = nck.g(Boolean.valueOf(Float.intBitsToFloat((int) (jI2 >> 32)) > 0.5f));
                                                fr5Var3.r(objX13);
                                            }
                                            z4e z4eVar3 = (z4e) objX13;
                                            if (((Boolean) z4eVar3.getValue()).booleanValue()) {
                                                dj3Var = dj3Var2;
                                                j5 = jI;
                                            } else {
                                                dj3Var = dj3Var2;
                                                j5 = jI;
                                                if (Float.intBitsToFloat((int) (jI2 >> 32)) > 0.55f) {
                                                    z4eVar3.setValue(Boolean.TRUE);
                                                }
                                                jjkVarA = o72.a(((Boolean) z4eVar3.getValue()).booleanValue() ? 40.0f : (dj3Var.d() - 80.0f) - 40.0f, null, "Crop Magnifier horizontal position", null, fr5Var3, 384, 10);
                                                zO2 = fr5Var3.O(jjkVarA);
                                                objX6 = fr5Var3.x();
                                                if (zO2 || objX6 == c0249a2) {
                                                    objX6 = new hk6(0, jjkVarA);
                                                    fr5Var3.r(objX6);
                                                }
                                                txd txdVarA2 = eig.a(dmc.a(aVar, (g5a) objX6), ((Number) jjkVarB.getValue()).floatValue());
                                                z2 = fr5Var3.z(ak6Var);
                                                objX7 = fr5Var3.x();
                                                if (z2 || objX7 == c0249a2) {
                                                    objX7 = new jd(1, ak6Var);
                                                    fr5Var3.r(objX7);
                                                }
                                                qj6.a(kdbVar3, j5, f9, txdVarA2, 0.0f, j4, (g5a) objX7, fr5Var3, 24576);
                                                fr5Var3.I();
                                            }
                                            if (((Boolean) z4eVar3.getValue()).booleanValue() && Float.intBitsToFloat((int) (jI2 >> 32)) < 0.45d) {
                                                z4eVar3.setValue(Boolean.FALSE);
                                            }
                                            jjkVarA = o72.a(((Boolean) z4eVar3.getValue()).booleanValue() ? 40.0f : (dj3Var.d() - 80.0f) - 40.0f, null, "Crop Magnifier horizontal position", null, fr5Var3, 384, 10);
                                            zO2 = fr5Var3.O(jjkVarA);
                                            objX6 = fr5Var3.x();
                                            if (zO2) {
                                                objX6 = new hk6(0, jjkVarA);
                                                fr5Var3.r(objX6);
                                            } else {
                                                objX6 = new hk6(0, jjkVarA);
                                                fr5Var3.r(objX6);
                                            }
                                            txd txdVarA3 = eig.a(dmc.a(aVar, (g5a) objX6), ((Number) jjkVarB.getValue()).floatValue());
                                            z2 = fr5Var3.z(ak6Var);
                                            objX7 = fr5Var3.x();
                                            if (z2) {
                                                objX7 = new jd(1, ak6Var);
                                                fr5Var3.r(objX7);
                                            } else {
                                                objX7 = new jd(1, ak6Var);
                                                fr5Var3.r(objX7);
                                            }
                                            qj6.a(kdbVar3, j5, f9, txdVarA3, 0.0f, j4, (g5a) objX7, fr5Var3, 24576);
                                            fr5Var3.I();
                                        } else {
                                            fr5Var3.P(1835785448);
                                            fr5Var3.I();
                                        }
                                    } else {
                                        fr5Var3.G();
                                    }
                                    return d6m.a;
                                }
                            }), fr5Var2, 3078, 6);
                            fr5Var2.I();
                        } else {
                            fr5Var2.P(1584998009);
                            fr5Var2.I();
                        }
                    } else {
                        fr5Var2.G();
                    }
                    return d6m.a;
                }
            }), pr5VarJ, 54);
        } else {
            pr5VarJ.G();
            f3 = f;
            j2 = j;
        }
        kzg kzgVarA0 = pr5VarJ.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a(qp5Var, f3, f2, j2, i, g5aVar, i2) { // from class: dk6
                public final /* synthetic */ qp5 b;
                public final /* synthetic */ float c;
                public final /* synthetic */ float d;
                public final /* synthetic */ long e;
                public final /* synthetic */ int f;
                public final /* synthetic */ g5a g;

                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    int iB = dae.b(1);
                    ok6.b(this.a, this.b, this.c, this.d, this.e, this.f, this.g, (fr5) obj, iB);
                    return d6m.a;
                }
            };
        }
    }
}
