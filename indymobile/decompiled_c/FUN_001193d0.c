
void FUN_001193d0(undefined8 *param_1,undefined8 *param_2)

{
  int iVar1;
  void *pvVar2;
  undefined8 uVar3;
  undefined8 uVar4;
  undefined8 uVar5;
  undefined8 *puVar6;
  undefined8 *puVar7;
  undefined8 uVar8;
  undefined8 uVar9;
  undefined8 uVar10;
  undefined8 uVar11;
  undefined8 uVar12;
  undefined8 uVar13;
  
  if (param_1 != param_2) {
    while (puVar6 = param_1, param_1 = puVar6 + 8, param_1 != param_2) {
      if (*(int *)(puVar6 + 7) < *(int *)(puVar6 + 0xf)) {
        uVar12 = puVar6[9];
        uVar11 = *param_1;
        uVar5 = puVar6[10];
        *param_1 = 0;
        puVar6[9] = 0;
        puVar6[10] = 0;
        uVar9 = puVar6[0xc];
        uVar8 = puVar6[0xb];
        uVar4 = puVar6[0xd];
        uVar13 = puVar6[0xe];
        iVar1 = *(int *)(puVar6 + 0xf);
        puVar6[0xc] = 0;
        puVar6[0xd] = 0;
        puVar6[0xb] = 0;
        puVar6 = param_1;
        do {
          puVar7 = puVar6;
          pvVar2 = (void *)*puVar7;
          if (pvVar2 != (void *)0x0) {
            puVar7[1] = pvVar2;
            operator_delete(pvVar2);
            puVar7[1] = 0;
            puVar7[2] = 0;
          }
          uVar10 = puVar7[-7];
          uVar3 = puVar7[-6];
          pvVar2 = (void *)puVar7[3];
          puVar7[-7] = 0;
          puVar7[-6] = 0;
          puVar7[1] = uVar10;
          *puVar7 = puVar7[-8];
          puVar7[2] = uVar3;
          puVar7[-8] = 0;
          if (pvVar2 != (void *)0x0) {
            puVar7[4] = pvVar2;
            operator_delete(pvVar2);
            puVar7[4] = 0;
            puVar7[5] = 0;
          }
          uVar3 = puVar7[-3];
          uVar10 = puVar7[-4];
          puVar7[-4] = 0;
          puVar7[-3] = 0;
          puVar7[4] = uVar10;
          puVar7[3] = puVar7[-5];
          puVar7[-5] = 0;
          puVar7[5] = uVar3;
          puVar7[6] = puVar7[-2];
          *(undefined4 *)(puVar7 + 7) = *(undefined4 *)(puVar7 + -1);
          puVar6 = puVar7 + -8;
        } while (*(int *)(puVar7 + -9) < iVar1);
        pvVar2 = (void *)*puVar6;
        if (pvVar2 != (void *)0x0) {
          puVar7[-7] = pvVar2;
          operator_delete(pvVar2);
        }
        pvVar2 = (void *)puVar7[-5];
        puVar7[-6] = uVar5;
        puVar7[-7] = uVar12;
        *puVar6 = uVar11;
        if (pvVar2 != (void *)0x0) {
          puVar7[-4] = pvVar2;
          operator_delete(pvVar2);
        }
        puVar7[-3] = uVar4;
        puVar7[-2] = uVar13;
        puVar7[-4] = uVar9;
        puVar7[-5] = uVar8;
        *(int *)(puVar7 + -1) = iVar1;
      }
    }
  }
  return;
}

