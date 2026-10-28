
undefined8 FUN_0011fbd8(long param_1,ulong param_2,long *param_3,uint param_4)

{
  bool bVar1;
  byte bVar2;
  undefined *__src;
  ulong uVar3;
  undefined *__dest;
  long *plVar4;
  size_t __n;
  long lVar5;
  undefined1 auStack_6e8 [32];
  undefined8 local_6c8;
  long local_6b0;
  undefined4 uStack_6a8;
  long local_698;
  long local_690;
  undefined8 uStack_688;
  undefined1 auStack_680 [16];
  uint local_670;
  
  if (((param_4 == 0) ||
      (uVar3 = FUN_001200fc(*(undefined8 *)(param_1 + 8),param_2,param_3[2],param_3[3],
                            param_3[2] + (ulong)param_4,&local_6b0,auStack_6e8), (uVar3 & 1) == 0))
     && ((param_3[4] == 0 ||
         (uVar3 = FUN_001202c4(*(undefined8 *)(param_1 + 8),param_2,param_3[4],(int)param_3[5],
                               &local_6b0,auStack_6e8), (uVar3 & 1) == 0)))) {
    lVar5 = *param_3;
    pthread_rwlock_rdlock((pthread_rwlock_t *)&DAT_001294dc);
    if (PTR_DAT_00129480 < PTR_DAT_00129488) {
      plVar4 = (long *)PTR_DAT_00129480;
      if (lVar5 == -1) {
        do {
          if (((ulong)plVar4[1] <= param_2) && (param_2 < (ulong)plVar4[2])) goto LAB_0011fce0;
          plVar4 = plVar4 + 4;
        } while (plVar4 < PTR_DAT_00129488);
      }
      else {
        do {
          if (((*plVar4 == lVar5) && ((ulong)plVar4[1] <= param_2)) && (param_2 < (ulong)plVar4[2]))
          goto LAB_0011fce0;
          plVar4 = plVar4 + 4;
        } while (plVar4 < PTR_DAT_00129488);
      }
    }
    pthread_rwlock_unlock((pthread_rwlock_t *)&DAT_001294dc);
    goto LAB_0011fd28;
  }
LAB_0011fd48:
  bVar2 = 1;
LAB_0011fd4c:
  lVar5 = *param_3;
  memset(auStack_680,0,0x618);
  uVar3 = FUN_0011de04(*(undefined8 *)(param_1 + 8),&local_6b0,auStack_6e8,param_2,4,auStack_680);
  if ((uVar3 & 1) == 0) {
    return 0;
  }
  *(undefined8 *)(param_1 + 0x230) = uStack_688;
  *(long *)(param_1 + 0x228) = local_690;
  *(undefined8 *)(param_1 + 0x238) = local_6c8;
  bVar1 = (bool)(bVar2 ^ 1);
  *(ulong *)(param_1 + 0x240) = (ulong)local_670;
  *(undefined4 *)(param_1 + 0x250) = 0x3000000;
  *(long *)(param_1 + 0x220) = local_698;
  *(undefined8 *)(param_1 + 0x248) = 0;
  *(undefined4 *)(param_1 + 0x254) = uStack_6a8;
  if (param_4 != 0) {
    bVar1 = true;
  }
  *(long *)(param_1 + 600) = local_6b0;
  *(long *)(param_1 + 0x260) = lVar5;
  if (!bVar1) {
    if (param_3[4] == 0) {
      lVar5 = *param_3;
      pthread_rwlock_wrlock((pthread_rwlock_t *)&DAT_001294dc);
      __src = PTR_DAT_00129480;
      if (DAT_00129490 <= PTR_DAT_00129488) {
        __n = (long)DAT_00129490 - (long)PTR_DAT_00129480;
        __dest = malloc(__n * 4);
        memcpy(__dest,__src,__n);
        if ((undefined8 *)__src != &DAT_00129518) {
          free(__src);
        }
        DAT_00129490 = __dest + ((long)__n >> 3) * 0x20;
        PTR_DAT_00129488 = __dest + __n;
        PTR_DAT_00129480 = __dest;
      }
      *(long *)PTR_DAT_00129488 = lVar5;
      *(long *)((long)PTR_DAT_00129488 + 8) = local_698;
      *(long *)((long)PTR_DAT_00129488 + 0x18) = local_6b0;
      *(long *)((long)PTR_DAT_00129488 + 0x10) = local_690;
      PTR_DAT_00129488 = (undefined *)((long)PTR_DAT_00129488 + 0x20);
      pthread_rwlock_unlock((pthread_rwlock_t *)&DAT_001294dc);
      return 1;
    }
    return 1;
  }
  return 1;
LAB_0011fce0:
  lVar5 = plVar4[3];
  pthread_rwlock_unlock((pthread_rwlock_t *)&DAT_001294dc);
  if ((lVar5 != 0) &&
     (uVar3 = FUN_001200fc(*(undefined8 *)(param_1 + 8),param_2,param_3[2],param_3[3],lVar5,
                           &local_6b0,auStack_6e8), (uVar3 & 1) != 0)) {
    bVar2 = 0;
    goto LAB_0011fd4c;
  }
LAB_0011fd28:
  uVar3 = FUN_001200fc(*(undefined8 *)(param_1 + 8),param_2,param_3[2],param_3[3],0,&local_6b0,
                       auStack_6e8);
  if ((uVar3 & 1) == 0) {
    return 0;
  }
  goto LAB_0011fd48;
}

