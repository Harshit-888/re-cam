.class public final enum Lhv3;
.super Ljava/lang/Enum;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"


# static fields
.field private static final synthetic $VALUES:[Lhv3;

.field public static final enum PSImageTypeDocumentImage:Lhv3;

.field public static final enum PSImageTypePhotoImage:Lhv3;


# direct methods
.method static constructor <clinit>()V
    .locals 4

    .line 1
    new-instance v0, Lhv3;

    .line 2
    .line 3
    const-string v1, "PSImageTypePhotoImage"

    .line 4
    .line 5
    const/4 v2, 0x0

    .line 6
    invoke-direct {v0, v1, v2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V

    .line 7
    .line 8
    .line 9
    sput-object v0, Lhv3;->PSImageTypePhotoImage:Lhv3;

    .line 10
    .line 11
    new-instance v1, Lhv3;

    .line 12
    .line 13
    const-string v2, "PSImageTypeDocumentImage"

    .line 14
    .line 15
    const/4 v3, 0x1

    .line 16
    invoke-direct {v1, v2, v3}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V

    .line 17
    .line 18
    .line 19
    sput-object v1, Lhv3;->PSImageTypeDocumentImage:Lhv3;

    .line 20
    .line 21
    filled-new-array {v0, v1}, [Lhv3;

    .line 22
    .line 23
    .line 24
    move-result-object v0

    .line 25
    sput-object v0, Lhv3;->$VALUES:[Lhv3;

    .line 26
    .line 27
    return-void
.end method

.method public static valueOf(Ljava/lang/String;)Lhv3;
    .locals 1

    .line 1
    const-class v0, Lhv3;

    .line 2
    .line 3
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;

    .line 4
    .line 5
    .line 6
    move-result-object p0

    .line 7
    check-cast p0, Lhv3;

    .line 8
    .line 9
    return-object p0
.end method

.method public static values()[Lhv3;
    .locals 1

    .line 1
    sget-object v0, Lhv3;->$VALUES:[Lhv3;

    .line 2
    .line 3
    invoke-virtual {v0}, [Lhv3;->clone()Ljava/lang/Object;

    .line 4
    .line 5
    .line 6
    move-result-object v0

    .line 7
    check-cast v0, [Lhv3;

    .line 8
    .line 9
    return-object v0
.end method
