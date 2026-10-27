package com.adobe.dcmscan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.adobe.dcmscan.ReviewActivity;
import com.adobe.dcmscan.document.Page;
import com.adobe.dcmscan.document.b;
import com.adobe.dcmscan.document.n;
import com.adobe.dcmscan.y;
import defpackage.a31;
import defpackage.cub;
import defpackage.d6m;
import defpackage.e8n;
import defpackage.f2b;
import defpackage.g5a;
import defpackage.h31;
import defpackage.iac;
import defpackage.m59;
import defpackage.ngc;
import defpackage.p2j;
import defpackage.qii;
import defpackage.s26;
import defpackage.sw6;
import defpackage.tw6;
import defpackage.tz3;
import defpackage.uz3;
import defpackage.v21;
import defpackage.vz3;
import defpackage.w21;
import defpackage.x75;
import defpackage.zvi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import kotlin.UninitializedPropertyAccessException;

/* JADX INFO: compiled from: ReviewActivity.kt */
/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes.dex */
public final class ReviewActivity extends a {
    public static final /* synthetic */ int X = 0;
    public final h31 Q;
    public final h31 R;
    public final h31 S;
    public int P = -1;
    public final h31 T = u0(new g5a() { // from class: cdh
        @Override // defpackage.g5a
        public final Object invoke(Object obj) {
            b bVar;
            v21 v21Var = (v21) obj;
            int i = ReviewActivity.X;
            v21Var.getClass();
            int i2 = v21Var.a;
            ReviewActivity reviewActivity = this.a;
            if (i2 == -1 && (bVar = reviewActivity.E) != null) {
                bVar.o = true;
            }
            ReviewActivity.B0(reviewActivity, true, 4);
            return d6m.a;
        }
    }, false);
    public final h31 U = u0(new g5a() { // from class: ddh
        @Override // defpackage.g5a
        public final Object invoke(Object obj) {
            b bVar;
            v21 v21Var = (v21) obj;
            int i = ReviewActivity.X;
            v21Var.getClass();
            int i2 = v21Var.a;
            ReviewActivity reviewActivity = this.a;
            if (i2 == -1 && (bVar = reviewActivity.E) != null) {
                bVar.p = true;
            }
            ReviewActivity.B0(reviewActivity, true, 4);
            return d6m.a;
        }
    }, false);
    public final h31 V = u0(new g5a() { // from class: edh
        @Override // defpackage.g5a
        public final Object invoke(Object obj) {
            v21 v21Var = (v21) obj;
            int i = ReviewActivity.X;
            v21Var.getClass();
            Intent intent = v21Var.b;
            int i2 = v21Var.a;
            ReviewActivity reviewActivity = this.a;
            if (i2 == -1 && intent != null) {
                reviewActivity.d0(intent, false);
            } else if (reviewActivity.o0().i == y.e.PHOTO_LIBRARY) {
                if (reviewActivity.m0() == 0) {
                    reviewActivity.setResult(1, null);
                    reviewActivity.finish();
                } else {
                    ReviewActivity.B0(reviewActivity, true, 4);
                }
            }
            return d6m.a;
        }
    }, false);
    public final h31 W = (h31) V(new a31(), new w21() { // from class: fdh
        @Override // defpackage.w21
        public final void a(Object obj) {
            boolean zBooleanValue = ((Boolean) obj).booleanValue();
            int i = ReviewActivity.X;
            this.a.E0(zBooleanValue);
        }
    });

    public ReviewActivity() {
        int i = 1;
        this.Q = u0(new tz3(i, this), false);
        this.R = u0(new uz3(i, this), true);
        this.S = u0(new vz3(i, this), false);
    }

    public static /* synthetic */ void B0(ReviewActivity reviewActivity, boolean z, int i) {
        reviewActivity.A0(z, null, (i & 2) == 0);
    }

    public final void A0(boolean z, String str, boolean z2) {
        Intent intent = new Intent(this, (Class<?>) CaptureActivity.class);
        r0(intent);
        if (!z) {
            intent.putExtra("takeAnotherPhoto", false);
        }
        if (z2) {
            intent.putExtra("StartInReviewMode", z2);
        }
        if (str != null) {
            intent.putExtra("ImportErrorMessage", str);
        }
        q0(this.R, intent);
    }

    public final void C0() {
        com.adobe.dcmscan.document.b bVar = this.E;
        HashMap<String, Object> map = new HashMap<>();
        y yVarO0 = o0();
        sw6.a aVar = sw6.f;
        sw6.a.w(bVar, map);
        map.put("adb.event.context.auto_launched", yVarO0.j ? "Yes" : "No");
        sw6.a.v(bVar, map);
        aVar.p().f("DCMScan:Lifecycle:Cancel", map);
        setResult(1, null);
        finish();
    }

    public final void D0() {
        sw6 sw6VarP = sw6.f.p();
        a0 a0Var = this.G;
        if (a0Var == null || a0Var.s) {
            return;
        }
        sw6VarP.f("DCMScan:Workflow:Review", null);
        a0Var.s = true;
    }

    public final void E0(boolean z) {
        if (z) {
            pkf.f(pkf.a, this, this.V, m0());
        } else {
            if (m0() <= 0) {
                C0();
                return;
            }
            LinkedHashMap linkedHashMap = rif.a;
            pkf.a.getClass();
            rif.c(pkf.g(), this);
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: kotlin.UninitializedPropertyAccessException */
    @Override // com.adobe.dcmscan.a
    public final m59 h0() throws UninitializedPropertyAccessException {
        cub.f("viewModel");
        throw null;
    }

    @Override // com.adobe.dcmscan.a
    public final h31 j0() {
        return this.Q;
    }

    @Override // com.adobe.dcmscan.a, androidx.fragment.app.f, defpackage.of5, defpackage.sf5, android.app.Activity
    public final void onCreate(Bundle bundle) {
        int i;
        Page pageD;
        if (zvi.a) {
            Log.d("com.adobe.dcmscan.ReviewActivity", "onCreate");
        }
        super.onCreate(bundle);
        if (isFinishing()) {
            return;
        }
        p2j.a.getClass();
        if (p2j.c.size() == 0) {
            finish();
            return;
        }
        if (bundle != null) {
            this.P = bundle.getInt("anchorPageIndex", -1);
            y0(bundle.getInt("currentPageIndex", 0));
        }
        if (bundle == null) {
            com.adobe.dcmscan.document.b bVar = this.E;
            if (bVar != null) {
                this.P = bVar.c() - 1;
                Iterator<Page> it = bVar.g().iterator();
                while (true) {
                    boolean zHasNext = it.hasNext();
                    h31 h31Var = this.S;
                    h31 h31Var2 = this.U;
                    if (!zHasNext) {
                        a0 a0Var = this.G;
                        ngc ngcVar = a0Var != null ? a0Var.x : null;
                        if (ngcVar != null && (pageD = bVar.d((i = ngcVar.a))) != null) {
                            int i2 = a.a[ngcVar.c.ordinal()];
                            if (i2 == 1) {
                                int i3 = ngcVar.b;
                                n nVar = (n) x75.P(i3, pageD.v());
                                if (nVar == null) {
                                    break;
                                }
                                y0(i);
                                c0(nVar.G(), i3, h31Var);
                                return;
                            }
                            if (i2 != 2) {
                                break;
                            }
                            y0(i);
                            f0(pageD.A(), h31Var2);
                            return;
                        }
                        break;
                        break;
                    }
                    Page next = it.next();
                    if (!TextUtils.isEmpty(next.A())) {
                        y0(bVar.f(next));
                        f0(next.A(), h31Var2);
                        return;
                    }
                    int size = next.v().size();
                    for (int i4 = 0; i4 < size; i4++) {
                        if (!TextUtils.isEmpty(((n) next.v().get(i4)).G())) {
                            y0(bVar.f(next));
                            c0(((n) next.v().get(i4)).G(), i4, h31Var);
                            return;
                        } else {
                            if (!TextUtils.isEmpty(((n) next.v().get(i4)).S())) {
                                y0(bVar.f(next));
                                e0(((n) next.v().get(i4)).S(), i4, this.T);
                                return;
                            }
                        }
                    }
                }
            }
            y.e eVar = o0().i;
            if (y.e.PHOTO_LIBRARY == eVar) {
                sw6.f.p().f("DCMScan:Workflow:Add from Document Detection", null);
                if (bVar != null) {
                    tw6.a(bVar, "Library");
                }
                pkf pkfVar = pkf.a;
                pkfVar.getClass();
                if (mif.a(this, pkf.g()) != 0) {
                    D0();
                }
                pkf.d(pkfVar, this, this.W, new ljd(1, this));
                return;
            }
            if (y.e.CAPTURE == eVar) {
                if (bVar != null) {
                    tw6.a(bVar, "Camera");
                }
                if (s26.a(this, "android.permission.CAMERA") != 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: bdh
                        @Override // java.lang.Runnable
                        public final void run() {
                            ReviewActivity.B0(this.a, true, 6);
                        }
                    });
                    return;
                } else {
                    B0(this, true, 6);
                    return;
                }
            }
            B0(this, true, 4);
        }
        D0();
        getOnBackPressedDispatcher().a(this, new b(this));
    }

    @Override // defpackage.pe2, androidx.fragment.app.f, android.app.Activity
    public final void onDestroy() {
        if (zvi.a) {
            Log.d("com.adobe.dcmscan.ReviewActivity", "onDestroy");
        }
        super.onDestroy();
    }

    @Override // com.adobe.dcmscan.a, defpackage.of5, defpackage.sf5, android.app.Activity
    public final void onSaveInstanceState(Bundle bundle) {
        bundle.getClass();
        super.onSaveInstanceState(bundle);
        bundle.putInt("anchorPageIndex", this.P);
        a0 a0Var = this.G;
        bundle.putInt("currentPageIndex", a0Var != null ? a0Var.i : 0);
    }

    @Override // com.adobe.dcmscan.a
    public final void s0(v21 v21Var, g5a<? super v21, d6m> g5aVar, boolean z) {
        v21Var.getClass();
        D0();
        super.s0(v21Var, g5aVar, z);
        if (!isFinishing()) {
            if (z) {
                f2b.a.Z();
            }
        } else {
            a0 a0Var = this.G;
            if (a0Var != null) {
                a0Var.p = false;
            }
        }
    }

    @Override // com.adobe.dcmscan.a
    public final void t0(int i, Intent intent, boolean z) {
        String str;
        ArrayList<String> stringArrayListExtra;
        if (i != -1 || intent == null || (stringArrayListExtra = intent.getStringArrayListExtra("intentData")) == null) {
            str = null;
        } else {
            Iterator<String> it = stringArrayListExtra.iterator();
            it.getClass();
            String strA = "\n";
            while (it.hasNext()) {
                String next = it.next();
                if (next != null) {
                    strA = e8n.a(strA, "\n", next);
                }
            }
            int length = strA.length() - 1;
            int i2 = 0;
            boolean z2 = false;
            while (i2 <= length) {
                boolean z3 = cub.c(strA.charAt(!z2 ? i2 : length), 32) <= 0;
                if (z2) {
                    if (!z3) {
                        break;
                    } else {
                        length--;
                    }
                } else if (z3) {
                    i2++;
                } else {
                    z2 = true;
                }
            }
            if (strA.subSequence(i2, length + 1).toString().length() > 0) {
                String string = getString(2132027042);
                string.getClass();
                str = String.format(string, Arrays.copyOf(new Object[]{strA}, 1));
            } else {
                f2b f2bVar = f2b.a;
                f2bVar.getClass();
                h2b h2bVar = f2b.U;
                iac<Object>[] iacVarArr = f2b.b;
                if (((Boolean) h2bVar.getValue(f2bVar, iacVarArr[40])).booleanValue()) {
                    h2bVar.setValue(f2bVar, iacVarArr[40], Boolean.FALSE);
                } else {
                    com.adobe.dcmscan.document.b bVar = this.E;
                    if (bVar != null) {
                        bVar.j = true;
                    }
                }
                str = null;
            }
        }
        if (!z || i == -1) {
            A0(true, str, true);
        } else {
            B0(this, false, 6);
        }
    }

    @Override // com.adobe.dcmscan.a
    public final void g0(Activity activity, qii qiiVar) {
    }
}
