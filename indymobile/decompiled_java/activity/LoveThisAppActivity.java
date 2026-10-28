package com.indymobile.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import com.indymobileapp.document.scanner.R;
import p000.AbstractActivityC11478mu;
import p000.AbstractC0744ch;
import p000.ss3;
import p000.ws3;
import p000.wv3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class LoveThisAppActivity extends AbstractActivityC11478mu implements View.OnClickListener {

    /* JADX INFO: renamed from: h */
    public View f30820h;

    /* JADX INFO: renamed from: i */
    public View f30821i;

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.f30820h) {
            AbstractC0744ch.m4800e0(this);
            SharedPreferences.Editor editorEdit = getSharedPreferences("apprater", 0).edit();
            editorEdit.putBoolean("dontshowagain", true);
            editorEdit.apply();
            ws3.m37348b("rate_app");
            return;
        }
        if (view == this.f30821i) {
            String str = String.format(wv3.m37383b(R.string.NAL_INVITE_FRIENDS_MESSAGE), wv3.m37383b(R.string.app_name), Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName()).toString(), Uri.parse("http://itunes.apple.com/app/id684155862?mt=8").toString());
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.SUBJECT", String.format(wv3.m37383b(R.string.NAL_INVITE_FRIENDS_TITLE), wv3.m37383b(R.string.app_name)));
            intent.putExtra("android.intent.extra.TEXT", str);
            ss3.m35588q().f60593n = true;
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.INVITE_FRIENDS_VIA)));
            ws3.m37349c("invite_friends", "from", "share");
        }
    }

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, p000.an0, android.app.Activity
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_love_this_app);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().mo33197r(true);
        getSupportActionBar().mo33193n(true);
        this.f30820h = findViewById(R.id.buttonWriteReview);
        this.f30821i = findViewById(R.id.buttonInviteFriend);
        this.f30820h.setOnClickListener(this);
        this.f30821i.setOnClickListener(this);
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
