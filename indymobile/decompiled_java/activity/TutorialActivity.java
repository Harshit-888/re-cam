package com.indymobile.app.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.widget.Toolbar;
import com.indymobileapp.document.scanner.R;
import p000.AbstractActivityC11478mu;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes.dex */
public class TutorialActivity extends AbstractActivityC11478mu {

    /* JADX INFO: renamed from: h */
    public WebView f31010h;

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, p000.an0, android.app.Activity
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_tutorial);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().mo33197r(true);
        getSupportActionBar().mo33193n(true);
        WebView webView = (WebView) findViewById(R.id.webView);
        this.f31010h = webView;
        webView.loadUrl("file:///android_asset/" + getString(R.string.NAL_HELP_FILE));
        WebSettings settings = this.f31010h.getSettings();
        settings.setDefaultFontSize(16);
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setDisplayZoomControls(true);
        settings.setBuiltInZoomControls(true);
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
