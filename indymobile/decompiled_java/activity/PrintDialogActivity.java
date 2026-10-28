package com.indymobile.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.widget.Toolbar;
import com.indymobileapp.document.scanner.R;
import p000.AbstractActivityC11478mu;
import p000.h84;
import p000.i84;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PrintDialogActivity extends AbstractActivityC11478mu {

    /* JADX INFO: renamed from: h */
    public WebView f30965h;

    /* JADX INFO: renamed from: i */
    public Intent f30966i;

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, android.app.Activity
    public final void onActivityResult(int i, int i2, Intent intent) {
        if (i == 65743 && i2 == -1) {
            this.f30965h.loadUrl(intent.getStringExtra("SCAN_RESULT"));
        }
    }

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, p000.an0, android.app.Activity
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_print_dialog);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().mo33197r(true);
        getSupportActionBar().mo33193n(true);
        this.f30965h = (WebView) findViewById(R.id.webview);
        this.f30966i = getIntent();
        this.f30965h.getSettings().setJavaScriptEnabled(true);
        this.f30965h.setWebViewClient(new i84(0, this));
        this.f30965h.addJavascriptInterface(new h84(this), "AndroidPrintDialog");
        this.f30965h.loadUrl("https://www.google.com/cloudprint/dialog.html");
    }
}
