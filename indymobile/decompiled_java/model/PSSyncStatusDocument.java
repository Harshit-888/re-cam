package com.indymobile.app.model;

import android.content.ContentValues;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSSyncStatusDocument {
    public static final String TABLE_NAME = "sync_stats_document";
    public static int columnCount = 3;
    public int documentId;
    public String etagModelLocal;
    public String etagModelRemote;

    public PSSyncStatusDocument(int i, String str, String str2) {
        this.documentId = i;
        this.etagModelLocal = str;
        this.etagModelRemote = str2;
    }

    /* JADX INFO: renamed from: a */
    public final ContentValues m15863a() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("i_document_id", Integer.valueOf(this.documentId));
        contentValues.put("etag_model_local", this.etagModelLocal);
        contentValues.put("etag_model_remote", this.etagModelRemote);
        return contentValues;
    }

    public PSSyncStatusDocument() {
    }
}
