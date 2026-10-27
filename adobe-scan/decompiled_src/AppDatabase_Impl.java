package com.adobe.dcmscan.util;

import androidx.room.c;
import defpackage.ac9;
import defpackage.og2;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* JADX INFO: compiled from: AppDatabase_Impl.kt */
/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes2.dex */
public final class AppDatabase_Impl extends AppDatabase {
    public static final /* synthetic */ int c = 0;
    public final b3l b = new b3l(new qc0(1, this));

    public final void clearAllTables() {
        performClear(false, new String[]{"FileDescriptor"});
    }

    public final List<xsd> createAutoMigrations(Map<u9c<? extends ou2>, ? extends ou2> map) {
        map.getClass();
        return new ArrayList();
    }

    public final c createInvalidationTracker() {
        return new c(this, new LinkedHashMap(), new LinkedHashMap(), new String[]{"FileDescriptor"});
    }

    public final pkh createOpenDelegate() {
        return new og2(this);
    }

    @Override // com.adobe.dcmscan.util.AppDatabase
    public final ac9 e() {
        return (ac9) this.b.getValue();
    }

    public final Set<u9c<? extends ou2>> getRequiredAutoMigrationSpecClasses() {
        return new LinkedHashSet();
    }

    public final Map<u9c<?>, List<u9c<?>>> getRequiredTypeConverterClasses() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put(g2h.a(ac9.class), el8.a);
        return linkedHashMap;
    }
}
