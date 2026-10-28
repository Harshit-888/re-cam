.class public final Lbx3;
.super Ljava/lang/Object;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"

# interfaces
.implements Lxt3;
.implements Lhs4;
.implements Lcz3;
.implements Ll95;
.implements Lj42;
.implements Lsq1;
.implements Liq1;
.implements Lja3;


# static fields
.field public static e:Lbx3;


# instance fields
.field public final synthetic a:I

.field public b:Ljava/lang/Object;

.field public c:Ljava/lang/Object;

.field public d:Ljava/lang/Object;


# direct methods
.method public synthetic constructor <init>(I)V
    .locals 0

    .line 134
    iput p1, p0, Lbx3;->a:I

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public synthetic constructor <init>(ILjava/lang/Object;Ljava/lang/Object;)V
    .locals 0

    .line 95
    iput p1, p0, Lbx3;->a:I

    iput-object p2, p0, Lbx3;->b:Ljava/lang/Object;

    iput-object p3, p0, Lbx3;->c:Ljava/lang/Object;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public synthetic constructor <init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Z)V
    .locals 0

    .line 96
    iput p1, p0, Lbx3;->a:I

    iput-object p2, p0, Lbx3;->b:Ljava/lang/Object;

    iput-object p3, p0, Lbx3;->c:Ljava/lang/Object;

    iput-object p4, p0, Lbx3;->d:Ljava/lang/Object;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .locals 5

    .line 1
    const/16 v0, 0xc

    .line 2
    .line 3
    iput v0, p0, Lbx3;->a:I

    .line 4
    .line 5
    sget-object v0, Llf2;->o:Llf2;

    .line 6
    .line 7
    const-string v1, "ImagePipelineFactory was not initialized!"

    .line 8
    .line 9
    invoke-static {v0, v1}, Lpr0;->i(Ljava/lang/Object;Ljava/lang/String;)V

    .line 10
    .line 11
    .line 12
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 13
    .line 14
    .line 15
    iput-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 16
    .line 17
    invoke-virtual {v0}, Llf2;->e()Lif2;

    .line 18
    .line 19
    .line 20
    move-result-object v1

    .line 21
    iput-object v1, p0, Lbx3;->c:Ljava/lang/Object;

    .line 22
    .line 23
    new-instance v2, Lvi5;

    .line 24
    .line 25
    const/16 v3, 0xd

    .line 26
    .line 27
    const/4 v4, 0x0

    .line 28
    invoke-direct {v2, v3, v4}, Lvi5;-><init>(IZ)V

    .line 29
    .line 30
    .line 31
    iput-object v2, p0, Lbx3;->d:Ljava/lang/Object;

    .line 32
    .line 33
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    .line 34
    .line 35
    .line 36
    move-result-object p0

    .line 37
    const-class p1, Lo71;

    .line 38
    .line 39
    monitor-enter p1

    .line 40
    :try_start_0
    sget-object v3, Lo71;->g:Lo71;

    .line 41
    .line 42
    if-nez v3, :cond_0

    .line 43
    .line 44
    new-instance v3, Lo71;

    .line 45
    .line 46
    invoke-direct {v3}, Lo71;-><init>()V

    .line 47
    .line 48
    .line 49
    sput-object v3, Lo71;->g:Lo71;

    .line 50
    .line 51
    goto :goto_0

    .line 52
    :catchall_0
    move-exception p0

    .line 53
    goto :goto_1

    .line 54
    :cond_0
    :goto_0
    sget-object v3, Lo71;->g:Lo71;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 55
    .line 56
    monitor-exit p1

    .line 57
    invoke-virtual {v0}, Llf2;->a()V

    .line 58
    .line 59
    .line 60
    iget-object p1, v0, Llf2;->b:Ljava/lang/Object;

    .line 61
    .line 62
    check-cast p1, Lkf2;

    .line 63
    .line 64
    iget-object p1, p1, Lkf2;->t:Lta4;

    .line 65
    .line 66
    invoke-virtual {p1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 67
    .line 68
    .line 69
    sget-object p1, Lwm5;->b:Lwm5;

    .line 70
    .line 71
    if-nez p1, :cond_1

    .line 72
    .line 73
    new-instance p1, Lwm5;

    .line 74
    .line 75
    invoke-direct {p1}, Lwm5;-><init>()V

    .line 76
    .line 77
    .line 78
    sput-object p1, Lwm5;->b:Lwm5;

    .line 79
    .line 80
    :cond_1
    sget-object p1, Lwm5;->b:Lwm5;

    .line 81
    .line 82
    iget-object v0, v1, Lif2;->e:Luc3;

    .line 83
    .line 84
    iput-object p0, v2, Lvi5;->b:Ljava/lang/Object;

    .line 85
    .line 86
    iput-object v3, v2, Lvi5;->c:Ljava/lang/Object;

    .line 87
    .line 88
    iput-object p1, v2, Lvi5;->d:Ljava/lang/Object;

    .line 89
    .line 90
    iput-object v0, v2, Lvi5;->e:Ljava/lang/Object;

    .line 91
    .line 92
    return-void

    .line 93
    :goto_1
    :try_start_1
    monitor-exit p1
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 94
    throw p0
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/location/LocationManager;)V
    .locals 1

    const/16 v0, 0x1d

    iput v0, p0, Lbx3;->a:I

    .line 128
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 129
    new-instance v0, Lb95;

    .line 130
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V

    .line 131
    iput-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 132
    iput-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 133
    iput-object p2, p0, Lbx3;->c:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Landroid/hardware/camera2/params/StreamConfigurationMap;Lw63;)V
    .locals 2

    const/16 v0, 0x16

    iput v0, p0, Lbx3;->a:I

    .line 113
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 114
    new-instance v0, Ljava/util/HashMap;

    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    iput-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 115
    new-instance v0, Ljava/util/HashMap;

    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    .line 116
    new-instance v0, Ljava/util/HashMap;

    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    .line 117
    new-instance v0, Ldu3;

    const/16 v1, 0x1d

    invoke-direct {v0, v1, p1}, Ldu3;-><init>(ILjava/lang/Object;)V

    iput-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 118
    iput-object p2, p0, Lbx3;->c:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Landroid/media/MediaCodec;)V
    .locals 2

    const/16 v0, 0x1a

    iput v0, p0, Lbx3;->a:I

    .line 145
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 146
    iput-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 147
    sget v0, Lfr5;->a:I

    const/16 v1, 0x15

    if-ge v0, v1, :cond_0

    .line 148
    invoke-virtual {p1}, Landroid/media/MediaCodec;->getInputBuffers()[Ljava/nio/ByteBuffer;

    move-result-object v0

    iput-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 149
    invoke-virtual {p1}, Landroid/media/MediaCodec;->getOutputBuffers()[Ljava/nio/ByteBuffer;

    move-result-object p1

    iput-object p1, p0, Lbx3;->d:Ljava/lang/Object;

    :cond_0
    return-void
.end method

.method public constructor <init>(Landroidx/work/impl/WorkDatabase_Impl;)V
    .locals 2

    const/16 v0, 0x1b

    iput v0, p0, Lbx3;->a:I

    .line 102
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 103
    iput-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 104
    new-instance v0, Lt81;

    const/4 v1, 0x2

    .line 105
    invoke-direct {v0, p1, v1}, Lt81;-><init>(Landroidx/work/impl/WorkDatabase;I)V

    .line 106
    iput-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 107
    new-instance v0, Lzb5;

    const/4 v1, 0x0

    .line 108
    invoke-direct {v0, p1, v1}, Lzb5;-><init>(Landroidx/work/impl/WorkDatabase;I)V

    .line 109
    iput-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Ldu3;)V
    .locals 1

    const/16 v0, 0x14

    iput v0, p0, Lbx3;->a:I

    .line 153
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 154
    iput-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 155
    new-instance p1, Ljava/util/concurrent/locks/ReentrantLock;

    invoke-direct {p1}, Ljava/util/concurrent/locks/ReentrantLock;-><init>()V

    iput-object p1, p0, Lbx3;->c:Ljava/lang/Object;

    .line 156
    new-instance p1, Ljava/util/WeakHashMap;

    invoke-direct {p1}, Ljava/util/WeakHashMap;-><init>()V

    iput-object p1, p0, Lbx3;->d:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Lex2;)V
    .locals 1

    const/16 v0, 0x13

    iput v0, p0, Lbx3;->a:I

    .line 110
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 111
    new-instance v0, Landroidx/lifecycle/a;

    invoke-direct {v0, p1}, Landroidx/lifecycle/a;-><init>(Lcx2;)V

    iput-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 112
    new-instance p1, Landroid/os/Handler;

    invoke-direct {p1}, Landroid/os/Handler;-><init>()V

    iput-object p1, p0, Lbx3;->c:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Lf55;Ljava/net/HttpURLConnection;)V
    .locals 1

    const/16 v0, 0x15

    iput v0, p0, Lbx3;->a:I

    .line 135
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 136
    iput-object p1, p0, Lbx3;->d:Ljava/lang/Object;

    .line 137
    iput-object p2, p0, Lbx3;->c:Ljava/lang/Object;

    .line 138
    new-instance p1, Lr00;

    const/4 v0, 0x1

    .line 139
    invoke-virtual {p2, v0}, Ljava/net/URLConnection;->setDoOutput(Z)V

    .line 140
    invoke-virtual {p2}, Ljava/net/URLConnection;->getOutputStream()Ljava/io/OutputStream;

    move-result-object v0

    .line 141
    invoke-direct {p1}, Lr00;-><init>()V

    .line 142
    iput-object v0, p1, Lr00;->b:Ljava/lang/Object;

    .line 143
    iput-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 144
    invoke-virtual {p2}, Ljava/net/URLConnection;->connect()V

    return-void
.end method

.method public constructor <init>(Liq1;La95;)V
    .locals 1

    const/16 v0, 0x17

    iput v0, p0, Lbx3;->a:I

    .line 124
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 125
    iput-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 126
    iput-object p2, p0, Lbx3;->c:Ljava/lang/Object;

    .line 127
    new-instance p1, Landroid/util/SparseArray;

    invoke-direct {p1}, Landroid/util/SparseArray;-><init>()V

    iput-object p1, p0, Lbx3;->d:Ljava/lang/Object;

    return-void
.end method

.method public synthetic constructor <init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)V
    .locals 0

    .line 97
    iput p4, p0, Lbx3;->a:I

    iput-object p1, p0, Lbx3;->d:Ljava/lang/Object;

    iput-object p2, p0, Lbx3;->b:Ljava/lang/Object;

    iput-object p3, p0, Lbx3;->c:Ljava/lang/Object;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public constructor <init>(Ljava/lang/String;)V
    .locals 1

    const/16 v0, 0x9

    iput v0, p0, Lbx3;->a:I

    .line 119
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 120
    new-instance v0, Lf02;

    invoke-direct {v0}, Lf02;-><init>()V

    .line 121
    invoke-static {p1}, Ldf3;->i(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    iput-object p1, v0, Lf02;->l:Ljava/lang/String;

    .line 122
    new-instance p1, Landroidx/media3/common/b;

    invoke-direct {p1, v0}, Landroidx/media3/common/b;-><init>(Lf02;)V

    .line 123
    iput-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Ljava/util/concurrent/Future;Li23;Ljava/util/concurrent/Executor;)V
    .locals 1

    const/16 v0, 0xb

    iput v0, p0, Lbx3;->a:I

    invoke-virtual {p2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    invoke-virtual {p3}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 98
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 99
    iput-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 100
    iput-object p2, p0, Lbx3;->c:Ljava/lang/Object;

    .line 101
    iput-object p3, p0, Lbx3;->d:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Lu80;Lq61;)V
    .locals 1

    const/16 v0, 0x18

    iput v0, p0, Lbx3;->a:I

    .line 150
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 151
    iput-object p1, p0, Lbx3;->c:Ljava/lang/Object;

    .line 152
    iput-object p2, p0, Lbx3;->b:Ljava/lang/Object;

    return-void
.end method

.method public static J(Landroid/net/Uri;Ljava/io/File;)V
    .locals 3

    .line 1
    const/4 v0, 0x0

    .line 2
    :try_start_0
    sget-object v1, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 3
    .line 4
    invoke-virtual {v1}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    .line 5
    .line 6
    .line 7
    move-result-object v1

    .line 8
    invoke-virtual {v1, p0}, Landroid/content/ContentResolver;->openInputStream(Landroid/net/Uri;)Ljava/io/InputStream;

    .line 9
    .line 10
    .line 11
    move-result-object p0
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_3
    .catchall {:try_start_0 .. :try_end_0} :catchall_2

    .line 12
    :try_start_1
    new-instance v1, Ljava/io/FileOutputStream;

    .line 13
    .line 14
    invoke-direct {v1, p1}, Ljava/io/FileOutputStream;-><init>(Ljava/io/File;)V
    :try_end_1
    .catch Ljava/lang/Exception; {:try_start_1 .. :try_end_1} :catch_2
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    .line 15
    .line 16
    .line 17
    :try_start_2
    invoke-static {p0, v1}, Lgd2;->b(Ljava/io/InputStream;Ljava/io/OutputStream;)V

    .line 18
    .line 19
    .line 20
    invoke-static {p1}, Ls46;->z(Ljava/io/File;)Z

    .line 21
    .line 22
    .line 23
    move-result p1
    :try_end_2
    .catch Ljava/lang/Exception; {:try_start_2 .. :try_end_2} :catch_1
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    .line 24
    if-eqz p1, :cond_0

    .line 25
    .line 26
    :try_start_3
    invoke-virtual {p0}, Ljava/io/InputStream;->close()V

    .line 27
    .line 28
    .line 29
    invoke-virtual {v1}, Ljava/io/OutputStream;->close()V
    :try_end_3
    .catch Ljava/lang/Exception; {:try_start_3 .. :try_end_3} :catch_0

    .line 30
    .line 31
    .line 32
    :catch_0
    return-void

    .line 33
    :cond_0
    :try_start_4
    new-instance p1, Lcom/indymobile/app/util/PSException;

    .line 34
    .line 35
    const-string v0, "Unsupported file type"

    .line 36
    .line 37
    invoke-direct {p1, v0}, Lcom/indymobile/app/util/PSException;-><init>(Ljava/lang/String;)V

    .line 38
    .line 39
    .line 40
    throw p1
    :try_end_4
    .catch Ljava/lang/Exception; {:try_start_4 .. :try_end_4} :catch_1
    .catchall {:try_start_4 .. :try_end_4} :catchall_0

    .line 41
    :catchall_0
    move-exception p1

    .line 42
    :goto_0
    move-object v0, p0

    .line 43
    goto :goto_3

    .line 44
    :catch_1
    move-exception p1

    .line 45
    :goto_1
    move-object v0, p0

    .line 46
    goto :goto_2

    .line 47
    :catchall_1
    move-exception p1

    .line 48
    move-object v1, v0

    .line 49
    goto :goto_0

    .line 50
    :catch_2
    move-exception p1

    .line 51
    move-object v1, v0

    .line 52
    goto :goto_1

    .line 53
    :catchall_2
    move-exception p1

    .line 54
    move-object v1, v0

    .line 55
    goto :goto_3

    .line 56
    :catch_3
    move-exception p1

    .line 57
    move-object v1, v0

    .line 58
    :goto_2
    :try_start_5
    instance-of p0, p1, Lcom/indymobile/app/util/PSException;

    .line 59
    .line 60
    if-eqz p0, :cond_1

    .line 61
    .line 62
    check-cast p1, Lcom/indymobile/app/util/PSException;

    .line 63
    .line 64
    throw p1

    .line 65
    :catchall_3
    move-exception p1

    .line 66
    goto :goto_3

    .line 67
    :cond_1
    new-instance p0, Lcom/indymobile/app/util/PSException;

    .line 68
    .line 69
    const v2, 0x7f120044

    .line 70
    .line 71
    .line 72
    invoke-static {v2}, Lwv3;->b(I)Ljava/lang/String;

    .line 73
    .line 74
    .line 75
    move-result-object v2

    .line 76
    invoke-direct {p0, v2, p1}, Ljava/lang/Exception;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    .line 77
    .line 78
    .line 79
    throw p0
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_3

    .line 80
    :goto_3
    :try_start_6
    invoke-virtual {v0}, Ljava/io/InputStream;->close()V

    .line 81
    .line 82
    .line 83
    invoke-virtual {v1}, Ljava/io/OutputStream;->close()V
    :try_end_6
    .catch Ljava/lang/Exception; {:try_start_6 .. :try_end_6} :catch_4

    .line 84
    .line 85
    .line 86
    :catch_4
    throw p1
.end method

.method public static K(Lcom/indymobile/app/activity/editor/markup/MarkupActivity;Ljava/util/ArrayList;Lzv3;)V
    .locals 11

    .line 1
    invoke-virtual {p1}, Ljava/util/ArrayList;->size()I

    .line 2
    .line 3
    .line 4
    move-result v0

    .line 5
    const/4 v1, 0x0

    .line 6
    const/4 v2, 0x0

    .line 7
    move-object v3, v1

    .line 8
    move v4, v2

    .line 9
    :goto_0
    if-ge v4, v0, :cond_9

    .line 10
    .line 11
    invoke-virtual {p1, v4}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    .line 12
    .line 13
    .line 14
    move-result-object v5

    .line 15
    add-int/lit8 v4, v4, 0x1

    .line 16
    .line 17
    check-cast v5, Lcom/indymobile/app/model/PSPage;

    .line 18
    .line 19
    :try_start_0
    invoke-virtual {v5}, Lcom/indymobile/app/model/PSPage;->n()Z

    .line 20
    .line 21
    .line 22
    move-result v6

    .line 23
    if-nez v6, :cond_0

    .line 24
    .line 25
    goto :goto_0

    .line 26
    :cond_0
    invoke-virtual {v5}, Lcom/indymobile/app/model/PSPage;->k()Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 27
    .line 28
    .line 29
    move-result-object v6

    .line 30
    if-nez v6, :cond_1

    .line 31
    .line 32
    goto :goto_0

    .line 33
    :cond_1
    invoke-static {v1, v5}, Lt41;->F(Landroid/graphics/Bitmap;Lcom/indymobile/app/model/PSPage;)Landroid/graphics/Bitmap;

    .line 34
    .line 35
    .line 36
    move-result-object v7

    .line 37
    invoke-static {v7}, Lfa1;->H(Landroid/graphics/Bitmap;)Lcom/indymobile/app/model/PSImageEstimationInfoBean;

    .line 38
    .line 39
    .line 40
    move-result-object v8

    .line 41
    iget-object v9, v6, Lcom/indymobile/app/model/PSDocumentProcessInfo;->enhanceType:Lcv3;

    .line 42
    .line 43
    sget-object v10, Lcv3;->kPSEnhanceTypeOriginal:Lcv3;

    .line 44
    .line 45
    if-ne v9, v10, :cond_2

    .line 46
    .line 47
    iget-object v6, v6, Lcom/indymobile/app/model/PSDocumentProcessInfo;->colorType:Lyu3;

    .line 48
    .line 49
    invoke-static {v7, v6}, Lqx3;->E(Landroid/graphics/Bitmap;Lyu3;)Lry;

    .line 50
    .line 51
    .line 52
    move-result-object v6

    .line 53
    goto :goto_2

    .line 54
    :catchall_0
    move-exception v3

    .line 55
    goto :goto_3

    .line 56
    :cond_2
    sget-object v10, Lcv3;->kPSEnhanceTypeLighten:Lcv3;

    .line 57
    .line 58
    if-ne v9, v10, :cond_3

    .line 59
    .line 60
    iget v9, v8, Lcom/indymobile/app/model/PSImageEstimationInfoBean;->whitePoint:F

    .line 61
    .line 62
    iget v8, v8, Lcom/indymobile/app/model/PSImageEstimationInfoBean;->blackPoint:F

    .line 63
    .line 64
    iget-object v6, v6, Lcom/indymobile/app/model/PSDocumentProcessInfo;->colorType:Lyu3;

    .line 65
    .line 66
    invoke-static {v7, v9, v8, v6}, Ltw3;->E(Landroid/graphics/Bitmap;FFLyu3;)Lry;

    .line 67
    .line 68
    .line 69
    move-result-object v6

    .line 70
    goto :goto_2

    .line 71
    :cond_3
    sget-object v8, Lcv3;->kPSEnhanceTypeDocumentLevel1:Lcv3;

    .line 72
    .line 73
    if-eq v9, v8, :cond_5

    .line 74
    .line 75
    sget-object v8, Lcv3;->kPSEnhanceTypeDocumentLevel2:Lcv3;

    .line 76
    .line 77
    if-ne v9, v8, :cond_4

    .line 78
    .line 79
    goto :goto_1

    .line 80
    :cond_4
    move-object v6, v1

    .line 81
    goto :goto_2

    .line 82
    :cond_5
    :goto_1
    iget-object v6, v6, Lcom/indymobile/app/model/PSDocumentProcessInfo;->colorType:Lyu3;

    .line 83
    .line 84
    invoke-static {v7, v9, v6}, Llu3;->E(Landroid/graphics/Bitmap;Lcv3;Lyu3;)Lry;

    .line 85
    .line 86
    .line 87
    move-result-object v6

    .line 88
    :goto_2
    iget-object v8, v6, Lry;->a:Landroid/graphics/Bitmap;

    .line 89
    .line 90
    if-eq v7, v8, :cond_6

    .line 91
    .line 92
    invoke-static {v7}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 93
    .line 94
    .line 95
    :cond_6
    invoke-virtual {v5}, Lcom/indymobile/app/model/PSPage;->j()Lcom/indymobile/app/model/PSDocumentFilterInfo;

    .line 96
    .line 97
    .line 98
    move-result-object v7

    .line 99
    iget v6, v6, Lry;->b:I

    .line 100
    .line 101
    iput v6, v7, Lcom/indymobile/app/model/PSDocumentFilterInfo;->bgColor:I

    .line 102
    .line 103
    sget-object v6, Ldv3;->kPageId:Ldv3;

    .line 104
    .line 105
    invoke-static {v5, v8, v1, v2, v6}, Lyn0;->f(Lcom/indymobile/app/model/PSPage;Landroid/graphics/Bitmap;Landroid/graphics/Paint;ZLdv3;)Ljava/io/File;

    .line 106
    .line 107
    .line 108
    invoke-static {v8}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 109
    .line 110
    .line 111
    invoke-static {v5}, Lgx3;->f(Lcom/indymobile/app/model/PSPage;)Z

    .line 112
    .line 113
    .line 114
    move-result v6

    .line 115
    if-eqz v6, :cond_7

    .line 116
    .line 117
    invoke-virtual {p0}, Lsf;->getResources()Landroid/content/res/Resources;

    .line 118
    .line 119
    .line 120
    invoke-static {v5}, Lgx3;->q(Lcom/indymobile/app/model/PSPage;)V

    .line 121
    .line 122
    .line 123
    :cond_7
    iget-object v6, p2, Lzv3;->b:Ljava/lang/Object;

    .line 124
    .line 125
    check-cast v6, Lio3;

    .line 126
    .line 127
    invoke-virtual {v6, v5}, Lio3;->k(Ljava/lang/Object;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 128
    .line 129
    .line 130
    goto :goto_0

    .line 131
    :goto_3
    instance-of v5, v3, Lcom/indymobile/app/util/PSException;

    .line 132
    .line 133
    if-eqz v5, :cond_8

    .line 134
    .line 135
    goto :goto_0

    .line 136
    :cond_8
    new-instance v5, Lcom/indymobile/app/util/PSException;

    .line 137
    .line 138
    invoke-direct {v5, v3}, Lcom/indymobile/app/util/PSException;-><init>(Ljava/lang/Throwable;)V

    .line 139
    .line 140
    .line 141
    move-object v3, v5

    .line 142
    goto/16 :goto_0

    .line 143
    .line 144
    :cond_9
    if-nez v3, :cond_a

    .line 145
    .line 146
    return-void

    .line 147
    :cond_a
    throw v3
.end method


# virtual methods
.method public A(I)[Landroid/util/Size;
    .locals 16

    .line 1
    move-object/from16 v0, p0

    .line 2
    .line 3
    move/from16 v1, p1

    .line 4
    .line 5
    iget-object v2, v0, Lbx3;->d:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast v2, Ljava/util/HashMap;

    .line 8
    .line 9
    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 10
    .line 11
    .line 12
    move-result-object v3

    .line 13
    invoke-virtual {v2, v3}, Ljava/util/HashMap;->containsKey(Ljava/lang/Object;)Z

    .line 14
    .line 15
    .line 16
    move-result v3

    .line 17
    if-eqz v3, :cond_1

    .line 18
    .line 19
    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 20
    .line 21
    .line 22
    move-result-object v0

    .line 23
    invoke-virtual {v2, v0}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 24
    .line 25
    .line 26
    move-result-object v0

    .line 27
    check-cast v0, [Landroid/util/Size;

    .line 28
    .line 29
    if-nez v0, :cond_0

    .line 30
    .line 31
    const/4 v0, 0x0

    .line 32
    return-object v0

    .line 33
    :cond_0
    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 34
    .line 35
    .line 36
    move-result-object v0

    .line 37
    invoke-virtual {v2, v0}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 38
    .line 39
    .line 40
    move-result-object v0

    .line 41
    check-cast v0, [Landroid/util/Size;

    .line 42
    .line 43
    invoke-virtual {v0}, [Landroid/util/Size;->clone()Ljava/lang/Object;

    .line 44
    .line 45
    .line 46
    move-result-object v0

    .line 47
    check-cast v0, [Landroid/util/Size;

    .line 48
    .line 49
    return-object v0

    .line 50
    :cond_1
    iget-object v3, v0, Lbx3;->b:Ljava/lang/Object;

    .line 51
    .line 52
    check-cast v3, Ldu3;

    .line 53
    .line 54
    iget-object v3, v3, Ldu3;->b:Ljava/lang/Object;

    .line 55
    .line 56
    check-cast v3, Landroid/hardware/camera2/params/StreamConfigurationMap;

    .line 57
    .line 58
    invoke-virtual {v3, v1}, Landroid/hardware/camera2/params/StreamConfigurationMap;->getOutputSizes(I)[Landroid/util/Size;

    .line 59
    .line 60
    .line 61
    move-result-object v3

    .line 62
    if-eqz v3, :cond_17

    .line 63
    .line 64
    array-length v4, v3

    .line 65
    if-nez v4, :cond_2

    .line 66
    .line 67
    goto/16 :goto_5

    .line 68
    .line 69
    :cond_2
    iget-object v0, v0, Lbx3;->c:Ljava/lang/Object;

    .line 70
    .line 71
    check-cast v0, Lw63;

    .line 72
    .line 73
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 74
    .line 75
    .line 76
    new-instance v4, Ljava/util/ArrayList;

    .line 77
    .line 78
    invoke-static {v3}, Ljava/util/Arrays;->asList([Ljava/lang/Object;)Ljava/util/List;

    .line 79
    .line 80
    .line 81
    move-result-object v3

    .line 82
    invoke-direct {v4, v3}, Ljava/util/ArrayList;-><init>(Ljava/util/Collection;)V

    .line 83
    .line 84
    .line 85
    iget-object v3, v0, Lw63;->b:Ljava/lang/Object;

    .line 86
    .line 87
    check-cast v3, Landroidx/camera/camera2/internal/compat/quirk/ExtraSupportedOutputSizeQuirk;

    .line 88
    .line 89
    const/4 v5, 0x0

    .line 90
    const/16 v6, 0x2d0

    .line 91
    .line 92
    const/16 v7, 0x438

    .line 93
    .line 94
    const/16 v8, 0x5a0

    .line 95
    .line 96
    const/16 v9, 0x22

    .line 97
    .line 98
    if-nez v3, :cond_3

    .line 99
    .line 100
    goto :goto_1

    .line 101
    :cond_3
    if-ne v1, v9, :cond_4

    .line 102
    .line 103
    const-string v3, "motorola"

    .line 104
    .line 105
    sget-object v10, Landroid/os/Build;->BRAND:Ljava/lang/String;

    .line 106
    .line 107
    invoke-virtual {v3, v10}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 108
    .line 109
    .line 110
    move-result v3

    .line 111
    if-eqz v3, :cond_4

    .line 112
    .line 113
    const-string v3, "moto e5 play"

    .line 114
    .line 115
    sget-object v10, Landroid/os/Build;->MODEL:Ljava/lang/String;

    .line 116
    .line 117
    invoke-virtual {v3, v10}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 118
    .line 119
    .line 120
    move-result v3

    .line 121
    if-eqz v3, :cond_4

    .line 122
    .line 123
    new-instance v3, Landroid/util/Size;

    .line 124
    .line 125
    invoke-direct {v3, v8, v7}, Landroid/util/Size;-><init>(II)V

    .line 126
    .line 127
    .line 128
    new-instance v10, Landroid/util/Size;

    .line 129
    .line 130
    const/16 v11, 0x3c0

    .line 131
    .line 132
    invoke-direct {v10, v11, v6}, Landroid/util/Size;-><init>(II)V

    .line 133
    .line 134
    .line 135
    filled-new-array {v3, v10}, [Landroid/util/Size;

    .line 136
    .line 137
    .line 138
    move-result-object v3

    .line 139
    goto :goto_0

    .line 140
    :cond_4
    new-array v3, v5, [Landroid/util/Size;

    .line 141
    .line 142
    :goto_0
    array-length v10, v3

    .line 143
    if-lez v10, :cond_5

    .line 144
    .line 145
    invoke-static {v3}, Ljava/util/Arrays;->asList([Ljava/lang/Object;)Ljava/util/List;

    .line 146
    .line 147
    .line 148
    move-result-object v3

    .line 149
    invoke-virtual {v4, v3}, Ljava/util/ArrayList;->addAll(Ljava/util/Collection;)Z

    .line 150
    .line 151
    .line 152
    :cond_5
    :goto_1
    iget-object v0, v0, Lw63;->c:Ljava/lang/Object;

    .line 153
    .line 154
    check-cast v0, Lj8;

    .line 155
    .line 156
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 157
    .line 158
    .line 159
    const-class v3, Landroidx/camera/camera2/internal/compat/quirk/ExcludedSupportedSizesQuirk;

    .line 160
    .line 161
    sget-object v10, Li91;->a:Lx51;

    .line 162
    .line 163
    invoke-virtual {v10, v3}, Lx51;->b(Ljava/lang/Class;)Lxc4;

    .line 164
    .line 165
    .line 166
    move-result-object v3

    .line 167
    check-cast v3, Landroidx/camera/camera2/internal/compat/quirk/ExcludedSupportedSizesQuirk;

    .line 168
    .line 169
    if-nez v3, :cond_6

    .line 170
    .line 171
    new-instance v0, Ljava/util/ArrayList;

    .line 172
    .line 173
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 174
    .line 175
    .line 176
    goto/16 :goto_3

    .line 177
    .line 178
    :cond_6
    iget-object v0, v0, Lj8;->b:Ljava/lang/String;

    .line 179
    .line 180
    sget-object v3, Landroid/os/Build;->BRAND:Ljava/lang/String;

    .line 181
    .line 182
    const-string v10, "OnePlus"

    .line 183
    .line 184
    invoke-virtual {v10, v3}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 185
    .line 186
    .line 187
    move-result v11

    .line 188
    const/16 v12, 0xbb8

    .line 189
    .line 190
    const/16 v13, 0xfa0

    .line 191
    .line 192
    const/16 v14, 0xc30

    .line 193
    .line 194
    const/16 v15, 0x1040

    .line 195
    .line 196
    const/16 v5, 0x100

    .line 197
    .line 198
    const-string v8, "0"

    .line 199
    .line 200
    if-eqz v11, :cond_8

    .line 201
    .line 202
    const-string v11, "OnePlus6"

    .line 203
    .line 204
    sget-object v7, Landroid/os/Build;->DEVICE:Ljava/lang/String;

    .line 205
    .line 206
    invoke-virtual {v11, v7}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 207
    .line 208
    .line 209
    move-result v7

    .line 210
    if-eqz v7, :cond_8

    .line 211
    .line 212
    new-instance v3, Ljava/util/ArrayList;

    .line 213
    .line 214
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 215
    .line 216
    .line 217
    invoke-virtual {v0, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 218
    .line 219
    .line 220
    move-result v0

    .line 221
    if-eqz v0, :cond_7

    .line 222
    .line 223
    if-ne v1, v5, :cond_7

    .line 224
    .line 225
    new-instance v0, Landroid/util/Size;

    .line 226
    .line 227
    invoke-direct {v0, v15, v14}, Landroid/util/Size;-><init>(II)V

    .line 228
    .line 229
    .line 230
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 231
    .line 232
    .line 233
    new-instance v0, Landroid/util/Size;

    .line 234
    .line 235
    invoke-direct {v0, v13, v12}, Landroid/util/Size;-><init>(II)V

    .line 236
    .line 237
    .line 238
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 239
    .line 240
    .line 241
    :cond_7
    :goto_2
    move-object v0, v3

    .line 242
    goto/16 :goto_3

    .line 243
    .line 244
    :cond_8
    invoke-virtual {v10, v3}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 245
    .line 246
    .line 247
    move-result v7

    .line 248
    if-eqz v7, :cond_9

    .line 249
    .line 250
    const-string v7, "OnePlus6T"

    .line 251
    .line 252
    sget-object v10, Landroid/os/Build;->DEVICE:Ljava/lang/String;

    .line 253
    .line 254
    invoke-virtual {v7, v10}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 255
    .line 256
    .line 257
    move-result v7

    .line 258
    if-eqz v7, :cond_9

    .line 259
    .line 260
    new-instance v3, Ljava/util/ArrayList;

    .line 261
    .line 262
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 263
    .line 264
    .line 265
    invoke-virtual {v0, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 266
    .line 267
    .line 268
    move-result v0

    .line 269
    if-eqz v0, :cond_7

    .line 270
    .line 271
    if-ne v1, v5, :cond_7

    .line 272
    .line 273
    new-instance v0, Landroid/util/Size;

    .line 274
    .line 275
    invoke-direct {v0, v15, v14}, Landroid/util/Size;-><init>(II)V

    .line 276
    .line 277
    .line 278
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 279
    .line 280
    .line 281
    new-instance v0, Landroid/util/Size;

    .line 282
    .line 283
    invoke-direct {v0, v13, v12}, Landroid/util/Size;-><init>(II)V

    .line 284
    .line 285
    .line 286
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 287
    .line 288
    .line 289
    goto :goto_2

    .line 290
    :cond_9
    const-string v7, "HUAWEI"

    .line 291
    .line 292
    invoke-virtual {v7, v3}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 293
    .line 294
    .line 295
    move-result v7

    .line 296
    const/16 v10, 0x23

    .line 297
    .line 298
    if-eqz v7, :cond_b

    .line 299
    .line 300
    const-string v7, "HWANE"

    .line 301
    .line 302
    sget-object v11, Landroid/os/Build;->DEVICE:Ljava/lang/String;

    .line 303
    .line 304
    invoke-virtual {v7, v11}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 305
    .line 306
    .line 307
    move-result v7

    .line 308
    if-eqz v7, :cond_b

    .line 309
    .line 310
    new-instance v3, Ljava/util/ArrayList;

    .line 311
    .line 312
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 313
    .line 314
    .line 315
    invoke-virtual {v0, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 316
    .line 317
    .line 318
    move-result v0

    .line 319
    if-eqz v0, :cond_7

    .line 320
    .line 321
    if-eq v1, v9, :cond_a

    .line 322
    .line 323
    if-eq v1, v10, :cond_a

    .line 324
    .line 325
    goto :goto_2

    .line 326
    :cond_a
    new-instance v0, Landroid/util/Size;

    .line 327
    .line 328
    invoke-direct {v0, v6, v6}, Landroid/util/Size;-><init>(II)V

    .line 329
    .line 330
    .line 331
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 332
    .line 333
    .line 334
    new-instance v0, Landroid/util/Size;

    .line 335
    .line 336
    const/16 v5, 0x190

    .line 337
    .line 338
    invoke-direct {v0, v5, v5}, Landroid/util/Size;-><init>(II)V

    .line 339
    .line 340
    .line 341
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 342
    .line 343
    .line 344
    goto :goto_2

    .line 345
    :cond_b
    invoke-static {}, Landroidx/camera/camera2/internal/compat/quirk/ExcludedSupportedSizesQuirk;->c()Z

    .line 346
    .line 347
    .line 348
    move-result v6

    .line 349
    const-string v7, "1"

    .line 350
    .line 351
    const/16 v12, 0xcc0

    .line 352
    .line 353
    const/16 v14, 0x990

    .line 354
    .line 355
    const/16 v5, 0xc10

    .line 356
    .line 357
    const/16 v11, 0x1020

    .line 358
    .line 359
    const/16 v15, 0x72c

    .line 360
    .line 361
    const/16 v13, 0x912

    .line 362
    .line 363
    if-eqz v6, :cond_f

    .line 364
    .line 365
    new-instance v3, Ljava/util/ArrayList;

    .line 366
    .line 367
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 368
    .line 369
    .line 370
    invoke-virtual {v0, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 371
    .line 372
    .line 373
    move-result v6

    .line 374
    if-eqz v6, :cond_d

    .line 375
    .line 376
    if-eq v1, v9, :cond_c

    .line 377
    .line 378
    if-ne v1, v10, :cond_7

    .line 379
    .line 380
    new-instance v0, Landroid/util/Size;

    .line 381
    .line 382
    invoke-direct {v0, v11, v13}, Landroid/util/Size;-><init>(II)V

    .line 383
    .line 384
    .line 385
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 386
    .line 387
    .line 388
    new-instance v0, Landroid/util/Size;

    .line 389
    .line 390
    invoke-direct {v0, v5, v5}, Landroid/util/Size;-><init>(II)V

    .line 391
    .line 392
    .line 393
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 394
    .line 395
    .line 396
    new-instance v0, Landroid/util/Size;

    .line 397
    .line 398
    invoke-direct {v0, v12, v14}, Landroid/util/Size;-><init>(II)V

    .line 399
    .line 400
    .line 401
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 402
    .line 403
    .line 404
    new-instance v0, Landroid/util/Size;

    .line 405
    .line 406
    invoke-direct {v0, v12, v15}, Landroid/util/Size;-><init>(II)V

    .line 407
    .line 408
    .line 409
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 410
    .line 411
    .line 412
    new-instance v0, Landroid/util/Size;

    .line 413
    .line 414
    const/16 v5, 0x800

    .line 415
    .line 416
    const/16 v6, 0x600

    .line 417
    .line 418
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 419
    .line 420
    .line 421
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 422
    .line 423
    .line 424
    new-instance v0, Landroid/util/Size;

    .line 425
    .line 426
    const/16 v6, 0x480

    .line 427
    .line 428
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 429
    .line 430
    .line 431
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 432
    .line 433
    .line 434
    new-instance v0, Landroid/util/Size;

    .line 435
    .line 436
    const/16 v5, 0x438

    .line 437
    .line 438
    const/16 v6, 0x780

    .line 439
    .line 440
    invoke-direct {v0, v6, v5}, Landroid/util/Size;-><init>(II)V

    .line 441
    .line 442
    .line 443
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 444
    .line 445
    .line 446
    goto/16 :goto_2

    .line 447
    .line 448
    :cond_c
    new-instance v0, Landroid/util/Size;

    .line 449
    .line 450
    const/16 v6, 0xc18

    .line 451
    .line 452
    invoke-direct {v0, v11, v6}, Landroid/util/Size;-><init>(II)V

    .line 453
    .line 454
    .line 455
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 456
    .line 457
    .line 458
    new-instance v0, Landroid/util/Size;

    .line 459
    .line 460
    invoke-direct {v0, v11, v13}, Landroid/util/Size;-><init>(II)V

    .line 461
    .line 462
    .line 463
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 464
    .line 465
    .line 466
    new-instance v0, Landroid/util/Size;

    .line 467
    .line 468
    invoke-direct {v0, v5, v5}, Landroid/util/Size;-><init>(II)V

    .line 469
    .line 470
    .line 471
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 472
    .line 473
    .line 474
    new-instance v0, Landroid/util/Size;

    .line 475
    .line 476
    invoke-direct {v0, v12, v14}, Landroid/util/Size;-><init>(II)V

    .line 477
    .line 478
    .line 479
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 480
    .line 481
    .line 482
    new-instance v0, Landroid/util/Size;

    .line 483
    .line 484
    invoke-direct {v0, v12, v15}, Landroid/util/Size;-><init>(II)V

    .line 485
    .line 486
    .line 487
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 488
    .line 489
    .line 490
    new-instance v0, Landroid/util/Size;

    .line 491
    .line 492
    const/16 v5, 0x800

    .line 493
    .line 494
    const/16 v6, 0x600

    .line 495
    .line 496
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 497
    .line 498
    .line 499
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 500
    .line 501
    .line 502
    new-instance v0, Landroid/util/Size;

    .line 503
    .line 504
    const/16 v6, 0x480

    .line 505
    .line 506
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 507
    .line 508
    .line 509
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 510
    .line 511
    .line 512
    new-instance v0, Landroid/util/Size;

    .line 513
    .line 514
    const/16 v5, 0x438

    .line 515
    .line 516
    const/16 v6, 0x780

    .line 517
    .line 518
    invoke-direct {v0, v6, v5}, Landroid/util/Size;-><init>(II)V

    .line 519
    .line 520
    .line 521
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 522
    .line 523
    .line 524
    goto/16 :goto_2

    .line 525
    .line 526
    :cond_d
    invoke-virtual {v0, v7}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 527
    .line 528
    .line 529
    move-result v0

    .line 530
    if-eqz v0, :cond_7

    .line 531
    .line 532
    if-eq v1, v9, :cond_e

    .line 533
    .line 534
    if-eq v1, v10, :cond_e

    .line 535
    .line 536
    goto/16 :goto_2

    .line 537
    .line 538
    :cond_e
    new-instance v0, Landroid/util/Size;

    .line 539
    .line 540
    invoke-direct {v0, v12, v14}, Landroid/util/Size;-><init>(II)V

    .line 541
    .line 542
    .line 543
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 544
    .line 545
    .line 546
    new-instance v0, Landroid/util/Size;

    .line 547
    .line 548
    invoke-direct {v0, v12, v15}, Landroid/util/Size;-><init>(II)V

    .line 549
    .line 550
    .line 551
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 552
    .line 553
    .line 554
    new-instance v0, Landroid/util/Size;

    .line 555
    .line 556
    invoke-direct {v0, v14, v14}, Landroid/util/Size;-><init>(II)V

    .line 557
    .line 558
    .line 559
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 560
    .line 561
    .line 562
    new-instance v0, Landroid/util/Size;

    .line 563
    .line 564
    const/16 v6, 0x780

    .line 565
    .line 566
    invoke-direct {v0, v6, v6}, Landroid/util/Size;-><init>(II)V

    .line 567
    .line 568
    .line 569
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 570
    .line 571
    .line 572
    new-instance v0, Landroid/util/Size;

    .line 573
    .line 574
    const/16 v5, 0x800

    .line 575
    .line 576
    const/16 v7, 0x600

    .line 577
    .line 578
    invoke-direct {v0, v5, v7}, Landroid/util/Size;-><init>(II)V

    .line 579
    .line 580
    .line 581
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 582
    .line 583
    .line 584
    new-instance v0, Landroid/util/Size;

    .line 585
    .line 586
    const/16 v7, 0x480

    .line 587
    .line 588
    invoke-direct {v0, v5, v7}, Landroid/util/Size;-><init>(II)V

    .line 589
    .line 590
    .line 591
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 592
    .line 593
    .line 594
    new-instance v0, Landroid/util/Size;

    .line 595
    .line 596
    const/16 v5, 0x438

    .line 597
    .line 598
    invoke-direct {v0, v6, v5}, Landroid/util/Size;-><init>(II)V

    .line 599
    .line 600
    .line 601
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 602
    .line 603
    .line 604
    goto/16 :goto_2

    .line 605
    .line 606
    :cond_f
    invoke-static {}, Landroidx/camera/camera2/internal/compat/quirk/ExcludedSupportedSizesQuirk;->b()Z

    .line 607
    .line 608
    .line 609
    move-result v6

    .line 610
    if-eqz v6, :cond_13

    .line 611
    .line 612
    new-instance v3, Ljava/util/ArrayList;

    .line 613
    .line 614
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 615
    .line 616
    .line 617
    invoke-virtual {v0, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 618
    .line 619
    .line 620
    move-result v6

    .line 621
    if-eqz v6, :cond_11

    .line 622
    .line 623
    if-eq v1, v9, :cond_10

    .line 624
    .line 625
    if-ne v1, v10, :cond_7

    .line 626
    .line 627
    new-instance v0, Landroid/util/Size;

    .line 628
    .line 629
    const/16 v5, 0x800

    .line 630
    .line 631
    const/16 v6, 0x600

    .line 632
    .line 633
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 634
    .line 635
    .line 636
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 637
    .line 638
    .line 639
    new-instance v0, Landroid/util/Size;

    .line 640
    .line 641
    const/16 v6, 0x480

    .line 642
    .line 643
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 644
    .line 645
    .line 646
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 647
    .line 648
    .line 649
    new-instance v0, Landroid/util/Size;

    .line 650
    .line 651
    const/16 v5, 0x438

    .line 652
    .line 653
    const/16 v6, 0x780

    .line 654
    .line 655
    invoke-direct {v0, v6, v5}, Landroid/util/Size;-><init>(II)V

    .line 656
    .line 657
    .line 658
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 659
    .line 660
    .line 661
    goto/16 :goto_2

    .line 662
    .line 663
    :cond_10
    new-instance v0, Landroid/util/Size;

    .line 664
    .line 665
    const/16 v6, 0xc18

    .line 666
    .line 667
    invoke-direct {v0, v11, v6}, Landroid/util/Size;-><init>(II)V

    .line 668
    .line 669
    .line 670
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 671
    .line 672
    .line 673
    new-instance v0, Landroid/util/Size;

    .line 674
    .line 675
    invoke-direct {v0, v11, v13}, Landroid/util/Size;-><init>(II)V

    .line 676
    .line 677
    .line 678
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 679
    .line 680
    .line 681
    new-instance v0, Landroid/util/Size;

    .line 682
    .line 683
    invoke-direct {v0, v5, v5}, Landroid/util/Size;-><init>(II)V

    .line 684
    .line 685
    .line 686
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 687
    .line 688
    .line 689
    new-instance v0, Landroid/util/Size;

    .line 690
    .line 691
    invoke-direct {v0, v12, v14}, Landroid/util/Size;-><init>(II)V

    .line 692
    .line 693
    .line 694
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 695
    .line 696
    .line 697
    new-instance v0, Landroid/util/Size;

    .line 698
    .line 699
    invoke-direct {v0, v12, v15}, Landroid/util/Size;-><init>(II)V

    .line 700
    .line 701
    .line 702
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 703
    .line 704
    .line 705
    new-instance v0, Landroid/util/Size;

    .line 706
    .line 707
    const/16 v5, 0x800

    .line 708
    .line 709
    const/16 v6, 0x600

    .line 710
    .line 711
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 712
    .line 713
    .line 714
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 715
    .line 716
    .line 717
    new-instance v0, Landroid/util/Size;

    .line 718
    .line 719
    const/16 v6, 0x480

    .line 720
    .line 721
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 722
    .line 723
    .line 724
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 725
    .line 726
    .line 727
    new-instance v0, Landroid/util/Size;

    .line 728
    .line 729
    const/16 v5, 0x438

    .line 730
    .line 731
    const/16 v6, 0x780

    .line 732
    .line 733
    invoke-direct {v0, v6, v5}, Landroid/util/Size;-><init>(II)V

    .line 734
    .line 735
    .line 736
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 737
    .line 738
    .line 739
    goto/16 :goto_2

    .line 740
    .line 741
    :cond_11
    invoke-virtual {v0, v7}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 742
    .line 743
    .line 744
    move-result v0

    .line 745
    if-eqz v0, :cond_7

    .line 746
    .line 747
    if-eq v1, v9, :cond_12

    .line 748
    .line 749
    if-eq v1, v10, :cond_12

    .line 750
    .line 751
    goto/16 :goto_2

    .line 752
    .line 753
    :cond_12
    new-instance v0, Landroid/util/Size;

    .line 754
    .line 755
    const/16 v5, 0xa10

    .line 756
    .line 757
    const/16 v6, 0x78c

    .line 758
    .line 759
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 760
    .line 761
    .line 762
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 763
    .line 764
    .line 765
    new-instance v0, Landroid/util/Size;

    .line 766
    .line 767
    const/16 v5, 0xa00

    .line 768
    .line 769
    const/16 v6, 0x5a0

    .line 770
    .line 771
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 772
    .line 773
    .line 774
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 775
    .line 776
    .line 777
    new-instance v0, Landroid/util/Size;

    .line 778
    .line 779
    const/16 v6, 0x780

    .line 780
    .line 781
    invoke-direct {v0, v6, v6}, Landroid/util/Size;-><init>(II)V

    .line 782
    .line 783
    .line 784
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 785
    .line 786
    .line 787
    new-instance v0, Landroid/util/Size;

    .line 788
    .line 789
    const/16 v5, 0x800

    .line 790
    .line 791
    const/16 v7, 0x600

    .line 792
    .line 793
    invoke-direct {v0, v5, v7}, Landroid/util/Size;-><init>(II)V

    .line 794
    .line 795
    .line 796
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 797
    .line 798
    .line 799
    new-instance v0, Landroid/util/Size;

    .line 800
    .line 801
    const/16 v7, 0x480

    .line 802
    .line 803
    invoke-direct {v0, v5, v7}, Landroid/util/Size;-><init>(II)V

    .line 804
    .line 805
    .line 806
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 807
    .line 808
    .line 809
    new-instance v0, Landroid/util/Size;

    .line 810
    .line 811
    const/16 v5, 0x438

    .line 812
    .line 813
    invoke-direct {v0, v6, v5}, Landroid/util/Size;-><init>(II)V

    .line 814
    .line 815
    .line 816
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 817
    .line 818
    .line 819
    goto/16 :goto_2

    .line 820
    .line 821
    :cond_13
    const-string v5, "REDMI"

    .line 822
    .line 823
    invoke-virtual {v5, v3}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 824
    .line 825
    .line 826
    move-result v3

    .line 827
    if-eqz v3, :cond_14

    .line 828
    .line 829
    const-string v3, "joyeuse"

    .line 830
    .line 831
    sget-object v5, Landroid/os/Build;->DEVICE:Ljava/lang/String;

    .line 832
    .line 833
    invoke-virtual {v3, v5}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 834
    .line 835
    .line 836
    move-result v3

    .line 837
    if-eqz v3, :cond_14

    .line 838
    .line 839
    new-instance v3, Ljava/util/ArrayList;

    .line 840
    .line 841
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 842
    .line 843
    .line 844
    invoke-virtual {v0, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 845
    .line 846
    .line 847
    move-result v0

    .line 848
    if-eqz v0, :cond_7

    .line 849
    .line 850
    const/16 v0, 0x100

    .line 851
    .line 852
    if-ne v1, v0, :cond_7

    .line 853
    .line 854
    new-instance v0, Landroid/util/Size;

    .line 855
    .line 856
    const/16 v5, 0x2440

    .line 857
    .line 858
    const/16 v6, 0x1b20

    .line 859
    .line 860
    invoke-direct {v0, v5, v6}, Landroid/util/Size;-><init>(II)V

    .line 861
    .line 862
    .line 863
    invoke-virtual {v3, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 864
    .line 865
    .line 866
    goto/16 :goto_2

    .line 867
    .line 868
    :cond_14
    const-string v0, "ExcludedSupportedSizesQuirk"

    .line 869
    .line 870
    const-string v3, "Cannot retrieve list of supported sizes to exclude on this device."

    .line 871
    .line 872
    invoke-static {v0, v3}, Lx43;->I(Ljava/lang/String;Ljava/lang/String;)V

    .line 873
    .line 874
    .line 875
    sget-object v0, Ljava/util/Collections;->EMPTY_LIST:Ljava/util/List;

    .line 876
    .line 877
    :goto_3
    invoke-interface {v0}, Ljava/util/List;->isEmpty()Z

    .line 878
    .line 879
    .line 880
    move-result v3

    .line 881
    if-eqz v3, :cond_15

    .line 882
    .line 883
    goto :goto_4

    .line 884
    :cond_15
    invoke-virtual {v4, v0}, Ljava/util/ArrayList;->removeAll(Ljava/util/Collection;)Z

    .line 885
    .line 886
    .line 887
    :goto_4
    invoke-virtual {v4}, Ljava/util/ArrayList;->isEmpty()Z

    .line 888
    .line 889
    .line 890
    move-result v0

    .line 891
    if-eqz v0, :cond_16

    .line 892
    .line 893
    const-string v0, "OutputSizesCorrector"

    .line 894
    .line 895
    const-string v3, "Sizes array becomes empty after excluding problematic output sizes."

    .line 896
    .line 897
    invoke-static {v0, v3}, Lx43;->I(Ljava/lang/String;Ljava/lang/String;)V

    .line 898
    .line 899
    .line 900
    :cond_16
    const/4 v0, 0x0

    .line 901
    new-array v0, v0, [Landroid/util/Size;

    .line 902
    .line 903
    invoke-virtual {v4, v0}, Ljava/util/ArrayList;->toArray([Ljava/lang/Object;)[Ljava/lang/Object;

    .line 904
    .line 905
    .line 906
    move-result-object v0

    .line 907
    check-cast v0, [Landroid/util/Size;

    .line 908
    .line 909
    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 910
    .line 911
    .line 912
    move-result-object v1

    .line 913
    invoke-virtual {v2, v1, v0}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 914
    .line 915
    .line 916
    invoke-virtual {v0}, [Landroid/util/Size;->clone()Ljava/lang/Object;

    .line 917
    .line 918
    .line 919
    move-result-object v0

    .line 920
    check-cast v0, [Landroid/util/Size;

    .line 921
    .line 922
    return-object v0

    .line 923
    :cond_17
    :goto_5
    new-instance v0, Ljava/lang/StringBuilder;

    .line 924
    .line 925
    const-string v2, "Retrieved output sizes array is null or empty for format "

    .line 926
    .line 927
    invoke-direct {v0, v2}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 928
    .line 929
    .line 930
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    .line 931
    .line 932
    .line 933
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 934
    .line 935
    .line 936
    move-result-object v0

    .line 937
    const-string v1, "StreamConfigurationMapCompat"

    .line 938
    .line 939
    invoke-static {v1, v0}, Lx43;->I(Ljava/lang/String;Ljava/lang/String;)V

    .line 940
    .line 941
    .line 942
    return-object v3
.end method

.method public B()Ljava/lang/Object;
    .locals 2

    .line 1
    invoke-static {}, Landroid/os/Looper;->myLooper()Landroid/os/Looper;

    .line 2
    .line 3
    .line 4
    move-result-object v0

    .line 5
    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;

    .line 6
    .line 7
    .line 8
    move-result-object v1

    .line 9
    invoke-static {v0, v1}, Lsk2;->e(Ljava/lang/Object;Ljava/lang/Object;)Z

    .line 10
    .line 11
    .line 12
    move-result v0

    .line 13
    if-nez v0, :cond_0

    .line 14
    .line 15
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 16
    .line 17
    check-cast p0, Ljava/util/concurrent/Future;

    .line 18
    .line 19
    invoke-interface {p0}, Ljava/util/concurrent/Future;->get()Ljava/lang/Object;

    .line 20
    .line 21
    .line 22
    move-result-object p0

    .line 23
    return-object p0

    .line 24
    :cond_0
    new-instance p0, Ljava/lang/IllegalThreadStateException;

    .line 25
    .line 26
    const-string v0, "Operation should not run from main thread."

    .line 27
    .line 28
    invoke-direct {p0, v0}, Ljava/lang/IllegalThreadStateException;-><init>(Ljava/lang/String;)V

    .line 29
    .line 30
    .line 31
    throw p0
.end method

.method public C(Ljava/lang/String;)Lyb5;
    .locals 3

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroidx/work/impl/WorkDatabase_Impl;

    .line 4
    .line 5
    const/4 v0, 0x1

    .line 6
    const-string v1, "SELECT `SystemIdInfo`.`work_spec_id` AS `work_spec_id`, `SystemIdInfo`.`system_id` AS `system_id` FROM SystemIdInfo WHERE work_spec_id=?"

    .line 7
    .line 8
    invoke-static {v0, v1}, Ljl4;->f(ILjava/lang/String;)Ljl4;

    .line 9
    .line 10
    .line 11
    move-result-object v1

    .line 12
    if-nez p1, :cond_0

    .line 13
    .line 14
    invoke-virtual {v1, v0}, Ljl4;->h(I)V

    .line 15
    .line 16
    .line 17
    goto :goto_0

    .line 18
    :cond_0
    invoke-virtual {v1, v0, p1}, Ljl4;->i(ILjava/lang/String;)V

    .line 19
    .line 20
    .line 21
    :goto_0
    invoke-virtual {p0}, Landroidx/work/impl/WorkDatabase;->b()V

    .line 22
    .line 23
    .line 24
    invoke-virtual {p0, v1}, Landroidx/work/impl/WorkDatabase;->i(Lw95;)Landroid/database/Cursor;

    .line 25
    .line 26
    .line 27
    move-result-object p0

    .line 28
    :try_start_0
    const-string p1, "work_spec_id"

    .line 29
    .line 30
    invoke-static {p0, p1}, Lqz0;->k(Landroid/database/Cursor;Ljava/lang/String;)I

    .line 31
    .line 32
    .line 33
    move-result p1

    .line 34
    const-string v0, "system_id"

    .line 35
    .line 36
    invoke-static {p0, v0}, Lqz0;->k(Landroid/database/Cursor;Ljava/lang/String;)I

    .line 37
    .line 38
    .line 39
    move-result v0

    .line 40
    invoke-interface {p0}, Landroid/database/Cursor;->moveToFirst()Z

    .line 41
    .line 42
    .line 43
    move-result v2

    .line 44
    if-eqz v2, :cond_1

    .line 45
    .line 46
    invoke-interface {p0, p1}, Landroid/database/Cursor;->getString(I)Ljava/lang/String;

    .line 47
    .line 48
    .line 49
    move-result-object p1

    .line 50
    invoke-interface {p0, v0}, Landroid/database/Cursor;->getInt(I)I

    .line 51
    .line 52
    .line 53
    move-result v0

    .line 54
    new-instance v2, Lyb5;

    .line 55
    .line 56
    invoke-direct {v2, p1, v0}, Lyb5;-><init>(Ljava/lang/String;I)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 57
    .line 58
    .line 59
    goto :goto_1

    .line 60
    :catchall_0
    move-exception p1

    .line 61
    goto :goto_2

    .line 62
    :cond_1
    const/4 v2, 0x0

    .line 63
    :goto_1
    invoke-interface {p0}, Landroid/database/Cursor;->close()V

    .line 64
    .line 65
    .line 66
    invoke-virtual {v1}, Ljl4;->k()V

    .line 67
    .line 68
    .line 69
    return-object v2

    .line 70
    :goto_2
    invoke-interface {p0}, Landroid/database/Cursor;->close()V

    .line 71
    .line 72
    .line 73
    invoke-virtual {v1}, Ljl4;->k()V

    .line 74
    .line 75
    .line 76
    throw p1
.end method

.method public D()V
    .locals 4

    .line 1
    sget-object v0, Lnq4;->b:Lml2;

    .line 2
    .line 3
    new-instance v1, Lzv3;

    .line 4
    .line 5
    const/16 v2, 0xc

    .line 6
    .line 7
    invoke-direct {v1, v2, p0}, Lzv3;-><init>(ILjava/lang/Object;)V

    .line 8
    .line 9
    .line 10
    new-instance v2, Ljo3;

    .line 11
    .line 12
    const/4 v3, 0x0

    .line 13
    invoke-direct {v2, v3, v1}, Ljo3;-><init>(ILjava/lang/Object;)V

    .line 14
    .line 15
    .line 16
    invoke-virtual {v2, v0}, Lgo3;->d(Lfq4;)Lto3;

    .line 17
    .line 18
    .line 19
    move-result-object v0

    .line 20
    invoke-static {}, Lpd;->a()Lfq4;

    .line 21
    .line 22
    .line 23
    move-result-object v1

    .line 24
    invoke-virtual {v0, v1}, Lgo3;->a(Lfq4;)Lno3;

    .line 25
    .line 26
    .line 27
    move-result-object v0

    .line 28
    new-instance v1, Ltt3;

    .line 29
    .line 30
    const/16 v2, 0x13

    .line 31
    .line 32
    invoke-direct {v1, v2, p0}, Ltt3;-><init>(ILjava/lang/Object;)V

    .line 33
    .line 34
    .line 35
    invoke-virtual {v0, v1}, Lgo3;->b(Lyo3;)V

    .line 36
    .line 37
    .line 38
    return-void
.end method

.method public E()V
    .locals 4

    .line 1
    sget-object v0, Lnq4;->b:Lml2;

    .line 2
    .line 3
    new-instance v1, Ldu3;

    .line 4
    .line 5
    const/16 v2, 0xb

    .line 6
    .line 7
    invoke-direct {v1, v2, p0}, Ldu3;-><init>(ILjava/lang/Object;)V

    .line 8
    .line 9
    .line 10
    new-instance v2, Ljo3;

    .line 11
    .line 12
    const/4 v3, 0x0

    .line 13
    invoke-direct {v2, v3, v1}, Ljo3;-><init>(ILjava/lang/Object;)V

    .line 14
    .line 15
    .line 16
    invoke-virtual {v2, v0}, Lgo3;->d(Lfq4;)Lto3;

    .line 17
    .line 18
    .line 19
    move-result-object v0

    .line 20
    invoke-static {}, Lpd;->a()Lfq4;

    .line 21
    .line 22
    .line 23
    move-result-object v1

    .line 24
    invoke-virtual {v0, v1}, Lgo3;->a(Lfq4;)Lno3;

    .line 25
    .line 26
    .line 27
    move-result-object v0

    .line 28
    new-instance v1, Lmu3;

    .line 29
    .line 30
    const/16 v2, 0x9

    .line 31
    .line 32
    invoke-direct {v1, v2, p0}, Lmu3;-><init>(ILjava/lang/Object;)V

    .line 33
    .line 34
    .line 35
    invoke-virtual {v0, v1}, Lgo3;->b(Lyo3;)V

    .line 36
    .line 37
    .line 38
    return-void
.end method

.method public F(Lyb5;)V
    .locals 1

    .line 1
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroidx/work/impl/WorkDatabase_Impl;

    .line 4
    .line 5
    invoke-virtual {v0}, Landroidx/work/impl/WorkDatabase;->b()V

    .line 6
    .line 7
    .line 8
    invoke-virtual {v0}, Landroidx/work/impl/WorkDatabase;->c()V

    .line 9
    .line 10
    .line 11
    :try_start_0
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 12
    .line 13
    check-cast p0, Lt81;

    .line 14
    .line 15
    invoke-virtual {p0, p1}, Lt81;->e(Ljava/lang/Object;)V

    .line 16
    .line 17
    .line 18
    invoke-virtual {v0}, Landroidx/work/impl/WorkDatabase;->j()V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 19
    .line 20
    .line 21
    invoke-virtual {v0}, Landroidx/work/impl/WorkDatabase;->g()V

    .line 22
    .line 23
    .line 24
    return-void

    .line 25
    :catchall_0
    move-exception p0

    .line 26
    invoke-virtual {v0}, Landroidx/work/impl/WorkDatabase;->g()V

    .line 27
    .line 28
    .line 29
    throw p0
.end method

.method public G(Landroid/app/Activity;Lz06;)V
    .locals 3

    .line 1
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Ljava/util/WeakHashMap;

    .line 4
    .line 5
    invoke-virtual {p1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 6
    .line 7
    .line 8
    iget-object v1, p0, Lbx3;->c:Ljava/lang/Object;

    .line 9
    .line 10
    check-cast v1, Ljava/util/concurrent/locks/ReentrantLock;

    .line 11
    .line 12
    invoke-virtual {v1}, Ljava/util/concurrent/locks/ReentrantLock;->lock()V

    .line 13
    .line 14
    .line 15
    :try_start_0
    invoke-virtual {v0, p1}, Ljava/util/WeakHashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 16
    .line 17
    .line 18
    move-result-object v2

    .line 19
    check-cast v2, Lz06;

    .line 20
    .line 21
    invoke-virtual {p2, v2}, Lz06;->equals(Ljava/lang/Object;)Z

    .line 22
    .line 23
    .line 24
    move-result v2
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 25
    if-eqz v2, :cond_0

    .line 26
    .line 27
    invoke-virtual {v1}, Ljava/util/concurrent/locks/ReentrantLock;->unlock()V

    .line 28
    .line 29
    .line 30
    return-void

    .line 31
    :cond_0
    :try_start_1
    invoke-virtual {v0, p1, p2}, Ljava/util/WeakHashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 32
    .line 33
    .line 34
    move-result-object v0

    .line 35
    check-cast v0, Lz06;
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 36
    .line 37
    invoke-virtual {v1}, Ljava/util/concurrent/locks/ReentrantLock;->unlock()V

    .line 38
    .line 39
    .line 40
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 41
    .line 42
    check-cast p0, Ldu3;

    .line 43
    .line 44
    iget-object p0, p0, Ldu3;->b:Ljava/lang/Object;

    .line 45
    .line 46
    check-cast p0, Lhz4;

    .line 47
    .line 48
    iget-object p0, p0, Lhz4;->b:Ljava/util/concurrent/CopyOnWriteArrayList;

    .line 49
    .line 50
    invoke-virtual {p0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    .line 51
    .line 52
    .line 53
    move-result-object p0

    .line 54
    :goto_0
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    .line 55
    .line 56
    .line 57
    move-result v0

    .line 58
    if-eqz v0, :cond_2

    .line 59
    .line 60
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 61
    .line 62
    .line 63
    move-result-object v0

    .line 64
    check-cast v0, Lgz4;

    .line 65
    .line 66
    iget-object v1, v0, Lgz4;->a:Landroid/app/Activity;

    .line 67
    .line 68
    invoke-virtual {v1, p1}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    .line 69
    .line 70
    .line 71
    move-result v1

    .line 72
    if-nez v1, :cond_1

    .line 73
    .line 74
    goto :goto_0

    .line 75
    :cond_1
    iput-object p2, v0, Lgz4;->c:Lz06;

    .line 76
    .line 77
    iget-object v0, v0, Lgz4;->b:Lma5;

    .line 78
    .line 79
    invoke-virtual {v0, p2}, Lma5;->accept(Ljava/lang/Object;)V

    .line 80
    .line 81
    .line 82
    goto :goto_0

    .line 83
    :cond_2
    return-void

    .line 84
    :catchall_0
    move-exception p0

    .line 85
    invoke-virtual {v1}, Ljava/util/concurrent/locks/ReentrantLock;->unlock()V

    .line 86
    .line 87
    .line 88
    throw p0
.end method

.method public H(Lvw2;)V
    .locals 2

    .line 1
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lt40;

    .line 4
    .line 5
    if-eqz v0, :cond_0

    .line 6
    .line 7
    invoke-virtual {v0}, Lt40;->run()V

    .line 8
    .line 9
    .line 10
    :cond_0
    new-instance v0, Lt40;

    .line 11
    .line 12
    iget-object v1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 13
    .line 14
    check-cast v1, Landroidx/lifecycle/a;

    .line 15
    .line 16
    invoke-direct {v0, v1, p1}, Lt40;-><init>(Landroidx/lifecycle/a;Lvw2;)V

    .line 17
    .line 18
    .line 19
    iput-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 20
    .line 21
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 22
    .line 23
    check-cast p0, Landroid/os/Handler;

    .line 24
    .line 25
    invoke-virtual {p0, v0}, Landroid/os/Handler;->postAtFrontOfQueue(Ljava/lang/Runnable;)Z

    .line 26
    .line 27
    .line 28
    return-void
.end method

.method public I(Ljava/lang/String;)V
    .locals 3

    .line 1
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroidx/work/impl/WorkDatabase_Impl;

    .line 4
    .line 5
    invoke-virtual {v0}, Landroidx/work/impl/WorkDatabase;->b()V

    .line 6
    .line 7
    .line 8
    iget-object p0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 9
    .line 10
    check-cast p0, Lzb5;

    .line 11
    .line 12
    invoke-virtual {p0}, Ljy4;->a()Lv22;

    .line 13
    .line 14
    .line 15
    move-result-object v1

    .line 16
    const/4 v2, 0x1

    .line 17
    if-nez p1, :cond_0

    .line 18
    .line 19
    invoke-virtual {v1, v2}, Lr22;->h(I)V

    .line 20
    .line 21
    .line 22
    goto :goto_0

    .line 23
    :cond_0
    invoke-virtual {v1, v2, p1}, Lr22;->i(ILjava/lang/String;)V

    .line 24
    .line 25
    .line 26
    :goto_0
    invoke-virtual {v0}, Landroidx/work/impl/WorkDatabase;->c()V

    .line 27
    .line 28
    .line 29
    :try_start_0
    invoke-virtual {v1}, Lv22;->p()V

    .line 30
    .line 31
    .line 32
    invoke-virtual {v0}, Landroidx/work/impl/WorkDatabase;->j()V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 33
    .line 34
    .line 35
    invoke-virtual {v0}, Landroidx/work/impl/WorkDatabase;->g()V

    .line 36
    .line 37
    .line 38
    invoke-virtual {p0, v1}, Ljy4;->c(Lv22;)V

    .line 39
    .line 40
    .line 41
    return-void

    .line 42
    :catchall_0
    move-exception p1

    .line 43
    invoke-virtual {v0}, Landroidx/work/impl/WorkDatabase;->g()V

    .line 44
    .line 45
    .line 46
    invoke-virtual {p0, v1}, Ljy4;->c(Lv22;)V

    .line 47
    .line 48
    .line 49
    throw p1
.end method

.method public L(Ljava/io/FileInputStream;)V
    .locals 3

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Lr00;

    .line 4
    .line 5
    const/16 v0, 0x4000

    .line 6
    .line 7
    :try_start_0
    new-array v0, v0, [B

    .line 8
    .line 9
    sget v1, Lfd2;->a:I
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 10
    .line 11
    :goto_0
    :try_start_1
    invoke-virtual {p1, v0}, Ljava/io/InputStream;->read([B)I

    .line 12
    .line 13
    .line 14
    move-result v1
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 15
    const/4 v2, -0x1

    .line 16
    if-ne v1, v2, :cond_0

    .line 17
    .line 18
    invoke-virtual {p0}, Lr00;->close()V

    .line 19
    .line 20
    .line 21
    return-void

    .line 22
    :cond_0
    const/4 v2, 0x0

    .line 23
    :try_start_2
    invoke-virtual {p0, v0, v2, v1}, Lr00;->write([BII)V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_0
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    .line 24
    .line 25
    .line 26
    goto :goto_0

    .line 27
    :catch_0
    move-exception p1

    .line 28
    :try_start_3
    new-instance v0, Lcom/dropbox/core/util/IOUtil$WriteException;

    .line 29
    .line 30
    invoke-direct {v0, p1}, Lcom/dropbox/core/util/IOUtil$WriteException;-><init>(Ljava/io/IOException;)V

    .line 31
    .line 32
    .line 33
    throw v0

    .line 34
    :catch_1
    move-exception p1

    .line 35
    new-instance v0, Lcom/dropbox/core/util/IOUtil$ReadException;

    .line 36
    .line 37
    invoke-direct {v0, p1}, Lcom/dropbox/core/util/IOUtil$ReadException;-><init>(Ljava/io/IOException;)V

    .line 38
    .line 39
    .line 40
    throw v0
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    .line 41
    :catchall_0
    move-exception p1

    .line 42
    invoke-virtual {p0}, Lr00;->close()V

    .line 43
    .line 44
    .line 45
    throw p1
.end method

.method public a(Lcom/indymobile/app/util/PSException;)V
    .locals 1

    .line 1
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lcom/indymobile/app/activity/PdfPreviewActivity;

    .line 4
    .line 5
    invoke-virtual {v0}, Lmu;->s()V

    .line 6
    .line 7
    .line 8
    invoke-virtual {p1}, Ljava/lang/Throwable;->getMessage()Ljava/lang/String;

    .line 9
    .line 10
    .line 11
    move-result-object p1

    .line 12
    invoke-static {v0, p1}, Lwv3;->i(Landroid/app/Activity;Ljava/lang/String;)V

    .line 13
    .line 14
    .line 15
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 16
    .line 17
    check-cast p0, Ljava/lang/Runnable;

    .line 18
    .line 19
    if-eqz p0, :cond_0

    .line 20
    .line 21
    invoke-virtual {v0, p0}, Landroid/app/Activity;->runOnUiThread(Ljava/lang/Runnable;)V

    .line 22
    .line 23
    .line 24
    :cond_0
    return-void
.end method

.method public b()Lvm4;
    .locals 4

    .line 1
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lkm4;

    .line 4
    .line 5
    const/4 v1, 0x0

    .line 6
    if-eqz v0, :cond_a

    .line 7
    .line 8
    iget-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 9
    .line 10
    check-cast v0, Ljava/math/BigInteger;

    .line 11
    .line 12
    if-eqz v0, :cond_9

    .line 13
    .line 14
    invoke-virtual {v0}, Ljava/math/BigInteger;->bitLength()I

    .line 15
    .line 16
    .line 17
    move-result v0

    .line 18
    iget-object v2, p0, Lbx3;->b:Ljava/lang/Object;

    .line 19
    .line 20
    check-cast v2, Lkm4;

    .line 21
    .line 22
    iget v3, v2, Lkm4;->a:I

    .line 23
    .line 24
    if-ne v0, v3, :cond_8

    .line 25
    .line 26
    invoke-virtual {v2}, Lkm4;->a()Z

    .line 27
    .line 28
    .line 29
    move-result v0

    .line 30
    if-eqz v0, :cond_1

    .line 31
    .line 32
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 33
    .line 34
    check-cast v0, Ljava/lang/Integer;

    .line 35
    .line 36
    if-eqz v0, :cond_0

    .line 37
    .line 38
    goto :goto_0

    .line 39
    :cond_0
    const-string p0, "Cannot create key without ID requirement with parameters with ID requirement"

    .line 40
    .line 41
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 42
    .line 43
    .line 44
    return-object v1

    .line 45
    :cond_1
    :goto_0
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 46
    .line 47
    check-cast v0, Lkm4;

    .line 48
    .line 49
    invoke-virtual {v0}, Lkm4;->a()Z

    .line 50
    .line 51
    .line 52
    move-result v0

    .line 53
    if-nez v0, :cond_3

    .line 54
    .line 55
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 56
    .line 57
    check-cast v0, Ljava/lang/Integer;

    .line 58
    .line 59
    if-nez v0, :cond_2

    .line 60
    .line 61
    goto :goto_1

    .line 62
    :cond_2
    const-string p0, "Cannot create key with ID requirement with parameters without ID requirement"

    .line 63
    .line 64
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 65
    .line 66
    .line 67
    return-object v1

    .line 68
    :cond_3
    :goto_1
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 69
    .line 70
    check-cast v0, Lkm4;

    .line 71
    .line 72
    iget-object v0, v0, Lkm4;->c:Ljm4;

    .line 73
    .line 74
    sget-object v2, Ljm4;->e:Ljm4;

    .line 75
    .line 76
    if-ne v0, v2, :cond_4

    .line 77
    .line 78
    sget-object v0, Lfs3;->a:Lg20;

    .line 79
    .line 80
    goto :goto_3

    .line 81
    :cond_4
    sget-object v2, Ljm4;->d:Ljm4;

    .line 82
    .line 83
    if-eq v0, v2, :cond_7

    .line 84
    .line 85
    sget-object v2, Ljm4;->c:Ljm4;

    .line 86
    .line 87
    if-ne v0, v2, :cond_5

    .line 88
    .line 89
    goto :goto_2

    .line 90
    :cond_5
    sget-object v2, Ljm4;->b:Ljm4;

    .line 91
    .line 92
    if-ne v0, v2, :cond_6

    .line 93
    .line 94
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 95
    .line 96
    check-cast v0, Ljava/lang/Integer;

    .line 97
    .line 98
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I

    .line 99
    .line 100
    .line 101
    move-result v0

    .line 102
    invoke-static {v0}, Lfs3;->b(I)Lg20;

    .line 103
    .line 104
    .line 105
    move-result-object v0

    .line 106
    goto :goto_3

    .line 107
    :cond_6
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 108
    .line 109
    check-cast p0, Lkm4;

    .line 110
    .line 111
    iget-object p0, p0, Lkm4;->c:Ljm4;

    .line 112
    .line 113
    const-string v0, "Unknown RsaSsaPkcs1Parameters.Variant: "

    .line 114
    .line 115
    invoke-static {p0, v0}, Lyb4;->j(Ljava/lang/Object;Ljava/lang/String;)V

    .line 116
    .line 117
    .line 118
    return-object v1

    .line 119
    :cond_7
    :goto_2
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 120
    .line 121
    check-cast v0, Ljava/lang/Integer;

    .line 122
    .line 123
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I

    .line 124
    .line 125
    .line 126
    move-result v0

    .line 127
    invoke-static {v0}, Lfs3;->a(I)Lg20;

    .line 128
    .line 129
    .line 130
    move-result-object v0

    .line 131
    :goto_3
    new-instance v1, Lvm4;

    .line 132
    .line 133
    iget-object v2, p0, Lbx3;->b:Ljava/lang/Object;

    .line 134
    .line 135
    check-cast v2, Lkm4;

    .line 136
    .line 137
    iget-object v3, p0, Lbx3;->c:Ljava/lang/Object;

    .line 138
    .line 139
    check-cast v3, Ljava/math/BigInteger;

    .line 140
    .line 141
    iget-object p0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 142
    .line 143
    check-cast p0, Ljava/lang/Integer;

    .line 144
    .line 145
    invoke-direct {v1, v2, v3, v0, p0}, Lvm4;-><init>(Lkm4;Ljava/math/BigInteger;Lg20;Ljava/lang/Integer;)V

    .line 146
    .line 147
    .line 148
    return-object v1

    .line 149
    :cond_8
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 150
    .line 151
    const-string v1, "Got modulus size "

    .line 152
    .line 153
    const-string v2, ", but parameters requires modulus size "

    .line 154
    .line 155
    invoke-static {v0, v3, v1, v2}, Lk26;->i(IILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    .line 156
    .line 157
    .line 158
    move-result-object v0

    .line 159
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 160
    .line 161
    .line 162
    throw p0

    .line 163
    :cond_9
    const-string p0, "Cannot build without modulus"

    .line 164
    .line 165
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 166
    .line 167
    .line 168
    return-object v1

    .line 169
    :cond_a
    const-string p0, "Cannot build without parameters"

    .line 170
    .line 171
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 172
    .line 173
    .line 174
    return-object v1
.end method

.method public c(La14;)V
    .locals 13

    .line 1
    iget-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lrg5;

    .line 4
    .line 5
    invoke-static {v0}, Ly43;->l(Ljava/lang/Object;)V

    .line 6
    .line 7
    .line 8
    sget v0, Lfr5;->a:I

    .line 9
    .line 10
    iget-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 11
    .line 12
    move-object v1, v0

    .line 13
    check-cast v1, Lrg5;

    .line 14
    .line 15
    monitor-enter v1

    .line 16
    :try_start_0
    iget-wide v2, v1, Lrg5;->c:J

    .line 17
    .line 18
    const-wide v4, -0x7fffffffffffffffL    # -4.9E-324

    .line 19
    .line 20
    .line 21
    .line 22
    .line 23
    cmp-long v0, v2, v4

    .line 24
    .line 25
    if-eqz v0, :cond_0

    .line 26
    .line 27
    iget-wide v6, v1, Lrg5;->b:J

    .line 28
    .line 29
    add-long/2addr v2, v6

    .line 30
    :goto_0
    move-wide v7, v2

    .line 31
    goto :goto_1

    .line 32
    :catchall_0
    move-exception v0

    .line 33
    move-object p0, v0

    .line 34
    goto :goto_3

    .line 35
    :cond_0
    invoke-virtual {v1}, Lrg5;->d()J

    .line 36
    .line 37
    .line 38
    move-result-wide v2
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 39
    goto :goto_0

    .line 40
    :goto_1
    monitor-exit v1

    .line 41
    iget-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 42
    .line 43
    move-object v2, v0

    .line 44
    check-cast v2, Lrg5;

    .line 45
    .line 46
    monitor-enter v2

    .line 47
    :try_start_1
    iget-wide v0, v2, Lrg5;->b:J
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    .line 48
    .line 49
    monitor-exit v2

    .line 50
    cmp-long v2, v7, v4

    .line 51
    .line 52
    if-eqz v2, :cond_3

    .line 53
    .line 54
    cmp-long v2, v0, v4

    .line 55
    .line 56
    if-nez v2, :cond_1

    .line 57
    .line 58
    goto :goto_2

    .line 59
    :cond_1
    iget-object v2, p0, Lbx3;->b:Ljava/lang/Object;

    .line 60
    .line 61
    check-cast v2, Landroidx/media3/common/b;

    .line 62
    .line 63
    iget-wide v3, v2, Landroidx/media3/common/b;->r:J

    .line 64
    .line 65
    cmp-long v3, v0, v3

    .line 66
    .line 67
    if-eqz v3, :cond_2

    .line 68
    .line 69
    invoke-virtual {v2}, Landroidx/media3/common/b;->a()Lf02;

    .line 70
    .line 71
    .line 72
    move-result-object v2

    .line 73
    iput-wide v0, v2, Lf02;->q:J

    .line 74
    .line 75
    new-instance v0, Landroidx/media3/common/b;

    .line 76
    .line 77
    invoke-direct {v0, v2}, Landroidx/media3/common/b;-><init>(Lf02;)V

    .line 78
    .line 79
    .line 80
    iput-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 81
    .line 82
    iget-object v1, p0, Lbx3;->d:Ljava/lang/Object;

    .line 83
    .line 84
    check-cast v1, Lpi5;

    .line 85
    .line 86
    invoke-interface {v1, v0}, Lpi5;->c(Landroidx/media3/common/b;)V

    .line 87
    .line 88
    .line 89
    :cond_2
    invoke-virtual {p1}, La14;->a()I

    .line 90
    .line 91
    .line 92
    move-result v10

    .line 93
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 94
    .line 95
    check-cast v0, Lpi5;

    .line 96
    .line 97
    const/4 v1, 0x0

    .line 98
    invoke-interface {v0, p1, v10, v1}, Lpi5;->b(La14;II)V

    .line 99
    .line 100
    .line 101
    iget-object p0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 102
    .line 103
    move-object v6, p0

    .line 104
    check-cast v6, Lpi5;

    .line 105
    .line 106
    const/4 v11, 0x0

    .line 107
    const/4 v12, 0x0

    .line 108
    const/4 v9, 0x1

    .line 109
    invoke-interface/range {v6 .. v12}, Lpi5;->a(JIIILoi5;)V

    .line 110
    .line 111
    .line 112
    :cond_3
    :goto_2
    return-void

    .line 113
    :catchall_1
    move-exception v0

    .line 114
    move-object p0, v0

    .line 115
    :try_start_2
    monitor-exit v2
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_1

    .line 116
    throw p0

    .line 117
    :goto_3
    :try_start_3
    monitor-exit v1
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    .line 118
    throw p0
.end method

.method public d(ILqx0;JI)V
    .locals 7

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    move-object v0, p0

    .line 4
    check-cast v0, Landroid/media/MediaCodec;

    .line 5
    .line 6
    iget-object v3, p2, Lqx0;->i:Landroid/media/MediaCodec$CryptoInfo;

    .line 7
    .line 8
    const/4 v2, 0x0

    .line 9
    move v1, p1

    .line 10
    move-wide v4, p3

    .line 11
    move v6, p5

    .line 12
    invoke-virtual/range {v0 .. v6}, Landroid/media/MediaCodec;->queueSecureInputBuffer(IILandroid/media/MediaCodec$CryptoInfo;JI)V

    .line 13
    .line 14
    .line 15
    return-void
.end method

.method public e(Landroid/os/Bundle;)V
    .locals 0

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    invoke-virtual {p0, p1}, Landroid/media/MediaCodec;->setParameters(Landroid/os/Bundle;)V

    .line 6
    .line 7
    .line 8
    return-void
.end method

.method public endTracks()V
    .locals 0

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Liq1;

    .line 4
    .line 5
    invoke-interface {p0}, Liq1;->endTracks()V

    .line 6
    .line 7
    .line 8
    return-void
.end method

.method public f(Lrg5;Liq1;Lif1;)V
    .locals 0

    .line 1
    iput-object p1, p0, Lbx3;->c:Ljava/lang/Object;

    .line 2
    .line 3
    invoke-virtual {p3}, Lif1;->a()V

    .line 4
    .line 5
    .line 6
    invoke-virtual {p3}, Lif1;->b()V

    .line 7
    .line 8
    .line 9
    iget p1, p3, Lif1;->c:I

    .line 10
    .line 11
    const/4 p3, 0x5

    .line 12
    invoke-interface {p2, p1, p3}, Liq1;->track(II)Lpi5;

    .line 13
    .line 14
    .line 15
    move-result-object p1

    .line 16
    iput-object p1, p0, Lbx3;->d:Ljava/lang/Object;

    .line 17
    .line 18
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 19
    .line 20
    check-cast p0, Landroidx/media3/common/b;

    .line 21
    .line 22
    invoke-interface {p1, p0}, Lpi5;->c(Landroidx/media3/common/b;)V

    .line 23
    .line 24
    .line 25
    return-void
.end method

.method public flush()V
    .locals 0

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    invoke-virtual {p0}, Landroid/media/MediaCodec;->flush()V

    .line 6
    .line 7
    .line 8
    return-void
.end method

.method public g(IIJI)V
    .locals 7

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    move-object v0, p0

    .line 4
    check-cast v0, Landroid/media/MediaCodec;

    .line 5
    .line 6
    const/4 v2, 0x0

    .line 7
    move v1, p1

    .line 8
    move v3, p2

    .line 9
    move-wide v4, p3

    .line 10
    move v6, p5

    .line 11
    invoke-virtual/range {v0 .. v6}, Landroid/media/MediaCodec;->queueInputBuffer(IIIJI)V

    .line 12
    .line 13
    .line 14
    return-void
.end method

.method public get()Ljava/lang/Object;
    .locals 7

    .line 1
    iget v0, p0, Lbx3;->a:I

    .line 2
    .line 3
    sparse-switch v0, :sswitch_data_0

    .line 4
    .line 5
    .line 6
    new-instance v2, Lim3;

    .line 7
    .line 8
    const/16 v0, 0x16

    .line 9
    .line 10
    invoke-direct {v2, v0}, Lim3;-><init>(I)V

    .line 11
    .line 12
    .line 13
    new-instance v3, Leh3;

    .line 14
    .line 15
    invoke-direct {v3, v0}, Leh3;-><init>(I)V

    .line 16
    .line 17
    .line 18
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 19
    .line 20
    check-cast v0, Lo71;

    .line 21
    .line 22
    invoke-virtual {v0}, Lo71;->get()Ljava/lang/Object;

    .line 23
    .line 24
    .line 25
    move-result-object v0

    .line 26
    move-object v4, v0

    .line 27
    check-cast v4, Lh61;

    .line 28
    .line 29
    iget-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 30
    .line 31
    check-cast v0, Lkn0;

    .line 32
    .line 33
    invoke-virtual {v0}, Lkn0;->get()Ljava/lang/Object;

    .line 34
    .line 35
    .line 36
    move-result-object v0

    .line 37
    move-object v5, v0

    .line 38
    check-cast v5, Lyo0;

    .line 39
    .line 40
    iget-object p0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 41
    .line 42
    check-cast p0, Lvi5;

    .line 43
    .line 44
    invoke-virtual {p0}, Lvi5;->get()Ljava/lang/Object;

    .line 45
    .line 46
    .line 47
    move-result-object p0

    .line 48
    move-object v6, p0

    .line 49
    check-cast v6, Lt41;

    .line 50
    .line 51
    new-instance v1, Lak5;

    .line 52
    .line 53
    invoke-direct/range {v1 .. v6}, Lak5;-><init>(Lii0;Lii0;Lh61;Lyo0;Lt41;)V

    .line 54
    .line 55
    .line 56
    return-object v1

    .line 57
    :sswitch_0
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 58
    .line 59
    check-cast v0, Lmb4;

    .line 60
    .line 61
    invoke-interface {v0}, Lmb4;->get()Ljava/lang/Object;

    .line 62
    .line 63
    .line 64
    move-result-object v0

    .line 65
    check-cast v0, Landroid/content/Context;

    .line 66
    .line 67
    iget-object v1, p0, Lbx3;->c:Ljava/lang/Object;

    .line 68
    .line 69
    check-cast v1, Lmb4;

    .line 70
    .line 71
    invoke-interface {v1}, Lmb4;->get()Ljava/lang/Object;

    .line 72
    .line 73
    .line 74
    move-result-object v1

    .line 75
    check-cast v1, Lno4;

    .line 76
    .line 77
    iget-object p0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 78
    .line 79
    check-cast p0, Lvx3;

    .line 80
    .line 81
    invoke-virtual {p0}, Lvx3;->get()Ljava/lang/Object;

    .line 82
    .line 83
    .line 84
    move-result-object p0

    .line 85
    check-cast p0, Lzr;

    .line 86
    .line 87
    new-instance v2, Lfa1;

    .line 88
    .line 89
    const/16 v3, 0xd

    .line 90
    .line 91
    invoke-direct {v2, v0, v1, p0, v3}, Lfa1;-><init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)V

    .line 92
    .line 93
    .line 94
    return-object v2

    .line 95
    :sswitch_1
    invoke-virtual {p0}, Lbx3;->z()Ls24;

    .line 96
    .line 97
    .line 98
    move-result-object p0

    .line 99
    return-object p0

    .line 100
    nop

    .line 101
    :sswitch_data_0
    .sparse-switch
        0xc -> :sswitch_1
        0x12 -> :sswitch_0
    .end sparse-switch
.end method

.method public i()V
    .locals 2

    .line 1
    iget v0, p0, Lbx3;->a:I

    .line 2
    .line 3
    const/4 v1, 0x0

    .line 4
    packed-switch v0, :pswitch_data_0

    .line 5
    .line 6
    .line 7
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast v0, Lox3;

    .line 10
    .line 11
    iput-boolean v1, v0, Lox3;->b:Z

    .line 12
    .line 13
    iget-object v1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 14
    .line 15
    check-cast v1, Landroidx/fragment/app/o;

    .line 16
    .line 17
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 18
    .line 19
    check-cast p0, Lwt3;

    .line 20
    .line 21
    invoke-virtual {v0, v1, p0}, Lox3;->B(Landroidx/fragment/app/o;Lwt3;)V

    .line 22
    .line 23
    .line 24
    return-void

    .line 25
    :pswitch_0
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 26
    .line 27
    check-cast v0, Ldx3;

    .line 28
    .line 29
    iput-boolean v1, v0, Ldx3;->e:Z

    .line 30
    .line 31
    iget-object v1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 32
    .line 33
    check-cast v1, Landroidx/fragment/app/o;

    .line 34
    .line 35
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 36
    .line 37
    check-cast p0, Lwt3;

    .line 38
    .line 39
    invoke-virtual {v0, v1, p0}, Ldx3;->B(Landroidx/fragment/app/o;Lwt3;)V

    .line 40
    .line 41
    .line 42
    return-void

    .line 43
    :pswitch_data_0
    .packed-switch 0x0
        :pswitch_0
    .end packed-switch
.end method

.method public j()Landroid/media/MediaFormat;
    .locals 0

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    invoke-virtual {p0}, Landroid/media/MediaCodec;->getOutputFormat()Landroid/media/MediaFormat;

    .line 6
    .line 7
    .line 8
    move-result-object p0

    .line 9
    return-object p0
.end method

.method public k(Lza3;Landroid/os/Handler;)V
    .locals 3

    .line 1
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    new-instance v1, Lgj;

    .line 6
    .line 7
    const/4 v2, 0x1

    .line 8
    invoke-direct {v1, p0, p1, v2}, Lgj;-><init>(Lja3;Lza3;I)V

    .line 9
    .line 10
    .line 11
    invoke-virtual {v0, v1, p2}, Landroid/media/MediaCodec;->setOnFrameRenderedListener(Landroid/media/MediaCodec$OnFrameRenderedListener;Landroid/os/Handler;)V

    .line 12
    .line 13
    .line 14
    return-void
.end method

.method public l(I)Ljava/nio/ByteBuffer;
    .locals 2

    .line 1
    sget v0, Lfr5;->a:I

    .line 2
    .line 3
    const/16 v1, 0x15

    .line 4
    .line 5
    if-lt v0, v1, :cond_0

    .line 6
    .line 7
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast p0, Landroid/media/MediaCodec;

    .line 10
    .line 11
    invoke-virtual {p0, p1}, Landroid/media/MediaCodec;->getInputBuffer(I)Ljava/nio/ByteBuffer;

    .line 12
    .line 13
    .line 14
    move-result-object p0

    .line 15
    return-object p0

    .line 16
    :cond_0
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 17
    .line 18
    check-cast p0, [Ljava/nio/ByteBuffer;

    .line 19
    .line 20
    aget-object p0, p0, p1

    .line 21
    .line 22
    return-object p0
.end method

.method public m(Landroid/view/Surface;)V
    .locals 0

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    invoke-virtual {p0, p1}, Landroid/media/MediaCodec;->setOutputSurface(Landroid/view/Surface;)V

    .line 6
    .line 7
    .line 8
    return-void
.end method

.method public n(Lls4;)V
    .locals 0

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Liq1;

    .line 4
    .line 5
    invoke-interface {p0, p1}, Liq1;->n(Lls4;)V

    .line 6
    .line 7
    .line 8
    return-void
.end method

.method public o(IJ)V
    .locals 0

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    invoke-virtual {p0, p1, p2, p3}, Landroid/media/MediaCodec;->releaseOutputBuffer(IJ)V

    .line 6
    .line 7
    .line 8
    return-void
.end method

.method public onFailure(Ljava/lang/Throwable;)V
    .locals 4

    .line 1
    iget v0, p0, Lbx3;->a:I

    .line 2
    .line 3
    const/4 v1, 0x0

    .line 4
    packed-switch v0, :pswitch_data_0

    .line 5
    .line 6
    .line 7
    instance-of v0, p1, Ljava/util/concurrent/CancellationException;

    .line 8
    .line 9
    iget-object v2, p0, Lbx3;->c:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast v2, Lt30;

    .line 12
    .line 13
    if-eqz v0, :cond_0

    .line 14
    .line 15
    new-instance v0, Lpa5;

    .line 16
    .line 17
    iget-object p0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 18
    .line 19
    check-cast p0, Ljava/lang/String;

    .line 20
    .line 21
    const-string v3, " cancelled."

    .line 22
    .line 23
    invoke-virtual {p0, v3}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    .line 24
    .line 25
    .line 26
    move-result-object p0

    .line 27
    invoke-direct {v0, p0, p1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    .line 28
    .line 29
    .line 30
    invoke-virtual {v2, v0}, Lt30;->b(Ljava/lang/Throwable;)Z

    .line 31
    .line 32
    .line 33
    move-result p0

    .line 34
    invoke-static {v1, p0}, Lgy0;->i(Ljava/lang/String;Z)V

    .line 35
    .line 36
    .line 37
    goto :goto_0

    .line 38
    :cond_0
    invoke-virtual {v2, v1}, Lt30;->a(Ljava/lang/Object;)Z

    .line 39
    .line 40
    .line 41
    :goto_0
    return-void

    .line 42
    :pswitch_0
    iget-object p1, p0, Lbx3;->d:Ljava/lang/Object;

    .line 43
    .line 44
    check-cast p1, Lyn0;

    .line 45
    .line 46
    iput-object v1, p1, Lyn0;->f:Ljava/lang/Object;

    .line 47
    .line 48
    iget-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 49
    .line 50
    check-cast p1, Ljava/util/ArrayList;

    .line 51
    .line 52
    invoke-virtual {p1}, Ljava/util/ArrayList;->isEmpty()Z

    .line 53
    .line 54
    .line 55
    move-result v0

    .line 56
    if-nez v0, :cond_2

    .line 57
    .line 58
    invoke-virtual {p1}, Ljava/util/ArrayList;->size()I

    .line 59
    .line 60
    .line 61
    move-result v0

    .line 62
    const/4 v1, 0x0

    .line 63
    :goto_1
    if-ge v1, v0, :cond_1

    .line 64
    .line 65
    invoke-virtual {p1, v1}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    .line 66
    .line 67
    .line 68
    move-result-object v2

    .line 69
    add-int/lit8 v1, v1, 0x1

    .line 70
    .line 71
    check-cast v2, Lu60;

    .line 72
    .line 73
    iget-object v3, p0, Lbx3;->c:Ljava/lang/Object;

    .line 74
    .line 75
    check-cast v3, Ls80;

    .line 76
    .line 77
    check-cast v3, Ls80;

    .line 78
    .line 79
    invoke-interface {v3, v2}, Ls80;->h(Lu60;)V

    .line 80
    .line 81
    .line 82
    goto :goto_1

    .line 83
    :cond_1
    invoke-virtual {p1}, Ljava/util/ArrayList;->clear()V

    .line 84
    .line 85
    .line 86
    :cond_2
    return-void

    .line 87
    :pswitch_data_0
    .packed-switch 0xd
        :pswitch_0
    .end packed-switch
.end method

.method public onSuccess()V
    .locals 2

    .line 1
    iget v0, p0, Lbx3;->a:I

    .line 2
    .line 3
    const/4 v1, 0x0

    .line 4
    packed-switch v0, :pswitch_data_0

    .line 5
    .line 6
    .line 7
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast v0, Lox3;

    .line 10
    .line 11
    iput-boolean v1, v0, Lox3;->b:Z

    .line 12
    .line 13
    iget-object v1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 14
    .line 15
    check-cast v1, Landroidx/fragment/app/o;

    .line 16
    .line 17
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 18
    .line 19
    check-cast p0, Lwt3;

    .line 20
    .line 21
    invoke-virtual {v0, v1, p0}, Lox3;->B(Landroidx/fragment/app/o;Lwt3;)V

    .line 22
    .line 23
    .line 24
    return-void

    .line 25
    :pswitch_0
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 26
    .line 27
    check-cast v0, Ldx3;

    .line 28
    .line 29
    iput-boolean v1, v0, Ldx3;->e:Z

    .line 30
    .line 31
    iget-object v1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 32
    .line 33
    check-cast v1, Landroidx/fragment/app/o;

    .line 34
    .line 35
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 36
    .line 37
    check-cast p0, Lwt3;

    .line 38
    .line 39
    invoke-virtual {v0, v1, p0}, Ldx3;->B(Landroidx/fragment/app/o;Lwt3;)V

    .line 40
    .line 41
    .line 42
    return-void

    .line 43
    :pswitch_data_0
    .packed-switch 0x0
        :pswitch_0
    .end packed-switch
.end method

.method public onSuccess(Ljava/lang/Object;)V
    .locals 2

    iget v0, p0, Lbx3;->a:I

    packed-switch v0, :pswitch_data_0

    .line 43
    check-cast p1, Landroid/view/Surface;

    .line 44
    iget-object p1, p0, Lbx3;->b:Ljava/lang/Object;

    check-cast p1, Liz2;

    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    check-cast p0, Lt30;

    .line 45
    invoke-static {}, Lpw0;->i()Lz91;

    move-result-object v0

    const/4 v1, 0x1

    .line 46
    invoke-static {v1, p1, p0, v0}, Ls46;->H(ZLiz2;Lt30;Lz91;)V

    return-void

    .line 47
    :pswitch_0
    check-cast p1, Ljava/lang/Void;

    .line 48
    iget-object p0, p0, Lbx3;->d:Ljava/lang/Object;

    check-cast p0, Lyn0;

    const/4 p1, 0x0

    iput-object p1, p0, Lyn0;->f:Ljava/lang/Object;

    return-void

    :pswitch_data_0
    .packed-switch 0xd
        :pswitch_0
    .end packed-switch
.end method

.method public p()I
    .locals 2

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    const-wide/16 v0, 0x0

    .line 6
    .line 7
    invoke-virtual {p0, v0, v1}, Landroid/media/MediaCodec;->dequeueInputBuffer(J)I

    .line 8
    .line 9
    .line 10
    move-result p0

    .line 11
    return p0
.end method

.method public q(Landroid/media/MediaCodec$BufferInfo;)I
    .locals 5

    .line 1
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    :cond_0
    const-wide/16 v1, 0x0

    .line 6
    .line 7
    invoke-virtual {v0, p1, v1, v2}, Landroid/media/MediaCodec;->dequeueOutputBuffer(Landroid/media/MediaCodec$BufferInfo;J)I

    .line 8
    .line 9
    .line 10
    move-result v1

    .line 11
    const/4 v2, -0x3

    .line 12
    if-ne v1, v2, :cond_1

    .line 13
    .line 14
    sget v3, Lfr5;->a:I

    .line 15
    .line 16
    const/16 v4, 0x15

    .line 17
    .line 18
    if-ge v3, v4, :cond_1

    .line 19
    .line 20
    invoke-virtual {v0}, Landroid/media/MediaCodec;->getOutputBuffers()[Ljava/nio/ByteBuffer;

    .line 21
    .line 22
    .line 23
    move-result-object v3

    .line 24
    iput-object v3, p0, Lbx3;->d:Ljava/lang/Object;

    .line 25
    .line 26
    :cond_1
    if-eq v1, v2, :cond_0

    .line 27
    .line 28
    return v1
.end method

.method public r(IZ)V
    .locals 0

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    invoke-virtual {p0, p1, p2}, Landroid/media/MediaCodec;->releaseOutputBuffer(IZ)V

    .line 6
    .line 7
    .line 8
    return-void
.end method

.method public release()V
    .locals 2

    .line 1
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    const/4 v1, 0x0

    .line 6
    iput-object v1, p0, Lbx3;->c:Ljava/lang/Object;

    .line 7
    .line 8
    iput-object v1, p0, Lbx3;->d:Ljava/lang/Object;

    .line 9
    .line 10
    :try_start_0
    sget p0, Lfr5;->a:I

    .line 11
    .line 12
    const/16 v1, 0x1e

    .line 13
    .line 14
    if-lt p0, v1, :cond_0

    .line 15
    .line 16
    const/16 v1, 0x21

    .line 17
    .line 18
    if-ge p0, v1, :cond_0

    .line 19
    .line 20
    invoke-virtual {v0}, Landroid/media/MediaCodec;->stop()V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 21
    .line 22
    .line 23
    goto :goto_0

    .line 24
    :catchall_0
    move-exception p0

    .line 25
    goto :goto_1

    .line 26
    :cond_0
    :goto_0
    invoke-virtual {v0}, Landroid/media/MediaCodec;->release()V

    .line 27
    .line 28
    .line 29
    return-void

    .line 30
    :goto_1
    invoke-virtual {v0}, Landroid/media/MediaCodec;->release()V

    .line 31
    .line 32
    .line 33
    throw p0
.end method

.method public s(I)Ljava/nio/ByteBuffer;
    .locals 2

    .line 1
    sget v0, Lfr5;->a:I

    .line 2
    .line 3
    const/16 v1, 0x15

    .line 4
    .line 5
    if-lt v0, v1, :cond_0

    .line 6
    .line 7
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast p0, Landroid/media/MediaCodec;

    .line 10
    .line 11
    invoke-virtual {p0, p1}, Landroid/media/MediaCodec;->getOutputBuffer(I)Ljava/nio/ByteBuffer;

    .line 12
    .line 13
    .line 14
    move-result-object p0

    .line 15
    return-object p0

    .line 16
    :cond_0
    iget-object p0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 17
    .line 18
    check-cast p0, [Ljava/nio/ByteBuffer;

    .line 19
    .line 20
    aget-object p0, p0, p1

    .line 21
    .line 22
    return-object p0
.end method

.method public setVideoScalingMode(I)V
    .locals 0

    .line 1
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/media/MediaCodec;

    .line 4
    .line 5
    invoke-virtual {p0, p1}, Landroid/media/MediaCodec;->setVideoScalingMode(I)V

    .line 6
    .line 7
    .line 8
    return-void
.end method

.method public t()Lvn4;
    .locals 4

    .line 1
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lkn4;

    .line 4
    .line 5
    const/4 v1, 0x0

    .line 6
    if-eqz v0, :cond_a

    .line 7
    .line 8
    iget-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 9
    .line 10
    check-cast v0, Ljava/math/BigInteger;

    .line 11
    .line 12
    if-eqz v0, :cond_9

    .line 13
    .line 14
    invoke-virtual {v0}, Ljava/math/BigInteger;->bitLength()I

    .line 15
    .line 16
    .line 17
    move-result v0

    .line 18
    iget-object v2, p0, Lbx3;->b:Ljava/lang/Object;

    .line 19
    .line 20
    check-cast v2, Lkn4;

    .line 21
    .line 22
    iget v3, v2, Lkn4;->a:I

    .line 23
    .line 24
    if-ne v0, v3, :cond_8

    .line 25
    .line 26
    invoke-virtual {v2}, Lkn4;->a()Z

    .line 27
    .line 28
    .line 29
    move-result v0

    .line 30
    if-eqz v0, :cond_1

    .line 31
    .line 32
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 33
    .line 34
    check-cast v0, Ljava/lang/Integer;

    .line 35
    .line 36
    if-eqz v0, :cond_0

    .line 37
    .line 38
    goto :goto_0

    .line 39
    :cond_0
    const-string p0, "Cannot create key without ID requirement with parameters with ID requirement"

    .line 40
    .line 41
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 42
    .line 43
    .line 44
    return-object v1

    .line 45
    :cond_1
    :goto_0
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 46
    .line 47
    check-cast v0, Lkn4;

    .line 48
    .line 49
    invoke-virtual {v0}, Lkn4;->a()Z

    .line 50
    .line 51
    .line 52
    move-result v0

    .line 53
    if-nez v0, :cond_3

    .line 54
    .line 55
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 56
    .line 57
    check-cast v0, Ljava/lang/Integer;

    .line 58
    .line 59
    if-nez v0, :cond_2

    .line 60
    .line 61
    goto :goto_1

    .line 62
    :cond_2
    const-string p0, "Cannot create key with ID requirement with parameters without ID requirement"

    .line 63
    .line 64
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 65
    .line 66
    .line 67
    return-object v1

    .line 68
    :cond_3
    :goto_1
    iget-object v0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 69
    .line 70
    check-cast v0, Lkn4;

    .line 71
    .line 72
    iget-object v0, v0, Lkn4;->c:Ljn4;

    .line 73
    .line 74
    sget-object v2, Ljn4;->e:Ljn4;

    .line 75
    .line 76
    if-ne v0, v2, :cond_4

    .line 77
    .line 78
    sget-object v0, Lfs3;->a:Lg20;

    .line 79
    .line 80
    goto :goto_3

    .line 81
    :cond_4
    sget-object v2, Ljn4;->d:Ljn4;

    .line 82
    .line 83
    if-eq v0, v2, :cond_7

    .line 84
    .line 85
    sget-object v2, Ljn4;->c:Ljn4;

    .line 86
    .line 87
    if-ne v0, v2, :cond_5

    .line 88
    .line 89
    goto :goto_2

    .line 90
    :cond_5
    sget-object v2, Ljn4;->b:Ljn4;

    .line 91
    .line 92
    if-ne v0, v2, :cond_6

    .line 93
    .line 94
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 95
    .line 96
    check-cast v0, Ljava/lang/Integer;

    .line 97
    .line 98
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I

    .line 99
    .line 100
    .line 101
    move-result v0

    .line 102
    invoke-static {v0}, Lfs3;->b(I)Lg20;

    .line 103
    .line 104
    .line 105
    move-result-object v0

    .line 106
    goto :goto_3

    .line 107
    :cond_6
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 108
    .line 109
    check-cast p0, Lkn4;

    .line 110
    .line 111
    iget-object p0, p0, Lkn4;->c:Ljn4;

    .line 112
    .line 113
    const-string v0, "Unknown RsaSsaPssParameters.Variant: "

    .line 114
    .line 115
    invoke-static {p0, v0}, Lyb4;->j(Ljava/lang/Object;Ljava/lang/String;)V

    .line 116
    .line 117
    .line 118
    return-object v1

    .line 119
    :cond_7
    :goto_2
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 120
    .line 121
    check-cast v0, Ljava/lang/Integer;

    .line 122
    .line 123
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I

    .line 124
    .line 125
    .line 126
    move-result v0

    .line 127
    invoke-static {v0}, Lfs3;->a(I)Lg20;

    .line 128
    .line 129
    .line 130
    move-result-object v0

    .line 131
    :goto_3
    new-instance v1, Lvn4;

    .line 132
    .line 133
    iget-object v2, p0, Lbx3;->b:Ljava/lang/Object;

    .line 134
    .line 135
    check-cast v2, Lkn4;

    .line 136
    .line 137
    iget-object v3, p0, Lbx3;->c:Ljava/lang/Object;

    .line 138
    .line 139
    check-cast v3, Ljava/math/BigInteger;

    .line 140
    .line 141
    iget-object p0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 142
    .line 143
    check-cast p0, Ljava/lang/Integer;

    .line 144
    .line 145
    invoke-direct {v1, v2, v3, v0, p0}, Lvn4;-><init>(Lkn4;Ljava/math/BigInteger;Lg20;Ljava/lang/Integer;)V

    .line 146
    .line 147
    .line 148
    return-object v1

    .line 149
    :cond_8
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 150
    .line 151
    const-string v1, "Got modulus size "

    .line 152
    .line 153
    const-string v2, ", but parameters requires modulus size "

    .line 154
    .line 155
    invoke-static {v0, v3, v1, v2}, Lk26;->i(IILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    .line 156
    .line 157
    .line 158
    move-result-object v0

    .line 159
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 160
    .line 161
    .line 162
    throw p0

    .line 163
    :cond_9
    const-string p0, "Cannot build without modulus"

    .line 164
    .line 165
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 166
    .line 167
    .line 168
    return-object v1

    .line 169
    :cond_a
    const-string p0, "Cannot build without parameters"

    .line 170
    .line 171
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 172
    .line 173
    .line 174
    return-object v1
.end method

.method public track(II)Lpi5;
    .locals 3

    .line 1
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/util/SparseArray;

    .line 4
    .line 5
    iget-object v1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast v1, Liq1;

    .line 8
    .line 9
    const/4 v2, 0x3

    .line 10
    if-eq p2, v2, :cond_0

    .line 11
    .line 12
    invoke-interface {v1, p1, p2}, Liq1;->track(II)Lpi5;

    .line 13
    .line 14
    .line 15
    move-result-object p0

    .line 16
    return-object p0

    .line 17
    :cond_0
    invoke-virtual {v0, p1}, Landroid/util/SparseArray;->get(I)Ljava/lang/Object;

    .line 18
    .line 19
    .line 20
    move-result-object v2

    .line 21
    check-cast v2, Le95;

    .line 22
    .line 23
    if-eqz v2, :cond_1

    .line 24
    .line 25
    return-object v2

    .line 26
    :cond_1
    new-instance v2, Le95;

    .line 27
    .line 28
    invoke-interface {v1, p1, p2}, Liq1;->track(II)Lpi5;

    .line 29
    .line 30
    .line 31
    move-result-object p2

    .line 32
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 33
    .line 34
    check-cast p0, La95;

    .line 35
    .line 36
    invoke-direct {v2, p2, p0}, Le95;-><init>(Lpi5;La95;)V

    .line 37
    .line 38
    .line 39
    invoke-virtual {v0, p1, v2}, Landroid/util/SparseArray;->put(ILjava/lang/Object;)V

    .line 40
    .line 41
    .line 42
    return-object v2
.end method

.method public u(Ljava/lang/String;)V
    .locals 4

    .line 1
    iget-object v0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lcom/indymobile/app/activity/PdfPreviewActivity;

    .line 4
    .line 5
    invoke-virtual {v0}, Lmu;->s()V

    .line 6
    .line 7
    .line 8
    invoke-static {p1}, Lud2;->u(Ljava/lang/String;)Z

    .line 9
    .line 10
    .line 11
    move-result v1

    .line 12
    if-nez v1, :cond_0

    .line 13
    .line 14
    const v1, 0x7f1201b2

    .line 15
    .line 16
    .line 17
    invoke-static {v1}, Lwv3;->b(I)Ljava/lang/String;

    .line 18
    .line 19
    .line 20
    move-result-object v1

    .line 21
    iget-object p0, p0, Lbx3;->b:Ljava/lang/Object;

    .line 22
    .line 23
    check-cast p0, Lr14;

    .line 24
    .line 25
    const v2, 0x1080027

    .line 26
    .line 27
    .line 28
    invoke-virtual {v0, v2, v1, p1, p0}, Lmu;->x(ILjava/lang/String;Ljava/lang/String;Llu;)V

    .line 29
    .line 30
    .line 31
    return-void

    .line 32
    :cond_0
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 33
    .line 34
    check-cast p0, Ljava/lang/Runnable;

    .line 35
    .line 36
    iget-object p1, v0, Lcom/indymobile/app/activity/PdfPreviewActivity;->q:Ljava/io/File;

    .line 37
    .line 38
    if-eqz p1, :cond_1

    .line 39
    .line 40
    :try_start_0
    invoke-static {p1}, Lsk2;->r(Ljava/io/File;)Z

    .line 41
    .line 42
    .line 43
    const/4 p1, 0x0

    .line 44
    iput-object p1, v0, Lcom/indymobile/app/activity/PdfPreviewActivity;->q:Ljava/io/File;
    :try_end_0
    .catch Lcom/indymobile/app/util/PSException; {:try_start_0 .. :try_end_0} :catch_0

    .line 45
    .line 46
    :catch_0
    :cond_1
    new-instance p1, Ljava/lang/StringBuilder;

    .line 47
    .line 48
    const-string v1, ""

    .line 49
    .line 50
    invoke-direct {p1, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 51
    .line 52
    .line 53
    new-instance v2, Ljava/util/Date;

    .line 54
    .line 55
    invoke-direct {v2}, Ljava/util/Date;-><init>()V

    .line 56
    .line 57
    .line 58
    invoke-virtual {v2}, Ljava/util/Date;->getTime()J

    .line 59
    .line 60
    .line 61
    move-result-wide v2

    .line 62
    invoke-virtual {p1, v2, v3}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;

    .line 63
    .line 64
    .line 65
    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 66
    .line 67
    .line 68
    move-result-object p1

    .line 69
    invoke-static {p1}, Lsk2;->k(Ljava/lang/String;)Ljava/io/File;

    .line 70
    .line 71
    .line 72
    move-result-object p1

    .line 73
    iget-object v2, v0, Lcom/indymobile/app/activity/PdfPreviewActivity;->n:Lcom/indymobile/app/model/bean/PSShareObject;

    .line 74
    .line 75
    iput-object p1, v2, Lcom/indymobile/app/model/bean/PSShareObject;->tempShareFolder:Ljava/io/File;

    .line 76
    .line 77
    sget-object v3, Ltv3;->kPSShareFileTypePDF:Ltv3;

    .line 78
    .line 79
    iput-object v3, v2, Lcom/indymobile/app/model/bean/PSShareObject;->shareFileType:Ltv3;

    .line 80
    .line 81
    new-instance v2, Ljava/lang/StringBuilder;

    .line 82
    .line 83
    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    .line 84
    .line 85
    .line 86
    const v3, 0x7f120042

    .line 87
    .line 88
    .line 89
    invoke-static {v3}, Lwv3;->b(I)Ljava/lang/String;

    .line 90
    .line 91
    .line 92
    move-result-object v3

    .line 93
    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 94
    .line 95
    .line 96
    const-string v3, "..."

    .line 97
    .line 98
    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 99
    .line 100
    .line 101
    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 102
    .line 103
    .line 104
    move-result-object v2

    .line 105
    invoke-virtual {v0, v1, v2}, Lmu;->K(Ljava/lang/String;Ljava/lang/String;)V

    .line 106
    .line 107
    .line 108
    new-instance v1, Lvi5;

    .line 109
    .line 110
    iget-object v2, v0, Lcom/indymobile/app/activity/PdfPreviewActivity;->n:Lcom/indymobile/app/model/bean/PSShareObject;

    .line 111
    .line 112
    new-instance v3, Lr14;

    .line 113
    .line 114
    invoke-direct {v3, v0, p0}, Lr14;-><init>(Lcom/indymobile/app/activity/PdfPreviewActivity;Ljava/lang/Runnable;)V

    .line 115
    .line 116
    .line 117
    invoke-direct {v1, v2, p1, v3}, Lvi5;-><init>(Lcom/indymobile/app/model/bean/PSShareObject;Ljava/io/File;Lfz3;)V

    .line 118
    .line 119
    .line 120
    invoke-virtual {v1}, Lvi5;->r()V

    .line 121
    .line 122
    .line 123
    return-void
.end method

.method public v()V
    .locals 2

    .line 1
    iget-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Ljava/net/HttpURLConnection;

    .line 4
    .line 5
    if-nez v0, :cond_0

    .line 6
    .line 7
    return-void

    .line 8
    :cond_0
    invoke-virtual {v0}, Ljava/net/URLConnection;->getDoOutput()Z

    .line 9
    .line 10
    .line 11
    move-result v0

    .line 12
    if-eqz v0, :cond_1

    .line 13
    .line 14
    :try_start_0
    iget-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 15
    .line 16
    check-cast v0, Ljava/net/HttpURLConnection;

    .line 17
    .line 18
    invoke-virtual {v0}, Ljava/net/URLConnection;->getOutputStream()Ljava/io/OutputStream;

    .line 19
    .line 20
    .line 21
    move-result-object v0

    .line 22
    sget v1, Lfd2;->a:I

    .line 23
    .line 24
    if-eqz v0, :cond_1

    .line 25
    .line 26
    invoke-interface {v0}, Ljava/io/Closeable;->close()V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    .line 27
    .line 28
    .line 29
    :catch_0
    :cond_1
    const/4 v0, 0x0

    .line 30
    iput-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 31
    .line 32
    return-void
.end method

.method public w(Lka5;Ljava/util/Map$Entry;)V
    .locals 9

    .line 1
    invoke-interface {p2}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;

    .line 2
    .line 3
    .line 4
    move-result-object v0

    .line 5
    move-object v2, v0

    .line 6
    check-cast v2, Lka5;

    .line 7
    .line 8
    iget-object v0, p1, Lka5;->g:Lis;

    .line 9
    .line 10
    iget-object v4, v0, Lis;->a:Landroid/util/Size;

    .line 11
    .line 12
    invoke-interface {p2}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;

    .line 13
    .line 14
    .line 15
    move-result-object v0

    .line 16
    check-cast v0, Lor;

    .line 17
    .line 18
    iget-object v5, v0, Lor;->d:Landroid/graphics/Rect;

    .line 19
    .line 20
    iget-boolean p1, p1, Lka5;->c:Z

    .line 21
    .line 22
    const/4 v0, 0x0

    .line 23
    if-eqz p1, :cond_0

    .line 24
    .line 25
    iget-object p1, p0, Lbx3;->c:Ljava/lang/Object;

    .line 26
    .line 27
    check-cast p1, Lu80;

    .line 28
    .line 29
    move-object v6, p1

    .line 30
    goto :goto_0

    .line 31
    :cond_0
    move-object v6, v0

    .line 32
    :goto_0
    invoke-interface {p2}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;

    .line 33
    .line 34
    .line 35
    move-result-object p1

    .line 36
    check-cast p1, Lor;

    .line 37
    .line 38
    iget v7, p1, Lor;->f:I

    .line 39
    .line 40
    invoke-interface {p2}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;

    .line 41
    .line 42
    .line 43
    move-result-object p1

    .line 44
    check-cast p1, Lor;

    .line 45
    .line 46
    iget-boolean v8, p1, Lor;->g:Z

    .line 47
    .line 48
    new-instance v3, Lls;

    .line 49
    .line 50
    invoke-direct/range {v3 .. v8}, Lls;-><init>(Landroid/util/Size;Landroid/graphics/Rect;Lu80;IZ)V

    .line 51
    .line 52
    .line 53
    invoke-interface {p2}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;

    .line 54
    .line 55
    .line 56
    move-result-object p1

    .line 57
    check-cast p1, Lor;

    .line 58
    .line 59
    iget v4, p1, Lor;->c:I

    .line 60
    .line 61
    invoke-virtual {v2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 62
    .line 63
    .line 64
    invoke-static {}, Lui;->d()V

    .line 65
    .line 66
    .line 67
    invoke-virtual {v2}, Lka5;->a()V

    .line 68
    .line 69
    .line 70
    iget-boolean p1, v2, Lka5;->j:Z

    .line 71
    .line 72
    const/4 p2, 0x1

    .line 73
    xor-int/2addr p1, p2

    .line 74
    const-string v1, "Consumer can only be linked once."

    .line 75
    .line 76
    invoke-static {v1, p1}, Lgy0;->i(Ljava/lang/String;Z)V

    .line 77
    .line 78
    .line 79
    iput-boolean p2, v2, Lka5;->j:Z

    .line 80
    .line 81
    move-object v5, v3

    .line 82
    iget-object v3, v2, Lka5;->l:Lja5;

    .line 83
    .line 84
    invoke-virtual {v3}, Lj71;->c()Liz2;

    .line 85
    .line 86
    .line 87
    move-result-object p1

    .line 88
    new-instance v1, Lia5;

    .line 89
    .line 90
    move-object v6, v0

    .line 91
    invoke-direct/range {v1 .. v6}, Lia5;-><init>(Lka5;Lja5;ILls;Lls;)V

    .line 92
    .line 93
    .line 94
    invoke-static {}, Lpw0;->v()Lt82;

    .line 95
    .line 96
    .line 97
    move-result-object p2

    .line 98
    invoke-static {p1, v1, p2}, Ls46;->T(Liz2;Laj;Ljava/util/concurrent/Executor;)Ljd0;

    .line 99
    .line 100
    .line 101
    move-result-object p1

    .line 102
    new-instance p2, Li43;

    .line 103
    .line 104
    const/16 v0, 0xd

    .line 105
    .line 106
    invoke-direct {p2, v0, p0, v2}, Li43;-><init>(ILjava/lang/Object;Ljava/lang/Object;)V

    .line 107
    .line 108
    .line 109
    invoke-static {}, Lpw0;->v()Lt82;

    .line 110
    .line 111
    .line 112
    move-result-object p0

    .line 113
    new-instance v0, Lp42;

    .line 114
    .line 115
    const/4 v1, 0x0

    .line 116
    invoke-direct {v0, v1, p1, p2}, Lp42;-><init>(ILjava/lang/Object;Ljava/lang/Object;)V

    .line 117
    .line 118
    .line 119
    invoke-virtual {p1, v0, p0}, Lk42;->a(Ljava/lang/Runnable;Ljava/util/concurrent/Executor;)V

    .line 120
    .line 121
    .line 122
    return-void
.end method

.method public x(Lci0;Ljava/io/ByteArrayOutputStream;)V
    .locals 3

    .line 1
    new-instance v0, Lgb4;

    .line 2
    .line 3
    iget-object v1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 4
    .line 5
    check-cast v1, Ljava/util/HashMap;

    .line 6
    .line 7
    iget-object v2, p0, Lbx3;->c:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast v2, Ljava/util/HashMap;

    .line 10
    .line 11
    iget-object p0, p0, Lbx3;->d:Ljava/lang/Object;

    .line 12
    .line 13
    check-cast p0, Lbo3;

    .line 14
    .line 15
    invoke-direct {v0, p2, v1, v2, p0}, Lgb4;-><init>(Ljava/io/ByteArrayOutputStream;Ljava/util/HashMap;Ljava/util/HashMap;Lbo3;)V

    .line 16
    .line 17
    .line 18
    const-class p0, Lci0;

    .line 19
    .line 20
    invoke-virtual {v1, p0}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 21
    .line 22
    .line 23
    move-result-object p2

    .line 24
    check-cast p2, Lbo3;

    .line 25
    .line 26
    if-eqz p2, :cond_0

    .line 27
    .line 28
    invoke-interface {p2, p1, v0}, Lkk1;->a(Ljava/lang/Object;Ljava/lang/Object;)V

    .line 29
    .line 30
    .line 31
    return-void

    .line 32
    :cond_0
    new-instance p1, Lcom/google/firebase/encoders/EncodingException;

    .line 33
    .line 34
    const-string p2, "No encoder for "

    .line 35
    .line 36
    invoke-static {p0, p2}, Lat1;->q(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/String;

    .line 37
    .line 38
    .line 39
    move-result-object p0

    .line 40
    invoke-direct {p1, p0}, Lcom/google/firebase/encoders/EncodingException;-><init>(Ljava/lang/String;)V

    .line 41
    .line 42
    .line 43
    throw p1
.end method

.method public y()Lfh;
    .locals 5

    .line 1
    iget-object v0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Ljava/net/HttpURLConnection;

    .line 4
    .line 5
    const/4 v1, 0x0

    .line 6
    if-eqz v0, :cond_2

    .line 7
    .line 8
    :try_start_0
    iget-object v2, p0, Lbx3;->d:Ljava/lang/Object;

    .line 9
    .line 10
    check-cast v2, Lf55;

    .line 11
    .line 12
    invoke-virtual {v2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 13
    .line 14
    .line 15
    invoke-virtual {v0}, Ljava/net/HttpURLConnection;->getResponseCode()I

    .line 16
    .line 17
    .line 18
    move-result v2

    .line 19
    const/16 v3, 0x190

    .line 20
    .line 21
    if-ge v2, v3, :cond_1

    .line 22
    .line 23
    const/4 v3, -0x1

    .line 24
    if-ne v2, v3, :cond_0

    .line 25
    .line 26
    goto :goto_0

    .line 27
    :cond_0
    invoke-virtual {v0}, Ljava/net/URLConnection;->getInputStream()Ljava/io/InputStream;

    .line 28
    .line 29
    .line 30
    move-result-object v3

    .line 31
    goto :goto_1

    .line 32
    :cond_1
    :goto_0
    invoke-virtual {v0}, Ljava/net/HttpURLConnection;->getErrorStream()Ljava/io/InputStream;

    .line 33
    .line 34
    .line 35
    move-result-object v3

    .line 36
    :goto_1
    new-instance v4, Lfh;

    .line 37
    .line 38
    invoke-virtual {v0}, Ljava/net/URLConnection;->getHeaderFields()Ljava/util/Map;

    .line 39
    .line 40
    .line 41
    move-result-object v0

    .line 42
    invoke-direct {v4, v2, v3, v0}, Lfh;-><init>(ILjava/io/InputStream;Ljava/util/Map;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 43
    .line 44
    .line 45
    iput-object v1, p0, Lbx3;->c:Ljava/lang/Object;

    .line 46
    .line 47
    return-object v4

    .line 48
    :catchall_0
    move-exception v0

    .line 49
    iput-object v1, p0, Lbx3;->c:Ljava/lang/Object;

    .line 50
    .line 51
    throw v0

    .line 52
    :cond_2
    const-string p0, "Can\'t finish().  Uploader already closed."

    .line 53
    .line 54
    invoke-static {p0}, Lr05;->k(Ljava/lang/String;)V

    .line 55
    .line 56
    .line 57
    return-object v1
.end method

.method public z()Ls24;
    .locals 3

    .line 1
    new-instance v0, Ls24;

    .line 2
    .line 3
    iget-object v1, p0, Lbx3;->b:Ljava/lang/Object;

    .line 4
    .line 5
    check-cast v1, Landroid/content/Context;

    .line 6
    .line 7
    iget-object v2, p0, Lbx3;->d:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast v2, Lvi5;

    .line 10
    .line 11
    iget-object p0, p0, Lbx3;->c:Ljava/lang/Object;

    .line 12
    .line 13
    check-cast p0, Lif2;

    .line 14
    .line 15
    invoke-direct {v0, v1, v2, p0}, Ls24;-><init>(Landroid/content/Context;Lvi5;Lif2;)V

    .line 16
    .line 17
    .line 18
    return-object v0
.end method
