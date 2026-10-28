
undefined8 FUN_0011feac(ulong *param_1,undefined8 param_2,undefined8 *param_3)

{
  ulong uVar1;
  undefined8 uVar2;
  long lVar3;
  uint uVar4;
  ulong uVar5;
  ulong uVar6;
  ulong *puVar7;
  long lVar8;
  int *piVar9;
  long lVar10;
  ulong uVar11;
  ulong uVar12;
  ulong uVar13;
  undefined8 local_70 [4];
  
  uVar12 = (ulong)(ushort)param_1[3];
  if (uVar12 != 0) {
    uVar6 = param_3[2];
    uVar13 = *param_1;
    if (uVar13 <= uVar6) {
      piVar9 = (int *)param_1[2];
      uVar5 = uVar12;
      do {
        if (((*piVar9 == 1) && (uVar1 = *(long *)(piVar9 + 4) + uVar13, uVar1 <= uVar6)) &&
           (uVar11 = *(ulong *)(piVar9 + 10), uVar6 < uVar11 + uVar1)) {
          puVar7 = (ulong *)param_3[1];
          uVar6 = uVar12 + 0xffffffff;
          *puVar7 = uVar1;
          puVar7[1] = uVar11;
          do {
            piVar9 = (int *)(param_1[2] + (uVar6 & 0xffffffff) * 0x38);
            if (*piVar9 == 0x6474e550) {
              uVar2 = *param_3;
              lVar3 = param_3[1];
              lVar8 = *(long *)(piVar9 + 10);
              lVar10 = *(long *)(piVar9 + 4) + uVar13;
              *(long *)(lVar3 + 0x20) = lVar10;
              *(long *)(lVar3 + 0x28) = lVar8;
              uVar5 = FUN_0011ffdc(uVar2,lVar10,lVar8 + lVar10,local_70);
              if ((uVar5 & 1) != 0) {
                lVar10 = param_3[1];
                *(undefined8 *)(lVar10 + 0x10) = local_70[0];
                *(undefined8 *)(lVar10 + 0x18) = 0xffffffffffffffff;
                return 1;
              }
            }
            uVar4 = (int)uVar12 - 1;
            uVar12 = (ulong)uVar4;
            uVar6 = uVar6 - 1;
          } while ((uVar4 & 0xffff) != 0);
          return 0;
        }
        uVar5 = uVar5 - 1;
        piVar9 = piVar9 + 0xe;
      } while (uVar5 != 0);
    }
  }
  return 0;
}

