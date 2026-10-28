package com.indymobile.app.model;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.Date;
import p000.ny3;
import p000.pl3;
import p000.qy3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSSyncHistory {
    public static final String TABLE_NAME = "sync_history";
    public static int columnCount = 30;
    public String accountId;
    public pl3 connectionStatus;
    public Date dateCreated;
    public Date dateEnd;
    public Date dateStart;
    public String errorMessage;
    public int localDirAdd;
    public int localDirTrash;
    public int localDirUpdate;
    public int localDocAdd;
    public int localDocTrash;
    public int localDocUpdate;
    public int localPageAdd;
    public int localPageTrash;
    public int localPageUpdate;
    public int remoteDirAdd;
    public int remoteDirTrash;
    public int remoteDirUpdate;
    public int remoteDocAdd;
    public int remoteDocTrash;
    public int remoteDocUpdate;
    public int remotePageAdd;
    public int remotePageTrash;
    public int remotePageUpdate;
    public ny3 syncCloudType;
    public int syncHistoryId;
    public qy3 syncStatus;
    public int totalDir;
    public int totalDoc;
    public int totalPage;

    public PSSyncHistory(Cursor cursor) {
        int i;
        Exception e;
        int i2;
        int i3;
        this.syncHistoryId = cursor.getInt(0);
        this.syncCloudType = ny3.GoogleDrive;
        try {
            i = 2;
            try {
                this.syncCloudType = ny3.values()[cursor.getInt(1)];
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
            }
        } catch (Exception e3) {
            i = 1;
            e = e3;
        }
        this.accountId = cursor.getString(i);
        this.dateCreated = new Date(cursor.getLong(i + 1));
        this.dateStart = new Date(cursor.getLong(i + 2));
        int i4 = i + 4;
        this.dateEnd = new Date(cursor.getLong(i + 3));
        this.connectionStatus = pl3.TYPE_WIFI;
        try {
            i2 = i + 5;
            try {
                this.connectionStatus = pl3.values()[cursor.getInt(i4)];
            } catch (Exception e4) {
                e = e4;
                i4 = i2;
                e.printStackTrace();
                i2 = i4;
            }
        } catch (Exception e5) {
            e = e5;
        }
        this.syncStatus = qy3.STATUS_SYNC_SUCCESS;
        try {
            i3 = i2 + 1;
            try {
                this.syncStatus = qy3.values()[cursor.getInt(i2)];
            } catch (Exception e6) {
                e = e6;
                i2 = i3;
                e.printStackTrace();
                i3 = i2;
            }
        } catch (Exception e7) {
            e = e7;
        }
        this.errorMessage = cursor.getString(i3);
        this.totalDir = cursor.getInt(i3 + 1);
        this.totalDoc = cursor.getInt(i3 + 2);
        this.totalPage = cursor.getInt(i3 + 3);
        this.localDirAdd = cursor.getInt(i3 + 4);
        this.localDirUpdate = cursor.getInt(i3 + 5);
        this.localDirTrash = cursor.getInt(i3 + 6);
        this.localDocAdd = cursor.getInt(i3 + 7);
        this.localDocUpdate = cursor.getInt(i3 + 8);
        this.localDocTrash = cursor.getInt(i3 + 9);
        this.localPageAdd = cursor.getInt(i3 + 10);
        this.localPageUpdate = cursor.getInt(i3 + 11);
        this.localPageTrash = cursor.getInt(i3 + 12);
        this.remoteDirAdd = cursor.getInt(i3 + 13);
        this.remoteDirUpdate = cursor.getInt(i3 + 14);
        this.remoteDirTrash = cursor.getInt(i3 + 15);
        this.remoteDocAdd = cursor.getInt(i3 + 16);
        this.remoteDocUpdate = cursor.getInt(i3 + 17);
        this.remoteDocTrash = cursor.getInt(i3 + 18);
        this.remotePageAdd = cursor.getInt(i3 + 19);
        this.remotePageUpdate = cursor.getInt(i3 + 20);
        this.remotePageTrash = cursor.getInt(i3 + 21);
    }

    /* JADX INFO: renamed from: a */
    public final ContentValues m15862a() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("i_sync_history_id", Integer.valueOf(this.syncHistoryId));
        contentValues.put("i_sync_cloud_type", Integer.valueOf(this.syncCloudType.ordinal()));
        contentValues.put("c_account_id", this.accountId);
        contentValues.put("i_date_created", Long.valueOf(this.dateCreated.getTime()));
        Date date = this.dateStart;
        if (date != null) {
            contentValues.put("i_date_start", Long.valueOf(date.getTime()));
        }
        Date date2 = this.dateEnd;
        if (date2 != null) {
            contentValues.put("i_date_end", Long.valueOf(date2.getTime()));
        }
        contentValues.put("i_connection_status", Integer.valueOf(this.connectionStatus.ordinal()));
        qy3 qy3Var = this.syncStatus;
        if (qy3Var != null) {
            contentValues.put("i_sync_status", Integer.valueOf(qy3Var.ordinal()));
        }
        contentValues.put("c_error_message", this.errorMessage);
        contentValues.put("i_total_dir", Integer.valueOf(this.totalDir));
        contentValues.put("i_total_doc", Integer.valueOf(this.totalDoc));
        contentValues.put("i_total_page", Integer.valueOf(this.totalPage));
        contentValues.put("i_local_dir_add", Integer.valueOf(this.localDirAdd));
        contentValues.put("i_local_dir_update", Integer.valueOf(this.localDirUpdate));
        contentValues.put("i_local_dir_trash", Integer.valueOf(this.localDirTrash));
        contentValues.put("i_local_doc_add", Integer.valueOf(this.localDocAdd));
        contentValues.put("i_local_doc_update", Integer.valueOf(this.localDocUpdate));
        contentValues.put("i_local_doc_trash", Integer.valueOf(this.localDocTrash));
        contentValues.put("i_local_page_add", Integer.valueOf(this.localPageAdd));
        contentValues.put("i_local_page_update", Integer.valueOf(this.localPageUpdate));
        contentValues.put("i_local_page_trash", Integer.valueOf(this.localPageTrash));
        contentValues.put("i_remote_dir_add", Integer.valueOf(this.remoteDirAdd));
        contentValues.put("i_remote_dir_update", Integer.valueOf(this.remoteDirUpdate));
        contentValues.put("i_remote_dir_trash", Integer.valueOf(this.remoteDirTrash));
        contentValues.put("i_remote_doc_add", Integer.valueOf(this.remoteDocAdd));
        contentValues.put("i_remote_doc_update", Integer.valueOf(this.remoteDocUpdate));
        contentValues.put("i_remote_doc_trash", Integer.valueOf(this.remoteDocTrash));
        contentValues.put("i_remote_page_add", Integer.valueOf(this.remotePageAdd));
        contentValues.put("i_remote_page_update", Integer.valueOf(this.remotePageUpdate));
        contentValues.put("i_remote_page_trash", Integer.valueOf(this.remotePageTrash));
        return contentValues;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof PSSyncHistory) && this.syncHistoryId == ((PSSyncHistory) obj).syncHistoryId;
    }

    public final int hashCode() {
        return this.syncHistoryId;
    }

    public PSSyncHistory() {
    }
}
