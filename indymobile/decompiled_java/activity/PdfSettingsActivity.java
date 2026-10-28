package com.indymobile.app.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import androidx.appcompat.widget.Toolbar;
import com.indymobile.app.model.PSDocument;
import com.indymobile.app.model.PSPaperSize;
import com.indymobile.app.model.bean.PSPaperSizeInfoBean;
import com.indymobileapp.document.scanner.R;
import java.util.ArrayList;
import java.util.Date;
import p000.AbstractActivityC11478mu;
import p000.av3;
import p000.iy3;
import p000.iz0;
import p000.t62;
import p000.ws3;
import p000.wv3;
import p000.zu3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PdfSettingsActivity extends AbstractActivityC11478mu {

    /* JADX INFO: renamed from: h */
    public PSDocument f30957h;

    /* JADX INFO: renamed from: i */
    public Spinner f30958i;

    /* JADX INFO: renamed from: j */
    public Spinner f30959j;

    /* JADX INFO: renamed from: k */
    public Spinner f30960k;

    /* JADX INFO: renamed from: l */
    public ArrayList f30961l;

    /* JADX INFO: renamed from: m */
    public ArrayList f30962m;

    /* JADX INFO: renamed from: n */
    public ArrayList f30963n;

    /* JADX INFO: renamed from: o */
    public boolean f30964o;

    @Override // p000.AbstractActivityC11478mu, androidx.fragment.app.AbstractActivityC0619o, p000.bn0, p000.an0, android.app.Activity
    public final void onCreate(Bundle bundle) {
        ArrayList arrayList;
        super.onCreate(bundle);
        this.f30957h = (PSDocument) getIntent().getBundleExtra("bundle").getParcelable(PSDocument.TABLE_NAME);
        setContentView(R.layout.activity_pdf_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().mo33197r(true);
        getSupportActionBar().mo33193n(true);
        this.f30958i = (Spinner) findViewById(R.id.spinnerPageSize);
        this.f30959j = (Spinner) findViewById(R.id.spinnerPageOrientation);
        this.f30960k = (Spinner) findViewById(R.id.spinnerImageScale);
        if (bundle != null) {
            this.f30964o = bundle.getBoolean("modified");
        }
        ArrayList arrayListM35825J = iy3.m31217b().f51581o.m35825J();
        this.f30961l = new ArrayList();
        for (int i = 0; i < arrayListM35825J.size(); i++) {
            PSPaperSize pSPaperSize = ((PSPaperSizeInfoBean) arrayListM35825J.get(i)).paperSize;
            if (pSPaperSize.paperSizeID != 999) {
                this.f30961l.add(pSPaperSize);
            }
        }
        ArrayList arrayList2 = new ArrayList();
        this.f30962m = arrayList2;
        arrayList2.add(wv3.m37383b(R.string.DOCUMENT_SETTINGS_PDF_ORIENTATION_PORTRAIT));
        this.f30962m.add(wv3.m37383b(R.string.DOCUMENT_SETTINGS_PDF_ORIENTATION_LANSCAPE));
        this.f30962m.add(wv3.m37383b(R.string.DOCUMENT_SETTINGS_PDF_ORIENTATION_AUTOMATIC));
        ArrayList arrayList3 = new ArrayList();
        this.f30963n = arrayList3;
        arrayList3.add(wv3.m37383b(R.string.DOCUMENT_SETTINGS_PDF_IMAGE_SCALE_ASPECT_FIT));
        this.f30963n.add(wv3.m37383b(R.string.DOCUMENT_SETTINGS_PDF_IMAGE_SCALE_SCALE_TO_FILL));
        ArrayList arrayList4 = new ArrayList();
        int i2 = 0;
        while (true) {
            int size = this.f30961l.size();
            arrayList = this.f30961l;
            if (i2 >= size) {
                break;
            }
            arrayList4.add(((PSPaperSize) arrayList.get(i2)).paperSizeName);
            i2++;
        }
        int i3 = this.f30957h.paperSizeID;
        int i4 = -1;
        int i5 = -1;
        for (int i6 = 0; i6 < arrayList.size(); i6++) {
            if (((PSPaperSize) arrayList.get(i6)).paperSizeID == i3) {
                i5 = i6;
            }
        }
        if (i5 < 0) {
            ArrayList arrayList5 = this.f30961l;
            int iM37387f = wv3.m37387f();
            for (int i7 = 0; i7 < arrayList5.size(); i7++) {
                if (((PSPaperSize) arrayList5.get(i7)).paperSizeID == iM37387f) {
                    i4 = i7;
                }
            }
            i5 = i4;
        }
        int iOrdinal = this.f30957h.pageOrientation.ordinal();
        int i8 = this.f30957h.pageContentMode == zu3.kPSDocumentPageContentModeAspectFit ? 0 : 1;
        String[] strArr = (String[]) arrayList4.toArray(new String[arrayList4.size()]);
        ArrayList arrayList6 = this.f30962m;
        String[] strArr2 = (String[]) arrayList6.toArray(new String[arrayList6.size()]);
        ArrayList arrayList7 = this.f30963n;
        String[] strArr3 = (String[]) arrayList7.toArray(new String[arrayList7.size()]);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, strArr);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.f30958i.setAdapter((SpinnerAdapter) arrayAdapter);
        this.f30958i.setSelection(i5);
        ArrayAdapter arrayAdapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, strArr2);
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.f30959j.setAdapter((SpinnerAdapter) arrayAdapter2);
        this.f30959j.setSelection(iOrdinal);
        ArrayAdapter arrayAdapter3 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, strArr3);
        arrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.f30960k.setAdapter((SpinnerAdapter) arrayAdapter3);
        this.f30960k.setSelection(i8);
    }

    @Override // android.app.Activity
    public final boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        mo15578y();
        return true;
    }

    @Override // p000.AbstractActivityC11478mu, p000.bn0, p000.an0, android.app.Activity
    public final void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("modified", this.f30964o);
        super.onSaveInstanceState(bundle);
    }

    @Override // p000.AbstractActivityC11478mu
    /* JADX INFO: renamed from: y */
    public final void mo15578y() {
        int i = ((PSPaperSize) this.f30961l.get(this.f30958i.getSelectedItemPosition())).paperSizeID;
        av3 av3Var = av3.values()[this.f30959j.getSelectedItemPosition()];
        zu3 zu3Var = this.f30960k.getSelectedItemPosition() == 0 ? zu3.kPSDocumentPageContentModeAspectFit : zu3.kPSDocumentPageContentModeScaleToFill;
        PSDocument pSDocument = this.f30957h;
        if (pSDocument.paperSizeID != i) {
            pSDocument.paperSizeID = i;
            this.f30964o = true;
            ws3.m37349c("pdf_settings", "action", "size_id_" + i);
        }
        PSDocument pSDocument2 = this.f30957h;
        if (pSDocument2.pageOrientation != av3Var) {
            pSDocument2.pageOrientation = av3Var;
            this.f30964o = true;
            ws3.m37349c("pdf_settings", "action", "orientation_" + av3Var.toString());
        }
        PSDocument pSDocument3 = this.f30957h;
        if (pSDocument3.pageContentMode != zu3Var) {
            pSDocument3.pageContentMode = zu3Var;
            this.f30964o = true;
            ws3.m37349c("pdf_settings", "action", "mode_" + zu3Var.toString());
        }
        if (this.f30964o) {
            iy3.m31217b().f51581o.m35827L(this.f30957h.documentID, new Date());
            t62 t62Var = iy3.m31217b().f51581o;
            PSDocument pSDocument4 = this.f30957h;
            SQLiteDatabase writableDatabase = ((iz0) t62Var.f60999b).getWritableDatabase();
            pSDocument4.getClass();
            ContentValues contentValues = new ContentValues();
            contentValues.put("i_document_id", Integer.valueOf(pSDocument4.documentID));
            contentValues.put("i_paper_size_id", Integer.valueOf(pSDocument4.paperSizeID));
            contentValues.put("i_page_content_mode", Integer.valueOf(pSDocument4.pageContentMode.ordinal()));
            contentValues.put("i_page_orientation", Integer.valueOf(pSDocument4.pageOrientation.ordinal()));
            writableDatabase.update(PSDocument.TABLE_NAME, contentValues, "i_document_id=" + pSDocument4.documentID, null);
        }
        Intent intent = new Intent();
        if (this.f30964o) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("updated_document", this.f30957h);
            intent.putExtra("bundle", bundle);
        }
        setResult(-1, intent);
        finish();
    }
}
