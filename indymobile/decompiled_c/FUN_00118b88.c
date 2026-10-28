
void FUN_00118b88(undefined8 *param_1,undefined8 *param_2)

{
  undefined8 *puVar1;
  undefined8 *puVar2;
  undefined8 *puVar3;
  void *pvVar4;
  undefined8 *puVar5;
  undefined8 *puVar6;
  undefined8 uVar7;
  undefined8 *puVar8;
  
  puVar8 = (undefined8 *)*param_1;
  puVar3 = (undefined8 *)param_1[1];
  puVar2 = (undefined8 *)((long)puVar8 + (param_2[1] - (long)puVar3));
  puVar5 = puVar2;
  puVar6 = puVar8;
  if (puVar3 != puVar8) {
    do {
      *puVar5 = 0;
      puVar5[1] = 0;
      puVar5[2] = 0;
      uVar7 = *puVar6;
      puVar5[1] = puVar6[1];
      *puVar5 = uVar7;
      puVar5[2] = puVar6[2];
      *puVar6 = 0;
      puVar6[1] = 0;
      puVar6[2] = 0;
      puVar5[3] = 0;
      puVar5[4] = 0;
      puVar5[5] = 0;
      uVar7 = puVar6[3];
      puVar5[4] = puVar6[4];
      puVar5[3] = uVar7;
      puVar5[5] = puVar6[5];
      uVar7 = puVar6[6];
      puVar6[3] = 0;
      puVar6[4] = 0;
      puVar6[5] = 0;
      puVar1 = puVar6 + 8;
      *(undefined4 *)(puVar5 + 7) = *(undefined4 *)(puVar6 + 7);
      puVar5[6] = uVar7;
      puVar5 = puVar5 + 8;
      puVar6 = puVar1;
    } while (puVar1 != puVar3);
    do {
      pvVar4 = (void *)puVar8[3];
      if (pvVar4 != (void *)0x0) {
        puVar8[4] = pvVar4;
        operator_delete(pvVar4);
      }
      pvVar4 = (void *)*puVar8;
      if (pvVar4 != (void *)0x0) {
        puVar8[1] = pvVar4;
        operator_delete(pvVar4);
      }
      puVar8 = puVar8 + 8;
    } while (puVar8 != puVar3);
  }
  param_2[1] = puVar2;
  uVar7 = *param_1;
  *param_1 = puVar2;
  param_1[1] = uVar7;
  param_2[1] = uVar7;
  uVar7 = param_1[1];
  param_1[1] = param_2[2];
  param_2[2] = uVar7;
  uVar7 = param_1[2];
  param_1[2] = param_2[3];
  param_2[3] = uVar7;
  *param_2 = param_2[1];
  return;
}

