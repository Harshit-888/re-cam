.class public final Lt41;
.super Ljava/lang/Object;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"

# interfaces
.implements Lf30;
.implements Lrq1;
.implements Lc95;
.implements Lu4;


# static fields
.field public static final e:[Ljava/lang/String;


# instance fields
.field public a:Ljava/lang/Object;

.field public b:Ljava/lang/Object;

.field public c:Ljava/lang/Object;

.field public d:Ljava/lang/Object;


# direct methods
.method static constructor <clinit>()V
    .locals 3

    .line 1
    const-string v0, "key"

    .line 2
    .line 3
    const-string v1, "metadata"

    .line 4
    .line 5
    const-string v2, "id"

    .line 6
    .line 7
    filled-new-array {v2, v0, v1}, [Ljava/lang/String;

    .line 8
    .line 9
    .line 10
    move-result-object v0

    .line 11
    sput-object v0, Lt41;->e:[Ljava/lang/String;

    .line 12
    .line 13
    return-void
.end method

.method public constructor <init>(IZ)V
    .locals 0

    sparse-switch p1, :sswitch_data_0

    .line 211
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 212
    new-instance p1, Ld54;

    const/16 p2, 0xa

    invoke-direct {p1, p2}, Ld54;-><init>(I)V

    iput-object p1, p0, Lt41;->a:Ljava/lang/Object;

    .line 213
    new-instance p1, Lx05;

    const/4 p2, 0x0

    .line 214
    invoke-direct {p1, p2}, Lx05;-><init>(I)V

    .line 215
    iput-object p1, p0, Lt41;->b:Ljava/lang/Object;

    .line 216
    new-instance p1, Ljava/util/ArrayList;

    invoke-direct {p1}, Ljava/util/ArrayList;-><init>()V

    iput-object p1, p0, Lt41;->c:Ljava/lang/Object;

    .line 217
    new-instance p1, Ljava/util/HashSet;

    invoke-direct {p1}, Ljava/util/HashSet;-><init>()V

    iput-object p1, p0, Lt41;->d:Ljava/lang/Object;

    return-void

    .line 218
    :sswitch_0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 219
    new-instance p1, Lli;

    const/4 p2, 0x0

    .line 220
    invoke-direct {p1, p2}, Lx05;-><init>(I)V

    .line 221
    iput-object p1, p0, Lt41;->a:Ljava/lang/Object;

    .line 222
    new-instance p1, Landroid/util/SparseArray;

    invoke-direct {p1}, Landroid/util/SparseArray;-><init>()V

    iput-object p1, p0, Lt41;->b:Ljava/lang/Object;

    .line 223
    new-instance p1, La33;

    invoke-direct {p1}, La33;-><init>()V

    iput-object p1, p0, Lt41;->c:Ljava/lang/Object;

    .line 224
    new-instance p1, Lli;

    .line 225
    invoke-direct {p1, p2}, Lx05;-><init>(I)V

    .line 226
    iput-object p1, p0, Lt41;->d:Ljava/lang/Object;

    return-void

    .line 227
    :sswitch_1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 228
    new-instance p1, La14;

    invoke-direct {p1}, La14;-><init>()V

    iput-object p1, p0, Lt41;->a:Ljava/lang/Object;

    .line 229
    new-instance p1, La14;

    invoke-direct {p1}, La14;-><init>()V

    iput-object p1, p0, Lt41;->b:Ljava/lang/Object;

    .line 230
    new-instance p1, Lh24;

    invoke-direct {p1}, Lh24;-><init>()V

    iput-object p1, p0, Lt41;->c:Ljava/lang/Object;

    return-void

    :sswitch_data_0
    .sparse-switch
        0xd -> :sswitch_1
        0x12 -> :sswitch_0
    .end sparse-switch
.end method

.method public constructor <init>(Landroid/graphics/Typeface;Lte3;)V
    .locals 7

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    .line 3
    .line 4
    iput-object p1, p0, Lt41;->d:Ljava/lang/Object;

    .line 5
    .line 6
    iput-object p2, p0, Lt41;->a:Ljava/lang/Object;

    .line 7
    .line 8
    new-instance p1, Lve3;

    .line 9
    .line 10
    const/16 v0, 0x400

    .line 11
    .line 12
    invoke-direct {p1, v0}, Lve3;-><init>(I)V

    .line 13
    .line 14
    .line 15
    iput-object p1, p0, Lt41;->c:Ljava/lang/Object;

    .line 16
    .line 17
    const/4 p1, 0x6

    .line 18
    invoke-virtual {p2, p1}, Ln53;->a(I)I

    .line 19
    .line 20
    .line 21
    move-result v0

    .line 22
    const/4 v1, 0x0

    .line 23
    if-eqz v0, :cond_0

    .line 24
    .line 25
    iget v2, p2, Ln53;->a:I

    .line 26
    .line 27
    add-int/2addr v0, v2

    .line 28
    iget-object v2, p2, Ln53;->d:Ljava/lang/Object;

    .line 29
    .line 30
    check-cast v2, Ljava/nio/ByteBuffer;

    .line 31
    .line 32
    invoke-virtual {v2, v0}, Ljava/nio/ByteBuffer;->getInt(I)I

    .line 33
    .line 34
    .line 35
    move-result v2

    .line 36
    add-int/2addr v2, v0

    .line 37
    iget-object v0, p2, Ln53;->d:Ljava/lang/Object;

    .line 38
    .line 39
    check-cast v0, Ljava/nio/ByteBuffer;

    .line 40
    .line 41
    invoke-virtual {v0, v2}, Ljava/nio/ByteBuffer;->getInt(I)I

    .line 42
    .line 43
    .line 44
    move-result v0

    .line 45
    goto :goto_0

    .line 46
    :cond_0
    move v0, v1

    .line 47
    :goto_0
    mul-int/lit8 v0, v0, 0x2

    .line 48
    .line 49
    new-array v0, v0, [C

    .line 50
    .line 51
    iput-object v0, p0, Lt41;->b:Ljava/lang/Object;

    .line 52
    .line 53
    invoke-virtual {p2, p1}, Ln53;->a(I)I

    .line 54
    .line 55
    .line 56
    move-result p1

    .line 57
    if-eqz p1, :cond_1

    .line 58
    .line 59
    iget v0, p2, Ln53;->a:I

    .line 60
    .line 61
    add-int/2addr p1, v0

    .line 62
    iget-object v0, p2, Ln53;->d:Ljava/lang/Object;

    .line 63
    .line 64
    check-cast v0, Ljava/nio/ByteBuffer;

    .line 65
    .line 66
    invoke-virtual {v0, p1}, Ljava/nio/ByteBuffer;->getInt(I)I

    .line 67
    .line 68
    .line 69
    move-result v0

    .line 70
    add-int/2addr v0, p1

    .line 71
    iget-object p1, p2, Ln53;->d:Ljava/lang/Object;

    .line 72
    .line 73
    check-cast p1, Ljava/nio/ByteBuffer;

    .line 74
    .line 75
    invoke-virtual {p1, v0}, Ljava/nio/ByteBuffer;->getInt(I)I

    .line 76
    .line 77
    .line 78
    move-result p1

    .line 79
    goto :goto_1

    .line 80
    :cond_1
    move p1, v1

    .line 81
    :goto_1
    move p2, v1

    .line 82
    :goto_2
    if-ge p2, p1, :cond_6

    .line 83
    .line 84
    new-instance v0, Lrl5;

    .line 85
    .line 86
    invoke-direct {v0, p0, p2}, Lrl5;-><init>(Lt41;I)V

    .line 87
    .line 88
    .line 89
    invoke-virtual {v0}, Lrl5;->b()Lse3;

    .line 90
    .line 91
    .line 92
    move-result-object v2

    .line 93
    const/4 v3, 0x4

    .line 94
    invoke-virtual {v2, v3}, Ln53;->a(I)I

    .line 95
    .line 96
    .line 97
    move-result v3

    .line 98
    if-eqz v3, :cond_2

    .line 99
    .line 100
    iget-object v4, v2, Ln53;->d:Ljava/lang/Object;

    .line 101
    .line 102
    check-cast v4, Ljava/nio/ByteBuffer;

    .line 103
    .line 104
    iget v2, v2, Ln53;->a:I

    .line 105
    .line 106
    add-int/2addr v3, v2

    .line 107
    invoke-virtual {v4, v3}, Ljava/nio/ByteBuffer;->getInt(I)I

    .line 108
    .line 109
    .line 110
    move-result v2

    .line 111
    goto :goto_3

    .line 112
    :cond_2
    move v2, v1

    .line 113
    :goto_3
    iget-object v3, p0, Lt41;->b:Ljava/lang/Object;

    .line 114
    .line 115
    check-cast v3, [C

    .line 116
    .line 117
    mul-int/lit8 v4, p2, 0x2

    .line 118
    .line 119
    invoke-static {v2, v3, v4}, Ljava/lang/Character;->toChars(I[CI)I

    .line 120
    .line 121
    .line 122
    invoke-virtual {v0}, Lrl5;->b()Lse3;

    .line 123
    .line 124
    .line 125
    move-result-object v2

    .line 126
    const/16 v3, 0x10

    .line 127
    .line 128
    invoke-virtual {v2, v3}, Ln53;->a(I)I

    .line 129
    .line 130
    .line 131
    move-result v4

    .line 132
    if-eqz v4, :cond_3

    .line 133
    .line 134
    iget v5, v2, Ln53;->a:I

    .line 135
    .line 136
    add-int/2addr v4, v5

    .line 137
    iget-object v5, v2, Ln53;->d:Ljava/lang/Object;

    .line 138
    .line 139
    check-cast v5, Ljava/nio/ByteBuffer;

    .line 140
    .line 141
    invoke-virtual {v5, v4}, Ljava/nio/ByteBuffer;->getInt(I)I

    .line 142
    .line 143
    .line 144
    move-result v5

    .line 145
    add-int/2addr v5, v4

    .line 146
    iget-object v2, v2, Ln53;->d:Ljava/lang/Object;

    .line 147
    .line 148
    check-cast v2, Ljava/nio/ByteBuffer;

    .line 149
    .line 150
    invoke-virtual {v2, v5}, Ljava/nio/ByteBuffer;->getInt(I)I

    .line 151
    .line 152
    .line 153
    move-result v2

    .line 154
    goto :goto_4

    .line 155
    :cond_3
    move v2, v1

    .line 156
    :goto_4
    const/4 v4, 0x1

    .line 157
    if-lez v2, :cond_4

    .line 158
    .line 159
    move v2, v4

    .line 160
    goto :goto_5

    .line 161
    :cond_4
    move v2, v1

    .line 162
    :goto_5
    const-string v5, "invalid metadata codepoint length"

    .line 163
    .line 164
    invoke-static {v2, v5}, Lgy0;->e(ZLjava/lang/String;)V

    .line 165
    .line 166
    .line 167
    iget-object v2, p0, Lt41;->c:Ljava/lang/Object;

    .line 168
    .line 169
    check-cast v2, Lve3;

    .line 170
    .line 171
    invoke-virtual {v0}, Lrl5;->b()Lse3;

    .line 172
    .line 173
    .line 174
    move-result-object v5

    .line 175
    invoke-virtual {v5, v3}, Ln53;->a(I)I

    .line 176
    .line 177
    .line 178
    move-result v3

    .line 179
    if-eqz v3, :cond_5

    .line 180
    .line 181
    iget v6, v5, Ln53;->a:I

    .line 182
    .line 183
    add-int/2addr v3, v6

    .line 184
    iget-object v6, v5, Ln53;->d:Ljava/lang/Object;

    .line 185
    .line 186
    check-cast v6, Ljava/nio/ByteBuffer;

    .line 187
    .line 188
    invoke-virtual {v6, v3}, Ljava/nio/ByteBuffer;->getInt(I)I

    .line 189
    .line 190
    .line 191
    move-result v6

    .line 192
    add-int/2addr v6, v3

    .line 193
    iget-object v3, v5, Ln53;->d:Ljava/lang/Object;

    .line 194
    .line 195
    check-cast v3, Ljava/nio/ByteBuffer;

    .line 196
    .line 197
    invoke-virtual {v3, v6}, Ljava/nio/ByteBuffer;->getInt(I)I

    .line 198
    .line 199
    .line 200
    move-result v3

    .line 201
    goto :goto_6

    .line 202
    :cond_5
    move v3, v1

    .line 203
    :goto_6
    sub-int/2addr v3, v4

    .line 204
    invoke-virtual {v2, v0, v1, v3}, Lve3;->a(Lrl5;II)V

    .line 205
    .line 206
    .line 207
    add-int/lit8 p2, p2, 0x1

    .line 208
    .line 209
    goto :goto_2

    .line 210
    :cond_6
    return-void
.end method

.method public synthetic constructor <init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    .locals 0

    .line 231
    iput-object p1, p0, Lt41;->a:Ljava/lang/Object;

    iput-object p2, p0, Lt41;->b:Ljava/lang/Object;

    iput-object p3, p0, Lt41;->c:Ljava/lang/Object;

    iput-object p4, p0, Lt41;->d:Ljava/lang/Object;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static F(Landroid/graphics/Bitmap;Lcom/indymobile/app/model/PSPage;)Landroid/graphics/Bitmap;
    .locals 19

    .line 1
    const v0, 0x186a0

    .line 2
    .line 3
    .line 4
    if-nez p0, :cond_0

    .line 5
    .line 6
    invoke-virtual/range {p1 .. p1}, Lcom/indymobile/app/model/PSPage;->c()Ljava/io/File;

    .line 7
    .line 8
    .line 9
    move-result-object v1

    .line 10
    invoke-virtual {v1}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    .line 11
    .line 12
    .line 13
    move-result-object v1

    .line 14
    invoke-static {v0, v1}, Ls46;->l(ILjava/lang/String;)Landroid/graphics/Bitmap;

    .line 15
    .line 16
    .line 17
    move-result-object v1

    .line 18
    const/4 v2, 0x1

    .line 19
    goto :goto_0

    .line 20
    :cond_0
    const/4 v2, 0x0

    .line 21
    move-object/from16 v1, p0

    .line 22
    .line 23
    :goto_0
    invoke-virtual/range {p1 .. p1}, Lcom/indymobile/app/model/PSPage;->k()Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 24
    .line 25
    .line 26
    move-result-object v3

    .line 27
    iget-boolean v4, v3, Lcom/indymobile/app/model/PSDocumentProcessInfo;->fixToFitImage:Z

    .line 28
    .line 29
    if-eqz v4, :cond_3

    .line 30
    .line 31
    invoke-virtual {v1}, Landroid/graphics/Bitmap;->getWidth()I

    .line 32
    .line 33
    .line 34
    move-result v3

    .line 35
    if-gt v3, v0, :cond_2

    .line 36
    .line 37
    invoke-virtual {v1}, Landroid/graphics/Bitmap;->getHeight()I

    .line 38
    .line 39
    .line 40
    move-result v3

    .line 41
    if-le v3, v0, :cond_1

    .line 42
    .line 43
    goto :goto_1

    .line 44
    :cond_1
    move-object v0, v1

    .line 45
    goto :goto_2

    .line 46
    :cond_2
    :goto_1
    invoke-static {v1, v0, v0}, Ls46;->L(Landroid/graphics/Bitmap;II)Landroid/graphics/Bitmap;

    .line 47
    .line 48
    .line 49
    move-result-object v0

    .line 50
    goto :goto_2

    .line 51
    :cond_3
    new-instance v0, Lorg/opencv/core/Mat;

    .line 52
    .line 53
    invoke-direct {v0}, Lorg/opencv/core/Mat;-><init>()V

    .line 54
    .line 55
    .line 56
    new-instance v4, Lorg/opencv/core/Mat;

    .line 57
    .line 58
    invoke-direct {v4}, Lorg/opencv/core/Mat;-><init>()V

    .line 59
    .line 60
    .line 61
    invoke-static {v1, v0}, Ls46;->e(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 62
    .line 63
    .line 64
    iget-object v3, v3, Lcom/indymobile/app/model/PSDocumentProcessInfo;->userSelectRect:Lcom/indymobile/app/model/PSRect;

    .line 65
    .line 66
    sget-object v5, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 67
    .line 68
    iget-object v6, v3, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 69
    .line 70
    iget v10, v6, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 71
    .line 72
    iget v11, v6, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 73
    .line 74
    iget-object v6, v3, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 75
    .line 76
    iget v12, v6, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 77
    .line 78
    iget v13, v6, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 79
    .line 80
    iget-object v6, v3, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 81
    .line 82
    iget v14, v6, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 83
    .line 84
    iget v15, v6, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 85
    .line 86
    iget-object v3, v3, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 87
    .line 88
    iget v6, v3, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 89
    .line 90
    iget v3, v3, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 91
    .line 92
    const v18, 0x47c35000    # 100000.0f

    .line 93
    .line 94
    .line 95
    iget-wide v7, v0, Lorg/opencv/core/Mat;->a:J

    .line 96
    .line 97
    move/from16 v16, v6

    .line 98
    .line 99
    move-wide v6, v7

    .line 100
    iget-wide v8, v4, Lorg/opencv/core/Mat;->a:J

    .line 101
    .line 102
    move/from16 v17, v3

    .line 103
    .line 104
    invoke-static/range {v5 .. v18}, Lcom/indymobile/app/PSOpenCV;->perspectiveTransformRect(Landroid/content/Context;JJFFFFFFFFF)V

    .line 105
    .line 106
    .line 107
    invoke-virtual {v4}, Lorg/opencv/core/Mat;->a()I

    .line 108
    .line 109
    .line 110
    move-result v0

    .line 111
    invoke-virtual {v4}, Lorg/opencv/core/Mat;->e()I

    .line 112
    .line 113
    .line 114
    move-result v3

    .line 115
    sget-object v5, Landroid/graphics/Bitmap$Config;->ARGB_8888:Landroid/graphics/Bitmap$Config;

    .line 116
    .line 117
    invoke-static {v0, v3, v5}, Landroid/graphics/Bitmap;->createBitmap(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;

    .line 118
    .line 119
    .line 120
    move-result-object v0

    .line 121
    invoke-static {v0, v4}, Lorg/opencv/android/Utils;->b(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 122
    .line 123
    .line 124
    :goto_2
    if-eqz v2, :cond_4

    .line 125
    .line 126
    if-eq v0, v1, :cond_4

    .line 127
    .line 128
    invoke-static {v1}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 129
    .line 130
    .line 131
    :cond_4
    return-object v0
.end method

.method public static final a(Lt41;Ljava/util/ArrayList;I)Landroid/os/Message;
    .locals 5

    .line 1
    new-instance p0, Ljava/util/ArrayList;

    .line 2
    .line 3
    invoke-direct {p0}, Ljava/util/ArrayList;-><init>()V

    .line 4
    .line 5
    .line 6
    invoke-virtual {p1}, Ljava/util/ArrayList;->size()I

    .line 7
    .line 8
    .line 9
    move-result v0

    .line 10
    const/4 v1, 0x0

    .line 11
    :cond_0
    :goto_0
    if-ge v1, v0, :cond_1

    .line 12
    .line 13
    invoke-virtual {p1, v1}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    .line 14
    .line 15
    .line 16
    move-result-object v2

    .line 17
    add-int/lit8 v1, v1, 0x1

    .line 18
    .line 19
    move-object v3, v2

    .line 20
    check-cast v3, Landroid/os/Message;

    .line 21
    .line 22
    iget v3, v3, Landroid/os/Message;->what:I

    .line 23
    .line 24
    if-ne v3, p2, :cond_0

    .line 25
    .line 26
    invoke-virtual {p0, v2}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 27
    .line 28
    .line 29
    goto :goto_0

    .line 30
    :cond_1
    invoke-virtual {p0}, Ljava/util/ArrayList;->iterator()Ljava/util/Iterator;

    .line 31
    .line 32
    .line 33
    move-result-object p0

    .line 34
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    .line 35
    .line 36
    .line 37
    move-result p1

    .line 38
    if-nez p1, :cond_2

    .line 39
    .line 40
    const/4 p0, 0x0

    .line 41
    goto :goto_2

    .line 42
    :cond_2
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 43
    .line 44
    .line 45
    move-result-object p1

    .line 46
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    .line 47
    .line 48
    .line 49
    move-result p2

    .line 50
    if-nez p2, :cond_3

    .line 51
    .line 52
    :goto_1
    move-object p0, p1

    .line 53
    goto :goto_2

    .line 54
    :cond_3
    move-object p2, p1

    .line 55
    check-cast p2, Landroid/os/Message;

    .line 56
    .line 57
    invoke-virtual {p2}, Landroid/os/Message;->getWhen()J

    .line 58
    .line 59
    .line 60
    move-result-wide v0

    .line 61
    :cond_4
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 62
    .line 63
    .line 64
    move-result-object p2

    .line 65
    move-object v2, p2

    .line 66
    check-cast v2, Landroid/os/Message;

    .line 67
    .line 68
    invoke-virtual {v2}, Landroid/os/Message;->getWhen()J

    .line 69
    .line 70
    .line 71
    move-result-wide v2

    .line 72
    cmp-long v4, v0, v2

    .line 73
    .line 74
    if-gez v4, :cond_5

    .line 75
    .line 76
    move-object p1, p2

    .line 77
    move-wide v0, v2

    .line 78
    :cond_5
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    .line 79
    .line 80
    .line 81
    move-result p2

    .line 82
    if-nez p2, :cond_4

    .line 83
    .line 84
    goto :goto_1

    .line 85
    :goto_2
    check-cast p0, Landroid/os/Message;

    .line 86
    .line 87
    return-object p0
.end method

.method public static v(Lzl;)Landroid/os/Bundle;
    .locals 3

    .line 1
    new-instance v0, Landroid/os/Bundle;

    .line 2
    .line 3
    invoke-direct {v0}, Landroid/os/Bundle;-><init>()V

    .line 4
    .line 5
    .line 6
    const-string v1, "com.microsoft.aad.adal:RequestId"

    .line 7
    .line 8
    iget v2, p0, Lzl;->a:I

    .line 9
    .line 10
    invoke-virtual {v0, v1, v2}, Landroid/os/BaseBundle;->putInt(Ljava/lang/String;I)V

    .line 11
    .line 12
    .line 13
    const-string v1, "account.authority"

    .line 14
    .line 15
    iget-object v2, p0, Lzl;->b:Ljava/lang/String;

    .line 16
    .line 17
    invoke-virtual {v0, v1, v2}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 18
    .line 19
    .line 20
    const-string v1, "account.resource"

    .line 21
    .line 22
    iget-object v2, p0, Lzl;->d:Ljava/lang/String;

    .line 23
    .line 24
    invoke-virtual {v0, v1, v2}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 25
    .line 26
    .line 27
    const-string v1, "account.redirect"

    .line 28
    .line 29
    iget-object v2, p0, Lzl;->c:Ljava/lang/String;

    .line 30
    .line 31
    invoke-virtual {v0, v1, v2}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 32
    .line 33
    .line 34
    const-string v1, "account.clientid.key"

    .line 35
    .line 36
    iget-object v2, p0, Lzl;->e:Ljava/lang/String;

    .line 37
    .line 38
    invoke-virtual {v0, v1, v2}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 39
    .line 40
    .line 41
    const-string v1, "adal.version.key"

    .line 42
    .line 43
    iget-object v2, p0, Lzl;->l:Ljava/lang/String;

    .line 44
    .line 45
    invoke-virtual {v0, v1, v2}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 46
    .line 47
    .line 48
    const-string v1, "account.extra.query.param"

    .line 49
    .line 50
    const/4 v2, 0x0

    .line 51
    invoke-virtual {v0, v1, v2}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 52
    .line 53
    .line 54
    iget-object v1, p0, Lzl;->i:Ljava/util/UUID;

    .line 55
    .line 56
    if-eqz v1, :cond_0

    .line 57
    .line 58
    const-string v2, "account.correlationid"

    .line 59
    .line 60
    invoke-virtual {v1}, Ljava/util/UUID;->toString()Ljava/lang/String;

    .line 61
    .line 62
    .line 63
    move-result-object v1

    .line 64
    invoke-virtual {v0, v2, v1}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 65
    .line 66
    .line 67
    :cond_0
    iget-object v1, p0, Lzl;->h:Ljava/lang/String;

    .line 68
    .line 69
    invoke-static {v1}, Lx22;->a(Ljava/lang/String;)Z

    .line 70
    .line 71
    .line 72
    move-result v2

    .line 73
    if-eqz v2, :cond_1

    .line 74
    .line 75
    iget-object v1, p0, Lzl;->f:Ljava/lang/String;

    .line 76
    .line 77
    :cond_1
    const-string v2, "account.login.hint"

    .line 78
    .line 79
    invoke-virtual {v0, v2, v1}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 80
    .line 81
    .line 82
    const-string v2, "account.name"

    .line 83
    .line 84
    invoke-virtual {v0, v2, v1}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 85
    .line 86
    .line 87
    iget-object p0, p0, Lzl;->j:Lka4;

    .line 88
    .line 89
    if-eqz p0, :cond_2

    .line 90
    .line 91
    const-string v1, "account.prompt"

    .line 92
    .line 93
    invoke-virtual {p0}, Ljava/lang/Enum;->name()Ljava/lang/String;

    .line 94
    .line 95
    .line 96
    move-result-object p0

    .line 97
    invoke-virtual {v0, v1, p0}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 98
    .line 99
    .line 100
    :cond_2
    return-object v0
.end method

.method public static x(Landroid/os/Bundle;)Lcm;
    .locals 9

    .line 1
    const/4 v0, 0x0

    .line 2
    if-eqz p0, :cond_5

    .line 3
    .line 4
    const-string v1, "errorCode"

    .line 5
    .line 6
    invoke-virtual {p0, v1}, Landroid/os/BaseBundle;->getInt(Ljava/lang/String;)I

    .line 7
    .line 8
    .line 9
    move-result v1

    .line 10
    const-string v2, "errorMessage"

    .line 11
    .line 12
    invoke-virtual {p0, v2}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;

    .line 13
    .line 14
    .line 15
    move-result-object v2

    .line 16
    invoke-static {v2}, Lx22;->a(Ljava/lang/String;)Z

    .line 17
    .line 18
    .line 19
    move-result v3

    .line 20
    if-nez v3, :cond_3

    .line 21
    .line 22
    sget-object p0, Lf;->BROKER_AUTHENTICATOR_ERROR_GETAUTHTOKEN:Lf;

    .line 23
    .line 24
    const/4 v0, 0x6

    .line 25
    if-eq v1, v0, :cond_2

    .line 26
    .line 27
    const/4 v0, 0x7

    .line 28
    if-eq v1, v0, :cond_1

    .line 29
    .line 30
    const/16 v0, 0x9

    .line 31
    .line 32
    if-eq v1, v0, :cond_0

    .line 33
    .line 34
    goto :goto_0

    .line 35
    :cond_0
    sget-object p0, Lf;->BROKER_AUTHENTICATOR_BAD_AUTHENTICATION:Lf;

    .line 36
    .line 37
    goto :goto_0

    .line 38
    :cond_1
    sget-object p0, Lf;->BROKER_AUTHENTICATOR_BAD_ARGUMENTS:Lf;

    .line 39
    .line 40
    goto :goto_0

    .line 41
    :cond_2
    sget-object p0, Lf;->BROKER_AUTHENTICATOR_UNSUPPORTED_OPERATION:Lf;

    .line 42
    .line 43
    :goto_0
    new-instance v0, Lcom/microsoft/aad/adal/AuthenticationException;

    .line 44
    .line 45
    invoke-direct {v0, p0, v2}, Lcom/microsoft/aad/adal/AuthenticationException;-><init>(Lf;Ljava/lang/String;)V

    .line 46
    .line 47
    .line 48
    throw v0

    .line 49
    :cond_3
    const-string v1, "account.initial.request"

    .line 50
    .line 51
    invoke-virtual {p0, v1}, Landroid/os/BaseBundle;->getBoolean(Ljava/lang/String;)Z

    .line 52
    .line 53
    .line 54
    move-result v1

    .line 55
    if-eqz v1, :cond_4

    .line 56
    .line 57
    new-instance p0, Lcm;

    .line 58
    .line 59
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 60
    .line 61
    .line 62
    sget-object v1, Lbm;->Failed:Lbm;

    .line 63
    .line 64
    iput-object v1, p0, Lcm;->l:Lbm;

    .line 65
    .line 66
    iput-object v0, p0, Lcm;->a:Ljava/lang/String;

    .line 67
    .line 68
    const/4 v0, 0x1

    .line 69
    iput-boolean v0, p0, Lcm;->m:Z

    .line 70
    .line 71
    return-object p0

    .line 72
    :cond_4
    invoke-static {p0}, Ldq5;->j(Landroid/os/Bundle;)Ldq5;

    .line 73
    .line 74
    .line 75
    move-result-object v6

    .line 76
    new-instance v1, Lcm;

    .line 77
    .line 78
    const-string v0, "authtoken"

    .line 79
    .line 80
    invoke-virtual {p0, v0}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;

    .line 81
    .line 82
    .line 83
    move-result-object v2

    .line 84
    const-string v7, ""

    .line 85
    .line 86
    const-string v8, ""

    .line 87
    .line 88
    const-string v3, ""

    .line 89
    .line 90
    const/4 v4, 0x0

    .line 91
    const/4 v5, 0x0

    .line 92
    invoke-direct/range {v1 .. v8}, Lcm;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/util/Date;ZLdq5;Ljava/lang/String;Ljava/lang/String;)V

    .line 93
    .line 94
    .line 95
    return-object v1

    .line 96
    :cond_5
    const-string p0, "bundleResult"

    .line 97
    .line 98
    invoke-static {p0}, Lla;->m(Ljava/lang/String;)V

    .line 99
    .line 100
    .line 101
    return-object v0
.end method


# virtual methods
.method public A(Lwr0;)Ljava/lang/Object;
    .locals 8

    .line 1
    instance-of v0, p1, Lfo4;

    .line 2
    .line 3
    if-eqz v0, :cond_0

    .line 4
    .line 5
    move-object v0, p1

    .line 6
    check-cast v0, Lfo4;

    .line 7
    .line 8
    iget v1, v0, Lfo4;->x:I

    .line 9
    .line 10
    const/high16 v2, -0x80000000

    .line 11
    .line 12
    and-int v3, v1, v2

    .line 13
    .line 14
    if-eqz v3, :cond_0

    .line 15
    .line 16
    sub-int/2addr v1, v2

    .line 17
    iput v1, v0, Lfo4;->x:I

    .line 18
    .line 19
    goto :goto_0

    .line 20
    :cond_0
    new-instance v0, Lfo4;

    .line 21
    .line 22
    invoke-direct {v0, p0, p1}, Lfo4;-><init>(Lt41;Lwr0;)V

    .line 23
    .line 24
    .line 25
    :goto_0
    iget-object p1, v0, Lfo4;->v:Ljava/lang/Object;

    .line 26
    .line 27
    sget-object v1, Lzs0;->COROUTINE_SUSPENDED:Lzs0;

    .line 28
    .line 29
    iget v2, v0, Lfo4;->x:I

    .line 30
    .line 31
    const/4 v3, 0x2

    .line 32
    const/4 v4, 0x1

    .line 33
    sget-object v5, Len5;->a:Len5;

    .line 34
    .line 35
    const/4 v6, 0x0

    .line 36
    if-eqz v2, :cond_3

    .line 37
    .line 38
    if-eq v2, v4, :cond_2

    .line 39
    .line 40
    if-ne v2, v3, :cond_1

    .line 41
    .line 42
    iget-object p0, v0, Lfo4;->u:Lji3;

    .line 43
    .line 44
    iget-object v0, v0, Lfo4;->t:Lt41;

    .line 45
    .line 46
    :try_start_0
    invoke-static {p1}, Ltq3;->I(Ljava/lang/Object;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 47
    .line 48
    .line 49
    goto :goto_3

    .line 50
    :catchall_0
    move-exception p1

    .line 51
    goto :goto_4

    .line 52
    :cond_1
    const-string p0, "call to \'resume\' before \'invoke\' with coroutine"

    .line 53
    .line 54
    invoke-static {p0}, Lr05;->k(Ljava/lang/String;)V

    .line 55
    .line 56
    .line 57
    return-object v6

    .line 58
    :cond_2
    iget-object p0, v0, Lfo4;->u:Lji3;

    .line 59
    .line 60
    iget-object v2, v0, Lfo4;->t:Lt41;

    .line 61
    .line 62
    invoke-static {p1}, Ltq3;->I(Ljava/lang/Object;)V

    .line 63
    .line 64
    .line 65
    move-object p1, p0

    .line 66
    move-object p0, v2

    .line 67
    goto :goto_1

    .line 68
    :cond_3
    invoke-static {p1}, Ltq3;->I(Ljava/lang/Object;)V

    .line 69
    .line 70
    .line 71
    iget-object p1, p0, Lt41;->b:Ljava/lang/Object;

    .line 72
    .line 73
    check-cast p1, Lem0;

    .line 74
    .line 75
    invoke-virtual {p1}, Lxn2;->J()Z

    .line 76
    .line 77
    .line 78
    move-result p1

    .line 79
    if-eqz p1, :cond_4

    .line 80
    .line 81
    return-object v5

    .line 82
    :cond_4
    iget-object p1, p0, Lt41;->a:Ljava/lang/Object;

    .line 83
    .line 84
    check-cast p1, Lli3;

    .line 85
    .line 86
    iput-object p0, v0, Lfo4;->t:Lt41;

    .line 87
    .line 88
    iput-object p1, v0, Lfo4;->u:Lji3;

    .line 89
    .line 90
    iput v4, v0, Lfo4;->x:I

    .line 91
    .line 92
    invoke-virtual {p1, v0}, Lli3;->e(Lur0;)Ljava/lang/Object;

    .line 93
    .line 94
    .line 95
    move-result-object v2

    .line 96
    if-ne v2, v1, :cond_5

    .line 97
    .line 98
    goto :goto_2

    .line 99
    :cond_5
    :goto_1
    :try_start_1
    iget-object v2, p0, Lt41;->b:Ljava/lang/Object;

    .line 100
    .line 101
    check-cast v2, Lem0;

    .line 102
    .line 103
    invoke-virtual {v2}, Lxn2;->J()Z

    .line 104
    .line 105
    .line 106
    move-result v2
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    .line 107
    if-eqz v2, :cond_6

    .line 108
    .line 109
    check-cast p1, Lli3;

    .line 110
    .line 111
    invoke-virtual {p1, v6}, Lli3;->h(Ljava/lang/Object;)V

    .line 112
    .line 113
    .line 114
    return-object v5

    .line 115
    :cond_6
    :try_start_2
    iput-object p0, v0, Lfo4;->t:Lt41;

    .line 116
    .line 117
    iput-object p1, v0, Lfo4;->u:Lji3;

    .line 118
    .line 119
    iput v3, v0, Lfo4;->x:I

    .line 120
    .line 121
    invoke-virtual {p0, v0}, Lt41;->s(Lwr0;)Ljava/lang/Object;

    .line 122
    .line 123
    .line 124
    move-result-object v0
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_1

    .line 125
    if-ne v0, v1, :cond_7

    .line 126
    .line 127
    :goto_2
    return-object v1

    .line 128
    :cond_7
    move-object v0, p0

    .line 129
    move-object p0, p1

    .line 130
    :goto_3
    :try_start_3
    iget-object p1, v0, Lt41;->b:Ljava/lang/Object;

    .line 131
    .line 132
    check-cast p1, Lem0;

    .line 133
    .line 134
    invoke-virtual {p1, v5}, Lxn2;->L(Ljava/lang/Object;)Z
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    .line 135
    .line 136
    .line 137
    check-cast p0, Lli3;

    .line 138
    .line 139
    invoke-virtual {p0, v6}, Lli3;->h(Ljava/lang/Object;)V

    .line 140
    .line 141
    .line 142
    return-object v5

    .line 143
    :catchall_1
    move-exception p0

    .line 144
    move-object v7, p1

    .line 145
    move-object p1, p0

    .line 146
    move-object p0, v7

    .line 147
    :goto_4
    check-cast p0, Lli3;

    .line 148
    .line 149
    invoke-virtual {p0, v6}, Lli3;->h(Ljava/lang/Object;)V

    .line 150
    .line 151
    .line 152
    throw p1
.end method

.method public B(I)V
    .locals 4

    .line 1
    new-instance v0, Ljava/util/ArrayList;

    .line 2
    .line 3
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 4
    .line 5
    .line 6
    iget-object v1, p0, Lt41;->c:Ljava/lang/Object;

    .line 7
    .line 8
    check-cast v1, Ljava/util/concurrent/LinkedBlockingDeque;

    .line 9
    .line 10
    invoke-virtual {v1, v0}, Ljava/util/concurrent/LinkedBlockingDeque;->drainTo(Ljava/util/Collection;)I

    .line 11
    .line 12
    .line 13
    const/4 v1, 0x0

    .line 14
    const/4 v2, 0x0

    .line 15
    invoke-static {v2, p1, v1, v1}, Landroid/os/Message;->obtain(Landroid/os/Handler;III)Landroid/os/Message;

    .line 16
    .line 17
    .line 18
    move-result-object p1

    .line 19
    invoke-virtual {p1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 20
    .line 21
    .line 22
    invoke-virtual {v0, p1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 23
    .line 24
    .line 25
    iget-object p1, p0, Lt41;->a:Ljava/lang/Object;

    .line 26
    .line 27
    check-cast p1, Lns0;

    .line 28
    .line 29
    invoke-static {p1}, Lpr0;->c(Lns0;)Lrr0;

    .line 30
    .line 31
    .line 32
    move-result-object p1

    .line 33
    new-instance v1, Lae0;

    .line 34
    .line 35
    const/16 v3, 0x9

    .line 36
    .line 37
    invoke-direct {v1, p0, v0, v2, v3}, Lae0;-><init>(Ljava/lang/Object;Ljava/lang/Object;Lur0;I)V

    .line 38
    .line 39
    .line 40
    const/4 p0, 0x3

    .line 41
    invoke-static {p1, v2, v2, v1, p0}, Lxc5;->r(Lys0;Lns0;Lct0;Lt32;I)La55;

    .line 42
    .line 43
    .line 44
    return-void
.end method

.method public C(I)V
    .locals 1

    .line 1
    const/16 v0, 0xc

    .line 2
    .line 3
    if-eq p1, v0, :cond_1

    .line 4
    .line 5
    const/16 v0, 0x10

    .line 6
    .line 7
    if-ne p1, v0, :cond_0

    .line 8
    .line 9
    goto :goto_0

    .line 10
    :cond_0
    new-instance p0, Ljava/security/GeneralSecurityException;

    .line 11
    .line 12
    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 13
    .line 14
    .line 15
    move-result-object p1

    .line 16
    filled-new-array {p1}, [Ljava/lang/Object;

    .line 17
    .line 18
    .line 19
    move-result-object p1

    .line 20
    const-string v0, "Invalid IV size in bytes %d; acceptable values have 12 or 16 bytes"

    .line 21
    .line 22
    invoke-static {v0, p1}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    .line 23
    .line 24
    .line 25
    move-result-object p1

    .line 26
    invoke-direct {p0, p1}, Ljava/security/GeneralSecurityException;-><init>(Ljava/lang/String;)V

    .line 27
    .line 28
    .line 29
    throw p0

    .line 30
    :cond_1
    :goto_0
    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 31
    .line 32
    .line 33
    move-result-object p1

    .line 34
    iput-object p1, p0, Lt41;->b:Ljava/lang/Object;

    .line 35
    .line 36
    return-void
.end method

.method public D(I)V
    .locals 1

    .line 1
    const/16 v0, 0x10

    .line 2
    .line 3
    if-eq p1, v0, :cond_1

    .line 4
    .line 5
    const/16 v0, 0x18

    .line 6
    .line 7
    if-eq p1, v0, :cond_1

    .line 8
    .line 9
    const/16 v0, 0x20

    .line 10
    .line 11
    if-ne p1, v0, :cond_0

    .line 12
    .line 13
    goto :goto_0

    .line 14
    :cond_0
    new-instance p0, Ljava/security/InvalidAlgorithmParameterException;

    .line 15
    .line 16
    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 17
    .line 18
    .line 19
    move-result-object p1

    .line 20
    filled-new-array {p1}, [Ljava/lang/Object;

    .line 21
    .line 22
    .line 23
    move-result-object p1

    .line 24
    const-string v0, "Invalid key size %d; only 16-byte, 24-byte and 32-byte AES keys are supported"

    .line 25
    .line 26
    invoke-static {v0, p1}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    .line 27
    .line 28
    .line 29
    move-result-object p1

    .line 30
    invoke-direct {p0, p1}, Ljava/security/InvalidAlgorithmParameterException;-><init>(Ljava/lang/String;)V

    .line 31
    .line 32
    .line 33
    throw p0

    .line 34
    :cond_1
    :goto_0
    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 35
    .line 36
    .line 37
    move-result-object p1

    .line 38
    iput-object p1, p0, Lt41;->a:Ljava/lang/Object;

    .line 39
    .line 40
    return-void
.end method

.method public E()V
    .locals 1

    .line 1
    const/16 v0, 0x10

    .line 2
    .line 3
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 4
    .line 5
    .line 6
    move-result-object v0

    .line 7
    iput-object v0, p0, Lt41;->c:Ljava/lang/Object;

    .line 8
    .line 9
    return-void
.end method

.method public G()V
    .locals 11

    .line 1
    iget-object v0, p0, Lt41;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lya5;

    .line 4
    .line 5
    iget-object v1, p0, Lt41;->a:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast v1, Lmx4;

    .line 8
    .line 9
    iget-object p0, p0, Lt41;->d:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast p0, Landroidx/viewpager2/widget/ViewPager2;

    .line 12
    .line 13
    const v2, 0x1020048

    .line 14
    .line 15
    .line 16
    invoke-static {v2, p0}, Lcu5;->l(ILandroid/view/View;)V

    .line 17
    .line 18
    .line 19
    const/4 v3, 0x0

    .line 20
    invoke-static {v3, p0}, Lcu5;->i(ILandroid/view/View;)V

    .line 21
    .line 22
    .line 23
    const v4, 0x1020049

    .line 24
    .line 25
    .line 26
    invoke-static {v4, p0}, Lcu5;->l(ILandroid/view/View;)V

    .line 27
    .line 28
    .line 29
    invoke-static {v3, p0}, Lcu5;->i(ILandroid/view/View;)V

    .line 30
    .line 31
    .line 32
    const v5, 0x1020046

    .line 33
    .line 34
    .line 35
    invoke-static {v5, p0}, Lcu5;->l(ILandroid/view/View;)V

    .line 36
    .line 37
    .line 38
    invoke-static {v3, p0}, Lcu5;->i(ILandroid/view/View;)V

    .line 39
    .line 40
    .line 41
    const v6, 0x1020047

    .line 42
    .line 43
    .line 44
    invoke-static {v6, p0}, Lcu5;->l(ILandroid/view/View;)V

    .line 45
    .line 46
    .line 47
    invoke-static {v3, p0}, Lcu5;->i(ILandroid/view/View;)V

    .line 48
    .line 49
    .line 50
    invoke-virtual {p0}, Landroidx/viewpager2/widget/ViewPager2;->getAdapter()Landroidx/recyclerview/widget/j;

    .line 51
    .line 52
    .line 53
    move-result-object v7

    .line 54
    if-nez v7, :cond_0

    .line 55
    .line 56
    goto :goto_1

    .line 57
    :cond_0
    invoke-virtual {p0}, Landroidx/viewpager2/widget/ViewPager2;->getAdapter()Landroidx/recyclerview/widget/j;

    .line 58
    .line 59
    .line 60
    move-result-object v7

    .line 61
    invoke-virtual {v7}, Landroidx/recyclerview/widget/j;->getItemCount()I

    .line 62
    .line 63
    .line 64
    move-result v7

    .line 65
    if-nez v7, :cond_1

    .line 66
    .line 67
    goto :goto_1

    .line 68
    :cond_1
    iget-boolean v8, p0, Landroidx/viewpager2/widget/ViewPager2;->r:Z

    .line 69
    .line 70
    if-nez v8, :cond_2

    .line 71
    .line 72
    goto :goto_1

    .line 73
    :cond_2
    invoke-virtual {p0}, Landroidx/viewpager2/widget/ViewPager2;->getOrientation()I

    .line 74
    .line 75
    .line 76
    move-result v8

    .line 77
    const/4 v9, 0x1

    .line 78
    const/4 v10, 0x0

    .line 79
    if-nez v8, :cond_7

    .line 80
    .line 81
    iget-object v5, p0, Landroidx/viewpager2/widget/ViewPager2;->g:Lgv5;

    .line 82
    .line 83
    invoke-virtual {v5}, Landroidx/recyclerview/widget/o;->getLayoutDirection()I

    .line 84
    .line 85
    .line 86
    move-result v5

    .line 87
    if-ne v5, v9, :cond_3

    .line 88
    .line 89
    move v3, v9

    .line 90
    :cond_3
    if-eqz v3, :cond_4

    .line 91
    .line 92
    move v5, v2

    .line 93
    goto :goto_0

    .line 94
    :cond_4
    move v5, v4

    .line 95
    :goto_0
    if-eqz v3, :cond_5

    .line 96
    .line 97
    move v2, v4

    .line 98
    :cond_5
    iget v3, p0, Landroidx/viewpager2/widget/ViewPager2;->d:I

    .line 99
    .line 100
    sub-int/2addr v7, v9

    .line 101
    if-ge v3, v7, :cond_6

    .line 102
    .line 103
    new-instance v3, Lv3;

    .line 104
    .line 105
    invoke-direct {v3, v5, v10}, Lv3;-><init>(ILjava/lang/CharSequence;)V

    .line 106
    .line 107
    .line 108
    invoke-static {p0, v3, v10, v1}, Lcu5;->m(Landroid/view/View;Lv3;Ljava/lang/String;Lj4;)V

    .line 109
    .line 110
    .line 111
    :cond_6
    iget v1, p0, Landroidx/viewpager2/widget/ViewPager2;->d:I

    .line 112
    .line 113
    if-lez v1, :cond_9

    .line 114
    .line 115
    new-instance v1, Lv3;

    .line 116
    .line 117
    invoke-direct {v1, v2, v10}, Lv3;-><init>(ILjava/lang/CharSequence;)V

    .line 118
    .line 119
    .line 120
    invoke-static {p0, v1, v10, v0}, Lcu5;->m(Landroid/view/View;Lv3;Ljava/lang/String;Lj4;)V

    .line 121
    .line 122
    .line 123
    return-void

    .line 124
    :cond_7
    iget v2, p0, Landroidx/viewpager2/widget/ViewPager2;->d:I

    .line 125
    .line 126
    sub-int/2addr v7, v9

    .line 127
    if-ge v2, v7, :cond_8

    .line 128
    .line 129
    new-instance v2, Lv3;

    .line 130
    .line 131
    invoke-direct {v2, v6, v10}, Lv3;-><init>(ILjava/lang/CharSequence;)V

    .line 132
    .line 133
    .line 134
    invoke-static {p0, v2, v10, v1}, Lcu5;->m(Landroid/view/View;Lv3;Ljava/lang/String;Lj4;)V

    .line 135
    .line 136
    .line 137
    :cond_8
    iget v1, p0, Landroidx/viewpager2/widget/ViewPager2;->d:I

    .line 138
    .line 139
    if-lez v1, :cond_9

    .line 140
    .line 141
    new-instance v1, Lv3;

    .line 142
    .line 143
    invoke-direct {v1, v5, v10}, Lv3;-><init>(ILjava/lang/CharSequence;)V

    .line 144
    .line 145
    .line 146
    invoke-static {p0, v1, v10, v0}, Lcu5;->m(Landroid/view/View;Lv3;Ljava/lang/String;Lj4;)V

    .line 147
    .line 148
    .line 149
    :cond_9
    :goto_1
    return-void
.end method

.method public H(Lcom/google/android/gms/internal/consent_sdk/zzcn;)Lfh;
    .locals 12

    .line 1
    iget-object v0, p0, Lt41;->c:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lcom/google/android/gms/internal/consent_sdk/zzaq;

    .line 4
    .line 5
    iget-object v1, p0, Lt41;->d:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast v1, Lcom/google/android/gms/internal/consent_sdk/zzcr;

    .line 8
    .line 9
    iget v2, p1, Lcom/google/android/gms/internal/consent_sdk/zzcn;->g:I

    .line 10
    .line 11
    const/16 v3, 0x8

    .line 12
    .line 13
    const/4 v4, 0x0

    .line 14
    const/4 v5, 0x1

    .line 15
    if-ne v2, v3, :cond_0

    .line 16
    .line 17
    move v3, v5

    .line 18
    goto :goto_0

    .line 19
    :cond_0
    move v3, v4

    .line 20
    :goto_0
    iget-object v6, v0, Lcom/google/android/gms/internal/consent_sdk/zzaq;->b:Landroid/content/SharedPreferences;

    .line 21
    .line 22
    invoke-interface {v6}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    .line 23
    .line 24
    .line 25
    move-result-object v6

    .line 26
    const-string v7, "is_pub_misconfigured"

    .line 27
    .line 28
    invoke-interface {v6, v7, v3}, Landroid/content/SharedPreferences$Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences$Editor;

    .line 29
    .line 30
    .line 31
    move-result-object v3

    .line 32
    invoke-interface {v3}, Landroid/content/SharedPreferences$Editor;->commit()Z

    .line 33
    .line 34
    .line 35
    add-int/lit8 v3, v2, -0x1

    .line 36
    .line 37
    const/4 v6, 0x0

    .line 38
    if-eqz v2, :cond_b

    .line 39
    .line 40
    const/4 v2, 0x3

    .line 41
    const-string v7, "Invalid response from server."

    .line 42
    .line 43
    const/4 v8, 0x2

    .line 44
    packed-switch v3, :pswitch_data_0

    .line 45
    .line 46
    .line 47
    new-instance p0, Lcom/google/android/gms/internal/consent_sdk/zzg;

    .line 48
    .line 49
    invoke-direct {p0, v5, v7}, Lcom/google/android/gms/internal/consent_sdk/zzg;-><init>(ILjava/lang/String;)V

    .line 50
    .line 51
    .line 52
    throw p0

    .line 53
    :pswitch_0
    new-instance p0, Lcom/google/android/gms/internal/consent_sdk/zzg;

    .line 54
    .line 55
    iget-object p1, p1, Lcom/google/android/gms/internal/consent_sdk/zzcn;->c:Ljava/lang/String;

    .line 56
    .line 57
    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    .line 58
    .line 59
    .line 60
    move-result-object p1

    .line 61
    const-string v0, "Publisher misconfiguration: "

    .line 62
    .line 63
    invoke-virtual {v0, p1}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    .line 64
    .line 65
    .line 66
    move-result-object p1

    .line 67
    invoke-direct {p0, v2, p1}, Lcom/google/android/gms/internal/consent_sdk/zzg;-><init>(ILjava/lang/String;)V

    .line 68
    .line 69
    .line 70
    throw p0

    .line 71
    :pswitch_1
    new-instance p0, Lcom/google/android/gms/internal/consent_sdk/zzg;

    .line 72
    .line 73
    iget-object p1, p1, Lcom/google/android/gms/internal/consent_sdk/zzcn;->c:Ljava/lang/String;

    .line 74
    .line 75
    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    .line 76
    .line 77
    .line 78
    move-result-object p1

    .line 79
    const-string v0, "Invalid response from server: "

    .line 80
    .line 81
    invoke-virtual {v0, p1}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    .line 82
    .line 83
    .line 84
    move-result-object p1

    .line 85
    invoke-direct {p0, v5, p1}, Lcom/google/android/gms/internal/consent_sdk/zzg;-><init>(ILjava/lang/String;)V

    .line 86
    .line 87
    .line 88
    throw p0

    .line 89
    :pswitch_2
    move v2, v5

    .line 90
    goto :goto_1

    .line 91
    :pswitch_3
    move v2, v8

    .line 92
    :goto_1
    :pswitch_4
    iget v3, p1, Lcom/google/android/gms/internal/consent_sdk/zzcn;->h:I

    .line 93
    .line 94
    add-int/lit8 v9, v3, -0x1

    .line 95
    .line 96
    if-eqz v3, :cond_a

    .line 97
    .line 98
    if-eq v9, v5, :cond_2

    .line 99
    .line 100
    if-ne v9, v8, :cond_1

    .line 101
    .line 102
    sget-object v3, Ltp0;->NOT_REQUIRED:Ltp0;

    .line 103
    .line 104
    goto :goto_2

    .line 105
    :cond_1
    new-instance p0, Lcom/google/android/gms/internal/consent_sdk/zzg;

    .line 106
    .line 107
    invoke-direct {p0, v5, v7}, Lcom/google/android/gms/internal/consent_sdk/zzg;-><init>(ILjava/lang/String;)V

    .line 108
    .line 109
    .line 110
    throw p0

    .line 111
    :cond_2
    sget-object v3, Ltp0;->REQUIRED:Ltp0;

    .line 112
    .line 113
    :goto_2
    iget-object v7, p1, Lcom/google/android/gms/internal/consent_sdk/zzcn;->a:Ljava/lang/String;

    .line 114
    .line 115
    if-nez v7, :cond_3

    .line 116
    .line 117
    move-object v9, v6

    .line 118
    goto :goto_3

    .line 119
    :cond_3
    new-instance v9, Lcom/google/android/gms/internal/consent_sdk/zzbs;

    .line 120
    .line 121
    iget-object v10, p1, Lcom/google/android/gms/internal/consent_sdk/zzcn;->b:Ljava/lang/String;

    .line 122
    .line 123
    invoke-direct {v9, v10, v7}, Lcom/google/android/gms/internal/consent_sdk/zzbs;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    .line 124
    .line 125
    .line 126
    :goto_3
    iget-object v7, p1, Lcom/google/android/gms/internal/consent_sdk/zzcn;->f:Ljava/lang/String;

    .line 127
    .line 128
    iget-object v1, v1, Lcom/google/android/gms/internal/consent_sdk/zzcr;->a:Ljava/util/concurrent/atomic/AtomicReference;

    .line 129
    .line 130
    invoke-virtual {v1, v7}, Ljava/util/concurrent/atomic/AtomicReference;->set(Ljava/lang/Object;)V

    .line 131
    .line 132
    .line 133
    new-instance v1, Ljava/util/HashSet;

    .line 134
    .line 135
    iget-object v7, p1, Lcom/google/android/gms/internal/consent_sdk/zzcn;->d:Ljava/util/List;

    .line 136
    .line 137
    invoke-direct {v1, v7}, Ljava/util/HashSet;-><init>(Ljava/util/Collection;)V

    .line 138
    .line 139
    .line 140
    iget-object v0, v0, Lcom/google/android/gms/internal/consent_sdk/zzaq;->b:Landroid/content/SharedPreferences;

    .line 141
    .line 142
    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    .line 143
    .line 144
    .line 145
    move-result-object v0

    .line 146
    const-string v7, "stored_info"

    .line 147
    .line 148
    invoke-interface {v0, v7, v1}, Landroid/content/SharedPreferences$Editor;->putStringSet(Ljava/lang/String;Ljava/util/Set;)Landroid/content/SharedPreferences$Editor;

    .line 149
    .line 150
    .line 151
    move-result-object v0

    .line 152
    invoke-interface {v0}, Landroid/content/SharedPreferences$Editor;->commit()Z

    .line 153
    .line 154
    .line 155
    iget-object p1, p1, Lcom/google/android/gms/internal/consent_sdk/zzcn;->e:Ljava/util/List;

    .line 156
    .line 157
    invoke-interface {p1}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    .line 158
    .line 159
    .line 160
    move-result-object p1

    .line 161
    :cond_4
    :goto_4
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    .line 162
    .line 163
    .line 164
    move-result v0

    .line 165
    if-eqz v0, :cond_9

    .line 166
    .line 167
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 168
    .line 169
    .line 170
    move-result-object v0

    .line 171
    check-cast v0, Lcom/google/android/gms/internal/consent_sdk/zzcm;

    .line 172
    .line 173
    iget v1, v0, Lcom/google/android/gms/internal/consent_sdk/zzcm;->b:I

    .line 174
    .line 175
    add-int/lit8 v7, v1, -0x1

    .line 176
    .line 177
    if-eqz v1, :cond_8

    .line 178
    .line 179
    if-eqz v7, :cond_7

    .line 180
    .line 181
    if-eq v7, v5, :cond_6

    .line 182
    .line 183
    if-ne v7, v8, :cond_5

    .line 184
    .line 185
    const-string v1, "clear"

    .line 186
    .line 187
    goto :goto_5

    .line 188
    :cond_5
    new-instance p0, Ljava/lang/RuntimeException;

    .line 189
    .line 190
    invoke-direct {p0, v6, v6}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    .line 191
    .line 192
    .line 193
    throw p0

    .line 194
    :cond_6
    const-string v1, "write"

    .line 195
    .line 196
    goto :goto_5

    .line 197
    :cond_7
    move-object v1, v6

    .line 198
    :goto_5
    if-eqz v1, :cond_4

    .line 199
    .line 200
    iget-object v7, p0, Lt41;->a:Ljava/lang/Object;

    .line 201
    .line 202
    check-cast v7, Lcom/google/android/gms/internal/consent_sdk/zze;

    .line 203
    .line 204
    iget-object v0, v0, Lcom/google/android/gms/internal/consent_sdk/zzcm;->a:Ljava/lang/String;

    .line 205
    .line 206
    iget-object v10, p0, Lt41;->b:Ljava/lang/Object;

    .line 207
    .line 208
    check-cast v10, Lcom/google/android/gms/internal/consent_sdk/zzao;

    .line 209
    .line 210
    new-array v11, v5, [Lcom/google/android/gms/internal/consent_sdk/zzd;

    .line 211
    .line 212
    aput-object v10, v11, v4

    .line 213
    .line 214
    invoke-virtual {v7}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 215
    .line 216
    .line 217
    new-instance v10, Lcom/google/android/gms/internal/consent_sdk/zzc;

    .line 218
    .line 219
    invoke-direct {v10, v1, v0, v11}, Lcom/google/android/gms/internal/consent_sdk/zzc;-><init>(Ljava/lang/String;Ljava/lang/String;[Lcom/google/android/gms/internal/consent_sdk/zzd;)V

    .line 220
    .line 221
    .line 222
    iget-object v0, v7, Lcom/google/android/gms/internal/consent_sdk/zze;->a:Ljava/util/concurrent/Executor;

    .line 223
    .line 224
    invoke-interface {v0, v10}, Ljava/util/concurrent/Executor;->execute(Ljava/lang/Runnable;)V

    .line 225
    .line 226
    .line 227
    goto :goto_4

    .line 228
    :cond_8
    throw v6

    .line 229
    :cond_9
    new-instance p0, Lfh;

    .line 230
    .line 231
    invoke-direct {p0, v2, v3, v9}, Lfh;-><init>(ILtp0;Lcom/google/android/gms/internal/consent_sdk/zzbs;)V

    .line 232
    .line 233
    .line 234
    return-object p0

    .line 235
    :cond_a
    throw v6

    .line 236
    :cond_b
    throw v6

    .line 237
    :pswitch_data_0
    .packed-switch 0x1
        :pswitch_4
        :pswitch_4
        :pswitch_4
        :pswitch_3
        :pswitch_2
        :pswitch_1
        :pswitch_0
    .end packed-switch
.end method

.method public b(Le30;)V
    .locals 1

    .line 1
    iget-object p0, p0, Lt41;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/util/SparseArray;

    .line 4
    .line 5
    iget v0, p1, Le30;->a:I

    .line 6
    .line 7
    invoke-virtual {p0, v0, p1}, Landroid/util/SparseArray;->put(ILjava/lang/Object;)V

    .line 8
    .line 9
    .line 10
    return-void
.end method

.method public c(Ljava/util/HashMap;)V
    .locals 5

    .line 1
    iget-object p1, p0, Lt41;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p1, Landroid/util/SparseArray;

    .line 4
    .line 5
    invoke-virtual {p1}, Landroid/util/SparseArray;->size()I

    .line 6
    .line 7
    .line 8
    move-result v0

    .line 9
    if-nez v0, :cond_0

    .line 10
    .line 11
    return-void

    .line 12
    :cond_0
    :try_start_0
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 13
    .line 14
    check-cast v0, Lb55;

    .line 15
    .line 16
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteOpenHelper;->getWritableDatabase()Landroid/database/sqlite/SQLiteDatabase;

    .line 17
    .line 18
    .line 19
    move-result-object v0

    .line 20
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteDatabase;->beginTransactionNonExclusive()V
    :try_end_0
    .catch Landroid/database/SQLException; {:try_start_0 .. :try_end_0} :catch_0

    .line 21
    .line 22
    .line 23
    const/4 v1, 0x0

    .line 24
    :goto_0
    :try_start_1
    invoke-virtual {p1}, Landroid/util/SparseArray;->size()I

    .line 25
    .line 26
    .line 27
    move-result v2

    .line 28
    if-ge v1, v2, :cond_2

    .line 29
    .line 30
    invoke-virtual {p1, v1}, Landroid/util/SparseArray;->valueAt(I)Ljava/lang/Object;

    .line 31
    .line 32
    .line 33
    move-result-object v2

    .line 34
    check-cast v2, Le30;

    .line 35
    .line 36
    if-nez v2, :cond_1

    .line 37
    .line 38
    invoke-virtual {p1, v1}, Landroid/util/SparseArray;->keyAt(I)I

    .line 39
    .line 40
    .line 41
    move-result v2

    .line 42
    iget-object v3, p0, Lt41;->d:Ljava/lang/Object;

    .line 43
    .line 44
    check-cast v3, Ljava/lang/String;

    .line 45
    .line 46
    invoke-virtual {v3}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 47
    .line 48
    .line 49
    const-string v4, "id = ?"

    .line 50
    .line 51
    invoke-static {v2}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;

    .line 52
    .line 53
    .line 54
    move-result-object v2

    .line 55
    filled-new-array {v2}, [Ljava/lang/String;

    .line 56
    .line 57
    .line 58
    move-result-object v2

    .line 59
    invoke-virtual {v0, v3, v4, v2}, Landroid/database/sqlite/SQLiteDatabase;->delete(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)I

    .line 60
    .line 61
    .line 62
    goto :goto_1

    .line 63
    :catchall_0
    move-exception p0

    .line 64
    goto :goto_2

    .line 65
    :cond_1
    invoke-virtual {p0, v0, v2}, Lt41;->f(Landroid/database/sqlite/SQLiteDatabase;Le30;)V

    .line 66
    .line 67
    .line 68
    :goto_1
    add-int/lit8 v1, v1, 0x1

    .line 69
    .line 70
    goto :goto_0

    .line 71
    :cond_2
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteDatabase;->setTransactionSuccessful()V

    .line 72
    .line 73
    .line 74
    invoke-virtual {p1}, Landroid/util/SparseArray;->clear()V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 75
    .line 76
    .line 77
    :try_start_2
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteDatabase;->endTransaction()V

    .line 78
    .line 79
    .line 80
    return-void

    .line 81
    :goto_2
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteDatabase;->endTransaction()V

    .line 82
    .line 83
    .line 84
    throw p0
    :try_end_2
    .catch Landroid/database/SQLException; {:try_start_2 .. :try_end_2} :catch_0

    .line 85
    :catch_0
    move-exception p0

    .line 86
    new-instance p1, Landroidx/media3/database/DatabaseIOException;

    .line 87
    .line 88
    invoke-direct {p1, p0}, Landroidx/media3/database/DatabaseIOException;-><init>(Landroid/database/SQLException;)V

    .line 89
    .line 90
    .line 91
    throw p1
.end method

.method public d(J)V
    .locals 0

    .line 1
    invoke-static {p1, p2}, Ljava/lang/Long;->toHexString(J)Ljava/lang/String;

    .line 2
    .line 3
    .line 4
    move-result-object p1

    .line 5
    iput-object p1, p0, Lt41;->c:Ljava/lang/Object;

    .line 6
    .line 7
    const-string p2, "ExoPlayerCacheIndex"

    .line 8
    .line 9
    invoke-static {p2, p1}, Lk26;->l(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    .line 10
    .line 11
    .line 12
    move-result-object p1

    .line 13
    iput-object p1, p0, Lt41;->d:Ljava/lang/Object;

    .line 14
    .line 15
    return-void
.end method

.method public delete()V
    .locals 8

    .line 1
    const-string v0, "ExoPlayerVersions"

    .line 2
    .line 3
    const-string v1, "DROP TABLE IF EXISTS "

    .line 4
    .line 5
    const-string v2, "ExoPlayerCacheIndex"

    .line 6
    .line 7
    iget-object v3, p0, Lt41;->a:Ljava/lang/Object;

    .line 8
    .line 9
    check-cast v3, Lb55;

    .line 10
    .line 11
    iget-object p0, p0, Lt41;->c:Ljava/lang/Object;

    .line 12
    .line 13
    check-cast p0, Ljava/lang/String;

    .line 14
    .line 15
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 16
    .line 17
    .line 18
    :try_start_0
    invoke-virtual {v2, p0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    .line 19
    .line 20
    .line 21
    move-result-object v2

    .line 22
    invoke-virtual {v3}, Landroid/database/sqlite/SQLiteOpenHelper;->getWritableDatabase()Landroid/database/sqlite/SQLiteDatabase;

    .line 23
    .line 24
    .line 25
    move-result-object v3

    .line 26
    invoke-virtual {v3}, Landroid/database/sqlite/SQLiteDatabase;->beginTransactionNonExclusive()V
    :try_end_0
    .catch Landroid/database/SQLException; {:try_start_0 .. :try_end_0} :catch_1

    .line 27
    .line 28
    .line 29
    :try_start_1
    sget v4, Los5;->a:I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 30
    .line 31
    :try_start_2
    sget v4, Lfr5;->a:I

    .line 32
    .line 33
    const-string v4, "sqlite_master"

    .line 34
    .line 35
    const-string v5, "tbl_name = ?"

    .line 36
    .line 37
    filled-new-array {v0}, [Ljava/lang/String;

    .line 38
    .line 39
    .line 40
    move-result-object v6

    .line 41
    invoke-static {v3, v4, v5, v6}, Landroid/database/DatabaseUtils;->queryNumEntries(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)J

    .line 42
    .line 43
    .line 44
    move-result-wide v4

    .line 45
    const-wide/16 v6, 0x0

    .line 46
    .line 47
    cmp-long v4, v4, v6

    .line 48
    .line 49
    const/4 v5, 0x1

    .line 50
    if-lez v4, :cond_0

    .line 51
    .line 52
    move v4, v5

    .line 53
    goto :goto_0

    .line 54
    :cond_0
    const/4 v4, 0x0

    .line 55
    :goto_0
    if-nez v4, :cond_1

    .line 56
    .line 57
    goto :goto_1

    .line 58
    :cond_1
    const-string v4, "feature = ? AND instance_uid = ?"

    .line 59
    .line 60
    invoke-static {v5}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;

    .line 61
    .line 62
    .line 63
    move-result-object v5

    .line 64
    filled-new-array {v5, p0}, [Ljava/lang/String;

    .line 65
    .line 66
    .line 67
    move-result-object p0

    .line 68
    invoke-virtual {v3, v0, v4, p0}, Landroid/database/sqlite/SQLiteDatabase;->delete(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)I
    :try_end_2
    .catch Landroid/database/SQLException; {:try_start_2 .. :try_end_2} :catch_0
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    .line 69
    .line 70
    .line 71
    :goto_1
    :try_start_3
    invoke-virtual {v1, v2}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    .line 72
    .line 73
    .line 74
    move-result-object p0

    .line 75
    invoke-virtual {v3, p0}, Landroid/database/sqlite/SQLiteDatabase;->execSQL(Ljava/lang/String;)V

    .line 76
    .line 77
    .line 78
    invoke-virtual {v3}, Landroid/database/sqlite/SQLiteDatabase;->setTransactionSuccessful()V
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    .line 79
    .line 80
    .line 81
    :try_start_4
    invoke-virtual {v3}, Landroid/database/sqlite/SQLiteDatabase;->endTransaction()V
    :try_end_4
    .catch Landroid/database/SQLException; {:try_start_4 .. :try_end_4} :catch_1

    .line 82
    .line 83
    .line 84
    return-void

    .line 85
    :catchall_0
    move-exception p0

    .line 86
    goto :goto_2

    .line 87
    :catch_0
    move-exception p0

    .line 88
    :try_start_5
    new-instance v0, Landroidx/media3/database/DatabaseIOException;

    .line 89
    .line 90
    invoke-direct {v0, p0}, Landroidx/media3/database/DatabaseIOException;-><init>(Landroid/database/SQLException;)V

    .line 91
    .line 92
    .line 93
    throw v0
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_0

    .line 94
    :goto_2
    :try_start_6
    invoke-virtual {v3}, Landroid/database/sqlite/SQLiteDatabase;->endTransaction()V

    .line 95
    .line 96
    .line 97
    throw p0
    :try_end_6
    .catch Landroid/database/SQLException; {:try_start_6 .. :try_end_6} :catch_1

    .line 98
    :catch_1
    move-exception p0

    .line 99
    new-instance v0, Landroidx/media3/database/DatabaseIOException;

    .line 100
    .line 101
    invoke-direct {v0, p0}, Landroidx/media3/database/DatabaseIOException;-><init>(Landroid/database/SQLException;)V

    .line 102
    .line 103
    .line 104
    throw v0
.end method

.method public e([BIILb95;Lwq0;)V
    .locals 35

    .line 1
    move-object/from16 v0, p0

    .line 2
    .line 3
    move/from16 v1, p2

    .line 4
    .line 5
    iget-object v2, v0, Lt41;->c:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast v2, Lh24;

    .line 8
    .line 9
    iget-object v3, v0, Lt41;->a:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast v3, La14;

    .line 12
    .line 13
    add-int v4, v1, p3

    .line 14
    .line 15
    move-object/from16 v5, p1

    .line 16
    .line 17
    invoke-virtual {v3, v5, v4}, La14;->D([BI)V

    .line 18
    .line 19
    .line 20
    invoke-virtual {v3, v1}, La14;->F(I)V

    .line 21
    .line 22
    .line 23
    iget-object v1, v0, Lt41;->b:Ljava/lang/Object;

    .line 24
    .line 25
    check-cast v1, La14;

    .line 26
    .line 27
    invoke-virtual {v3}, La14;->a()I

    .line 28
    .line 29
    .line 30
    move-result v4

    .line 31
    const/16 v5, 0xff

    .line 32
    .line 33
    if-lez v4, :cond_1

    .line 34
    .line 35
    iget-object v4, v3, La14;->a:[B

    .line 36
    .line 37
    iget v6, v3, La14;->b:I

    .line 38
    .line 39
    aget-byte v4, v4, v6

    .line 40
    .line 41
    and-int/2addr v4, v5

    .line 42
    const/16 v6, 0x78

    .line 43
    .line 44
    if-ne v4, v6, :cond_1

    .line 45
    .line 46
    iget-object v4, v0, Lt41;->d:Ljava/lang/Object;

    .line 47
    .line 48
    check-cast v4, Ljava/util/zip/Inflater;

    .line 49
    .line 50
    if-nez v4, :cond_0

    .line 51
    .line 52
    new-instance v4, Ljava/util/zip/Inflater;

    .line 53
    .line 54
    invoke-direct {v4}, Ljava/util/zip/Inflater;-><init>()V

    .line 55
    .line 56
    .line 57
    iput-object v4, v0, Lt41;->d:Ljava/lang/Object;

    .line 58
    .line 59
    :cond_0
    iget-object v0, v0, Lt41;->d:Ljava/lang/Object;

    .line 60
    .line 61
    check-cast v0, Ljava/util/zip/Inflater;

    .line 62
    .line 63
    invoke-static {v3, v1, v0}, Lfr5;->x(La14;La14;Ljava/util/zip/Inflater;)Z

    .line 64
    .line 65
    .line 66
    move-result v0

    .line 67
    if-eqz v0, :cond_1

    .line 68
    .line 69
    iget-object v0, v1, La14;->a:[B

    .line 70
    .line 71
    iget v1, v1, La14;->c:I

    .line 72
    .line 73
    invoke-virtual {v3, v0, v1}, La14;->D([BI)V

    .line 74
    .line 75
    .line 76
    :cond_1
    const/4 v0, 0x0

    .line 77
    iput v0, v2, Lh24;->d:I

    .line 78
    .line 79
    iget-object v1, v2, Lh24;->b:[I

    .line 80
    .line 81
    iget-object v4, v2, Lh24;->a:La14;

    .line 82
    .line 83
    iput v0, v2, Lh24;->e:I

    .line 84
    .line 85
    iput v0, v2, Lh24;->f:I

    .line 86
    .line 87
    iput v0, v2, Lh24;->g:I

    .line 88
    .line 89
    iput v0, v2, Lh24;->h:I

    .line 90
    .line 91
    iput v0, v2, Lh24;->i:I

    .line 92
    .line 93
    invoke-virtual {v4, v0}, La14;->C(I)V

    .line 94
    .line 95
    .line 96
    iput-boolean v0, v2, Lh24;->c:Z

    .line 97
    .line 98
    new-instance v7, Ljava/util/ArrayList;

    .line 99
    .line 100
    invoke-direct {v7}, Ljava/util/ArrayList;-><init>()V

    .line 101
    .line 102
    .line 103
    :goto_0
    invoke-virtual {v3}, La14;->a()I

    .line 104
    .line 105
    .line 106
    move-result v6

    .line 107
    const/4 v8, 0x3

    .line 108
    if-lt v6, v8, :cond_15

    .line 109
    .line 110
    iget v6, v3, La14;->c:I

    .line 111
    .line 112
    invoke-virtual {v3}, La14;->t()I

    .line 113
    .line 114
    .line 115
    move-result v9

    .line 116
    invoke-virtual {v3}, La14;->z()I

    .line 117
    .line 118
    .line 119
    move-result v10

    .line 120
    iget v11, v3, La14;->b:I

    .line 121
    .line 122
    add-int/2addr v11, v10

    .line 123
    if-le v11, v6, :cond_2

    .line 124
    .line 125
    invoke-virtual {v3, v6}, La14;->F(I)V

    .line 126
    .line 127
    .line 128
    move v6, v0

    .line 129
    move-object/from16 v16, v1

    .line 130
    .line 131
    move-object/from16 p0, v7

    .line 132
    .line 133
    const/4 v12, 0x0

    .line 134
    goto/16 :goto_d

    .line 135
    .line 136
    :cond_2
    const/16 v6, 0x80

    .line 137
    .line 138
    if-eq v9, v6, :cond_c

    .line 139
    .line 140
    packed-switch v9, :pswitch_data_0

    .line 141
    .line 142
    .line 143
    :cond_3
    :goto_1
    move-object/from16 v16, v1

    .line 144
    .line 145
    move-object/from16 p0, v7

    .line 146
    .line 147
    goto/16 :goto_4

    .line 148
    .line 149
    :pswitch_0
    const/16 v6, 0x13

    .line 150
    .line 151
    if-ge v10, v6, :cond_4

    .line 152
    .line 153
    goto :goto_1

    .line 154
    :cond_4
    invoke-virtual {v3}, La14;->z()I

    .line 155
    .line 156
    .line 157
    move-result v6

    .line 158
    iput v6, v2, Lh24;->d:I

    .line 159
    .line 160
    invoke-virtual {v3}, La14;->z()I

    .line 161
    .line 162
    .line 163
    move-result v6

    .line 164
    iput v6, v2, Lh24;->e:I

    .line 165
    .line 166
    const/16 v6, 0xb

    .line 167
    .line 168
    invoke-virtual {v3, v6}, La14;->G(I)V

    .line 169
    .line 170
    .line 171
    invoke-virtual {v3}, La14;->z()I

    .line 172
    .line 173
    .line 174
    move-result v6

    .line 175
    iput v6, v2, Lh24;->f:I

    .line 176
    .line 177
    invoke-virtual {v3}, La14;->z()I

    .line 178
    .line 179
    .line 180
    move-result v6

    .line 181
    iput v6, v2, Lh24;->g:I

    .line 182
    .line 183
    goto :goto_1

    .line 184
    :pswitch_1
    const/4 v9, 0x4

    .line 185
    if-ge v10, v9, :cond_5

    .line 186
    .line 187
    goto :goto_1

    .line 188
    :cond_5
    invoke-virtual {v3, v8}, La14;->G(I)V

    .line 189
    .line 190
    .line 191
    invoke-virtual {v3}, La14;->t()I

    .line 192
    .line 193
    .line 194
    move-result v8

    .line 195
    and-int/2addr v6, v8

    .line 196
    if-eqz v6, :cond_6

    .line 197
    .line 198
    const/4 v13, 0x1

    .line 199
    goto :goto_2

    .line 200
    :cond_6
    move v13, v0

    .line 201
    :goto_2
    add-int/lit8 v6, v10, -0x4

    .line 202
    .line 203
    if-eqz v13, :cond_9

    .line 204
    .line 205
    const/4 v8, 0x7

    .line 206
    if-ge v6, v8, :cond_7

    .line 207
    .line 208
    goto :goto_1

    .line 209
    :cond_7
    invoke-virtual {v3}, La14;->w()I

    .line 210
    .line 211
    .line 212
    move-result v6

    .line 213
    if-ge v6, v9, :cond_8

    .line 214
    .line 215
    goto :goto_1

    .line 216
    :cond_8
    invoke-virtual {v3}, La14;->z()I

    .line 217
    .line 218
    .line 219
    move-result v8

    .line 220
    iput v8, v2, Lh24;->h:I

    .line 221
    .line 222
    invoke-virtual {v3}, La14;->z()I

    .line 223
    .line 224
    .line 225
    move-result v8

    .line 226
    iput v8, v2, Lh24;->i:I

    .line 227
    .line 228
    add-int/lit8 v6, v6, -0x4

    .line 229
    .line 230
    invoke-virtual {v4, v6}, La14;->C(I)V

    .line 231
    .line 232
    .line 233
    add-int/lit8 v6, v10, -0xb

    .line 234
    .line 235
    :cond_9
    iget v8, v4, La14;->b:I

    .line 236
    .line 237
    iget v9, v4, La14;->c:I

    .line 238
    .line 239
    if-ge v8, v9, :cond_3

    .line 240
    .line 241
    if-lez v6, :cond_3

    .line 242
    .line 243
    sub-int/2addr v9, v8

    .line 244
    invoke-static {v6, v9}, Ljava/lang/Math;->min(II)I

    .line 245
    .line 246
    .line 247
    move-result v6

    .line 248
    iget-object v9, v4, La14;->a:[B

    .line 249
    .line 250
    invoke-virtual {v3, v9, v8, v6}, La14;->e([BII)V

    .line 251
    .line 252
    .line 253
    add-int/2addr v8, v6

    .line 254
    invoke-virtual {v4, v8}, La14;->F(I)V

    .line 255
    .line 256
    .line 257
    goto :goto_1

    .line 258
    :pswitch_2
    rem-int/lit8 v8, v10, 0x5

    .line 259
    .line 260
    const/4 v9, 0x2

    .line 261
    if-eq v8, v9, :cond_a

    .line 262
    .line 263
    goto :goto_1

    .line 264
    :cond_a
    invoke-virtual {v3, v9}, La14;->G(I)V

    .line 265
    .line 266
    .line 267
    invoke-static {v1, v0}, Ljava/util/Arrays;->fill([II)V

    .line 268
    .line 269
    .line 270
    div-int/lit8 v10, v10, 0x5

    .line 271
    .line 272
    move v8, v0

    .line 273
    :goto_3
    if-ge v8, v10, :cond_b

    .line 274
    .line 275
    invoke-virtual {v3}, La14;->t()I

    .line 276
    .line 277
    .line 278
    move-result v9

    .line 279
    invoke-virtual {v3}, La14;->t()I

    .line 280
    .line 281
    .line 282
    move-result v14

    .line 283
    invoke-virtual {v3}, La14;->t()I

    .line 284
    .line 285
    .line 286
    move-result v15

    .line 287
    invoke-virtual {v3}, La14;->t()I

    .line 288
    .line 289
    .line 290
    move-result v16

    .line 291
    invoke-virtual {v3}, La14;->t()I

    .line 292
    .line 293
    .line 294
    move-result v17

    .line 295
    move/from16 p1, v6

    .line 296
    .line 297
    move-object/from16 p0, v7

    .line 298
    .line 299
    int-to-double v6, v14

    .line 300
    add-int/lit8 v15, v15, -0x80

    .line 301
    .line 302
    int-to-double v14, v15

    .line 303
    const-wide v18, 0x3ff66e978d4fdf3bL    # 1.402

    .line 304
    .line 305
    .line 306
    .line 307
    .line 308
    mul-double v18, v18, v14

    .line 309
    .line 310
    add-double v12, v18, v6

    .line 311
    .line 312
    double-to-int v12, v12

    .line 313
    add-int/lit8 v13, v16, -0x80

    .line 314
    .line 315
    move-object/from16 v16, v1

    .line 316
    .line 317
    int-to-double v0, v13

    .line 318
    const-wide v18, 0x3fd60663c74fb54aL    # 0.34414

    .line 319
    .line 320
    .line 321
    .line 322
    .line 323
    mul-double v18, v18, v0

    .line 324
    .line 325
    sub-double v18, v6, v18

    .line 326
    .line 327
    const-wide v20, 0x3fe6da3c21187e7cL    # 0.71414

    .line 328
    .line 329
    .line 330
    .line 331
    .line 332
    mul-double v14, v14, v20

    .line 333
    .line 334
    sub-double v13, v18, v14

    .line 335
    .line 336
    double-to-int v13, v13

    .line 337
    const-wide v14, 0x3ffc5a1cac083127L    # 1.772

    .line 338
    .line 339
    .line 340
    .line 341
    .line 342
    mul-double/2addr v0, v14

    .line 343
    add-double/2addr v0, v6

    .line 344
    double-to-int v0, v0

    .line 345
    shl-int/lit8 v1, v17, 0x18

    .line 346
    .line 347
    const/4 v6, 0x0

    .line 348
    invoke-static {v12, v6, v5}, Lfr5;->h(III)I

    .line 349
    .line 350
    .line 351
    move-result v7

    .line 352
    shl-int/lit8 v7, v7, 0x10

    .line 353
    .line 354
    or-int/2addr v1, v7

    .line 355
    invoke-static {v13, v6, v5}, Lfr5;->h(III)I

    .line 356
    .line 357
    .line 358
    move-result v7

    .line 359
    shl-int/lit8 v7, v7, 0x8

    .line 360
    .line 361
    or-int/2addr v1, v7

    .line 362
    invoke-static {v0, v6, v5}, Lfr5;->h(III)I

    .line 363
    .line 364
    .line 365
    move-result v0

    .line 366
    or-int/2addr v0, v1

    .line 367
    aput v0, v16, v9

    .line 368
    .line 369
    add-int/lit8 v8, v8, 0x1

    .line 370
    .line 371
    const/4 v0, 0x0

    .line 372
    move-object/from16 v7, p0

    .line 373
    .line 374
    move/from16 v6, p1

    .line 375
    .line 376
    move-object/from16 v1, v16

    .line 377
    .line 378
    goto :goto_3

    .line 379
    :cond_b
    move-object/from16 v16, v1

    .line 380
    .line 381
    move-object/from16 p0, v7

    .line 382
    .line 383
    const/4 v0, 0x1

    .line 384
    iput-boolean v0, v2, Lh24;->c:Z

    .line 385
    .line 386
    :goto_4
    const/4 v6, 0x0

    .line 387
    const/4 v12, 0x0

    .line 388
    goto/16 :goto_c

    .line 389
    .line 390
    :cond_c
    move-object/from16 v16, v1

    .line 391
    .line 392
    move-object/from16 p0, v7

    .line 393
    .line 394
    iget v0, v2, Lh24;->d:I

    .line 395
    .line 396
    if-eqz v0, :cond_13

    .line 397
    .line 398
    iget v0, v2, Lh24;->e:I

    .line 399
    .line 400
    if-eqz v0, :cond_13

    .line 401
    .line 402
    iget v0, v2, Lh24;->h:I

    .line 403
    .line 404
    if-eqz v0, :cond_13

    .line 405
    .line 406
    iget v0, v2, Lh24;->i:I

    .line 407
    .line 408
    if-eqz v0, :cond_13

    .line 409
    .line 410
    iget v0, v4, La14;->c:I

    .line 411
    .line 412
    if-eqz v0, :cond_13

    .line 413
    .line 414
    iget v1, v4, La14;->b:I

    .line 415
    .line 416
    if-ne v1, v0, :cond_13

    .line 417
    .line 418
    iget-boolean v0, v2, Lh24;->c:Z

    .line 419
    .line 420
    if-nez v0, :cond_d

    .line 421
    .line 422
    goto/16 :goto_a

    .line 423
    .line 424
    :cond_d
    const/4 v6, 0x0

    .line 425
    invoke-virtual {v4, v6}, La14;->F(I)V

    .line 426
    .line 427
    .line 428
    iget v0, v2, Lh24;->h:I

    .line 429
    .line 430
    iget v1, v2, Lh24;->i:I

    .line 431
    .line 432
    mul-int/2addr v0, v1

    .line 433
    new-array v1, v0, [I

    .line 434
    .line 435
    const/4 v6, 0x0

    .line 436
    :cond_e
    :goto_5
    if-ge v6, v0, :cond_12

    .line 437
    .line 438
    invoke-virtual {v4}, La14;->t()I

    .line 439
    .line 440
    .line 441
    move-result v7

    .line 442
    if-eqz v7, :cond_f

    .line 443
    .line 444
    add-int/lit8 v8, v6, 0x1

    .line 445
    .line 446
    aget v7, v16, v7

    .line 447
    .line 448
    aput v7, v1, v6

    .line 449
    .line 450
    :goto_6
    move v6, v8

    .line 451
    goto :goto_5

    .line 452
    :cond_f
    invoke-virtual {v4}, La14;->t()I

    .line 453
    .line 454
    .line 455
    move-result v7

    .line 456
    if-eqz v7, :cond_e

    .line 457
    .line 458
    and-int/lit8 v8, v7, 0x40

    .line 459
    .line 460
    if-nez v8, :cond_10

    .line 461
    .line 462
    and-int/lit8 v8, v7, 0x3f

    .line 463
    .line 464
    goto :goto_7

    .line 465
    :cond_10
    and-int/lit8 v8, v7, 0x3f

    .line 466
    .line 467
    shl-int/lit8 v8, v8, 0x8

    .line 468
    .line 469
    invoke-virtual {v4}, La14;->t()I

    .line 470
    .line 471
    .line 472
    move-result v9

    .line 473
    or-int/2addr v8, v9

    .line 474
    :goto_7
    and-int/lit16 v7, v7, 0x80

    .line 475
    .line 476
    if-nez v7, :cond_11

    .line 477
    .line 478
    const/4 v7, 0x0

    .line 479
    aget v9, v16, v7

    .line 480
    .line 481
    goto :goto_8

    .line 482
    :cond_11
    invoke-virtual {v4}, La14;->t()I

    .line 483
    .line 484
    .line 485
    move-result v7

    .line 486
    aget v9, v16, v7

    .line 487
    .line 488
    :goto_8
    add-int/2addr v8, v6

    .line 489
    invoke-static {v1, v6, v8, v9}, Ljava/util/Arrays;->fill([IIII)V

    .line 490
    .line 491
    .line 492
    goto :goto_6

    .line 493
    :cond_12
    iget v0, v2, Lh24;->h:I

    .line 494
    .line 495
    iget v6, v2, Lh24;->i:I

    .line 496
    .line 497
    sget-object v7, Landroid/graphics/Bitmap$Config;->ARGB_8888:Landroid/graphics/Bitmap$Config;

    .line 498
    .line 499
    invoke-static {v1, v0, v6, v7}, Landroid/graphics/Bitmap;->createBitmap([IIILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;

    .line 500
    .line 501
    .line 502
    move-result-object v21

    .line 503
    iget v0, v2, Lh24;->f:I

    .line 504
    .line 505
    int-to-float v0, v0

    .line 506
    iget v1, v2, Lh24;->d:I

    .line 507
    .line 508
    int-to-float v1, v1

    .line 509
    div-float v25, v0, v1

    .line 510
    .line 511
    iget v0, v2, Lh24;->g:I

    .line 512
    .line 513
    int-to-float v0, v0

    .line 514
    iget v6, v2, Lh24;->e:I

    .line 515
    .line 516
    int-to-float v6, v6

    .line 517
    div-float v22, v0, v6

    .line 518
    .line 519
    iget v0, v2, Lh24;->h:I

    .line 520
    .line 521
    int-to-float v0, v0

    .line 522
    div-float v29, v0, v1

    .line 523
    .line 524
    iget v0, v2, Lh24;->i:I

    .line 525
    .line 526
    int-to-float v0, v0

    .line 527
    div-float v30, v0, v6

    .line 528
    .line 529
    new-instance v17, Ltx0;

    .line 530
    .line 531
    const/16 v18, 0x0

    .line 532
    .line 533
    const/16 v23, 0x0

    .line 534
    .line 535
    const/16 v24, 0x0

    .line 536
    .line 537
    const/16 v26, 0x0

    .line 538
    .line 539
    const/high16 v27, -0x80000000

    .line 540
    .line 541
    const v28, -0x800001

    .line 542
    .line 543
    .line 544
    const/16 v31, 0x0

    .line 545
    .line 546
    const/high16 v32, -0x1000000

    .line 547
    .line 548
    const/16 v34, 0x0

    .line 549
    .line 550
    move-object/from16 v19, v18

    .line 551
    .line 552
    move-object/from16 v20, v18

    .line 553
    .line 554
    move/from16 v33, v27

    .line 555
    .line 556
    invoke-direct/range {v17 .. v34}, Ltx0;-><init>(Ljava/lang/CharSequence;Landroid/text/Layout$Alignment;Landroid/text/Layout$Alignment;Landroid/graphics/Bitmap;FIIFIIFFFZIIF)V

    .line 557
    .line 558
    .line 559
    move-object/from16 v12, v17

    .line 560
    .line 561
    :goto_9
    const/4 v6, 0x0

    .line 562
    goto :goto_b

    .line 563
    :cond_13
    :goto_a
    const/4 v12, 0x0

    .line 564
    goto :goto_9

    .line 565
    :goto_b
    iput v6, v2, Lh24;->d:I

    .line 566
    .line 567
    iput v6, v2, Lh24;->e:I

    .line 568
    .line 569
    iput v6, v2, Lh24;->f:I

    .line 570
    .line 571
    iput v6, v2, Lh24;->g:I

    .line 572
    .line 573
    iput v6, v2, Lh24;->h:I

    .line 574
    .line 575
    iput v6, v2, Lh24;->i:I

    .line 576
    .line 577
    invoke-virtual {v4, v6}, La14;->C(I)V

    .line 578
    .line 579
    .line 580
    iput-boolean v6, v2, Lh24;->c:Z

    .line 581
    .line 582
    :goto_c
    invoke-virtual {v3, v11}, La14;->F(I)V

    .line 583
    .line 584
    .line 585
    :goto_d
    move-object/from16 v7, p0

    .line 586
    .line 587
    if-eqz v12, :cond_14

    .line 588
    .line 589
    invoke-virtual {v7, v12}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 590
    .line 591
    .line 592
    :cond_14
    move v0, v6

    .line 593
    move-object/from16 v1, v16

    .line 594
    .line 595
    goto/16 :goto_0

    .line 596
    .line 597
    :cond_15
    new-instance v6, Lwx0;

    .line 598
    .line 599
    const-wide v8, -0x7fffffffffffffffL    # -4.9E-324

    .line 600
    .line 601
    .line 602
    .line 603
    .line 604
    const-wide v10, -0x7fffffffffffffffL    # -4.9E-324

    .line 605
    .line 606
    .line 607
    .line 608
    .line 609
    invoke-direct/range {v6 .. v11}, Lwx0;-><init>(Ljava/util/List;JJ)V

    .line 610
    .line 611
    .line 612
    move-object/from16 v0, p5

    .line 613
    .line 614
    invoke-interface {v0, v6}, Lwq0;->accept(Ljava/lang/Object;)V

    .line 615
    .line 616
    .line 617
    return-void

    .line 618
    nop

    .line 619
    :pswitch_data_0
    .packed-switch 0x14
        :pswitch_2
        :pswitch_1
        :pswitch_0
    .end packed-switch
.end method

.method public exists()Z
    .locals 2

    .line 1
    :try_start_0
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lb55;

    .line 4
    .line 5
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteOpenHelper;->getReadableDatabase()Landroid/database/sqlite/SQLiteDatabase;

    .line 6
    .line 7
    .line 8
    move-result-object v0

    .line 9
    iget-object p0, p0, Lt41;->c:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast p0, Ljava/lang/String;

    .line 12
    .line 13
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 14
    .line 15
    .line 16
    const/4 v1, 0x1

    .line 17
    invoke-static {v0, v1, p0}, Los5;->a(Landroid/database/sqlite/SQLiteDatabase;ILjava/lang/String;)I

    .line 18
    .line 19
    .line 20
    move-result p0
    :try_end_0
    .catch Landroid/database/SQLException; {:try_start_0 .. :try_end_0} :catch_0

    .line 21
    const/4 v0, -0x1

    .line 22
    if-eq p0, v0, :cond_0

    .line 23
    .line 24
    return v1

    .line 25
    :cond_0
    const/4 p0, 0x0

    .line 26
    return p0

    .line 27
    :catch_0
    move-exception p0

    .line 28
    new-instance v0, Landroidx/media3/database/DatabaseIOException;

    .line 29
    .line 30
    invoke-direct {v0, p0}, Landroidx/media3/database/DatabaseIOException;-><init>(Landroid/database/SQLException;)V

    .line 31
    .line 32
    .line 33
    throw v0
.end method

.method public f(Landroid/database/sqlite/SQLiteDatabase;Le30;)V
    .locals 4

    .line 1
    new-instance v0, Ljava/io/ByteArrayOutputStream;

    .line 2
    .line 3
    invoke-direct {v0}, Ljava/io/ByteArrayOutputStream;-><init>()V

    .line 4
    .line 5
    .line 6
    iget-object v1, p2, Le30;->e:Le41;

    .line 7
    .line 8
    new-instance v2, Ljava/io/DataOutputStream;

    .line 9
    .line 10
    invoke-direct {v2, v0}, Ljava/io/DataOutputStream;-><init>(Ljava/io/OutputStream;)V

    .line 11
    .line 12
    .line 13
    invoke-static {v1, v2}, Laf3;->i(Le41;Ljava/io/DataOutputStream;)V

    .line 14
    .line 15
    .line 16
    invoke-virtual {v0}, Ljava/io/ByteArrayOutputStream;->toByteArray()[B

    .line 17
    .line 18
    .line 19
    move-result-object v0

    .line 20
    new-instance v1, Landroid/content/ContentValues;

    .line 21
    .line 22
    invoke-direct {v1}, Landroid/content/ContentValues;-><init>()V

    .line 23
    .line 24
    .line 25
    iget v2, p2, Le30;->a:I

    .line 26
    .line 27
    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 28
    .line 29
    .line 30
    move-result-object v2

    .line 31
    const-string v3, "id"

    .line 32
    .line 33
    invoke-virtual {v1, v3, v2}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V

    .line 34
    .line 35
    .line 36
    const-string v2, "key"

    .line 37
    .line 38
    iget-object p2, p2, Le30;->b:Ljava/lang/String;

    .line 39
    .line 40
    invoke-virtual {v1, v2, p2}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/String;)V

    .line 41
    .line 42
    .line 43
    const-string p2, "metadata"

    .line 44
    .line 45
    invoke-virtual {v1, p2, v0}, Landroid/content/ContentValues;->put(Ljava/lang/String;[B)V

    .line 46
    .line 47
    .line 48
    iget-object p0, p0, Lt41;->d:Ljava/lang/Object;

    .line 49
    .line 50
    check-cast p0, Ljava/lang/String;

    .line 51
    .line 52
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 53
    .line 54
    .line 55
    const/4 p2, 0x0

    .line 56
    invoke-virtual {p1, p0, p2, v1}, Landroid/database/sqlite/SQLiteDatabase;->replaceOrThrow(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J

    .line 57
    .line 58
    .line 59
    return-void
.end method

.method public g(Lv4;Landroid/view/MenuItem;)Z
    .locals 2

    .line 1
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/view/ActionMode$Callback;

    .line 4
    .line 5
    invoke-virtual {p0, p1}, Lt41;->t(Lv4;)Lp95;

    .line 6
    .line 7
    .line 8
    move-result-object p1

    .line 9
    new-instance v1, Lod3;

    .line 10
    .line 11
    iget-object p0, p0, Lt41;->b:Ljava/lang/Object;

    .line 12
    .line 13
    check-cast p0, Landroid/content/Context;

    .line 14
    .line 15
    check-cast p2, Lt95;

    .line 16
    .line 17
    invoke-direct {v1, p0, p2}, Lod3;-><init>(Landroid/content/Context;Lt95;)V

    .line 18
    .line 19
    .line 20
    invoke-interface {v0, p1, v1}, Landroid/view/ActionMode$Callback;->onActionItemClicked(Landroid/view/ActionMode;Landroid/view/MenuItem;)Z

    .line 21
    .line 22
    .line 23
    move-result p0

    .line 24
    return p0
.end method

.method public get()Ljava/lang/Object;
    .locals 4

    .line 1
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lij2;

    .line 4
    .line 5
    iget-object v0, v0, Lij2;->a:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast v0, Lvu1;

    .line 8
    .line 9
    iget-object v1, p0, Lt41;->b:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast v1, Lmb4;

    .line 12
    .line 13
    invoke-interface {v1}, Lmb4;->get()Ljava/lang/Object;

    .line 14
    .line 15
    .line 16
    move-result-object v1

    .line 17
    check-cast v1, Ltv4;

    .line 18
    .line 19
    iget-object v2, p0, Lt41;->c:Ljava/lang/Object;

    .line 20
    .line 21
    check-cast v2, Lmb4;

    .line 22
    .line 23
    invoke-interface {v2}, Lmb4;->get()Ljava/lang/Object;

    .line 24
    .line 25
    .line 26
    move-result-object v2

    .line 27
    check-cast v2, Lns0;

    .line 28
    .line 29
    iget-object p0, p0, Lt41;->d:Ljava/lang/Object;

    .line 30
    .line 31
    check-cast p0, Lmb4;

    .line 32
    .line 33
    invoke-interface {p0}, Lmb4;->get()Ljava/lang/Object;

    .line 34
    .line 35
    .line 36
    move-result-object p0

    .line 37
    check-cast p0, Lmv4;

    .line 38
    .line 39
    new-instance v3, Lnv1;

    .line 40
    .line 41
    invoke-direct {v3, v0, v1, v2, p0}, Lnv1;-><init>(Lvu1;Ltv4;Lns0;Lmv4;)V

    .line 42
    .line 43
    .line 44
    return-object v3
.end method

.method public h(Ljava/util/HashMap;)V
    .locals 2

    .line 1
    :try_start_0
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lb55;

    .line 4
    .line 5
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteOpenHelper;->getWritableDatabase()Landroid/database/sqlite/SQLiteDatabase;

    .line 6
    .line 7
    .line 8
    move-result-object v0

    .line 9
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteDatabase;->beginTransactionNonExclusive()V
    :try_end_0
    .catch Landroid/database/SQLException; {:try_start_0 .. :try_end_0} :catch_0

    .line 10
    .line 11
    .line 12
    :try_start_1
    invoke-virtual {p0, v0}, Lt41;->y(Landroid/database/sqlite/SQLiteDatabase;)V

    .line 13
    .line 14
    .line 15
    invoke-virtual {p1}, Ljava/util/HashMap;->values()Ljava/util/Collection;

    .line 16
    .line 17
    .line 18
    move-result-object p1

    .line 19
    invoke-interface {p1}, Ljava/util/Collection;->iterator()Ljava/util/Iterator;

    .line 20
    .line 21
    .line 22
    move-result-object p1

    .line 23
    :goto_0
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    .line 24
    .line 25
    .line 26
    move-result v1

    .line 27
    if-eqz v1, :cond_0

    .line 28
    .line 29
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 30
    .line 31
    .line 32
    move-result-object v1

    .line 33
    check-cast v1, Le30;

    .line 34
    .line 35
    invoke-virtual {p0, v0, v1}, Lt41;->f(Landroid/database/sqlite/SQLiteDatabase;Le30;)V

    .line 36
    .line 37
    .line 38
    goto :goto_0

    .line 39
    :catchall_0
    move-exception p0

    .line 40
    goto :goto_1

    .line 41
    :cond_0
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteDatabase;->setTransactionSuccessful()V

    .line 42
    .line 43
    .line 44
    iget-object p0, p0, Lt41;->b:Ljava/lang/Object;

    .line 45
    .line 46
    check-cast p0, Landroid/util/SparseArray;

    .line 47
    .line 48
    invoke-virtual {p0}, Landroid/util/SparseArray;->clear()V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 49
    .line 50
    .line 51
    :try_start_2
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteDatabase;->endTransaction()V

    .line 52
    .line 53
    .line 54
    return-void

    .line 55
    :goto_1
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteDatabase;->endTransaction()V

    .line 56
    .line 57
    .line 58
    throw p0
    :try_end_2
    .catch Landroid/database/SQLException; {:try_start_2 .. :try_end_2} :catch_0

    .line 59
    :catch_0
    move-exception p0

    .line 60
    new-instance p1, Landroidx/media3/database/DatabaseIOException;

    .line 61
    .line 62
    invoke-direct {p1, p0}, Landroidx/media3/database/DatabaseIOException;-><init>(Landroid/database/SQLException;)V

    .line 63
    .line 64
    .line 65
    throw p1
.end method

.method public i(Lv4;)V
    .locals 1

    .line 1
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/view/ActionMode$Callback;

    .line 4
    .line 5
    invoke-virtual {p0, p1}, Lt41;->t(Lv4;)Lp95;

    .line 6
    .line 7
    .line 8
    move-result-object p0

    .line 9
    invoke-interface {v0, p0}, Landroid/view/ActionMode$Callback;->onDestroyActionMode(Landroid/view/ActionMode;)V

    .line 10
    .line 11
    .line 12
    return-void
.end method

.method public j(Le30;Z)V
    .locals 0

    .line 1
    iget-object p0, p0, Lt41;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Landroid/util/SparseArray;

    .line 4
    .line 5
    iget p1, p1, Le30;->a:I

    .line 6
    .line 7
    if-eqz p2, :cond_0

    .line 8
    .line 9
    invoke-virtual {p0, p1}, Landroid/util/SparseArray;->delete(I)V

    .line 10
    .line 11
    .line 12
    return-void

    .line 13
    :cond_0
    const/4 p2, 0x0

    .line 14
    invoke-virtual {p0, p1, p2}, Landroid/util/SparseArray;->put(ILjava/lang/Object;)V

    .line 15
    .line 16
    .line 17
    return-void
.end method

.method public l(Ljava/util/HashMap;Landroid/util/SparseArray;)V
    .locals 12

    .line 1
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lb55;

    .line 4
    .line 5
    iget-object v1, p0, Lt41;->b:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast v1, Landroid/util/SparseArray;

    .line 8
    .line 9
    invoke-virtual {v1}, Landroid/util/SparseArray;->size()I

    .line 10
    .line 11
    .line 12
    move-result v1

    .line 13
    const/4 v2, 0x0

    .line 14
    const/4 v3, 0x1

    .line 15
    if-nez v1, :cond_0

    .line 16
    .line 17
    move v1, v3

    .line 18
    goto :goto_0

    .line 19
    :cond_0
    move v1, v2

    .line 20
    :goto_0
    invoke-static {v1}, Ly43;->k(Z)V

    .line 21
    .line 22
    .line 23
    :try_start_0
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteOpenHelper;->getReadableDatabase()Landroid/database/sqlite/SQLiteDatabase;

    .line 24
    .line 25
    .line 26
    move-result-object v1

    .line 27
    iget-object v4, p0, Lt41;->c:Ljava/lang/Object;

    .line 28
    .line 29
    check-cast v4, Ljava/lang/String;

    .line 30
    .line 31
    invoke-virtual {v4}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 32
    .line 33
    .line 34
    invoke-static {v1, v3, v4}, Los5;->a(Landroid/database/sqlite/SQLiteDatabase;ILjava/lang/String;)I

    .line 35
    .line 36
    .line 37
    move-result v1

    .line 38
    if-eq v1, v3, :cond_1

    .line 39
    .line 40
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteOpenHelper;->getWritableDatabase()Landroid/database/sqlite/SQLiteDatabase;

    .line 41
    .line 42
    .line 43
    move-result-object v1

    .line 44
    invoke-virtual {v1}, Landroid/database/sqlite/SQLiteDatabase;->beginTransactionNonExclusive()V
    :try_end_0
    .catch Landroid/database/sqlite/SQLiteException; {:try_start_0 .. :try_end_0} :catch_0

    .line 45
    .line 46
    .line 47
    :try_start_1
    invoke-virtual {p0, v1}, Lt41;->y(Landroid/database/sqlite/SQLiteDatabase;)V

    .line 48
    .line 49
    .line 50
    invoke-virtual {v1}, Landroid/database/sqlite/SQLiteDatabase;->setTransactionSuccessful()V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 51
    .line 52
    .line 53
    :try_start_2
    invoke-virtual {v1}, Landroid/database/sqlite/SQLiteDatabase;->endTransaction()V

    .line 54
    .line 55
    .line 56
    goto :goto_1

    .line 57
    :catch_0
    move-exception v0

    .line 58
    move-object p0, v0

    .line 59
    goto :goto_5

    .line 60
    :catchall_0
    move-exception v0

    .line 61
    move-object p0, v0

    .line 62
    invoke-virtual {v1}, Landroid/database/sqlite/SQLiteDatabase;->endTransaction()V

    .line 63
    .line 64
    .line 65
    throw p0

    .line 66
    :cond_1
    :goto_1
    invoke-virtual {v0}, Landroid/database/sqlite/SQLiteOpenHelper;->getReadableDatabase()Landroid/database/sqlite/SQLiteDatabase;

    .line 67
    .line 68
    .line 69
    move-result-object v4

    .line 70
    iget-object p0, p0, Lt41;->d:Ljava/lang/Object;

    .line 71
    .line 72
    move-object v5, p0

    .line 73
    check-cast v5, Ljava/lang/String;

    .line 74
    .line 75
    invoke-virtual {v5}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 76
    .line 77
    .line 78
    sget-object v6, Lt41;->e:[Ljava/lang/String;

    .line 79
    .line 80
    const/4 v10, 0x0

    .line 81
    const/4 v11, 0x0

    .line 82
    const/4 v7, 0x0

    .line 83
    const/4 v8, 0x0

    .line 84
    const/4 v9, 0x0

    .line 85
    invoke-virtual/range {v4 .. v11}, Landroid/database/sqlite/SQLiteDatabase;->query(Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;

    .line 86
    .line 87
    .line 88
    move-result-object p0
    :try_end_2
    .catch Landroid/database/sqlite/SQLiteException; {:try_start_2 .. :try_end_2} :catch_0

    .line 89
    :goto_2
    :try_start_3
    invoke-interface {p0}, Landroid/database/Cursor;->moveToNext()Z

    .line 90
    .line 91
    .line 92
    move-result v0

    .line 93
    if-eqz v0, :cond_2

    .line 94
    .line 95
    invoke-interface {p0, v2}, Landroid/database/Cursor;->getInt(I)I

    .line 96
    .line 97
    .line 98
    move-result v0

    .line 99
    invoke-interface {p0, v3}, Landroid/database/Cursor;->getString(I)Ljava/lang/String;

    .line 100
    .line 101
    .line 102
    move-result-object v1

    .line 103
    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 104
    .line 105
    .line 106
    const/4 v4, 0x2

    .line 107
    invoke-interface {p0, v4}, Landroid/database/Cursor;->getBlob(I)[B

    .line 108
    .line 109
    .line 110
    move-result-object v4

    .line 111
    new-instance v5, Ljava/io/ByteArrayInputStream;

    .line 112
    .line 113
    invoke-direct {v5, v4}, Ljava/io/ByteArrayInputStream;-><init>([B)V

    .line 114
    .line 115
    .line 116
    new-instance v4, Ljava/io/DataInputStream;

    .line 117
    .line 118
    invoke-direct {v4, v5}, Ljava/io/DataInputStream;-><init>(Ljava/io/InputStream;)V

    .line 119
    .line 120
    .line 121
    invoke-static {v4}, Laf3;->e(Ljava/io/DataInputStream;)Le41;

    .line 122
    .line 123
    .line 124
    move-result-object v4

    .line 125
    new-instance v5, Le30;

    .line 126
    .line 127
    invoke-direct {v5, v0, v1, v4}, Le30;-><init>(ILjava/lang/String;Le41;)V

    .line 128
    .line 129
    .line 130
    invoke-virtual {p1, v1, v5}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 131
    .line 132
    .line 133
    invoke-virtual {p2, v0, v1}, Landroid/util/SparseArray;->put(ILjava/lang/Object;)V
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_1

    .line 134
    .line 135
    .line 136
    goto :goto_2

    .line 137
    :catchall_1
    move-exception v0

    .line 138
    move-object v1, v0

    .line 139
    goto :goto_3

    .line 140
    :cond_2
    :try_start_4
    invoke-interface {p0}, Landroid/database/Cursor;->close()V
    :try_end_4
    .catch Landroid/database/sqlite/SQLiteException; {:try_start_4 .. :try_end_4} :catch_0

    .line 141
    .line 142
    .line 143
    return-void

    .line 144
    :goto_3
    if-eqz p0, :cond_3

    .line 145
    .line 146
    :try_start_5
    invoke-interface {p0}, Landroid/database/Cursor;->close()V
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_2

    .line 147
    .line 148
    .line 149
    goto :goto_4

    .line 150
    :catchall_2
    move-exception v0

    .line 151
    move-object p0, v0

    .line 152
    :try_start_6
    invoke-virtual {v1, p0}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V

    .line 153
    .line 154
    .line 155
    :cond_3
    :goto_4
    throw v1
    :try_end_6
    .catch Landroid/database/sqlite/SQLiteException; {:try_start_6 .. :try_end_6} :catch_0

    .line 156
    :goto_5
    invoke-virtual {p1}, Ljava/util/HashMap;->clear()V

    .line 157
    .line 158
    .line 159
    invoke-virtual {p2}, Landroid/util/SparseArray;->clear()V

    .line 160
    .line 161
    .line 162
    new-instance p1, Landroidx/media3/database/DatabaseIOException;

    .line 163
    .line 164
    invoke-direct {p1, p0}, Landroidx/media3/database/DatabaseIOException;-><init>(Landroid/database/SQLException;)V

    .line 165
    .line 166
    .line 167
    throw p1
.end method

.method public m(Lv4;Landroid/view/Menu;)Z
    .locals 4

    .line 1
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/view/ActionMode$Callback;

    .line 4
    .line 5
    invoke-virtual {p0, p1}, Lt41;->t(Lv4;)Lp95;

    .line 6
    .line 7
    .line 8
    move-result-object p1

    .line 9
    iget-object v1, p0, Lt41;->d:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast v1, Lx05;

    .line 12
    .line 13
    invoke-virtual {v1, p2}, Lx05;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 14
    .line 15
    .line 16
    move-result-object v2

    .line 17
    check-cast v2, Landroid/view/Menu;

    .line 18
    .line 19
    if-nez v2, :cond_0

    .line 20
    .line 21
    new-instance v2, Lwd3;

    .line 22
    .line 23
    iget-object p0, p0, Lt41;->b:Ljava/lang/Object;

    .line 24
    .line 25
    check-cast p0, Landroid/content/Context;

    .line 26
    .line 27
    move-object v3, p2

    .line 28
    check-cast v3, Landroidx/appcompat/view/menu/MenuBuilder;

    .line 29
    .line 30
    invoke-direct {v2, p0, v3}, Lwd3;-><init>(Landroid/content/Context;Landroidx/appcompat/view/menu/MenuBuilder;)V

    .line 31
    .line 32
    .line 33
    invoke-virtual {v1, p2, v2}, Lx05;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 34
    .line 35
    .line 36
    :cond_0
    invoke-interface {v0, p1, v2}, Landroid/view/ActionMode$Callback;->onCreateActionMode(Landroid/view/ActionMode;Landroid/view/Menu;)Z

    .line 37
    .line 38
    .line 39
    move-result p0

    .line 40
    return p0
.end method

.method public n()Lj9;
    .locals 4

    .line 1
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Ljava/lang/Integer;

    .line 4
    .line 5
    const/4 v1, 0x0

    .line 6
    if-eqz v0, :cond_3

    .line 7
    .line 8
    iget-object v2, p0, Lt41;->b:Ljava/lang/Object;

    .line 9
    .line 10
    check-cast v2, Ljava/lang/Integer;

    .line 11
    .line 12
    if-eqz v2, :cond_2

    .line 13
    .line 14
    iget-object v2, p0, Lt41;->d:Ljava/lang/Object;

    .line 15
    .line 16
    check-cast v2, Li9;

    .line 17
    .line 18
    if-eqz v2, :cond_1

    .line 19
    .line 20
    iget-object v2, p0, Lt41;->c:Ljava/lang/Object;

    .line 21
    .line 22
    check-cast v2, Ljava/lang/Integer;

    .line 23
    .line 24
    if-eqz v2, :cond_0

    .line 25
    .line 26
    new-instance v1, Lj9;

    .line 27
    .line 28
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I

    .line 29
    .line 30
    .line 31
    move-result v0

    .line 32
    iget-object v2, p0, Lt41;->b:Ljava/lang/Object;

    .line 33
    .line 34
    check-cast v2, Ljava/lang/Integer;

    .line 35
    .line 36
    invoke-virtual {v2}, Ljava/lang/Integer;->intValue()I

    .line 37
    .line 38
    .line 39
    move-result v2

    .line 40
    iget-object v3, p0, Lt41;->c:Ljava/lang/Object;

    .line 41
    .line 42
    check-cast v3, Ljava/lang/Integer;

    .line 43
    .line 44
    invoke-virtual {v3}, Ljava/lang/Integer;->intValue()I

    .line 45
    .line 46
    .line 47
    move-result v3

    .line 48
    iget-object p0, p0, Lt41;->d:Ljava/lang/Object;

    .line 49
    .line 50
    check-cast p0, Li9;

    .line 51
    .line 52
    invoke-direct {v1, v0, v2, v3, p0}, Lj9;-><init>(IIILi9;)V

    .line 53
    .line 54
    .line 55
    return-object v1

    .line 56
    :cond_0
    const-string p0, "Tag size is not set"

    .line 57
    .line 58
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 59
    .line 60
    .line 61
    return-object v1

    .line 62
    :cond_1
    const-string p0, "Variant is not set"

    .line 63
    .line 64
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 65
    .line 66
    .line 67
    return-object v1

    .line 68
    :cond_2
    const-string p0, "IV size is not set"

    .line 69
    .line 70
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 71
    .line 72
    .line 73
    return-object v1

    .line 74
    :cond_3
    const-string p0, "Key size is not set"

    .line 75
    .line 76
    invoke-static {p0}, Lla6;->x(Ljava/lang/String;)V

    .line 77
    .line 78
    .line 79
    return-object v1
.end method

.method public o()Z
    .locals 15

    .line 1
    iget-object v0, p0, Lt41;->b:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/accounts/AccountManager;

    .line 4
    .line 5
    iget-object v1, p0, Lt41;->a:Ljava/lang/Object;

    .line 6
    .line 7
    check-cast v1, Landroid/content/Context;

    .line 8
    .line 9
    invoke-virtual {v1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    .line 10
    .line 11
    .line 12
    move-result-object v2

    .line 13
    sget-object v3, Ldm;->INSTANCE:Ldm;

    .line 14
    .line 15
    invoke-virtual {v3}, Ldm;->o()Z

    .line 16
    .line 17
    .line 18
    move-result v3

    .line 19
    const/4 v4, 0x0

    .line 20
    if-nez v3, :cond_9

    .line 21
    .line 22
    invoke-virtual {v1}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    .line 23
    .line 24
    .line 25
    move-result-object v3

    .line 26
    const-string v5, "android.permission.GET_ACCOUNTS"

    .line 27
    .line 28
    invoke-virtual {v1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    .line 29
    .line 30
    .line 31
    move-result-object v6

    .line 32
    invoke-virtual {v3, v5, v6}, Landroid/content/pm/PackageManager;->checkPermission(Ljava/lang/String;Ljava/lang/String;)I

    .line 33
    .line 34
    .line 35
    move-result v5

    .line 36
    const/4 v6, 0x1

    .line 37
    if-nez v5, :cond_0

    .line 38
    .line 39
    const-string v5, "android.permission.MANAGE_ACCOUNTS"

    .line 40
    .line 41
    invoke-virtual {v1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    .line 42
    .line 43
    .line 44
    move-result-object v7

    .line 45
    invoke-virtual {v3, v5, v7}, Landroid/content/pm/PackageManager;->checkPermission(Ljava/lang/String;Ljava/lang/String;)I

    .line 46
    .line 47
    .line 48
    move-result v5

    .line 49
    if-nez v5, :cond_0

    .line 50
    .line 51
    const-string v5, "android.permission.USE_CREDENTIALS"

    .line 52
    .line 53
    invoke-virtual {v1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    .line 54
    .line 55
    .line 56
    move-result-object v7

    .line 57
    invoke-virtual {v3, v5, v7}, Landroid/content/pm/PackageManager;->checkPermission(Ljava/lang/String;Ljava/lang/String;)I

    .line 58
    .line 59
    .line 60
    move-result v3

    .line 61
    if-nez v3, :cond_0

    .line 62
    .line 63
    move v3, v6

    .line 64
    goto :goto_0

    .line 65
    :cond_0
    move v3, v4

    .line 66
    :goto_0
    const-string v5, ""

    .line 67
    .line 68
    const-string v7, "BrokerProxy"

    .line 69
    .line 70
    if-nez v3, :cond_1

    .line 71
    .line 72
    const-string v8, "Broker related permissions are missing for GET_ACCOUNTS, MANAGE_ACCOUNTS, USE_CREDENTIALS"

    .line 73
    .line 74
    sget-object v9, Lf;->DEVELOPER_BROKER_PERMISSIONS_MISSING:Lf;

    .line 75
    .line 76
    invoke-static {v7, v8, v5, v9}, Lh23;->g(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lf;)V

    .line 77
    .line 78
    .line 79
    :cond_1
    if-eqz v3, :cond_9

    .line 80
    .line 81
    invoke-virtual {v0}, Landroid/accounts/AccountManager;->getAuthenticatorTypes()[Landroid/accounts/AuthenticatorDescription;

    .line 82
    .line 83
    .line 84
    move-result-object v3

    .line 85
    array-length v8, v3

    .line 86
    move v9, v4

    .line 87
    :goto_1
    if-ge v9, v8, :cond_9

    .line 88
    .line 89
    aget-object v10, v3, v9

    .line 90
    .line 91
    iget-object v11, v10, Landroid/accounts/AuthenticatorDescription;->type:Ljava/lang/String;

    .line 92
    .line 93
    const-string v12, "com.microsoft.workaccount"

    .line 94
    .line 95
    invoke-virtual {v11, v12}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 96
    .line 97
    .line 98
    move-result v11

    .line 99
    if-eqz v11, :cond_8

    .line 100
    .line 101
    invoke-virtual {v0, v12}, Landroid/accounts/AccountManager;->getAccountsByType(Ljava/lang/String;)[Landroid/accounts/Account;

    .line 102
    .line 103
    .line 104
    move-result-object v11

    .line 105
    iget-object v13, v10, Landroid/accounts/AuthenticatorDescription;->packageName:Ljava/lang/String;

    .line 106
    .line 107
    const-string v14, "com.microsoft.windowsintune.companyportal"

    .line 108
    .line 109
    invoke-virtual {v13, v14}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 110
    .line 111
    .line 112
    move-result v13

    .line 113
    const-string v14, "com.azure.authenticator"

    .line 114
    .line 115
    if-eqz v13, :cond_2

    .line 116
    .line 117
    if-eqz v11, :cond_9

    .line 118
    .line 119
    array-length v3, v11

    .line 120
    if-lez v3, :cond_9

    .line 121
    .line 122
    goto :goto_2

    .line 123
    :cond_2
    iget-object v13, v10, Landroid/accounts/AuthenticatorDescription;->packageName:Ljava/lang/String;

    .line 124
    .line 125
    invoke-virtual {v13, v14}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 126
    .line 127
    .line 128
    move-result v13

    .line 129
    if-nez v13, :cond_3

    .line 130
    .line 131
    iget-object v10, v10, Landroid/accounts/AuthenticatorDescription;->packageName:Ljava/lang/String;

    .line 132
    .line 133
    sget-object v13, Ldm;->INSTANCE:Ldm;

    .line 134
    .line 135
    invoke-virtual {v13}, Ldm;->g()Ljava/lang/String;

    .line 136
    .line 137
    .line 138
    move-result-object v13

    .line 139
    invoke-virtual {v10, v13}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 140
    .line 141
    .line 142
    move-result v10

    .line 143
    if-eqz v10, :cond_8

    .line 144
    .line 145
    :cond_3
    new-instance v3, Landroid/content/Intent;

    .line 146
    .line 147
    invoke-direct {v3}, Landroid/content/Intent;-><init>()V

    .line 148
    .line 149
    .line 150
    invoke-virtual {v3, v14}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;

    .line 151
    .line 152
    .line 153
    const-string v8, "com.azure.authenticator.ui.AccountChooserActivity"

    .line 154
    .line 155
    invoke-virtual {v3, v14, v8}, Landroid/content/Intent;->setClassName(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;

    .line 156
    .line 157
    .line 158
    invoke-virtual {v1}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    .line 159
    .line 160
    .line 161
    move-result-object v8

    .line 162
    invoke-virtual {v8, v3, v4}, Landroid/content/pm/PackageManager;->queryIntentActivities(Landroid/content/Intent;I)Ljava/util/List;

    .line 163
    .line 164
    .line 165
    move-result-object v3

    .line 166
    invoke-interface {v3}, Ljava/util/List;->size()I

    .line 167
    .line 168
    .line 169
    move-result v3

    .line 170
    if-lez v3, :cond_4

    .line 171
    .line 172
    const-string v3, "Broker supports to add user through app"

    .line 173
    .line 174
    invoke-static {v7, v3}, Lh23;->f(Ljava/lang/String;Ljava/lang/String;)V

    .line 175
    .line 176
    .line 177
    goto :goto_2

    .line 178
    :cond_4
    if-eqz v11, :cond_9

    .line 179
    .line 180
    array-length v3, v11

    .line 181
    if-lez v3, :cond_9

    .line 182
    .line 183
    :goto_2
    sget-object v3, Ldm;->INSTANCE:Ldm;

    .line 184
    .line 185
    invoke-virtual {v3}, Ldm;->g()Ljava/lang/String;

    .line 186
    .line 187
    .line 188
    move-result-object v3

    .line 189
    invoke-virtual {v2, v3}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 190
    .line 191
    .line 192
    move-result v3

    .line 193
    if-nez v3, :cond_9

    .line 194
    .line 195
    invoke-virtual {v2, v14}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 196
    .line 197
    .line 198
    move-result v2

    .line 199
    if-nez v2, :cond_9

    .line 200
    .line 201
    invoke-virtual {v0}, Landroid/accounts/AccountManager;->getAuthenticatorTypes()[Landroid/accounts/AuthenticatorDescription;

    .line 202
    .line 203
    .line 204
    move-result-object v0

    .line 205
    array-length v2, v0

    .line 206
    move v3, v4

    .line 207
    :goto_3
    if-ge v3, v2, :cond_9

    .line 208
    .line 209
    aget-object v8, v0, v3

    .line 210
    .line 211
    iget-object v9, v8, Landroid/accounts/AuthenticatorDescription;->type:Ljava/lang/String;

    .line 212
    .line 213
    invoke-virtual {v9, v12}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 214
    .line 215
    .line 216
    move-result v9

    .line 217
    if-eqz v9, :cond_7

    .line 218
    .line 219
    iget-object v8, v8, Landroid/accounts/AuthenticatorDescription;->packageName:Ljava/lang/String;

    .line 220
    .line 221
    :try_start_0
    invoke-virtual {v1}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    .line 222
    .line 223
    .line 224
    move-result-object v9

    .line 225
    const/16 v10, 0x40

    .line 226
    .line 227
    invoke-virtual {v9, v8, v10}, Landroid/content/pm/PackageManager;->getPackageInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;

    .line 228
    .line 229
    .line 230
    move-result-object v8

    .line 231
    if-eqz v8, :cond_7

    .line 232
    .line 233
    iget-object v8, v8, Landroid/content/pm/PackageInfo;->signatures:[Landroid/content/pm/Signature;

    .line 234
    .line 235
    if-eqz v8, :cond_7

    .line 236
    .line 237
    array-length v9, v8

    .line 238
    move v10, v4

    .line 239
    :goto_4
    if-ge v10, v9, :cond_7

    .line 240
    .line 241
    aget-object v11, v8, v10

    .line 242
    .line 243
    const-string v13, "SHA"

    .line 244
    .line 245
    invoke-static {v13}, Ljava/security/MessageDigest;->getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;

    .line 246
    .line 247
    .line 248
    move-result-object v13

    .line 249
    invoke-virtual {v11}, Landroid/content/pm/Signature;->toByteArray()[B

    .line 250
    .line 251
    .line 252
    move-result-object v11

    .line 253
    invoke-virtual {v13, v11}, Ljava/security/MessageDigest;->update([B)V

    .line 254
    .line 255
    .line 256
    invoke-virtual {v13}, Ljava/security/MessageDigest;->digest()[B

    .line 257
    .line 258
    .line 259
    move-result-object v11

    .line 260
    const/4 v13, 0x2

    .line 261
    invoke-static {v11, v13}, Landroid/util/Base64;->encodeToString([BI)Ljava/lang/String;

    .line 262
    .line 263
    .line 264
    move-result-object v11

    .line 265
    iget-object v13, p0, Lt41;->d:Ljava/lang/Object;

    .line 266
    .line 267
    check-cast v13, Ljava/lang/String;

    .line 268
    .line 269
    invoke-virtual {v11, v13}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 270
    .line 271
    .line 272
    move-result v13

    .line 273
    if-nez v13, :cond_6

    .line 274
    .line 275
    const-string v13, "ho040S3ffZkmxqtQrSwpTVOn9r0="

    .line 276
    .line 277
    invoke-virtual {v11, v13}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    .line 278
    .line 279
    .line 280
    move-result v11
    :try_end_0
    .catch Landroid/content/pm/PackageManager$NameNotFoundException; {:try_start_0 .. :try_end_0} :catch_2
    .catch Ljava/security/NoSuchAlgorithmException; {:try_start_0 .. :try_end_0} :catch_1
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    .line 281
    if-eqz v11, :cond_5

    .line 282
    .line 283
    goto :goto_5

    .line 284
    :cond_5
    add-int/lit8 v10, v10, 0x1

    .line 285
    .line 286
    goto :goto_4

    .line 287
    :catch_0
    move-exception v8

    .line 288
    goto :goto_6

    .line 289
    :cond_6
    :goto_5
    return v6

    .line 290
    :goto_6
    const-string v9, "Error in verifying signature"

    .line 291
    .line 292
    sget-object v10, Lf;->BROKER_VERIFICATION_FAILED:Lf;

    .line 293
    .line 294
    invoke-static {v7, v9, v5, v10, v8}, Lh23;->c(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lf;Ljava/lang/Exception;)V

    .line 295
    .line 296
    .line 297
    goto :goto_7

    .line 298
    :catch_1
    const-string v8, "Digest SHA algorithm does not exists"

    .line 299
    .line 300
    sget-object v9, Lf;->DEVICE_NO_SUCH_ALGORITHM:Lf;

    .line 301
    .line 302
    invoke-static {v7, v8, v5, v9}, Lh23;->b(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lf;)V

    .line 303
    .line 304
    .line 305
    goto :goto_7

    .line 306
    :catch_2
    const-string v8, "Broker related package does not exist"

    .line 307
    .line 308
    sget-object v9, Lf;->BROKER_PACKAGE_NAME_NOT_FOUND:Lf;

    .line 309
    .line 310
    invoke-static {v7, v8, v5, v9}, Lh23;->b(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lf;)V

    .line 311
    .line 312
    .line 313
    :cond_7
    :goto_7
    add-int/lit8 v3, v3, 0x1

    .line 314
    .line 315
    goto :goto_3

    .line 316
    :cond_8
    add-int/lit8 v9, v9, 0x1

    .line 317
    .line 318
    goto/16 :goto_1

    .line 319
    .line 320
    :cond_9
    return v4
.end method

.method public p(Lv4;Landroid/view/Menu;)Z
    .locals 4

    .line 1
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Landroid/view/ActionMode$Callback;

    .line 4
    .line 5
    invoke-virtual {p0, p1}, Lt41;->t(Lv4;)Lp95;

    .line 6
    .line 7
    .line 8
    move-result-object p1

    .line 9
    iget-object v1, p0, Lt41;->d:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast v1, Lx05;

    .line 12
    .line 13
    invoke-virtual {v1, p2}, Lx05;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 14
    .line 15
    .line 16
    move-result-object v2

    .line 17
    check-cast v2, Landroid/view/Menu;

    .line 18
    .line 19
    if-nez v2, :cond_0

    .line 20
    .line 21
    new-instance v2, Lwd3;

    .line 22
    .line 23
    iget-object p0, p0, Lt41;->b:Ljava/lang/Object;

    .line 24
    .line 25
    check-cast p0, Landroid/content/Context;

    .line 26
    .line 27
    move-object v3, p2

    .line 28
    check-cast v3, Landroidx/appcompat/view/menu/MenuBuilder;

    .line 29
    .line 30
    invoke-direct {v2, p0, v3}, Lwd3;-><init>(Landroid/content/Context;Landroidx/appcompat/view/menu/MenuBuilder;)V

    .line 31
    .line 32
    .line 33
    invoke-virtual {v1, p2, v2}, Lx05;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 34
    .line 35
    .line 36
    :cond_0
    invoke-interface {v0, p1, v2}, Landroid/view/ActionMode$Callback;->onPrepareActionMode(Landroid/view/ActionMode;Landroid/view/Menu;)Z

    .line 37
    .line 38
    .line 39
    move-result p0

    .line 40
    return p0
.end method

.method public q(Ljava/lang/Object;Ljava/util/ArrayList;Ljava/util/HashSet;)V
    .locals 4

    .line 1
    invoke-virtual {p2, p1}, Ljava/util/ArrayList;->contains(Ljava/lang/Object;)Z

    .line 2
    .line 3
    .line 4
    move-result v0

    .line 5
    if-eqz v0, :cond_0

    .line 6
    .line 7
    return-void

    .line 8
    :cond_0
    invoke-virtual {p3, p1}, Ljava/util/HashSet;->contains(Ljava/lang/Object;)Z

    .line 9
    .line 10
    .line 11
    move-result v0

    .line 12
    if-nez v0, :cond_2

    .line 13
    .line 14
    invoke-virtual {p3, p1}, Ljava/util/HashSet;->add(Ljava/lang/Object;)Z

    .line 15
    .line 16
    .line 17
    iget-object v0, p0, Lt41;->b:Ljava/lang/Object;

    .line 18
    .line 19
    check-cast v0, Lx05;

    .line 20
    .line 21
    invoke-virtual {v0, p1}, Lx05;->get(Ljava/lang/Object;)Ljava/lang/Object;

    .line 22
    .line 23
    .line 24
    move-result-object v0

    .line 25
    check-cast v0, Ljava/util/ArrayList;

    .line 26
    .line 27
    if-eqz v0, :cond_1

    .line 28
    .line 29
    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    .line 30
    .line 31
    .line 32
    move-result v1

    .line 33
    const/4 v2, 0x0

    .line 34
    :goto_0
    if-ge v2, v1, :cond_1

    .line 35
    .line 36
    invoke-virtual {v0, v2}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    .line 37
    .line 38
    .line 39
    move-result-object v3

    .line 40
    invoke-virtual {p0, v3, p2, p3}, Lt41;->q(Ljava/lang/Object;Ljava/util/ArrayList;Ljava/util/HashSet;)V

    .line 41
    .line 42
    .line 43
    add-int/lit8 v2, v2, 0x1

    .line 44
    .line 45
    goto :goto_0

    .line 46
    :cond_1
    invoke-virtual {p3, p1}, Ljava/util/HashSet;->remove(Ljava/lang/Object;)Z

    .line 47
    .line 48
    .line 49
    invoke-virtual {p2, p1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 50
    .line 51
    .line 52
    return-void

    .line 53
    :cond_2
    const-string p0, "This graph contains cyclic dependencies"

    .line 54
    .line 55
    invoke-static {p0}, Lla6;->h(Ljava/lang/String;)V

    .line 56
    .line 57
    .line 58
    return-void
.end method

.method public r([B)V
    .locals 3

    .line 1
    iget-object v0, p0, Lt41;->c:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Llj4;

    .line 4
    .line 5
    if-eqz v0, :cond_1

    .line 6
    .line 7
    new-instance v1, Lo22;

    .line 8
    .line 9
    iget-object v2, p0, Lt41;->d:Ljava/lang/Object;

    .line 10
    .line 11
    check-cast v2, Lwb4;

    .line 12
    .line 13
    iget v2, v2, Lwb4;->b:I

    .line 14
    .line 15
    invoke-direct {v1, v0, p1, v2}, Lo22;-><init>(Llj4;[BI)V

    .line 16
    .line 17
    .line 18
    iget-object p1, p0, Lt41;->b:Ljava/lang/Object;

    .line 19
    .line 20
    check-cast p1, Ljava/util/LinkedHashSet;

    .line 21
    .line 22
    invoke-interface {p1}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;

    .line 23
    .line 24
    .line 25
    move-result-object p1

    .line 26
    :goto_0
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    .line 27
    .line 28
    .line 29
    move-result v0

    .line 30
    if-eqz v0, :cond_0

    .line 31
    .line 32
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 33
    .line 34
    .line 35
    move-result-object v0

    .line 36
    check-cast v0, Lf32;

    .line 37
    .line 38
    invoke-interface {v0, v1}, Lf32;->invoke(Ljava/lang/Object;)Ljava/lang/Object;

    .line 39
    .line 40
    .line 41
    goto :goto_0

    .line 42
    :cond_0
    iget-object p0, p0, Lt41;->a:Ljava/lang/Object;

    .line 43
    .line 44
    check-cast p0, Landroid/hardware/Camera;

    .line 45
    .line 46
    iget-object p1, v1, Lo22;->b:[B

    .line 47
    .line 48
    invoke-virtual {p0, p1}, Landroid/hardware/Camera;->addCallbackBuffer([B)V

    .line 49
    .line 50
    .line 51
    return-void

    .line 52
    :cond_1
    const-string p0, "previewSize is null. Frame was not added?"

    .line 53
    .line 54
    invoke-static {p0}, Lr05;->k(Ljava/lang/String;)V

    .line 55
    .line 56
    .line 57
    return-void
.end method

.method public s(Lwr0;)Ljava/lang/Object;
    .locals 7

    .line 1
    iget-object v0, p0, Lt41;->d:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Ld11;

    .line 4
    .line 5
    instance-of v1, p1, Le01;

    .line 6
    .line 7
    if-eqz v1, :cond_0

    .line 8
    .line 9
    move-object v1, p1

    .line 10
    check-cast v1, Le01;

    .line 11
    .line 12
    iget v2, v1, Le01;->w:I

    .line 13
    .line 14
    const/high16 v3, -0x80000000

    .line 15
    .line 16
    and-int v4, v2, v3

    .line 17
    .line 18
    if-eqz v4, :cond_0

    .line 19
    .line 20
    sub-int/2addr v2, v3

    .line 21
    iput v2, v1, Le01;->w:I

    .line 22
    .line 23
    goto :goto_0

    .line 24
    :cond_0
    new-instance v1, Le01;

    .line 25
    .line 26
    invoke-direct {v1, p0, p1}, Le01;-><init>(Lt41;Lwr0;)V

    .line 27
    .line 28
    .line 29
    :goto_0
    iget-object p1, v1, Le01;->u:Ljava/lang/Object;

    .line 30
    .line 31
    sget-object v2, Lzs0;->COROUTINE_SUSPENDED:Lzs0;

    .line 32
    .line 33
    iget v3, v1, Le01;->w:I

    .line 34
    .line 35
    const/4 v4, 0x0

    .line 36
    const/4 v5, 0x2

    .line 37
    const/4 v6, 0x1

    .line 38
    if-eqz v3, :cond_3

    .line 39
    .line 40
    if-eq v3, v6, :cond_2

    .line 41
    .line 42
    if-ne v3, v5, :cond_1

    .line 43
    .line 44
    iget-object p0, v1, Le01;->t:Lt41;

    .line 45
    .line 46
    invoke-static {p1}, Ltq3;->I(Ljava/lang/Object;)V

    .line 47
    .line 48
    .line 49
    goto :goto_1

    .line 50
    :cond_1
    const-string p0, "call to \'resume\' before \'invoke\' with coroutine"

    .line 51
    .line 52
    invoke-static {p0}, Lr05;->k(Ljava/lang/String;)V

    .line 53
    .line 54
    .line 55
    return-object v4

    .line 56
    :cond_2
    iget-object p0, v1, Le01;->t:Lt41;

    .line 57
    .line 58
    invoke-static {p1}, Ltq3;->I(Ljava/lang/Object;)V

    .line 59
    .line 60
    .line 61
    goto :goto_4

    .line 62
    :cond_3
    invoke-static {p1}, Ltq3;->I(Ljava/lang/Object;)V

    .line 63
    .line 64
    .line 65
    iget-object p1, p0, Lt41;->c:Ljava/lang/Object;

    .line 66
    .line 67
    check-cast p1, Ljava/util/List;

    .line 68
    .line 69
    if-eqz p1, :cond_6

    .line 70
    .line 71
    invoke-interface {p1}, Ljava/util/List;->isEmpty()Z

    .line 72
    .line 73
    .line 74
    move-result p1

    .line 75
    if-eqz p1, :cond_4

    .line 76
    .line 77
    goto :goto_2

    .line 78
    :cond_4
    invoke-virtual {v0}, Ld11;->g()Lj15;

    .line 79
    .line 80
    .line 81
    move-result-object p1

    .line 82
    new-instance v3, Lh01;

    .line 83
    .line 84
    invoke-direct {v3, v0, p0, v4}, Lh01;-><init>(Ld11;Lt41;Lur0;)V

    .line 85
    .line 86
    .line 87
    iput-object p0, v1, Le01;->t:Lt41;

    .line 88
    .line 89
    iput v5, v1, Le01;->w:I

    .line 90
    .line 91
    invoke-virtual {p1, v3, v1}, Lj15;->b(Lf32;Lwr0;)Ljava/lang/Object;

    .line 92
    .line 93
    .line 94
    move-result-object p1

    .line 95
    if-ne p1, v2, :cond_5

    .line 96
    .line 97
    goto :goto_3

    .line 98
    :cond_5
    :goto_1
    check-cast p1, Loz0;

    .line 99
    .line 100
    goto :goto_5

    .line 101
    :cond_6
    :goto_2
    iput-object p0, v1, Le01;->t:Lt41;

    .line 102
    .line 103
    iput v6, v1, Le01;->w:I

    .line 104
    .line 105
    const/4 p1, 0x0

    .line 106
    invoke-static {v0, p1, v1}, Ld11;->f(Ld11;ZLwr0;)Ljava/lang/Object;

    .line 107
    .line 108
    .line 109
    move-result-object p1

    .line 110
    if-ne p1, v2, :cond_7

    .line 111
    .line 112
    :goto_3
    return-object v2

    .line 113
    :cond_7
    :goto_4
    check-cast p1, Loz0;

    .line 114
    .line 115
    :goto_5
    iget-object p0, p0, Lt41;->d:Ljava/lang/Object;

    .line 116
    .line 117
    check-cast p0, Ld11;

    .line 118
    .line 119
    iget-object p0, p0, Ld11;->h:Lz33;

    .line 120
    .line 121
    invoke-virtual {p0, p1}, Lz33;->t(Lt55;)V

    .line 122
    .line 123
    .line 124
    sget-object p0, Len5;->a:Len5;

    .line 125
    .line 126
    return-object p0
.end method

.method public t(Lv4;)Lp95;
    .locals 5

    .line 1
    iget-object v0, p0, Lt41;->c:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Ljava/util/ArrayList;

    .line 4
    .line 5
    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    .line 6
    .line 7
    .line 8
    move-result v1

    .line 9
    const/4 v2, 0x0

    .line 10
    :goto_0
    if-ge v2, v1, :cond_1

    .line 11
    .line 12
    invoke-virtual {v0, v2}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    .line 13
    .line 14
    .line 15
    move-result-object v3

    .line 16
    check-cast v3, Lp95;

    .line 17
    .line 18
    if-eqz v3, :cond_0

    .line 19
    .line 20
    iget-object v4, v3, Lp95;->b:Lv4;

    .line 21
    .line 22
    if-ne v4, p1, :cond_0

    .line 23
    .line 24
    return-object v3

    .line 25
    :cond_0
    add-int/lit8 v2, v2, 0x1

    .line 26
    .line 27
    goto :goto_0

    .line 28
    :cond_1
    new-instance v1, Lp95;

    .line 29
    .line 30
    iget-object p0, p0, Lt41;->b:Ljava/lang/Object;

    .line 31
    .line 32
    check-cast p0, Landroid/content/Context;

    .line 33
    .line 34
    invoke-direct {v1, p0, p1}, Lp95;-><init>(Landroid/content/Context;Lv4;)V

    .line 35
    .line 36
    .line 37
    invoke-virtual {v0, v1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 38
    .line 39
    .line 40
    return-object v1
.end method

.method public u(Lzl;)Lcm;
    .locals 14

    .line 1
    const-string v1, "Authenticator cancels the request"

    .line 2
    .line 3
    iget-object v0, p0, Lt41;->a:Ljava/lang/Object;

    .line 4
    .line 5
    check-cast v0, Landroid/content/Context;

    .line 6
    .line 7
    invoke-static {}, Landroid/os/Looper;->myLooper()Landroid/os/Looper;

    .line 8
    .line 9
    .line 10
    move-result-object v2

    .line 11
    const-string v3, ""

    .line 12
    .line 13
    const-string v4, "BrokerProxy"

    .line 14
    .line 15
    if-eqz v2, :cond_1

    .line 16
    .line 17
    invoke-virtual {v0}, Landroid/content/Context;->getMainLooper()Landroid/os/Looper;

    .line 18
    .line 19
    .line 20
    move-result-object v5

    .line 21
    if-ne v2, v5, :cond_1

    .line 22
    .line 23
    new-instance v2, Ljava/lang/IllegalStateException;

    .line 24
    .line 25
    const-string v5, "calling this from your main thread can lead to deadlock"

    .line 26
    .line 27
    invoke-direct {v2, v5}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    .line 28
    .line 29
    .line 30
    const-string v5, "calling this from your main thread can lead to deadlock and/or ANRs"

    .line 31
    .line 32
    sget-object v6, Lf;->DEVELOPER_CALLING_ON_MAIN_THREAD:Lf;

    .line 33
    .line 34
    invoke-static {v4, v5, v3, v6, v2}, Lh23;->c(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lf;Ljava/lang/Exception;)V

    .line 35
    .line 36
    .line 37
    invoke-virtual {v0}, Landroid/content/Context;->getApplicationInfo()Landroid/content/pm/ApplicationInfo;

    .line 38
    .line 39
    .line 40
    move-result-object v0

    .line 41
    iget v0, v0, Landroid/content/pm/ApplicationInfo;->targetSdkVersion:I

    .line 42
    .line 43
    const/16 v5, 0x8

    .line 44
    .line 45
    if-ge v0, v5, :cond_0

    .line 46
    .line 47
    goto :goto_0

    .line 48
    :cond_0
    throw v2

    .line 49
    :cond_1
    :goto_0
    iget-object v0, p0, Lt41;->b:Ljava/lang/Object;

    .line 50
    .line 51
    check-cast v0, Landroid/accounts/AccountManager;

    .line 52
    .line 53
    const-string v2, "com.microsoft.workaccount"

    .line 54
    .line 55
    invoke-virtual {v0, v2}, Landroid/accounts/AccountManager;->getAccountsByType(Ljava/lang/String;)[Landroid/accounts/Account;

    .line 56
    .line 57
    .line 58
    move-result-object v0

    .line 59
    iget-object v2, p1, Lzl;->h:Ljava/lang/String;

    .line 60
    .line 61
    invoke-static {v2}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    .line 62
    .line 63
    .line 64
    move-result v2

    .line 65
    const/4 v5, 0x0

    .line 66
    const/4 v6, 0x0

    .line 67
    if-nez v2, :cond_3

    .line 68
    .line 69
    iget-object v2, p1, Lzl;->h:Ljava/lang/String;

    .line 70
    .line 71
    if-eqz v0, :cond_7

    .line 72
    .line 73
    array-length v7, v0

    .line 74
    :goto_1
    if-ge v5, v7, :cond_7

    .line 75
    .line 76
    aget-object v8, v0, v5

    .line 77
    .line 78
    if-eqz v8, :cond_2

    .line 79
    .line 80
    iget-object v9, v8, Landroid/accounts/Account;->name:Ljava/lang/String;

    .line 81
    .line 82
    if-eqz v9, :cond_2

    .line 83
    .line 84
    invoke-virtual {v9, v2}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 85
    .line 86
    .line 87
    move-result v9

    .line 88
    if-eqz v9, :cond_2

    .line 89
    .line 90
    goto :goto_5

    .line 91
    :cond_2
    add-int/lit8 v5, v5, 0x1

    .line 92
    .line 93
    goto :goto_1

    .line 94
    :cond_3
    :try_start_0
    invoke-virtual {p0}, Lt41;->w()[Ldq5;

    .line 95
    .line 96
    .line 97
    move-result-object v2

    .line 98
    iget-object v7, p1, Lzl;->g:Ljava/lang/String;

    .line 99
    .line 100
    if-eqz v2, :cond_5

    .line 101
    .line 102
    array-length v8, v2

    .line 103
    move v9, v5

    .line 104
    :goto_2
    if-ge v9, v8, :cond_5

    .line 105
    .line 106
    aget-object v10, v2, v9

    .line 107
    .line 108
    if-eqz v10, :cond_4

    .line 109
    .line 110
    iget-object v11, v10, Ldq5;->a:Ljava/lang/String;

    .line 111
    .line 112
    invoke-static {v11}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    .line 113
    .line 114
    .line 115
    move-result v12

    .line 116
    if-nez v12, :cond_4

    .line 117
    .line 118
    invoke-virtual {v11, v7}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 119
    .line 120
    .line 121
    move-result v11

    .line 122
    if-eqz v11, :cond_4

    .line 123
    .line 124
    goto :goto_3

    .line 125
    :cond_4
    add-int/lit8 v9, v9, 0x1

    .line 126
    .line 127
    goto :goto_2

    .line 128
    :cond_5
    move-object v10, v6

    .line 129
    :goto_3
    if-eqz v10, :cond_7

    .line 130
    .line 131
    iget-object v2, v10, Ldq5;->b:Ljava/lang/String;

    .line 132
    .line 133
    if-eqz v0, :cond_7

    .line 134
    .line 135
    array-length v7, v0

    .line 136
    :goto_4
    if-ge v5, v7, :cond_7

    .line 137
    .line 138
    aget-object v8, v0, v5

    .line 139
    .line 140
    if-eqz v8, :cond_6

    .line 141
    .line 142
    iget-object v9, v8, Landroid/accounts/Account;->name:Ljava/lang/String;

    .line 143
    .line 144
    if-eqz v9, :cond_6

    .line 145
    .line 146
    invoke-virtual {v9, v2}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    .line 147
    .line 148
    .line 149
    move-result v9
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    .line 150
    if-eqz v9, :cond_6

    .line 151
    .line 152
    goto :goto_5

    .line 153
    :cond_6
    add-int/lit8 v5, v5, 0x1

    .line 154
    .line 155
    goto :goto_4

    .line 156
    :catch_0
    move-exception v0

    .line 157
    invoke-virtual {v0}, Ljava/lang/Throwable;->getMessage()Ljava/lang/String;

    .line 158
    .line 159
    .line 160
    move-result-object v2

    .line 161
    sget-object v5, Lf;->BROKER_AUTHENTICATOR_IO_EXCEPTION:Lf;

    .line 162
    .line 163
    invoke-static {v4, v2, v3, v5, v0}, Lh23;->c(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lf;Ljava/lang/Exception;)V

    .line 164
    .line 165
    .line 166
    :cond_7
    move-object v8, v6

    .line 167
    :goto_5
    if-eqz v8, :cond_8

    .line 168
    .line 169
    invoke-static {p1}, Lt41;->v(Lzl;)Landroid/os/Bundle;

    .line 170
    .line 171
    .line 172
    move-result-object v10

    .line 173
    :try_start_1
    iget-object p1, p0, Lt41;->b:Ljava/lang/Object;

    .line 174
    .line 175
    move-object v7, p1

    .line 176
    check-cast v7, Landroid/accounts/AccountManager;

    .line 177
    .line 178
    const-string v9, "adal.authtoken.type"

    .line 179
    .line 180
    iget-object p0, p0, Lt41;->c:Ljava/lang/Object;

    .line 181
    .line 182
    move-object v13, p0

    .line 183
    check-cast v13, Landroid/os/Handler;

    .line 184
    .line 185
    const/4 v11, 0x0

    .line 186
    const/4 v12, 0x0

    .line 187
    invoke-virtual/range {v7 .. v13}, Landroid/accounts/AccountManager;->getAuthToken(Landroid/accounts/Account;Ljava/lang/String;Landroid/os/Bundle;ZLandroid/accounts/AccountManagerCallback;Landroid/os/Handler;)Landroid/accounts/AccountManagerFuture;

    .line 188
    .line 189
    .line 190
    move-result-object p0

    .line 191
    const-string p1, "Received result from Authenticator"

    .line 192
    .line 193
    invoke-static {v4, p1}, Lh23;->f(Ljava/lang/String;Ljava/lang/String;)V

    .line 194
    .line 195
    .line 196
    invoke-interface {p0}, Landroid/accounts/AccountManagerFuture;->getResult()Ljava/lang/Object;

    .line 197
    .line 198
    .line 199
    move-result-object p0

    .line 200
    check-cast p0, Landroid/os/Bundle;

    .line 201
    .line 202
    invoke-static {p0}, Lt41;->x(Landroid/os/Bundle;)Lcm;

    .line 203
    .line 204
    .line 205
    move-result-object v6
    :try_end_1
    .catch Landroid/accounts/OperationCanceledException; {:try_start_1 .. :try_end_1} :catch_1
    .catch Landroid/accounts/AuthenticatorException; {:try_start_1 .. :try_end_1} :catch_3
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_2

    .line 206
    goto :goto_7

    .line 207
    :catch_1
    move-exception v0

    .line 208
    move-object p0, v0

    .line 209
    goto :goto_6

    .line 210
    :catch_2
    sget-object p0, Lf;->BROKER_AUTHENTICATOR_IO_EXCEPTION:Lf;

    .line 211
    .line 212
    invoke-static {v4, v1, v3, p0}, Lh23;->b(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lf;)V

    .line 213
    .line 214
    .line 215
    goto :goto_7

    .line 216
    :catch_3
    sget-object p0, Lf;->BROKER_AUTHENTICATOR_NOT_RESPONDING:Lf;

    .line 217
    .line 218
    invoke-static {v4, v1, v3, p0}, Lh23;->b(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lf;)V

    .line 219
    .line 220
    .line 221
    goto :goto_7

    .line 222
    :goto_6
    sget-object p1, Lf;->AUTH_FAILED_CANCELLED:Lf;

    .line 223
    .line 224
    invoke-static {v4, v1, v3, p1, p0}, Lh23;->c(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lf;Ljava/lang/Exception;)V

    .line 225
    .line 226
    .line 227
    :goto_7
    const-string p0, "Returning result from Authenticator"

    .line 228
    .line 229
    invoke-static {v4, p0}, Lh23;->f(Ljava/lang/String;Ljava/lang/String;)V

    .line 230
    .line 231
    .line 232
    return-object v6

    .line 233
    :cond_8
    const-string p0, "Target account is not found"

    .line 234
    .line 235
    invoke-static {v4, p0}, Lh23;->f(Ljava/lang/String;Ljava/lang/String;)V

    .line 236
    .line 237
    .line 238
    return-object v6
.end method

.method public w()[Ldq5;
    .locals 13

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
    const/4 v2, 0x0

    .line 10
    if-eq v0, v1, :cond_2

    .line 11
    .line 12
    iget-object v0, p0, Lt41;->b:Ljava/lang/Object;

    .line 13
    .line 14
    check-cast v0, Landroid/accounts/AccountManager;

    .line 15
    .line 16
    const-string v1, "com.microsoft.workaccount"

    .line 17
    .line 18
    invoke-virtual {v0, v1}, Landroid/accounts/AccountManager;->getAccountsByType(Ljava/lang/String;)[Landroid/accounts/Account;

    .line 19
    .line 20
    .line 21
    move-result-object v0

    .line 22
    new-instance v6, Landroid/os/Bundle;

    .line 23
    .line 24
    invoke-direct {v6}, Landroid/os/Bundle;-><init>()V

    .line 25
    .line 26
    .line 27
    const-string v1, "com.microsoft.workaccount.user.info"

    .line 28
    .line 29
    const/4 v3, 0x1

    .line 30
    invoke-virtual {v6, v1, v3}, Landroid/os/BaseBundle;->putBoolean(Ljava/lang/String;Z)V

    .line 31
    .line 32
    .line 33
    if-eqz v0, :cond_1

    .line 34
    .line 35
    array-length v1, v0

    .line 36
    new-array v1, v1, [Ldq5;

    .line 37
    .line 38
    const/4 v2, 0x0

    .line 39
    :goto_0
    array-length v3, v0

    .line 40
    if-ge v2, v3, :cond_0

    .line 41
    .line 42
    iget-object v3, p0, Lt41;->b:Ljava/lang/Object;

    .line 43
    .line 44
    check-cast v3, Landroid/accounts/AccountManager;

    .line 45
    .line 46
    aget-object v4, v0, v2

    .line 47
    .line 48
    const/4 v8, 0x0

    .line 49
    const/4 v9, 0x0

    .line 50
    const-string v5, "adal.authtoken.type"

    .line 51
    .line 52
    const/4 v7, 0x0

    .line 53
    invoke-virtual/range {v3 .. v9}, Landroid/accounts/AccountManager;->updateCredentials(Landroid/accounts/Account;Ljava/lang/String;Landroid/os/Bundle;Landroid/app/Activity;Landroid/accounts/AccountManagerCallback;Landroid/os/Handler;)Landroid/accounts/AccountManagerFuture;

    .line 54
    .line 55
    .line 56
    move-result-object v3

    .line 57
    const-string v4, "BrokerProxy"

    .line 58
    .line 59
    const-string v5, "Waiting for the result"

    .line 60
    .line 61
    invoke-static {v4, v5}, Lh23;->f(Ljava/lang/String;Ljava/lang/String;)V

    .line 62
    .line 63
    .line 64
    invoke-interface {v3}, Landroid/accounts/AccountManagerFuture;->getResult()Ljava/lang/Object;

    .line 65
    .line 66
    .line 67
    move-result-object v3

    .line 68
    check-cast v3, Landroid/os/Bundle;

    .line 69
    .line 70
    new-instance v7, Ldq5;

    .line 71
    .line 72
    const-string v4, "account.userinfo.userid"

    .line 73
    .line 74
    invoke-virtual {v3, v4}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;

    .line 75
    .line 76
    .line 77
    move-result-object v8

    .line 78
    const-string v4, "account.userinfo.given.name"

    .line 79
    .line 80
    invoke-virtual {v3, v4}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;

    .line 81
    .line 82
    .line 83
    move-result-object v9

    .line 84
    const-string v4, "account.userinfo.family.name"

    .line 85
    .line 86
    invoke-virtual {v3, v4}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;

    .line 87
    .line 88
    .line 89
    move-result-object v10

    .line 90
    const-string v4, "account.userinfo.identity.provider"

    .line 91
    .line 92
    invoke-virtual {v3, v4}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;

    .line 93
    .line 94
    .line 95
    move-result-object v11

    .line 96
    const-string v4, "account.userinfo.userid.displayable"

    .line 97
    .line 98
    invoke-virtual {v3, v4}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;

    .line 99
    .line 100
    .line 101
    move-result-object v12

    .line 102
    invoke-direct/range {v7 .. v12}, Ldq5;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V

    .line 103
    .line 104
    .line 105
    aput-object v7, v1, v2

    .line 106
    .line 107
    add-int/lit8 v2, v2, 0x1

    .line 108
    .line 109
    goto :goto_0

    .line 110
    :cond_0
    return-object v1

    .line 111
    :cond_1
    return-object v2

    .line 112
    :cond_2
    const-string p0, "Calling getBrokerUsers on main thread"

    .line 113
    .line 114
    invoke-static {p0}, Lla;->m(Ljava/lang/String;)V

    .line 115
    .line 116
    .line 117
    return-object v2
.end method

.method public y(Landroid/database/sqlite/SQLiteDatabase;)V
    .locals 2

    .line 1
    iget-object v0, p0, Lt41;->c:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Ljava/lang/String;

    .line 4
    .line 5
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 6
    .line 7
    .line 8
    const/4 v1, 0x1

    .line 9
    invoke-static {p1, v1, v0}, Los5;->b(Landroid/database/sqlite/SQLiteDatabase;ILjava/lang/String;)V

    .line 10
    .line 11
    .line 12
    iget-object v0, p0, Lt41;->d:Ljava/lang/Object;

    .line 13
    .line 14
    check-cast v0, Ljava/lang/String;

    .line 15
    .line 16
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 17
    .line 18
    .line 19
    const-string v1, "DROP TABLE IF EXISTS "

    .line 20
    .line 21
    invoke-virtual {v1, v0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    .line 22
    .line 23
    .line 24
    move-result-object v0

    .line 25
    invoke-virtual {p1, v0}, Landroid/database/sqlite/SQLiteDatabase;->execSQL(Ljava/lang/String;)V

    .line 26
    .line 27
    .line 28
    new-instance v0, Ljava/lang/StringBuilder;

    .line 29
    .line 30
    const-string v1, "CREATE TABLE "

    .line 31
    .line 32
    invoke-direct {v0, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 33
    .line 34
    .line 35
    iget-object p0, p0, Lt41;->d:Ljava/lang/Object;

    .line 36
    .line 37
    check-cast p0, Ljava/lang/String;

    .line 38
    .line 39
    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 40
    .line 41
    .line 42
    const-string p0, " (id INTEGER PRIMARY KEY NOT NULL,key TEXT NOT NULL,metadata BLOB NOT NULL)"

    .line 43
    .line 44
    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 45
    .line 46
    .line 47
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 48
    .line 49
    .line 50
    move-result-object p0

    .line 51
    invoke-virtual {p1, p0}, Landroid/database/sqlite/SQLiteDatabase;->execSQL(Ljava/lang/String;)V

    .line 52
    .line 53
    .line 54
    return-void
.end method

.method public z(Landroid/os/Message;)V
    .locals 3

    .line 1
    iget-object p0, p0, Lt41;->c:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Ljava/util/concurrent/LinkedBlockingDeque;

    .line 4
    .line 5
    invoke-virtual {p0, p1}, Ljava/util/concurrent/LinkedBlockingDeque;->offer(Ljava/lang/Object;)Z

    .line 6
    .line 7
    .line 8
    move-result v0

    .line 9
    const-string v1, "SessionLifecycleClient"

    .line 10
    .line 11
    if-eqz v0, :cond_0

    .line 12
    .line 13
    new-instance v0, Ljava/lang/StringBuilder;

    .line 14
    .line 15
    const-string v2, "Queued message "

    .line 16
    .line 17
    invoke-direct {v0, v2}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 18
    .line 19
    .line 20
    iget p1, p1, Landroid/os/Message;->what:I

    .line 21
    .line 22
    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    .line 23
    .line 24
    .line 25
    const-string p1, ". Queue size "

    .line 26
    .line 27
    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 28
    .line 29
    .line 30
    invoke-virtual {p0}, Ljava/util/concurrent/LinkedBlockingDeque;->size()I

    .line 31
    .line 32
    .line 33
    move-result p0

    .line 34
    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    .line 35
    .line 36
    .line 37
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 38
    .line 39
    .line 40
    move-result-object p0

    .line 41
    invoke-static {v1, p0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 42
    .line 43
    .line 44
    return-void

    .line 45
    :cond_0
    new-instance p0, Ljava/lang/StringBuilder;

    .line 46
    .line 47
    const-string v0, "Failed to enqueue message "

    .line 48
    .line 49
    invoke-direct {p0, v0}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 50
    .line 51
    .line 52
    iget p1, p1, Landroid/os/Message;->what:I

    .line 53
    .line 54
    invoke-virtual {p0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    .line 55
    .line 56
    .line 57
    const-string p1, ". Dropping."

    .line 58
    .line 59
    invoke-virtual {p0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 60
    .line 61
    .line 62
    invoke-virtual {p0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 63
    .line 64
    .line 65
    move-result-object p0

    .line 66
    invoke-static {v1, p0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 67
    .line 68
    .line 69
    return-void
.end method
