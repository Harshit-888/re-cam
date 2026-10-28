package com.indymobile.app.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.indymobile.app.activity.ProStatusActivity;
import com.indymobileapp.document.scanner.R;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import p000.AbstractActivityC11478mu;
import p000.AbstractC11526o4;
import p000.dw3;
import p000.dy3;
import p000.g02;
import p000.l43;
import p000.rw3;
import p000.wv3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class ProStatusActivity extends AbstractActivityC11478mu {

    /* JADX INFO: renamed from: p */
    public static final /* synthetic */ int f30967p = 0;

    /* JADX INFO: renamed from: h */
    public TextView f30968h;

    /* JADX INFO: renamed from: i */
    public TextView f30969i;

    /* JADX INFO: renamed from: j */
    public TextView f30970j;

    /* JADX INFO: renamed from: k */
    public TextView f30971k;

    /* JADX INFO: renamed from: l */
    public TextView f30972l;

    /* JADX INFO: renamed from: m */
    public TextView f30973m;

    /* JADX INFO: renamed from: n */
    public View f30974n;

    /* JADX INFO: renamed from: o */
    public l43 f30975o;

    /* JADX INFO: renamed from: O */
    public final void m15673O() {
        boolean z = dy3.m28727f().f46865Z;
        rw3 rw3VarM35094f = rw3.m35094f();
        Object obj = ((HashMap) rw3VarM35094f.f59796f).get("com.indymobileapp.document.scanner.updatepro");
        dw3 dw3Var = dw3.PURCHASED_AND_ACKNOWLEDGED;
        boolean z2 = obj == dw3Var || rw3VarM35094f.m35100i();
        boolean z3 = ((HashMap) rw3.m35094f().f59796f).get("subscription_pro") == dw3Var;
        if (!z && !z2 && !z3) {
            finish();
            return;
        }
        TextView textView = this.f30968h;
        String strM29821b = "";
        if (z2) {
            textView.setText(wv3.m37383b(R.string.pro_status_plan_lifetime));
            this.f30969i.setText(wv3.m37383b(R.string.pro_status_lifetime_note_android));
            this.f30970j.setText(R.string.pro_status_purchased_on);
            TextView textView2 = this.f30971k;
            Long l = (Long) ((HashMap) rw3.m35094f().f59797g).get("com.indymobileapp.document.scanner.updatepro");
            if (l != null) {
                strM29821b = g02.m29821b(new Date(l.longValue()));
                strM29821b.getClass();
            }
            textView2.setText(strM29821b);
            this.f30972l.setText(R.string.pro_status_access);
            this.f30973m.setText(R.string.pro_status_no_expiration);
            this.f30974n.setVisibility(8);
            return;
        }
        if (!z3) {
            textView.setText("");
            this.f30969i.setText("");
            this.f30970j.setText("");
            this.f30971k.setText("");
            this.f30972l.setText("");
            this.f30973m.setText("");
            this.f30974n.setVisibility(8);
            return;
        }
        textView.setText(wv3.m37383b(R.string.pro_status_plan_subscription));
        this.f30969i.setText(wv3.m37383b(R.string.pro_status_subscription_note_android));
        this.f30970j.setText(R.string.pro_status_started_on);
        TextView textView3 = this.f30971k;
        Long l2 = (Long) ((HashMap) rw3.m35094f().f59797g).get("subscription_pro");
        if (l2 != null) {
            strM29821b = g02.m29821b(new Date(l2.longValue()));
            strM29821b.getClass();
        }
        textView3.setText(strM29821b);
        this.f30972l.setText(R.string.pro_status_status);
        this.f30973m.setText(R.string.pro_status_active);
        this.f30974n.setVisibility(0);
    }

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, p000.an0, android.app.Activity
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_pro_status);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AbstractC11526o4 supportActionBar = getSupportActionBar();
        final int i = 0;
        if (supportActionBar != null) {
            supportActionBar.mo33194o();
            supportActionBar.mo33193n(false);
        }
        toolbar.setNavigationIcon(R.drawable.baseline_close_white_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(this) { // from class: t84

            /* JADX INFO: renamed from: b */
            public final /* synthetic */ ProStatusActivity f61044b;

            {
                this.f61044b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                int i2 = i;
                ProStatusActivity proStatusActivity = this.f61044b;
                switch (i2) {
                    case 0:
                        int i3 = ProStatusActivity.f30967p;
                        proStatusActivity.finish();
                        break;
                    default:
                        int i4 = ProStatusActivity.f30967p;
                        try {
                            proStatusActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/account/subscriptions").buildUpon().appendQueryParameter("sku", "subscription_pro").appendQueryParameter("package", proStatusActivity.getPackageName()).build()));
                        } catch (Exception unused) {
                            proStatusActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/account/subscriptions")));
                        }
                        break;
                }
            }
        });
        this.f30968h = (TextView) findViewById(R.id.txtCurrentPlanValue);
        this.f30969i = (TextView) findViewById(R.id.txtPlanNote);
        this.f30970j = (TextView) findViewById(R.id.txtPlanDateLabel);
        this.f30971k = (TextView) findViewById(R.id.txtPlanDateValue);
        this.f30972l = (TextView) findViewById(R.id.txtPlanStatusLabel);
        this.f30973m = (TextView) findViewById(R.id.txtPlanStatusValue);
        View viewFindViewById = findViewById(R.id.btManageSubscription);
        this.f30974n = viewFindViewById;
        final int i2 = 1;
        viewFindViewById.setOnClickListener(new View.OnClickListener(this) { // from class: t84

            /* JADX INFO: renamed from: b */
            public final /* synthetic */ ProStatusActivity f61044b;

            {
                this.f61044b = this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                int i3 = i2;
                ProStatusActivity proStatusActivity = this.f61044b;
                switch (i3) {
                    case 0:
                        int i4 = ProStatusActivity.f30967p;
                        proStatusActivity.finish();
                        break;
                    default:
                        int i5 = ProStatusActivity.f30967p;
                        try {
                            proStatusActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/account/subscriptions").buildUpon().appendQueryParameter("sku", "subscription_pro").appendQueryParameter("package", proStatusActivity.getPackageName()).build()));
                        } catch (Exception unused) {
                            proStatusActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/account/subscriptions")));
                        }
                        break;
                }
            }
        });
        this.f30975o = new l43(this, i2);
        rw3 rw3VarM35094f = rw3.m35094f();
        l43 l43Var = this.f30975o;
        l43Var.getClass();
        ArrayList arrayList = (ArrayList) rw3VarM35094f.f59793c;
        if (!arrayList.contains(l43Var)) {
            arrayList.add(l43Var);
        }
        m15673O();
    }

    @Override // p000.AbstractActivityC11478mu, p000.AbstractActivityC11689sf, androidx.fragment.app.AbstractActivityC0619o, android.app.Activity
    public final void onDestroy() {
        super.onDestroy();
        rw3 rw3VarM35094f = rw3.m35094f();
        l43 l43Var = this.f30975o;
        l43Var.getClass();
        ((ArrayList) rw3VarM35094f.f59793c).remove(l43Var);
    }

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, android.app.Activity
    public final void onResume() {
        super.onResume();
        rw3.m35094f().m35102k();
    }
}
