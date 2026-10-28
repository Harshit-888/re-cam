
ulong FUN_0011f214(byte *param_1,undefined8 param_2,long param_3,ulong param_4)

{
  ulong *puVar1;
  ulong *puVar2;
  ulong *puVar3;
  ulong *puVar4;
  ulong *puVar5;
  byte *pbVar6;
  byte bVar7;
  byte bVar8;
  undefined *puVar9;
  uint uVar10;
  long lVar11;
  ulong uVar12;
  ulong uVar13;
  byte *pbVar14;
  ulong *puVar15;
  uint uVar16;
  ulong uVar17;
  ulong uVar18;
  byte *pbVar19;
  ulong *puVar20;
  undefined1 auVar21 [16];
  ulong local_380 [99];
  byte *local_68;
  
  local_68 = param_1;
  lVar11 = FUN_0011f04c(&local_68,param_1 + 0x14);
  local_380[0] = param_4;
  pbVar6 = local_68 + lVar11;
  if (local_68 < pbVar6) {
    puVar1 = (ulong *)(param_3 + 0xf0);
    puVar2 = (ulong *)(param_3 + 0xe8);
    puVar3 = (ulong *)(param_3 + 0x108);
    puVar4 = (ulong *)(param_3 + 0xf8);
    puVar20 = local_380;
    puVar5 = (ulong *)(param_3 + 0x100);
    pbVar19 = local_68;
    do {
      puVar9 = PTR___sF_001250b0;
      pbVar14 = pbVar19 + 1;
      local_68 = pbVar14;
      bVar8 = *pbVar19;
      uVar10 = (uint)bVar8;
      if (0x91 < uVar10 - 3) {
code_r0x0011fa98:
        fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","evaluateExpression",
                "DWARF opcode not implemented");
        fflush((FILE *)(puVar9 + 0x130));
                    /* WARNING: Subroutine does not return */
        abort();
      }
      uVar16 = (uint)bVar8;
      puVar15 = puVar4;
      switch(uVar10) {
      case 3:
      case 0xe:
      case 0xf:
        uVar17 = *(ulong *)(pbVar19 + 1);
        local_68 = pbVar19 + 9;
        goto LAB_0011f6fc;
      case 4:
      case 5:
      case 7:
        goto code_r0x0011fa98;
      case 6:
        puVar15 = (ulong *)*puVar20;
LAB_0011f68c:
        *puVar20 = *puVar15;
        goto LAB_0011f298;
      case 8:
        uVar17 = (ulong)pbVar19[1];
        goto LAB_0011f660;
      case 9:
        uVar17 = (ulong)(char)pbVar19[1];
LAB_0011f660:
        local_68 = pbVar19 + 2;
        goto LAB_0011f6fc;
      case 10:
        uVar17 = (ulong)*(ushort *)(pbVar19 + 1);
        local_68 = pbVar19 + 3;
        goto LAB_0011f6fc;
      case 0xb:
        uVar17 = (ulong)*(short *)(pbVar19 + 1);
        local_68 = pbVar19 + 3;
        goto LAB_0011f6fc;
      case 0xc:
        uVar17 = (ulong)*(uint *)(pbVar19 + 1);
        local_68 = pbVar19 + 5;
        goto LAB_0011f6fc;
      case 0xd:
        uVar17 = (ulong)*(int *)(pbVar19 + 1);
        local_68 = pbVar19 + 5;
        goto LAB_0011f6fc;
      case 0x10:
        uVar17 = FUN_0011f04c(&local_68,pbVar6);
        puVar20 = puVar20 + 1;
        *puVar20 = uVar17;
        goto LAB_0011f298;
      case 0x11:
        uVar17 = 0;
        uVar12 = 0;
        pbVar19 = pbVar14;
        do {
          if (pbVar19 == pbVar6) {
            fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","getSLEB128",
                    "truncated sleb128 expression");
            fflush((FILE *)(puVar9 + 0x130));
                    /* WARNING: Subroutine does not return */
            abort();
          }
          bVar8 = *pbVar19;
          pbVar14 = pbVar14 + 1;
          uVar13 = uVar17 & 0x3f;
          uVar17 = uVar17 + 7;
          uVar12 = ((ulong)bVar8 & 0x7f) << uVar13 | uVar12;
          pbVar19 = pbVar19 + 1;
        } while ((char)bVar8 < '\0');
        local_68 = pbVar14;
        uVar13 = -1L << (uVar17 & 0x3f);
        if (0x38 < (int)uVar17 - 7U || bVar8 < 0x40) {
          uVar13 = 0;
        }
        puVar20 = puVar20 + 1;
        *puVar20 = uVar12 | uVar13;
        goto LAB_0011f298;
      case 0x12:
        uVar17 = *puVar20;
        goto LAB_0011f294;
      case 0x13:
        puVar20 = puVar20 + -1;
        goto LAB_0011f298;
      case 0x14:
        uVar17 = puVar20[-1];
        goto LAB_0011f294;
      case 0x15:
        local_68 = pbVar19 + 2;
        uVar17 = puVar20[-(ulong)pbVar19[1]];
LAB_0011f6fc:
LAB_0011f294:
        puVar20[1] = uVar17;
        puVar20 = puVar20 + 1;
        goto LAB_0011f298;
      case 0x16:
        auVar21 = NEON_ext(*(undefined1 (*) [16])(puVar20 + -1),*(undefined1 (*) [16])(puVar20 + -1)
                           ,8,1);
        *puVar20 = auVar21._8_8_;
        puVar20[-1] = auVar21._0_8_;
        goto LAB_0011f298;
      case 0x17:
        uVar17 = *puVar20;
        *puVar20 = SUB168(*(undefined1 (*) [16])(puVar20 + -2),8);
        puVar20[-1] = SUB168(*(undefined1 (*) [16])(puVar20 + -2),0);
        puVar20[-2] = uVar17;
        goto LAB_0011f298;
      case 0x18:
        puVar15 = (ulong *)*puVar20;
        puVar20 = puVar20 + -1;
        *puVar20 = *puVar15;
        goto LAB_0011f298;
      case 0x19:
        uVar17 = *puVar20;
        if ((long)uVar17 < 0) goto LAB_0011f834;
        goto LAB_0011f298;
      case 0x1a:
        uVar17 = puVar20[-1] & *puVar20;
        break;
      case 0x1b:
        uVar17 = 0;
        if (*puVar20 != 0) {
          uVar17 = (long)puVar20[-1] / (long)*puVar20;
        }
        break;
      case 0x1c:
        uVar17 = puVar20[-1] - *puVar20;
        break;
      case 0x1d:
        uVar17 = *puVar20;
        lVar11 = 0;
        if (uVar17 != 0) {
          lVar11 = (long)puVar20[-1] / (long)uVar17;
        }
        uVar17 = puVar20[-1] - lVar11 * uVar17;
        break;
      case 0x1e:
        uVar17 = puVar20[-1] * *puVar20;
        break;
      case 0x1f:
        uVar17 = *puVar20;
LAB_0011f834:
        *puVar20 = -uVar17;
        goto LAB_0011f298;
      case 0x20:
        *puVar20 = ~*puVar20;
        goto LAB_0011f298;
      case 0x21:
        uVar17 = puVar20[-1] | *puVar20;
        break;
      case 0x22:
        uVar17 = puVar20[-1] + *puVar20;
        break;
      case 0x23:
        lVar11 = FUN_0011f04c(&local_68,pbVar6);
        *puVar20 = *puVar20 + lVar11;
        goto LAB_0011f298;
      case 0x24:
        uVar17 = puVar20[-1] << (*puVar20 & 0x3f);
        break;
      case 0x25:
        uVar17 = puVar20[-1] >> (*puVar20 & 0x3f);
        break;
      case 0x26:
        uVar17 = (long)puVar20[-1] >> (*puVar20 & 0x3f);
        break;
      case 0x27:
        uVar17 = puVar20[-1] ^ *puVar20;
        break;
      case 0x28:
        puVar15 = puVar20 + -1;
        uVar17 = *puVar20;
        local_68 = pbVar19 + 3;
        puVar20 = puVar15;
        if (uVar17 != 0) {
          local_68 = pbVar19 + 3 + *(short *)(pbVar19 + 1);
        }
        goto LAB_0011f298;
      case 0x29:
        uVar17 = (ulong)(puVar20[-1] == *puVar20);
        break;
      case 0x2a:
        uVar17 = (ulong)(*puVar20 <= puVar20[-1]);
        break;
      case 0x2b:
        uVar17 = (ulong)(*puVar20 < puVar20[-1]);
        break;
      case 0x2c:
        uVar17 = (ulong)(puVar20[-1] <= *puVar20);
        break;
      case 0x2d:
        uVar17 = (ulong)(puVar20[-1] < *puVar20);
        break;
      case 0x2e:
        uVar17 = (ulong)(puVar20[-1] != *puVar20);
        break;
      case 0x2f:
        local_68 = pbVar19 + (long)*(short *)(pbVar19 + 1) + 3;
        goto LAB_0011f298;
      default:
        uVar17 = (ulong)(uVar16 - 0x30);
        goto LAB_0011f294;
      case 0x50:
      case 0x51:
      case 0x52:
      case 0x53:
      case 0x54:
      case 0x55:
      case 0x56:
      case 0x57:
      case 0x58:
      case 0x59:
      case 0x5a:
      case 0x5b:
      case 0x5c:
      case 0x5d:
      case 0x5e:
      case 0x5f:
      case 0x60:
      case 0x61:
      case 0x62:
      case 99:
      case 100:
      case 0x65:
      case 0x66:
      case 0x67:
      case 0x68:
      case 0x69:
      case 0x6a:
      case 0x6b:
      case 0x6c:
      case 0x6d:
      case 0x6e:
      case 0x6f:
        if (((uVar16 != 0x6f) && (puVar15 = puVar1, bVar8 != 0x6e)) &&
           (puVar15 = puVar2, bVar8 != 0x6d)) {
          puVar15 = (ulong *)(param_3 + (ulong)(uVar16 - 0x50) * 8);
        }
        goto LAB_0011f290;
      case 0x70:
      case 0x71:
      case 0x72:
      case 0x73:
      case 0x74:
      case 0x75:
      case 0x76:
      case 0x77:
      case 0x78:
      case 0x79:
      case 0x7a:
      case 0x7b:
      case 0x7c:
      case 0x7d:
      case 0x7e:
      case 0x7f:
      case 0x80:
      case 0x81:
      case 0x82:
      case 0x83:
      case 0x84:
      case 0x85:
      case 0x86:
      case 0x87:
      case 0x88:
      case 0x89:
      case 0x8a:
      case 0x8b:
      case 0x8c:
      case 0x8d:
      case 0x8e:
      case 0x8f:
        uVar17 = 0;
        uVar12 = 0;
        pbVar19 = pbVar14;
        do {
          if (pbVar19 == pbVar6) {
            fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","getSLEB128",
                    "truncated sleb128 expression");
            fflush((FILE *)(puVar9 + 0x130));
                    /* WARNING: Subroutine does not return */
            abort();
          }
          bVar7 = *pbVar19;
          pbVar14 = pbVar14 + 1;
          uVar13 = uVar17 & 0x3f;
          uVar17 = uVar17 + 7;
          uVar12 = ((ulong)bVar7 & 0x7f) << uVar13 | uVar12;
          pbVar19 = pbVar19 + 1;
        } while ((char)bVar7 < '\0');
        local_68 = pbVar14;
        uVar13 = -1L << (uVar17 & 0x3f);
        if (0x38 < (int)uVar17 - 7U || bVar7 < 0x40) {
          uVar13 = 0;
        }
        if (bVar8 < 0x8e) {
          if (((bVar8 != 0x6e) && (puVar15 = puVar5, bVar8 != 0x6f)) &&
             (puVar15 = puVar2, bVar8 != 0x8d)) goto LAB_0011f3d0;
        }
        else if (bVar8 < 0x90) {
          puVar15 = puVar1;
          if ((bVar8 != 0x8e) && (puVar15 = puVar4, uVar16 != 0x8f)) {
LAB_0011f3d0:
            if (0x1c < uVar10 - 0x70) {
              fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","getRegister",
                      "unsupported arm64 register");
              fflush((FILE *)(puVar9 + 0x130));
                    /* WARNING: Subroutine does not return */
              abort();
            }
            puVar15 = (ulong *)(param_3 + (ulong)(uVar10 - 0x70) * 8);
          }
        }
        else {
          puVar15 = puVar5;
          if ((bVar8 != 0x90) && (puVar15 = puVar3, bVar8 != 0x92)) goto LAB_0011f3d0;
        }
        uVar17 = *puVar15 + (uVar12 | uVar13);
        goto LAB_0011f294;
      case 0x90:
        uVar17 = FUN_0011f04c(&local_68,pbVar6);
        puVar9 = PTR___sF_001250b0;
        uVar10 = (uint)uVar17;
        if ((int)uVar10 < 0x1e) {
          if (((uVar10 != 0xfffffffe) && (puVar15 = puVar5, uVar10 != 0xffffffff)) &&
             (puVar15 = puVar2, uVar10 != 0x1d)) goto LAB_0011f934;
        }
        else if ((int)uVar10 < 0x20) {
          puVar15 = puVar1;
          if ((uVar10 != 0x1e) && (puVar15 = puVar4, uVar10 != 0x1f)) {
LAB_0011f934:
            if (0x1c < uVar10) {
              fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","getRegister",
                      "unsupported arm64 register");
              fflush((FILE *)(puVar9 + 0x130));
                    /* WARNING: Subroutine does not return */
              abort();
            }
            puVar15 = (ulong *)(param_3 + (uVar17 & 0x1f) * 8);
          }
        }
        else {
          puVar15 = puVar5;
          if ((uVar10 != 0x20) && (puVar15 = puVar3, uVar10 != 0x22)) goto LAB_0011f934;
        }
LAB_0011f290:
        uVar17 = *puVar15;
        goto LAB_0011f294;
      case 0x91:
        fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","evaluateExpression",
                "DW_OP_fbreg not implemented");
        fflush((FILE *)(puVar9 + 0x130));
                    /* WARNING: Subroutine does not return */
        abort();
      case 0x92:
        uVar12 = FUN_0011f04c(&local_68,pbVar6);
        puVar9 = PTR___sF_001250b0;
        uVar17 = 0;
        uVar13 = 0;
        pbVar19 = local_68;
        pbVar14 = local_68;
        do {
          if (pbVar14 == pbVar6) {
            fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","getSLEB128",
                    "truncated sleb128 expression");
            fflush((FILE *)(puVar9 + 0x130));
                    /* WARNING: Subroutine does not return */
            abort();
          }
          bVar8 = *pbVar14;
          pbVar19 = pbVar19 + 1;
          uVar18 = uVar17 & 0x3f;
          uVar17 = uVar17 + 7;
          uVar13 = ((ulong)bVar8 & 0x7f) << uVar18 | uVar13;
          pbVar14 = pbVar14 + 1;
        } while ((char)bVar8 < '\0');
        local_68 = pbVar19;
        uVar18 = -1L << (uVar17 & 0x3f);
        if (0x38 < (int)uVar17 - 7U || bVar8 < 0x40) {
          uVar18 = 0;
        }
        uVar10 = (uint)uVar12;
        if ((int)uVar10 < 0x1e) {
          if (((uVar10 != 0xfffffffe) && (puVar15 = puVar5, uVar10 != 0xffffffff)) &&
             (puVar15 = puVar2, uVar10 != 0x1d)) goto LAB_0011f960;
        }
        else if ((int)uVar10 < 0x20) {
          puVar15 = puVar1;
          if ((uVar10 != 0x1e) && (puVar15 = puVar4, uVar10 != 0x1f)) {
LAB_0011f960:
            if (0x1c < uVar10) {
              fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","getRegister",
                      "unsupported arm64 register");
              fflush((FILE *)(puVar9 + 0x130));
                    /* WARNING: Subroutine does not return */
              abort();
            }
            puVar15 = (ulong *)(param_3 + (uVar12 & 0x1f) * 8);
          }
        }
        else {
          puVar15 = puVar5;
          if ((uVar10 != 0x20) && (puVar15 = puVar3, uVar10 != 0x22)) goto LAB_0011f960;
        }
        uVar17 = *puVar15 + (uVar13 | uVar18);
        goto LAB_0011f294;
      case 0x93:
        fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","evaluateExpression",
                "DW_OP_piece not implemented");
        fflush((FILE *)(puVar9 + 0x130));
                    /* WARNING: Subroutine does not return */
        abort();
      case 0x94:
        local_68 = pbVar19 + 2;
        puVar15 = (ulong *)*puVar20;
        bVar8 = pbVar19[1];
        if (bVar8 < 4) {
          if (bVar8 == 1) {
            *puVar20 = (ulong)(byte)*puVar15;
          }
          else {
            if (bVar8 != 2) {
LAB_0011fb3c:
              fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n",
                      "evaluateExpression","DW_OP_deref_size with bad size");
              fflush((FILE *)(puVar9 + 0x130));
                    /* WARNING: Subroutine does not return */
              abort();
            }
            *puVar20 = (ulong)(ushort)*puVar15;
          }
        }
        else {
          if (bVar8 != 4) {
            if (bVar8 != 8) goto LAB_0011fb3c;
            goto LAB_0011f68c;
          }
          *puVar20 = (ulong)(uint)*puVar15;
        }
        goto LAB_0011f298;
      }
      puVar20[-1] = uVar17;
      puVar20 = puVar20 + -1;
LAB_0011f298:
      pbVar19 = local_68;
    } while (local_68 < pbVar6);
    param_4 = *puVar20;
  }
  return param_4;
}

