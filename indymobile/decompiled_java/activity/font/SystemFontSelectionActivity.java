package com.indymobile.app.activity.font;

import android.graphics.Typeface;
import android.graphics.fonts.SystemFonts;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.indymobileapp.document.scanner.R;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import p000.AbstractActivityC11478mu;
import p000.AbstractC11526o4;
import p000.eb5;
import p000.nb1;
import p000.qh0;
import p000.x43;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class SystemFontSelectionActivity extends AbstractActivityC11478mu {

    /* JADX INFO: renamed from: h */
    public RecyclerView f31342h;

    /* JADX INFO: renamed from: i */
    public qh0 f31343i;

    /* JADX INFO: renamed from: j */
    public final ArrayList f31344j = new ArrayList();

    /* JADX INFO: renamed from: k */
    public final HashMap f31345k = new HashMap();

    /* JADX INFO: renamed from: l */
    public final HashMap f31346l = new HashMap();

    /* JADX INFO: renamed from: O */
    public final void m15826O(File file) {
        Typeface typefaceCreateFromFile;
        String strReplaceFirst = file.getName().replaceFirst("[.][^.]+$", "");
        ArrayList arrayList = this.f31344j;
        if (arrayList.contains(strReplaceFirst) || (typefaceCreateFromFile = Typeface.createFromFile(file)) == null) {
            return;
        }
        arrayList.add(strReplaceFirst);
        this.f31345k.put(strReplaceFirst, typefaceCreateFromFile);
        this.f31346l.put(strReplaceFirst, file.getAbsolutePath());
    }

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, p000.an0, android.app.Activity
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_system_font_selection);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        AbstractC11526o4 supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.mo33197r(true);
            supportActionBar.mo33193n(true);
        }
        ArrayList arrayList = this.f31344j;
        arrayList.clear();
        this.f31345k.clear();
        this.f31346l.clear();
        if (Build.VERSION.SDK_INT >= 29) {
            Iterator it = SystemFonts.getAvailableFonts().iterator();
            while (it.hasNext()) {
                File file = eb5.m28906c(it.next()).getFile();
                if (file != null && file.exists() && file.canRead() && file.getAbsolutePath().toLowerCase().endsWith(".ttf")) {
                    m15826O(file);
                }
            }
        } else {
            File[] fileArrListFiles = new File("/system/fonts").listFiles();
            if (fileArrListFiles != null) {
                for (File file2 : fileArrListFiles) {
                    if (file2.exists() && file2.canRead() && file2.getName().toLowerCase().endsWith(".ttf")) {
                        m15826O(file2);
                    }
                }
            }
        }
        Collections.sort(arrayList, String.CASE_INSENSITIVE_ORDER);
        this.f31343i = new qh0(this, 5);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        this.f31342h = recyclerView;
        recyclerView.setHasFixedSize(true);
        this.f31342h.setLayoutManager(new LinearLayoutManager(1, false));
        this.f31342h.m3536f(new nb1(this));
        this.f31342h.setAdapter(this.f31343i);
        this.f31343i.notifyDataSetChanged();
    }

    @Override // android.app.Activity
    public final boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override // android.app.Activity
    public final boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        mo15578y();
        return true;
    }

    @Override // android.app.Activity
    public final boolean onPrepareOptionsMenu(Menu menu) {
        x43.m37497E(menu);
        x43.m37494B(this, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override // p000.AbstractActivityC11478mu
    /* JADX INFO: renamed from: y */
    public final void mo15578y() {
        setResult(0);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
    }
}
