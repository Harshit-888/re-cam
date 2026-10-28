
void FUN_0010be24(undefined8 *param_1,undefined8 *param_2,undefined8 *param_3)

{
  undefined8 *puVar1;
  long lVar2;
  ulong uVar3;
  long lVar4;
  undefined8 *puVar5;
  undefined8 *puVar6;
  undefined8 uVar7;
  undefined8 local_90;
  undefined8 uStack_88;
  undefined8 local_80;
  undefined8 local_70;
  undefined8 uStack_68;
  long local_58;
  
  lVar2 = tpidr_el0;
  local_58 = *(long *)(lVar2 + 0x28);
  lVar4 = 0;
  uStack_88 = param_1[1];
  local_90 = *param_1;
  local_80 = param_1[2];
  do {
    lVar4 = lVar4 + 0x18;
    uVar3 = (*(code *)*param_3)(lVar4 + (long)param_1,&local_90);
  } while ((uVar3 & 1) != 0);
  puVar1 = (undefined8 *)((long)param_1 + lVar4);
  if (lVar4 == 0x18) {
    do {
      if (param_2 <= puVar1) break;
      param_2 = param_2 + -3;
      uVar3 = (*(code *)*param_3)(param_2,&local_90);
    } while ((uVar3 & 1) == 0);
  }
  else {
    do {
      param_2 = param_2 + -3;
      uVar3 = (*(code *)*param_3)(param_2,&local_90);
    } while ((uVar3 & 1) == 0);
  }
  puVar5 = puVar1;
  puVar6 = param_2;
  if (puVar1 < param_2) {
    do {
      uVar7 = *puVar5;
      *puVar5 = *puVar6;
      *puVar6 = uVar7;
      uStack_68 = puVar5[2];
      local_70 = puVar5[1];
      uVar7 = puVar6[1];
      puVar5[2] = puVar6[2];
      puVar5[1] = uVar7;
      puVar6[2] = uStack_68;
      puVar6[1] = local_70;
      do {
        puVar5 = puVar5 + 3;
        uVar3 = (*(code *)*param_3)(puVar5,&local_90);
      } while ((uVar3 & 1) != 0);
      do {
        puVar6 = puVar6 + -3;
        uVar3 = (*(code *)*param_3)(puVar6,&local_90);
      } while ((uVar3 & 1) == 0);
    } while (puVar5 < puVar6);
  }
  if (puVar5 + -3 != param_1) {
    *param_1 = puVar5[-3];
    uVar7 = puVar5[-2];
    param_1[2] = puVar5[-1];
    param_1[1] = uVar7;
  }
  puVar5[-3] = local_90;
  puVar5[-1] = local_80;
  puVar5[-2] = uStack_88;
  if (*(long *)(lVar2 + 0x28) != local_58) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail(puVar5 + -3,param_2 <= puVar1);
  }
  return;
}

