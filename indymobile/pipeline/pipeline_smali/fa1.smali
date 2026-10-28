.class public final Lfa1;
.super Ljava/lang/Object;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"

# interfaces
.implements Lep1;
.implements Lu30;
.implements Lti2;
.implements Lr60;
.implements Lyy3;
.implements Lcom/google/android/gms/tasks/OnFailureListener;
.implements Lxt3;


# instance fields
.field public final synthetic a:I

.field public b:Ljava/lang/Object;

.field public c:Ljava/lang/Object;

.field public d:Ljava/lang/Object;


# direct methods
.method public constructor <init>()V
    .locals 7

    const/4 v0, 0x4

    iput v0, p0, Lfa1;->a:I

    const/16 v0, 0xa

    .line 116
    new-array v3, v0, [J

    new-array v4, v0, [J

    new-array v5, v0, [J

    const/4 v2, 0x4

    const/4 v6, 0x0

    move-object v1, p0

    invoke-direct/range {v1 .. v6}, Lfa1;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Z)V

    return-void
.end method

.method public synthetic constructor <init>(I)V
    .locals 0

    .line 149
    iput p1, p0, Lfa1;->a:I

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public synthetic constructor <init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Z)V
    .locals 0

    .line 96
    iput p1, p0, Lfa1;->a:I

    iput-object p2, p0, Lfa1;->b:Ljava/lang/Object;

    iput-object p3, p0, Lfa1;->c:Ljava/lang/Object;

    iput-object p4, p0, Lfa1;->d:Ljava/lang/Object;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .locals 2

    const/4 v0, 0x0

    iput v0, p0, Lfa1;->a:I

    .line 144
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 145
    new-instance v0, Lo92;

    const/16 v1, 0xb

    .line 146
    invoke-direct {v0, v1}, Lo92;-><init>(I)V

    .line 147
    iput-object v0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 148
    iput-object p1, p0, Lfa1;->d:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Landroid/widget/FrameLayout;)V
    .locals 3

    const/16 v0, 0x12

    iput v0, p0, Lfa1;->a:I

    .line 106
    move-object v0, p1

    check-cast v0, Ld83;

    .line 107
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 108
    sget v1, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v2, 0x22

    if-lt v1, v2, :cond_0

    .line 109
    new-instance v1, Lg83;

    .line 110
    invoke-direct {v1}, Ljava/lang/Object;-><init>()V

    goto :goto_0

    :cond_0
    const/16 v2, 0x21

    if-lt v1, v2, :cond_1

    .line 111
    new-instance v1, Le83;

    .line 112
    invoke-direct {v1}, Ljava/lang/Object;-><init>()V

    goto :goto_0

    :cond_1
    const/4 v1, 0x0

    .line 113
    :goto_0
    iput-object v1, p0, Lfa1;->b:Ljava/lang/Object;

    .line 114
    iput-object v0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 115
    iput-object p1, p0, Lfa1;->d:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Lfa1;)V
    .locals 2

    const/4 v0, 0x4

    iput v0, p0, Lfa1;->a:I

    .line 117
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 118
    iget-object v0, p1, Lfa1;->b:Ljava/lang/Object;

    check-cast v0, [J

    const/16 v1, 0xa

    invoke-static {v0, v1}, Ljava/util/Arrays;->copyOf([JI)[J

    move-result-object v0

    iput-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 119
    iget-object v0, p1, Lfa1;->c:Ljava/lang/Object;

    check-cast v0, [J

    invoke-static {v0, v1}, Ljava/util/Arrays;->copyOf([JI)[J

    move-result-object v0

    iput-object v0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 120
    iget-object p1, p1, Lfa1;->d:Ljava/lang/Object;

    check-cast p1, [J

    invoke-static {p1, v1}, Ljava/util/Arrays;->copyOf([JI)[J

    move-result-object p1

    iput-object p1, p0, Lfa1;->d:Ljava/lang/Object;

    return-void
.end method

.method public synthetic constructor <init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)V
    .locals 0

    .line 97
    iput p4, p0, Lfa1;->a:I

    iput-object p1, p0, Lfa1;->d:Ljava/lang/Object;

    iput-object p2, p0, Lfa1;->b:Ljava/lang/Object;

    iput-object p3, p0, Lfa1;->c:Ljava/lang/Object;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public constructor <init>(Ljava/lang/String;I)V
    .locals 2

    iput p2, p0, Lfa1;->a:I

    packed-switch p2, :pswitch_data_0

    .line 132
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 133
    new-instance p2, Liq2;

    const/4 v0, 0x5

    const/4 v1, 0x0

    .line 134
    invoke-direct {p2, v0, v1}, Liq2;-><init>(IZ)V

    .line 135
    iput-object p2, p0, Lfa1;->c:Ljava/lang/Object;

    .line 136
    iput-object p2, p0, Lfa1;->d:Ljava/lang/Object;

    .line 137
    iput-object p1, p0, Lfa1;->b:Ljava/lang/Object;

    return-void

    .line 138
    :pswitch_0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 139
    new-instance p2, Lfa1;

    const/16 v0, 0x15

    .line 140
    invoke-direct {p2, v0}, Lfa1;-><init>(I)V

    .line 141
    iput-object p2, p0, Lfa1;->c:Ljava/lang/Object;

    .line 142
    iput-object p2, p0, Lfa1;->d:Ljava/lang/Object;

    .line 143
    iput-object p1, p0, Lfa1;->b:Ljava/lang/Object;

    return-void

    nop

    :pswitch_data_0
    .packed-switch 0x16
        :pswitch_0
    .end packed-switch
.end method

.method public constructor <init>(Ljava/lang/String;Ljava/util/HashMap;)V
    .locals 1

    const/16 v0, 0x9

    iput v0, p0, Lfa1;->a:I

    .line 102
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 103
    iput-object p1, p0, Lfa1;->b:Ljava/lang/Object;

    .line 104
    iput-object p2, p0, Lfa1;->c:Ljava/lang/Object;

    .line 105
    new-instance p1, Ljava/util/HashMap;

    invoke-direct {p1}, Ljava/util/HashMap;-><init>()V

    iput-object p1, p0, Lfa1;->d:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Ljava/util/List;Lnf3;)V
    .locals 5

    .line 1
    const/16 v0, 0xe

    .line 2
    .line 3
    iput v0, p0, Lfa1;->a:I

    .line 4
    .line 5
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    .line 7
    .line 8
    iput-object p1, p0, Lfa1;->b:Ljava/lang/Object;

    .line 9
    .line 10
    iput-object p2, p0, Lfa1;->c:Ljava/lang/Object;

    .line 11
    .line 12
    sget-object p2, Lw62;->a:Luj;

    .line 13
    .line 14
    iget-object p2, p2, Luj;->a:Ljava/util/concurrent/atomic/AtomicBoolean;

    .line 15
    .line 16
    invoke-virtual {p2}, Ljava/util/concurrent/atomic/AtomicBoolean;->get()Z

    .line 17
    .line 18
    .line 19
    move-result p2

    .line 20
    const/4 v0, 0x0

    .line 21
    if-eqz p2, :cond_4

    .line 22
    .line 23
    new-instance p2, Ljava/util/HashSet;

    .line 24
    .line 25
    invoke-direct {p2}, Ljava/util/HashSet;-><init>()V

    .line 26
    .line 27
    .line 28
    invoke-interface {p1}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    .line 29
    .line 30
    .line 31
    move-result-object p1

    .line 32
    const/4 v1, 0x0

    .line 33
    :cond_0
    :goto_0
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    .line 34
    .line 35
    .line 36
    move-result v2

    .line 37
    if-eqz v2, :cond_2

    .line 38
    .line 39
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 40
    .line 41
    .line 42
    move-result-object v2

    .line 43
    check-cast v2, Lwt2;

    .line 44
    .line 45
    iget v3, v2, Lwt2;->d:I

    .line 46
    .line 47
    invoke-static {v3}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 48
    .line 49
    .line 50
    move-result-object v4

    .line 51
    invoke-virtual {p2, v4}, Ljava/util/HashSet;->contains(Ljava/lang/Object;)Z

    .line 52
    .line 53
    .line 54
    move-result v4

    .line 55
    if-nez v4, :cond_1

    .line 56
    .line 57
    invoke-static {v3}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 58
    .line 59
    .line 60
    move-result-object v3

    .line 61
    invoke-virtual {p2, v3}, Ljava/util/HashSet;->add(Ljava/lang/Object;)Z

    .line 62
    .line 63
    .line 64
    iget-boolean v2, v2, Lwt2;->e:Z

    .line 65
    .line 66
    if-eqz v2, :cond_0

    .line 67
    .line 68
    const/4 v1, 0x1

    .line 69
    goto :goto_0

    .line 70
    :cond_1
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 71
    .line 72
    const-string p1, "KeyID "

    .line 73
    .line 74
    const-string p2, " is duplicated in the keyset, and Tink is configured to reject such keysets with the flag validateKeysetsOnParsing."

    .line 75
    .line 76
    invoke-static {v3, p1, p2}, Lha6;->g(ILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    .line 77
    .line 78
    .line 79
    move-result-object p1

    .line 80
    invoke-direct {p0, p1}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 81
    .line 82
    .line 83
    throw p0

    .line 84
    :cond_2
    if-eqz v1, :cond_3

    .line 85
    .line 86
    goto :goto_1

    .line 87
    :cond_3
    const-string p0, "Primary key id not found in keyset, and Tink is configured to reject such keysets with the flag validateKeysetsOnParsing."

    .line 88
    .line 89
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 90
    .line 91
    .line 92
    throw v0

    .line 93
    :cond_4
    :goto_1
    iput-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 94
    .line 95
    return-void
.end method

.method public constructor <init>(Lt31;)V
    .locals 2

    const/16 v0, 0x17

    iput v0, p0, Lfa1;->a:I

    .line 98
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 99
    new-instance v0, Landroid/util/SparseArray;

    invoke-direct {v0}, Landroid/util/SparseArray;-><init>()V

    iput-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 100
    new-instance v0, Landroid/os/Handler;

    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;

    move-result-object v1

    invoke-direct {v0, v1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    iput-object v0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 101
    iput-object p1, p0, Lfa1;->d:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>(Lt41;Lb41;Lx41;Ljava/util/Set;)V
    .locals 8

    const/4 v0, 0x5

    iput v0, p0, Lfa1;->a:I

    .line 123
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 124
    iput-object p2, p0, Lfa1;->b:Ljava/lang/Object;

    .line 125
    iput-object p1, p0, Lfa1;->c:Ljava/lang/Object;

    .line 126
    iput-object p3, p0, Lfa1;->d:Ljava/lang/Object;

    .line 127
    invoke-interface {p4}, Ljava/util/Set;->isEmpty()Z

    move-result p1

    if-eqz p1, :cond_0

    goto :goto_1

    .line 128
    :cond_0
    invoke-interface {p4}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object p1

    :goto_0
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    move-result p2

    if-eqz p2, :cond_1

    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object p2

    check-cast p2, [I

    .line 129
    new-instance v2, Ljava/lang/String;

    array-length p3, p2

    const/4 p4, 0x0

    invoke-direct {v2, p2, p4, p3}, Ljava/lang/String;-><init>([III)V

    .line 130
    new-instance v7, Lq7;

    invoke-direct {v7, v2, v0}, Lq7;-><init>(Ljava/lang/String;I)V

    .line 131
    invoke-virtual {v2}, Ljava/lang/String;->length()I

    move-result v4

    const/4 v5, 0x1

    const/4 v6, 0x1

    const/4 v3, 0x0

    move-object v1, p0

    invoke-virtual/range {v1 .. v7}, Lfa1;->E(Ljava/lang/CharSequence;IIIZLpj1;)Ljava/lang/Object;

    goto :goto_0

    :cond_1
    :goto_1
    return-void
.end method

.method public constructor <init>(Lv07;)V
    .locals 1

    const/4 v0, 0x4

    iput v0, p0, Lfa1;->a:I

    .line 121
    invoke-direct {p0}, Lfa1;-><init>()V

    .line 122
    invoke-static {p0, p1}, Lfa1;->v(Lfa1;Lv07;)V

    return-void
.end method

.method public static D(Lss2;)Z
    .locals 2

    .line 1
    sget-object v0, Lvt2;->a:[I

    .line 2
    .line 3
    invoke-virtual {p0}, Ljava/lang/Enum;->ordinal()I

    .line 4
    .line 5
    .line 6
    move-result p0

    .line 7
    aget p0, v0, p0

    .line 8
    .line 9
    const/4 v0, 0x1

    .line 10
    if-eq p0, v0, :cond_0

    .line 11
    .line 12
    const/4 v1, 0x2

    .line 13
    if-eq p0, v1, :cond_0

    .line 14
    .line 15
    const/4 v1, 0x3

    .line 16
    if-eq p0, v1, :cond_0

    .line 17
    .line 18
    const/4 p0, 0x0

    .line 19
    return p0

    .line 20
    :cond_0
    return v0
.end method

.method public static final F(Lhp2;)Lfa1;
    .locals 5

    .line 1
    const-string v0, "invalid keyset"

    .line 2
    .line 3
    const/4 v1, 0x0

    .line 4
    :try_start_0
    iget-object p0, p0, Lhp2;->a:Ljava/io/ByteArrayInputStream;
    :try_end_0
    .catch Lcom/google/crypto/tink/shaded/protobuf/InvalidProtocolBufferException; {:try_start_0 .. :try_end_0} :catch_3

    .line 5
    .line 6
    :try_start_1
    new-instance v2, Ljava/lang/String;

    .line 7
    .line 8
    invoke-static {p0}, Lzq5;->a(Ljava/io/ByteArrayInputStream;)[B

    .line 9
    .line 10
    .line 11
    move-result-object v3

    .line 12
    sget-object v4, Lhp2;->b:Ljava/nio/charset/Charset;

    .line 13
    .line 14
    invoke-direct {v2, v3, v4}, Ljava/lang/String;-><init>([BLjava/nio/charset/Charset;)V

    .line 15
    .line 16
    .line 17
    invoke-static {v2}, Lcom/google/crypto/tink/internal/a;->b(Ljava/lang/String;)Lzo2;

    .line 18
    .line 19
    .line 20
    move-result-object v2

    .line 21
    invoke-virtual {v2}, Lzo2;->u()Ltp2;

    .line 22
    .line 23
    .line 24
    move-result-object v2

    .line 25
    invoke-static {v2}, Lhp2;->b(Ltp2;)Lut2;

    .line 26
    .line 27
    .line 28
    move-result-object v2
    :try_end_1
    .catch Lcom/google/gson/JsonParseException; {:try_start_1 .. :try_end_1} :catch_2
    .catch Ljava/lang/IllegalStateException; {:try_start_1 .. :try_end_1} :catch_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 29
    :try_start_2
    invoke-virtual {p0}, Ljava/io/InputStream;->close()V

    .line 30
    .line 31
    .line 32
    invoke-virtual {v2}, Lcom/google/crypto/tink/shaded/protobuf/a;->c()[B

    .line 33
    .line 34
    .line 35
    move-result-object p0
    :try_end_2
    .catch Lcom/google/crypto/tink/shaded/protobuf/InvalidProtocolBufferException; {:try_start_2 .. :try_end_2} :catch_3

    .line 36
    :try_start_3
    invoke-static {}, Lkp1;->a()Lkp1;

    .line 37
    .line 38
    .line 39
    move-result-object v2

    .line 40
    invoke-static {p0, v2}, Lut2;->B([BLkp1;)Lut2;

    .line 41
    .line 42
    .line 43
    move-result-object p0

    .line 44
    invoke-static {p0}, Lfa1;->m(Lut2;)V

    .line 45
    .line 46
    .line 47
    invoke-static {p0}, Lfa1;->u(Lut2;)Lfa1;

    .line 48
    .line 49
    .line 50
    move-result-object p0
    :try_end_3
    .catch Lcom/google/crypto/tink/shaded/protobuf/InvalidProtocolBufferException; {:try_start_3 .. :try_end_3} :catch_0

    .line 51
    return-object p0

    .line 52
    :catch_0
    invoke-static {v0}, Lla6;->x(Ljava/lang/String;)V

    .line 53
    .line 54
    .line 55
    return-object v1

    .line 56
    :catchall_0
    move-exception v2

    .line 57
    goto :goto_1

    .line 58
    :catch_1
    move-exception v2

    .line 59
    goto :goto_0

    .line 60
    :catch_2
    move-exception v2

    .line 61
    :goto_0
    :try_start_4
    new-instance v3, Ljava/io/IOException;

    .line 62
    .line 63
    invoke-direct {v3, v2}, Ljava/io/IOException;-><init>(Ljava/lang/Throwable;)V

    .line 64
    .line 65
    .line 66
    throw v3
    :try_end_4
    .catchall {:try_start_4 .. :try_end_4} :catchall_0

    .line 67
    :goto_1
    :try_start_5
    invoke-virtual {p0}, Ljava/io/InputStream;->close()V

    .line 68
    .line 69
    .line 70
    throw v2
    :try_end_5
    .catch Lcom/google/crypto/tink/shaded/protobuf/InvalidProtocolBufferException; {:try_start_5 .. :try_end_5} :catch_3

    .line 71
    :catch_3
    invoke-static {v0}, Lla6;->x(Ljava/lang/String;)V

    .line 72
    .line 73
    .line 74
    return-object v1
.end method

.method public static H(Landroid/graphics/Bitmap;)Lcom/indymobile/app/model/PSImageEstimationInfoBean;
    .locals 18

    .line 1
    const/16 v0, 0x64

    .line 2
    .line 3
    const/4 v1, 0x1

    .line 4
    move-object/from16 v2, p0

    .line 5
    .line 6
    invoke-static {v2, v0, v0, v1}, Landroid/graphics/Bitmap;->createScaledBitmap(Landroid/graphics/Bitmap;IIZ)Landroid/graphics/Bitmap;

    .line 7
    .line 8
    .line 9
    move-result-object v2

    .line 10
    const/16 v3, 0x100

    .line 11
    .line 12
    new-array v4, v3, [I

    .line 13
    .line 14
    new-array v5, v3, [I

    .line 15
    .line 16
    new-array v6, v3, [I

    .line 17
    .line 18
    new-array v7, v3, [I

    .line 19
    .line 20
    const/4 v9, 0x0

    .line 21
    const/4 v10, 0x0

    .line 22
    const/4 v11, 0x0

    .line 23
    const/4 v12, 0x0

    .line 24
    :goto_0
    if-ge v9, v0, :cond_3

    .line 25
    .line 26
    const/4 v13, 0x0

    .line 27
    :goto_1
    if-ge v13, v0, :cond_2

    .line 28
    .line 29
    invoke-virtual {v2, v9, v13}, Landroid/graphics/Bitmap;->getPixel(II)I

    .line 30
    .line 31
    .line 32
    move-result v14

    .line 33
    invoke-static {v14}, Landroid/graphics/Color;->red(I)I

    .line 34
    .line 35
    .line 36
    move-result v15

    .line 37
    invoke-static {v14}, Landroid/graphics/Color;->blue(I)I

    .line 38
    .line 39
    .line 40
    move-result v0

    .line 41
    invoke-static {v14}, Landroid/graphics/Color;->green(I)I

    .line 42
    .line 43
    .line 44
    move-result v14

    .line 45
    add-int v16, v15, v14

    .line 46
    .line 47
    add-int v16, v16, v0

    .line 48
    .line 49
    div-int/lit8 v16, v16, 0x3

    .line 50
    .line 51
    move/from16 v17, v1

    .line 52
    .line 53
    sget v1, Lwv3;->b:I

    .line 54
    .line 55
    if-lt v15, v1, :cond_0

    .line 56
    .line 57
    sget v1, Lwv3;->c:I

    .line 58
    .line 59
    if-lt v14, v1, :cond_0

    .line 60
    .line 61
    sget v1, Lwv3;->d:I

    .line 62
    .line 63
    if-lt v0, v1, :cond_0

    .line 64
    .line 65
    sub-int v1, v15, v0

    .line 66
    .line 67
    const/16 v8, 0x6e

    .line 68
    .line 69
    if-gt v1, v8, :cond_0

    .line 70
    .line 71
    add-int/lit8 v1, v0, -0x14

    .line 72
    .line 73
    if-lt v14, v1, :cond_0

    .line 74
    .line 75
    add-int/lit8 v1, v15, 0x14

    .line 76
    .line 77
    if-gt v14, v1, :cond_0

    .line 78
    .line 79
    add-int/lit8 v10, v10, 0x1

    .line 80
    .line 81
    :cond_0
    sget v1, Lwv3;->e:I

    .line 82
    .line 83
    if-lt v15, v1, :cond_1

    .line 84
    .line 85
    sget v1, Lwv3;->f:I

    .line 86
    .line 87
    if-lt v14, v1, :cond_1

    .line 88
    .line 89
    sget v1, Lwv3;->g:I

    .line 90
    .line 91
    if-lt v0, v1, :cond_1

    .line 92
    .line 93
    if-gt v15, v14, :cond_1

    .line 94
    .line 95
    if-gt v14, v0, :cond_1

    .line 96
    .line 97
    add-int/lit8 v12, v12, 0x1

    .line 98
    .line 99
    :cond_1
    aget v1, v5, v15

    .line 100
    .line 101
    add-int/lit8 v1, v1, 0x1

    .line 102
    .line 103
    aput v1, v5, v15

    .line 104
    .line 105
    aget v1, v6, v14

    .line 106
    .line 107
    add-int/lit8 v1, v1, 0x1

    .line 108
    .line 109
    aput v1, v6, v14

    .line 110
    .line 111
    aget v1, v7, v0

    .line 112
    .line 113
    add-int/lit8 v1, v1, 0x1

    .line 114
    .line 115
    aput v1, v7, v0

    .line 116
    .line 117
    aget v0, v4, v16

    .line 118
    .line 119
    add-int/lit8 v0, v0, 0x1

    .line 120
    .line 121
    aput v0, v4, v16

    .line 122
    .line 123
    add-int/lit8 v11, v11, 0x1

    .line 124
    .line 125
    add-int/lit8 v13, v13, 0x1

    .line 126
    .line 127
    move/from16 v1, v17

    .line 128
    .line 129
    const/16 v0, 0x64

    .line 130
    .line 131
    goto :goto_1

    .line 132
    :cond_2
    move/from16 v17, v1

    .line 133
    .line 134
    add-int/lit8 v9, v9, 0x1

    .line 135
    .line 136
    const/16 v0, 0x64

    .line 137
    .line 138
    goto :goto_0

    .line 139
    :cond_3
    invoke-static {v2}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 140
    .line 141
    .line 142
    int-to-float v0, v10

    .line 143
    const/high16 v1, 0x42c80000    # 100.0f

    .line 144
    .line 145
    mul-float/2addr v0, v1

    .line 146
    int-to-float v2, v11

    .line 147
    div-float/2addr v0, v2

    .line 148
    int-to-float v5, v12

    .line 149
    mul-float/2addr v5, v1

    .line 150
    div-float/2addr v5, v2

    .line 151
    const/4 v1, 0x0

    .line 152
    const/4 v2, 0x0

    .line 153
    :goto_2
    const/16 v6, 0xc8

    .line 154
    .line 155
    if-ge v1, v6, :cond_4

    .line 156
    .line 157
    if-ge v2, v3, :cond_4

    .line 158
    .line 159
    aget v6, v4, v2

    .line 160
    .line 161
    add-int/2addr v1, v6

    .line 162
    add-int/lit8 v2, v2, 0x1

    .line 163
    .line 164
    goto :goto_2

    .line 165
    :cond_4
    const/16 v1, 0xff

    .line 166
    .line 167
    const/4 v8, 0x0

    .line 168
    :goto_3
    if-ge v8, v6, :cond_5

    .line 169
    .line 170
    if-lez v1, :cond_5

    .line 171
    .line 172
    aget v3, v4, v1

    .line 173
    .line 174
    add-int/2addr v8, v3

    .line 175
    add-int/lit8 v1, v1, -0x1

    .line 176
    .line 177
    goto :goto_3

    .line 178
    :cond_5
    const/high16 v3, 0x42700000    # 60.0f

    .line 179
    .line 180
    cmpl-float v0, v0, v3

    .line 181
    .line 182
    if-gtz v0, :cond_7

    .line 183
    .line 184
    cmpl-float v0, v5, v3

    .line 185
    .line 186
    if-lez v0, :cond_6

    .line 187
    .line 188
    goto :goto_4

    .line 189
    :cond_6
    sget-object v0, Lhv3;->PSImageTypePhotoImage:Lhv3;

    .line 190
    .line 191
    goto :goto_5

    .line 192
    :cond_7
    :goto_4
    sget-object v0, Lhv3;->PSImageTypeDocumentImage:Lhv3;

    .line 193
    .line 194
    :goto_5
    new-instance v3, Lcom/indymobile/app/model/PSImageEstimationInfoBean;

    .line 195
    .line 196
    invoke-direct {v3}, Lcom/indymobile/app/model/PSImageEstimationInfoBean;-><init>()V

    .line 197
    .line 198
    .line 199
    int-to-float v2, v2

    .line 200
    iput v2, v3, Lcom/indymobile/app/model/PSImageEstimationInfoBean;->blackPoint:F

    .line 201
    .line 202
    int-to-float v1, v1

    .line 203
    iput v1, v3, Lcom/indymobile/app/model/PSImageEstimationInfoBean;->whitePoint:F

    .line 204
    .line 205
    iput-object v0, v3, Lcom/indymobile/app/model/PSImageEstimationInfoBean;->imageType:Lhv3;

    .line 206
    .line 207
    return-object v3
.end method

.method public static J(Ltt2;)Lpr0;
    .locals 5

    .line 1
    invoke-static {p0}, Lfa1;->K(Ltt2;)Lsa4;

    .line 2
    .line 3
    .line 4
    move-result-object p0

    .line 5
    sget-object v0, Lfi3;->b:Lfi3;

    .line 6
    .line 7
    iget-object v1, v0, Lfi3;->a:Ljava/util/concurrent/atomic/AtomicReference;

    .line 8
    .line 9
    invoke-virtual {v1}, Ljava/util/concurrent/atomic/AtomicReference;->get()Ljava/lang/Object;

    .line 10
    .line 11
    .line 12
    move-result-object v1

    .line 13
    check-cast v1, Leu4;

    .line 14
    .line 15
    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 16
    .line 17
    .line 18
    new-instance v2, Lcu4;

    .line 19
    .line 20
    const-class v3, Lsa4;

    .line 21
    .line 22
    iget-object v4, p0, Lsa4;->b:Lg20;

    .line 23
    .line 24
    invoke-direct {v2, v3, v4}, Lcu4;-><init>(Ljava/lang/Class;Lg20;)V

    .line 25
    .line 26
    .line 27
    iget-object v1, v1, Leu4;->b:Ljava/util/HashMap;

    .line 28
    .line 29
    invoke-virtual {v1, v2}, Ljava/util/HashMap;->containsKey(Ljava/lang/Object;)Z

    .line 30
    .line 31
    .line 32
    move-result v1

    .line 33
    if-nez v1, :cond_0

    .line 34
    .line 35
    new-instance v0, Ljw2;

    .line 36
    .line 37
    invoke-direct {v0, p0}, Ljw2;-><init>(Lsa4;)V

    .line 38
    .line 39
    .line 40
    return-object v0

    .line 41
    :cond_0
    invoke-virtual {v0, p0}, Lfi3;->a(Lsa4;)Lpr0;

    .line 42
    .line 43
    .line 44
    move-result-object p0

    .line 45
    return-object p0
.end method

.method public static K(Ltt2;)Lsa4;
    .locals 4

    .line 1
    invoke-virtual {p0}, Ltt2;->z()I

    .line 2
    .line 3
    .line 4
    move-result v0

    .line 5
    invoke-virtual {p0}, Ltt2;->A()Les3;

    .line 6
    .line 7
    .line 8
    move-result-object v1

    .line 9
    sget-object v2, Les3;->RAW:Les3;

    .line 10
    .line 11
    if-ne v1, v2, :cond_0

    .line 12
    .line 13
    const/4 v0, 0x0

    .line 14
    goto :goto_0

    .line 15
    :cond_0
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 16
    .line 17
    .line 18
    move-result-object v0

    .line 19
    :goto_0
    invoke-virtual {p0}, Ltt2;->y()Lis2;

    .line 20
    .line 21
    .line 22
    move-result-object v1

    .line 23
    invoke-virtual {v1}, Lis2;->z()Ljava/lang/String;

    .line 24
    .line 25
    .line 26
    move-result-object v1

    .line 27
    invoke-virtual {p0}, Ltt2;->y()Lis2;

    .line 28
    .line 29
    .line 30
    move-result-object v2

    .line 31
    invoke-virtual {v2}, Lis2;->A()Ld20;

    .line 32
    .line 33
    .line 34
    move-result-object v2

    .line 35
    invoke-virtual {p0}, Ltt2;->y()Lis2;

    .line 36
    .line 37
    .line 38
    move-result-object v3

    .line 39
    invoke-virtual {v3}, Lis2;->y()Lhs2;

    .line 40
    .line 41
    .line 42
    move-result-object v3

    .line 43
    invoke-virtual {p0}, Ltt2;->A()Les3;

    .line 44
    .line 45
    .line 46
    move-result-object p0

    .line 47
    invoke-static {v1, v2, v3, p0, v0}, Lsa4;->a(Ljava/lang/String;Ld20;Lhs2;Les3;Ljava/lang/Integer;)Lsa4;

    .line 48
    .line 49
    .line 50
    move-result-object p0

    .line 51
    return-object p0
.end method

.method public static m(Lut2;)V
    .locals 4

    .line 1
    invoke-virtual {p0}, Lut2;->y()Lgk2;

    .line 2
    .line 3
    .line 4
    move-result-object p0

    .line 5
    invoke-interface {p0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    .line 6
    .line 7
    .line 8
    move-result-object p0

    .line 9
    :goto_0
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    .line 10
    .line 11
    .line 12
    move-result v0

    .line 13
    if-eqz v0, :cond_1

    .line 14
    .line 15
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 16
    .line 17
    .line 18
    move-result-object v0

    .line 19
    check-cast v0, Ltt2;

    .line 20
    .line 21
    invoke-virtual {v0}, Ltt2;->y()Lis2;

    .line 22
    .line 23
    .line 24
    move-result-object v1

    .line 25
    invoke-virtual {v1}, Lis2;->y()Lhs2;

    .line 26
    .line 27
    .line 28
    move-result-object v1

    .line 29
    sget-object v2, Lhs2;->UNKNOWN_KEYMATERIAL:Lhs2;

    .line 30
    .line 31
    if-eq v1, v2, :cond_0

    .line 32
    .line 33
    invoke-virtual {v0}, Ltt2;->y()Lis2;

    .line 34
    .line 35
    .line 36
    move-result-object v1

    .line 37
    invoke-virtual {v1}, Lis2;->y()Lhs2;

    .line 38
    .line 39
    .line 40
    move-result-object v1

    .line 41
    sget-object v2, Lhs2;->SYMMETRIC:Lhs2;

    .line 42
    .line 43
    if-eq v1, v2, :cond_0

    .line 44
    .line 45
    invoke-virtual {v0}, Ltt2;->y()Lis2;

    .line 46
    .line 47
    .line 48
    move-result-object v1

    .line 49
    invoke-virtual {v1}, Lis2;->y()Lhs2;

    .line 50
    .line 51
    .line 52
    move-result-object v1

    .line 53
    sget-object v2, Lhs2;->ASYMMETRIC_PRIVATE:Lhs2;

    .line 54
    .line 55
    if-eq v1, v2, :cond_0

    .line 56
    .line 57
    goto :goto_0

    .line 58
    :cond_0
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 59
    .line 60
    invoke-virtual {v0}, Ltt2;->y()Lis2;

    .line 61
    .line 62
    .line 63
    move-result-object v1

    .line 64
    invoke-virtual {v1}, Lis2;->y()Lhs2;

    .line 65
    .line 66
    .line 67
    move-result-object v1

    .line 68
    invoke-virtual {v1}, Ljava/lang/Enum;->name()Ljava/lang/String;

    .line 69
    .line 70
    .line 71
    move-result-object v1

    .line 72
    invoke-virtual {v0}, Ltt2;->y()Lis2;

    .line 73
    .line 74
    .line 75
    move-result-object v0

    .line 76
    invoke-virtual {v0}, Lis2;->z()Ljava/lang/String;

    .line 77
    .line 78
    .line 79
    move-result-object v0

    .line 80
    const-string v2, "keyset contains key material of type "

    .line 81
    .line 82
    const-string v3, " for type url "

    .line 83
    .line 84
    invoke-static {v2, v1, v3, v0}, Lha6;->C(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    .line 85
    .line 86
    .line 87
    move-result-object v0

    .line 88
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 89
    .line 90
    .line 91
    throw p0

    .line 92
    :cond_1
    return-void
.end method

.method public static q(Lpr0;Lss2;I)Ltt2;
    .locals 4

    .line 1
    sget-object v0, Lfi3;->b:Lfi3;

    .line 2
    .line 3
    iget-object v0, v0, Lfi3;->a:Ljava/util/concurrent/atomic/AtomicReference;

    .line 4
    .line 5
    invoke-virtual {v0}, Ljava/util/concurrent/atomic/AtomicReference;->get()Ljava/lang/Object;

    .line 6
    .line 7
    .line 8
    move-result-object v0

    .line 9
    check-cast v0, Leu4;

    .line 10
    .line 11
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 12
    .line 13
    .line 14
    new-instance v1, Ldu4;

    .line 15
    .line 16
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 17
    .line 18
    .line 19
    move-result-object v2

    .line 20
    const-class v3, Lsa4;

    .line 21
    .line 22
    invoke-direct {v1, v2, v3}, Ldu4;-><init>(Ljava/lang/Class;Ljava/lang/Class;)V

    .line 23
    .line 24
    .line 25
    iget-object v0, v0, Leu4;->a:Ljava/util/HashMap;

    .line 26
    .line 27
    invoke-virtual {v0, v1}, Ljava/util/HashMap;->containsKey(Ljava/lang/Object;)Z

    .line 28
    .line 29
    .line 30
    move-result v2

    .line 31
    const/4 v3, 0x0

    .line 32
    if-eqz v2, :cond_0

    .line 33
    .line 34
    invoke-virtual {v0, v1}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 35
    .line 36
    .line 37
    move-result-object v0

    .line 38
    check-cast v0, Lqs2;

    .line 39
    .line 40
    iget-object v0, v0, Lqs2;->b:Lrs2;

    .line 41
    .line 42
    invoke-interface {v0, p0}, Lrs2;->i(Lpr0;)Lsa4;

    .line 43
    .line 44
    .line 45
    move-result-object v0

    .line 46
    goto :goto_0

    .line 47
    :cond_0
    const-string v0, "No Key serializer for "

    .line 48
    .line 49
    const-string v2, " available"

    .line 50
    .line 51
    invoke-static {v0, v1, v2}, Lgr5;->n(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V

    .line 52
    .line 53
    .line 54
    move-object v0, v3

    .line 55
    :goto_0
    invoke-virtual {p0}, Lpr0;->s()Ljava/lang/Integer;

    .line 56
    .line 57
    .line 58
    move-result-object p0

    .line 59
    if-eqz p0, :cond_2

    .line 60
    .line 61
    invoke-virtual {p0}, Ljava/lang/Integer;->intValue()I

    .line 62
    .line 63
    .line 64
    move-result p0

    .line 65
    if-ne p0, p2, :cond_1

    .line 66
    .line 67
    goto :goto_1

    .line 68
    :cond_1
    const-string p0, "Wrong ID set for key with ID requirement"

    .line 69
    .line 70
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 71
    .line 72
    .line 73
    return-object v3

    .line 74
    :cond_2
    :goto_1
    invoke-static {}, Ltt2;->D()Lst2;

    .line 75
    .line 76
    .line 77
    move-result-object p0

    .line 78
    invoke-static {}, Lis2;->B()Lgs2;

    .line 79
    .line 80
    .line 81
    move-result-object v1

    .line 82
    iget-object v2, v0, Lsa4;->a:Ljava/lang/String;

    .line 83
    .line 84
    invoke-virtual {v1}, Le52;->e()V

    .line 85
    .line 86
    .line 87
    iget-object v3, v1, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 88
    .line 89
    check-cast v3, Lis2;

    .line 90
    .line 91
    invoke-static {v3, v2}, Lis2;->u(Lis2;Ljava/lang/String;)V

    .line 92
    .line 93
    .line 94
    iget-object v2, v0, Lsa4;->c:Ld20;

    .line 95
    .line 96
    invoke-virtual {v1}, Le52;->e()V

    .line 97
    .line 98
    .line 99
    iget-object v3, v1, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 100
    .line 101
    check-cast v3, Lis2;

    .line 102
    .line 103
    invoke-static {v3, v2}, Lis2;->v(Lis2;Ld20;)V

    .line 104
    .line 105
    .line 106
    iget-object v2, v0, Lsa4;->d:Lhs2;

    .line 107
    .line 108
    invoke-virtual {v1}, Le52;->e()V

    .line 109
    .line 110
    .line 111
    iget-object v3, v1, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 112
    .line 113
    check-cast v3, Lis2;

    .line 114
    .line 115
    invoke-static {v3, v2}, Lis2;->w(Lis2;Lhs2;)V

    .line 116
    .line 117
    .line 118
    invoke-virtual {p0}, Le52;->e()V

    .line 119
    .line 120
    .line 121
    iget-object v2, p0, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 122
    .line 123
    check-cast v2, Ltt2;

    .line 124
    .line 125
    invoke-virtual {v1}, Le52;->a()Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 126
    .line 127
    .line 128
    move-result-object v1

    .line 129
    check-cast v1, Lis2;

    .line 130
    .line 131
    invoke-static {v2, v1}, Ltt2;->u(Ltt2;Lis2;)V

    .line 132
    .line 133
    .line 134
    invoke-virtual {p0}, Le52;->e()V

    .line 135
    .line 136
    .line 137
    iget-object v1, p0, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 138
    .line 139
    check-cast v1, Ltt2;

    .line 140
    .line 141
    invoke-static {v1, p1}, Ltt2;->w(Ltt2;Lss2;)V

    .line 142
    .line 143
    .line 144
    invoke-virtual {p0}, Le52;->e()V

    .line 145
    .line 146
    .line 147
    iget-object p1, p0, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 148
    .line 149
    check-cast p1, Ltt2;

    .line 150
    .line 151
    invoke-static {p1, p2}, Ltt2;->x(Ltt2;I)V

    .line 152
    .line 153
    .line 154
    iget-object p1, v0, Lsa4;->e:Les3;

    .line 155
    .line 156
    invoke-virtual {p0}, Le52;->e()V

    .line 157
    .line 158
    .line 159
    iget-object p2, p0, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 160
    .line 161
    check-cast p2, Ltt2;

    .line 162
    .line 163
    invoke-static {p2, p1}, Ltt2;->v(Ltt2;Les3;)V

    .line 164
    .line 165
    .line 166
    invoke-virtual {p0}, Le52;->a()Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 167
    .line 168
    .line 169
    move-result-object p0

    .line 170
    check-cast p0, Ltt2;

    .line 171
    .line 172
    return-object p0
.end method

.method public static r(Ljava/lang/String;Ljava/util/HashMap;)Ljava/lang/String;
    .locals 6

    .line 1
    new-instance v0, Ljava/lang/StringBuilder;

    .line 2
    .line 3
    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    .line 4
    .line 5
    .line 6
    invoke-virtual {p1}, Ljava/util/HashMap;->entrySet()Ljava/util/Set;

    .line 7
    .line 8
    .line 9
    move-result-object p1

    .line 10
    invoke-interface {p1}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    .line 11
    .line 12
    .line 13
    move-result-object p1

    .line 14
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 15
    .line 16
    .line 17
    move-result-object v1

    .line 18
    check-cast v1, Ljava/util/Map$Entry;

    .line 19
    .line 20
    invoke-interface {v1}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;

    .line 21
    .line 22
    .line 23
    move-result-object v2

    .line 24
    check-cast v2, Ljava/lang/String;

    .line 25
    .line 26
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 27
    .line 28
    .line 29
    const-string v2, "="

    .line 30
    .line 31
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 32
    .line 33
    .line 34
    invoke-interface {v1}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;

    .line 35
    .line 36
    .line 37
    move-result-object v3

    .line 38
    const-string v4, ""

    .line 39
    .line 40
    const-string v5, "UTF-8"

    .line 41
    .line 42
    if-eqz v3, :cond_0

    .line 43
    .line 44
    invoke-interface {v1}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;

    .line 45
    .line 46
    .line 47
    move-result-object v1

    .line 48
    check-cast v1, Ljava/lang/String;

    .line 49
    .line 50
    invoke-static {v1, v5}, Ljava/net/URLEncoder;->encode(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    .line 51
    .line 52
    .line 53
    move-result-object v1

    .line 54
    goto :goto_0

    .line 55
    :cond_0
    move-object v1, v4

    .line 56
    :goto_0
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 57
    .line 58
    .line 59
    :goto_1
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    .line 60
    .line 61
    .line 62
    move-result v1

    .line 63
    const-string v3, "&"

    .line 64
    .line 65
    if-eqz v1, :cond_2

    .line 66
    .line 67
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 68
    .line 69
    .line 70
    move-result-object v1

    .line 71
    check-cast v1, Ljava/util/Map$Entry;

    .line 72
    .line 73
    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 74
    .line 75
    .line 76
    invoke-interface {v1}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;

    .line 77
    .line 78
    .line 79
    move-result-object v3

    .line 80
    check-cast v3, Ljava/lang/String;

    .line 81
    .line 82
    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 83
    .line 84
    .line 85
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 86
    .line 87
    .line 88
    invoke-interface {v1}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;

    .line 89
    .line 90
    .line 91
    move-result-object v3

    .line 92
    if-eqz v3, :cond_1

    .line 93
    .line 94
    invoke-interface {v1}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;

    .line 95
    .line 96
    .line 97
    move-result-object v1

    .line 98
    check-cast v1, Ljava/lang/String;

    .line 99
    .line 100
    invoke-static {v1, v5}, Ljava/net/URLEncoder;->encode(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    .line 101
    .line 102
    .line 103
    move-result-object v1

    .line 104
    goto :goto_2

    .line 105
    :cond_1
    move-object v1, v4

    .line 106
    :goto_2
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 107
    .line 108
    .line 109
    goto :goto_1

    .line 110
    :cond_2
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 111
    .line 112
    .line 113
    move-result-object p1

    .line 114
    invoke-virtual {p1}, Ljava/lang/String;->isEmpty()Z

    .line 115
    .line 116
    .line 117
    move-result v0

    .line 118
    if-eqz v0, :cond_3

    .line 119
    .line 120
    return-object p0

    .line 121
    :cond_3
    const-string v0, "?"

    .line 122
    .line 123
    invoke-virtual {p0, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    .line 124
    .line 125
    .line 126
    move-result v1

    .line 127
    if-eqz v1, :cond_5

    .line 128
    .line 129
    invoke-virtual {p0, v3}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z

    .line 130
    .line 131
    .line 132
    move-result v0

    .line 133
    if-nez v0, :cond_4

    .line 134
    .line 135
    invoke-virtual {v3, p1}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    .line 136
    .line 137
    .line 138
    move-result-object p1

    .line 139
    :cond_4
    invoke-virtual {p0, p1}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    .line 140
    .line 141
    .line 142
    move-result-object p0

    .line 143
    return-object p0

    .line 144
    :cond_5
    invoke-static {p0, v0, p1}, Lk26;->D(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    .line 145
    .line 146
    .line 147
    move-result-object p0

    .line 148
    return-object p0
.end method

.method public static s(Landroid/text/Editable;Landroid/view/KeyEvent;Z)Z
    .locals 6

    .line 1
    invoke-virtual {p1}, Landroid/view/KeyEvent;->getMetaState()I

    .line 2
    .line 3
    .line 4
    move-result p1

    .line 5
    invoke-static {p1}, Landroid/view/KeyEvent;->metaStateHasNoModifiers(I)Z

    .line 6
    .line 7
    .line 8
    move-result p1

    .line 9
    const/4 v0, 0x0

    .line 10
    if-nez p1, :cond_0

    .line 11
    .line 12
    goto :goto_1

    .line 13
    :cond_0
    invoke-static {p0}, Landroid/text/Selection;->getSelectionStart(Ljava/lang/CharSequence;)I

    .line 14
    .line 15
    .line 16
    move-result p1

    .line 17
    invoke-static {p0}, Landroid/text/Selection;->getSelectionEnd(Ljava/lang/CharSequence;)I

    .line 18
    .line 19
    .line 20
    move-result v1

    .line 21
    const/4 v2, -0x1

    .line 22
    if-eq p1, v2, :cond_6

    .line 23
    .line 24
    if-eq v1, v2, :cond_6

    .line 25
    .line 26
    if-eq p1, v1, :cond_1

    .line 27
    .line 28
    goto :goto_1

    .line 29
    :cond_1
    const-class v2, Lsl5;

    .line 30
    .line 31
    invoke-interface {p0, p1, v1, v2}, Landroid/text/Spanned;->getSpans(IILjava/lang/Class;)[Ljava/lang/Object;

    .line 32
    .line 33
    .line 34
    move-result-object v1

    .line 35
    check-cast v1, [Lsl5;

    .line 36
    .line 37
    if-eqz v1, :cond_6

    .line 38
    .line 39
    array-length v2, v1

    .line 40
    if-lez v2, :cond_6

    .line 41
    .line 42
    array-length v2, v1

    .line 43
    move v3, v0

    .line 44
    :goto_0
    if-ge v3, v2, :cond_6

    .line 45
    .line 46
    aget-object v4, v1, v3

    .line 47
    .line 48
    invoke-interface {p0, v4}, Landroid/text/Spanned;->getSpanStart(Ljava/lang/Object;)I

    .line 49
    .line 50
    .line 51
    move-result v5

    .line 52
    invoke-interface {p0, v4}, Landroid/text/Spanned;->getSpanEnd(Ljava/lang/Object;)I

    .line 53
    .line 54
    .line 55
    move-result v4

    .line 56
    if-eqz p2, :cond_2

    .line 57
    .line 58
    if-eq v5, p1, :cond_4

    .line 59
    .line 60
    :cond_2
    if-nez p2, :cond_3

    .line 61
    .line 62
    if-eq v4, p1, :cond_4

    .line 63
    .line 64
    :cond_3
    if-le p1, v5, :cond_5

    .line 65
    .line 66
    if-ge p1, v4, :cond_5

    .line 67
    .line 68
    :cond_4
    invoke-interface {p0, v5, v4}, Landroid/text/Editable;->delete(II)Landroid/text/Editable;

    .line 69
    .line 70
    .line 71
    const/4 p0, 0x1

    .line 72
    return p0

    .line 73
    :cond_5
    add-int/lit8 v3, v3, 0x1

    .line 74
    .line 75
    goto :goto_0

    .line 76
    :cond_6
    :goto_1
    return v0
.end method

.method public static final u(Lut2;)Lfa1;
    .locals 12

    .line 1
    invoke-virtual {p0}, Lut2;->x()I

    .line 2
    .line 3
    .line 4
    move-result v0

    .line 5
    const/4 v1, 0x0

    .line 6
    if-lez v0, :cond_5

    .line 7
    .line 8
    new-instance v2, Ljava/util/ArrayList;

    .line 9
    .line 10
    invoke-virtual {p0}, Lut2;->x()I

    .line 11
    .line 12
    .line 13
    move-result v0

    .line 14
    invoke-direct {v2, v0}, Ljava/util/ArrayList;-><init>(I)V

    .line 15
    .line 16
    .line 17
    invoke-virtual {p0}, Lut2;->y()Lgk2;

    .line 18
    .line 19
    .line 20
    move-result-object v0

    .line 21
    invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    .line 22
    .line 23
    .line 24
    move-result-object v3

    .line 25
    :goto_0
    invoke-interface {v3}, Ljava/util/Iterator;->hasNext()Z

    .line 26
    .line 27
    .line 28
    move-result v0

    .line 29
    if-eqz v0, :cond_4

    .line 30
    .line 31
    invoke-interface {v3}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 32
    .line 33
    .line 34
    move-result-object v0

    .line 35
    move-object v4, v0

    .line 36
    check-cast v4, Ltt2;

    .line 37
    .line 38
    invoke-virtual {v4}, Ltt2;->z()I

    .line 39
    .line 40
    .line 41
    move-result v8

    .line 42
    const/4 v5, 0x1

    .line 43
    const/4 v6, 0x0

    .line 44
    :try_start_0
    invoke-static {v4}, Lfa1;->J(Ltt2;)Lpr0;

    .line 45
    .line 46
    .line 47
    move-result-object v0
    :try_end_0
    .catch Ljava/security/GeneralSecurityException; {:try_start_0 .. :try_end_0} :catch_0

    .line 48
    move v10, v6

    .line 49
    goto :goto_1

    .line 50
    :catch_0
    move-exception v0

    .line 51
    sget-object v7, Lw62;->a:Luj;

    .line 52
    .line 53
    iget-object v7, v7, Luj;->a:Ljava/util/concurrent/atomic/AtomicBoolean;

    .line 54
    .line 55
    invoke-virtual {v7}, Ljava/util/concurrent/atomic/AtomicBoolean;->get()Z

    .line 56
    .line 57
    .line 58
    move-result v7

    .line 59
    if-nez v7, :cond_3

    .line 60
    .line 61
    new-instance v0, Ljw2;

    .line 62
    .line 63
    invoke-static {v4}, Lfa1;->K(Ltt2;)Lsa4;

    .line 64
    .line 65
    .line 66
    move-result-object v7

    .line 67
    invoke-direct {v0, v7}, Ljw2;-><init>(Lsa4;)V

    .line 68
    .line 69
    .line 70
    move v10, v5

    .line 71
    :goto_1
    sget-object v7, Lw62;->a:Luj;

    .line 72
    .line 73
    iget-object v7, v7, Luj;->a:Ljava/util/concurrent/atomic/AtomicBoolean;

    .line 74
    .line 75
    invoke-virtual {v7}, Ljava/util/concurrent/atomic/AtomicBoolean;->get()Z

    .line 76
    .line 77
    .line 78
    move-result v7

    .line 79
    if-eqz v7, :cond_0

    .line 80
    .line 81
    invoke-virtual {v4}, Ltt2;->B()Lss2;

    .line 82
    .line 83
    .line 84
    move-result-object v7

    .line 85
    invoke-static {v7}, Lfa1;->D(Lss2;)Z

    .line 86
    .line 87
    .line 88
    move-result v7

    .line 89
    if-eqz v7, :cond_1

    .line 90
    .line 91
    :cond_0
    move v7, v5

    .line 92
    goto :goto_2

    .line 93
    :cond_1
    const-string p0, "Parsing of a single key failed (wrong status) and Tink is configured via validateKeysetsOnParsing to reject such keysets."

    .line 94
    .line 95
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 96
    .line 97
    .line 98
    return-object v1

    .line 99
    :goto_2
    new-instance v5, Lwt2;

    .line 100
    .line 101
    invoke-virtual {v4}, Ltt2;->B()Lss2;

    .line 102
    .line 103
    .line 104
    move-result-object v4

    .line 105
    invoke-virtual {p0}, Lut2;->z()I

    .line 106
    .line 107
    .line 108
    move-result v9

    .line 109
    if-ne v8, v9, :cond_2

    .line 110
    .line 111
    move v9, v7

    .line 112
    goto :goto_3

    .line 113
    :cond_2
    move v9, v6

    .line 114
    :goto_3
    sget-object v11, Lwt2;->h:Lj92;

    .line 115
    .line 116
    move-object v6, v0

    .line 117
    move-object v7, v4

    .line 118
    invoke-direct/range {v5 .. v11}, Lwt2;-><init>(Lpr0;Lss2;IZZLj92;)V

    .line 119
    .line 120
    .line 121
    invoke-virtual {v2, v5}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 122
    .line 123
    .line 124
    goto :goto_0

    .line 125
    :cond_3
    throw v0

    .line 126
    :cond_4
    invoke-static {v2}, Ljava/util/Collections;->unmodifiableList(Ljava/util/List;)Ljava/util/List;

    .line 127
    .line 128
    .line 129
    move-result-object p0

    .line 130
    new-instance v0, Lfa1;

    .line 131
    .line 132
    sget-object v1, Lnf3;->b:Lnf3;

    .line 133
    .line 134
    invoke-direct {v0, p0, v1}, Lfa1;-><init>(Ljava/util/List;Lnf3;)V

    .line 135
    .line 136
    .line 137
    return-object v0

    .line 138
    :cond_5
    const-string p0, "empty keyset"

    .line 139
    .line 140
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 141
    .line 142
    .line 143
    return-object v1
.end method

.method public static v(Lfa1;Lv07;)V
    .locals 3

    .line 1
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, [J

    .line 4
    .line 5
    iget-object v1, p1, Lv07;->b:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast v1, Lfa1;

    .line 8
    .line 9
    iget-object v2, v1, Lfa1;->b:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast v2, [J

    .line 12
    .line 13
    iget-object p1, p1, Lv07;->c:Ljava/lang/Object;

    .line 14
    .line 15
    check-cast p1, [J

    .line 16
    .line 17
    invoke-static {v0, v2, p1}, Ltq3;->s([J[J[J)V

    .line 18
    .line 19
    .line 20
    iget-object v0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 21
    .line 22
    check-cast v0, [J

    .line 23
    .line 24
    iget-object v2, v1, Lfa1;->c:Ljava/lang/Object;

    .line 25
    .line 26
    check-cast v2, [J

    .line 27
    .line 28
    iget-object v1, v1, Lfa1;->d:Ljava/lang/Object;

    .line 29
    .line 30
    check-cast v1, [J

    .line 31
    .line 32
    invoke-static {v0, v2, v1}, Ltq3;->s([J[J[J)V

    .line 33
    .line 34
    .line 35
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 36
    .line 37
    check-cast p0, [J

    .line 38
    .line 39
    invoke-static {p0, v1, p1}, Ltq3;->s([J[J[J)V

    .line 40
    .line 41
    .line 42
    return-void
.end method


# virtual methods
.method public A(Ljava/lang/Object;)V
    .locals 1

    .line 1
    check-cast p1, Ljava/lang/Void;

    .line 2
    .line 3
    iget-object p1, p0, Lfa1;->d:Ljava/lang/Object;

    .line 4
    .line 5
    check-cast p1, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;

    .line 6
    .line 7
    sget v0, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;->n0:I

    .line 8
    .line 9
    invoke-virtual {p1}, Lmu;->s()V

    .line 10
    .line 11
    .line 12
    iget-object p1, p1, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;->W:Lcom/indymobile/app/activity/editor/markup/MarkupView;

    .line 13
    .line 14
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 15
    .line 16
    check-cast v0, Lcom/indymobile/app/model/PSPage;

    .line 17
    .line 18
    invoke-virtual {p1, v0}, Lcom/indymobile/app/activity/editor/markup/MarkupView;->K(Lcom/indymobile/app/model/PSPage;)V

    .line 19
    .line 20
    .line 21
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 22
    .line 23
    check-cast p0, Ljava/lang/Runnable;

    .line 24
    .line 25
    if-eqz p0, :cond_0

    .line 26
    .line 27
    invoke-interface {p0}, Ljava/lang/Runnable;->run()V

    .line 28
    .line 29
    .line 30
    :cond_0
    return-void
.end method

.method public B(Ljava/lang/CharSequence;IILrl5;)Z
    .locals 6

    .line 1
    iget v0, p4, Lrl5;->c:I

    .line 2
    .line 3
    and-int/lit8 v0, v0, 0x3

    .line 4
    .line 5
    const/4 v1, 0x2

    .line 6
    const/4 v2, 0x0

    .line 7
    const/4 v3, 0x1

    .line 8
    if-nez v0, :cond_4

    .line 9
    .line 10
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 11
    .line 12
    check-cast p0, Lx41;

    .line 13
    .line 14
    invoke-virtual {p4}, Lrl5;->b()Lse3;

    .line 15
    .line 16
    .line 17
    move-result-object v0

    .line 18
    const/16 v4, 0x8

    .line 19
    .line 20
    invoke-virtual {v0, v4}, Ln53;->a(I)I

    .line 21
    .line 22
    .line 23
    move-result v4

    .line 24
    if-eqz v4, :cond_0

    .line 25
    .line 26
    iget-object v5, v0, Ln53;->d:Ljava/lang/Object;

    .line 27
    .line 28
    check-cast v5, Ljava/nio/ByteBuffer;

    .line 29
    .line 30
    iget v0, v0, Ln53;->a:I

    .line 31
    .line 32
    add-int/2addr v4, v0

    .line 33
    invoke-virtual {v5, v4}, Ljava/nio/ByteBuffer;->getShort(I)S

    .line 34
    .line 35
    .line 36
    :cond_0
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 37
    .line 38
    .line 39
    sget-object v0, Lx41;->b:Ljava/lang/ThreadLocal;

    .line 40
    .line 41
    invoke-virtual {v0}, Ljava/lang/ThreadLocal;->get()Ljava/lang/Object;

    .line 42
    .line 43
    .line 44
    move-result-object v4

    .line 45
    if-nez v4, :cond_1

    .line 46
    .line 47
    new-instance v4, Ljava/lang/StringBuilder;

    .line 48
    .line 49
    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    .line 50
    .line 51
    .line 52
    invoke-virtual {v0, v4}, Ljava/lang/ThreadLocal;->set(Ljava/lang/Object;)V

    .line 53
    .line 54
    .line 55
    :cond_1
    invoke-virtual {v0}, Ljava/lang/ThreadLocal;->get()Ljava/lang/Object;

    .line 56
    .line 57
    .line 58
    move-result-object v0

    .line 59
    check-cast v0, Ljava/lang/StringBuilder;

    .line 60
    .line 61
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->setLength(I)V

    .line 62
    .line 63
    .line 64
    :goto_0
    if-ge p2, p3, :cond_2

    .line 65
    .line 66
    invoke-interface {p1, p2}, Ljava/lang/CharSequence;->charAt(I)C

    .line 67
    .line 68
    .line 69
    move-result v4

    .line 70
    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    .line 71
    .line 72
    .line 73
    add-int/lit8 p2, p2, 0x1

    .line 74
    .line 75
    goto :goto_0

    .line 76
    :cond_2
    iget-object p0, p0, Lx41;->a:Landroid/text/TextPaint;

    .line 77
    .line 78
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 79
    .line 80
    .line 81
    move-result-object p1

    .line 82
    sget p2, Lzz3;->a:I

    .line 83
    .line 84
    invoke-virtual {p0, p1}, Landroid/graphics/Paint;->hasGlyph(Ljava/lang/String;)Z

    .line 85
    .line 86
    .line 87
    move-result p0

    .line 88
    iget p1, p4, Lrl5;->c:I

    .line 89
    .line 90
    and-int/lit8 p1, p1, 0x4

    .line 91
    .line 92
    if-eqz p0, :cond_3

    .line 93
    .line 94
    or-int/lit8 p0, p1, 0x2

    .line 95
    .line 96
    goto :goto_1

    .line 97
    :cond_3
    or-int/lit8 p0, p1, 0x1

    .line 98
    .line 99
    :goto_1
    iput p0, p4, Lrl5;->c:I

    .line 100
    .line 101
    :cond_4
    iget p0, p4, Lrl5;->c:I

    .line 102
    .line 103
    and-int/lit8 p0, p0, 0x3

    .line 104
    .line 105
    if-ne p0, v1, :cond_5

    .line 106
    .line 107
    return v3

    .line 108
    :cond_5
    return v2
.end method

.method public C(Ljava/lang/String;Ljava/lang/String;)V
    .locals 0

    .line 1
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Ljava/util/HashMap;

    .line 4
    .line 5
    invoke-virtual {p0, p1, p2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 6
    .line 7
    .line 8
    return-void
.end method

.method public E(Ljava/lang/CharSequence;IIIZLpj1;)Ljava/lang/Object;
    .locals 16

    .line 1
    move-object/from16 v0, p0

    .line 2
    .line 3
    move-object/from16 v1, p1

    .line 4
    .line 5
    move/from16 v2, p3

    .line 6
    .line 7
    move/from16 v3, p4

    .line 8
    .line 9
    move-object/from16 v4, p6

    .line 10
    .line 11
    new-instance v5, Lqj1;

    .line 12
    .line 13
    iget-object v6, v0, Lfa1;->c:Ljava/lang/Object;

    .line 14
    .line 15
    check-cast v6, Lt41;

    .line 16
    .line 17
    iget-object v6, v6, Lt41;->c:Ljava/lang/Object;

    .line 18
    .line 19
    check-cast v6, Lve3;

    .line 20
    .line 21
    invoke-direct {v5, v6}, Lqj1;-><init>(Lve3;)V

    .line 22
    .line 23
    .line 24
    invoke-static/range {p1 .. p2}, Ljava/lang/Character;->codePointAt(Ljava/lang/CharSequence;I)I

    .line 25
    .line 26
    .line 27
    move-result v6

    .line 28
    const/4 v7, 0x0

    .line 29
    const/4 v8, 0x1

    .line 30
    move v9, v6

    .line 31
    move v10, v7

    .line 32
    move v11, v8

    .line 33
    move/from16 v6, p2

    .line 34
    .line 35
    :cond_0
    :goto_0
    move v7, v6

    .line 36
    :goto_1
    const/4 v12, 0x2

    .line 37
    if-ge v6, v2, :cond_f

    .line 38
    .line 39
    if-ge v10, v3, :cond_f

    .line 40
    .line 41
    if-eqz v11, :cond_f

    .line 42
    .line 43
    iget-object v13, v5, Lqj1;->c:Lve3;

    .line 44
    .line 45
    iget-object v13, v13, Lve3;->a:Landroid/util/SparseArray;

    .line 46
    .line 47
    if-nez v13, :cond_1

    .line 48
    .line 49
    const/4 v13, 0x0

    .line 50
    goto :goto_2

    .line 51
    :cond_1
    invoke-virtual {v13, v9}, Landroid/util/SparseArray;->get(I)Ljava/lang/Object;

    .line 52
    .line 53
    .line 54
    move-result-object v13

    .line 55
    check-cast v13, Lve3;

    .line 56
    .line 57
    :goto_2
    iget v14, v5, Lqj1;->a:I

    .line 58
    .line 59
    const/4 v15, 0x3

    .line 60
    if-eq v14, v12, :cond_3

    .line 61
    .line 62
    if-nez v13, :cond_2

    .line 63
    .line 64
    invoke-virtual {v5}, Lqj1;->a()V

    .line 65
    .line 66
    .line 67
    :goto_3
    move v13, v8

    .line 68
    goto :goto_6

    .line 69
    :cond_2
    iput v12, v5, Lqj1;->a:I

    .line 70
    .line 71
    iput-object v13, v5, Lqj1;->c:Lve3;

    .line 72
    .line 73
    iput v8, v5, Lqj1;->f:I

    .line 74
    .line 75
    :goto_4
    move v13, v12

    .line 76
    goto :goto_6

    .line 77
    :cond_3
    if-eqz v13, :cond_4

    .line 78
    .line 79
    iput-object v13, v5, Lqj1;->c:Lve3;

    .line 80
    .line 81
    iget v13, v5, Lqj1;->f:I

    .line 82
    .line 83
    add-int/2addr v13, v8

    .line 84
    iput v13, v5, Lqj1;->f:I

    .line 85
    .line 86
    goto :goto_4

    .line 87
    :cond_4
    const v13, 0xfe0e

    .line 88
    .line 89
    .line 90
    if-ne v9, v13, :cond_5

    .line 91
    .line 92
    invoke-virtual {v5}, Lqj1;->a()V

    .line 93
    .line 94
    .line 95
    goto :goto_3

    .line 96
    :cond_5
    const v13, 0xfe0f

    .line 97
    .line 98
    .line 99
    if-ne v9, v13, :cond_6

    .line 100
    .line 101
    goto :goto_4

    .line 102
    :cond_6
    iget-object v13, v5, Lqj1;->c:Lve3;

    .line 103
    .line 104
    iget-object v14, v13, Lve3;->b:Lrl5;

    .line 105
    .line 106
    if-eqz v14, :cond_9

    .line 107
    .line 108
    iget v14, v5, Lqj1;->f:I

    .line 109
    .line 110
    if-ne v14, v8, :cond_8

    .line 111
    .line 112
    invoke-virtual {v5}, Lqj1;->b()Z

    .line 113
    .line 114
    .line 115
    move-result v13

    .line 116
    if-eqz v13, :cond_7

    .line 117
    .line 118
    iget-object v13, v5, Lqj1;->c:Lve3;

    .line 119
    .line 120
    iput-object v13, v5, Lqj1;->d:Lve3;

    .line 121
    .line 122
    invoke-virtual {v5}, Lqj1;->a()V

    .line 123
    .line 124
    .line 125
    :goto_5
    move v13, v15

    .line 126
    goto :goto_6

    .line 127
    :cond_7
    invoke-virtual {v5}, Lqj1;->a()V

    .line 128
    .line 129
    .line 130
    goto :goto_3

    .line 131
    :cond_8
    iput-object v13, v5, Lqj1;->d:Lve3;

    .line 132
    .line 133
    invoke-virtual {v5}, Lqj1;->a()V

    .line 134
    .line 135
    .line 136
    goto :goto_5

    .line 137
    :cond_9
    invoke-virtual {v5}, Lqj1;->a()V

    .line 138
    .line 139
    .line 140
    goto :goto_3

    .line 141
    :goto_6
    iput v9, v5, Lqj1;->e:I

    .line 142
    .line 143
    if-eq v13, v8, :cond_e

    .line 144
    .line 145
    if-eq v13, v12, :cond_c

    .line 146
    .line 147
    if-eq v13, v15, :cond_a

    .line 148
    .line 149
    goto :goto_1

    .line 150
    :cond_a
    if-nez p5, :cond_b

    .line 151
    .line 152
    iget-object v12, v5, Lqj1;->d:Lve3;

    .line 153
    .line 154
    iget-object v12, v12, Lve3;->b:Lrl5;

    .line 155
    .line 156
    invoke-virtual {v0, v1, v7, v6, v12}, Lfa1;->B(Ljava/lang/CharSequence;IILrl5;)Z

    .line 157
    .line 158
    .line 159
    move-result v12

    .line 160
    if-nez v12, :cond_0

    .line 161
    .line 162
    :cond_b
    iget-object v11, v5, Lqj1;->d:Lve3;

    .line 163
    .line 164
    iget-object v11, v11, Lve3;->b:Lrl5;

    .line 165
    .line 166
    invoke-interface {v4, v1, v7, v6, v11}, Lpj1;->e(Ljava/lang/CharSequence;IILrl5;)Z

    .line 167
    .line 168
    .line 169
    move-result v11

    .line 170
    add-int/lit8 v10, v10, 0x1

    .line 171
    .line 172
    goto/16 :goto_0

    .line 173
    .line 174
    :cond_c
    invoke-static {v9}, Ljava/lang/Character;->charCount(I)I

    .line 175
    .line 176
    .line 177
    move-result v12

    .line 178
    add-int/2addr v12, v6

    .line 179
    if-ge v12, v2, :cond_d

    .line 180
    .line 181
    invoke-static {v1, v12}, Ljava/lang/Character;->codePointAt(Ljava/lang/CharSequence;I)I

    .line 182
    .line 183
    .line 184
    move-result v6

    .line 185
    move v9, v6

    .line 186
    :cond_d
    move v6, v12

    .line 187
    goto/16 :goto_1

    .line 188
    .line 189
    :cond_e
    invoke-static {v1, v7}, Ljava/lang/Character;->codePointAt(Ljava/lang/CharSequence;I)I

    .line 190
    .line 191
    .line 192
    move-result v6

    .line 193
    invoke-static {v6}, Ljava/lang/Character;->charCount(I)I

    .line 194
    .line 195
    .line 196
    move-result v6

    .line 197
    add-int/2addr v6, v7

    .line 198
    if-ge v6, v2, :cond_0

    .line 199
    .line 200
    invoke-static {v1, v6}, Ljava/lang/Character;->codePointAt(Ljava/lang/CharSequence;I)I

    .line 201
    .line 202
    .line 203
    move-result v7

    .line 204
    move v9, v7

    .line 205
    goto/16 :goto_0

    .line 206
    .line 207
    :cond_f
    iget v2, v5, Lqj1;->a:I

    .line 208
    .line 209
    if-ne v2, v12, :cond_12

    .line 210
    .line 211
    iget-object v2, v5, Lqj1;->c:Lve3;

    .line 212
    .line 213
    iget-object v2, v2, Lve3;->b:Lrl5;

    .line 214
    .line 215
    if-eqz v2, :cond_12

    .line 216
    .line 217
    iget v2, v5, Lqj1;->f:I

    .line 218
    .line 219
    if-gt v2, v8, :cond_10

    .line 220
    .line 221
    invoke-virtual {v5}, Lqj1;->b()Z

    .line 222
    .line 223
    .line 224
    move-result v2

    .line 225
    if-eqz v2, :cond_12

    .line 226
    .line 227
    :cond_10
    if-ge v10, v3, :cond_12

    .line 228
    .line 229
    if-eqz v11, :cond_12

    .line 230
    .line 231
    if-nez p5, :cond_11

    .line 232
    .line 233
    iget-object v2, v5, Lqj1;->c:Lve3;

    .line 234
    .line 235
    iget-object v2, v2, Lve3;->b:Lrl5;

    .line 236
    .line 237
    invoke-virtual {v0, v1, v7, v6, v2}, Lfa1;->B(Ljava/lang/CharSequence;IILrl5;)Z

    .line 238
    .line 239
    .line 240
    move-result v0

    .line 241
    if-nez v0, :cond_12

    .line 242
    .line 243
    :cond_11
    iget-object v0, v5, Lqj1;->c:Lve3;

    .line 244
    .line 245
    iget-object v0, v0, Lve3;->b:Lrl5;

    .line 246
    .line 247
    invoke-interface {v4, v1, v7, v6, v0}, Lpj1;->e(Ljava/lang/CharSequence;IILrl5;)Z

    .line 248
    .line 249
    .line 250
    :cond_12
    invoke-interface {v4}, Lpj1;->getResult()Ljava/lang/Object;

    .line 251
    .line 252
    .line 253
    move-result-object v0

    .line 254
    return-object v0
.end method

.method public G(Lts;IZ)V
    .locals 17

    .line 1
    move-object/from16 v0, p0

    .line 2
    .line 3
    move-object/from16 v1, p1

    .line 4
    .line 5
    move/from16 v2, p2

    .line 6
    .line 7
    iget-object v3, v0, Lfa1;->c:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast v3, Lzr;

    .line 10
    .line 11
    new-instance v4, Landroid/content/ComponentName;

    .line 12
    .line 13
    iget-object v5, v0, Lfa1;->d:Ljava/lang/Object;

    .line 14
    .line 15
    check-cast v5, Landroid/content/Context;

    .line 16
    .line 17
    const-class v6, Lcom/google/android/datatransport/runtime/scheduling/jobscheduling/JobInfoSchedulerService;

    .line 18
    .line 19
    invoke-direct {v4, v5, v6}, Landroid/content/ComponentName;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    .line 20
    .line 21
    .line 22
    const-string v6, "jobscheduler"

    .line 23
    .line 24
    invoke-virtual {v5, v6}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    .line 25
    .line 26
    .line 27
    move-result-object v6

    .line 28
    check-cast v6, Landroid/app/job/JobScheduler;

    .line 29
    .line 30
    new-instance v7, Ljava/util/zip/Adler32;

    .line 31
    .line 32
    invoke-direct {v7}, Ljava/util/zip/Adler32;-><init>()V

    .line 33
    .line 34
    .line 35
    invoke-virtual {v5}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    .line 36
    .line 37
    .line 38
    move-result-object v5

    .line 39
    const-string v8, "UTF-8"

    .line 40
    .line 41
    invoke-static {v8}, Ljava/nio/charset/Charset;->forName(Ljava/lang/String;)Ljava/nio/charset/Charset;

    .line 42
    .line 43
    .line 44
    move-result-object v9

    .line 45
    invoke-virtual {v5, v9}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B

    .line 46
    .line 47
    .line 48
    move-result-object v5

    .line 49
    invoke-virtual {v7, v5}, Ljava/util/zip/Adler32;->update([B)V

    .line 50
    .line 51
    .line 52
    iget-object v5, v1, Lts;->a:Ljava/lang/String;

    .line 53
    .line 54
    invoke-static {v8}, Ljava/nio/charset/Charset;->forName(Ljava/lang/String;)Ljava/nio/charset/Charset;

    .line 55
    .line 56
    .line 57
    move-result-object v8

    .line 58
    invoke-virtual {v5, v8}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B

    .line 59
    .line 60
    .line 61
    move-result-object v8

    .line 62
    invoke-virtual {v7, v8}, Ljava/util/zip/Adler32;->update([B)V

    .line 63
    .line 64
    .line 65
    const/4 v8, 0x4

    .line 66
    invoke-static {v8}, Ljava/nio/ByteBuffer;->allocate(I)Ljava/nio/ByteBuffer;

    .line 67
    .line 68
    .line 69
    move-result-object v8

    .line 70
    iget-object v9, v1, Lts;->c:Lk84;

    .line 71
    .line 72
    invoke-static {v9}, Ln84;->a(Lk84;)I

    .line 73
    .line 74
    .line 75
    move-result v10

    .line 76
    invoke-virtual {v8, v10}, Ljava/nio/ByteBuffer;->putInt(I)Ljava/nio/ByteBuffer;

    .line 77
    .line 78
    .line 79
    move-result-object v8

    .line 80
    invoke-virtual {v8}, Ljava/nio/ByteBuffer;->array()[B

    .line 81
    .line 82
    .line 83
    move-result-object v8

    .line 84
    invoke-virtual {v7, v8}, Ljava/util/zip/Adler32;->update([B)V

    .line 85
    .line 86
    .line 87
    iget-object v8, v1, Lts;->b:[B

    .line 88
    .line 89
    if-eqz v8, :cond_0

    .line 90
    .line 91
    invoke-virtual {v7, v8}, Ljava/util/zip/Adler32;->update([B)V

    .line 92
    .line 93
    .line 94
    :cond_0
    invoke-virtual {v7}, Ljava/util/zip/Adler32;->getValue()J

    .line 95
    .line 96
    .line 97
    move-result-wide v10

    .line 98
    long-to-int v7, v10

    .line 99
    const-string v10, "JobInfoScheduler"

    .line 100
    .line 101
    const-string v11, "attemptNumber"

    .line 102
    .line 103
    if-nez p3, :cond_2

    .line 104
    .line 105
    invoke-virtual {v6}, Landroid/app/job/JobScheduler;->getAllPendingJobs()Ljava/util/List;

    .line 106
    .line 107
    .line 108
    move-result-object v12

    .line 109
    invoke-interface {v12}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    .line 110
    .line 111
    .line 112
    move-result-object v12

    .line 113
    :cond_1
    invoke-interface {v12}, Ljava/util/Iterator;->hasNext()Z

    .line 114
    .line 115
    .line 116
    move-result v13

    .line 117
    if-eqz v13, :cond_2

    .line 118
    .line 119
    invoke-interface {v12}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 120
    .line 121
    .line 122
    move-result-object v13

    .line 123
    check-cast v13, Landroid/app/job/JobInfo;

    .line 124
    .line 125
    invoke-virtual {v13}, Landroid/app/job/JobInfo;->getExtras()Landroid/os/PersistableBundle;

    .line 126
    .line 127
    .line 128
    move-result-object v14

    .line 129
    invoke-virtual {v14, v11}, Landroid/os/BaseBundle;->getInt(Ljava/lang/String;)I

    .line 130
    .line 131
    .line 132
    move-result v14

    .line 133
    invoke-virtual {v13}, Landroid/app/job/JobInfo;->getId()I

    .line 134
    .line 135
    .line 136
    move-result v13

    .line 137
    if-ne v13, v7, :cond_1

    .line 138
    .line 139
    if-lt v14, v2, :cond_2

    .line 140
    .line 141
    const-string v0, "Upload for context %s is already scheduled. Returning..."

    .line 142
    .line 143
    invoke-static {v10, v0, v1}, Ly43;->t(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V

    .line 144
    .line 145
    .line 146
    return-void

    .line 147
    :cond_2
    iget-object v0, v0, Lfa1;->b:Ljava/lang/Object;

    .line 148
    .line 149
    check-cast v0, Lno4;

    .line 150
    .line 151
    invoke-virtual {v0}, Lno4;->d()Landroid/database/sqlite/SQLiteDatabase;

    .line 152
    .line 153
    .line 154
    move-result-object v0

    .line 155
    invoke-static {v9}, Ln84;->a(Lk84;)I

    .line 156
    .line 157
    .line 158
    move-result v12

    .line 159
    invoke-static {v12}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    .line 160
    .line 161
    .line 162
    move-result-object v12

    .line 163
    filled-new-array {v5, v12}, [Ljava/lang/String;

    .line 164
    .line 165
    .line 166
    move-result-object v12

    .line 167
    const-string v13, "SELECT next_request_ms FROM transport_contexts WHERE backend_name = ? and priority = ?"

    .line 168
    .line 169
    invoke-virtual {v0, v13, v12}, Landroid/database/sqlite/SQLiteDatabase;->rawQuery(Ljava/lang/String;[Ljava/lang/String;)Landroid/database/Cursor;

    .line 170
    .line 171
    .line 172
    move-result-object v12

    .line 173
    :try_start_0
    move-object v0, v12

    .line 174
    check-cast v0, Landroid/database/Cursor;

    .line 175
    .line 176
    invoke-interface {v0}, Landroid/database/Cursor;->moveToNext()Z

    .line 177
    .line 178
    .line 179
    move-result v13

    .line 180
    const/4 v14, 0x0

    .line 181
    if-eqz v13, :cond_3

    .line 182
    .line 183
    invoke-interface {v0, v14}, Landroid/database/Cursor;->getLong(I)J

    .line 184
    .line 185
    .line 186
    move-result-wide v15

    .line 187
    invoke-static/range {v15 .. v16}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    .line 188
    .line 189
    .line 190
    move-result-object v0

    .line 191
    goto :goto_0

    .line 192
    :cond_3
    const-wide/16 v15, 0x0

    .line 193
    .line 194
    invoke-static/range {v15 .. v16}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    .line 195
    .line 196
    .line 197
    move-result-object v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 198
    :goto_0
    invoke-interface {v12}, Landroid/database/Cursor;->close()V

    .line 199
    .line 200
    .line 201
    invoke-virtual {v0}, Ljava/lang/Long;->longValue()J

    .line 202
    .line 203
    .line 204
    move-result-wide v12

    .line 205
    new-instance v15, Landroid/app/job/JobInfo$Builder;

    .line 206
    .line 207
    invoke-direct {v15, v7, v4}, Landroid/app/job/JobInfo$Builder;-><init>(ILandroid/content/ComponentName;)V

    .line 208
    .line 209
    .line 210
    move-object v4, v6

    .line 211
    move/from16 v16, v7

    .line 212
    .line 213
    invoke-virtual {v3, v9, v12, v13, v2}, Lzr;->a(Lk84;JI)J

    .line 214
    .line 215
    .line 216
    move-result-wide v6

    .line 217
    invoke-virtual {v15, v6, v7}, Landroid/app/job/JobInfo$Builder;->setMinimumLatency(J)Landroid/app/job/JobInfo$Builder;

    .line 218
    .line 219
    .line 220
    iget-object v6, v3, Lzr;->b:Ljava/util/HashMap;

    .line 221
    .line 222
    invoke-virtual {v6, v9}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 223
    .line 224
    .line 225
    move-result-object v6

    .line 226
    check-cast v6, Las;

    .line 227
    .line 228
    iget-object v6, v6, Las;->c:Ljava/util/Set;

    .line 229
    .line 230
    sget-object v7, Lhq4;->NETWORK_UNMETERED:Lhq4;

    .line 231
    .line 232
    invoke-interface {v6, v7}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z

    .line 233
    .line 234
    .line 235
    move-result v7

    .line 236
    const/4 v14, 0x1

    .line 237
    if-eqz v7, :cond_4

    .line 238
    .line 239
    const/4 v7, 0x2

    .line 240
    invoke-virtual {v15, v7}, Landroid/app/job/JobInfo$Builder;->setRequiredNetworkType(I)Landroid/app/job/JobInfo$Builder;

    .line 241
    .line 242
    .line 243
    goto :goto_1

    .line 244
    :cond_4
    invoke-virtual {v15, v14}, Landroid/app/job/JobInfo$Builder;->setRequiredNetworkType(I)Landroid/app/job/JobInfo$Builder;

    .line 245
    .line 246
    .line 247
    :goto_1
    sget-object v7, Lhq4;->DEVICE_CHARGING:Lhq4;

    .line 248
    .line 249
    invoke-interface {v6, v7}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z

    .line 250
    .line 251
    .line 252
    move-result v7

    .line 253
    if-eqz v7, :cond_5

    .line 254
    .line 255
    invoke-virtual {v15, v14}, Landroid/app/job/JobInfo$Builder;->setRequiresCharging(Z)Landroid/app/job/JobInfo$Builder;

    .line 256
    .line 257
    .line 258
    :cond_5
    sget-object v7, Lhq4;->DEVICE_IDLE:Lhq4;

    .line 259
    .line 260
    invoke-interface {v6, v7}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z

    .line 261
    .line 262
    .line 263
    move-result v6

    .line 264
    if-eqz v6, :cond_6

    .line 265
    .line 266
    invoke-virtual {v15, v14}, Landroid/app/job/JobInfo$Builder;->setRequiresDeviceIdle(Z)Landroid/app/job/JobInfo$Builder;

    .line 267
    .line 268
    .line 269
    :cond_6
    new-instance v6, Landroid/os/PersistableBundle;

    .line 270
    .line 271
    invoke-direct {v6}, Landroid/os/PersistableBundle;-><init>()V

    .line 272
    .line 273
    .line 274
    invoke-virtual {v6, v11, v2}, Landroid/os/BaseBundle;->putInt(Ljava/lang/String;I)V

    .line 275
    .line 276
    .line 277
    const-string v7, "backendName"

    .line 278
    .line 279
    invoke-virtual {v6, v7, v5}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 280
    .line 281
    .line 282
    const-string v5, "priority"

    .line 283
    .line 284
    invoke-static {v9}, Ln84;->a(Lk84;)I

    .line 285
    .line 286
    .line 287
    move-result v7

    .line 288
    invoke-virtual {v6, v5, v7}, Landroid/os/BaseBundle;->putInt(Ljava/lang/String;I)V

    .line 289
    .line 290
    .line 291
    if-eqz v8, :cond_7

    .line 292
    .line 293
    const-string v5, "extras"

    .line 294
    .line 295
    const/4 v7, 0x0

    .line 296
    invoke-static {v8, v7}, Landroid/util/Base64;->encodeToString([BI)Ljava/lang/String;

    .line 297
    .line 298
    .line 299
    move-result-object v7

    .line 300
    invoke-virtual {v6, v5, v7}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 301
    .line 302
    .line 303
    :cond_7
    invoke-virtual {v15, v6}, Landroid/app/job/JobInfo$Builder;->setExtras(Landroid/os/PersistableBundle;)Landroid/app/job/JobInfo$Builder;

    .line 304
    .line 305
    .line 306
    invoke-static/range {v16 .. v16}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 307
    .line 308
    .line 309
    move-result-object v5

    .line 310
    invoke-virtual {v3, v9, v12, v13, v2}, Lzr;->a(Lk84;JI)J

    .line 311
    .line 312
    .line 313
    move-result-wide v6

    .line 314
    invoke-static {v6, v7}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    .line 315
    .line 316
    .line 317
    move-result-object v3

    .line 318
    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 319
    .line 320
    .line 321
    move-result-object v2

    .line 322
    filled-new-array {v1, v5, v3, v0, v2}, [Ljava/lang/Object;

    .line 323
    .line 324
    .line 325
    move-result-object v0

    .line 326
    invoke-static {v10}, Ly43;->B(Ljava/lang/String;)Ljava/lang/String;

    .line 327
    .line 328
    .line 329
    move-result-object v1

    .line 330
    const/4 v2, 0x3

    .line 331
    invoke-static {v1, v2}, Landroid/util/Log;->isLoggable(Ljava/lang/String;I)Z

    .line 332
    .line 333
    .line 334
    move-result v2

    .line 335
    if-eqz v2, :cond_8

    .line 336
    .line 337
    const-string v2, "Scheduling upload for context %s with jobId=%d in %dms(Backend next call timestamp %d). Attempt %d"

    .line 338
    .line 339
    invoke-static {v2, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    .line 340
    .line 341
    .line 342
    move-result-object v0

    .line 343
    invoke-static {v1, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 344
    .line 345
    .line 346
    :cond_8
    invoke-virtual {v15}, Landroid/app/job/JobInfo$Builder;->build()Landroid/app/job/JobInfo;

    .line 347
    .line 348
    .line 349
    move-result-object v0

    .line 350
    invoke-virtual {v4, v0}, Landroid/app/job/JobScheduler;->schedule(Landroid/app/job/JobInfo;)I

    .line 351
    .line 352
    .line 353
    return-void

    .line 354
    :catchall_0
    move-exception v0

    .line 355
    invoke-interface {v12}, Landroid/database/Cursor;->close()V

    .line 356
    .line 357
    .line 358
    throw v0
.end method

.method public I()[B
    .locals 4

    .line 1
    const/16 v0, 0xa

    .line 2
    .line 3
    new-array v1, v0, [J

    .line 4
    .line 5
    new-array v2, v0, [J

    .line 6
    .line 7
    new-array v0, v0, [J

    .line 8
    .line 9
    iget-object v3, p0, Lfa1;->d:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast v3, [J

    .line 12
    .line 13
    invoke-static {v1, v3}, Ltq3;->o([J[J)V

    .line 14
    .line 15
    .line 16
    iget-object v3, p0, Lfa1;->b:Ljava/lang/Object;

    .line 17
    .line 18
    check-cast v3, [J

    .line 19
    .line 20
    invoke-static {v2, v3, v1}, Ltq3;->s([J[J[J)V

    .line 21
    .line 22
    .line 23
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 24
    .line 25
    check-cast p0, [J

    .line 26
    .line 27
    invoke-static {v0, p0, v1}, Ltq3;->s([J[J[J)V

    .line 28
    .line 29
    .line 30
    invoke-static {v0}, Ltq3;->i([J)[B

    .line 31
    .line 32
    .line 33
    move-result-object p0

    .line 34
    const/16 v0, 0x1f

    .line 35
    .line 36
    aget-byte v1, p0, v0

    .line 37
    .line 38
    invoke-static {v2}, Ltq3;->i([J)[B

    .line 39
    .line 40
    .line 41
    move-result-object v2

    .line 42
    const/4 v3, 0x0

    .line 43
    aget-byte v2, v2, v3

    .line 44
    .line 45
    and-int/lit8 v2, v2, 0x1

    .line 46
    .line 47
    shl-int/lit8 v2, v2, 0x7

    .line 48
    .line 49
    xor-int/2addr v1, v2

    .line 50
    int-to-byte v1, v1

    .line 51
    aput-byte v1, p0, v0

    .line 52
    .line 53
    return-object p0
.end method

.method public a(Lcom/indymobile/app/util/PSException;)V
    .locals 1

    .line 1
    iget-object p1, p0, Lfa1;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p1, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;

    .line 4
    .line 5
    sget v0, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;->n0:I

    .line 6
    .line 7
    invoke-virtual {p1}, Lmu;->s()V

    .line 8
    .line 9
    .line 10
    iget-object p1, p1, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;->W:Lcom/indymobile/app/activity/editor/markup/MarkupView;

    .line 11
    .line 12
    iget-object p0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 13
    .line 14
    check-cast p0, Lcom/indymobile/app/model/PSPage;

    .line 15
    .line 16
    invoke-virtual {p1, p0}, Lcom/indymobile/app/activity/editor/markup/MarkupView;->K(Lcom/indymobile/app/model/PSPage;)V

    .line 17
    .line 18
    .line 19
    return-void
.end method

.method public b()Ljava/lang/Object;
    .locals 0

    .line 1
    const/4 p0, 0x0

    .line 2
    return-object p0
.end method

.method public c()Lorg/opencv/core/Mat;
    .locals 9

    .line 1
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/media/Image;

    .line 4
    .line 5
    invoke-virtual {v0}, Landroid/media/Image;->getPlanes()[Landroid/media/Image$Plane;

    .line 6
    .line 7
    .line 8
    move-result-object v1

    .line 9
    invoke-virtual {v0}, Landroid/media/Image;->getWidth()I

    .line 10
    .line 11
    .line 12
    move-result v4

    .line 13
    invoke-virtual {v0}, Landroid/media/Image;->getHeight()I

    .line 14
    .line 15
    .line 16
    move-result v3

    .line 17
    const/4 v0, 0x0

    .line 18
    aget-object v2, v1, v0

    .line 19
    .line 20
    invoke-virtual {v2}, Landroid/media/Image$Plane;->getBuffer()Ljava/nio/ByteBuffer;

    .line 21
    .line 22
    .line 23
    move-result-object v6

    .line 24
    aget-object v0, v1, v0

    .line 25
    .line 26
    invoke-virtual {v0}, Landroid/media/Image$Plane;->getRowStride()I

    .line 27
    .line 28
    .line 29
    move-result v0

    .line 30
    new-instance v2, Lorg/opencv/core/Mat;

    .line 31
    .line 32
    sget v5, Lez0;->a:I

    .line 33
    .line 34
    int-to-long v7, v0

    .line 35
    invoke-direct/range {v2 .. v8}, Lorg/opencv/core/Mat;-><init>(IIILjava/nio/ByteBuffer;J)V

    .line 36
    .line 37
    .line 38
    iput-object v2, p0, Lfa1;->d:Ljava/lang/Object;

    .line 39
    .line 40
    return-object v2
.end method

.method public d()Lorg/opencv/core/Mat;
    .locals 23

    .line 1
    move-object/from16 v0, p0

    .line 2
    .line 3
    iget-object v1, v0, Lfa1;->c:Ljava/lang/Object;

    .line 4
    .line 5
    check-cast v1, Lorg/opencv/core/Mat;

    .line 6
    .line 7
    iget-object v0, v0, Lfa1;->b:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast v0, Landroid/media/Image;

    .line 10
    .line 11
    invoke-virtual {v0}, Landroid/media/Image;->getPlanes()[Landroid/media/Image$Plane;

    .line 12
    .line 13
    .line 14
    move-result-object v2

    .line 15
    invoke-virtual {v0}, Landroid/media/Image;->getWidth()I

    .line 16
    .line 17
    .line 18
    move-result v5

    .line 19
    invoke-virtual {v0}, Landroid/media/Image;->getHeight()I

    .line 20
    .line 21
    .line 22
    move-result v4

    .line 23
    const/4 v0, 0x1

    .line 24
    aget-object v3, v2, v0

    .line 25
    .line 26
    invoke-virtual {v3}, Landroid/media/Image$Plane;->getPixelStride()I

    .line 27
    .line 28
    .line 29
    move-result v3

    .line 30
    const/4 v6, 0x0

    .line 31
    const/4 v10, 0x2

    .line 32
    if-ne v3, v10, :cond_1

    .line 33
    .line 34
    aget-object v3, v2, v6

    .line 35
    .line 36
    invoke-virtual {v3}, Landroid/media/Image$Plane;->getBuffer()Ljava/nio/ByteBuffer;

    .line 37
    .line 38
    .line 39
    move-result-object v7

    .line 40
    aget-object v3, v2, v6

    .line 41
    .line 42
    invoke-virtual {v3}, Landroid/media/Image$Plane;->getRowStride()I

    .line 43
    .line 44
    .line 45
    move-result v3

    .line 46
    aget-object v6, v2, v0

    .line 47
    .line 48
    invoke-virtual {v6}, Landroid/media/Image$Plane;->getBuffer()Ljava/nio/ByteBuffer;

    .line 49
    .line 50
    .line 51
    move-result-object v15

    .line 52
    aget-object v0, v2, v0

    .line 53
    .line 54
    invoke-virtual {v0}, Landroid/media/Image$Plane;->getRowStride()I

    .line 55
    .line 56
    .line 57
    move-result v0

    .line 58
    aget-object v6, v2, v10

    .line 59
    .line 60
    invoke-virtual {v6}, Landroid/media/Image$Plane;->getBuffer()Ljava/nio/ByteBuffer;

    .line 61
    .line 62
    .line 63
    move-result-object v20

    .line 64
    aget-object v2, v2, v10

    .line 65
    .line 66
    invoke-virtual {v2}, Landroid/media/Image$Plane;->getRowStride()I

    .line 67
    .line 68
    .line 69
    move-result v2

    .line 70
    new-instance v6, Lorg/opencv/core/Mat;

    .line 71
    .line 72
    move-object v8, v6

    .line 73
    sget v6, Lez0;->a:I

    .line 74
    .line 75
    int-to-long v11, v3

    .line 76
    move-object v3, v8

    .line 77
    move-wide v8, v11

    .line 78
    invoke-direct/range {v3 .. v9}, Lorg/opencv/core/Mat;-><init>(IIILjava/nio/ByteBuffer;J)V

    .line 79
    .line 80
    .line 81
    new-instance v11, Lorg/opencv/core/Mat;

    .line 82
    .line 83
    div-int/lit8 v17, v4, 0x2

    .line 84
    .line 85
    div-int/lit8 v18, v5, 0x2

    .line 86
    .line 87
    sget v19, Lez0;->b:I

    .line 88
    .line 89
    int-to-long v4, v0

    .line 90
    move/from16 v12, v17

    .line 91
    .line 92
    move/from16 v13, v18

    .line 93
    .line 94
    move/from16 v14, v19

    .line 95
    .line 96
    move-wide/from16 v16, v4

    .line 97
    .line 98
    invoke-direct/range {v11 .. v17}, Lorg/opencv/core/Mat;-><init>(IIILjava/nio/ByteBuffer;J)V

    .line 99
    .line 100
    .line 101
    move/from16 v17, v12

    .line 102
    .line 103
    new-instance v16, Lorg/opencv/core/Mat;

    .line 104
    .line 105
    int-to-long v4, v2

    .line 106
    move-wide/from16 v21, v4

    .line 107
    .line 108
    invoke-direct/range {v16 .. v22}, Lorg/opencv/core/Mat;-><init>(IIILjava/nio/ByteBuffer;J)V

    .line 109
    .line 110
    .line 111
    move-object/from16 v0, v16

    .line 112
    .line 113
    invoke-virtual {v0}, Lorg/opencv/core/Mat;->b()J

    .line 114
    .line 115
    .line 116
    move-result-wide v4

    .line 117
    invoke-virtual {v11}, Lorg/opencv/core/Mat;->b()J

    .line 118
    .line 119
    .line 120
    move-result-wide v6

    .line 121
    sub-long/2addr v4, v6

    .line 122
    const-wide/16 v6, 0x0

    .line 123
    .line 124
    cmp-long v2, v4, v6

    .line 125
    .line 126
    if-lez v2, :cond_0

    .line 127
    .line 128
    const/16 v0, 0x5e

    .line 129
    .line 130
    invoke-static {v3, v11, v1, v0}, Lorg/opencv/imgproc/Imgproc;->c(Lorg/opencv/core/Mat;Lorg/opencv/core/Mat;Lorg/opencv/core/Mat;I)V

    .line 131
    .line 132
    .line 133
    return-object v1

    .line 134
    :cond_0
    const/16 v2, 0x60

    .line 135
    .line 136
    invoke-static {v3, v0, v1, v2}, Lorg/opencv/imgproc/Imgproc;->c(Lorg/opencv/core/Mat;Lorg/opencv/core/Mat;Lorg/opencv/core/Mat;I)V

    .line 137
    .line 138
    .line 139
    return-object v1

    .line 140
    :cond_1
    div-int/lit8 v3, v4, 0x2

    .line 141
    .line 142
    add-int v7, v4, v3

    .line 143
    .line 144
    mul-int v8, v5, v7

    .line 145
    .line 146
    new-array v8, v8, [B

    .line 147
    .line 148
    aget-object v9, v2, v6

    .line 149
    .line 150
    invoke-virtual {v9}, Landroid/media/Image$Plane;->getBuffer()Ljava/nio/ByteBuffer;

    .line 151
    .line 152
    .line 153
    move-result-object v9

    .line 154
    aget-object v11, v2, v0

    .line 155
    .line 156
    invoke-virtual {v11}, Landroid/media/Image$Plane;->getBuffer()Ljava/nio/ByteBuffer;

    .line 157
    .line 158
    .line 159
    move-result-object v11

    .line 160
    aget-object v10, v2, v10

    .line 161
    .line 162
    invoke-virtual {v10}, Landroid/media/Image$Plane;->getBuffer()Ljava/nio/ByteBuffer;

    .line 163
    .line 164
    .line 165
    move-result-object v10

    .line 166
    aget-object v12, v2, v6

    .line 167
    .line 168
    invoke-virtual {v12}, Landroid/media/Image$Plane;->getRowStride()I

    .line 169
    .line 170
    .line 171
    move-result v12

    .line 172
    if-ne v12, v5, :cond_2

    .line 173
    .line 174
    mul-int v12, v5, v4

    .line 175
    .line 176
    invoke-virtual {v9, v8, v6, v12}, Ljava/nio/ByteBuffer;->get([BII)Ljava/nio/ByteBuffer;

    .line 177
    .line 178
    .line 179
    goto :goto_1

    .line 180
    :cond_2
    sub-int/2addr v12, v5

    .line 181
    move v13, v6

    .line 182
    move v14, v13

    .line 183
    :goto_0
    if-ge v14, v4, :cond_4

    .line 184
    .line 185
    invoke-virtual {v9, v8, v13, v5}, Ljava/nio/ByteBuffer;->get([BII)Ljava/nio/ByteBuffer;

    .line 186
    .line 187
    .line 188
    add-int/2addr v13, v5

    .line 189
    add-int/lit8 v15, v4, -0x1

    .line 190
    .line 191
    if-ge v14, v15, :cond_3

    .line 192
    .line 193
    invoke-virtual {v9}, Ljava/nio/Buffer;->position()I

    .line 194
    .line 195
    .line 196
    move-result v15

    .line 197
    add-int/2addr v15, v12

    .line 198
    invoke-virtual {v9, v15}, Ljava/nio/ByteBuffer;->position(I)Ljava/nio/Buffer;

    .line 199
    .line 200
    .line 201
    :cond_3
    add-int/lit8 v14, v14, 0x1

    .line 202
    .line 203
    goto :goto_0

    .line 204
    :cond_4
    move v12, v13

    .line 205
    :goto_1
    aget-object v0, v2, v0

    .line 206
    .line 207
    invoke-virtual {v0}, Landroid/media/Image$Plane;->getRowStride()I

    .line 208
    .line 209
    .line 210
    move-result v0

    .line 211
    div-int/lit8 v2, v5, 0x2

    .line 212
    .line 213
    sub-int/2addr v0, v2

    .line 214
    if-nez v0, :cond_5

    .line 215
    .line 216
    mul-int v0, v5, v4

    .line 217
    .line 218
    div-int/lit8 v0, v0, 0x4

    .line 219
    .line 220
    invoke-virtual {v11, v8, v12, v0}, Ljava/nio/ByteBuffer;->get([BII)Ljava/nio/ByteBuffer;

    .line 221
    .line 222
    .line 223
    add-int/2addr v12, v0

    .line 224
    invoke-virtual {v10, v8, v12, v0}, Ljava/nio/ByteBuffer;->get([BII)Ljava/nio/ByteBuffer;

    .line 225
    .line 226
    .line 227
    goto :goto_4

    .line 228
    :cond_5
    move v4, v6

    .line 229
    :goto_2
    if-ge v4, v3, :cond_7

    .line 230
    .line 231
    invoke-virtual {v11, v8, v12, v2}, Ljava/nio/ByteBuffer;->get([BII)Ljava/nio/ByteBuffer;

    .line 232
    .line 233
    .line 234
    add-int/2addr v12, v2

    .line 235
    add-int/lit8 v9, v3, -0x1

    .line 236
    .line 237
    if-ge v4, v9, :cond_6

    .line 238
    .line 239
    invoke-virtual {v11}, Ljava/nio/Buffer;->position()I

    .line 240
    .line 241
    .line 242
    move-result v9

    .line 243
    add-int/2addr v9, v0

    .line 244
    invoke-virtual {v11, v9}, Ljava/nio/ByteBuffer;->position(I)Ljava/nio/Buffer;

    .line 245
    .line 246
    .line 247
    :cond_6
    add-int/lit8 v4, v4, 0x1

    .line 248
    .line 249
    goto :goto_2

    .line 250
    :cond_7
    :goto_3
    if-ge v6, v3, :cond_9

    .line 251
    .line 252
    invoke-virtual {v10, v8, v12, v2}, Ljava/nio/ByteBuffer;->get([BII)Ljava/nio/ByteBuffer;

    .line 253
    .line 254
    .line 255
    add-int/2addr v12, v2

    .line 256
    add-int/lit8 v4, v3, -0x1

    .line 257
    .line 258
    if-ge v6, v4, :cond_8

    .line 259
    .line 260
    invoke-virtual {v10}, Ljava/nio/Buffer;->position()I

    .line 261
    .line 262
    .line 263
    move-result v4

    .line 264
    add-int/2addr v4, v0

    .line 265
    invoke-virtual {v10, v4}, Ljava/nio/ByteBuffer;->position(I)Ljava/nio/Buffer;

    .line 266
    .line 267
    .line 268
    :cond_8
    add-int/lit8 v6, v6, 0x1

    .line 269
    .line 270
    goto :goto_3

    .line 271
    :cond_9
    :goto_4
    new-instance v0, Lorg/opencv/core/Mat;

    .line 272
    .line 273
    sget v2, Lez0;->a:I

    .line 274
    .line 275
    invoke-direct {v0, v7, v5, v2}, Lorg/opencv/core/Mat;-><init>(III)V

    .line 276
    .line 277
    .line 278
    invoke-virtual {v0, v8}, Lorg/opencv/core/Mat;->c([B)V

    .line 279
    .line 280
    .line 281
    const/16 v2, 0x68

    .line 282
    .line 283
    invoke-static {v0, v1, v2}, Lorg/opencv/imgproc/Imgproc;->b(Lorg/opencv/core/Mat;Lorg/opencv/core/Mat;I)V

    .line 284
    .line 285
    .line 286
    return-object v1
.end method

.method public e()Landroid/net/Uri;
    .locals 0

    .line 1
    iget-object p0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/net/Uri;

    .line 4
    .line 5
    return-object p0
.end method

.method public f()V
    .locals 0

    .line 1
    return-void
.end method

.method public g()Landroid/net/Uri;
    .locals 0

    .line 1
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/net/Uri;

    .line 4
    .line 5
    return-object p0
.end method

.method public getDescription()Landroid/content/ClipDescription;
    .locals 0

    .line 1
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/content/ClipDescription;

    .line 4
    .line 5
    return-object p0
.end method

.method public getHeight()I
    .locals 2

    .line 1
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;

    .line 4
    .line 5
    iget v0, v0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;->f0:I

    .line 6
    .line 7
    const/4 v1, -0x1

    .line 8
    if-ne v0, v1, :cond_0

    .line 9
    .line 10
    iget-object p0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 11
    .line 12
    check-cast p0, Lv07;

    .line 13
    .line 14
    invoke-virtual {p0}, Lv07;->getHeight()I

    .line 15
    .line 16
    .line 17
    move-result p0

    .line 18
    return p0

    .line 19
    :cond_0
    if-eqz v0, :cond_2

    .line 20
    .line 21
    const/4 v1, -0x2

    .line 22
    if-ne v0, v1, :cond_1

    .line 23
    .line 24
    goto :goto_0

    .line 25
    :cond_1
    return v0

    .line 26
    :cond_2
    :goto_0
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 27
    .line 28
    check-cast p0, Leb1;

    .line 29
    .line 30
    iget-object p0, p0, Leb1;->b:Ljava/lang/Object;

    .line 31
    .line 32
    check-cast p0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;

    .line 33
    .line 34
    invoke-virtual {p0}, Landroid/view/View;->getMeasuredHeight()I

    .line 35
    .line 36
    .line 37
    move-result p0

    .line 38
    return p0
.end method

.method public getPaddingEnd()I
    .locals 0

    .line 1
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;

    .line 4
    .line 5
    iget p0, p0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;->V:I

    .line 6
    .line 7
    return p0
.end method

.method public getPaddingStart()I
    .locals 0

    .line 1
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;

    .line 4
    .line 5
    iget p0, p0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;->U:I

    .line 6
    .line 7
    return p0
.end method

.method public getWidth()I
    .locals 2

    .line 1
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;

    .line 4
    .line 5
    iget v0, v0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;->e0:I

    .line 6
    .line 7
    const/4 v1, -0x1

    .line 8
    if-ne v0, v1, :cond_0

    .line 9
    .line 10
    iget-object p0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 11
    .line 12
    check-cast p0, Lv07;

    .line 13
    .line 14
    invoke-virtual {p0}, Lv07;->getWidth()I

    .line 15
    .line 16
    .line 17
    move-result p0

    .line 18
    return p0

    .line 19
    :cond_0
    if-eqz v0, :cond_2

    .line 20
    .line 21
    const/4 v1, -0x2

    .line 22
    if-ne v0, v1, :cond_1

    .line 23
    .line 24
    goto :goto_0

    .line 25
    :cond_1
    return v0

    .line 26
    :cond_2
    :goto_0
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 27
    .line 28
    check-cast p0, Leb1;

    .line 29
    .line 30
    invoke-virtual {p0}, Leb1;->getWidth()I

    .line 31
    .line 32
    .line 33
    move-result p0

    .line 34
    return p0
.end method

.method public h(Lt30;)Ljava/lang/Object;
    .locals 3

    .line 1
    new-instance v0, Ldb;

    .line 2
    .line 3
    const/16 v1, 0x19

    .line 4
    .line 5
    invoke-direct {v0, v1, p0}, Ldb;-><init>(ILjava/lang/Object;)V

    .line 6
    .line 7
    .line 8
    invoke-static {}, Lpw0;->i()Lz91;

    .line 9
    .line 10
    .line 11
    move-result-object v1

    .line 12
    iget-object v2, p1, Lt30;->c:Lqj4;

    .line 13
    .line 14
    if-eqz v2, :cond_0

    .line 15
    .line 16
    invoke-virtual {v2, v0, v1}, Lt2;->a(Ljava/lang/Runnable;Ljava/util/concurrent/Executor;)V

    .line 17
    .line 18
    .line 19
    :cond_0
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 20
    .line 21
    check-cast v0, Ls82;

    .line 22
    .line 23
    iget-object v0, v0, Ls82;->a:Ljava/util/concurrent/atomic/AtomicReference;

    .line 24
    .line 25
    invoke-virtual {v0, p1}, Ljava/util/concurrent/atomic/AtomicReference;->set(Ljava/lang/Object;)V

    .line 26
    .line 27
    .line 28
    new-instance p1, Ljava/lang/StringBuilder;

    .line 29
    .line 30
    const-string v0, "HandlerScheduledFuture-"

    .line 31
    .line 32
    invoke-direct {p1, v0}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 33
    .line 34
    .line 35
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 36
    .line 37
    check-cast p0, Ljava/util/concurrent/Callable;

    .line 38
    .line 39
    invoke-virtual {p0}, Ljava/lang/Object;->toString()Ljava/lang/String;

    .line 40
    .line 41
    .line 42
    move-result-object p0

    .line 43
    invoke-virtual {p1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 44
    .line 45
    .line 46
    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 47
    .line 48
    .line 49
    move-result-object p0

    .line 50
    return-object p0
.end method

.method public i()V
    .locals 2

    .line 1
    iget v0, p0, Lfa1;->a:I

    .line 2
    .line 3
    const/4 v1, 0x0

    .line 4
    sparse-switch v0, :sswitch_data_0

    .line 5
    .line 6
    .line 7
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast v0, Law3;

    .line 10
    .line 11
    iput-boolean v1, v0, Law3;->b:Z

    .line 12
    .line 13
    iget-object v1, p0, Lfa1;->b:Ljava/lang/Object;

    .line 14
    .line 15
    check-cast v1, Landroidx/fragment/app/o;

    .line 16
    .line 17
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 18
    .line 19
    check-cast p0, Lwt3;

    .line 20
    .line 21
    invoke-virtual {v0, v1, p0}, Law3;->B(Landroidx/fragment/app/o;Lwt3;)V

    .line 22
    .line 23
    .line 24
    return-void

    .line 25
    :sswitch_0
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 26
    .line 27
    check-cast v0, Lku3;

    .line 28
    .line 29
    iput-boolean v1, v0, Lku3;->b:Z

    .line 30
    .line 31
    iget-object v1, p0, Lfa1;->b:Ljava/lang/Object;

    .line 32
    .line 33
    check-cast v1, Landroidx/fragment/app/o;

    .line 34
    .line 35
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 36
    .line 37
    check-cast p0, Lwt3;

    .line 38
    .line 39
    invoke-virtual {v0, v1, p0}, Lku3;->B(Landroidx/fragment/app/o;Lwt3;)V

    .line 40
    .line 41
    .line 42
    return-void

    .line 43
    :sswitch_1
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 44
    .line 45
    check-cast v0, Lot3;

    .line 46
    .line 47
    iput-boolean v1, v0, Lot3;->b:Z

    .line 48
    .line 49
    iget-object v1, p0, Lfa1;->b:Ljava/lang/Object;

    .line 50
    .line 51
    check-cast v1, Landroidx/fragment/app/o;

    .line 52
    .line 53
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 54
    .line 55
    check-cast p0, Lwt3;

    .line 56
    .line 57
    invoke-virtual {v0, v1, p0}, Lot3;->B(Landroidx/fragment/app/o;Lwt3;)V

    .line 58
    .line 59
    .line 60
    return-void

    .line 61
    :sswitch_data_0
    .sparse-switch
        0x18 -> :sswitch_1
        0x1b -> :sswitch_0
    .end sparse-switch
.end method

.method public j(Ljava/lang/String;Z)V
    .locals 0

    .line 1
    invoke-static {p2}, Ljava/lang/String;->valueOf(Z)Ljava/lang/String;

    .line 2
    .line 3
    .line 4
    move-result-object p2

    .line 5
    invoke-virtual {p0, p2, p1}, Lfa1;->k(Ljava/lang/Object;Ljava/lang/String;)V

    .line 6
    .line 7
    .line 8
    return-void
.end method

.method public k(Ljava/lang/Object;Ljava/lang/String;)V
    .locals 2

    .line 1
    new-instance v0, Lfa1;

    .line 2
    .line 3
    const/16 v1, 0x15

    .line 4
    .line 5
    invoke-direct {v0, v1}, Lfa1;-><init>(I)V

    .line 6
    .line 7
    .line 8
    iget-object v1, p0, Lfa1;->d:Ljava/lang/Object;

    .line 9
    .line 10
    check-cast v1, Lfa1;

    .line 11
    .line 12
    iput-object v0, v1, Lfa1;->d:Ljava/lang/Object;

    .line 13
    .line 14
    iput-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 15
    .line 16
    iput-object p1, v0, Lfa1;->c:Ljava/lang/Object;

    .line 17
    .line 18
    iput-object p2, v0, Lfa1;->b:Ljava/lang/Object;

    .line 19
    .line 20
    return-void
.end method

.method public bridge synthetic l(Ljava/lang/Object;)V
    .locals 0

    .line 1
    check-cast p1, Ljava/lang/Void;

    .line 2
    .line 3
    return-void
.end method

.method public n()Lgh1;
    .locals 4

    .line 1
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lug1;

    .line 4
    .line 5
    const/4 v1, 0x0

    .line 6
    if-eqz v0, :cond_9

    .line 7
    .line 8
    iget-object v2, p0, Lfa1;->c:Ljava/lang/Object;

    .line 9
    .line 10
    check-cast v2, Ljava/security/spec/ECPoint;

    .line 11
    .line 12
    if-eqz v2, :cond_8

    .line 13
    .line 14
    iget-object v0, v0, Lug1;->b:Ltg1;

    .line 15
    .line 16
    iget-object v0, v0, Ltg1;->b:Ljava/security/spec/ECParameterSpec;

    .line 17
    .line 18
    invoke-virtual {v0}, Ljava/security/spec/ECParameterSpec;->getCurve()Ljava/security/spec/EllipticCurve;

    .line 19
    .line 20
    .line 21
    move-result-object v0

    .line 22
    invoke-static {v2, v0}, Lbj1;->b(Ljava/security/spec/ECPoint;Ljava/security/spec/EllipticCurve;)V

    .line 23
    .line 24
    .line 25
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 26
    .line 27
    check-cast v0, Lug1;

    .line 28
    .line 29
    invoke-virtual {v0}, Lug1;->a()Z

    .line 30
    .line 31
    .line 32
    move-result v0

    .line 33
    if-eqz v0, :cond_1

    .line 34
    .line 35
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 36
    .line 37
    check-cast v0, Ljava/lang/Integer;

    .line 38
    .line 39
    if-eqz v0, :cond_0

    .line 40
    .line 41
    goto :goto_0

    .line 42
    :cond_0
    const-string p0, "Cannot create key without ID requirement with parameters with ID requirement"

    .line 43
    .line 44
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 45
    .line 46
    .line 47
    return-object v1

    .line 48
    :cond_1
    :goto_0
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 49
    .line 50
    check-cast v0, Lug1;

    .line 51
    .line 52
    invoke-virtual {v0}, Lug1;->a()Z

    .line 53
    .line 54
    .line 55
    move-result v0

    .line 56
    if-nez v0, :cond_3

    .line 57
    .line 58
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 59
    .line 60
    check-cast v0, Ljava/lang/Integer;

    .line 61
    .line 62
    if-nez v0, :cond_2

    .line 63
    .line 64
    goto :goto_1

    .line 65
    :cond_2
    const-string p0, "Cannot create key with ID requirement with parameters without ID requirement"

    .line 66
    .line 67
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 68
    .line 69
    .line 70
    return-object v1

    .line 71
    :cond_3
    :goto_1
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 72
    .line 73
    check-cast v0, Lug1;

    .line 74
    .line 75
    iget-object v0, v0, Lug1;->d:Lk8;

    .line 76
    .line 77
    sget-object v2, Lk8;->l:Lk8;

    .line 78
    .line 79
    if-ne v0, v2, :cond_4

    .line 80
    .line 81
    sget-object v0, Lfs3;->a:Lg20;

    .line 82
    .line 83
    goto :goto_3

    .line 84
    :cond_4
    sget-object v2, Lk8;->k:Lk8;

    .line 85
    .line 86
    if-eq v0, v2, :cond_7

    .line 87
    .line 88
    sget-object v2, Lk8;->j:Lk8;

    .line 89
    .line 90
    if-ne v0, v2, :cond_5

    .line 91
    .line 92
    goto :goto_2

    .line 93
    :cond_5
    sget-object v2, Lk8;->i:Lk8;

    .line 94
    .line 95
    if-ne v0, v2, :cond_6

    .line 96
    .line 97
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 98
    .line 99
    check-cast v0, Ljava/lang/Integer;

    .line 100
    .line 101
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I

    .line 102
    .line 103
    .line 104
    move-result v0

    .line 105
    invoke-static {v0}, Lfs3;->b(I)Lg20;

    .line 106
    .line 107
    .line 108
    move-result-object v0

    .line 109
    goto :goto_3

    .line 110
    :cond_6
    iget-object p0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 111
    .line 112
    check-cast p0, Lug1;

    .line 113
    .line 114
    iget-object p0, p0, Lug1;->d:Lk8;

    .line 115
    .line 116
    const-string v0, "Unknown EcdsaParameters.Variant: "

    .line 117
    .line 118
    invoke-static {p0, v0}, Lyb4;->j(Ljava/lang/Object;Ljava/lang/String;)V

    .line 119
    .line 120
    .line 121
    return-object v1

    .line 122
    :cond_7
    :goto_2
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 123
    .line 124
    check-cast v0, Ljava/lang/Integer;

    .line 125
    .line 126
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I

    .line 127
    .line 128
    .line 129
    move-result v0

    .line 130
    invoke-static {v0}, Lfs3;->a(I)Lg20;

    .line 131
    .line 132
    .line 133
    move-result-object v0

    .line 134
    :goto_3
    new-instance v1, Lgh1;

    .line 135
    .line 136
    iget-object v2, p0, Lfa1;->b:Ljava/lang/Object;

    .line 137
    .line 138
    check-cast v2, Lug1;

    .line 139
    .line 140
    iget-object v3, p0, Lfa1;->c:Ljava/lang/Object;

    .line 141
    .line 142
    check-cast v3, Ljava/security/spec/ECPoint;

    .line 143
    .line 144
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 145
    .line 146
    check-cast p0, Ljava/lang/Integer;

    .line 147
    .line 148
    invoke-direct {v1, v2, v3, v0, p0}, Lgh1;-><init>(Lug1;Ljava/security/spec/ECPoint;Lg20;Ljava/lang/Integer;)V

    .line 149
    .line 150
    .line 151
    return-object v1

    .line 152
    :cond_8
    const-string p0, "Cannot build without public point"

    .line 153
    .line 154
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 155
    .line 156
    .line 157
    return-object v1

    .line 158
    :cond_9
    const-string p0, "Cannot build without parameters"

    .line 159
    .line 160
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 161
    .line 162
    .line 163
    return-object v1
.end method

.method public o()Landroid/view/ViewGroup$LayoutParams;
    .locals 3

    .line 1
    new-instance v0, Landroid/view/ViewGroup$LayoutParams;

    .line 2
    .line 3
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 4
    .line 5
    check-cast p0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;

    .line 6
    .line 7
    iget v1, p0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;->e0:I

    .line 8
    .line 9
    const/4 v2, -0x2

    .line 10
    if-nez v1, :cond_0

    .line 11
    .line 12
    move v1, v2

    .line 13
    :cond_0
    iget p0, p0, Lcom/google/android/material/floatingactionbutton/ExtendedFloatingActionButton;->f0:I

    .line 14
    .line 15
    if-nez p0, :cond_1

    .line 16
    .line 17
    goto :goto_0

    .line 18
    :cond_1
    move v2, p0

    .line 19
    :goto_0
    invoke-direct {v0, v1, v2}, Landroid/view/ViewGroup$LayoutParams;-><init>(II)V

    .line 20
    .line 21
    .line 22
    return-object v0
.end method

.method public onFailure(Ljava/lang/Exception;)V
    .locals 1

    .line 1
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/graphics/Bitmap;

    .line 4
    .line 5
    invoke-static {v0}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 6
    .line 7
    .line 8
    :try_start_0
    iget-object v0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 9
    .line 10
    check-cast v0, Lcom/google/mlkit/vision/text/internal/zzn;

    .line 11
    .line 12
    invoke-interface {v0}, Lcf5;->close()V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    .line 13
    .line 14
    .line 15
    goto :goto_0

    .line 16
    :catch_0
    move-exception v0

    .line 17
    invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V

    .line 18
    .line 19
    .line 20
    :goto_0
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 21
    .line 22
    check-cast p0, Lio3;

    .line 23
    .line 24
    invoke-virtual {p0, p1}, Lio3;->j(Ljava/lang/Throwable;)V

    .line 25
    .line 26
    .line 27
    return-void
.end method

.method public onSuccess()V
    .locals 2

    .line 1
    iget v0, p0, Lfa1;->a:I

    .line 2
    .line 3
    const/4 v1, 0x0

    .line 4
    sparse-switch v0, :sswitch_data_0

    .line 5
    .line 6
    .line 7
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast v0, Law3;

    .line 10
    .line 11
    iput-boolean v1, v0, Law3;->b:Z

    .line 12
    .line 13
    iget-object v1, p0, Lfa1;->b:Ljava/lang/Object;

    .line 14
    .line 15
    check-cast v1, Landroidx/fragment/app/o;

    .line 16
    .line 17
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 18
    .line 19
    check-cast p0, Lwt3;

    .line 20
    .line 21
    invoke-virtual {v0, v1, p0}, Law3;->B(Landroidx/fragment/app/o;Lwt3;)V

    .line 22
    .line 23
    .line 24
    return-void

    .line 25
    :sswitch_0
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 26
    .line 27
    check-cast v0, Lku3;

    .line 28
    .line 29
    iput-boolean v1, v0, Lku3;->b:Z

    .line 30
    .line 31
    iget-object v1, p0, Lfa1;->b:Ljava/lang/Object;

    .line 32
    .line 33
    check-cast v1, Landroidx/fragment/app/o;

    .line 34
    .line 35
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 36
    .line 37
    check-cast p0, Lwt3;

    .line 38
    .line 39
    invoke-virtual {v0, v1, p0}, Lku3;->B(Landroidx/fragment/app/o;Lwt3;)V

    .line 40
    .line 41
    .line 42
    return-void

    .line 43
    :sswitch_1
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 44
    .line 45
    check-cast v0, Lot3;

    .line 46
    .line 47
    iput-boolean v1, v0, Lot3;->b:Z

    .line 48
    .line 49
    iget-object v1, p0, Lfa1;->b:Ljava/lang/Object;

    .line 50
    .line 51
    check-cast v1, Landroidx/fragment/app/o;

    .line 52
    .line 53
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 54
    .line 55
    check-cast p0, Lwt3;

    .line 56
    .line 57
    invoke-virtual {v0, v1, p0}, Lot3;->B(Landroidx/fragment/app/o;Lwt3;)V

    .line 58
    .line 59
    .line 60
    return-void

    .line 61
    :sswitch_data_0
    .sparse-switch
        0x18 -> :sswitch_1
        0x1b -> :sswitch_0
    .end sparse-switch
.end method

.method public p()Lda2;
    .locals 4

    .line 1
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lka2;

    .line 4
    .line 5
    const/4 v1, 0x0

    .line 6
    if-eqz v0, :cond_9

    .line 7
    .line 8
    iget-object v2, p0, Lfa1;->c:Ljava/lang/Object;

    .line 9
    .line 10
    check-cast v2, Ltt3;

    .line 11
    .line 12
    if-eqz v2, :cond_9

    .line 13
    .line 14
    iget v3, v0, Lka2;->a:I

    .line 15
    .line 16
    iget-object v2, v2, Ltt3;->b:Ljava/lang/Object;

    .line 17
    .line 18
    check-cast v2, Lg20;

    .line 19
    .line 20
    iget-object v2, v2, Lg20;->a:[B

    .line 21
    .line 22
    array-length v2, v2

    .line 23
    if-ne v3, v2, :cond_8

    .line 24
    .line 25
    invoke-virtual {v0}, Lka2;->a()Z

    .line 26
    .line 27
    .line 28
    move-result v0

    .line 29
    if-eqz v0, :cond_1

    .line 30
    .line 31
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 32
    .line 33
    check-cast v0, Ljava/lang/Integer;

    .line 34
    .line 35
    if-eqz v0, :cond_0

    .line 36
    .line 37
    goto :goto_0

    .line 38
    :cond_0
    const-string p0, "Cannot create key without ID requirement with parameters with ID requirement"

    .line 39
    .line 40
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 41
    .line 42
    .line 43
    return-object v1

    .line 44
    :cond_1
    :goto_0
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 45
    .line 46
    check-cast v0, Lka2;

    .line 47
    .line 48
    invoke-virtual {v0}, Lka2;->a()Z

    .line 49
    .line 50
    .line 51
    move-result v0

    .line 52
    if-nez v0, :cond_3

    .line 53
    .line 54
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 55
    .line 56
    check-cast v0, Ljava/lang/Integer;

    .line 57
    .line 58
    if-nez v0, :cond_2

    .line 59
    .line 60
    goto :goto_1

    .line 61
    :cond_2
    const-string p0, "Cannot create key with ID requirement with parameters without ID requirement"

    .line 62
    .line 63
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 64
    .line 65
    .line 66
    return-object v1

    .line 67
    :cond_3
    :goto_1
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 68
    .line 69
    check-cast v0, Lka2;

    .line 70
    .line 71
    iget-object v0, v0, Lka2;->c:Lja2;

    .line 72
    .line 73
    sget-object v2, Lja2;->e:Lja2;

    .line 74
    .line 75
    if-ne v0, v2, :cond_4

    .line 76
    .line 77
    sget-object v0, Lfs3;->a:Lg20;

    .line 78
    .line 79
    goto :goto_3

    .line 80
    :cond_4
    sget-object v2, Lja2;->d:Lja2;

    .line 81
    .line 82
    if-eq v0, v2, :cond_7

    .line 83
    .line 84
    sget-object v2, Lja2;->c:Lja2;

    .line 85
    .line 86
    if-ne v0, v2, :cond_5

    .line 87
    .line 88
    goto :goto_2

    .line 89
    :cond_5
    sget-object v2, Lja2;->b:Lja2;

    .line 90
    .line 91
    if-ne v0, v2, :cond_6

    .line 92
    .line 93
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 94
    .line 95
    check-cast v0, Ljava/lang/Integer;

    .line 96
    .line 97
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I

    .line 98
    .line 99
    .line 100
    move-result v0

    .line 101
    invoke-static {v0}, Lfs3;->b(I)Lg20;

    .line 102
    .line 103
    .line 104
    move-result-object v0

    .line 105
    goto :goto_3

    .line 106
    :cond_6
    iget-object p0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 107
    .line 108
    check-cast p0, Lka2;

    .line 109
    .line 110
    iget-object p0, p0, Lka2;->c:Lja2;

    .line 111
    .line 112
    const-string v0, "Unknown HmacParameters.Variant: "

    .line 113
    .line 114
    invoke-static {p0, v0}, Lyb4;->j(Ljava/lang/Object;Ljava/lang/String;)V

    .line 115
    .line 116
    .line 117
    return-object v1

    .line 118
    :cond_7
    :goto_2
    iget-object v0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 119
    .line 120
    check-cast v0, Ljava/lang/Integer;

    .line 121
    .line 122
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I

    .line 123
    .line 124
    .line 125
    move-result v0

    .line 126
    invoke-static {v0}, Lfs3;->a(I)Lg20;

    .line 127
    .line 128
    .line 129
    move-result-object v0

    .line 130
    :goto_3
    new-instance v1, Lda2;

    .line 131
    .line 132
    iget-object v2, p0, Lfa1;->b:Ljava/lang/Object;

    .line 133
    .line 134
    check-cast v2, Lka2;

    .line 135
    .line 136
    iget-object v3, p0, Lfa1;->c:Ljava/lang/Object;

    .line 137
    .line 138
    check-cast v3, Ltt3;

    .line 139
    .line 140
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 141
    .line 142
    check-cast p0, Ljava/lang/Integer;

    .line 143
    .line 144
    invoke-direct {v1, v2, v3, v0, p0}, Lda2;-><init>(Lka2;Ltt3;Lg20;Ljava/lang/Integer;)V

    .line 145
    .line 146
    .line 147
    return-object v1

    .line 148
    :cond_8
    const-string p0, "Key size mismatch"

    .line 149
    .line 150
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 151
    .line 152
    .line 153
    return-object v1

    .line 154
    :cond_9
    const-string p0, "Cannot build without parameters and/or key material"

    .line 155
    .line 156
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 157
    .line 158
    .line 159
    return-object v1
.end method

.method public t()Lec2;
    .locals 7

    .line 1
    const-string v0, "GET Request URL: "

    .line 2
    .line 3
    const-string v1, "FirebaseCrashlytics"

    .line 4
    .line 5
    invoke-static {}, Lvg;->m()V

    .line 6
    .line 7
    .line 8
    const/4 v2, 0x0

    .line 9
    :try_start_0
    iget-object v3, p0, Lfa1;->b:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast v3, Ljava/lang/String;

    .line 12
    .line 13
    iget-object v4, p0, Lfa1;->c:Ljava/lang/Object;

    .line 14
    .line 15
    check-cast v4, Ljava/util/HashMap;

    .line 16
    .line 17
    invoke-static {v3, v4}, Lfa1;->r(Ljava/lang/String;Ljava/util/HashMap;)Ljava/lang/String;

    .line 18
    .line 19
    .line 20
    move-result-object v3

    .line 21
    invoke-virtual {v0, v3}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    .line 22
    .line 23
    .line 24
    move-result-object v0

    .line 25
    const/4 v4, 0x2

    .line 26
    invoke-static {v1, v4}, Landroid/util/Log;->isLoggable(Ljava/lang/String;I)Z

    .line 27
    .line 28
    .line 29
    move-result v4

    .line 30
    if-eqz v4, :cond_0

    .line 31
    .line 32
    invoke-static {v1, v0, v2}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 33
    .line 34
    .line 35
    :cond_0
    new-instance v0, Ljava/net/URL;

    .line 36
    .line 37
    invoke-direct {v0, v3}, Ljava/net/URL;-><init>(Ljava/lang/String;)V

    .line 38
    .line 39
    .line 40
    invoke-virtual {v0}, Ljava/net/URL;->openConnection()Ljava/net/URLConnection;

    .line 41
    .line 42
    .line 43
    move-result-object v0

    .line 44
    check-cast v0, Ljavax/net/ssl/HttpsURLConnection;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_2

    .line 45
    .line 46
    const/16 v1, 0x2710

    .line 47
    .line 48
    :try_start_1
    invoke-virtual {v0, v1}, Ljava/net/URLConnection;->setReadTimeout(I)V

    .line 49
    .line 50
    .line 51
    invoke-virtual {v0, v1}, Ljava/net/URLConnection;->setConnectTimeout(I)V

    .line 52
    .line 53
    .line 54
    const-string v1, "GET"

    .line 55
    .line 56
    invoke-virtual {v0, v1}, Ljava/net/HttpURLConnection;->setRequestMethod(Ljava/lang/String;)V

    .line 57
    .line 58
    .line 59
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 60
    .line 61
    check-cast p0, Ljava/util/HashMap;

    .line 62
    .line 63
    invoke-virtual {p0}, Ljava/util/HashMap;->entrySet()Ljava/util/Set;

    .line 64
    .line 65
    .line 66
    move-result-object p0

    .line 67
    invoke-interface {p0}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    .line 68
    .line 69
    .line 70
    move-result-object p0

    .line 71
    :goto_0
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    .line 72
    .line 73
    .line 74
    move-result v1

    .line 75
    if-eqz v1, :cond_1

    .line 76
    .line 77
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 78
    .line 79
    .line 80
    move-result-object v1

    .line 81
    check-cast v1, Ljava/util/Map$Entry;

    .line 82
    .line 83
    invoke-interface {v1}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;

    .line 84
    .line 85
    .line 86
    move-result-object v3

    .line 87
    check-cast v3, Ljava/lang/String;

    .line 88
    .line 89
    invoke-interface {v1}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;

    .line 90
    .line 91
    .line 92
    move-result-object v1

    .line 93
    check-cast v1, Ljava/lang/String;

    .line 94
    .line 95
    invoke-virtual {v0, v3, v1}, Ljava/net/URLConnection;->addRequestProperty(Ljava/lang/String;Ljava/lang/String;)V

    .line 96
    .line 97
    .line 98
    goto :goto_0

    .line 99
    :catchall_0
    move-exception p0

    .line 100
    goto :goto_3

    .line 101
    :cond_1
    invoke-virtual {v0}, Ljava/net/URLConnection;->connect()V

    .line 102
    .line 103
    .line 104
    invoke-virtual {v0}, Ljava/net/HttpURLConnection;->getResponseCode()I

    .line 105
    .line 106
    .line 107
    move-result p0

    .line 108
    invoke-virtual {v0}, Ljava/net/URLConnection;->getInputStream()Ljava/io/InputStream;

    .line 109
    .line 110
    .line 111
    move-result-object v1
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 112
    if-eqz v1, :cond_3

    .line 113
    .line 114
    :try_start_2
    new-instance v2, Ljava/io/BufferedReader;

    .line 115
    .line 116
    new-instance v3, Ljava/io/InputStreamReader;

    .line 117
    .line 118
    const-string v4, "UTF-8"

    .line 119
    .line 120
    invoke-direct {v3, v1, v4}, Ljava/io/InputStreamReader;-><init>(Ljava/io/InputStream;Ljava/lang/String;)V

    .line 121
    .line 122
    .line 123
    invoke-direct {v2, v3}, Ljava/io/BufferedReader;-><init>(Ljava/io/Reader;)V

    .line 124
    .line 125
    .line 126
    const/16 v3, 0x2000

    .line 127
    .line 128
    new-array v3, v3, [C

    .line 129
    .line 130
    new-instance v4, Ljava/lang/StringBuilder;

    .line 131
    .line 132
    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    .line 133
    .line 134
    .line 135
    :goto_1
    invoke-virtual {v2, v3}, Ljava/io/Reader;->read([C)I

    .line 136
    .line 137
    .line 138
    move-result v5

    .line 139
    const/4 v6, -0x1

    .line 140
    if-eq v5, v6, :cond_2

    .line 141
    .line 142
    const/4 v6, 0x0

    .line 143
    invoke-virtual {v4, v3, v6, v5}, Ljava/lang/StringBuilder;->append([CII)Ljava/lang/StringBuilder;

    .line 144
    .line 145
    .line 146
    goto :goto_1

    .line 147
    :cond_2
    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 148
    .line 149
    .line 150
    move-result-object v2
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_1

    .line 151
    goto :goto_2

    .line 152
    :catchall_1
    move-exception p0

    .line 153
    move-object v2, v1

    .line 154
    goto :goto_3

    .line 155
    :cond_3
    :goto_2
    if-eqz v1, :cond_4

    .line 156
    .line 157
    invoke-virtual {v1}, Ljava/io/InputStream;->close()V

    .line 158
    .line 159
    .line 160
    :cond_4
    invoke-virtual {v0}, Ljava/net/HttpURLConnection;->disconnect()V

    .line 161
    .line 162
    .line 163
    new-instance v0, Lec2;

    .line 164
    .line 165
    invoke-direct {v0, p0, v2}, Lec2;-><init>(ILjava/lang/String;)V

    .line 166
    .line 167
    .line 168
    return-object v0

    .line 169
    :catchall_2
    move-exception p0

    .line 170
    move-object v0, v2

    .line 171
    :goto_3
    if-eqz v2, :cond_5

    .line 172
    .line 173
    invoke-virtual {v2}, Ljava/io/InputStream;->close()V

    .line 174
    .line 175
    .line 176
    :cond_5
    if-eqz v0, :cond_6

    .line 177
    .line 178
    invoke-virtual {v0}, Ljava/net/HttpURLConnection;->disconnect()V

    .line 179
    .line 180
    .line 181
    :cond_6
    throw p0
.end method

.method public toString()Ljava/lang/String;
    .locals 7

    .line 1
    iget v0, p0, Lfa1;->a:I

    .line 2
    .line 3
    const/16 v1, 0x7d

    .line 4
    .line 5
    const-string v2, ", "

    .line 6
    .line 7
    const/16 v3, 0x7b

    .line 8
    .line 9
    const/16 v4, 0x20

    .line 10
    .line 11
    const-string v5, ""

    .line 12
    .line 13
    const/4 v6, 0x1

    .line 14
    sparse-switch v0, :sswitch_data_0

    .line 15
    .line 16
    .line 17
    invoke-super {p0}, Ljava/lang/Object;->toString()Ljava/lang/String;

    .line 18
    .line 19
    .line 20
    move-result-object p0

    .line 21
    return-object p0

    .line 22
    :sswitch_0
    new-instance v0, Ljava/lang/StringBuilder;

    .line 23
    .line 24
    invoke-direct {v0, v4}, Ljava/lang/StringBuilder;-><init>(I)V

    .line 25
    .line 26
    .line 27
    iget-object v4, p0, Lfa1;->b:Ljava/lang/Object;

    .line 28
    .line 29
    check-cast v4, Ljava/lang/String;

    .line 30
    .line 31
    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 32
    .line 33
    .line 34
    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    .line 35
    .line 36
    .line 37
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 38
    .line 39
    check-cast p0, Lfa1;

    .line 40
    .line 41
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 42
    .line 43
    check-cast p0, Lfa1;

    .line 44
    .line 45
    :goto_0
    if-eqz p0, :cond_2

    .line 46
    .line 47
    iget-object v3, p0, Lfa1;->c:Ljava/lang/Object;

    .line 48
    .line 49
    invoke-virtual {v0, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 50
    .line 51
    .line 52
    iget-object v4, p0, Lfa1;->b:Ljava/lang/Object;

    .line 53
    .line 54
    check-cast v4, Ljava/lang/String;

    .line 55
    .line 56
    if-eqz v4, :cond_0

    .line 57
    .line 58
    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 59
    .line 60
    .line 61
    const/16 v4, 0x3d

    .line 62
    .line 63
    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    .line 64
    .line 65
    .line 66
    :cond_0
    if-eqz v3, :cond_1

    .line 67
    .line 68
    invoke-virtual {v3}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 69
    .line 70
    .line 71
    move-result-object v4

    .line 72
    invoke-virtual {v4}, Ljava/lang/Class;->isArray()Z

    .line 73
    .line 74
    .line 75
    move-result v4

    .line 76
    if-eqz v4, :cond_1

    .line 77
    .line 78
    filled-new-array {v3}, [Ljava/lang/Object;

    .line 79
    .line 80
    .line 81
    move-result-object v3

    .line 82
    invoke-static {v3}, Ljava/util/Arrays;->deepToString([Ljava/lang/Object;)Ljava/lang/String;

    .line 83
    .line 84
    .line 85
    move-result-object v3

    .line 86
    invoke-virtual {v3}, Ljava/lang/String;->length()I

    .line 87
    .line 88
    .line 89
    move-result v4

    .line 90
    sub-int/2addr v4, v6

    .line 91
    invoke-virtual {v0, v3, v6, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/CharSequence;II)Ljava/lang/StringBuilder;

    .line 92
    .line 93
    .line 94
    goto :goto_1

    .line 95
    :cond_1
    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 96
    .line 97
    .line 98
    :goto_1
    iget-object p0, p0, Lfa1;->d:Ljava/lang/Object;

    .line 99
    .line 100
    check-cast p0, Lfa1;

    .line 101
    .line 102
    move-object v5, v2

    .line 103
    goto :goto_0

    .line 104
    :cond_2
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    .line 105
    .line 106
    .line 107
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 108
    .line 109
    .line 110
    move-result-object p0

    .line 111
    return-object p0

    .line 112
    :sswitch_1
    new-instance v0, Ljava/lang/StringBuilder;

    .line 113
    .line 114
    invoke-direct {v0, v4}, Ljava/lang/StringBuilder;-><init>(I)V

    .line 115
    .line 116
    .line 117
    iget-object v4, p0, Lfa1;->b:Ljava/lang/Object;

    .line 118
    .line 119
    check-cast v4, Ljava/lang/String;

    .line 120
    .line 121
    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 122
    .line 123
    .line 124
    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    .line 125
    .line 126
    .line 127
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 128
    .line 129
    check-cast p0, Liq2;

    .line 130
    .line 131
    iget-object p0, p0, Liq2;->c:Ljava/lang/Object;

    .line 132
    .line 133
    check-cast p0, Liq2;

    .line 134
    .line 135
    :goto_2
    if-eqz p0, :cond_4

    .line 136
    .line 137
    iget-object v3, p0, Liq2;->b:Ljava/lang/Object;

    .line 138
    .line 139
    invoke-virtual {v0, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 140
    .line 141
    .line 142
    if-eqz v3, :cond_3

    .line 143
    .line 144
    invoke-virtual {v3}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 145
    .line 146
    .line 147
    move-result-object v4

    .line 148
    invoke-virtual {v4}, Ljava/lang/Class;->isArray()Z

    .line 149
    .line 150
    .line 151
    move-result v4

    .line 152
    if-eqz v4, :cond_3

    .line 153
    .line 154
    filled-new-array {v3}, [Ljava/lang/Object;

    .line 155
    .line 156
    .line 157
    move-result-object v3

    .line 158
    invoke-static {v3}, Ljava/util/Arrays;->deepToString([Ljava/lang/Object;)Ljava/lang/String;

    .line 159
    .line 160
    .line 161
    move-result-object v3

    .line 162
    invoke-virtual {v3}, Ljava/lang/String;->length()I

    .line 163
    .line 164
    .line 165
    move-result v4

    .line 166
    sub-int/2addr v4, v6

    .line 167
    invoke-virtual {v0, v3, v6, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/CharSequence;II)Ljava/lang/StringBuilder;

    .line 168
    .line 169
    .line 170
    goto :goto_3

    .line 171
    :cond_3
    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 172
    .line 173
    .line 174
    :goto_3
    iget-object p0, p0, Liq2;->c:Ljava/lang/Object;

    .line 175
    .line 176
    check-cast p0, Liq2;

    .line 177
    .line 178
    move-object v5, v2

    .line 179
    goto :goto_2

    .line 180
    :cond_4
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    .line 181
    .line 182
    .line 183
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 184
    .line 185
    .line 186
    move-result-object p0

    .line 187
    return-object p0

    .line 188
    :sswitch_2
    invoke-virtual {p0}, Lfa1;->x()Lut2;

    .line 189
    .line 190
    .line 191
    move-result-object p0

    .line 192
    sget v0, Lzq5;->a:I

    .line 193
    .line 194
    invoke-static {}, Lbu2;->w()Lyt2;

    .line 195
    .line 196
    .line 197
    move-result-object v0

    .line 198
    invoke-virtual {p0}, Lut2;->z()I

    .line 199
    .line 200
    .line 201
    move-result v1

    .line 202
    invoke-virtual {v0}, Le52;->e()V

    .line 203
    .line 204
    .line 205
    iget-object v2, v0, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 206
    .line 207
    check-cast v2, Lbu2;

    .line 208
    .line 209
    invoke-static {v2, v1}, Lbu2;->u(Lbu2;I)V

    .line 210
    .line 211
    .line 212
    invoke-virtual {p0}, Lut2;->y()Lgk2;

    .line 213
    .line 214
    .line 215
    move-result-object p0

    .line 216
    invoke-interface {p0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    .line 217
    .line 218
    .line 219
    move-result-object p0

    .line 220
    :goto_4
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    .line 221
    .line 222
    .line 223
    move-result v1

    .line 224
    if-eqz v1, :cond_5

    .line 225
    .line 226
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 227
    .line 228
    .line 229
    move-result-object v1

    .line 230
    check-cast v1, Ltt2;

    .line 231
    .line 232
    invoke-static {}, Lau2;->y()Lzt2;

    .line 233
    .line 234
    .line 235
    move-result-object v2

    .line 236
    invoke-virtual {v1}, Ltt2;->y()Lis2;

    .line 237
    .line 238
    .line 239
    move-result-object v3

    .line 240
    invoke-virtual {v3}, Lis2;->z()Ljava/lang/String;

    .line 241
    .line 242
    .line 243
    move-result-object v3

    .line 244
    invoke-virtual {v2}, Le52;->e()V

    .line 245
    .line 246
    .line 247
    iget-object v4, v2, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 248
    .line 249
    check-cast v4, Lau2;

    .line 250
    .line 251
    invoke-static {v4, v3}, Lau2;->u(Lau2;Ljava/lang/String;)V

    .line 252
    .line 253
    .line 254
    invoke-virtual {v1}, Ltt2;->B()Lss2;

    .line 255
    .line 256
    .line 257
    move-result-object v3

    .line 258
    invoke-virtual {v2}, Le52;->e()V

    .line 259
    .line 260
    .line 261
    iget-object v4, v2, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 262
    .line 263
    check-cast v4, Lau2;

    .line 264
    .line 265
    invoke-static {v4, v3}, Lau2;->w(Lau2;Lss2;)V

    .line 266
    .line 267
    .line 268
    invoke-virtual {v1}, Ltt2;->A()Les3;

    .line 269
    .line 270
    .line 271
    move-result-object v3

    .line 272
    invoke-virtual {v2}, Le52;->e()V

    .line 273
    .line 274
    .line 275
    iget-object v4, v2, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 276
    .line 277
    check-cast v4, Lau2;

    .line 278
    .line 279
    invoke-static {v4, v3}, Lau2;->v(Lau2;Les3;)V

    .line 280
    .line 281
    .line 282
    invoke-virtual {v1}, Ltt2;->z()I

    .line 283
    .line 284
    .line 285
    move-result v1

    .line 286
    invoke-virtual {v2}, Le52;->e()V

    .line 287
    .line 288
    .line 289
    iget-object v3, v2, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 290
    .line 291
    check-cast v3, Lau2;

    .line 292
    .line 293
    invoke-static {v3, v1}, Lau2;->x(Lau2;I)V

    .line 294
    .line 295
    .line 296
    invoke-virtual {v2}, Le52;->a()Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 297
    .line 298
    .line 299
    move-result-object v1

    .line 300
    check-cast v1, Lau2;

    .line 301
    .line 302
    invoke-virtual {v0}, Le52;->e()V

    .line 303
    .line 304
    .line 305
    iget-object v2, v0, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 306
    .line 307
    check-cast v2, Lbu2;

    .line 308
    .line 309
    invoke-static {v2, v1}, Lbu2;->v(Lbu2;Lau2;)V

    .line 310
    .line 311
    .line 312
    goto :goto_4

    .line 313
    :cond_5
    invoke-virtual {v0}, Le52;->a()Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 314
    .line 315
    .line 316
    move-result-object p0

    .line 317
    check-cast p0, Lbu2;

    .line 318
    .line 319
    invoke-virtual {p0}, Lcom/google/crypto/tink/shaded/protobuf/d;->toString()Ljava/lang/String;

    .line 320
    .line 321
    .line 322
    move-result-object p0

    .line 323
    return-object p0

    .line 324
    nop

    .line 325
    :sswitch_data_0
    .sparse-switch
        0xe -> :sswitch_2
        0x13 -> :sswitch_1
        0x16 -> :sswitch_0
    .end sparse-switch
.end method

.method public w(I)Lwt2;
    .locals 4

    .line 1
    iget-object p0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Ljava/util/List;

    .line 4
    .line 5
    const/4 v0, 0x0

    .line 6
    if-ltz p1, :cond_2

    .line 7
    .line 8
    invoke-interface {p0}, Ljava/util/List;->size()I

    .line 9
    .line 10
    .line 11
    move-result v1

    .line 12
    if-ge p1, v1, :cond_2

    .line 13
    .line 14
    invoke-interface {p0, p1}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 15
    .line 16
    .line 17
    move-result-object v1

    .line 18
    check-cast v1, Lwt2;

    .line 19
    .line 20
    iget-object v2, v1, Lwt2;->b:Lss2;

    .line 21
    .line 22
    invoke-static {v2}, Lfa1;->D(Lss2;)Z

    .line 23
    .line 24
    .line 25
    move-result v2

    .line 26
    const-string v3, "Keyset-Entry at position "

    .line 27
    .line 28
    if-eqz v2, :cond_1

    .line 29
    .line 30
    iget-boolean v1, v1, Lwt2;->f:Z

    .line 31
    .line 32
    if-nez v1, :cond_0

    .line 33
    .line 34
    invoke-interface {p0, p1}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 35
    .line 36
    .line 37
    move-result-object p0

    .line 38
    check-cast p0, Lwt2;

    .line 39
    .line 40
    return-object p0

    .line 41
    :cond_0
    const-string p0, " didn\'t parse correctly"

    .line 42
    .line 43
    invoke-static {p1, v3, p0}, Lha6;->g(ILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    .line 44
    .line 45
    .line 46
    move-result-object p0

    .line 47
    invoke-static {p0}, Lr05;->k(Ljava/lang/String;)V

    .line 48
    .line 49
    .line 50
    return-object v0

    .line 51
    :cond_1
    const-string p0, " has wrong status"

    .line 52
    .line 53
    invoke-static {p1, v3, p0}, Lha6;->g(ILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    .line 54
    .line 55
    .line 56
    move-result-object p0

    .line 57
    invoke-static {p0}, Lr05;->k(Ljava/lang/String;)V

    .line 58
    .line 59
    .line 60
    return-object v0

    .line 61
    :cond_2
    const-string v1, "Invalid index "

    .line 62
    .line 63
    const-string v2, " for keyset of size "

    .line 64
    .line 65
    invoke-static {p1, v1, v2}, Lha6;->r(ILjava/lang/String;Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 66
    .line 67
    .line 68
    move-result-object p1

    .line 69
    invoke-interface {p0}, Ljava/util/List;->size()I

    .line 70
    .line 71
    .line 72
    move-result p0

    .line 73
    invoke-static {p1, p0}, Lj92;->l(Ljava/lang/StringBuilder;I)V

    .line 74
    .line 75
    .line 76
    return-object v0
.end method

.method public x()Lut2;
    .locals 5

    .line 1
    :try_start_0
    invoke-static {}, Lut2;->A()Lrt2;

    .line 2
    .line 3
    .line 4
    move-result-object v0

    .line 5
    iget-object p0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast p0, Ljava/util/List;

    .line 8
    .line 9
    invoke-interface {p0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    .line 10
    .line 11
    .line 12
    move-result-object p0

    .line 13
    :cond_0
    :goto_0
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    .line 14
    .line 15
    .line 16
    move-result v1

    .line 17
    if-eqz v1, :cond_1

    .line 18
    .line 19
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 20
    .line 21
    .line 22
    move-result-object v1

    .line 23
    check-cast v1, Lwt2;

    .line 24
    .line 25
    invoke-virtual {v1}, Lwt2;->a()Lpr0;

    .line 26
    .line 27
    .line 28
    move-result-object v2

    .line 29
    iget v3, v1, Lwt2;->d:I

    .line 30
    .line 31
    iget-object v4, v1, Lwt2;->b:Lss2;

    .line 32
    .line 33
    invoke-static {v2, v4, v3}, Lfa1;->q(Lpr0;Lss2;I)Ltt2;

    .line 34
    .line 35
    .line 36
    move-result-object v2

    .line 37
    invoke-virtual {v0}, Le52;->e()V

    .line 38
    .line 39
    .line 40
    iget-object v4, v0, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 41
    .line 42
    check-cast v4, Lut2;

    .line 43
    .line 44
    invoke-static {v4, v2}, Lut2;->v(Lut2;Ltt2;)V

    .line 45
    .line 46
    .line 47
    iget-boolean v1, v1, Lwt2;->e:Z

    .line 48
    .line 49
    if-eqz v1, :cond_0

    .line 50
    .line 51
    invoke-virtual {v0}, Le52;->e()V

    .line 52
    .line 53
    .line 54
    iget-object v1, v0, Le52;->b:Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 55
    .line 56
    check-cast v1, Lut2;

    .line 57
    .line 58
    invoke-static {v1, v3}, Lut2;->u(Lut2;I)V

    .line 59
    .line 60
    .line 61
    goto :goto_0

    .line 62
    :cond_1
    invoke-virtual {v0}, Le52;->a()Lcom/google/crypto/tink/shaded/protobuf/d;

    .line 63
    .line 64
    .line 65
    move-result-object p0

    .line 66
    check-cast p0, Lut2;
    :try_end_0
    .catch Ljava/security/GeneralSecurityException; {:try_start_0 .. :try_end_0} :catch_0

    .line 67
    .line 68
    return-object p0

    .line 69
    :catch_0
    move-exception p0

    .line 70
    invoke-static {p0}, Lj92;->r(Ljava/lang/Throwable;)V

    .line 71
    .line 72
    .line 73
    const/4 p0, 0x0

    .line 74
    return-object p0
.end method

.method public y()Lwt2;
    .locals 3

    .line 1
    iget-object p0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Ljava/util/List;

    .line 4
    .line 5
    invoke-interface {p0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    .line 6
    .line 7
    .line 8
    move-result-object p0

    .line 9
    :cond_0
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    .line 10
    .line 11
    .line 12
    move-result v0

    .line 13
    const/4 v1, 0x0

    .line 14
    if-eqz v0, :cond_2

    .line 15
    .line 16
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 17
    .line 18
    .line 19
    move-result-object v0

    .line 20
    check-cast v0, Lwt2;

    .line 21
    .line 22
    if-eqz v0, :cond_0

    .line 23
    .line 24
    iget-boolean v2, v0, Lwt2;->e:Z

    .line 25
    .line 26
    if-eqz v2, :cond_0

    .line 27
    .line 28
    iget-object p0, v0, Lwt2;->c:Li9;

    .line 29
    .line 30
    sget-object v2, Li9;->j:Li9;

    .line 31
    .line 32
    if-ne p0, v2, :cond_1

    .line 33
    .line 34
    return-object v0

    .line 35
    :cond_1
    const-string p0, "Keyset has primary which isn\'t enabled"

    .line 36
    .line 37
    invoke-static {p0}, Lr05;->k(Ljava/lang/String;)V

    .line 38
    .line 39
    .line 40
    return-object v1

    .line 41
    :cond_2
    const-string p0, "Keyset has no valid primary"

    .line 42
    .line 43
    invoke-static {p0}, Lr05;->k(Ljava/lang/String;)V

    .line 44
    .line 45
    .line 46
    return-object v1
.end method

.method public z()Ljava/lang/Object;
    .locals 14

    .line 1
    iget-object v0, p0, Lfa1;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Ljava/util/List;

    .line 4
    .line 5
    iget-object v1, p0, Lfa1;->d:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast v1, Lfa1;

    .line 8
    .line 9
    if-nez v1, :cond_0

    .line 10
    .line 11
    move-object v2, p0

    .line 12
    goto :goto_0

    .line 13
    :cond_0
    move-object v2, v1

    .line 14
    :goto_0
    invoke-virtual {v2}, Lfa1;->x()Lut2;

    .line 15
    .line 16
    .line 17
    move-result-object v2

    .line 18
    sget v3, Lzq5;->a:I

    .line 19
    .line 20
    invoke-virtual {v2}, Lut2;->z()I

    .line 21
    .line 22
    .line 23
    move-result v3

    .line 24
    invoke-virtual {v2}, Lut2;->y()Lgk2;

    .line 25
    .line 26
    .line 27
    move-result-object v4

    .line 28
    invoke-interface {v4}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    .line 29
    .line 30
    .line 31
    move-result-object v4

    .line 32
    const/4 v5, 0x1

    .line 33
    const/4 v6, 0x0

    .line 34
    move v9, v5

    .line 35
    move v7, v6

    .line 36
    move v8, v7

    .line 37
    :goto_1
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    .line 38
    .line 39
    .line 40
    move-result v10

    .line 41
    const/4 v11, 0x0

    .line 42
    if-eqz v10, :cond_8

    .line 43
    .line 44
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 45
    .line 46
    .line 47
    move-result-object v10

    .line 48
    check-cast v10, Ltt2;

    .line 49
    .line 50
    invoke-virtual {v10}, Ltt2;->B()Lss2;

    .line 51
    .line 52
    .line 53
    move-result-object v12

    .line 54
    sget-object v13, Lss2;->ENABLED:Lss2;

    .line 55
    .line 56
    if-eq v12, v13, :cond_1

    .line 57
    .line 58
    goto :goto_1

    .line 59
    :cond_1
    invoke-virtual {v10}, Ltt2;->C()Z

    .line 60
    .line 61
    .line 62
    move-result v12

    .line 63
    if-eqz v12, :cond_7

    .line 64
    .line 65
    invoke-virtual {v10}, Ltt2;->A()Les3;

    .line 66
    .line 67
    .line 68
    move-result-object v12

    .line 69
    sget-object v13, Les3;->UNKNOWN_PREFIX:Les3;

    .line 70
    .line 71
    if-eq v12, v13, :cond_6

    .line 72
    .line 73
    invoke-virtual {v10}, Ltt2;->B()Lss2;

    .line 74
    .line 75
    .line 76
    move-result-object v12

    .line 77
    sget-object v13, Lss2;->UNKNOWN_STATUS:Lss2;

    .line 78
    .line 79
    if-eq v12, v13, :cond_5

    .line 80
    .line 81
    invoke-virtual {v10}, Ltt2;->z()I

    .line 82
    .line 83
    .line 84
    move-result v12

    .line 85
    if-ne v12, v3, :cond_3

    .line 86
    .line 87
    if-nez v8, :cond_2

    .line 88
    .line 89
    move v8, v5

    .line 90
    goto :goto_2

    .line 91
    :cond_2
    const-string p0, "keyset contains multiple primary keys"

    .line 92
    .line 93
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 94
    .line 95
    .line 96
    return-object v11

    .line 97
    :cond_3
    :goto_2
    invoke-virtual {v10}, Ltt2;->y()Lis2;

    .line 98
    .line 99
    .line 100
    move-result-object v10

    .line 101
    invoke-virtual {v10}, Lis2;->y()Lhs2;

    .line 102
    .line 103
    .line 104
    move-result-object v10

    .line 105
    sget-object v11, Lhs2;->ASYMMETRIC_PUBLIC:Lhs2;

    .line 106
    .line 107
    if-eq v10, v11, :cond_4

    .line 108
    .line 109
    move v9, v6

    .line 110
    :cond_4
    add-int/lit8 v7, v7, 0x1

    .line 111
    .line 112
    goto :goto_1

    .line 113
    :cond_5
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 114
    .line 115
    invoke-virtual {v10}, Ltt2;->z()I

    .line 116
    .line 117
    .line 118
    move-result v0

    .line 119
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 120
    .line 121
    .line 122
    move-result-object v0

    .line 123
    filled-new-array {v0}, [Ljava/lang/Object;

    .line 124
    .line 125
    .line 126
    move-result-object v0

    .line 127
    const-string v1, "key %d has unknown status"

    .line 128
    .line 129
    invoke-static {v1, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    .line 130
    .line 131
    .line 132
    move-result-object v0

    .line 133
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 134
    .line 135
    .line 136
    throw p0

    .line 137
    :cond_6
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 138
    .line 139
    invoke-virtual {v10}, Ltt2;->z()I

    .line 140
    .line 141
    .line 142
    move-result v0

    .line 143
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 144
    .line 145
    .line 146
    move-result-object v0

    .line 147
    filled-new-array {v0}, [Ljava/lang/Object;

    .line 148
    .line 149
    .line 150
    move-result-object v0

    .line 151
    const-string v1, "key %d has unknown prefix"

    .line 152
    .line 153
    invoke-static {v1, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    .line 154
    .line 155
    .line 156
    move-result-object v0

    .line 157
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 158
    .line 159
    .line 160
    throw p0

    .line 161
    :cond_7
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 162
    .line 163
    invoke-virtual {v10}, Ltt2;->z()I

    .line 164
    .line 165
    .line 166
    move-result v0

    .line 167
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 168
    .line 169
    .line 170
    move-result-object v0

    .line 171
    filled-new-array {v0}, [Ljava/lang/Object;

    .line 172
    .line 173
    .line 174
    move-result-object v0

    .line 175
    const-string v1, "key %d has no key data"

    .line 176
    .line 177
    invoke-static {v1, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    .line 178
    .line 179
    .line 180
    move-result-object v0

    .line 181
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 182
    .line 183
    .line 184
    throw p0

    .line 185
    :cond_8
    if-eqz v7, :cond_2f

    .line 186
    .line 187
    if-nez v8, :cond_a

    .line 188
    .line 189
    if-eqz v9, :cond_9

    .line 190
    .line 191
    goto :goto_3

    .line 192
    :cond_9
    const-string p0, "keyset doesn\'t contain a valid primary key"

    .line 193
    .line 194
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 195
    .line 196
    .line 197
    return-object v11

    .line 198
    :cond_a
    :goto_3
    move v3, v6

    .line 199
    :goto_4
    invoke-interface {v0}, Ljava/util/List;->size()I

    .line 200
    .line 201
    .line 202
    move-result v4

    .line 203
    if-ge v3, v4, :cond_c

    .line 204
    .line 205
    invoke-interface {v0, v3}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 206
    .line 207
    .line 208
    move-result-object v4

    .line 209
    check-cast v4, Lwt2;

    .line 210
    .line 211
    iget-boolean v4, v4, Lwt2;->f:Z

    .line 212
    .line 213
    if-nez v4, :cond_b

    .line 214
    .line 215
    invoke-interface {v0, v3}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 216
    .line 217
    .line 218
    move-result-object v4

    .line 219
    check-cast v4, Lwt2;

    .line 220
    .line 221
    iget-object v4, v4, Lwt2;->b:Lss2;

    .line 222
    .line 223
    invoke-static {v4}, Lfa1;->D(Lss2;)Z

    .line 224
    .line 225
    .line 226
    move-result v4

    .line 227
    if-eqz v4, :cond_b

    .line 228
    .line 229
    add-int/lit8 v3, v3, 0x1

    .line 230
    .line 231
    goto :goto_4

    .line 232
    :cond_b
    invoke-virtual {v2, v3}, Lut2;->w(I)Ltt2;

    .line 233
    .line 234
    .line 235
    move-result-object p0

    .line 236
    new-instance v0, Ljava/security/GeneralSecurityException;

    .line 237
    .line 238
    const-string v1, "Key parsing of key with index "

    .line 239
    .line 240
    const-string v2, " and type_url "

    .line 241
    .line 242
    invoke-static {v3, v1, v2}, Lha6;->r(ILjava/lang/String;Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 243
    .line 244
    .line 245
    move-result-object v1

    .line 246
    invoke-virtual {p0}, Ltt2;->y()Lis2;

    .line 247
    .line 248
    .line 249
    move-result-object p0

    .line 250
    invoke-virtual {p0}, Lis2;->z()Ljava/lang/String;

    .line 251
    .line 252
    .line 253
    move-result-object p0

    .line 254
    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 255
    .line 256
    .line 257
    const-string p0, " failed, unable to get primitive"

    .line 258
    .line 259
    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 260
    .line 261
    .line 262
    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 263
    .line 264
    .line 265
    move-result-object p0

    .line 266
    invoke-direct {v0, p0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 267
    .line 268
    .line 269
    throw v0

    .line 270
    :cond_c
    if-nez v1, :cond_d

    .line 271
    .line 272
    move-object v1, p0

    .line 273
    :cond_d
    iget-object p0, p0, Lfa1;->c:Ljava/lang/Object;

    .line 274
    .line 275
    check-cast p0, Lnf3;

    .line 276
    .line 277
    sget-object v0, Ldi3;->b:Ldi3;

    .line 278
    .line 279
    iget-object v0, v0, Ldi3;->a:Ljava/util/concurrent/atomic/AtomicReference;

    .line 280
    .line 281
    invoke-virtual {v0}, Ljava/util/concurrent/atomic/AtomicReference;->get()Ljava/lang/Object;

    .line 282
    .line 283
    .line 284
    move-result-object v0

    .line 285
    check-cast v0, Ld84;

    .line 286
    .line 287
    iget-object v2, v0, Ld84;->b:Ljava/util/HashMap;

    .line 288
    .line 289
    const-class v3, Lxb4;

    .line 290
    .line 291
    invoke-virtual {v2, v3}, Ljava/util/HashMap;->containsKey(Ljava/lang/Object;)Z

    .line 292
    .line 293
    .line 294
    move-result v4

    .line 295
    if-eqz v4, :cond_2e

    .line 296
    .line 297
    invoke-virtual {v2, v3}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 298
    .line 299
    .line 300
    move-result-object v2

    .line 301
    check-cast v2, Lh7;

    .line 302
    .line 303
    new-instance v3, Lbd;

    .line 304
    .line 305
    const/16 v4, 0x1b

    .line 306
    .line 307
    invoke-direct {v3, v4, v0, v2}, Lbd;-><init>(ILjava/lang/Object;Ljava/lang/Object;)V

    .line 308
    .line 309
    .line 310
    iget v0, v2, Lh7;->a:I

    .line 311
    .line 312
    const-string v2, " with parameters "

    .line 313
    .line 314
    const-string v4, "Cannot get output prefix for key of class "

    .line 315
    .line 316
    const-string v5, "PrefixMap only supports 0 and 5 byte prefixes"

    .line 317
    .line 318
    const/4 v7, 0x5

    .line 319
    packed-switch v0, :pswitch_data_0

    .line 320
    .line 321
    .line 322
    new-instance v0, Ljava/util/HashMap;

    .line 323
    .line 324
    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    .line 325
    .line 326
    .line 327
    :goto_5
    iget-object v8, v1, Lfa1;->b:Ljava/lang/Object;

    .line 328
    .line 329
    check-cast v8, Ljava/util/List;

    .line 330
    .line 331
    invoke-interface {v8}, Ljava/util/List;->size()I

    .line 332
    .line 333
    .line 334
    move-result v8

    .line 335
    if-ge v6, v8, :cond_14

    .line 336
    .line 337
    invoke-virtual {v1, v6}, Lfa1;->w(I)Lwt2;

    .line 338
    .line 339
    .line 340
    move-result-object v8

    .line 341
    iget-object v9, v8, Lwt2;->c:Li9;

    .line 342
    .line 343
    sget-object v10, Li9;->j:Li9;

    .line 344
    .line 345
    invoke-virtual {v9, v10}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    .line 346
    .line 347
    .line 348
    move-result v9

    .line 349
    if-eqz v9, :cond_13

    .line 350
    .line 351
    invoke-virtual {v3, v8}, Lbd;->c(Lwt2;)Ljava/lang/Object;

    .line 352
    .line 353
    .line 354
    move-result-object v9

    .line 355
    check-cast v9, Lxb4;

    .line 356
    .line 357
    invoke-virtual {v8}, Lwt2;->a()Lpr0;

    .line 358
    .line 359
    .line 360
    move-result-object v10

    .line 361
    instance-of v12, v10, Lv05;

    .line 362
    .line 363
    if-eqz v12, :cond_e

    .line 364
    .line 365
    check-cast v10, Lv05;

    .line 366
    .line 367
    invoke-virtual {v10}, Lv05;->J()Lg20;

    .line 368
    .line 369
    .line 370
    move-result-object v10

    .line 371
    goto :goto_6

    .line 372
    :cond_e
    instance-of v12, v10, Ljw2;

    .line 373
    .line 374
    if-eqz v12, :cond_12

    .line 375
    .line 376
    check-cast v10, Ljw2;

    .line 377
    .line 378
    invoke-virtual {v10}, Ljw2;->J()Lg20;

    .line 379
    .line 380
    .line 381
    move-result-object v10

    .line 382
    :goto_6
    new-instance v12, Lzb4;

    .line 383
    .line 384
    iget v8, v8, Lwt2;->d:I

    .line 385
    .line 386
    invoke-direct {v12, v9, v8}, Lzb4;-><init>(Lxb4;I)V

    .line 387
    .line 388
    .line 389
    iget-object v8, v10, Lg20;->a:[B

    .line 390
    .line 391
    array-length v9, v8

    .line 392
    if-eqz v9, :cond_10

    .line 393
    .line 394
    array-length v8, v8

    .line 395
    if-ne v8, v7, :cond_f

    .line 396
    .line 397
    goto :goto_7

    .line 398
    :cond_f
    invoke-static {v5}, Lla6;->x(Ljava/lang/String;)V

    .line 399
    .line 400
    .line 401
    goto/16 :goto_19

    .line 402
    .line 403
    :cond_10
    :goto_7
    invoke-virtual {v0, v10}, Ljava/util/HashMap;->containsKey(Ljava/lang/Object;)Z

    .line 404
    .line 405
    .line 406
    move-result v8

    .line 407
    if-eqz v8, :cond_11

    .line 408
    .line 409
    invoke-virtual {v0, v10}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 410
    .line 411
    .line 412
    move-result-object v8

    .line 413
    check-cast v8, Ljava/util/List;

    .line 414
    .line 415
    goto :goto_8

    .line 416
    :cond_11
    new-instance v8, Ljava/util/ArrayList;

    .line 417
    .line 418
    invoke-direct {v8}, Ljava/util/ArrayList;-><init>()V

    .line 419
    .line 420
    .line 421
    invoke-virtual {v0, v10, v8}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 422
    .line 423
    .line 424
    :goto_8
    invoke-interface {v8, v12}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 425
    .line 426
    .line 427
    goto :goto_9

    .line 428
    :cond_12
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 429
    .line 430
    invoke-virtual {v10}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 431
    .line 432
    .line 433
    move-result-object v0

    .line 434
    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    .line 435
    .line 436
    .line 437
    move-result-object v0

    .line 438
    invoke-virtual {v10}, Lpr0;->u()Lr04;

    .line 439
    .line 440
    .line 441
    move-result-object v1

    .line 442
    new-instance v3, Ljava/lang/StringBuilder;

    .line 443
    .line 444
    invoke-direct {v3, v4}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 445
    .line 446
    .line 447
    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 448
    .line 449
    .line 450
    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 451
    .line 452
    .line 453
    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 454
    .line 455
    .line 456
    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 457
    .line 458
    .line 459
    move-result-object v0

    .line 460
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 461
    .line 462
    .line 463
    throw p0

    .line 464
    :cond_13
    :goto_9
    add-int/lit8 v6, v6, 0x1

    .line 465
    .line 466
    goto/16 :goto_5

    .line 467
    .line 468
    :cond_14
    iget-object p0, p0, Lnf3;->a:Ljava/util/Map;

    .line 469
    .line 470
    invoke-interface {p0}, Ljava/util/Map;->isEmpty()Z

    .line 471
    .line 472
    .line 473
    move-result p0

    .line 474
    if-nez p0, :cond_15

    .line 475
    .line 476
    sget-object p0, Lzh3;->b:Lzh3;

    .line 477
    .line 478
    invoke-virtual {p0}, Lzh3;->a()Lyh3;

    .line 479
    .line 480
    .line 481
    move-result-object p0

    .line 482
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 483
    .line 484
    .line 485
    :cond_15
    new-instance v11, Lac4;

    .line 486
    .line 487
    new-instance p0, Ls64;

    .line 488
    .line 489
    invoke-direct {p0, v0}, Ls64;-><init>(Ljava/util/HashMap;)V

    .line 490
    .line 491
    .line 492
    invoke-direct {v11, p0}, Lac4;-><init>(Ls64;)V

    .line 493
    .line 494
    .line 495
    goto/16 :goto_19

    .line 496
    .line 497
    :pswitch_0
    iget-object p0, p0, Lnf3;->a:Ljava/util/Map;

    .line 498
    .line 499
    invoke-interface {p0}, Ljava/util/Map;->isEmpty()Z

    .line 500
    .line 501
    .line 502
    move-result p0

    .line 503
    if-nez p0, :cond_16

    .line 504
    .line 505
    sget-object p0, Lzh3;->b:Lzh3;

    .line 506
    .line 507
    invoke-virtual {p0}, Lzh3;->a()Lyh3;

    .line 508
    .line 509
    .line 510
    move-result-object p0

    .line 511
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 512
    .line 513
    .line 514
    :cond_16
    new-instance v11, Ltv2;

    .line 515
    .line 516
    new-instance p0, Lmi0;

    .line 517
    .line 518
    invoke-virtual {v1}, Lfa1;->y()Lwt2;

    .line 519
    .line 520
    .line 521
    move-result-object v0

    .line 522
    invoke-virtual {v3, v0}, Lbd;->c(Lwt2;)Ljava/lang/Object;

    .line 523
    .line 524
    .line 525
    move-result-object v0

    .line 526
    check-cast v0, Ltb4;

    .line 527
    .line 528
    invoke-virtual {v1}, Lfa1;->y()Lwt2;

    .line 529
    .line 530
    .line 531
    move-result-object v1

    .line 532
    iget v1, v1, Lwt2;->d:I

    .line 533
    .line 534
    invoke-direct {p0, v0, v1}, Lmi0;-><init>(Ltb4;I)V

    .line 535
    .line 536
    .line 537
    invoke-direct {v11}, Ljava/lang/Object;-><init>()V

    .line 538
    .line 539
    .line 540
    goto/16 :goto_19

    .line 541
    .line 542
    :pswitch_1
    new-instance v0, Ljava/util/HashMap;

    .line 543
    .line 544
    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    .line 545
    .line 546
    .line 547
    :goto_a
    iget-object v8, v1, Lfa1;->b:Ljava/lang/Object;

    .line 548
    .line 549
    check-cast v8, Ljava/util/List;

    .line 550
    .line 551
    invoke-interface {v8}, Ljava/util/List;->size()I

    .line 552
    .line 553
    .line 554
    move-result v8

    .line 555
    if-ge v6, v8, :cond_1d

    .line 556
    .line 557
    invoke-virtual {v1, v6}, Lfa1;->w(I)Lwt2;

    .line 558
    .line 559
    .line 560
    move-result-object v8

    .line 561
    iget-object v9, v8, Lwt2;->c:Li9;

    .line 562
    .line 563
    sget-object v10, Li9;->j:Li9;

    .line 564
    .line 565
    invoke-virtual {v9, v10}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    .line 566
    .line 567
    .line 568
    move-result v9

    .line 569
    if-eqz v9, :cond_1c

    .line 570
    .line 571
    invoke-virtual {v3, v8}, Lbd;->c(Lwt2;)Ljava/lang/Object;

    .line 572
    .line 573
    .line 574
    move-result-object v9

    .line 575
    check-cast v9, Lt74;

    .line 576
    .line 577
    invoke-virtual {v8}, Lwt2;->a()Lpr0;

    .line 578
    .line 579
    .line 580
    move-result-object v8

    .line 581
    instance-of v9, v8, Lb43;

    .line 582
    .line 583
    if-eqz v9, :cond_17

    .line 584
    .line 585
    check-cast v8, Lb43;

    .line 586
    .line 587
    invoke-virtual {v8}, Lb43;->J()Lg20;

    .line 588
    .line 589
    .line 590
    move-result-object v8

    .line 591
    goto :goto_b

    .line 592
    :cond_17
    instance-of v9, v8, Ljw2;

    .line 593
    .line 594
    if-eqz v9, :cond_1b

    .line 595
    .line 596
    check-cast v8, Ljw2;

    .line 597
    .line 598
    invoke-virtual {v8}, Ljw2;->J()Lg20;

    .line 599
    .line 600
    .line 601
    move-result-object v8

    .line 602
    :goto_b
    new-instance v9, Lqi;

    .line 603
    .line 604
    const/16 v10, 0x10

    .line 605
    .line 606
    invoke-direct {v9, v10}, Lqi;-><init>(I)V

    .line 607
    .line 608
    .line 609
    iget-object v10, v8, Lg20;->a:[B

    .line 610
    .line 611
    array-length v12, v10

    .line 612
    if-eqz v12, :cond_19

    .line 613
    .line 614
    array-length v10, v10

    .line 615
    if-ne v10, v7, :cond_18

    .line 616
    .line 617
    goto :goto_c

    .line 618
    :cond_18
    invoke-static {v5}, Lla6;->x(Ljava/lang/String;)V

    .line 619
    .line 620
    .line 621
    goto/16 :goto_19

    .line 622
    .line 623
    :cond_19
    :goto_c
    invoke-virtual {v0, v8}, Ljava/util/HashMap;->containsKey(Ljava/lang/Object;)Z

    .line 624
    .line 625
    .line 626
    move-result v10

    .line 627
    if-eqz v10, :cond_1a

    .line 628
    .line 629
    invoke-virtual {v0, v8}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 630
    .line 631
    .line 632
    move-result-object v8

    .line 633
    check-cast v8, Ljava/util/List;

    .line 634
    .line 635
    goto :goto_d

    .line 636
    :cond_1a
    new-instance v10, Ljava/util/ArrayList;

    .line 637
    .line 638
    invoke-direct {v10}, Ljava/util/ArrayList;-><init>()V

    .line 639
    .line 640
    .line 641
    invoke-virtual {v0, v8, v10}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 642
    .line 643
    .line 644
    move-object v8, v10

    .line 645
    :goto_d
    invoke-interface {v8, v9}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 646
    .line 647
    .line 648
    goto :goto_e

    .line 649
    :cond_1b
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 650
    .line 651
    invoke-virtual {v8}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 652
    .line 653
    .line 654
    move-result-object v0

    .line 655
    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    .line 656
    .line 657
    .line 658
    move-result-object v0

    .line 659
    invoke-virtual {v8}, Lpr0;->u()Lr04;

    .line 660
    .line 661
    .line 662
    move-result-object v1

    .line 663
    new-instance v3, Ljava/lang/StringBuilder;

    .line 664
    .line 665
    invoke-direct {v3, v4}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 666
    .line 667
    .line 668
    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 669
    .line 670
    .line 671
    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 672
    .line 673
    .line 674
    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 675
    .line 676
    .line 677
    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 678
    .line 679
    .line 680
    move-result-object v0

    .line 681
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 682
    .line 683
    .line 684
    throw p0

    .line 685
    :cond_1c
    :goto_e
    add-int/lit8 v6, v6, 0x1

    .line 686
    .line 687
    goto/16 :goto_a

    .line 688
    .line 689
    :cond_1d
    iget-object p0, p0, Lnf3;->a:Ljava/util/Map;

    .line 690
    .line 691
    invoke-interface {p0}, Ljava/util/Map;->isEmpty()Z

    .line 692
    .line 693
    .line 694
    move-result p0

    .line 695
    if-nez p0, :cond_1e

    .line 696
    .line 697
    sget-object p0, Lzh3;->b:Lzh3;

    .line 698
    .line 699
    invoke-virtual {p0}, Lzh3;->a()Lyh3;

    .line 700
    .line 701
    .line 702
    move-result-object p0

    .line 703
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 704
    .line 705
    .line 706
    :cond_1e
    invoke-virtual {v1}, Lfa1;->y()Lwt2;

    .line 707
    .line 708
    .line 709
    move-result-object p0

    .line 710
    invoke-virtual {v3, p0}, Lbd;->c(Lwt2;)Ljava/lang/Object;

    .line 711
    .line 712
    .line 713
    move-result-object p0

    .line 714
    check-cast p0, Lt74;

    .line 715
    .line 716
    new-instance v11, Lt74;

    .line 717
    .line 718
    invoke-virtual {v1}, Lfa1;->y()Lwt2;

    .line 719
    .line 720
    .line 721
    new-instance p0, Ls64;

    .line 722
    .line 723
    invoke-direct {v11}, Ljava/lang/Object;-><init>()V

    .line 724
    .line 725
    .line 726
    goto/16 :goto_19

    .line 727
    .line 728
    :pswitch_2
    invoke-virtual {v1}, Lfa1;->y()Lwt2;

    .line 729
    .line 730
    .line 731
    move-result-object p0

    .line 732
    new-instance v0, Ljava/util/HashMap;

    .line 733
    .line 734
    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    .line 735
    .line 736
    .line 737
    :goto_f
    iget-object v8, v1, Lfa1;->b:Ljava/lang/Object;

    .line 738
    .line 739
    check-cast v8, Ljava/util/List;

    .line 740
    .line 741
    invoke-interface {v8}, Ljava/util/List;->size()I

    .line 742
    .line 743
    .line 744
    move-result v8

    .line 745
    if-ge v6, v8, :cond_25

    .line 746
    .line 747
    invoke-virtual {v1, v6}, Lfa1;->w(I)Lwt2;

    .line 748
    .line 749
    .line 750
    move-result-object v8

    .line 751
    iget-object v9, v8, Lwt2;->c:Li9;

    .line 752
    .line 753
    sget-object v10, Li9;->j:Li9;

    .line 754
    .line 755
    invoke-virtual {v9, v10}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    .line 756
    .line 757
    .line 758
    move-result v9

    .line 759
    if-eqz v9, :cond_24

    .line 760
    .line 761
    invoke-virtual {v3, v8}, Lbd;->c(Lwt2;)Ljava/lang/Object;

    .line 762
    .line 763
    .line 764
    move-result-object v9

    .line 765
    check-cast v9, Lzf0;

    .line 766
    .line 767
    invoke-virtual {v8}, Lwt2;->a()Lpr0;

    .line 768
    .line 769
    .line 770
    move-result-object v8

    .line 771
    instance-of v10, v8, Lb43;

    .line 772
    .line 773
    if-eqz v10, :cond_1f

    .line 774
    .line 775
    check-cast v8, Lb43;

    .line 776
    .line 777
    invoke-virtual {v8}, Lb43;->J()Lg20;

    .line 778
    .line 779
    .line 780
    move-result-object v8

    .line 781
    goto :goto_10

    .line 782
    :cond_1f
    instance-of v10, v8, Ljw2;

    .line 783
    .line 784
    if-eqz v10, :cond_23

    .line 785
    .line 786
    check-cast v8, Ljw2;

    .line 787
    .line 788
    invoke-virtual {v8}, Ljw2;->J()Lg20;

    .line 789
    .line 790
    .line 791
    move-result-object v8

    .line 792
    :goto_10
    iget-object v10, v8, Lg20;->a:[B

    .line 793
    .line 794
    array-length v12, v10

    .line 795
    if-eqz v12, :cond_21

    .line 796
    .line 797
    array-length v10, v10

    .line 798
    if-ne v10, v7, :cond_20

    .line 799
    .line 800
    goto :goto_11

    .line 801
    :cond_20
    invoke-static {v5}, Lla6;->x(Ljava/lang/String;)V

    .line 802
    .line 803
    .line 804
    goto/16 :goto_19

    .line 805
    .line 806
    :cond_21
    :goto_11
    invoke-virtual {v0, v8}, Ljava/util/HashMap;->containsKey(Ljava/lang/Object;)Z

    .line 807
    .line 808
    .line 809
    move-result v10

    .line 810
    if-eqz v10, :cond_22

    .line 811
    .line 812
    invoke-virtual {v0, v8}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 813
    .line 814
    .line 815
    move-result-object v8

    .line 816
    check-cast v8, Ljava/util/List;

    .line 817
    .line 818
    goto :goto_12

    .line 819
    :cond_22
    new-instance v10, Ljava/util/ArrayList;

    .line 820
    .line 821
    invoke-direct {v10}, Ljava/util/ArrayList;-><init>()V

    .line 822
    .line 823
    .line 824
    invoke-virtual {v0, v8, v10}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 825
    .line 826
    .line 827
    move-object v8, v10

    .line 828
    :goto_12
    invoke-interface {v8, v9}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 829
    .line 830
    .line 831
    goto :goto_13

    .line 832
    :cond_23
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 833
    .line 834
    invoke-virtual {v8}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 835
    .line 836
    .line 837
    move-result-object v0

    .line 838
    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    .line 839
    .line 840
    .line 841
    move-result-object v0

    .line 842
    invoke-virtual {v8}, Lpr0;->u()Lr04;

    .line 843
    .line 844
    .line 845
    move-result-object v1

    .line 846
    new-instance v3, Ljava/lang/StringBuilder;

    .line 847
    .line 848
    invoke-direct {v3, v4}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 849
    .line 850
    .line 851
    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 852
    .line 853
    .line 854
    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 855
    .line 856
    .line 857
    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 858
    .line 859
    .line 860
    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 861
    .line 862
    .line 863
    move-result-object v0

    .line 864
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 865
    .line 866
    .line 867
    throw p0

    .line 868
    :cond_24
    :goto_13
    add-int/lit8 v6, v6, 0x1

    .line 869
    .line 870
    goto/16 :goto_f

    .line 871
    .line 872
    :cond_25
    invoke-virtual {v3, p0}, Lbd;->c(Lwt2;)Ljava/lang/Object;

    .line 873
    .line 874
    .line 875
    move-result-object p0

    .line 876
    check-cast p0, Lzf0;

    .line 877
    .line 878
    new-instance v11, Lag0;

    .line 879
    .line 880
    new-instance p0, Ls64;

    .line 881
    .line 882
    invoke-direct {v11}, Ljava/lang/Object;-><init>()V

    .line 883
    .line 884
    .line 885
    goto/16 :goto_19

    .line 886
    .line 887
    :pswitch_3
    new-instance v0, Ljava/util/HashMap;

    .line 888
    .line 889
    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    .line 890
    .line 891
    .line 892
    :goto_14
    iget-object v8, v1, Lfa1;->b:Ljava/lang/Object;

    .line 893
    .line 894
    check-cast v8, Ljava/util/List;

    .line 895
    .line 896
    invoke-interface {v8}, Ljava/util/List;->size()I

    .line 897
    .line 898
    .line 899
    move-result v8

    .line 900
    if-ge v6, v8, :cond_2c

    .line 901
    .line 902
    invoke-virtual {v1, v6}, Lfa1;->w(I)Lwt2;

    .line 903
    .line 904
    .line 905
    move-result-object v8

    .line 906
    iget-object v9, v8, Lwt2;->c:Li9;

    .line 907
    .line 908
    sget-object v10, Li9;->j:Li9;

    .line 909
    .line 910
    invoke-virtual {v9, v10}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    .line 911
    .line 912
    .line 913
    move-result v9

    .line 914
    if-eqz v9, :cond_2b

    .line 915
    .line 916
    invoke-virtual {v8}, Lwt2;->a()Lpr0;

    .line 917
    .line 918
    .line 919
    move-result-object v9

    .line 920
    instance-of v10, v9, Le7;

    .line 921
    .line 922
    if-eqz v10, :cond_26

    .line 923
    .line 924
    check-cast v9, Le7;

    .line 925
    .line 926
    invoke-virtual {v9}, Le7;->J()Lg20;

    .line 927
    .line 928
    .line 929
    move-result-object v9

    .line 930
    goto :goto_15

    .line 931
    :cond_26
    instance-of v10, v9, Ljw2;

    .line 932
    .line 933
    if-eqz v10, :cond_2a

    .line 934
    .line 935
    check-cast v9, Ljw2;

    .line 936
    .line 937
    invoke-virtual {v9}, Ljw2;->J()Lg20;

    .line 938
    .line 939
    .line 940
    move-result-object v9

    .line 941
    :goto_15
    new-instance v10, Ly97;

    .line 942
    .line 943
    invoke-virtual {v3, v8}, Lbd;->c(Lwt2;)Ljava/lang/Object;

    .line 944
    .line 945
    .line 946
    move-result-object v8

    .line 947
    check-cast v8, Lc7;

    .line 948
    .line 949
    const/4 v8, 0x7

    .line 950
    invoke-direct {v10, v8}, Ly97;-><init>(I)V

    .line 951
    .line 952
    .line 953
    iget-object v8, v9, Lg20;->a:[B

    .line 954
    .line 955
    array-length v12, v8

    .line 956
    if-eqz v12, :cond_28

    .line 957
    .line 958
    array-length v8, v8

    .line 959
    if-ne v8, v7, :cond_27

    .line 960
    .line 961
    goto :goto_16

    .line 962
    :cond_27
    invoke-static {v5}, Lla6;->x(Ljava/lang/String;)V

    .line 963
    .line 964
    .line 965
    goto :goto_19

    .line 966
    :cond_28
    :goto_16
    invoke-virtual {v0, v9}, Ljava/util/HashMap;->containsKey(Ljava/lang/Object;)Z

    .line 967
    .line 968
    .line 969
    move-result v8

    .line 970
    if-eqz v8, :cond_29

    .line 971
    .line 972
    invoke-virtual {v0, v9}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 973
    .line 974
    .line 975
    move-result-object v8

    .line 976
    check-cast v8, Ljava/util/List;

    .line 977
    .line 978
    goto :goto_17

    .line 979
    :cond_29
    new-instance v8, Ljava/util/ArrayList;

    .line 980
    .line 981
    invoke-direct {v8}, Ljava/util/ArrayList;-><init>()V

    .line 982
    .line 983
    .line 984
    invoke-virtual {v0, v9, v8}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 985
    .line 986
    .line 987
    :goto_17
    invoke-interface {v8, v10}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 988
    .line 989
    .line 990
    goto :goto_18

    .line 991
    :cond_2a
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 992
    .line 993
    invoke-virtual {v9}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 994
    .line 995
    .line 996
    move-result-object v0

    .line 997
    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    .line 998
    .line 999
    .line 1000
    move-result-object v0

    .line 1001
    invoke-virtual {v9}, Lpr0;->u()Lr04;

    .line 1002
    .line 1003
    .line 1004
    move-result-object v1

    .line 1005
    new-instance v3, Ljava/lang/StringBuilder;

    .line 1006
    .line 1007
    invoke-direct {v3, v4}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 1008
    .line 1009
    .line 1010
    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 1011
    .line 1012
    .line 1013
    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 1014
    .line 1015
    .line 1016
    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 1017
    .line 1018
    .line 1019
    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 1020
    .line 1021
    .line 1022
    move-result-object v0

    .line 1023
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 1024
    .line 1025
    .line 1026
    throw p0

    .line 1027
    :cond_2b
    :goto_18
    add-int/lit8 v6, v6, 0x1

    .line 1028
    .line 1029
    goto/16 :goto_14

    .line 1030
    .line 1031
    :cond_2c
    iget-object p0, p0, Lnf3;->a:Ljava/util/Map;

    .line 1032
    .line 1033
    invoke-interface {p0}, Ljava/util/Map;->isEmpty()Z

    .line 1034
    .line 1035
    .line 1036
    move-result p0

    .line 1037
    if-nez p0, :cond_2d

    .line 1038
    .line 1039
    sget-object p0, Lzh3;->b:Lzh3;

    .line 1040
    .line 1041
    invoke-virtual {p0}, Lzh3;->a()Lyh3;

    .line 1042
    .line 1043
    .line 1044
    move-result-object p0

    .line 1045
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 1046
    .line 1047
    .line 1048
    :cond_2d
    new-instance v11, Lg7;

    .line 1049
    .line 1050
    invoke-virtual {v1}, Lfa1;->y()Lwt2;

    .line 1051
    .line 1052
    .line 1053
    move-result-object p0

    .line 1054
    invoke-virtual {v3, p0}, Lbd;->c(Lwt2;)Ljava/lang/Object;

    .line 1055
    .line 1056
    .line 1057
    move-result-object p0

    .line 1058
    check-cast p0, Lc7;

    .line 1059
    .line 1060
    invoke-virtual {v1}, Lfa1;->y()Lwt2;

    .line 1061
    .line 1062
    .line 1063
    new-instance p0, Ls64;

    .line 1064
    .line 1065
    invoke-direct {v11}, Ljava/lang/Object;-><init>()V

    .line 1066
    .line 1067
    .line 1068
    :goto_19
    return-object v11

    .line 1069
    :cond_2e
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 1070
    .line 1071
    const-string v0, "No wrapper found for "

    .line 1072
    .line 1073
    invoke-static {v3, v0}, Lat1;->q(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/String;

    .line 1074
    .line 1075
    .line 1076
    move-result-object v0

    .line 1077
    invoke-direct {p0, v0}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 1078
    .line 1079
    .line 1080
    throw p0

    .line 1081
    :cond_2f
    const-string p0, "keyset must contain at least one ENABLED key"

    .line 1082
    .line 1083
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 1084
    .line 1085
    .line 1086
    return-object v11

    .line 1087
    :pswitch_data_0
    .packed-switch 0x0
        :pswitch_3
        :pswitch_2
        :pswitch_1
        :pswitch_0
    .end packed-switch
.end method
