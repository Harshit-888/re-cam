package com.indymobile.app.activity.editor.markup;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.slider.Slider;
import com.indymobileapp.document.scanner.R;
import java.io.File;
import p000.AbstractActivityC11478mu;
import p000.AbstractC11526o4;
import p000.lh0;
import p000.ql4;
import p000.x43;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class RoundedCornersActivity extends AbstractActivityC11478mu {

    /* JADX INFO: renamed from: l */
    public static final /* synthetic */ int f31329l = 0;

    /* JADX INFO: renamed from: h */
    public Slider f31330h;

    /* JADX INFO: renamed from: i */
    public TextView f31331i;

    /* JADX INFO: renamed from: j */
    public ImageView f31332j;

    /* JADX INFO: renamed from: k */
    public ql4 f31333k;

    /* JADX INFO: renamed from: O */
    public final void m15824O() {
        ql4 ql4Var = this.f31333k;
        int iMin = Math.min(ql4Var.f58758k, ql4Var.f58759l);
        int value = (int) this.f31330h.getValue();
        ql4 ql4Var2 = this.f31333k;
        float f = ((iMin * value) / 100.0f) * 0.5f;
        if (ql4Var2.f58754g == f) {
            return;
        }
        boolean z = f > 0.05f;
        Paint paint = ql4Var2.f58751d;
        if (z) {
            paint.setShader(ql4Var2.f58752e);
        } else {
            paint.setShader(null);
        }
        ql4Var2.f58754g = f;
        ql4Var2.invalidateSelf();
    }

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, p000.an0, android.app.Activity
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_rounded_corners);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        AbstractC11526o4 supportActionBar = getSupportActionBar();
        int i = 1;
        if (supportActionBar != null) {
            supportActionBar.mo33197r(true);
            supportActionBar.mo33193n(true);
        }
        Bundle bundleExtra = getIntent().getBundleExtra("bundle");
        int i2 = bundleExtra.getInt("rounded_corners", 0);
        File file = (File) bundleExtra.getSerializable("image_file");
        Slider slider = (Slider) findViewById(R.id.slider_rounded_corners);
        this.f31330h = slider;
        slider.setValueFrom(0.0f);
        this.f31330h.setValueTo(100.0f);
        this.f31330h.m13998V(new lh0(this, i));
        this.f31331i = (TextView) findViewById(R.id.txt_rounded_corners);
        if (bundle != null) {
            i2 = bundle.getInt("rounded_corners", 0);
        }
        this.f31330h.setValue(i2);
        this.f31331i.setText(String.valueOf(i2));
        this.f31332j = (ImageView) findViewById(R.id.image_view);
        Resources resources = getResources();
        String absolutePath = file.getAbsolutePath();
        ql4 ql4Var = new ql4(resources, BitmapFactory.decodeFile(absolutePath));
        if (ql4Var.f58748a == null) {
            Log.w("RoundedBitmapDrawableFa", "RoundedBitmapDrawable cannot decode " + absolutePath);
        }
        this.f31333k = ql4Var;
        this.f31332j.setImageDrawable(ql4Var);
        m15824O();
    }

    @Override // android.app.Activity
    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rounded_corners, menu);
        return true;
    }

    @Override // android.app.Activity
    public final boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == 16908332) {
            mo15578y();
            return true;
        }
        if (itemId != R.id.action_ok) {
            return super.onOptionsItemSelected(menuItem);
        }
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("rounded_corners", (int) this.f31330h.getValue());
        intent.putExtra("bundle", bundle);
        setResult(-1, intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
        return true;
    }

    @Override // android.app.Activity
    public final boolean onPrepareOptionsMenu(Menu menu) {
        x43.m37497E(menu);
        x43.m37494B(this, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override // p000.AbstractActivityC11478mu, p000.bn0, p000.an0, android.app.Activity
    public final void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("rounded_corners", (int) this.f31330h.getValue());
    }

    @Override // p000.AbstractActivityC11478mu
    /* JADX INFO: renamed from: y */
    public final void mo15578y() {
        setResult(0);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
