package com.adobe.dcmscan.screens.reorder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.adobe.dcmscan.MarkupActivity;
import com.adobe.dcmscan.MarkupShapesActivity;
import com.adobe.dcmscan.screens.reorder.ReorderActivity;
import defpackage.c73;
import defpackage.h9;
import defpackage.i6h;
import defpackage.wtl;
import java.util.HashMap;
import java.util.List;

/* JADX INFO: compiled from: ReorderActivity.kt */
/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes2.dex */
public final class ReorderActivity extends c73 {
    public static final /* synthetic */ int I = 0;
    public final b3l E = new b3l(new v8(2));
    public final b3l F = new b3l(new e5a() { // from class: w5h
        public final Object invoke() {
            int i = ReorderActivity.I;
            RecyclerView recyclerViewInflate = this.a.getLayoutInflater().inflate(2131624211, (ViewGroup) null, false);
            if (recyclerViewInflate != null) {
                RecyclerView recyclerView = recyclerViewInflate;
                return new i6h(recyclerView, recyclerView);
            }
            r5o.a("rootView");
            return null;
        }
    });
    public final amm G = new amm(g2h.a(com.adobe.dcmscan.screens.reorder.c.class), new c(), new x8(2), new d());
    public final wtl H;

    /* JADX INFO: compiled from: ReorderActivity.kt */
    public static final class a {
        public static Intent a(Context context) {
            return new Intent(context, (Class<?>) ReorderActivity.class);
        }
    }

    /* JADX INFO: compiled from: ReorderActivity.kt */
    public static final class b implements zne, e6a {
        public final /* synthetic */ g5a a;

        public b(g5a g5aVar) {
            this.a = g5aVar;
        }

        public final /* synthetic */ void a(Object obj) {
            this.a.invoke(obj);
        }

        public final v5a<?> b() {
            return this.a;
        }

        public final boolean equals(Object obj) {
            if ((obj instanceof zne) && (obj instanceof e6a)) {
                return this.a.equals(((e6a) obj).b());
            }
            return false;
        }

        public final int hashCode() {
            return this.a.hashCode();
        }
    }

    /* JADX INFO: compiled from: ActivityViewModelLazy.kt */
    public static final class c implements e5a<dmm> {
        public c() {
        }

        public final Object invoke() {
            return ReorderActivity.this.getViewModelStore();
        }
    }

    /* JADX INFO: compiled from: ActivityViewModelLazy.kt */
    public static final class d implements e5a<yc6> {
        public d() {
        }

        public final Object invoke() {
            return ReorderActivity.this.getDefaultViewModelCreationExtras();
        }
    }

    public ReorderActivity() {
        final int i = 1;
        this.H = new wtl(new e5a() { // from class: egd
            /* JADX WARN: Type inference failed for: r2v2, types: [android.content.Context, com.adobe.dcmscan.MarkupActivity, com.adobe.dcmscan.a] */
            public final Object invoke() {
                int i2 = i;
                pe2 pe2Var = this;
                switch (i2) {
                    case 0:
                        ?? r2 = (MarkupActivity) pe2Var;
                        List<cuj> list = MarkupActivity.W;
                        Intent intent = new Intent((Context) r2, (Class<?>) MarkupShapesActivity.class);
                        r2.r0(intent);
                        r2.q0(r2.V, intent);
                        sw6.f.p().f("DCMScan:Operation:Shapes See All Tapped", (HashMap) null);
                        break;
                    default:
                        int i3 = ReorderActivity.I;
                        ((ReorderActivity) pe2Var).getOnBackPressedDispatcher().c();
                        break;
                }
                return d6m.a;
            }
        }, (e5a) new wk7(2, this), (e5a) null, (e5a) null, 28);
    }

    public final i6h b0() {
        return (i6h) this.F.getValue();
    }

    @Override // defpackage.c73
    /* JADX INFO: renamed from: c0, reason: merged with bridge method [inline-methods] */
    public final com.adobe.dcmscan.screens.reorder.c a0() {
        return (com.adobe.dcmscan.screens.reorder.c) this.G.getValue();
    }

    @Override // defpackage.c73
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        tf5.a(this, new ug5(-226432175, true, new u5a() { // from class: t5h
            public final Object invoke(Object obj, Object obj2) {
                fr5 fr5Var = (fr5) obj;
                int iIntValue = ((Integer) obj2).intValue();
                int i = ReorderActivity.I;
                if (fr5Var.q(iIntValue & 1, (iIntValue & 3) != 2)) {
                    final ReorderActivity reorderActivity = this.a;
                    gr0.a(false, vg5.c(-1855828230, fr5Var, new u5a() { // from class: z5h
                        public final Object invoke(Object obj3, Object obj4) {
                            fr5 fr5Var2 = (fr5) obj3;
                            int iIntValue2 = ((Integer) obj4).intValue();
                            int i2 = ReorderActivity.I;
                            if (fr5Var2.q(iIntValue2 & 1, (iIntValue2 & 3) != 2)) {
                                final ReorderActivity reorderActivity2 = reorderActivity;
                                p6h.a(reorderActivity2.H, vg5.c(1086296051, fr5Var2, new w5a() { // from class: u5h
                                    public final Object invoke(Object obj5, Object obj6, Object obj7) {
                                        y68 y68Var = (y68) obj5;
                                        fr5 fr5Var3 = (fr5) obj6;
                                        int iIntValue3 = ((Integer) obj7).intValue();
                                        int i3 = ReorderActivity.I;
                                        if ((iIntValue3 & 6) == 0) {
                                            iIntValue3 |= fr5Var3.d(y68Var.a) ? 4 : 2;
                                        }
                                        if (fr5Var3.q(iIntValue3 & 1, (iIntValue3 & 19) != 18)) {
                                            final int iP0 = ((sg7) fr5Var3.L(ys5.h)).p0(y68Var.a);
                                            final ReorderActivity reorderActivity3 = reorderActivity2;
                                            boolean z = fr5Var3.z(reorderActivity3);
                                            Object objX = fr5Var3.x();
                                            a aVar = a.a;
                                            if (z || objX == aVar) {
                                                objX = new j8(1, reorderActivity3);
                                                fr5Var3.r(objX);
                                            }
                                            g5a g5aVar = (g5a) objX;
                                            txd txdVarD = i6k.d(a.a, 1.0f);
                                            boolean z2 = fr5Var3.z(reorderActivity3) | fr5Var3.f(iP0);
                                            Object objX2 = fr5Var3.x();
                                            if (z2 || objX2 == aVar) {
                                                objX2 = new g5a() { // from class: v5h
                                                    public final Object invoke(Object obj8) {
                                                        int i4 = ReorderActivity.I;
                                                        ((RecyclerView) obj8).getClass();
                                                        RecyclerView recyclerView = reorderActivity3.b0().a;
                                                        recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(), recyclerView.getPaddingRight(), iP0);
                                                        return d6m.a;
                                                    }
                                                };
                                                fr5Var3.r(objX2);
                                            }
                                            i62.a(48, 0, fr5Var3, g5aVar, (g5a) objX2, txdVarD);
                                        } else {
                                            fr5Var3.G();
                                        }
                                        return d6m.a;
                                    }
                                }), fr5Var2, 48);
                            } else {
                                fr5Var2.G();
                            }
                            return d6m.a;
                        }
                    }), fr5Var, 48, 1);
                } else {
                    fr5Var.G();
                }
                return d6m.a;
            }
        }));
        b3l b3lVar = this.E;
        ((f2b) b3lVar.getValue()).getClass();
        int i = f2b.m(this).x;
        a0().e = i / (i / getResources().getDimensionPixelSize(2131168726));
        ((f2b) b3lVar.getValue()).getClass();
        b0().b.setLayoutManager(new GridLayoutManager(f2b.m(this).x / getResources().getDimensionPixelSize(2131168726)));
        a0().j.e(this, new b(new h9(1, this)));
        a0().k.e(this, new b(new g5a() { // from class: x5h
            public final Object invoke(Object obj) {
                Boolean bool = (Boolean) obj;
                int i2 = ReorderActivity.I;
                bool.getClass();
                boolean zBooleanValue = bool.booleanValue();
                pe2 pe2Var = this.a;
                if (zBooleanValue) {
                    pe2Var.setResult(-1, new Intent());
                    pe2Var.finish();
                } else {
                    pe2Var.getOnBackPressedDispatcher().c();
                }
                return d6m.a;
            }
        }));
        a0().l.e(this, new b(new g5a() { // from class: y5h
            public final Object invoke(Object obj) {
                int i2 = ReorderActivity.I;
                long jUptimeMillis = SystemClock.uptimeMillis();
                this.a.b0().b.onTouchEvent(MotionEvent.obtain(jUptimeMillis, jUptimeMillis, 3, 0.0f, 0.0f, 0));
                return d6m.a;
            }
        }));
    }
}
