
/* WARNING: Type propagation algorithm not settling */

undefined8
FUN_0011de04(undefined8 param_1,long *param_2,long *param_3,long param_4,int param_5,
            undefined4 *param_6)

{
  byte bVar1;
  byte *pbVar2;
  byte bVar3;
  undefined *puVar4;
  undefined8 *puVar5;
  undefined4 uVar6;
  long lVar7;
  char *__ptr;
  size_t __size;
  FILE *__s;
  uint uVar8;
  byte *pbVar9;
  ulong uVar10;
  undefined8 *puVar11;
  long lVar12;
  byte *pbVar13;
  int iVar14;
  ulong uVar15;
  ulong uVar16;
  ulong uVar17;
  ulong uVar18;
  long lVar19;
  ulong uVar20;
  undefined8 uVar21;
  undefined8 uVar22;
  undefined1 auStack_6f0 [8];
  undefined8 *local_6e8;
  undefined8 *puStack_6e0;
  undefined8 local_6d8;
  int local_6cc;
  undefined8 *local_6c8;
  undefined1 *local_6c0;
  undefined1 auStack_6a0 [544];
  undefined8 local_480 [124];
  byte *local_a0;
  ulong local_98 [7];
  
  puVar5 = (undefined8 *)auStack_6f0;
  lVar19 = 0;
  local_98[0] = param_3[2];
  local_98[1] = param_3[1] + *param_3;
  local_98[2] = 0xffffffffffffffff;
  local_98[3] = param_2[2];
  local_6cc = param_5;
  local_6d8 = param_1;
  local_98[4] = param_2[1] + *param_2;
  local_98[5] = param_4 - param_2[3];
  local_6c8 = (undefined8 *)0x0;
  local_6c0 = auStack_6a0;
  local_6e8 = local_480;
  puStack_6e0 = (undefined8 *)(param_6 + 0x8e);
  while( true ) {
    pbVar13 = *(byte **)((long)local_98 + lVar19);
    pbVar2 = *(byte **)((long)local_98 + lVar19 + 8);
    uVar20 = *(ulong *)((long)local_98 + lVar19 + 0x10);
    local_a0 = pbVar13;
    if (pbVar13 < pbVar2 && uVar20 != 0) break;
LAB_0011de94:
    lVar19 = lVar19 + 0x18;
    if (lVar19 == 0x30) {
      return 1;
    }
  }
  uVar17 = 0;
LAB_0011dec4:
  puVar11 = local_6c8;
  puVar4 = PTR___sF_001250b0;
  pbVar9 = pbVar13 + 1;
  bVar3 = *pbVar13;
  local_a0 = pbVar9;
  switch((ulong)bVar3) {
  case 0:
    goto code_r0x0011e6bc;
  case 1:
    uVar17 = FUN_0011ecc0(local_6d8,&local_a0,pbVar2,(char)param_3[3],0);
    pbVar13 = local_a0;
    break;
  case 2:
    uVar8 = (uint)pbVar13[1];
    iVar14 = (int)param_3[5];
    local_a0 = pbVar13 + 2;
    goto LAB_0011e2f4;
  case 3:
    uVar8 = (uint)*(ushort *)(pbVar13 + 1);
    iVar14 = (int)param_3[5];
    local_a0 = pbVar13 + 3;
    goto LAB_0011e2f4;
  case 4:
    uVar8 = *(uint *)(pbVar13 + 1);
    iVar14 = (int)param_3[5];
    local_a0 = pbVar13 + 5;
LAB_0011e2f4:
    uVar17 = uVar17 + iVar14 * uVar8;
    pbVar13 = local_a0;
    break;
  case 5:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    lVar7 = FUN_0011f04c(&local_a0,pbVar2);
    if (0x5f < uVar18) {
      __ptr = "libunwind: malformed DW_CFA_offset_extended DWARF unwind, reg too big\n";
      __size = 0x46;
      __s = (FILE *)(PTR___sF_001250b0 + 0x130);
      goto LAB_0011e86c;
    }
    puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
    lVar12 = (long)*(int *)((long)param_3 + 0x2c);
    if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
      uVar21 = *puVar11;
      *(undefined1 *)((long)puVar11 + 4) = 1;
      *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
      *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
    }
LAB_0011e4a4:
    lVar7 = lVar7 * lVar12;
LAB_0011e4a8:
    *(undefined4 *)puVar11 = 2;
    puVar11[1] = lVar7;
    pbVar13 = local_a0;
    break;
  case 6:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    if (0x5f < uVar18) {
      __ptr = "libunwind: malformed DW_CFA_restore_extended DWARF unwind, reg too big\n";
LAB_0011e75c:
      __s = (FILE *)(PTR___sF_001250b0 + 0x130);
      __size = 0x47;
      goto LAB_0011e86c;
    }
    puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
    if (*(char *)((long)puVar11 + 4) != '\0') {
      uVar22 = *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8);
      uVar21 = *(undefined8 *)(local_6c0 + uVar18 * 0x10);
LAB_0011e6b4:
      puVar11[1] = uVar22;
      *puVar11 = uVar21;
    }
    goto code_r0x0011e6bc;
  case 7:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    if (0x5f < uVar18) {
      __ptr = "libunwind: malformed DW_CFA_undefined DWARF unwind, reg too big\n";
LAB_0011e860:
      __s = (FILE *)(PTR___sF_001250b0 + 0x130);
      __size = 0x40;
      goto LAB_0011e86c;
    }
    puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
    if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
      uVar21 = *puVar11;
      *(undefined1 *)((long)puVar11 + 4) = 1;
      *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
      *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
    }
    *(undefined4 *)puVar11 = 1;
    pbVar13 = local_a0;
    break;
  case 8:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    if (0x5f < uVar18) {
      __ptr = "libunwind: malformed DW_CFA_same_value DWARF unwind, reg too big\n";
LAB_0011e844:
      __s = (FILE *)(PTR___sF_001250b0 + 0x130);
      __size = 0x41;
      goto LAB_0011e86c;
    }
    puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
    if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
      uVar21 = *puVar11;
      *(undefined1 *)((long)puVar11 + 4) = 1;
      *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
      *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
    }
    *(undefined4 *)puVar11 = 0;
    pbVar13 = local_a0;
    break;
  case 9:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    uVar15 = FUN_0011f04c(&local_a0,pbVar2);
    if (0x5f < uVar18) {
      __ptr = "libunwind: malformed DW_CFA_register DWARF unwind, reg too big\n";
      __size = 0x3f;
      __s = (FILE *)(PTR___sF_001250b0 + 0x130);
      goto LAB_0011e86c;
    }
    if (0x5f < uVar15) {
      __ptr = "libunwind: malformed DW_CFA_register DWARF unwind, reg2 too big\n";
      goto LAB_0011e860;
    }
    puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
    if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
      uVar21 = *puVar11;
      *(undefined1 *)((long)puVar11 + 4) = 1;
      *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
      *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
    }
    puVar11[1] = uVar15;
    *(undefined4 *)puVar11 = 5;
    pbVar13 = local_a0;
    break;
  case 10:
    puVar11 = (undefined8 *)((long)puVar5 + -0x620);
    *puVar11 = local_6c8;
    memcpy((undefined1 *)((long)puVar5 + -0x618),param_6,0x618);
    local_6c8 = puVar11;
    puVar5 = puVar11;
    pbVar13 = local_a0;
    break;
  case 0xb:
    if (local_6c8 == (undefined8 *)0x0) {
      return 0;
    }
    memcpy(param_6,local_6c8 + 1,0x618);
    local_6c8 = (undefined8 *)*puVar11;
    pbVar13 = local_a0;
    break;
  case 0xc:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    uVar6 = FUN_0011f04c(&local_a0,pbVar2);
    if (0x5f < uVar18) {
      __ptr = "libunwind: malformed DW_CFA_def_cfa DWARF unwind, reg too big\n";
      __size = 0x3e;
      __s = (FILE *)(PTR___sF_001250b0 + 0x130);
      goto LAB_0011e86c;
    }
    *param_6 = (int)uVar18;
    param_6[1] = uVar6;
    pbVar13 = local_a0;
    break;
  case 0xd:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    if (0x5f < uVar18) {
      __ptr = "libunwind: malformed DW_CFA_def_cfa_register DWARF unwind, reg too big\n";
      goto LAB_0011e75c;
    }
    *param_6 = (int)uVar18;
    pbVar13 = local_a0;
    break;
  case 0xe:
    uVar6 = FUN_0011f04c(&local_a0,pbVar2);
    param_6[1] = uVar6;
    pbVar13 = local_a0;
    break;
  case 0xf:
    *param_6 = 0;
    *(byte **)(param_6 + 2) = pbVar9;
    goto LAB_0011e60c;
  case 0x10:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    if (0x5f < uVar18) {
      __ptr = "libunwind: malformed DW_CFA_expression DWARF unwind, reg too big\n";
      goto LAB_0011e844;
    }
    puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
    if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
      uVar21 = *puVar11;
      *(undefined1 *)((long)puVar11 + 4) = 1;
      *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
      *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
    }
    uVar6 = 6;
LAB_0011e604:
    *(undefined4 *)puVar11 = uVar6;
    puVar11[1] = local_a0;
LAB_0011e60c:
    lVar7 = FUN_0011f04c(&local_a0,pbVar2);
    local_a0 = local_a0 + lVar7;
    pbVar13 = local_a0;
    break;
  case 0x11:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    puVar4 = PTR___sF_001250b0;
    if (0x5f < uVar18) {
      __ptr = "libunwind: malformed DW_CFA_offset_extended_sf DWARF unwind, reg too big\n";
      __size = 0x49;
      __s = (FILE *)(PTR___sF_001250b0 + 0x130);
      goto LAB_0011e86c;
    }
    uVar15 = 0;
    uVar10 = 0;
    pbVar13 = local_a0;
    pbVar9 = local_a0;
    do {
      if (pbVar9 == pbVar2) {
        fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","getSLEB128",
                "truncated sleb128 expression");
        fflush((FILE *)(puVar4 + 0x130));
                    /* WARNING: Subroutine does not return */
        abort();
      }
      bVar3 = *pbVar9;
      pbVar13 = pbVar13 + 1;
      uVar16 = uVar15 & 0x3f;
      uVar15 = uVar15 + 7;
      uVar10 = ((ulong)bVar3 & 0x7f) << uVar16 | uVar10;
      pbVar9 = pbVar9 + 1;
    } while ((char)bVar3 < '\0');
    puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
    local_a0 = pbVar13;
    uVar16 = -1L << (uVar15 & 0x3f);
    iVar14 = *(int *)((long)param_3 + 0x2c);
    if (0x38 < (int)uVar15 - 7U || bVar3 < 0x40) {
      uVar16 = 0;
    }
    if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
      uVar21 = *puVar11;
      *(undefined1 *)((long)puVar11 + 4) = 1;
      *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
      *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
    }
    uVar10 = uVar10 | uVar16;
    uVar6 = 2;
LAB_0011e5a8:
    *(undefined4 *)puVar11 = uVar6;
    puVar11[1] = uVar10 * (long)iVar14;
    pbVar13 = local_a0;
    break;
  case 0x12:
    uVar15 = FUN_0011f04c(&local_a0,pbVar2);
    puVar4 = PTR___sF_001250b0;
    uVar18 = 0;
    uVar10 = 0;
    pbVar13 = local_a0;
    pbVar9 = local_a0;
    do {
      if (pbVar9 == pbVar2) {
        fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","getSLEB128",
                "truncated sleb128 expression");
        fflush((FILE *)(puVar4 + 0x130));
                    /* WARNING: Subroutine does not return */
        abort();
      }
      bVar3 = *pbVar9;
      pbVar13 = pbVar13 + 1;
      uVar16 = uVar18 & 0x3f;
      uVar18 = uVar18 + 7;
      uVar10 = ((ulong)bVar3 & 0x7f) << uVar16 | uVar10;
      pbVar9 = pbVar9 + 1;
    } while ((char)bVar3 < '\0');
    local_a0 = pbVar13;
    if (0x5f < uVar15) {
      __ptr = "libunwind: malformed DW_CFA_def_cfa_sf DWARF unwind, reg too big\n";
      goto LAB_0011e844;
    }
    iVar14 = *(int *)((long)param_3 + 0x2c);
    uVar8 = (uint)(-1L << (uVar18 & 0x3f));
    if (0x38 < (int)uVar18 - 7U || bVar3 < 0x40) {
      uVar8 = 0;
    }
    *param_6 = (int)uVar15;
    param_6[1] = iVar14 * ((uint)uVar10 | uVar8);
    break;
  case 0x13:
    uVar18 = 0;
    uVar15 = 0;
    pbVar13 = pbVar9;
    do {
      if (pbVar13 == pbVar2) {
        fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","getSLEB128",
                "truncated sleb128 expression");
        fflush((FILE *)(puVar4 + 0x130));
                    /* WARNING: Subroutine does not return */
        abort();
      }
      bVar3 = *pbVar13;
      pbVar9 = pbVar9 + 1;
      uVar10 = uVar18 & 0x3f;
      uVar18 = uVar18 + 7;
      uVar15 = ((ulong)bVar3 & 0x7f) << uVar10 | uVar15;
      pbVar13 = pbVar13 + 1;
    } while ((char)bVar3 < '\0');
    local_a0 = pbVar9;
    uVar8 = (uint)(-1L << (uVar18 & 0x3f));
    if (0x38 < (int)uVar18 - 7U || bVar3 < 0x40) {
      uVar8 = 0;
    }
    param_6[1] = *(int *)((long)param_3 + 0x2c) * ((uint)uVar15 | uVar8);
    pbVar13 = pbVar9;
    break;
  case 0x14:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    if (0x5f < uVar18) {
      fprintf((FILE *)(PTR___sF_001250b0 + 0x130),
              "libunwind: malformed DW_CFA_val_offset DWARF unwind, reg (%lu) out of range\n\n",
              uVar18);
      goto LAB_0011e874;
    }
    lVar7 = FUN_0011f04c(&local_a0,pbVar2);
    puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
    iVar14 = *(int *)((long)param_3 + 0x2c);
    if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
      uVar21 = *puVar11;
      *(undefined1 *)((long)puVar11 + 4) = 1;
      *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
      *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
    }
    *(undefined4 *)puVar11 = 4;
    puVar11[1] = lVar7 * iVar14;
    pbVar13 = local_a0;
    break;
  case 0x15:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    puVar4 = PTR___sF_001250b0;
    if (uVar18 < 0x60) {
      uVar15 = 0;
      uVar10 = 0;
      pbVar13 = local_a0;
      pbVar9 = local_a0;
      do {
        if (pbVar9 == pbVar2) {
          fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","getSLEB128",
                  "truncated sleb128 expression");
          fflush((FILE *)(puVar4 + 0x130));
                    /* WARNING: Subroutine does not return */
          abort();
        }
        bVar3 = *pbVar9;
        pbVar13 = pbVar13 + 1;
        uVar16 = uVar15 & 0x3f;
        uVar15 = uVar15 + 7;
        uVar10 = ((ulong)bVar3 & 0x7f) << uVar16 | uVar10;
        pbVar9 = pbVar9 + 1;
      } while ((char)bVar3 < '\0');
      puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
      local_a0 = pbVar13;
      uVar16 = -1L << (uVar15 & 0x3f);
      iVar14 = *(int *)((long)param_3 + 0x2c);
      if (0x38 < (int)uVar15 - 7U || bVar3 < 0x40) {
        uVar16 = 0;
      }
      if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
        uVar21 = *puVar11;
        *(undefined1 *)((long)puVar11 + 4) = 1;
        *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
        *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
      }
      uVar10 = uVar10 | uVar16;
      uVar6 = 4;
      goto LAB_0011e5a8;
    }
    __ptr = "libunwind: malformed DW_CFA_val_offset_sf DWARF unwind, reg too big\n";
    __size = 0x44;
    __s = (FILE *)(PTR___sF_001250b0 + 0x130);
    goto LAB_0011e86c;
  case 0x16:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    if (uVar18 < 0x60) {
      puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
      if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
        uVar21 = *puVar11;
        *(undefined1 *)((long)puVar11 + 4) = 1;
        *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
        *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
      }
      uVar6 = 7;
      goto LAB_0011e604;
    }
    __ptr = "libunwind: malformed DW_CFA_val_expression DWARF unwind, reg too big\n";
    __size = 0x45;
    __s = (FILE *)(PTR___sF_001250b0 + 0x130);
    goto LAB_0011e86c;
  default:
    bVar1 = bVar3 & 0xc0;
    uVar18 = (ulong)bVar3 & 0x3f;
    if (bVar1 != 0x40) {
      if (bVar1 != 0xc0) {
        if (bVar1 != 0x80) {
          return 0;
        }
        lVar7 = FUN_0011f04c(&local_a0,pbVar2);
        puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
        lVar12 = (long)*(int *)((long)param_3 + 0x2c);
        if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
          uVar21 = *puVar11;
          *(undefined1 *)((long)puVar11 + 4) = 1;
          *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
          *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
        }
        goto LAB_0011e4a4;
      }
      puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
      if (*(char *)((long)puVar11 + 4) != '\0') {
        uVar22 = *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8);
        uVar21 = *(undefined8 *)(local_6c0 + uVar18 * 0x10);
        goto LAB_0011e6b4;
      }
      goto code_r0x0011e6bc;
    }
    uVar17 = uVar17 + (uint)((int)param_3[5] * (int)uVar18);
    pbVar13 = pbVar9;
    break;
  case 0x2d:
    if (local_6cc != 4) goto code_r0x0011e6bc;
    if ((*(byte *)(param_6 + 0x8f) & 1) == 0) {
      uVar22 = puStack_6e0[1];
      uVar21 = *puStack_6e0;
      *(undefined1 *)(param_6 + 0x8f) = 1;
      local_6e8[1] = uVar22;
      *local_6e8 = uVar21;
    }
    *(ulong *)(param_6 + 0x90) = *(ulong *)(param_6 + 0x90) ^ 1;
    pbVar13 = local_a0;
    break;
  case 0x2e:
    uVar6 = FUN_0011f04c(&local_a0,pbVar2);
    param_6[4] = uVar6;
    pbVar13 = local_a0;
    break;
  case 0x2f:
    uVar18 = FUN_0011f04c(&local_a0,pbVar2);
    if (uVar18 < 0x60) {
      lVar7 = FUN_0011f04c(&local_a0,pbVar2);
      puVar11 = (undefined8 *)(param_6 + uVar18 * 4 + 6);
      iVar14 = *(int *)((long)param_3 + 0x2c);
      if ((*(byte *)((long)puVar11 + 4) & 1) == 0) {
        uVar21 = *puVar11;
        *(undefined1 *)((long)puVar11 + 4) = 1;
        *(undefined8 *)((long)(local_6c0 + uVar18 * 0x10) + 8) = puVar11[1];
        *(undefined8 *)(local_6c0 + uVar18 * 0x10) = uVar21;
      }
      lVar7 = -(lVar7 * iVar14);
      goto LAB_0011e4a8;
    }
    __ptr = "libunwind: malformed DW_CFA_GNU_negative_offset_extended DWARF unwind, reg too big\n";
    __size = 0x53;
    __s = (FILE *)(PTR___sF_001250b0 + 0x130);
LAB_0011e86c:
    fwrite(__ptr,__size,1,__s);
LAB_0011e874:
    fflush((FILE *)(PTR___sF_001250b0 + 0x130));
    return 0;
  }
joined_r0x0011e448:
  if ((pbVar2 <= pbVar13) || (uVar20 <= uVar17)) goto LAB_0011de94;
  goto LAB_0011dec4;
code_r0x0011e6bc:
  pbVar13 = local_a0;
  goto joined_r0x0011e448;
}

