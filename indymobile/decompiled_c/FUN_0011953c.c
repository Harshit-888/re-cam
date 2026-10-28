
void FUN_0011953c(long param_1,long param_2,long param_3)

{
  int iVar1;
  long lVar2;
  long *plVar3;
  undefined8 uVar4;
  long *plVar5;
  long local_40;
  long local_38;
  long lStack_30;
  long local_28;
  
  plVar5 = &local_40;
  lVar2 = tpidr_el0;
  local_28 = *(long *)(lVar2 + 0x28);
  iVar1 = *(int *)(param_2 + 0x38);
  local_40 = param_3;
  local_38 = param_2;
  lStack_30 = param_1;
  if (*(int *)(param_1 + 0x38) < iVar1) {
    if (iVar1 < *(int *)(param_3 + 0x38)) {
      FUN_001190dc(&lStack_30,&local_40);
    }
    else {
      FUN_001190dc(&lStack_30,&local_38);
      if (*(int *)(local_38 + 0x38) < *(int *)(local_40 + 0x38)) {
        plVar3 = &local_38;
LAB_001195e4:
        FUN_001190dc(plVar3,plVar5);
        uVar4 = 2;
        goto LAB_001195fc;
      }
    }
  }
  else {
    if (*(int *)(param_3 + 0x38) <= iVar1) {
      uVar4 = 0;
      goto LAB_001195fc;
    }
    FUN_001190dc(&local_38,&local_40);
    if (*(int *)(lStack_30 + 0x38) < *(int *)(local_38 + 0x38)) {
      plVar3 = &lStack_30;
      plVar5 = &local_38;
      goto LAB_001195e4;
    }
  }
  uVar4 = 1;
LAB_001195fc:
  if (*(long *)(lVar2 + 0x28) == local_28) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail(uVar4);
}

