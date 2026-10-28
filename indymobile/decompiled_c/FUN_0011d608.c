
void FUN_0011d608(long *param_1,ulong param_2)

{
  int iVar1;
  int iVar2;
  long lVar3;
  ulong uVar4;
  int *piVar5;
  int *piVar6;
  undefined *puVar7;
  ulong uVar8;
  undefined1 auStack_6f0 [32];
  long local_6d0;
  long local_6b8;
  undefined4 uStack_6b0;
  long local_6a0;
  long lStack_698;
  long local_690;
  undefined1 auStack_688 [16];
  long local_678;
  long local_658;
  undefined1 *puStack_650;
  ulong local_648;
  
  *(undefined1 *)((long)param_1 + 0x26a) = 0;
  lVar3 = (**(code **)(*param_1 + 0x18))(param_1,0xffffffff);
  if (lVar3 != 0) {
    local_658 = param_1[1];
    puStack_650 = auStack_688;
    uVar8 = lVar3 - (param_2 & 1);
    local_648 = uVar8;
    iVar2 = dl_iterate_phdr(FUN_0011feac,&local_658);
    if (((iVar2 != 0) && (local_678 != 0)) &&
       (uVar4 = FUN_0011fbd8(param_1,uVar8,auStack_688,0), (uVar4 & 1) != 0)) {
      return;
    }
    pthread_rwlock_rdlock((pthread_rwlock_t *)&DAT_001294dc);
    for (puVar7 = PTR_DAT_00129480; puVar7 < PTR_DAT_00129488; puVar7 = puVar7 + 0x20) {
      if ((*(ulong *)(puVar7 + 8) <= uVar8) && (uVar8 < *(ulong *)(puVar7 + 0x10))) {
        lVar3 = *(long *)(puVar7 + 0x18);
        pthread_rwlock_unlock((pthread_rwlock_t *)&DAT_001294dc);
        if ((lVar3 != 0) &&
           (lVar3 = FUN_0011cde0(param_1[1],lVar3,&local_6b8,auStack_6f0,0), lVar3 == 0)) {
          memset(&local_658,0,0x618);
          uVar8 = FUN_0011de04(param_1[1],&local_6b8,auStack_6f0,uVar8,4,&local_658);
          if ((uVar8 & 1) != 0) {
            param_1[0x49] = 0;
            param_1[0x46] = local_690;
            param_1[0x47] = local_6d0;
            param_1[0x48] = local_648 & 0xffffffff;
            *(undefined4 *)(param_1 + 0x4a) = 0x3000000;
            param_1[0x45] = lStack_698;
            param_1[0x44] = local_6a0;
            param_1[0x4c] = 0;
            param_1[0x4b] = local_6b8;
            *(undefined4 *)((long)param_1 + 0x254) = uStack_6b0;
            return;
          }
        }
        goto LAB_0011d780;
      }
    }
    pthread_rwlock_unlock((pthread_rwlock_t *)&DAT_001294dc);
LAB_0011d780:
    piVar5 = (int *)(**(code **)(*param_1 + 0x18))(param_1,0xffffffff);
    if (piVar5 != (int *)0x0) {
      piVar6 = (int *)__errno();
      iVar2 = *piVar6;
      syscall(0x87,0xffffffff,piVar5,0,8);
      iVar1 = *piVar6;
      *piVar6 = iVar2;
      if (((iVar1 != 0xe) && (*piVar5 == -0x2d7fee98)) && (piVar5[1] == -0x2bffffff)) {
        param_1[0x4c] = 0;
        param_1[0x45] = (long)(piVar5 + 1);
        param_1[0x44] = (long)piVar5;
        *(undefined1 *)((long)param_1 + 0x26a) = 1;
        param_1[0x49] = 0;
        param_1[0x48] = 0;
        param_1[0x4b] = 0;
        param_1[0x4a] = 0;
        param_1[0x47] = 0;
        param_1[0x46] = 0;
        return;
      }
    }
  }
  *(undefined1 *)(param_1 + 0x4d) = 1;
  return;
}

