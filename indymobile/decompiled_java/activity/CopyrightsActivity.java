package com.indymobile.app.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import androidx.appcompat.widget.Toolbar;
import com.indymobileapp.document.scanner.R;
import p000.AbstractActivityC11478mu;
import p000.ds0;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class CopyrightsActivity extends AbstractActivityC11478mu {

    /* JADX INFO: renamed from: h */
    public WebView f30700h;

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, p000.an0, android.app.Activity
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_copyrights);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().mo33197r(true);
        getSupportActionBar().mo33193n(true);
        WebView webView = (WebView) findViewById(R.id.webView);
        this.f30700h = webView;
        webView.loadUrl("file:///android_asset/copyrights.html");
        this.f30700h.getSettings().setDefaultFontSize(16);
        this.f30700h.setWebViewClient(new ds0());
    }

    @Override // android.app.Activity
    public final boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        mo15578y();
        return true;
    }
}
