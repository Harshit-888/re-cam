package com.indymobile.app.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.AbstractC0624t;
import androidx.fragment.app.C0604a;
import androidx.preference.CheckBoxPreference;
import com.indymobileapp.document.scanner.R;
import p000.AbstractActivityC11478mu;
import p000.a64;
import p000.dy3;
import p000.w54;
import p000.ws3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class SyncSettingsActivity extends AbstractActivityC11478mu {

    /* JADX INFO: renamed from: com.indymobile.app.activity.SyncSettingsActivity$a */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public static class SharedPreferencesOnSharedPreferenceChangeListenerC6649a extends w54 implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override // p000.w54
        /* JADX INFO: renamed from: f */
        public final void mo15678f(String str) {
            a64 a64Var = this.f63773b;
            a64Var.f148f = "settings_prefs";
            a64Var.f145c = null;
            m37153j(R.xml.sync_preferences, str);
            m15689l();
        }

        /* JADX INFO: renamed from: l */
        public final void m15689l() {
            dy3 dy3VarM28727f = dy3.m28727f();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) m37152e("KEY_PS_SETTINGS_AUTO_SYNC_ENABLED");
            if (checkBoxPreference != null) {
                dy3VarM28727f.f46857R = checkBoxPreference.f16204M;
            }
            CheckBoxPreference checkBoxPreference2 = (CheckBoxPreference) m37152e("KEY_PS_SETTINGS_AUTO_SYNC_WIFI_ONLY");
            if (checkBoxPreference2 != null) {
                dy3VarM28727f.f46858S = checkBoxPreference2.f16204M;
            }
            CheckBoxPreference checkBoxPreference3 = (CheckBoxPreference) m37152e("KEY_PS_SETTINGS_DATA_USAGE_WARNING");
            if (checkBoxPreference3 != null) {
                dy3VarM28727f.f46859T = checkBoxPreference3.f16204M;
            }
        }

        @Override // androidx.fragment.app.Fragment
        public final void onPause() {
            this.f63773b.m129b().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override // androidx.fragment.app.Fragment
        public final void onResume() {
            super.onResume();
            this.f63773b.m129b().registerOnSharedPreferenceChangeListener(this);
            m15689l();
        }

        @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
        public final void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            String str2 = "";
            m15689l();
            try {
                try {
                    str2 = "" + sharedPreferences.getString(str, "");
                } catch (Exception unused) {
                }
            } catch (Exception unused2) {
                str2 = "" + sharedPreferences.getBoolean(str, false);
            }
            try {
                ws3.m37349c("settings", "change", str + "_" + str2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, p000.an0, android.app.Activity
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_sync_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().mo33197r(true);
        getSupportActionBar().mo33193n(true);
        if (getSupportFragmentManager().m3317A(R.id.content_frame) == null) {
            AbstractC0624t supportFragmentManager = getSupportFragmentManager();
            supportFragmentManager.getClass();
            C0604a c0604a = new C0604a(supportFragmentManager);
            c0604a.m3278h(R.id.content_frame, new SharedPreferencesOnSharedPreferenceChangeListenerC6649a(), null);
            c0604a.m3274d(false);
        }
    }

    @Override // android.app.Activity
    public final boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        mo15578y();
        return true;
    }

    @Override // p000.AbstractActivityC11478mu
    /* JADX INFO: renamed from: y */
    public final void mo15578y() {
        super.mo15578y();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
    }
}
