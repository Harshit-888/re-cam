.class public final enum Lyu3;
.super Ljava/lang/Enum;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"


# static fields
.field private static final synthetic $VALUES:[Lyu3;

.field public static final enum kPSColorTypeColor:Lyu3;

.field public static final enum kPSColorTypeGrayScale:Lyu3;


# instance fields
.field private final name:Ljava/lang/String;


# direct methods
.method static constructor <clinit>()V
    .locals 5

    .line 1
    new-instance v0, Lyu3;

    .line 2
    .line 3
    const/4 v1, 0x0

    .line 4
    const-string v2, "color"

    .line 5
    .line 6
    const-string v3, "kPSColorTypeColor"

    .line 7
    .line 8
    invoke-direct {v0, v3, v1, v2}, Lyu3;-><init>(Ljava/lang/String;ILjava/lang/String;)V

    .line 9
    .line 10
    .line 11
    sput-object v0, Lyu3;->kPSColorTypeColor:Lyu3;

    .line 12
    .line 13
    new-instance v1, Lyu3;

    .line 14
    .line 15
    const/4 v2, 0x1

    .line 16
    const-string v3, "gray"

    .line 17
    .line 18
    const-string v4, "kPSColorTypeGrayScale"

    .line 19
    .line 20
    invoke-direct {v1, v4, v2, v3}, Lyu3;-><init>(Ljava/lang/String;ILjava/lang/String;)V

    .line 21
    .line 22
    .line 23
    sput-object v1, Lyu3;->kPSColorTypeGrayScale:Lyu3;

    .line 24
    .line 25
    filled-new-array {v0, v1}, [Lyu3;

    .line 26
    .line 27
    .line 28
    move-result-object v0

    .line 29
    sput-object v0, Lyu3;->$VALUES:[Lyu3;

    .line 30
    .line 31
    return-void
.end method

.method public constructor <init>(Ljava/lang/String;ILjava/lang/String;)V
    .locals 0

    .line 1
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V

    .line 2
    .line 3
    .line 4
    iput-object p3, p0, Lyu3;->name:Ljava/lang/String;

    .line 5
    .line 6
    return-void
.end method

.method public static valueOf(Ljava/lang/String;)Lyu3;
    .locals 1

    .line 1
    const-class v0, Lyu3;

    .line 2
    .line 3
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;

    .line 4
    .line 5
    .line 6
    move-result-object p0

    .line 7
    check-cast p0, Lyu3;

    .line 8
    .line 9
    return-object p0
.end method

.method public static values()[Lyu3;
    .locals 1

    .line 1
    sget-object v0, Lyu3;->$VALUES:[Lyu3;

    .line 2
    .line 3
    invoke-virtual {v0}, [Lyu3;->clone()Ljava/lang/Object;

    .line 4
    .line 5
    .line 6
    move-result-object v0

    .line 7
    check-cast v0, [Lyu3;

    .line 8
    .line 9
    return-object v0
.end method


# virtual methods
.method public final toString()Ljava/lang/String;
    .locals 0

    .line 1
    iget-object p0, p0, Lyu3;->name:Ljava/lang/String;

    .line 2
    .line 3
    return-object p0
.end method
