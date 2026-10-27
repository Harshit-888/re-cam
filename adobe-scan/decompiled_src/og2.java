package defpackage;

import com.adobe.dcmscan.util.AppDatabase_Impl;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/* JADX INFO: compiled from: AppDatabase_Impl.kt */
/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes2.dex */
public final class og2 extends okh {
    public final /* synthetic */ AppDatabase_Impl d;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public og2(AppDatabase_Impl appDatabase_Impl) {
        super(2, "767695d44f642dea14414e73aadbe106", "54769e6844efea8fd32bf1640445bc7b");
        this.d = appDatabase_Impl;
    }

    public final void a(woh wohVar) {
        wohVar.getClass();
        voh.a(wohVar, "CREATE TABLE IF NOT EXISTS `FileDescriptor` (`mPrimaryKey` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uri_path` TEXT, `modified_date` INTEGER NOT NULL, `is_document` INTEGER NOT NULL)");
        voh.a(wohVar, "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        voh.a(wohVar, "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '767695d44f642dea14414e73aadbe106')");
    }

    public final void b(woh wohVar) {
        wohVar.getClass();
        voh.a(wohVar, "DROP TABLE IF EXISTS `FileDescriptor`");
    }

    public final void c(woh wohVar) {
        wohVar.getClass();
    }

    public final void d(woh wohVar) {
        wohVar.getClass();
        int i = AppDatabase_Impl.c;
        this.d.internalInitInvalidationTracker(wohVar);
    }

    public final void e(woh wohVar) {
        wohVar.getClass();
    }

    public final void f(woh wohVar) {
        wohVar.getClass();
        to6.a(wohVar);
    }

    /* JADX WARN: Type inference failed for: r11v3, types: [okh$a] */
    /* JADX WARN: Type inference failed for: r12v3, types: [okh$a] */
    public final okh$a g(woh wohVar) {
        wohVar.getClass();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("mPrimaryKey", new v5l.a("mPrimaryKey", "INTEGER", true, 1, null, 1));
        linkedHashMap.put("uri_path", new v5l.a("uri_path", "TEXT", false, 0, null, 1));
        linkedHashMap.put("modified_date", new v5l.a("modified_date", "INTEGER", true, 0, null, 1));
        linkedHashMap.put("is_document", new v5l.a("is_document", "INTEGER", true, 0, null, 1));
        v5l v5lVar = new v5l("FileDescriptor", linkedHashMap, new LinkedHashSet(), new LinkedHashSet());
        v5l v5lVarA = v5l.b.a(wohVar, "FileDescriptor");
        if (v5lVar.equals(v5lVarA)) {
            final boolean z = true;
            final String str = null;
            return new Object(z, str) { // from class: okh$a
                public final boolean a;
                public final String b;

                {
                    this.a = z;
                    this.b = str;
                }
            };
        }
        final String strB = ds0.b("FileDescriptor(com.adobe.dcmscan.util.FileDescriptor).\n Expected:\n", v5lVar, "\n Found:\n", v5lVarA);
        final boolean z2 = false;
        return new Object(z2, strB) { // from class: okh$a
            public final boolean a;
            public final String b;

            {
                this.a = z2;
                this.b = strB;
            }
        };
    }
}
