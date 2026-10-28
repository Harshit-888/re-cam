package com.indymobile.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.AbstractC0624t;
import androidx.fragment.app.C0604a;
import com.indymobile.app.model.bean.PSShareObject;
import com.indymobileapp.document.scanner.R;
import p000.AbstractActivityC11478mu;
import p000.dy3;
import p000.px4;
import p000.uv3;
import p000.wv3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class ShareActivity extends AbstractActivityC11478mu {

    /* JADX INFO: renamed from: h */
    public PSShareObject f31002h = null;

    /* JADX INFO: renamed from: O */
    public final void m15681O(PSShareObject pSShareObject, Intent intent) {
        dy3.m28727f().f46841B = pSShareObject.shareFileSize;
        dy3.m28727f().f46840A = pSShareObject.shareFileType;
        dy3.m28727f().m28731e();
        Intent intent2 = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("shareIntent", intent);
        bundle.putParcelable("shareObject", pSShareObject);
        intent2.putExtra("bundle", bundle);
        setResult(-1, intent2);
        finish();
    }

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, p000.an0, android.app.Activity
    public final void onCreate(Bundle bundle) {
        String strM37383b;
        super.onCreate(bundle);
        setContentView(R.layout.activity_share);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().mo33193n(true);
        Bundle bundleExtra = getIntent().getBundleExtra("bundle");
        this.f31002h = (PSShareObject) bundleExtra.getParcelable("shareObject");
        if (bundle != null) {
            this.f31002h = (PSShareObject) bundle.getParcelable("shareObject");
        }
        uv3 uv3Var = this.f31002h.shareType;
        if (uv3Var == uv3.kPSShareTypeIntent) {
            strM37383b = wv3.m37383b(R.string.SHARE);
        } else if (uv3Var == uv3.kPSShareTypeEmailToMySelf) {
            strM37383b = wv3.m37383b(R.string.SEND_EMAIL);
        } else {
            strM37383b = uv3Var == uv3.kPSShareTypeSaveToDisk ? wv3.m37383b(R.string.SAVE_TO_STORAGE) : "";
        }
        getSupportActionBar().mo33201v(strM37383b);
        if (bundle == null) {
            px4 px4Var = new px4();
            px4Var.setArguments(bundleExtra);
            AbstractC0624t supportFragmentManager = getSupportFragmentManager();
            supportFragmentManager.getClass();
            C0604a c0604a = new C0604a(supportFragmentManager);
            c0604a.m3278h(R.id.fragment_content, px4Var, "shareFragment");
            c0604a.m3274d(false);
        }
    }

    @Override // android.app.Activity
    public final boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return true;
        }
        mo15578y();
        return true;
    }

    @Override // p000.AbstractActivityC11478mu, p000.bn0, p000.an0, android.app.Activity
    public final void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelable("shareObject", this.f31002h);
        super.onSaveInstanceState(bundle);
    }
}
