.class public final enum Lcv3;
.super Ljava/lang/Enum;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"


# static fields
.field private static final synthetic $VALUES:[Lcv3;

.field public static final enum kPSEnhanceTypeDocumentLevel1:Lcv3;

.field public static final enum kPSEnhanceTypeDocumentLevel2:Lcv3;

.field public static final enum kPSEnhanceTypeLighten:Lcv3;

.field public static final enum kPSEnhanceTypeOriginal:Lcv3;

.field public static final enum kPSEnhanceTypeUndefined:Lcv3;


# instance fields
.field private final name:Ljava/lang/String;


# direct methods
.method static constructor <clinit>()V
    .locals 8

    .line 1
    new-instance v0, Lcv3;

    .line 2
    .line 3
    const/4 v1, 0x0

    .line 4
    const-string v2, "undefined"

    .line 5
    .line 6
    const-string v3, "kPSEnhanceTypeUndefined"

    .line 7
    .line 8
    invoke-direct {v0, v3, v1, v2}, Lcv3;-><init>(Ljava/lang/String;ILjava/lang/String;)V

    .line 9
    .line 10
    .line 11
    sput-object v0, Lcv3;->kPSEnhanceTypeUndefined:Lcv3;

    .line 12
    .line 13
    new-instance v1, Lcv3;

    .line 14
    .line 15
    const/4 v2, 0x1

    .line 16
    const-string v3, "original"

    .line 17
    .line 18
    const-string v4, "kPSEnhanceTypeOriginal"

    .line 19
    .line 20
    invoke-direct {v1, v4, v2, v3}, Lcv3;-><init>(Ljava/lang/String;ILjava/lang/String;)V

    .line 21
    .line 22
    .line 23
    sput-object v1, Lcv3;->kPSEnhanceTypeOriginal:Lcv3;

    .line 24
    .line 25
    new-instance v2, Lcv3;

    .line 26
    .line 27
    const/4 v3, 0x2

    .line 28
    const-string v4, "photo"

    .line 29
    .line 30
    const-string v5, "kPSEnhanceTypeLighten"

    .line 31
    .line 32
    invoke-direct {v2, v5, v3, v4}, Lcv3;-><init>(Ljava/lang/String;ILjava/lang/String;)V

    .line 33
    .line 34
    .line 35
    sput-object v2, Lcv3;->kPSEnhanceTypeLighten:Lcv3;

    .line 36
    .line 37
    new-instance v3, Lcv3;

    .line 38
    .line 39
    const/4 v4, 0x3

    .line 40
    const-string v5, "doc"

    .line 41
    .line 42
    const-string v6, "kPSEnhanceTypeDocumentLevel1"

    .line 43
    .line 44
    invoke-direct {v3, v6, v4, v5}, Lcv3;-><init>(Ljava/lang/String;ILjava/lang/String;)V

    .line 45
    .line 46
    .line 47
    sput-object v3, Lcv3;->kPSEnhanceTypeDocumentLevel1:Lcv3;

    .line 48
    .line 49
    new-instance v4, Lcv3;

    .line 50
    .line 51
    const/4 v5, 0x4

    .line 52
    const-string v6, "clear"

    .line 53
    .line 54
    const-string v7, "kPSEnhanceTypeDocumentLevel2"

    .line 55
    .line 56
    invoke-direct {v4, v7, v5, v6}, Lcv3;-><init>(Ljava/lang/String;ILjava/lang/String;)V

    .line 57
    .line 58
    .line 59
    sput-object v4, Lcv3;->kPSEnhanceTypeDocumentLevel2:Lcv3;

    .line 60
    .line 61
    filled-new-array {v0, v1, v2, v3, v4}, [Lcv3;

    .line 62
    .line 63
    .line 64
    move-result-object v0

    .line 65
    sput-object v0, Lcv3;->$VALUES:[Lcv3;

    .line 66
    .line 67
    return-void
.end method

.method public constructor <init>(Ljava/lang/String;ILjava/lang/String;)V
    .locals 0

    .line 1
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V

    .line 2
    .line 3
    .line 4
    iput-object p3, p0, Lcv3;->name:Ljava/lang/String;

    .line 5
    .line 6
    return-void
.end method

.method public static valueOf(Ljava/lang/String;)Lcv3;
    .locals 1

    .line 1
    const-class v0, Lcv3;

    .line 2
    .line 3
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;

    .line 4
    .line 5
    .line 6
    move-result-object p0

    .line 7
    check-cast p0, Lcv3;

    .line 8
    .line 9
    return-object p0
.end method

.method public static values()[Lcv3;
    .locals 1

    .line 1
    sget-object v0, Lcv3;->$VALUES:[Lcv3;

    .line 2
    .line 3
    invoke-virtual {v0}, [Lcv3;->clone()Ljava/lang/Object;

    .line 4
    .line 5
    .line 6
    move-result-object v0

    .line 7
    check-cast v0, [Lcv3;

    .line 8
    .line 9
    return-object v0
.end method


# virtual methods
.method public final toString()Ljava/lang/String;
    .locals 0

    .line 1
    iget-object p0, p0, Lcv3;->name:Ljava/lang/String;

    .line 2
    .line 3
    return-object p0
.end method
