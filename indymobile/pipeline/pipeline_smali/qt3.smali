.class public final Lqt3;
.super Ljava/lang/Object;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"


# instance fields
.field public final synthetic a:I

.field public final b:Lcom/indymobile/app/model/PSPage;

.field public final c:Landroid/graphics/Bitmap;

.field public final d:Lhu3;


# direct methods
.method public synthetic constructor <init>(Lcom/indymobile/app/model/PSPage;Landroid/graphics/Bitmap;Lhu3;I)V
    .locals 0

    .line 1
    iput p4, p0, Lqt3;->a:I

    .line 2
    .line 3
    iput-object p1, p0, Lqt3;->b:Lcom/indymobile/app/model/PSPage;

    .line 4
    .line 5
    iput-object p2, p0, Lqt3;->c:Landroid/graphics/Bitmap;

    .line 6
    .line 7
    iput-object p3, p0, Lqt3;->d:Lhu3;

    .line 8
    .line 9
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 10
    .line 11
    .line 12
    return-void
.end method

.method public static b(Landroid/graphics/Bitmap;Lcom/indymobile/app/model/PSPage;)V
    .locals 13

    .line 1
    invoke-static {}, Ljava/lang/Thread;->currentThread()Ljava/lang/Thread;

    .line 2
    .line 3
    .line 4
    move-result-object v0

    .line 5
    invoke-virtual {v0}, Ljava/lang/Thread;->getName()Ljava/lang/String;

    .line 6
    .line 7
    .line 8
    const/4 v0, 0x1

    .line 9
    const/4 v1, 0x0

    .line 10
    if-nez p0, :cond_0

    .line 11
    .line 12
    invoke-virtual {p1}, Lcom/indymobile/app/model/PSPage;->c()Ljava/io/File;

    .line 13
    .line 14
    .line 15
    move-result-object p0

    .line 16
    invoke-virtual {p0}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    .line 17
    .line 18
    .line 19
    move-result-object p0

    .line 20
    const v2, 0x186a0

    .line 21
    .line 22
    .line 23
    invoke-static {v2, p0}, Ls46;->l(ILjava/lang/String;)Landroid/graphics/Bitmap;

    .line 24
    .line 25
    .line 26
    move-result-object p0

    .line 27
    move v2, v0

    .line 28
    goto :goto_0

    .line 29
    :cond_0
    move v2, v1

    .line 30
    :goto_0
    const/16 v3, 0x280

    .line 31
    .line 32
    invoke-static {p0, v3, v3}, Ls46;->L(Landroid/graphics/Bitmap;II)Landroid/graphics/Bitmap;

    .line 33
    .line 34
    .line 35
    move-result-object v3

    .line 36
    invoke-virtual {p0}, Landroid/graphics/Bitmap;->getWidth()I

    .line 37
    .line 38
    .line 39
    move-result v7

    .line 40
    invoke-virtual {p0}, Landroid/graphics/Bitmap;->getHeight()I

    .line 41
    .line 42
    .line 43
    move-result v8

    .line 44
    invoke-virtual {v3}, Landroid/graphics/Bitmap;->getWidth()I

    .line 45
    .line 46
    .line 47
    move-result v4

    .line 48
    invoke-virtual {v3}, Landroid/graphics/Bitmap;->getHeight()I

    .line 49
    .line 50
    .line 51
    move-result v5

    .line 52
    if-eqz v2, :cond_1

    .line 53
    .line 54
    invoke-static {p0}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 55
    .line 56
    .line 57
    :cond_1
    if-le v7, v8, :cond_2

    .line 58
    .line 59
    int-to-float p0, v4

    .line 60
    int-to-float v2, v7

    .line 61
    :goto_1
    div-float/2addr p0, v2

    .line 62
    move v9, p0

    .line 63
    goto :goto_2

    .line 64
    :cond_2
    int-to-float p0, v5

    .line 65
    int-to-float v2, v8

    .line 66
    goto :goto_1

    .line 67
    :goto_2
    new-instance p0, Lorg/opencv/core/Mat;

    .line 68
    .line 69
    invoke-direct {p0}, Lorg/opencv/core/Mat;-><init>()V

    .line 70
    .line 71
    .line 72
    invoke-static {v3, p0}, Ls46;->e(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 73
    .line 74
    .line 75
    sget-object v4, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 76
    .line 77
    iget-wide v5, p0, Lorg/opencv/core/Mat;->a:J

    .line 78
    .line 79
    invoke-static/range {v4 .. v9}, Lcom/indymobile/app/PSOpenCV;->detectPageBorder(Landroid/content/Context;JIIF)Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;

    .line 80
    .line 81
    .line 82
    move-result-object p0

    .line 83
    new-instance v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 84
    .line 85
    invoke-direct {v2}, Lcom/indymobile/app/model/PSDocumentProcessInfo;-><init>()V

    .line 86
    .line 87
    .line 88
    iput-object v2, p1, Lcom/indymobile/app/model/PSPage;->a:Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 89
    .line 90
    iput-boolean v1, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->fixToFitImage:Z

    .line 91
    .line 92
    int-to-float v3, v7

    .line 93
    iput v3, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->width:F

    .line 94
    .line 95
    int-to-float v3, v8

    .line 96
    iput v3, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->height:F

    .line 97
    .line 98
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 99
    .line 100
    .line 101
    move-result-object v3

    .line 102
    iget-object v3, v3, Ldy3;->n:Lyu3;

    .line 103
    .line 104
    iput-object v3, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->colorType:Lyu3;

    .line 105
    .line 106
    new-instance v3, Lcom/indymobile/app/model/PSRect;

    .line 107
    .line 108
    invoke-direct {v3}, Lcom/indymobile/app/model/PSRect;-><init>()V

    .line 109
    .line 110
    .line 111
    new-instance v4, Lcom/indymobile/app/model/PSPoint;

    .line 112
    .line 113
    iget-object v5, p0, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jAutoDetectRects:[F

    .line 114
    .line 115
    aget v6, v5, v1

    .line 116
    .line 117
    aget v7, v5, v0

    .line 118
    .line 119
    invoke-direct {v4, v6, v7}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 120
    .line 121
    .line 122
    iput-object v4, v3, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 123
    .line 124
    new-instance v4, Lcom/indymobile/app/model/PSPoint;

    .line 125
    .line 126
    const/4 v6, 0x2

    .line 127
    aget v7, v5, v6

    .line 128
    .line 129
    const/4 v8, 0x3

    .line 130
    aget v9, v5, v8

    .line 131
    .line 132
    invoke-direct {v4, v7, v9}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 133
    .line 134
    .line 135
    iput-object v4, v3, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 136
    .line 137
    new-instance v4, Lcom/indymobile/app/model/PSPoint;

    .line 138
    .line 139
    const/4 v7, 0x4

    .line 140
    aget v9, v5, v7

    .line 141
    .line 142
    const/4 v10, 0x5

    .line 143
    aget v11, v5, v10

    .line 144
    .line 145
    invoke-direct {v4, v9, v11}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 146
    .line 147
    .line 148
    iput-object v4, v3, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 149
    .line 150
    new-instance v4, Lcom/indymobile/app/model/PSPoint;

    .line 151
    .line 152
    const/4 v9, 0x6

    .line 153
    aget v11, v5, v9

    .line 154
    .line 155
    const/4 v12, 0x7

    .line 156
    aget v5, v5, v12

    .line 157
    .line 158
    invoke-direct {v4, v11, v5}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 159
    .line 160
    .line 161
    iput-object v4, v3, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 162
    .line 163
    iput-object v3, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->autoDetectRect:Lcom/indymobile/app/model/PSRect;

    .line 164
    .line 165
    new-instance v3, Lcom/indymobile/app/model/PSRect;

    .line 166
    .line 167
    invoke-direct {v3}, Lcom/indymobile/app/model/PSRect;-><init>()V

    .line 168
    .line 169
    .line 170
    new-instance v4, Lcom/indymobile/app/model/PSPoint;

    .line 171
    .line 172
    iget-object v5, p0, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jAutoDetectRects:[F

    .line 173
    .line 174
    aget v11, v5, v1

    .line 175
    .line 176
    aget v0, v5, v0

    .line 177
    .line 178
    invoke-direct {v4, v11, v0}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 179
    .line 180
    .line 181
    iput-object v4, v3, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 182
    .line 183
    new-instance v0, Lcom/indymobile/app/model/PSPoint;

    .line 184
    .line 185
    aget v4, v5, v6

    .line 186
    .line 187
    aget v6, v5, v8

    .line 188
    .line 189
    invoke-direct {v0, v4, v6}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 190
    .line 191
    .line 192
    iput-object v0, v3, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 193
    .line 194
    new-instance v0, Lcom/indymobile/app/model/PSPoint;

    .line 195
    .line 196
    aget v4, v5, v7

    .line 197
    .line 198
    aget v6, v5, v10

    .line 199
    .line 200
    invoke-direct {v0, v4, v6}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 201
    .line 202
    .line 203
    iput-object v0, v3, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 204
    .line 205
    new-instance v0, Lcom/indymobile/app/model/PSPoint;

    .line 206
    .line 207
    aget v4, v5, v9

    .line 208
    .line 209
    aget v5, v5, v12

    .line 210
    .line 211
    invoke-direct {v0, v4, v5}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 212
    .line 213
    .line 214
    iput-object v0, v3, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 215
    .line 216
    iput-object v3, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->userSelectRect:Lcom/indymobile/app/model/PSRect;

    .line 217
    .line 218
    new-instance v0, Ljava/util/ArrayList;

    .line 219
    .line 220
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 221
    .line 222
    .line 223
    iput-object v0, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->topLines:Ljava/util/List;

    .line 224
    .line 225
    move v0, v1

    .line 226
    :goto_3
    iget-object v3, p0, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jTopPSLines:[F

    .line 227
    .line 228
    array-length v4, v3

    .line 229
    if-ge v0, v4, :cond_3

    .line 230
    .line 231
    iget-object v4, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->topLines:Ljava/util/List;

    .line 232
    .line 233
    new-instance v5, Lcom/indymobile/app/model/PSLine;

    .line 234
    .line 235
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 236
    .line 237
    aget v7, v3, v0

    .line 238
    .line 239
    add-int/lit8 v8, v0, 0x1

    .line 240
    .line 241
    aget v8, v3, v8

    .line 242
    .line 243
    invoke-direct {v6, v7, v8}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 244
    .line 245
    .line 246
    new-instance v7, Lcom/indymobile/app/model/PSPoint;

    .line 247
    .line 248
    add-int/lit8 v8, v0, 0x2

    .line 249
    .line 250
    aget v8, v3, v8

    .line 251
    .line 252
    add-int/lit8 v9, v0, 0x3

    .line 253
    .line 254
    aget v3, v3, v9

    .line 255
    .line 256
    invoke-direct {v7, v8, v3}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 257
    .line 258
    .line 259
    invoke-direct {v5, v6, v7}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 260
    .line 261
    .line 262
    invoke-interface {v4, v5}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 263
    .line 264
    .line 265
    add-int/lit8 v0, v0, 0x4

    .line 266
    .line 267
    goto :goto_3

    .line 268
    :cond_3
    new-instance v0, Ljava/util/ArrayList;

    .line 269
    .line 270
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 271
    .line 272
    .line 273
    iput-object v0, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->leftLines:Ljava/util/List;

    .line 274
    .line 275
    move v0, v1

    .line 276
    :goto_4
    iget-object v3, p0, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jLeftPSLines:[F

    .line 277
    .line 278
    array-length v4, v3

    .line 279
    if-ge v0, v4, :cond_4

    .line 280
    .line 281
    iget-object v4, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->leftLines:Ljava/util/List;

    .line 282
    .line 283
    new-instance v5, Lcom/indymobile/app/model/PSLine;

    .line 284
    .line 285
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 286
    .line 287
    aget v7, v3, v0

    .line 288
    .line 289
    add-int/lit8 v8, v0, 0x1

    .line 290
    .line 291
    aget v8, v3, v8

    .line 292
    .line 293
    invoke-direct {v6, v7, v8}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 294
    .line 295
    .line 296
    new-instance v7, Lcom/indymobile/app/model/PSPoint;

    .line 297
    .line 298
    add-int/lit8 v8, v0, 0x2

    .line 299
    .line 300
    aget v8, v3, v8

    .line 301
    .line 302
    add-int/lit8 v9, v0, 0x3

    .line 303
    .line 304
    aget v3, v3, v9

    .line 305
    .line 306
    invoke-direct {v7, v8, v3}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 307
    .line 308
    .line 309
    invoke-direct {v5, v6, v7}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 310
    .line 311
    .line 312
    invoke-interface {v4, v5}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 313
    .line 314
    .line 315
    add-int/lit8 v0, v0, 0x4

    .line 316
    .line 317
    goto :goto_4

    .line 318
    :cond_4
    new-instance v0, Ljava/util/ArrayList;

    .line 319
    .line 320
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 321
    .line 322
    .line 323
    iput-object v0, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->bottomLines:Ljava/util/List;

    .line 324
    .line 325
    move v0, v1

    .line 326
    :goto_5
    iget-object v3, p0, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jBottomPSLines:[F

    .line 327
    .line 328
    array-length v4, v3

    .line 329
    if-ge v0, v4, :cond_5

    .line 330
    .line 331
    iget-object v4, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->bottomLines:Ljava/util/List;

    .line 332
    .line 333
    new-instance v5, Lcom/indymobile/app/model/PSLine;

    .line 334
    .line 335
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 336
    .line 337
    aget v7, v3, v0

    .line 338
    .line 339
    add-int/lit8 v8, v0, 0x1

    .line 340
    .line 341
    aget v8, v3, v8

    .line 342
    .line 343
    invoke-direct {v6, v7, v8}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 344
    .line 345
    .line 346
    new-instance v7, Lcom/indymobile/app/model/PSPoint;

    .line 347
    .line 348
    add-int/lit8 v8, v0, 0x2

    .line 349
    .line 350
    aget v8, v3, v8

    .line 351
    .line 352
    add-int/lit8 v9, v0, 0x3

    .line 353
    .line 354
    aget v3, v3, v9

    .line 355
    .line 356
    invoke-direct {v7, v8, v3}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 357
    .line 358
    .line 359
    invoke-direct {v5, v6, v7}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 360
    .line 361
    .line 362
    invoke-interface {v4, v5}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 363
    .line 364
    .line 365
    add-int/lit8 v0, v0, 0x4

    .line 366
    .line 367
    goto :goto_5

    .line 368
    :cond_5
    new-instance v0, Ljava/util/ArrayList;

    .line 369
    .line 370
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 371
    .line 372
    .line 373
    iput-object v0, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->rightLines:Ljava/util/List;

    .line 374
    .line 375
    :goto_6
    iget-object v0, p0, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jRightPSLines:[F

    .line 376
    .line 377
    array-length v3, v0

    .line 378
    if-ge v1, v3, :cond_6

    .line 379
    .line 380
    iget-object v3, v2, Lcom/indymobile/app/model/PSDocumentProcessInfo;->rightLines:Ljava/util/List;

    .line 381
    .line 382
    new-instance v4, Lcom/indymobile/app/model/PSLine;

    .line 383
    .line 384
    new-instance v5, Lcom/indymobile/app/model/PSPoint;

    .line 385
    .line 386
    aget v6, v0, v1

    .line 387
    .line 388
    add-int/lit8 v7, v1, 0x1

    .line 389
    .line 390
    aget v7, v0, v7

    .line 391
    .line 392
    invoke-direct {v5, v6, v7}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 393
    .line 394
    .line 395
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 396
    .line 397
    add-int/lit8 v7, v1, 0x2

    .line 398
    .line 399
    aget v7, v0, v7

    .line 400
    .line 401
    add-int/lit8 v8, v1, 0x3

    .line 402
    .line 403
    aget v0, v0, v8

    .line 404
    .line 405
    invoke-direct {v6, v7, v0}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 406
    .line 407
    .line 408
    invoke-direct {v4, v5, v6}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 409
    .line 410
    .line 411
    invoke-interface {v3, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 412
    .line 413
    .line 414
    add-int/lit8 v1, v1, 0x4

    .line 415
    .line 416
    goto :goto_6

    .line 417
    :cond_6
    invoke-virtual {p1}, Lcom/indymobile/app/model/PSPage;->t()V

    .line 418
    .line 419
    .line 420
    return-void
.end method

.method public static c(Landroid/graphics/Bitmap;Lcom/indymobile/app/model/PSPage;)V
    .locals 26

    .line 1
    const/4 v0, 0x1

    .line 2
    const/4 v1, 0x0

    .line 3
    if-nez p0, :cond_0

    .line 4
    .line 5
    invoke-virtual/range {p1 .. p1}, Lcom/indymobile/app/model/PSPage;->c()Ljava/io/File;

    .line 6
    .line 7
    .line 8
    move-result-object v2

    .line 9
    invoke-virtual {v2}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    .line 10
    .line 11
    .line 12
    move-result-object v2

    .line 13
    const v3, 0x186a0

    .line 14
    .line 15
    .line 16
    invoke-static {v3, v2}, Ls46;->l(ILjava/lang/String;)Landroid/graphics/Bitmap;

    .line 17
    .line 18
    .line 19
    move-result-object v2

    .line 20
    move v3, v0

    .line 21
    goto :goto_0

    .line 22
    :cond_0
    move-object/from16 v2, p0

    .line 23
    .line 24
    move v3, v1

    .line 25
    :goto_0
    invoke-virtual {v2}, Landroid/graphics/Bitmap;->getWidth()I

    .line 26
    .line 27
    .line 28
    move-result v7

    .line 29
    invoke-virtual {v2}, Landroid/graphics/Bitmap;->getHeight()I

    .line 30
    .line 31
    .line 32
    move-result v8

    .line 33
    const/16 v4, 0x280

    .line 34
    .line 35
    invoke-static {v2, v4, v4}, Ls46;->L(Landroid/graphics/Bitmap;II)Landroid/graphics/Bitmap;

    .line 36
    .line 37
    .line 38
    move-result-object v4

    .line 39
    invoke-virtual {v4}, Landroid/graphics/Bitmap;->getConfig()Landroid/graphics/Bitmap$Config;

    .line 40
    .line 41
    .line 42
    move-result-object v5

    .line 43
    sget-object v6, Landroid/graphics/Bitmap$Config;->ARGB_8888:Landroid/graphics/Bitmap$Config;

    .line 44
    .line 45
    if-eq v5, v6, :cond_1

    .line 46
    .line 47
    invoke-virtual {v4, v6, v0}, Landroid/graphics/Bitmap;->copy(Landroid/graphics/Bitmap$Config;Z)Landroid/graphics/Bitmap;

    .line 48
    .line 49
    .line 50
    move-result-object v5

    .line 51
    invoke-static {v4}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 52
    .line 53
    .line 54
    move-object v4, v5

    .line 55
    :cond_1
    if-eqz v3, :cond_2

    .line 56
    .line 57
    invoke-static {v2}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 58
    .line 59
    .line 60
    :cond_2
    invoke-virtual {v4}, Landroid/graphics/Bitmap;->getWidth()I

    .line 61
    .line 62
    .line 63
    move-result v2

    .line 64
    invoke-virtual {v4}, Landroid/graphics/Bitmap;->getHeight()I

    .line 65
    .line 66
    .line 67
    move-result v3

    .line 68
    if-le v7, v8, :cond_3

    .line 69
    .line 70
    int-to-float v2, v2

    .line 71
    int-to-float v3, v7

    .line 72
    :goto_1
    div-float/2addr v2, v3

    .line 73
    move v9, v2

    .line 74
    goto :goto_2

    .line 75
    :cond_3
    int-to-float v2, v3

    .line 76
    int-to-float v3, v8

    .line 77
    goto :goto_1

    .line 78
    :goto_2
    sget-object v2, Lht3;->a:Leh3;

    .line 79
    .line 80
    invoke-virtual {v2}, Leh3;->m()Lht3;

    .line 81
    .line 82
    .line 83
    move-result-object v2

    .line 84
    invoke-virtual {v2, v4}, Lht3;->a(Landroid/graphics/Bitmap;)Lit3;

    .line 85
    .line 86
    .line 87
    move-result-object v2

    .line 88
    new-instance v3, Lorg/opencv/core/Mat;

    .line 89
    .line 90
    invoke-direct {v3}, Lorg/opencv/core/Mat;-><init>()V

    .line 91
    .line 92
    .line 93
    invoke-static {v4, v3}, Ls46;->e(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 94
    .line 95
    .line 96
    sget-object v4, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 97
    .line 98
    iget-wide v5, v3, Lorg/opencv/core/Mat;->a:J

    .line 99
    .line 100
    invoke-static/range {v4 .. v9}, Lcom/indymobile/app/PSOpenCV;->detectPageBorder(Landroid/content/Context;JIIF)Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;

    .line 101
    .line 102
    .line 103
    move-result-object v3

    .line 104
    new-instance v4, Lcom/indymobile/app/model/PSRect;

    .line 105
    .line 106
    invoke-direct {v4}, Lcom/indymobile/app/model/PSRect;-><init>()V

    .line 107
    .line 108
    .line 109
    new-instance v5, Lcom/indymobile/app/model/PSRect;

    .line 110
    .line 111
    invoke-direct {v5}, Lcom/indymobile/app/model/PSRect;-><init>()V

    .line 112
    .line 113
    .line 114
    iget v6, v2, Lit3;->e:F

    .line 115
    .line 116
    const/high16 v9, 0x3f000000    # 0.5f

    .line 117
    .line 118
    cmpl-float v6, v6, v9

    .line 119
    .line 120
    if-ltz v6, :cond_6

    .line 121
    .line 122
    sget-object v6, Lwv3;->a:Ljava/util/Date;

    .line 123
    .line 124
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 125
    .line 126
    iget-object v9, v2, Lit3;->a:Lsw3;

    .line 127
    .line 128
    iget-object v9, v9, Lsw3;->a:Landroid/graphics/PointF;

    .line 129
    .line 130
    iget v10, v9, Landroid/graphics/PointF;->x:F

    .line 131
    .line 132
    int-to-float v11, v7

    .line 133
    mul-float/2addr v10, v11

    .line 134
    iget v9, v9, Landroid/graphics/PointF;->y:F

    .line 135
    .line 136
    int-to-float v12, v8

    .line 137
    mul-float/2addr v9, v12

    .line 138
    invoke-direct {v6, v10, v9}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 139
    .line 140
    .line 141
    iput-object v6, v4, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 142
    .line 143
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 144
    .line 145
    iget-object v9, v2, Lit3;->b:Lsw3;

    .line 146
    .line 147
    iget-object v9, v9, Lsw3;->a:Landroid/graphics/PointF;

    .line 148
    .line 149
    iget v10, v9, Landroid/graphics/PointF;->x:F

    .line 150
    .line 151
    mul-float/2addr v10, v11

    .line 152
    iget v9, v9, Landroid/graphics/PointF;->y:F

    .line 153
    .line 154
    mul-float/2addr v9, v12

    .line 155
    invoke-direct {v6, v10, v9}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 156
    .line 157
    .line 158
    iput-object v6, v4, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 159
    .line 160
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 161
    .line 162
    iget-object v9, v2, Lit3;->d:Lsw3;

    .line 163
    .line 164
    iget-object v9, v9, Lsw3;->a:Landroid/graphics/PointF;

    .line 165
    .line 166
    iget v10, v9, Landroid/graphics/PointF;->x:F

    .line 167
    .line 168
    mul-float/2addr v10, v11

    .line 169
    iget v9, v9, Landroid/graphics/PointF;->y:F

    .line 170
    .line 171
    mul-float/2addr v9, v12

    .line 172
    invoke-direct {v6, v10, v9}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 173
    .line 174
    .line 175
    iput-object v6, v4, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 176
    .line 177
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 178
    .line 179
    iget-object v2, v2, Lit3;->c:Lsw3;

    .line 180
    .line 181
    iget-object v2, v2, Lsw3;->a:Landroid/graphics/PointF;

    .line 182
    .line 183
    iget v9, v2, Landroid/graphics/PointF;->x:F

    .line 184
    .line 185
    mul-float/2addr v9, v11

    .line 186
    iget v2, v2, Landroid/graphics/PointF;->y:F

    .line 187
    .line 188
    mul-float/2addr v2, v12

    .line 189
    invoke-direct {v6, v9, v2}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 190
    .line 191
    .line 192
    iput-object v6, v4, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 193
    .line 194
    iget-boolean v2, v3, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jFoundContour:Z

    .line 195
    .line 196
    if-eqz v2, :cond_5

    .line 197
    .line 198
    new-instance v2, Lcom/indymobile/app/model/PSPoint;

    .line 199
    .line 200
    iget-object v6, v3, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jAutoDetectRects:[F

    .line 201
    .line 202
    aget v9, v6, v1

    .line 203
    .line 204
    aget v10, v6, v0

    .line 205
    .line 206
    invoke-direct {v2, v9, v10}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 207
    .line 208
    .line 209
    iput-object v2, v5, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 210
    .line 211
    new-instance v9, Lcom/indymobile/app/model/PSPoint;

    .line 212
    .line 213
    const/4 v10, 0x2

    .line 214
    aget v11, v6, v10

    .line 215
    .line 216
    const/4 v12, 0x3

    .line 217
    aget v13, v6, v12

    .line 218
    .line 219
    invoke-direct {v9, v11, v13}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 220
    .line 221
    .line 222
    iput-object v9, v5, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 223
    .line 224
    new-instance v11, Lcom/indymobile/app/model/PSPoint;

    .line 225
    .line 226
    const/4 v13, 0x4

    .line 227
    aget v14, v6, v13

    .line 228
    .line 229
    const/4 v15, 0x5

    .line 230
    move/from16 v16, v0

    .line 231
    .line 232
    aget v0, v6, v15

    .line 233
    .line 234
    invoke-direct {v11, v14, v0}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 235
    .line 236
    .line 237
    iput-object v11, v5, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 238
    .line 239
    new-instance v0, Lcom/indymobile/app/model/PSPoint;

    .line 240
    .line 241
    const/4 v14, 0x6

    .line 242
    move/from16 p0, v10

    .line 243
    .line 244
    aget v10, v6, v14

    .line 245
    .line 246
    const/16 v17, 0x7

    .line 247
    .line 248
    aget v6, v6, v17

    .line 249
    .line 250
    invoke-direct {v0, v10, v6}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 251
    .line 252
    .line 253
    iput-object v0, v5, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 254
    .line 255
    iget-object v6, v4, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 256
    .line 257
    iget v10, v6, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 258
    .line 259
    iget v6, v6, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 260
    .line 261
    move/from16 v18, v12

    .line 262
    .line 263
    iget-object v12, v4, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 264
    .line 265
    move/from16 v19, v13

    .line 266
    .line 267
    iget v13, v12, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 268
    .line 269
    iget v12, v12, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 270
    .line 271
    move/from16 v20, v14

    .line 272
    .line 273
    iget-object v14, v4, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 274
    .line 275
    move/from16 v21, v15

    .line 276
    .line 277
    iget v15, v14, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 278
    .line 279
    iget v14, v14, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 280
    .line 281
    move/from16 v22, v1

    .line 282
    .line 283
    iget-object v1, v4, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 284
    .line 285
    move-object/from16 v23, v4

    .line 286
    .line 287
    iget v4, v1, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 288
    .line 289
    iget v1, v1, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 290
    .line 291
    move/from16 v24, v1

    .line 292
    .line 293
    const/16 v1, 0x8

    .line 294
    .line 295
    move/from16 v25, v4

    .line 296
    .line 297
    new-array v4, v1, [F

    .line 298
    .line 299
    aput v10, v4, v22

    .line 300
    .line 301
    aput v6, v4, v16

    .line 302
    .line 303
    aput v13, v4, p0

    .line 304
    .line 305
    aput v12, v4, v18

    .line 306
    .line 307
    aput v15, v4, v19

    .line 308
    .line 309
    aput v14, v4, v21

    .line 310
    .line 311
    aput v25, v4, v20

    .line 312
    .line 313
    aput v24, v4, v17

    .line 314
    .line 315
    iget v6, v2, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 316
    .line 317
    iget v2, v2, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 318
    .line 319
    iget v10, v9, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 320
    .line 321
    iget v9, v9, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 322
    .line 323
    iget v12, v0, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 324
    .line 325
    iget v0, v0, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 326
    .line 327
    iget v13, v11, Lcom/indymobile/app/model/PSPoint;->x:F

    .line 328
    .line 329
    iget v11, v11, Lcom/indymobile/app/model/PSPoint;->y:F

    .line 330
    .line 331
    new-array v1, v1, [F

    .line 332
    .line 333
    aput v6, v1, v22

    .line 334
    .line 335
    aput v2, v1, v16

    .line 336
    .line 337
    aput v10, v1, p0

    .line 338
    .line 339
    aput v9, v1, v18

    .line 340
    .line 341
    aput v12, v1, v19

    .line 342
    .line 343
    aput v0, v1, v21

    .line 344
    .line 345
    aput v13, v1, v20

    .line 346
    .line 347
    aput v11, v1, v17

    .line 348
    .line 349
    sget-object v0, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 350
    .line 351
    invoke-static {v0, v4, v1}, Lcom/indymobile/app/PSOpenCV;->calculateIOU(Landroid/content/Context;[F[F)F

    .line 352
    .line 353
    .line 354
    move-result v0

    .line 355
    const v1, 0x3f75c28f    # 0.96f

    .line 356
    .line 357
    .line 358
    cmpl-float v0, v0, v1

    .line 359
    .line 360
    if-ltz v0, :cond_4

    .line 361
    .line 362
    move-object v4, v5

    .line 363
    goto :goto_3

    .line 364
    :cond_4
    move-object/from16 v4, v23

    .line 365
    .line 366
    goto :goto_3

    .line 367
    :cond_5
    move/from16 v22, v1

    .line 368
    .line 369
    move-object/from16 v23, v4

    .line 370
    .line 371
    goto :goto_3

    .line 372
    :cond_6
    move/from16 v22, v1

    .line 373
    .line 374
    sget-object v0, Lwv3;->a:Ljava/util/Date;

    .line 375
    .line 376
    new-instance v4, Lcom/indymobile/app/model/PSRect;

    .line 377
    .line 378
    invoke-direct {v4}, Lcom/indymobile/app/model/PSRect;-><init>()V

    .line 379
    .line 380
    .line 381
    new-instance v0, Lcom/indymobile/app/model/PSPoint;

    .line 382
    .line 383
    const/4 v1, 0x0

    .line 384
    invoke-direct {v0, v1, v1}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 385
    .line 386
    .line 387
    iput-object v0, v4, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 388
    .line 389
    new-instance v0, Lcom/indymobile/app/model/PSPoint;

    .line 390
    .line 391
    int-to-float v2, v7

    .line 392
    invoke-direct {v0, v2, v1}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 393
    .line 394
    .line 395
    iput-object v0, v4, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 396
    .line 397
    new-instance v0, Lcom/indymobile/app/model/PSPoint;

    .line 398
    .line 399
    int-to-float v5, v8

    .line 400
    invoke-direct {v0, v1, v5}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 401
    .line 402
    .line 403
    iput-object v0, v4, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 404
    .line 405
    new-instance v0, Lcom/indymobile/app/model/PSPoint;

    .line 406
    .line 407
    invoke-direct {v0, v2, v5}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 408
    .line 409
    .line 410
    iput-object v0, v4, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 411
    .line 412
    :goto_3
    new-instance v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 413
    .line 414
    invoke-direct {v0}, Lcom/indymobile/app/model/PSDocumentProcessInfo;-><init>()V

    .line 415
    .line 416
    .line 417
    move-object/from16 v1, p1

    .line 418
    .line 419
    iput-object v0, v1, Lcom/indymobile/app/model/PSPage;->a:Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 420
    .line 421
    move/from16 v2, v22

    .line 422
    .line 423
    iput-boolean v2, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->fixToFitImage:Z

    .line 424
    .line 425
    int-to-float v5, v7

    .line 426
    iput v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->width:F

    .line 427
    .line 428
    int-to-float v5, v8

    .line 429
    iput v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->height:F

    .line 430
    .line 431
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 432
    .line 433
    .line 434
    move-result-object v5

    .line 435
    iget-object v5, v5, Ldy3;->n:Lyu3;

    .line 436
    .line 437
    iput-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->colorType:Lyu3;

    .line 438
    .line 439
    iput-object v4, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->autoDetectRect:Lcom/indymobile/app/model/PSRect;

    .line 440
    .line 441
    invoke-virtual {v4}, Lcom/indymobile/app/model/PSRect;->a()Lcom/indymobile/app/model/PSRect;

    .line 442
    .line 443
    .line 444
    move-result-object v5

    .line 445
    iput-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->userSelectRect:Lcom/indymobile/app/model/PSRect;

    .line 446
    .line 447
    new-instance v5, Ljava/util/ArrayList;

    .line 448
    .line 449
    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V

    .line 450
    .line 451
    .line 452
    iput-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->topLines:Ljava/util/List;

    .line 453
    .line 454
    new-instance v5, Ljava/util/ArrayList;

    .line 455
    .line 456
    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V

    .line 457
    .line 458
    .line 459
    iput-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->leftLines:Ljava/util/List;

    .line 460
    .line 461
    new-instance v5, Ljava/util/ArrayList;

    .line 462
    .line 463
    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V

    .line 464
    .line 465
    .line 466
    iput-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->bottomLines:Ljava/util/List;

    .line 467
    .line 468
    new-instance v5, Ljava/util/ArrayList;

    .line 469
    .line 470
    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V

    .line 471
    .line 472
    .line 473
    iput-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->rightLines:Ljava/util/List;

    .line 474
    .line 475
    iget-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->leftLines:Ljava/util/List;

    .line 476
    .line 477
    new-instance v6, Lcom/indymobile/app/model/PSLine;

    .line 478
    .line 479
    iget-object v7, v4, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 480
    .line 481
    iget-object v8, v4, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 482
    .line 483
    invoke-direct {v6, v7, v8}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 484
    .line 485
    .line 486
    invoke-interface {v5, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 487
    .line 488
    .line 489
    iget-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->topLines:Ljava/util/List;

    .line 490
    .line 491
    new-instance v6, Lcom/indymobile/app/model/PSLine;

    .line 492
    .line 493
    iget-object v7, v4, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 494
    .line 495
    iget-object v8, v4, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 496
    .line 497
    invoke-direct {v6, v7, v8}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 498
    .line 499
    .line 500
    invoke-interface {v5, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 501
    .line 502
    .line 503
    iget-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->rightLines:Ljava/util/List;

    .line 504
    .line 505
    new-instance v6, Lcom/indymobile/app/model/PSLine;

    .line 506
    .line 507
    iget-object v7, v4, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 508
    .line 509
    iget-object v8, v4, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 510
    .line 511
    invoke-direct {v6, v7, v8}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 512
    .line 513
    .line 514
    invoke-interface {v5, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 515
    .line 516
    .line 517
    iget-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->bottomLines:Ljava/util/List;

    .line 518
    .line 519
    new-instance v6, Lcom/indymobile/app/model/PSLine;

    .line 520
    .line 521
    iget-object v7, v4, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 522
    .line 523
    iget-object v4, v4, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 524
    .line 525
    invoke-direct {v6, v7, v4}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 526
    .line 527
    .line 528
    invoke-interface {v5, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 529
    .line 530
    .line 531
    move v4, v2

    .line 532
    :goto_4
    iget-object v5, v3, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jTopPSLines:[F

    .line 533
    .line 534
    array-length v6, v5

    .line 535
    if-ge v4, v6, :cond_7

    .line 536
    .line 537
    iget-object v6, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->topLines:Ljava/util/List;

    .line 538
    .line 539
    new-instance v7, Lcom/indymobile/app/model/PSLine;

    .line 540
    .line 541
    new-instance v8, Lcom/indymobile/app/model/PSPoint;

    .line 542
    .line 543
    aget v9, v5, v4

    .line 544
    .line 545
    add-int/lit8 v10, v4, 0x1

    .line 546
    .line 547
    aget v10, v5, v10

    .line 548
    .line 549
    invoke-direct {v8, v9, v10}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 550
    .line 551
    .line 552
    new-instance v9, Lcom/indymobile/app/model/PSPoint;

    .line 553
    .line 554
    add-int/lit8 v10, v4, 0x2

    .line 555
    .line 556
    aget v10, v5, v10

    .line 557
    .line 558
    add-int/lit8 v11, v4, 0x3

    .line 559
    .line 560
    aget v5, v5, v11

    .line 561
    .line 562
    invoke-direct {v9, v10, v5}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 563
    .line 564
    .line 565
    invoke-direct {v7, v8, v9}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 566
    .line 567
    .line 568
    invoke-interface {v6, v7}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 569
    .line 570
    .line 571
    add-int/lit8 v4, v4, 0x4

    .line 572
    .line 573
    goto :goto_4

    .line 574
    :cond_7
    move v4, v2

    .line 575
    :goto_5
    iget-object v5, v3, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jLeftPSLines:[F

    .line 576
    .line 577
    array-length v6, v5

    .line 578
    if-ge v4, v6, :cond_8

    .line 579
    .line 580
    iget-object v6, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->leftLines:Ljava/util/List;

    .line 581
    .line 582
    new-instance v7, Lcom/indymobile/app/model/PSLine;

    .line 583
    .line 584
    new-instance v8, Lcom/indymobile/app/model/PSPoint;

    .line 585
    .line 586
    aget v9, v5, v4

    .line 587
    .line 588
    add-int/lit8 v10, v4, 0x1

    .line 589
    .line 590
    aget v10, v5, v10

    .line 591
    .line 592
    invoke-direct {v8, v9, v10}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 593
    .line 594
    .line 595
    new-instance v9, Lcom/indymobile/app/model/PSPoint;

    .line 596
    .line 597
    add-int/lit8 v10, v4, 0x2

    .line 598
    .line 599
    aget v10, v5, v10

    .line 600
    .line 601
    add-int/lit8 v11, v4, 0x3

    .line 602
    .line 603
    aget v5, v5, v11

    .line 604
    .line 605
    invoke-direct {v9, v10, v5}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 606
    .line 607
    .line 608
    invoke-direct {v7, v8, v9}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 609
    .line 610
    .line 611
    invoke-interface {v6, v7}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 612
    .line 613
    .line 614
    add-int/lit8 v4, v4, 0x4

    .line 615
    .line 616
    goto :goto_5

    .line 617
    :cond_8
    move v4, v2

    .line 618
    :goto_6
    iget-object v5, v3, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jBottomPSLines:[F

    .line 619
    .line 620
    array-length v6, v5

    .line 621
    if-ge v4, v6, :cond_9

    .line 622
    .line 623
    iget-object v6, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->bottomLines:Ljava/util/List;

    .line 624
    .line 625
    new-instance v7, Lcom/indymobile/app/model/PSLine;

    .line 626
    .line 627
    new-instance v8, Lcom/indymobile/app/model/PSPoint;

    .line 628
    .line 629
    aget v9, v5, v4

    .line 630
    .line 631
    add-int/lit8 v10, v4, 0x1

    .line 632
    .line 633
    aget v10, v5, v10

    .line 634
    .line 635
    invoke-direct {v8, v9, v10}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 636
    .line 637
    .line 638
    new-instance v9, Lcom/indymobile/app/model/PSPoint;

    .line 639
    .line 640
    add-int/lit8 v10, v4, 0x2

    .line 641
    .line 642
    aget v10, v5, v10

    .line 643
    .line 644
    add-int/lit8 v11, v4, 0x3

    .line 645
    .line 646
    aget v5, v5, v11

    .line 647
    .line 648
    invoke-direct {v9, v10, v5}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 649
    .line 650
    .line 651
    invoke-direct {v7, v8, v9}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 652
    .line 653
    .line 654
    invoke-interface {v6, v7}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 655
    .line 656
    .line 657
    add-int/lit8 v4, v4, 0x4

    .line 658
    .line 659
    goto :goto_6

    .line 660
    :cond_9
    :goto_7
    iget-object v4, v3, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jRightPSLines:[F

    .line 661
    .line 662
    array-length v5, v4

    .line 663
    if-ge v2, v5, :cond_a

    .line 664
    .line 665
    iget-object v5, v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->rightLines:Ljava/util/List;

    .line 666
    .line 667
    new-instance v6, Lcom/indymobile/app/model/PSLine;

    .line 668
    .line 669
    new-instance v7, Lcom/indymobile/app/model/PSPoint;

    .line 670
    .line 671
    aget v8, v4, v2

    .line 672
    .line 673
    add-int/lit8 v9, v2, 0x1

    .line 674
    .line 675
    aget v9, v4, v9

    .line 676
    .line 677
    invoke-direct {v7, v8, v9}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 678
    .line 679
    .line 680
    new-instance v8, Lcom/indymobile/app/model/PSPoint;

    .line 681
    .line 682
    add-int/lit8 v9, v2, 0x2

    .line 683
    .line 684
    aget v9, v4, v9

    .line 685
    .line 686
    add-int/lit8 v10, v2, 0x3

    .line 687
    .line 688
    aget v4, v4, v10

    .line 689
    .line 690
    invoke-direct {v8, v9, v4}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 691
    .line 692
    .line 693
    invoke-direct {v6, v7, v8}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 694
    .line 695
    .line 696
    invoke-interface {v5, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 697
    .line 698
    .line 699
    add-int/lit8 v2, v2, 0x4

    .line 700
    .line 701
    goto :goto_7

    .line 702
    :cond_a
    invoke-virtual {v1}, Lcom/indymobile/app/model/PSPage;->t()V

    .line 703
    .line 704
    .line 705
    return-void
.end method

.method public static d(Landroid/graphics/Bitmap;Lcom/indymobile/app/model/PSPage;)V
    .locals 10

    .line 1
    const/4 v0, 0x1

    .line 2
    const/4 v1, 0x0

    .line 3
    if-nez p0, :cond_0

    .line 4
    .line 5
    invoke-virtual {p1}, Lcom/indymobile/app/model/PSPage;->c()Ljava/io/File;

    .line 6
    .line 7
    .line 8
    move-result-object p0

    .line 9
    invoke-virtual {p0}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    .line 10
    .line 11
    .line 12
    move-result-object p0

    .line 13
    const v2, 0x186a0

    .line 14
    .line 15
    .line 16
    invoke-static {v2, p0}, Ls46;->l(ILjava/lang/String;)Landroid/graphics/Bitmap;

    .line 17
    .line 18
    .line 19
    move-result-object p0

    .line 20
    move v2, v0

    .line 21
    goto :goto_0

    .line 22
    :cond_0
    move v2, v1

    .line 23
    :goto_0
    invoke-virtual {p0}, Landroid/graphics/Bitmap;->getWidth()I

    .line 24
    .line 25
    .line 26
    move-result v6

    .line 27
    invoke-virtual {p0}, Landroid/graphics/Bitmap;->getHeight()I

    .line 28
    .line 29
    .line 30
    move-result v7

    .line 31
    const/16 v3, 0x280

    .line 32
    .line 33
    invoke-static {p0, v3, v3}, Ls46;->L(Landroid/graphics/Bitmap;II)Landroid/graphics/Bitmap;

    .line 34
    .line 35
    .line 36
    move-result-object v3

    .line 37
    invoke-virtual {v3}, Landroid/graphics/Bitmap;->getConfig()Landroid/graphics/Bitmap$Config;

    .line 38
    .line 39
    .line 40
    move-result-object v4

    .line 41
    sget-object v5, Landroid/graphics/Bitmap$Config;->ARGB_8888:Landroid/graphics/Bitmap$Config;

    .line 42
    .line 43
    if-eq v4, v5, :cond_1

    .line 44
    .line 45
    invoke-virtual {v3, v5, v0}, Landroid/graphics/Bitmap;->copy(Landroid/graphics/Bitmap$Config;Z)Landroid/graphics/Bitmap;

    .line 46
    .line 47
    .line 48
    move-result-object v0

    .line 49
    invoke-static {v3}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 50
    .line 51
    .line 52
    move-object v3, v0

    .line 53
    :cond_1
    if-eqz v2, :cond_2

    .line 54
    .line 55
    invoke-static {p0}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 56
    .line 57
    .line 58
    :cond_2
    invoke-virtual {v3}, Landroid/graphics/Bitmap;->getWidth()I

    .line 59
    .line 60
    .line 61
    move-result p0

    .line 62
    invoke-virtual {v3}, Landroid/graphics/Bitmap;->getHeight()I

    .line 63
    .line 64
    .line 65
    move-result v0

    .line 66
    if-le v6, v7, :cond_3

    .line 67
    .line 68
    int-to-float p0, p0

    .line 69
    int-to-float v0, v6

    .line 70
    :goto_1
    div-float/2addr p0, v0

    .line 71
    move v8, p0

    .line 72
    goto :goto_2

    .line 73
    :cond_3
    int-to-float p0, v0

    .line 74
    int-to-float v0, v7

    .line 75
    goto :goto_1

    .line 76
    :goto_2
    sget-object p0, Lht3;->a:Leh3;

    .line 77
    .line 78
    invoke-virtual {p0}, Leh3;->m()Lht3;

    .line 79
    .line 80
    .line 81
    move-result-object p0

    .line 82
    invoke-virtual {p0, v3}, Lht3;->a(Landroid/graphics/Bitmap;)Lit3;

    .line 83
    .line 84
    .line 85
    move-result-object p0

    .line 86
    new-instance v0, Lorg/opencv/core/Mat;

    .line 87
    .line 88
    invoke-direct {v0}, Lorg/opencv/core/Mat;-><init>()V

    .line 89
    .line 90
    .line 91
    invoke-static {v3, v0}, Ls46;->e(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 92
    .line 93
    .line 94
    sget-object v3, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 95
    .line 96
    iget-wide v4, v0, Lorg/opencv/core/Mat;->a:J

    .line 97
    .line 98
    invoke-static/range {v3 .. v8}, Lcom/indymobile/app/PSOpenCV;->detectPageBorder(Landroid/content/Context;JIIF)Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;

    .line 99
    .line 100
    .line 101
    move-result-object v0

    .line 102
    new-instance v2, Lcom/indymobile/app/model/PSRect;

    .line 103
    .line 104
    invoke-direct {v2}, Lcom/indymobile/app/model/PSRect;-><init>()V

    .line 105
    .line 106
    .line 107
    iget v3, p0, Lit3;->e:F

    .line 108
    .line 109
    const/high16 v4, 0x3f000000    # 0.5f

    .line 110
    .line 111
    cmpl-float v3, v3, v4

    .line 112
    .line 113
    if-ltz v3, :cond_4

    .line 114
    .line 115
    new-instance v3, Lcom/indymobile/app/model/PSPoint;

    .line 116
    .line 117
    iget-object v4, p0, Lit3;->a:Lsw3;

    .line 118
    .line 119
    iget-object v4, v4, Lsw3;->a:Landroid/graphics/PointF;

    .line 120
    .line 121
    iget v5, v4, Landroid/graphics/PointF;->x:F

    .line 122
    .line 123
    int-to-float v8, v6

    .line 124
    mul-float/2addr v5, v8

    .line 125
    iget v4, v4, Landroid/graphics/PointF;->y:F

    .line 126
    .line 127
    int-to-float v9, v7

    .line 128
    mul-float/2addr v4, v9

    .line 129
    invoke-direct {v3, v5, v4}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 130
    .line 131
    .line 132
    iput-object v3, v2, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 133
    .line 134
    new-instance v3, Lcom/indymobile/app/model/PSPoint;

    .line 135
    .line 136
    iget-object v4, p0, Lit3;->b:Lsw3;

    .line 137
    .line 138
    iget-object v4, v4, Lsw3;->a:Landroid/graphics/PointF;

    .line 139
    .line 140
    iget v5, v4, Landroid/graphics/PointF;->x:F

    .line 141
    .line 142
    mul-float/2addr v5, v8

    .line 143
    iget v4, v4, Landroid/graphics/PointF;->y:F

    .line 144
    .line 145
    mul-float/2addr v4, v9

    .line 146
    invoke-direct {v3, v5, v4}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 147
    .line 148
    .line 149
    iput-object v3, v2, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 150
    .line 151
    new-instance v3, Lcom/indymobile/app/model/PSPoint;

    .line 152
    .line 153
    iget-object v4, p0, Lit3;->d:Lsw3;

    .line 154
    .line 155
    iget-object v4, v4, Lsw3;->a:Landroid/graphics/PointF;

    .line 156
    .line 157
    iget v5, v4, Landroid/graphics/PointF;->x:F

    .line 158
    .line 159
    mul-float/2addr v5, v8

    .line 160
    iget v4, v4, Landroid/graphics/PointF;->y:F

    .line 161
    .line 162
    mul-float/2addr v4, v9

    .line 163
    invoke-direct {v3, v5, v4}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 164
    .line 165
    .line 166
    iput-object v3, v2, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 167
    .line 168
    new-instance v3, Lcom/indymobile/app/model/PSPoint;

    .line 169
    .line 170
    iget-object p0, p0, Lit3;->c:Lsw3;

    .line 171
    .line 172
    iget-object p0, p0, Lsw3;->a:Landroid/graphics/PointF;

    .line 173
    .line 174
    iget v4, p0, Landroid/graphics/PointF;->x:F

    .line 175
    .line 176
    mul-float/2addr v4, v8

    .line 177
    iget p0, p0, Landroid/graphics/PointF;->y:F

    .line 178
    .line 179
    mul-float/2addr p0, v9

    .line 180
    invoke-direct {v3, v4, p0}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 181
    .line 182
    .line 183
    iput-object v3, v2, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 184
    .line 185
    goto :goto_3

    .line 186
    :cond_4
    new-instance p0, Lcom/indymobile/app/model/PSPoint;

    .line 187
    .line 188
    const/4 v3, 0x0

    .line 189
    invoke-direct {p0, v3, v3}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 190
    .line 191
    .line 192
    iput-object p0, v2, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 193
    .line 194
    new-instance p0, Lcom/indymobile/app/model/PSPoint;

    .line 195
    .line 196
    int-to-float v4, v6

    .line 197
    invoke-direct {p0, v4, v3}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 198
    .line 199
    .line 200
    iput-object p0, v2, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 201
    .line 202
    new-instance p0, Lcom/indymobile/app/model/PSPoint;

    .line 203
    .line 204
    int-to-float v5, v7

    .line 205
    invoke-direct {p0, v3, v5}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 206
    .line 207
    .line 208
    iput-object p0, v2, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 209
    .line 210
    new-instance p0, Lcom/indymobile/app/model/PSPoint;

    .line 211
    .line 212
    invoke-direct {p0, v4, v5}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 213
    .line 214
    .line 215
    iput-object p0, v2, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 216
    .line 217
    :goto_3
    new-instance p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 218
    .line 219
    invoke-direct {p0}, Lcom/indymobile/app/model/PSDocumentProcessInfo;-><init>()V

    .line 220
    .line 221
    .line 222
    iput-object p0, p1, Lcom/indymobile/app/model/PSPage;->a:Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 223
    .line 224
    iput-boolean v1, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->fixToFitImage:Z

    .line 225
    .line 226
    int-to-float v3, v6

    .line 227
    iput v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->width:F

    .line 228
    .line 229
    int-to-float v3, v7

    .line 230
    iput v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->height:F

    .line 231
    .line 232
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 233
    .line 234
    .line 235
    move-result-object v3

    .line 236
    iget-object v3, v3, Ldy3;->n:Lyu3;

    .line 237
    .line 238
    iput-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->colorType:Lyu3;

    .line 239
    .line 240
    iput-object v2, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->autoDetectRect:Lcom/indymobile/app/model/PSRect;

    .line 241
    .line 242
    invoke-virtual {v2}, Lcom/indymobile/app/model/PSRect;->a()Lcom/indymobile/app/model/PSRect;

    .line 243
    .line 244
    .line 245
    move-result-object v3

    .line 246
    iput-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->userSelectRect:Lcom/indymobile/app/model/PSRect;

    .line 247
    .line 248
    new-instance v3, Ljava/util/ArrayList;

    .line 249
    .line 250
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 251
    .line 252
    .line 253
    iput-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->topLines:Ljava/util/List;

    .line 254
    .line 255
    new-instance v3, Ljava/util/ArrayList;

    .line 256
    .line 257
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 258
    .line 259
    .line 260
    iput-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->leftLines:Ljava/util/List;

    .line 261
    .line 262
    new-instance v3, Ljava/util/ArrayList;

    .line 263
    .line 264
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 265
    .line 266
    .line 267
    iput-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->bottomLines:Ljava/util/List;

    .line 268
    .line 269
    new-instance v3, Ljava/util/ArrayList;

    .line 270
    .line 271
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 272
    .line 273
    .line 274
    iput-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->rightLines:Ljava/util/List;

    .line 275
    .line 276
    iget-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->leftLines:Ljava/util/List;

    .line 277
    .line 278
    new-instance v4, Lcom/indymobile/app/model/PSLine;

    .line 279
    .line 280
    iget-object v5, v2, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 281
    .line 282
    iget-object v6, v2, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 283
    .line 284
    invoke-direct {v4, v5, v6}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 285
    .line 286
    .line 287
    invoke-interface {v3, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 288
    .line 289
    .line 290
    iget-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->topLines:Ljava/util/List;

    .line 291
    .line 292
    new-instance v4, Lcom/indymobile/app/model/PSLine;

    .line 293
    .line 294
    iget-object v5, v2, Lcom/indymobile/app/model/PSRect;->topLeft:Lcom/indymobile/app/model/PSPoint;

    .line 295
    .line 296
    iget-object v6, v2, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 297
    .line 298
    invoke-direct {v4, v5, v6}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 299
    .line 300
    .line 301
    invoke-interface {v3, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 302
    .line 303
    .line 304
    iget-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->rightLines:Ljava/util/List;

    .line 305
    .line 306
    new-instance v4, Lcom/indymobile/app/model/PSLine;

    .line 307
    .line 308
    iget-object v5, v2, Lcom/indymobile/app/model/PSRect;->topRight:Lcom/indymobile/app/model/PSPoint;

    .line 309
    .line 310
    iget-object v6, v2, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 311
    .line 312
    invoke-direct {v4, v5, v6}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 313
    .line 314
    .line 315
    invoke-interface {v3, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 316
    .line 317
    .line 318
    iget-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->bottomLines:Ljava/util/List;

    .line 319
    .line 320
    new-instance v4, Lcom/indymobile/app/model/PSLine;

    .line 321
    .line 322
    iget-object v5, v2, Lcom/indymobile/app/model/PSRect;->bottomLeft:Lcom/indymobile/app/model/PSPoint;

    .line 323
    .line 324
    iget-object v2, v2, Lcom/indymobile/app/model/PSRect;->bottomRight:Lcom/indymobile/app/model/PSPoint;

    .line 325
    .line 326
    invoke-direct {v4, v5, v2}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 327
    .line 328
    .line 329
    invoke-interface {v3, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 330
    .line 331
    .line 332
    move v2, v1

    .line 333
    :goto_4
    iget-object v3, v0, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jTopPSLines:[F

    .line 334
    .line 335
    array-length v4, v3

    .line 336
    if-ge v2, v4, :cond_5

    .line 337
    .line 338
    iget-object v4, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->topLines:Ljava/util/List;

    .line 339
    .line 340
    new-instance v5, Lcom/indymobile/app/model/PSLine;

    .line 341
    .line 342
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 343
    .line 344
    aget v7, v3, v2

    .line 345
    .line 346
    add-int/lit8 v8, v2, 0x1

    .line 347
    .line 348
    aget v8, v3, v8

    .line 349
    .line 350
    invoke-direct {v6, v7, v8}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 351
    .line 352
    .line 353
    new-instance v7, Lcom/indymobile/app/model/PSPoint;

    .line 354
    .line 355
    add-int/lit8 v8, v2, 0x2

    .line 356
    .line 357
    aget v8, v3, v8

    .line 358
    .line 359
    add-int/lit8 v9, v2, 0x3

    .line 360
    .line 361
    aget v3, v3, v9

    .line 362
    .line 363
    invoke-direct {v7, v8, v3}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 364
    .line 365
    .line 366
    invoke-direct {v5, v6, v7}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 367
    .line 368
    .line 369
    invoke-interface {v4, v5}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 370
    .line 371
    .line 372
    add-int/lit8 v2, v2, 0x4

    .line 373
    .line 374
    goto :goto_4

    .line 375
    :cond_5
    move v2, v1

    .line 376
    :goto_5
    iget-object v3, v0, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jLeftPSLines:[F

    .line 377
    .line 378
    array-length v4, v3

    .line 379
    if-ge v2, v4, :cond_6

    .line 380
    .line 381
    iget-object v4, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->leftLines:Ljava/util/List;

    .line 382
    .line 383
    new-instance v5, Lcom/indymobile/app/model/PSLine;

    .line 384
    .line 385
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 386
    .line 387
    aget v7, v3, v2

    .line 388
    .line 389
    add-int/lit8 v8, v2, 0x1

    .line 390
    .line 391
    aget v8, v3, v8

    .line 392
    .line 393
    invoke-direct {v6, v7, v8}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 394
    .line 395
    .line 396
    new-instance v7, Lcom/indymobile/app/model/PSPoint;

    .line 397
    .line 398
    add-int/lit8 v8, v2, 0x2

    .line 399
    .line 400
    aget v8, v3, v8

    .line 401
    .line 402
    add-int/lit8 v9, v2, 0x3

    .line 403
    .line 404
    aget v3, v3, v9

    .line 405
    .line 406
    invoke-direct {v7, v8, v3}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 407
    .line 408
    .line 409
    invoke-direct {v5, v6, v7}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 410
    .line 411
    .line 412
    invoke-interface {v4, v5}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 413
    .line 414
    .line 415
    add-int/lit8 v2, v2, 0x4

    .line 416
    .line 417
    goto :goto_5

    .line 418
    :cond_6
    move v2, v1

    .line 419
    :goto_6
    iget-object v3, v0, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jBottomPSLines:[F

    .line 420
    .line 421
    array-length v4, v3

    .line 422
    if-ge v2, v4, :cond_7

    .line 423
    .line 424
    iget-object v4, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->bottomLines:Ljava/util/List;

    .line 425
    .line 426
    new-instance v5, Lcom/indymobile/app/model/PSLine;

    .line 427
    .line 428
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 429
    .line 430
    aget v7, v3, v2

    .line 431
    .line 432
    add-int/lit8 v8, v2, 0x1

    .line 433
    .line 434
    aget v8, v3, v8

    .line 435
    .line 436
    invoke-direct {v6, v7, v8}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 437
    .line 438
    .line 439
    new-instance v7, Lcom/indymobile/app/model/PSPoint;

    .line 440
    .line 441
    add-int/lit8 v8, v2, 0x2

    .line 442
    .line 443
    aget v8, v3, v8

    .line 444
    .line 445
    add-int/lit8 v9, v2, 0x3

    .line 446
    .line 447
    aget v3, v3, v9

    .line 448
    .line 449
    invoke-direct {v7, v8, v3}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 450
    .line 451
    .line 452
    invoke-direct {v5, v6, v7}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 453
    .line 454
    .line 455
    invoke-interface {v4, v5}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 456
    .line 457
    .line 458
    add-int/lit8 v2, v2, 0x4

    .line 459
    .line 460
    goto :goto_6

    .line 461
    :cond_7
    :goto_7
    iget-object v2, v0, Lcom/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo;->jRightPSLines:[F

    .line 462
    .line 463
    array-length v3, v2

    .line 464
    if-ge v1, v3, :cond_8

    .line 465
    .line 466
    iget-object v3, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->rightLines:Ljava/util/List;

    .line 467
    .line 468
    new-instance v4, Lcom/indymobile/app/model/PSLine;

    .line 469
    .line 470
    new-instance v5, Lcom/indymobile/app/model/PSPoint;

    .line 471
    .line 472
    aget v6, v2, v1

    .line 473
    .line 474
    add-int/lit8 v7, v1, 0x1

    .line 475
    .line 476
    aget v7, v2, v7

    .line 477
    .line 478
    invoke-direct {v5, v6, v7}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 479
    .line 480
    .line 481
    new-instance v6, Lcom/indymobile/app/model/PSPoint;

    .line 482
    .line 483
    add-int/lit8 v7, v1, 0x2

    .line 484
    .line 485
    aget v7, v2, v7

    .line 486
    .line 487
    add-int/lit8 v8, v1, 0x3

    .line 488
    .line 489
    aget v2, v2, v8

    .line 490
    .line 491
    invoke-direct {v6, v7, v2}, Lcom/indymobile/app/model/PSPoint;-><init>(FF)V

    .line 492
    .line 493
    .line 494
    invoke-direct {v4, v5, v6}, Lcom/indymobile/app/model/PSLine;-><init>(Lcom/indymobile/app/model/PSPoint;Lcom/indymobile/app/model/PSPoint;)V

    .line 495
    .line 496
    .line 497
    invoke-interface {v3, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 498
    .line 499
    .line 500
    add-int/lit8 v1, v1, 0x4

    .line 501
    .line 502
    goto :goto_7

    .line 503
    :cond_8
    invoke-virtual {p1}, Lcom/indymobile/app/model/PSPage;->t()V

    .line 504
    .line 505
    .line 506
    return-void
.end method


# virtual methods
.method public final a()V
    .locals 5

    .line 1
    iget v0, p0, Lqt3;->a:I

    .line 2
    .line 3
    const/4 v1, 0x0

    .line 4
    packed-switch v0, :pswitch_data_0

    .line 5
    .line 6
    .line 7
    sget-object v0, Lnq4;->a:Lko0;

    .line 8
    .line 9
    new-instance v2, Ltt3;

    .line 10
    .line 11
    const/16 v3, 0x8

    .line 12
    .line 13
    invoke-direct {v2, v3, p0}, Ltt3;-><init>(ILjava/lang/Object;)V

    .line 14
    .line 15
    .line 16
    new-instance v3, Ljo3;

    .line 17
    .line 18
    invoke-direct {v3, v1, v2}, Ljo3;-><init>(ILjava/lang/Object;)V

    .line 19
    .line 20
    .line 21
    invoke-virtual {v3, v0}, Lgo3;->d(Lfq4;)Lto3;

    .line 22
    .line 23
    .line 24
    move-result-object v0

    .line 25
    invoke-static {}, Lpd;->a()Lfq4;

    .line 26
    .line 27
    .line 28
    move-result-object v1

    .line 29
    invoke-virtual {v0, v1}, Lgo3;->a(Lfq4;)Lno3;

    .line 30
    .line 31
    .line 32
    move-result-object v0

    .line 33
    new-instance v1, Ldu3;

    .line 34
    .line 35
    const/4 v2, 0x4

    .line 36
    invoke-direct {v1, v2, p0}, Ldu3;-><init>(ILjava/lang/Object;)V

    .line 37
    .line 38
    .line 39
    invoke-virtual {v0, v1}, Lgo3;->b(Lyo3;)V

    .line 40
    .line 41
    .line 42
    return-void

    .line 43
    :pswitch_0
    sget-object v0, Lnq4;->a:Lko0;

    .line 44
    .line 45
    new-instance v2, Lmu3;

    .line 46
    .line 47
    const/4 v3, 0x1

    .line 48
    invoke-direct {v2, v3, p0}, Lmu3;-><init>(ILjava/lang/Object;)V

    .line 49
    .line 50
    .line 51
    new-instance v4, Ljo3;

    .line 52
    .line 53
    invoke-direct {v4, v1, v2}, Ljo3;-><init>(ILjava/lang/Object;)V

    .line 54
    .line 55
    .line 56
    invoke-virtual {v4, v0}, Lgo3;->d(Lfq4;)Lto3;

    .line 57
    .line 58
    .line 59
    move-result-object v0

    .line 60
    invoke-static {}, Lpd;->a()Lfq4;

    .line 61
    .line 62
    .line 63
    move-result-object v1

    .line 64
    invoke-virtual {v0, v1}, Lgo3;->a(Lfq4;)Lno3;

    .line 65
    .line 66
    .line 67
    move-result-object v0

    .line 68
    new-instance v1, Lzv3;

    .line 69
    .line 70
    invoke-direct {v1, v3, p0}, Lzv3;-><init>(ILjava/lang/Object;)V

    .line 71
    .line 72
    .line 73
    invoke-virtual {v0, v1}, Lgo3;->b(Lyo3;)V

    .line 74
    .line 75
    .line 76
    return-void

    .line 77
    :pswitch_1
    sget-object v0, Lnq4;->a:Lko0;

    .line 78
    .line 79
    new-instance v2, Ldf1;

    .line 80
    .line 81
    const/16 v3, 0x18

    .line 82
    .line 83
    invoke-direct {v2, v3, p0}, Ldf1;-><init>(ILjava/lang/Object;)V

    .line 84
    .line 85
    .line 86
    new-instance v3, Ljo3;

    .line 87
    .line 88
    invoke-direct {v3, v1, v2}, Ljo3;-><init>(ILjava/lang/Object;)V

    .line 89
    .line 90
    .line 91
    invoke-virtual {v3, v0}, Lgo3;->d(Lfq4;)Lto3;

    .line 92
    .line 93
    .line 94
    move-result-object v0

    .line 95
    invoke-static {}, Lpd;->a()Lfq4;

    .line 96
    .line 97
    .line 98
    move-result-object v1

    .line 99
    invoke-virtual {v0, v1}, Lgo3;->a(Lfq4;)Lno3;

    .line 100
    .line 101
    .line 102
    move-result-object v0

    .line 103
    new-instance v1, Lea1;

    .line 104
    .line 105
    const/16 v2, 0x17

    .line 106
    .line 107
    invoke-direct {v1, v2, p0}, Lea1;-><init>(ILjava/lang/Object;)V

    .line 108
    .line 109
    .line 110
    invoke-virtual {v0, v1}, Lgo3;->b(Lyo3;)V

    .line 111
    .line 112
    .line 113
    return-void

    .line 114
    nop

    .line 115
    :pswitch_data_0
    .packed-switch 0x0
        :pswitch_1
        :pswitch_0
    .end packed-switch
.end method
