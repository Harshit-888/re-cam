package defpackage;

import androidx.compose.ui.node.e;
import androidx.compose.ui.platform.AndroidCompositionLocals_androidKt;
import com.adobe.dcmscan.CaptureActivity;
import com.adobe.dcmscan.b;
import com.adobe.dcmscan.c;
import com.adobe.dcmscan.document.Page;
import com.adobe.scan.android.R;
import com.adobe.t5.pdf.Document;
import com.inmobi.commons.core.configs.CrashConfig;

/* JADX INFO: compiled from: CaptureActivityHints.kt */
/* JADX INFO: loaded from: classes.dex */
public final class lj4 {

    /* JADX INFO: compiled from: CaptureActivityHints.kt */
    /* JADX INFO: loaded from: classes2.dex */
    @r67(c = "com.adobe.dcmscan.ui.CaptureActivityHintsKt$LiveTextHint$4$1", f = "CaptureActivityHints.kt", l = {278, 279, 280}, m = "invokeSuspend", v = 2)
    public static final class a extends p0l implements u5a<s86, u36<? super d6m>, Object> {
        public int t;
        public final /* synthetic */ o62<Float, ia2> u;
        public final /* synthetic */ long v;
        public final /* synthetic */ e5a<d6m> w;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public a(o62<Float, ia2> o62Var, long j, e5a<d6m> e5aVar, u36<? super a> u36Var) {
            super(2, u36Var);
            this.u = o62Var;
            this.v = j;
            this.w = e5aVar;
        }

        @Override // defpackage.k73
        public final u36<d6m> create(Object obj, u36<?> u36Var) {
            return new a(this.u, this.v, this.w, u36Var);
        }

        @Override // defpackage.u5a
        public final Object invoke(s86 s86Var, u36<? super d6m> u36Var) {
            return ((a) create(s86Var, u36Var)).invokeSuspend(d6m.a);
        }

        /* JADX WARN: Code restructure failed: missing block: B:19:0x0061, code lost:
        
            if (defpackage.o62.c(r10.u, r5, r6, r7, r10, 4) == r0) goto L20;
         */
        @Override // defpackage.k73
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public final java.lang.Object invokeSuspend(java.lang.Object r11) {
            /*
                r10 = this;
                u86 r0 = defpackage.u86.COROUTINE_SUSPENDED
                int r1 = r10.t
                r2 = 0
                r3 = 3
                r4 = 2
                r5 = 1
                if (r1 == 0) goto L22
                if (r1 == r5) goto L1e
                if (r1 == r4) goto L1a
                if (r1 != r3) goto L14
                defpackage.pbh.b(r11)
                goto L64
            L14:
                java.lang.String r10 = "call to 'resume' before 'invoke' with coroutine"
                defpackage.ll3.a(r10)
                return r2
            L1a:
                defpackage.pbh.b(r11)
                goto L42
            L1e:
                defpackage.pbh.b(r11)
                goto L37
            L22:
                defpackage.pbh.b(r11)
                java.lang.Float r11 = new java.lang.Float
                r1 = 1065353216(0x3f800000, float:1.0)
                r11.<init>(r1)
                r10.t = r5
                o62<java.lang.Float, ia2> r1 = r10.u
                java.lang.Object r11 = r1.e(r10, r11)
                if (r11 != r0) goto L37
                goto L63
            L37:
                r10.t = r4
                long r4 = r10.v
                java.lang.Object r11 = defpackage.we7.b(r4, r10)
                if (r11 != r0) goto L42
                goto L63
            L42:
                java.lang.Float r5 = new java.lang.Float
                r11 = 0
                r5.<init>(r11)
                r11 = 250(0xfa, float:3.5E-43)
                r1 = 6
                r4 = 0
                i0m r6 = defpackage.ea2.f(r11, r4, r2, r1)
                kj4 r7 = new kj4
                e5a<d6m> r11 = r10.w
                r7.<init>(r4, r11)
                r10.t = r3
                o62<java.lang.Float, ia2> r4 = r10.u
                r9 = 4
                r8 = r10
                java.lang.Object r10 = defpackage.o62.c(r4, r5, r6, r7, r8, r9)
                if (r10 != r0) goto L64
            L63:
                return r0
            L64:
                d6m r10 = defpackage.d6m.a
                return r10
            */
            throw new UnsupportedOperationException("Method not decompiled: lj4.a.invokeSuspend(java.lang.Object):java.lang.Object");
        }
    }

    /* JADX INFO: compiled from: CaptureActivityHints.kt */
    public static final /* synthetic */ class b {
        public static final /* synthetic */ int[] a;
        public static final /* synthetic */ int[] b;
        public static final /* synthetic */ int[] c;

        static {
            int[] iArr = new int[rk4.values().length];
            try {
                iArr[rk4.TapToStart.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                iArr[rk4.AutoCaptureOff.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                iArr[rk4.AutoCaptureOn.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                iArr[rk4.AutoCaptureOnBookMode.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                iArr[rk4.WaitingForCameraToStart.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                iArr[rk4.NoImageForCapability.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            a = iArr;
            int[] iArr2 = new int[k0d.values().length];
            try {
                iArr2[k0d.LookingFor.ordinal()] = 1;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                iArr2[k0d.CenterWhiteboard.ordinal()] = 2;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                iArr2[k0d.IdCardHintFirstPage.ordinal()] = 3;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                iArr2[k0d.IdCardHintSecondPage.ordinal()] = 4;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                iArr2[k0d.Capturing.ordinal()] = 5;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                iArr2[k0d.NoTypeFound.ordinal()] = 6;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                iArr2[k0d.FlashStatusChanged.ordinal()] = 7;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                iArr2[k0d.AutoCaptureStatusChanged.ordinal()] = 8;
            } catch (NoSuchFieldError unused14) {
            }
            b = iArr2;
            int[] iArr3 = new int[Page.CaptureMode.values().length];
            try {
                iArr3[Page.CaptureMode.BUSINESS_CARD.ordinal()] = 1;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                iArr3[Page.CaptureMode.ID_CARD.ordinal()] = 2;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                iArr3[Page.CaptureMode.BOOK.ordinal()] = 3;
            } catch (NoSuchFieldError unused17) {
            }
            c = iArr3;
        }
    }

    public static final void a(final rk4 rk4Var, final k0d k0dVar, final hcf hcfVar, final oh7 oh7Var, final jjk jjkVar, final jjk jjkVar2, final j0d j0dVar, final String str, final String str2, fr5 fr5Var, final int i) {
        rk4Var.getClass();
        k0dVar.getClass();
        jjkVar.getClass();
        jjkVar2.getClass();
        j0dVar.getClass();
        pr5 pr5VarJ = fr5Var.j(2075149269);
        int i2 = i | (pr5VarJ.f(rk4Var.ordinal()) ? 4 : 2) | (pr5VarJ.f(k0dVar.ordinal()) ? 32 : 16) | (pr5VarJ.O(hcfVar) ? Document.PERMITTED_OPERATION_FORM_ENTRY : 128) | (pr5VarJ.O(oh7Var) ? Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING : 1024) | (pr5VarJ.O(jjkVar) ? 16384 : 8192) | (pr5VarJ.O(jjkVar2) ? 131072 : 65536) | (pr5VarJ.O(j0dVar) ? 1048576 : 524288) | (pr5VarJ.O(str) ? 8388608 : 4194304) | (pr5VarJ.O(str2) ? 67108864 : 33554432);
        if (!pr5VarJ.q(i2 & 1, (38347923 & i2) != 38347922)) {
            pr5VarJ.G();
        } else if (rk4Var != rk4.None) {
            pr5VarJ.P(-986838166);
            int i3 = (i2 & 14) | ((i2 >> 3) & 112) | ((i2 >> 9) & 896);
            int i4 = i2 >> 12;
            d(rk4Var, hcfVar, jjkVar2, str, str2, pr5VarJ, i3 | (i4 & 7168) | (i4 & 57344));
            pr5VarJ.Y(false);
        } else if (k0dVar != k0d.None) {
            pr5VarJ.P(-986487277);
            int i5 = i2 >> 3;
            e(k0dVar, jjkVar2, hcfVar, oh7Var, jjkVar, j0dVar, pr5VarJ, (i2 & 57344) | (i5 & 14) | ((i2 >> 12) & 112) | (i2 & 896) | (i2 & 7168) | (i5 & 458752));
            pr5VarJ = pr5VarJ;
            pr5VarJ.Y(false);
        } else {
            pr5VarJ.P(-986216275);
            pr5VarJ.Y(false);
        }
        kzg kzgVarA0 = pr5VarJ.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a(k0dVar, hcfVar, oh7Var, jjkVar, jjkVar2, j0dVar, str, str2, i) { // from class: vi4
                public final /* synthetic */ k0d b;
                public final /* synthetic */ hcf c;
                public final /* synthetic */ oh7 d;
                public final /* synthetic */ jjk e;
                public final /* synthetic */ jjk f;
                public final /* synthetic */ j0d g;
                public final /* synthetic */ String h;
                public final /* synthetic */ String i;

                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    int iB = dae.b(1);
                    lj4.a(this.a, this.b, this.c, this.d, this.e, this.f, this.g, this.h, this.i, (fr5) obj, iB);
                    return d6m.a;
                }
            };
        }
    }

    /* JADX WARN: Code duplicated, block: B:103:0x013c  */
    /* JADX WARN: Code duplicated, block: B:106:0x017a  */
    /* JADX WARN: Code duplicated, block: B:107:0x017c  */
    /* JADX WARN: Code duplicated, block: B:111:0x0185  */
    /* JADX WARN: Code duplicated, block: B:114:0x01b7  */
    /* JADX WARN: Code duplicated, block: B:115:0x01bb  */
    /* JADX WARN: Code duplicated, block: B:118:0x01e1  */
    /* JADX WARN: Code duplicated, block: B:121:0x0221  */
    /* JADX WARN: Code duplicated, block: B:122:0x0225  */
    /* JADX WARN: Code duplicated, block: B:125:0x024a  */
    /* JADX WARN: Code duplicated, block: B:126:0x02ac  */
    /* JADX WARN: Code duplicated, block: B:128:0x0312  */
    /* JADX WARN: Code duplicated, block: B:131:0x031c  */
    /* JADX WARN: Code duplicated, block: B:133:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:30:0x005d  */
    /* JADX WARN: Code duplicated, block: B:32:0x0063  */
    /* JADX WARN: Code duplicated, block: B:33:0x0066  */
    /* JADX WARN: Code duplicated, block: B:37:0x006d  */
    /* JADX WARN: Code duplicated, block: B:39:0x0073  */
    /* JADX WARN: Code duplicated, block: B:40:0x0076  */
    /* JADX WARN: Code duplicated, block: B:44:0x007e  */
    /* JADX WARN: Code duplicated, block: B:46:0x0084  */
    /* JADX WARN: Code duplicated, block: B:47:0x0087  */
    /* JADX WARN: Code duplicated, block: B:51:0x008f  */
    /* JADX WARN: Code duplicated, block: B:53:0x0095  */
    /* JADX WARN: Code duplicated, block: B:54:0x0098  */
    /* JADX WARN: Code duplicated, block: B:58:0x00a0  */
    /* JADX WARN: Code duplicated, block: B:60:0x00a9  */
    /* JADX WARN: Code duplicated, block: B:61:0x00ac  */
    /* JADX WARN: Code duplicated, block: B:63:0x00b0  */
    /* JADX WARN: Code duplicated, block: B:66:0x00be  */
    /* JADX WARN: Code duplicated, block: B:67:0x00c0  */
    /* JADX WARN: Code duplicated, block: B:70:0x00c9  */
    /* JADX WARN: Code duplicated, block: B:76:0x00db A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:77:0x00dd  */
    /* JADX WARN: Code duplicated, block: B:80:0x00ec  */
    /* JADX WARN: Code duplicated, block: B:81:0x00ef  */
    /* JADX WARN: Code duplicated, block: B:84:0x00f8  */
    /* JADX WARN: Code duplicated, block: B:85:0x00fb  */
    /* JADX WARN: Code duplicated, block: B:88:0x0103  */
    /* JADX WARN: Code duplicated, block: B:89:0x0105  */
    /* JADX WARN: Code duplicated, block: B:95:0x0115  */
    /* JADX WARN: Code duplicated, block: B:98:0x0131  */
    /* JADX WARN: Code duplicated, block: B:99:0x0133  */
    public static final void b(final rk4 rk4Var, final String str, String str2, final ug5 ug5Var, final jjk jjkVar, final u3f u3fVar, final atj atjVar, final long j, fr5 fr5Var, final int i, final int i2) {
        int i3;
        String str3;
        boolean z;
        final String str4;
        kzg kzgVarA0;
        Object objL;
        final CaptureActivity captureActivity;
        int i4;
        boolean z2;
        boolean z3;
        boolean z4;
        Object objX;
        fr5.a.C0249a c0249a;
        boolean z5;
        Object objX2;
        txd.a aVar;
        tkk tkkVar;
        int i5;
        ge3.a aVar2;
        boolean z6;
        Object objX3;
        e.a aVar3;
        Object objX4;
        String str5;
        int i6;
        gb5 gb5Var;
        int i7;
        String str6;
        boolean z7;
        int i8;
        int i9;
        int i10;
        int i11;
        int i12;
        rk4Var.getClass();
        pr5 pr5VarJ = fr5Var.j(252278776);
        if ((i & 6) == 0) {
            i3 = (pr5VarJ.f(rk4Var.ordinal()) ? 4 : 2) | i;
        } else {
            i3 = i;
        }
        if ((i & 48) == 0) {
            i3 |= pr5VarJ.O(str) ? 32 : 16;
        }
        int i13 = i2 & 4;
        if (i13 == 0) {
            if ((i & 384) == 0) {
                str3 = str2;
                i3 |= pr5VarJ.O(str3) ? Document.PERMITTED_OPERATION_FORM_ENTRY : 128;
            }
            if ((i & 3072) == 0) {
                if (pr5VarJ.z(ug5Var)) {
                    i12 = Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING;
                } else {
                    i12 = 1024;
                }
                i3 |= i12;
            }
            if ((i & 24576) == 0) {
                if (pr5VarJ.O(jjkVar)) {
                    i11 = 16384;
                } else {
                    i11 = 8192;
                }
                i3 |= i11;
            }
            if ((196608 & i) == 0) {
                if (pr5VarJ.O(u3fVar)) {
                    i10 = 131072;
                } else {
                    i10 = 65536;
                }
                i3 |= i10;
            }
            if ((1572864 & i) == 0) {
                if (pr5VarJ.O(atjVar)) {
                    i9 = 1048576;
                } else {
                    i9 = 524288;
                }
                i3 |= i9;
            }
            if ((12582912 & i) != 0) {
                if (pr5VarJ.g(j)) {
                    i8 = 8388608;
                } else {
                    i8 = 4194304;
                }
                i3 |= i8;
            }
            if ((4793491 & i3) != 4793490) {
                z = true;
            } else {
                z = false;
            }
            if (pr5VarJ.q(i3 & 1, z)) {
                pr5VarJ.z0();
                if ((i & 1) == 0 && !pr5VarJ.g0()) {
                    pr5VarJ.G();
                } else if (i13 != 0) {
                    str3 = "";
                }
                pr5VarJ.Z();
                objL = pr5VarJ.L(AndroidCompositionLocals_androidKt.b);
                if (objL instanceof CaptureActivity) {
                    captureActivity = (CaptureActivity) objL;
                } else {
                    captureActivity = null;
                }
                boolean z8 = pr5VarJ.z(captureActivity);
                i4 = i3 & 112;
                if (i4 == 32) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                boolean z9 = z8 | z2;
                if ((i3 & 14) == 4) {
                    z3 = true;
                } else {
                    z3 = false;
                }
                z4 = z3 | z9;
                objX = pr5VarJ.x();
                c0249a = fr5.a.a;
                if (z4 || objX == c0249a) {
                    objX = new g5a() { // from class: cj4
                        @Override // defpackage.g5a
                        public final Object invoke(Object obj) {
                            c cVar;
                            ((c18) obj).getClass();
                            CaptureActivity captureActivity2 = captureActivity;
                            b bVarB = (captureActivity2 == null || (cVar = captureActivity2.C0) == null) ? null : cVar.b(str);
                            int i14 = rk4Var == rk4.WaitingForCameraToStart ? 15 : 11;
                            if (bVarB != null) {
                                bVarB.d(i14);
                            }
                            return new jj4(bVarB, i14);
                        }
                    };
                    pr5VarJ.r(objX);
                }
                int i14 = (i3 >> 3) & 14;
                oi8.a(str, (g5a) objX, pr5VarJ);
                if ((i3 & 57344) == 16384) {
                    z5 = true;
                } else {
                    z5 = false;
                }
                objX2 = pr5VarJ.x();
                if (z5 || objX2 == c0249a) {
                    objX2 = new g5a() { // from class: dj4
                        @Override // defpackage.g5a
                        public final Object invoke(Object obj) {
                            xra xraVar = (xra) obj;
                            xraVar.getClass();
                            xraVar.T0(((Number) jjkVar.getValue()).floatValue());
                            return d6m.a;
                        }
                    };
                    pr5VarJ.r(objX2);
                }
                aVar = txd.a.a;
                txd txdVarA = vra.a(aVar, (g5a) objX2);
                tkkVar = l1j.g;
                i5 = i3;
                txd txdVarE = r3f.e(i6k.p(z23.c(txdVarA, ((j1j) pr5VarJ.L(tkkVar)).E.f, atjVar)), u3fVar);
                aVar2 = um1.a.n;
                txd txdVarB = yw9.b(2, i6k.z(txdVarE, aVar2, 2), true);
                if (i4 == 32) {
                    z6 = true;
                } else {
                    z6 = false;
                }
                objX3 = pr5VarJ.x();
                if (z6 || objX3 == c0249a) {
                    objX3 = new g5a() { // from class: ej4
                        @Override // defpackage.g5a
                        public final Object invoke(Object obj) {
                            uhj uhjVar = (uhj) obj;
                            uhjVar.getClass();
                            rhj.i(uhjVar, 0);
                            rhj.h(str, uhjVar);
                            return d6m.a;
                        }
                    };
                    pr5VarJ.r(objX3);
                }
                txd txdVarB2 = dhj.b(txdVarB, true, (g5a) objX3);
                bmd bmdVarD = si3.d(um1.a.a, false);
                int iHashCode = Long.hashCode(pr5VarJ.T);
                xif xifVarT = pr5VarJ.T();
                txd txdVarC = er5.c(pr5VarJ, txdVarB2);
                cr5.lo.getClass();
                aVar3 = cr5.a.b;
                pr5VarJ.D();
                if (pr5VarJ.S) {
                    pr5VarJ.A(aVar3);
                } else {
                    pr5VarJ.p();
                }
                cr5.a.e eVar = cr5.a.g;
                pi9.e(pr5VarJ, eVar, bmdVarD);
                cr5.a.g gVar = cr5.a.f;
                pi9.e(pr5VarJ, gVar, xifVarT);
                Integer numValueOf = Integer.valueOf(iHashCode);
                cr5.a.b bVar = cr5.a.j;
                pi9.c(pr5VarJ, numValueOf, bVar);
                cr5.a.C0215a c0215a = cr5.a.k;
                pi9.d(pr5VarJ, c0215a);
                cr5.a.f fVar = cr5.a.d;
                pi9.e(pr5VarJ, fVar, txdVarC);
                objX4 = pr5VarJ.x();
                if (objX4 == c0249a) {
                    objX4 = new fj4();
                    pr5VarJ.r(objX4);
                }
                str5 = str3;
                txd txdVarA2 = z92.a(yw9.b(2, dhj.a(aVar, (g5a) objX4), false), ea2.f(250, 0, td8.a, 2), 2);
                db5 db5VarA = bb5.a(kj2.c, um1.a.m, pr5VarJ, 0);
                int iHashCode2 = Long.hashCode(pr5VarJ.T);
                xif xifVarT2 = pr5VarJ.T();
                txd txdVarC2 = er5.c(pr5VarJ, txdVarA2);
                pr5VarJ.D();
                if (pr5VarJ.S) {
                    pr5VarJ.A(aVar3);
                } else {
                    pr5VarJ.p();
                }
                pi9.e(pr5VarJ, eVar, db5VarA);
                pi9.e(pr5VarJ, gVar, xifVarT2);
                k36.a(iHashCode2, pr5VarJ, bVar, pr5VarJ, c0215a);
                pi9.e(pr5VarJ, fVar, txdVarC2);
                i6 = i5 >> 6;
                Object objValueOf = Integer.valueOf((i6 & 112) | 6);
                gb5Var = gb5.a;
                ug5Var.invoke(gb5Var, pr5VarJ, objValueOf);
                i7 = 3;
                if (str5.length() > 0) {
                    pr5VarJ.P(-1395034586);
                    z7 = true;
                    kil.b(str5, gb5Var.b(aVar2, i6k.f(r3f.i(aVar, 0.0f, 0.0f, 0.0f, 8.0f), 1.0f)), ((j1j) pr5VarJ.L(tkkVar)).E.g, j, null, null, vp5.a(), 0L, null, new aal(i7), 0L, 0, false, 0, 0, null, null, pr5VarJ, (i6 & 14) | ((i5 >> 9) & 57344), 0, 260968);
                    str6 = str5;
                    pr5VarJ.Y(false);
                } else {
                    str6 = str5;
                    z7 = true;
                    pr5VarJ.P(-1394542554);
                    pr5VarJ.Y(false);
                }
                kil.b(str, null, ((j1j) pr5VarJ.L(tkkVar)).E.g, rkl.e(15), null, null, vp5.a(), 0L, null, new aal(i7), 0L, 0, false, 0, 0, null, null, pr5VarJ, i14 | 24576, 0, 260970);
                pr5VarJ = pr5VarJ;
                pr5VarJ.Y(true);
                pr5VarJ.Y(true);
                str4 = str6;
            } else {
                pr5VarJ.G();
                str4 = str3;
            }
            kzgVarA0 = pr5VarJ.a0();
            if (kzgVarA0 != null) {
                kzgVarA0.d = new u5a() { // from class: gj4
                    @Override // defpackage.u5a
                    public final Object invoke(Object obj, Object obj2) {
                        ((Integer) obj2).getClass();
                        lj4.b(rk4Var, str, str4, ug5Var, jjkVar, u3fVar, atjVar, j, (fr5) obj, dae.b(i | 1), i2);
                        return d6m.a;
                    }
                };
            }
        }
        i3 |= 384;
        str3 = str2;
        if ((i & 3072) == 0) {
            if (pr5VarJ.z(ug5Var)) {
                i12 = Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING;
            } else {
                i12 = 1024;
            }
            i3 |= i12;
        }
        if ((i & 24576) == 0) {
            if (pr5VarJ.O(jjkVar)) {
                i11 = 16384;
            } else {
                i11 = 8192;
            }
            i3 |= i11;
        }
        if ((196608 & i) == 0) {
            if (pr5VarJ.O(u3fVar)) {
                i10 = 131072;
            } else {
                i10 = 65536;
            }
            i3 |= i10;
        }
        if ((1572864 & i) == 0) {
            if (pr5VarJ.O(atjVar)) {
                i9 = 1048576;
            } else {
                i9 = 524288;
            }
            i3 |= i9;
        }
        if ((12582912 & i) != 0) {
            if (pr5VarJ.g(j)) {
                i8 = 8388608;
            } else {
                i8 = 4194304;
            }
            i3 |= i8;
        }
        if ((4793491 & i3) != 4793490) {
            z = true;
        } else {
            z = false;
        }
        if (pr5VarJ.q(i3 & 1, z)) {
            pr5VarJ.z0();
            if ((i & 1) == 0) {
                if (i13 != 0) {
                    str3 = "";
                }
            } else if (i13 != 0) {
                str3 = "";
            }
            pr5VarJ.Z();
            objL = pr5VarJ.L(AndroidCompositionLocals_androidKt.b);
            if (objL instanceof CaptureActivity) {
                captureActivity = (CaptureActivity) objL;
            } else {
                captureActivity = null;
            }
            boolean z10 = pr5VarJ.z(captureActivity);
            i4 = i3 & 112;
            if (i4 == 32) {
                z2 = true;
            } else {
                z2 = false;
            }
            boolean z11 = z10 | z2;
            if ((i3 & 14) == 4) {
                z3 = true;
            } else {
                z3 = false;
            }
            z4 = z3 | z11;
            objX = pr5VarJ.x();
            c0249a = fr5.a.a;
            if (z4) {
                objX = new g5a() { // from class: cj4
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj) {
                        c cVar;
                        ((c18) obj).getClass();
                        CaptureActivity captureActivity2 = captureActivity;
                        b bVarB = (captureActivity2 == null || (cVar = captureActivity2.C0) == null) ? null : cVar.b(str);
                        int i15 = rk4Var == rk4.WaitingForCameraToStart ? 15 : 11;
                        if (bVarB != null) {
                            bVarB.d(i15);
                        }
                        return new jj4(bVarB, i15);
                    }
                };
                pr5VarJ.r(objX);
            } else {
                objX = new g5a() { // from class: cj4
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj) {
                        c cVar;
                        ((c18) obj).getClass();
                        CaptureActivity captureActivity2 = captureActivity;
                        b bVarB = (captureActivity2 == null || (cVar = captureActivity2.C0) == null) ? null : cVar.b(str);
                        int i15 = rk4Var == rk4.WaitingForCameraToStart ? 15 : 11;
                        if (bVarB != null) {
                            bVarB.d(i15);
                        }
                        return new jj4(bVarB, i15);
                    }
                };
                pr5VarJ.r(objX);
            }
            int i15 = (i3 >> 3) & 14;
            oi8.a(str, (g5a) objX, pr5VarJ);
            if ((i3 & 57344) == 16384) {
                z5 = true;
            } else {
                z5 = false;
            }
            objX2 = pr5VarJ.x();
            if (z5) {
                objX2 = new g5a() { // from class: dj4
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj) {
                        xra xraVar = (xra) obj;
                        xraVar.getClass();
                        xraVar.T0(((Number) jjkVar.getValue()).floatValue());
                        return d6m.a;
                    }
                };
                pr5VarJ.r(objX2);
            } else {
                objX2 = new g5a() { // from class: dj4
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj) {
                        xra xraVar = (xra) obj;
                        xraVar.getClass();
                        xraVar.T0(((Number) jjkVar.getValue()).floatValue());
                        return d6m.a;
                    }
                };
                pr5VarJ.r(objX2);
            }
            aVar = txd.a.a;
            txd txdVarA3 = vra.a(aVar, (g5a) objX2);
            tkkVar = l1j.g;
            i5 = i3;
            txd txdVarE2 = r3f.e(i6k.p(z23.c(txdVarA3, ((j1j) pr5VarJ.L(tkkVar)).E.f, atjVar)), u3fVar);
            aVar2 = um1.a.n;
            txd txdVarB3 = yw9.b(2, i6k.z(txdVarE2, aVar2, 2), true);
            if (i4 == 32) {
                z6 = true;
            } else {
                z6 = false;
            }
            objX3 = pr5VarJ.x();
            if (z6) {
                objX3 = new g5a() { // from class: ej4
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj) {
                        uhj uhjVar = (uhj) obj;
                        uhjVar.getClass();
                        rhj.i(uhjVar, 0);
                        rhj.h(str, uhjVar);
                        return d6m.a;
                    }
                };
                pr5VarJ.r(objX3);
            } else {
                objX3 = new g5a() { // from class: ej4
                    @Override // defpackage.g5a
                    public final Object invoke(Object obj) {
                        uhj uhjVar = (uhj) obj;
                        uhjVar.getClass();
                        rhj.i(uhjVar, 0);
                        rhj.h(str, uhjVar);
                        return d6m.a;
                    }
                };
                pr5VarJ.r(objX3);
            }
            txd txdVarB4 = dhj.b(txdVarB3, true, (g5a) objX3);
            bmd bmdVarD2 = si3.d(um1.a.a, false);
            int iHashCode3 = Long.hashCode(pr5VarJ.T);
            xif xifVarT3 = pr5VarJ.T();
            txd txdVarC3 = er5.c(pr5VarJ, txdVarB4);
            cr5.lo.getClass();
            aVar3 = cr5.a.b;
            pr5VarJ.D();
            if (pr5VarJ.S) {
                pr5VarJ.A(aVar3);
            } else {
                pr5VarJ.p();
            }
            cr5.a.e eVar2 = cr5.a.g;
            pi9.e(pr5VarJ, eVar2, bmdVarD2);
            cr5.a.g gVar2 = cr5.a.f;
            pi9.e(pr5VarJ, gVar2, xifVarT3);
            Integer numValueOf2 = Integer.valueOf(iHashCode3);
            cr5.a.b bVar2 = cr5.a.j;
            pi9.c(pr5VarJ, numValueOf2, bVar2);
            cr5.a.C0215a c0215a2 = cr5.a.k;
            pi9.d(pr5VarJ, c0215a2);
            cr5.a.f fVar2 = cr5.a.d;
            pi9.e(pr5VarJ, fVar2, txdVarC3);
            objX4 = pr5VarJ.x();
            if (objX4 == c0249a) {
                objX4 = new fj4();
                pr5VarJ.r(objX4);
            }
            str5 = str3;
            txd txdVarA4 = z92.a(yw9.b(2, dhj.a(aVar, (g5a) objX4), false), ea2.f(250, 0, td8.a, 2), 2);
            db5 db5VarA2 = bb5.a(kj2.c, um1.a.m, pr5VarJ, 0);
            int iHashCode4 = Long.hashCode(pr5VarJ.T);
            xif xifVarT4 = pr5VarJ.T();
            txd txdVarC4 = er5.c(pr5VarJ, txdVarA4);
            pr5VarJ.D();
            if (pr5VarJ.S) {
                pr5VarJ.A(aVar3);
            } else {
                pr5VarJ.p();
            }
            pi9.e(pr5VarJ, eVar2, db5VarA2);
            pi9.e(pr5VarJ, gVar2, xifVarT4);
            k36.a(iHashCode4, pr5VarJ, bVar2, pr5VarJ, c0215a2);
            pi9.e(pr5VarJ, fVar2, txdVarC4);
            i6 = i5 >> 6;
            Object objValueOf2 = Integer.valueOf((i6 & 112) | 6);
            gb5Var = gb5.a;
            ug5Var.invoke(gb5Var, pr5VarJ, objValueOf2);
            i7 = 3;
            if (str5.length() > 0) {
                pr5VarJ.P(-1395034586);
                z7 = true;
                kil.b(str5, gb5Var.b(aVar2, i6k.f(r3f.i(aVar, 0.0f, 0.0f, 0.0f, 8.0f), 1.0f)), ((j1j) pr5VarJ.L(tkkVar)).E.g, j, null, null, vp5.a(), 0L, null, new aal(i7), 0L, 0, false, 0, 0, null, null, pr5VarJ, (i6 & 14) | ((i5 >> 9) & 57344), 0, 260968);
                str6 = str5;
                pr5VarJ.Y(false);
            } else {
                str6 = str5;
                z7 = true;
                pr5VarJ.P(-1394542554);
                pr5VarJ.Y(false);
            }
            kil.b(str, null, ((j1j) pr5VarJ.L(tkkVar)).E.g, rkl.e(15), null, null, vp5.a(), 0L, null, new aal(i7), 0L, 0, false, 0, 0, null, null, pr5VarJ, i15 | 24576, 0, 260970);
            pr5VarJ = pr5VarJ;
            pr5VarJ.Y(true);
            pr5VarJ.Y(true);
            str4 = str6;
        } else {
            pr5VarJ.G();
            str4 = str3;
        }
        kzgVarA0 = pr5VarJ.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a() { // from class: gj4
                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    lj4.b(rk4Var, str, str4, ug5Var, jjkVar, u3fVar, atjVar, j, (fr5) obj, dae.b(i | 1), i2);
                    return d6m.a;
                }
            };
        }
    }

    public static final void c(final rk4 rk4Var, final String str, final String str2, final jjk jjkVar, u3f u3fVar, atj atjVar, long j, fr5 fr5Var, final int i) {
        int i2;
        pr5 pr5Var;
        final u3f u3fVar2;
        final atj atjVar2;
        final long j2;
        int i3;
        u3f w3fVar;
        atj atjVar3;
        long jE;
        rk4Var.getClass();
        pr5 pr5VarJ = fr5Var.j(-2144162853);
        if ((i & 6) == 0) {
            i2 = (pr5VarJ.f(rk4Var.ordinal()) ? 4 : 2) | i;
        } else {
            i2 = i;
        }
        if ((i & 48) == 0) {
            i2 |= pr5VarJ.O(str) ? 32 : 16;
        }
        if ((i & 384) == 0) {
            i2 |= pr5VarJ.O(str2) ? Document.PERMITTED_OPERATION_FORM_ENTRY : 128;
        }
        if ((i & 3072) == 0) {
            i2 |= pr5VarJ.f(0) ? Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING : 1024;
        }
        if ((i & 24576) == 0) {
            i2 |= pr5VarJ.O(jjkVar) ? 16384 : 8192;
        }
        int i4 = 196608 | i2;
        if ((1572864 & i) == 0) {
            i4 = 720896 | i2;
        }
        int i5 = 12582912 | i4;
        if (pr5VarJ.q(i5 & 1, (4793491 & i5) != 4793490)) {
            pr5VarJ.z0();
            if ((i & 1) == 0 || pr5VarJ.g0()) {
                i3 = i5 & (-3670017);
                w3fVar = new w3f(20.0f, 25.0f, 20.0f, 25.0f);
                atjVar3 = br5.a;
                jE = rkl.e(15);
            } else {
                pr5VarJ.G();
                i3 = i5 & (-3670017);
                w3fVar = u3fVar;
                atjVar3 = atjVar;
                jE = j;
            }
            pr5VarJ.Z();
            pr5Var = pr5VarJ;
            b(rk4Var, str, str2, vg5.c(-1912583297, pr5VarJ, new yi4()), jjkVar, w3fVar, atjVar3, jE, pr5Var, (i3 & 14) | 3072 | (i3 & 112) | (i3 & 896) | (57344 & i3) | (458752 & i3) | (i3 & 29360128), 0);
            u3fVar2 = w3fVar;
            atjVar2 = atjVar3;
            j2 = jE;
        } else {
            pr5Var = pr5VarJ;
            pr5Var.G();
            u3fVar2 = u3fVar;
            atjVar2 = atjVar;
            j2 = j;
        }
        kzg kzgVarA0 = pr5Var.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a() { // from class: zi4
                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    lj4.c(rk4Var, str, str2, jjkVar, u3fVar2, atjVar2, j2, (fr5) obj, dae.b(i | 1));
                    return d6m.a;
                }
            };
        }
    }

    public static final void d(final rk4 rk4Var, hcf hcfVar, final jjk jjkVar, final String str, final String str2, fr5 fr5Var, final int i) {
        hcf hcfVar2;
        String strA;
        int i2;
        int i3;
        rk4Var.getClass();
        pr5 pr5VarJ = fr5Var.j(-1956418135);
        int i4 = (i & 6) == 0 ? (pr5VarJ.f(rk4Var.ordinal()) ? 4 : 2) | i : i;
        if ((i & 48) == 0) {
            hcfVar2 = hcfVar;
            i4 |= pr5VarJ.O(hcfVar2) ? 32 : 16;
        } else {
            hcfVar2 = hcfVar;
        }
        if ((i & 384) == 0) {
            i4 |= pr5VarJ.O(jjkVar) ? Document.PERMITTED_OPERATION_FORM_ENTRY : 128;
        }
        if ((i & 3072) == 0) {
            i4 |= pr5VarJ.O(str) ? Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING : 1024;
        }
        if ((i & 24576) == 0) {
            i4 |= pr5VarJ.O(str2) ? 16384 : 8192;
        }
        if (pr5VarJ.q(i4 & 1, (i4 & 9363) != 9362)) {
            int[] iArr = b.a;
            String strA2 = "";
            switch (iArr[rk4Var.ordinal()]) {
                case 1:
                    pr5VarJ.P(-946116837);
                    if (str == null) {
                        strA = q04.a(pr5VarJ, 1077859565, R.string.tap_to_start_coachmark_message_initial, pr5VarJ, false);
                    } else {
                        pr5VarJ.P(1077858759);
                        pr5VarJ.Y(false);
                        strA = str;
                    }
                    pr5VarJ.Y(false);
                    break;
                case 2:
                    pr5VarJ.P(1077863256);
                    strA = hcfVar2.getValue() == Page.CaptureMode.ID_CARD ? q04.a(pr5VarJ, 1077864772, R.string.auto_capture_off, pr5VarJ, false) : q04.a(pr5VarJ, 1077866852, R.string.manual_mode_coachmark_message, pr5VarJ, false);
                    pr5VarJ.Y(false);
                    break;
                case 3:
                    i2 = 1077870115;
                    i3 = R.string.auto_capture_on_hint_message;
                    strA = q04.a(pr5VarJ, i2, i3, pr5VarJ, false);
                    break;
                case 4:
                    i2 = 1077873613;
                    i3 = R.string.book_mode_auto_capture_on_hint_message;
                    strA = q04.a(pr5VarJ, i2, i3, pr5VarJ, false);
                    break;
                case 5:
                    i2 = 1077877474;
                    i3 = R.string.waiting_for_camera_to_start;
                    strA = q04.a(pr5VarJ, i2, i3, pr5VarJ, false);
                    break;
                case 6:
                    pr5VarJ.P(-945431117);
                    pr5VarJ.Y(false);
                    strA = str2 != null ? str2 : "";
                    break;
                default:
                    pr5VarJ.P(-945377675);
                    pr5VarJ.Y(false);
                    break;
            }
            int i5 = iArr[rk4Var.ordinal()];
            if (i5 == 2) {
                pr5VarJ.P(1077885298);
                if (hcfVar2.getValue() == Page.CaptureMode.ID_CARD) {
                    pr5VarJ.P(-945246731);
                    pr5VarJ.Y(false);
                } else {
                    strA2 = q04.a(pr5VarJ, 1077887127, R.string.auto_capture_off, pr5VarJ, false);
                }
                pr5VarJ.Y(false);
            } else if (i5 == 3 || i5 == 4) {
                strA2 = q04.a(pr5VarJ, 1077891254, R.string.auto_capture_on, pr5VarJ, false);
            } else {
                pr5VarJ.P(-945049323);
                pr5VarJ.Y(false);
            }
            c(rk4Var, strA, strA2, jjkVar, null, null, 0L, pr5VarJ, (i4 & 14) | 3072 | ((i4 << 6) & 57344));
        } else {
            pr5VarJ.G();
        }
        kzg kzgVarA0 = pr5VarJ.a0();
        if (kzgVarA0 != null) {
            final hcf hcfVar3 = hcfVar2;
            kzgVarA0.d = new u5a() { // from class: bj4
                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    lj4.d(rk4Var, hcfVar3, jjkVar, str, str2, (fr5) obj, dae.b(i | 1));
                    return d6m.a;
                }
            };
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static final void e(final k0d k0dVar, final jjk jjkVar, hcf hcfVar, oh7 oh7Var, jjk jjkVar2, final j0d j0dVar, fr5 fr5Var, final int i) {
        int i2;
        final hcf hcfVar2;
        final oh7 oh7Var2;
        final jjk jjkVar3;
        int i3;
        int i4;
        long j;
        k0dVar.getClass();
        j0dVar.getClass();
        pr5 pr5VarJ = fr5Var.j(632157403);
        if ((i & 6) == 0) {
            i2 = (pr5VarJ.f(k0dVar.ordinal()) ? 4 : 2) | i;
        } else {
            i2 = i;
        }
        if ((i & 48) == 0) {
            i2 |= pr5VarJ.O(jjkVar) ? 32 : 16;
        }
        if ((i & 384) == 0) {
            hcfVar2 = hcfVar;
            i2 |= pr5VarJ.O(hcfVar2) ? Document.PERMITTED_OPERATION_FORM_ENTRY : 128;
        } else {
            hcfVar2 = hcfVar;
        }
        if ((i & 3072) == 0) {
            oh7Var2 = oh7Var;
            i2 |= pr5VarJ.O(oh7Var2) ? Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING : 1024;
        } else {
            oh7Var2 = oh7Var;
        }
        if ((i & 24576) == 0) {
            jjkVar3 = jjkVar2;
            i2 |= pr5VarJ.O(jjkVar3) ? 16384 : 8192;
        } else {
            jjkVar3 = jjkVar2;
        }
        if ((196608 & i) == 0) {
            i2 |= pr5VarJ.O(j0dVar) ? 131072 : 65536;
        }
        if (pr5VarJ.q(i2 & 1, (74899 & i2) != 74898)) {
            Page.CaptureMode captureMode = (Page.CaptureMode) hcfVar2.getValue();
            bo9 bo9Var = (bo9) oh7Var2.getValue();
            boolean zBooleanValue = ((Boolean) jjkVar3.getValue()).booleanValue();
            int[] iArr = b.b;
            switch (iArr[k0dVar.ordinal()]) {
                case 1:
                    if (captureMode == Page.CaptureMode.BUSINESS_CARD) {
                        i3 = R.string.live_hint_looking_for_business_card;
                    } else {
                        i3 = captureMode == Page.CaptureMode.BOOK ? R.string.live_hint_looking_for_book : R.string.live_hint_looking_for_document;
                    }
                    i4 = i3;
                    break;
                case 2:
                    i3 = R.string.live_hint_center_whiteboard;
                    i4 = i3;
                    break;
                case 3:
                    i3 = R.string.live_hint_id_card_first;
                    i4 = i3;
                    break;
                case 4:
                    i3 = R.string.live_hint_id_card_second;
                    i4 = i3;
                    break;
                case 5:
                    i3 = R.string.live_hint_ready_to_capture;
                    i4 = i3;
                    break;
                case 6:
                    int i5 = b.c[captureMode.ordinal()];
                    if (i5 == 1) {
                        i3 = R.string.live_hint_no_business_card;
                    } else if (i5 != 2) {
                        i3 = i5 != 3 ? R.string.live_hint_no_document : R.string.live_hint_no_book;
                    } else {
                        i3 = R.string.live_hint_no_id_card;
                    }
                    i4 = i3;
                    break;
                case 7:
                    i3 = bo9Var.b;
                    i4 = i3;
                    break;
                case 8:
                    i3 = zBooleanValue ? R.string.auto_capture_on : R.string.auto_capture_off;
                    i4 = i3;
                    break;
                default:
                    i4 = 0;
                    break;
            }
            switch (iArr[k0dVar.ordinal()]) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    j = CrashConfig.DEFAULT_ANR_WATCHDOG_INTERVAL;
                    break;
                default:
                    j = 1000;
                    break;
            }
            if (k0dVar == k0d.IdCardHintFirstPage || k0dVar == k0d.IdCardHintSecondPage) {
                long j2 = j;
                pr5VarJ.P(-829657239);
                f(i4, jjkVar, j2, br5.c, new w3f(16.0f, 24.0f, 24.0f, 24.0f), j0dVar.a, pr5VarJ, (i2 & 112) | 24576, 0);
                pr5VarJ.Y(false);
            } else {
                pr5VarJ.P(-829496132);
                f(i4, jjkVar, j, null, null, j0dVar.a, pr5VarJ, i2 & 112, 24);
                pr5VarJ.Y(false);
            }
        } else {
            pr5VarJ.G();
        }
        kzg kzgVarA0 = pr5VarJ.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a() { // from class: aj4
                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    lj4.e(k0dVar, jjkVar, hcfVar2, oh7Var2, jjkVar3, j0dVar, (fr5) obj, dae.b(i | 1));
                    return d6m.a;
                }
            };
        }
    }

    /* JADX WARN: Code duplicated, block: B:101:0x0148  */
    /* JADX WARN: Code duplicated, block: B:102:0x014a  */
    /* JADX WARN: Code duplicated, block: B:106:0x0158  */
    /* JADX WARN: Code duplicated, block: B:110:0x01a5  */
    /* JADX WARN: Code duplicated, block: B:112:0x020f  */
    /* JADX WARN: Code duplicated, block: B:115:0x021b  */
    /* JADX WARN: Code duplicated, block: B:117:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:118:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:48:0x007f  */
    /* JADX WARN: Code duplicated, block: B:50:0x0087  */
    /* JADX WARN: Code duplicated, block: B:51:0x0089  */
    /* JADX WARN: Code duplicated, block: B:53:0x008d  */
    /* JADX WARN: Code duplicated, block: B:56:0x0099  */
    /* JADX WARN: Code duplicated, block: B:57:0x009b  */
    /* JADX WARN: Code duplicated, block: B:60:0x00a4  */
    /* JADX WARN: Code duplicated, block: B:69:0x00be  */
    /* JADX WARN: Code duplicated, block: B:71:0x00c2  */
    /* JADX WARN: Code duplicated, block: B:72:0x00c7  */
    /* JADX WARN: Code duplicated, block: B:74:0x00ca  */
    /* JADX WARN: Code duplicated, block: B:78:0x00db  */
    /* JADX WARN: Code duplicated, block: B:80:0x00e1  */
    /* JADX WARN: Code duplicated, block: B:82:0x00ed  */
    /* JADX WARN: Code duplicated, block: B:84:0x00f5  */
    /* JADX WARN: Code duplicated, block: B:87:0x010d  */
    /* JADX WARN: Code duplicated, block: B:88:0x010f  */
    /* JADX WARN: Code duplicated, block: B:91:0x0116  */
    /* JADX WARN: Code duplicated, block: B:92:0x0118  */
    /* JADX WARN: Code duplicated, block: B:96:0x0122  */
    public static final void f(final int i, final jjk<Float> jjkVar, final long j, atj atjVar, u3f u3fVar, final e5a<d6m> e5aVar, fr5 fr5Var, final int i2, final int i3) {
        int i4;
        final atj atjVar2;
        u3f w3fVar;
        e5a<d6m> e5aVar2;
        boolean z;
        pr5 pr5Var;
        final u3f u3fVar2;
        kzg kzgVarA0;
        atj atjVar3;
        final u3f u3fVar3;
        Object objX;
        fr5.a.C0249a c0249a;
        o62 o62Var;
        boolean z2;
        boolean z3;
        boolean z4;
        Object objX2;
        String strD;
        boolean z5;
        boolean z6;
        Object objX3;
        int i5;
        boolean zO;
        Object objX4;
        kzg kzgVarA1;
        int i6;
        pr5 pr5VarJ = fr5Var.j(1719289666);
        if ((i2 & 6) == 0) {
            i4 = (pr5VarJ.f(i) ? 4 : 2) | i2;
        } else {
            i4 = i2;
        }
        if ((i2 & 48) == 0) {
            i4 |= pr5VarJ.O(jjkVar) ? 32 : 16;
        }
        if ((i2 & 384) == 0) {
            i4 |= pr5VarJ.g(j) ? 256 : 128;
        }
        if ((i2 & 3072) == 0) {
            if ((i3 & 8) == 0) {
                atjVar2 = atjVar;
                int i7 = pr5VarJ.O(atjVar2) ? Document.PERMITTED_OPERATION_HIGH_QUALITY_PRINTING : 1024;
                i4 |= i7;
            } else {
                atjVar2 = atjVar;
            }
            i4 |= i7;
        } else {
            atjVar2 = atjVar;
        }
        int i8 = i3 & 16;
        if (i8 == 0) {
            if ((i2 & 24576) == 0) {
                w3fVar = u3fVar;
                i4 |= pr5VarJ.O(w3fVar) ? 16384 : 8192;
            }
            if ((196608 & i2) == 0) {
                e5aVar2 = e5aVar;
                if (pr5VarJ.z(e5aVar2)) {
                    i6 = 131072;
                } else {
                    i6 = 65536;
                }
                i4 |= i6;
            } else {
                e5aVar2 = e5aVar;
            }
            if ((74899 & i4) != 74898) {
                z = true;
            } else {
                z = false;
            }
            if (pr5VarJ.q(i4 & 1, z)) {
                pr5VarJ.z0();
                if ((i2 & 1) != 0 || pr5VarJ.g0()) {
                    if ((i3 & 8) != 0) {
                        atjVar3 = br5.d;
                        i4 &= -7169;
                    } else {
                        atjVar3 = atjVar2;
                    }
                    if (i8 != 0) {
                        w3fVar = new w3f(20.0f, 12.0f, 20.0f, 12.0f);
                    }
                    atjVar2 = atjVar3;
                } else {
                    pr5VarJ.G();
                    if ((i3 & 8) != 0) {
                        i4 &= -7169;
                    }
                }
                u3fVar3 = w3fVar;
                pr5VarJ.Z();
                if (i == 0) {
                    kzgVarA1 = pr5VarJ.a0();
                    if (kzgVarA1 != null) {
                        final e5a<d6m> e5aVar3 = e5aVar2;
                        kzgVarA1.d = new u5a() { // from class: hj4
                            @Override // defpackage.u5a
                            public final Object invoke(Object obj, Object obj2) {
                                ((Integer) obj2).getClass();
                                lj4.f(i, jjkVar, j, atjVar2, u3fVar3, e5aVar3, (fr5) obj, dae.b(i2 | 1), i3);
                                return d6m.a;
                            }
                        };
                        return;
                    }
                    return;
                }
                objX = pr5VarJ.x();
                c0249a = fr5.a.a;
                if (objX == c0249a) {
                    objX = w62.a(1.0f);
                    pr5VarJ.r(objX);
                }
                o62Var = (o62) objX;
                Integer numValueOf = Integer.valueOf(i);
                boolean z7 = pr5VarJ.z(o62Var);
                if ((i4 & 896) == 256) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                boolean z8 = z2 | z7;
                if ((458752 & i4) == 131072) {
                    z3 = true;
                } else {
                    z3 = false;
                }
                z4 = z8 | z3;
                objX2 = pr5VarJ.x();
                if (z4 || objX2 == c0249a) {
                    a aVar = new a(o62Var, j, e5aVar, null);
                    pr5VarJ.r(aVar);
                    objX2 = aVar;
                }
                oi8.d(pr5VarJ, (u5a) objX2, numValueOf);
                strD = ebc.d(pr5VarJ, i);
                String strD2 = ebc.d(pr5VarJ, i);
                if ((i4 & 112) == 32) {
                    z5 = true;
                } else {
                    z5 = false;
                }
                z6 = z5 | pr5VarJ.z(o62Var);
                objX3 = pr5VarJ.x();
                if (z6 || objX3 == c0249a) {
                    objX3 = new ij4(0, jjkVar, o62Var);
                    pr5VarJ.r(objX3);
                }
                txd txdVarA = vra.a(txd.a.a, (g5a) objX3);
                tkk tkkVar = l1j.g;
                i5 = 0;
                txd txdVarB = yw9.b(2, z92.a(i6k.z(r3f.e(i6k.p(z23.c(txdVarA, ((j1j) pr5VarJ.L(tkkVar)).E.f, atjVar2)), u3fVar3), um1.a.n, 2), ea2.f(250, 0, td8.a, 2), 2), true);
                zO = pr5VarJ.O(strD);
                objX4 = pr5VarJ.x();
                if (zO || objX4 == c0249a) {
                    objX4 = new wi4(i5, strD);
                    pr5VarJ.r(objX4);
                }
                pr5Var = pr5VarJ;
                kil.b(strD2, dhj.b(txdVarB, true, (g5a) objX4), ((j1j) pr5VarJ.L(tkkVar)).E.g, rkl.e(13), null, null, vp5.a(), 0L, null, new aal(3), 0L, 0, false, 0, 0, null, null, pr5Var, 24576, 0, 260968);
                atjVar2 = atjVar2;
                u3fVar2 = u3fVar3;
            } else {
                pr5Var = pr5VarJ;
                pr5Var.G();
                u3fVar2 = w3fVar;
            }
            kzgVarA0 = pr5Var.a0();
            if (kzgVarA0 != null) {
                kzgVarA0.d = new u5a() { // from class: xi4
                    @Override // defpackage.u5a
                    public final Object invoke(Object obj, Object obj2) {
                        ((Integer) obj2).getClass();
                        lj4.f(i, jjkVar, j, atjVar2, u3fVar2, e5aVar, (fr5) obj, dae.b(i2 | 1), i3);
                        return d6m.a;
                    }
                };
            }
        }
        i4 |= 24576;
        w3fVar = u3fVar;
        if ((196608 & i2) == 0) {
            e5aVar2 = e5aVar;
            if (pr5VarJ.z(e5aVar2)) {
                i6 = 131072;
            } else {
                i6 = 65536;
            }
            i4 |= i6;
        } else {
            e5aVar2 = e5aVar;
        }
        if ((74899 & i4) != 74898) {
            z = true;
        } else {
            z = false;
        }
        if (pr5VarJ.q(i4 & 1, z)) {
            pr5VarJ.z0();
            if ((i2 & 1) != 0) {
                if ((i3 & 8) != 0) {
                    atjVar3 = br5.d;
                    i4 &= -7169;
                } else {
                    atjVar3 = atjVar2;
                }
                if (i8 != 0) {
                    w3fVar = new w3f(20.0f, 12.0f, 20.0f, 12.0f);
                }
                atjVar2 = atjVar3;
            } else {
                if ((i3 & 8) != 0) {
                    atjVar3 = br5.d;
                    i4 &= -7169;
                } else {
                    atjVar3 = atjVar2;
                }
                if (i8 != 0) {
                    w3fVar = new w3f(20.0f, 12.0f, 20.0f, 12.0f);
                }
                atjVar2 = atjVar3;
            }
            u3fVar3 = w3fVar;
            pr5VarJ.Z();
            if (i == 0) {
                kzgVarA1 = pr5VarJ.a0();
                if (kzgVarA1 != null) {
                    final e5a e5aVar4 = e5aVar2;
                    kzgVarA1.d = new u5a() { // from class: hj4
                        @Override // defpackage.u5a
                        public final Object invoke(Object obj, Object obj2) {
                            ((Integer) obj2).getClass();
                            lj4.f(i, jjkVar, j, atjVar2, u3fVar3, e5aVar4, (fr5) obj, dae.b(i2 | 1), i3);
                            return d6m.a;
                        }
                    };
                    return;
                }
                return;
            }
            objX = pr5VarJ.x();
            c0249a = fr5.a.a;
            if (objX == c0249a) {
                objX = w62.a(1.0f);
                pr5VarJ.r(objX);
            }
            o62Var = (o62) objX;
            Integer numValueOf2 = Integer.valueOf(i);
            boolean z9 = pr5VarJ.z(o62Var);
            if ((i4 & 896) == 256) {
                z2 = true;
            } else {
                z2 = false;
            }
            boolean z10 = z2 | z9;
            if ((458752 & i4) == 131072) {
                z3 = true;
            } else {
                z3 = false;
            }
            z4 = z10 | z3;
            objX2 = pr5VarJ.x();
            if (z4) {
                a aVar2 = new a(o62Var, j, e5aVar, null);
                pr5VarJ.r(aVar2);
                objX2 = aVar2;
            } else {
                a aVar3 = new a(o62Var, j, e5aVar, null);
                pr5VarJ.r(aVar3);
                objX2 = aVar3;
            }
            oi8.d(pr5VarJ, (u5a) objX2, numValueOf2);
            strD = ebc.d(pr5VarJ, i);
            String strD3 = ebc.d(pr5VarJ, i);
            if ((i4 & 112) == 32) {
                z5 = true;
            } else {
                z5 = false;
            }
            z6 = z5 | pr5VarJ.z(o62Var);
            objX3 = pr5VarJ.x();
            if (z6) {
                objX3 = new ij4(0, jjkVar, o62Var);
                pr5VarJ.r(objX3);
            } else {
                objX3 = new ij4(0, jjkVar, o62Var);
                pr5VarJ.r(objX3);
            }
            txd txdVarA2 = vra.a(txd.a.a, (g5a) objX3);
            tkk tkkVar2 = l1j.g;
            i5 = 0;
            txd txdVarB2 = yw9.b(2, z92.a(i6k.z(r3f.e(i6k.p(z23.c(txdVarA2, ((j1j) pr5VarJ.L(tkkVar2)).E.f, atjVar2)), u3fVar3), um1.a.n, 2), ea2.f(250, 0, td8.a, 2), 2), true);
            zO = pr5VarJ.O(strD);
            objX4 = pr5VarJ.x();
            if (zO) {
                objX4 = new wi4(i5, strD);
                pr5VarJ.r(objX4);
            } else {
                objX4 = new wi4(i5, strD);
                pr5VarJ.r(objX4);
            }
            pr5Var = pr5VarJ;
            kil.b(strD3, dhj.b(txdVarB2, true, (g5a) objX4), ((j1j) pr5VarJ.L(tkkVar2)).E.g, rkl.e(13), null, null, vp5.a(), 0L, null, new aal(3), 0L, 0, false, 0, 0, null, null, pr5Var, 24576, 0, 260968);
            atjVar2 = atjVar2;
            u3fVar2 = u3fVar3;
        } else {
            pr5Var = pr5VarJ;
            pr5Var.G();
            u3fVar2 = w3fVar;
        }
        kzgVarA0 = pr5Var.a0();
        if (kzgVarA0 != null) {
            kzgVarA0.d = new u5a() { // from class: xi4
                @Override // defpackage.u5a
                public final Object invoke(Object obj, Object obj2) {
                    ((Integer) obj2).getClass();
                    lj4.f(i, jjkVar, j, atjVar2, u3fVar2, e5aVar, (fr5) obj, dae.b(i2 | 1), i3);
                    return d6m.a;
                }
            };
        }
    }
}
