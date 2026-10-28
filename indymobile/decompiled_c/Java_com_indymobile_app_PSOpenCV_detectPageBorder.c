
/* WARNING: Type propagation algorithm not settling */

undefined8
Java_com_indymobile_app_PSOpenCV_detectPageBorder
          (float param_1,_JNIEnv *param_2,undefined8 param_3,_jobject *param_4,Mat *param_5,
          int param_6,int param_7)

{
  int iVar1;
  long lVar2;
  bool bVar3;
  uint uVar4;
  ulong uVar5;
  undefined8 *puVar6;
  undefined8 *puVar7;
  undefined8 *puVar8;
  undefined8 uVar9;
  undefined8 uVar10;
  undefined8 uVar11;
  undefined8 uVar12;
  undefined8 uVar13;
  undefined8 **ppuVar14;
  float *pfVar15;
  long lVar16;
  ulong uVar17;
  long lVar18;
  ulong uVar19;
  undefined8 *puVar20;
  int iVar21;
  size_t sVar22;
  void *pvVar23;
  void *pvVar24;
  undefined8 *puVar25;
  undefined8 uVar26;
  undefined8 uVar27;
  undefined8 uVar28;
  undefined8 uVar29;
  undefined8 uVar30;
  undefined8 uVar31;
  undefined8 local_1a8;
  undefined8 uStack_1a0;
  undefined8 local_198;
  undefined8 *local_190;
  undefined8 *local_188;
  undefined8 *local_180;
  undefined8 *local_178;
  undefined8 *local_170;
  undefined8 *local_168;
  undefined8 *local_160;
  undefined8 *local_158;
  undefined8 *local_150;
  undefined8 *local_148;
  undefined8 *local_140;
  undefined8 *local_138;
  undefined8 *local_130;
  undefined8 *local_128;
  undefined8 *local_120;
  float *local_118;
  float *local_110;
  undefined8 local_108;
  float *local_100;
  float *local_f8;
  undefined8 local_f0;
  float *local_e8;
  float *local_e0;
  undefined8 local_d8;
  float *local_d0;
  float *local_c8;
  undefined8 local_c0;
  void *local_b8;
  void *local_b0;
  undefined8 local_a8;
  float local_a0;
  undefined4 local_9c;
  float local_98;
  undefined4 local_94;
  undefined8 local_90;
  undefined8 local_88;
  long local_80;
  
  lVar2 = tpidr_el0;
  local_80 = *(long *)(lVar2 + 0x28);
  uVar5 = OpenCVUtil::checkAuthentication(param_2,param_4);
  if ((uVar5 & 1) == 0) {
    uVar26 = 0;
  }
  else {
    local_b8 = (void *)0x0;
    local_b0 = (void *)0x0;
    local_a8 = 0;
    local_d0 = (float *)0x0;
    local_c8 = (float *)0x0;
    local_c0 = 0;
    local_e8 = (float *)0x0;
    local_e0 = (float *)0x0;
    local_d8 = 0;
    local_100 = (float *)0x0;
    local_f8 = (float *)0x0;
    local_f0 = 0;
    local_118 = (float *)0x0;
    local_110 = (float *)0x0;
    local_108 = 0;
    uVar4 = OpenCVUtil::findContoursRGB(param_5,(vector *)&local_b8);
    OpenCVUtil::findHoughLines
              (param_5,(vector *)&local_d0,(vector *)&local_100,(vector *)&local_118,
               (vector *)&local_e8);
    if ((long)local_c8 - (long)local_d0 != 0) {
      uVar17 = (long)local_c8 - (long)local_d0 >> 3;
      uVar5 = uVar17;
      if (uVar17 < 2) {
        uVar5 = 1;
      }
      if (uVar17 < 5) {
        lVar18 = 0;
      }
      else {
        uVar17 = 4;
        if ((uVar5 & 3) != 0) {
          uVar17 = uVar5 & 3;
        }
        lVar18 = uVar5 - uVar17;
        lVar16 = lVar18;
        pfVar15 = local_d0;
        do {
          lVar16 = lVar16 + -4;
          *pfVar15 = *pfVar15 / param_1;
          pfVar15[6] = pfVar15[6] / param_1;
          pfVar15[2] = pfVar15[2] / param_1;
          pfVar15[4] = pfVar15[4] / param_1;
          pfVar15 = pfVar15 + 8;
        } while (lVar16 != 0);
      }
      lVar16 = uVar5 - lVar18;
      pfVar15 = local_d0 + lVar18 * 2;
      do {
        lVar16 = lVar16 + -1;
        *pfVar15 = *pfVar15 / param_1;
        pfVar15 = pfVar15 + 2;
      } while (lVar16 != 0);
    }
    if ((long)local_e0 - (long)local_e8 != 0) {
      uVar17 = (long)local_e0 - (long)local_e8 >> 3;
      uVar5 = uVar17;
      if (uVar17 < 2) {
        uVar5 = 1;
      }
      if (uVar17 < 5) {
        lVar18 = 0;
      }
      else {
        uVar17 = 4;
        if ((uVar5 & 3) != 0) {
          uVar17 = uVar5 & 3;
        }
        lVar18 = uVar5 - uVar17;
        lVar16 = lVar18;
        pfVar15 = local_e8;
        do {
          lVar16 = lVar16 + -4;
          *pfVar15 = *pfVar15 / param_1;
          pfVar15[6] = pfVar15[6] / param_1;
          pfVar15[2] = pfVar15[2] / param_1;
          pfVar15[4] = pfVar15[4] / param_1;
          pfVar15 = pfVar15 + 8;
        } while (lVar16 != 0);
      }
      lVar16 = uVar5 - lVar18;
      pfVar15 = local_e8 + lVar18 * 2;
      do {
        lVar16 = lVar16 + -1;
        *pfVar15 = *pfVar15 / param_1;
        pfVar15 = pfVar15 + 2;
      } while (lVar16 != 0);
    }
    if ((long)local_f8 - (long)local_100 != 0) {
      uVar17 = (long)local_f8 - (long)local_100 >> 3;
      uVar5 = uVar17;
      if (uVar17 < 2) {
        uVar5 = 1;
      }
      if (uVar17 < 5) {
        lVar18 = 0;
      }
      else {
        uVar17 = 4;
        if ((uVar5 & 3) != 0) {
          uVar17 = uVar5 & 3;
        }
        lVar18 = uVar5 - uVar17;
        lVar16 = lVar18;
        pfVar15 = local_100;
        do {
          lVar16 = lVar16 + -4;
          *pfVar15 = *pfVar15 / param_1;
          pfVar15[6] = pfVar15[6] / param_1;
          pfVar15[2] = pfVar15[2] / param_1;
          pfVar15[4] = pfVar15[4] / param_1;
          pfVar15 = pfVar15 + 8;
        } while (lVar16 != 0);
      }
      lVar16 = uVar5 - lVar18;
      pfVar15 = local_100 + lVar18 * 2;
      do {
        lVar16 = lVar16 + -1;
        *pfVar15 = *pfVar15 / param_1;
        pfVar15 = pfVar15 + 2;
      } while (lVar16 != 0);
    }
    if ((long)local_110 - (long)local_118 != 0) {
      uVar17 = (long)local_110 - (long)local_118 >> 3;
      uVar5 = uVar17;
      if (uVar17 < 2) {
        uVar5 = 1;
      }
      if (uVar17 < 5) {
        lVar18 = 0;
      }
      else {
        uVar17 = 4;
        if ((uVar5 & 3) != 0) {
          uVar17 = uVar5 & 3;
        }
        lVar18 = uVar5 - uVar17;
        lVar16 = lVar18;
        pfVar15 = local_118;
        do {
          lVar16 = lVar16 + -4;
          *pfVar15 = *pfVar15 / param_1;
          pfVar15[6] = pfVar15[6] / param_1;
          pfVar15[2] = pfVar15[2] / param_1;
          pfVar15[4] = pfVar15[4] / param_1;
          pfVar15 = pfVar15 + 8;
        } while (lVar16 != 0);
      }
      lVar16 = uVar5 - lVar18;
      pfVar15 = local_118 + lVar18 * 2;
      do {
        lVar16 = lVar16 + -1;
        *pfVar15 = *pfVar15 / param_1;
        pfVar15 = pfVar15 + 2;
      } while (lVar16 != 0);
    }
    local_98 = (float)param_6;
    local_88 = 0;
    local_9c = 0;
    local_90 = DAT_001044f8;
    local_a0 = (float)param_7;
    local_94 = 0x3fc90fdb;
    local_130 = (undefined8 *)0x0;
    local_128 = (undefined8 *)0x0;
    local_120 = (undefined8 *)0x0;
    local_148 = (undefined8 *)0x0;
    local_140 = (undefined8 *)0x0;
    local_138 = (undefined8 *)0x0;
    local_160 = (undefined8 *)0x0;
    local_158 = (undefined8 *)0x0;
    local_150 = (undefined8 *)0x0;
    local_178 = (undefined8 *)0x0;
    local_170 = (undefined8 *)0x0;
    local_168 = (undefined8 *)0x0;
    if ((uVar4 & 1) != 0) {
      local_190 = (undefined8 *)0x0;
      local_188 = (undefined8 *)0x0;
      local_180 = (undefined8 *)0x0;
      if (local_b0 == local_b8) {
LAB_0011b000:
        puVar8 = local_160;
        puVar25 = local_190;
        sVar22 = (long)local_158 - (long)local_160;
        lVar16 = (long)sVar22 >> 3;
        uVar5 = lVar16 + 1;
        if (uVar5 >> 0x3d != 0) {
LAB_0011c414:
          ppuVar14 = &local_160;
          goto LAB_0011c430;
        }
        uVar17 = (long)local_150 - (long)local_160 >> 2;
        if (uVar17 <= uVar5) {
          uVar17 = uVar5;
        }
        if (0x7ffffffffffffff7 < (ulong)((long)local_150 - (long)local_160)) {
          uVar17 = 0x1fffffffffffffff;
        }
        if (uVar17 == 0) {
          puVar6 = (undefined8 *)0x0;
LAB_0011b058:
          puVar7 = puVar6 + lVar16 + 1;
          puVar6[lVar16] = *puVar25;
          memcpy(puVar6,puVar8,sVar22);
          local_160 = puVar6;
          local_150 = puVar6 + uVar17;
          if (puVar8 != (undefined8 *)0x0) {
            local_158 = puVar7;
            operator_delete(puVar8);
          }
          goto LAB_0011b08c;
        }
        if (uVar17 >> 0x3d == 0) {
          puVar6 = operator_new(uVar17 << 3);
          goto LAB_0011b058;
        }
LAB_0011c400:
        if (*(long *)(lVar2 + 0x28) == local_80) {
                    /* WARNING: Subroutine does not return */
          FUN_0010b4cc();
        }
        goto LAB_0011c5c0;
      }
      puVar25 = (undefined8 *)0x0;
      uVar5 = 0;
      puVar8 = (undefined8 *)0x0;
      pvVar23 = local_b0;
      pvVar24 = local_b8;
      do {
        uVar26 = NEON_scvtf(*(undefined8 *)((long)pvVar24 + uVar5 * 8),4);
        uVar26 = CONCAT44((float)((ulong)uVar26 >> 0x20) / param_1,(float)uVar26 / param_1);
        if (local_188 < puVar25) {
          puVar7 = local_188 + 1;
          *local_188 = uVar26;
          puVar6 = puVar8;
        }
        else {
          sVar22 = (long)local_188 - (long)puVar8;
          lVar16 = (long)sVar22 >> 3;
          uVar17 = lVar16 + 1;
          if (uVar17 >> 0x3d != 0) {
            if (*(long *)(lVar2 + 0x28) == local_80) {
              FUN_0011c980(&local_190);
LAB_0011c3e8:
              if (*(long *)(lVar2 + 0x28) == local_80) {
                    /* WARNING: Subroutine does not return */
                FUN_0010b4cc();
              }
            }
            goto LAB_0011c5c0;
          }
          uVar19 = (long)puVar25 - (long)puVar8 >> 2;
          if (uVar19 <= uVar17) {
            uVar19 = uVar17;
          }
          if (0x7ffffffffffffff7 < (ulong)((long)puVar25 - (long)puVar8)) {
            uVar19 = 0x1fffffffffffffff;
          }
          if (uVar19 == 0) {
            puVar6 = (undefined8 *)0x0;
          }
          else {
            if (uVar19 >> 0x3d != 0) goto LAB_0011c3e8;
            puVar6 = operator_new(uVar19 << 3);
          }
          puVar25 = puVar6 + uVar19;
          puVar7 = puVar6 + lVar16 + 1;
          puVar6[lVar16] = uVar26;
          memcpy(puVar6,puVar8,sVar22);
          local_190 = puVar6;
          local_180 = puVar25;
          if (puVar8 != (undefined8 *)0x0) {
            local_188 = puVar7;
            operator_delete(puVar8);
            pvVar23 = local_b0;
            pvVar24 = local_b8;
          }
        }
        uVar5 = uVar5 + 1;
        puVar8 = puVar6;
        local_188 = puVar7;
      } while (uVar5 < (ulong)((long)pvVar23 - (long)pvVar24 >> 3));
      if (local_150 <= local_158) goto LAB_0011b000;
      puVar7 = local_158 + 1;
      *local_158 = *local_190;
      puVar25 = local_190;
LAB_0011b08c:
      puVar8 = local_160;
      if (puVar7 < local_150) {
        puVar20 = puVar7 + 1;
        *puVar7 = puVar25[3];
      }
      else {
        sVar22 = (long)puVar7 - (long)local_160;
        lVar16 = (long)sVar22 >> 3;
        uVar5 = lVar16 + 1;
        local_158 = puVar7;
        if (uVar5 >> 0x3d != 0) goto LAB_0011c414;
        uVar17 = (long)local_150 - (long)local_160 >> 2;
        if (uVar17 <= uVar5) {
          uVar17 = uVar5;
        }
        if (0x7ffffffffffffff7 < (ulong)((long)local_150 - (long)local_160)) {
          uVar17 = 0x1fffffffffffffff;
        }
        if (uVar17 == 0) {
          puVar6 = (undefined8 *)0x0;
        }
        else {
          if (uVar17 >> 0x3d != 0) goto LAB_0011c400;
          puVar6 = operator_new(uVar17 << 3);
        }
        puVar20 = puVar6 + lVar16 + 1;
        puVar6[lVar16] = puVar25[3];
        memcpy(puVar6,puVar8,sVar22);
        local_160 = puVar6;
        local_150 = puVar6 + uVar17;
        if (puVar8 != (undefined8 *)0x0) {
          local_158 = puVar20;
          operator_delete(puVar8);
        }
      }
      puVar8 = local_130;
      local_158 = puVar20;
      if (local_128 < local_120) {
        puVar7 = local_128 + 1;
        *local_128 = *puVar25;
LAB_0011b1dc:
        puVar8 = local_130;
        if (puVar7 < local_120) {
          puVar20 = puVar7 + 1;
          *puVar7 = puVar25[1];
        }
        else {
          sVar22 = (long)puVar7 - (long)local_130;
          lVar16 = (long)sVar22 >> 3;
          uVar5 = lVar16 + 1;
          local_128 = puVar7;
          if (uVar5 >> 0x3d != 0) goto LAB_0011c41c;
          uVar17 = (long)local_120 - (long)local_130 >> 2;
          if (uVar17 <= uVar5) {
            uVar17 = uVar5;
          }
          if (0x7ffffffffffffff7 < (ulong)((long)local_120 - (long)local_130)) {
            uVar17 = 0x1fffffffffffffff;
          }
          if (uVar17 == 0) {
            puVar6 = (undefined8 *)0x0;
          }
          else {
            if (uVar17 >> 0x3d != 0) goto LAB_0011c400;
            puVar6 = operator_new(uVar17 << 3);
          }
          puVar20 = puVar6 + lVar16 + 1;
          puVar6[lVar16] = puVar25[1];
          memcpy(puVar6,puVar8,sVar22);
          local_130 = puVar6;
          local_120 = puVar6 + uVar17;
          if (puVar8 != (undefined8 *)0x0) {
            local_128 = puVar20;
            operator_delete(puVar8);
          }
        }
        puVar8 = local_178;
        local_128 = puVar20;
        if (local_170 < local_168) {
          puVar7 = local_170 + 1;
          *local_170 = puVar25[1];
LAB_0011b32c:
          puVar8 = local_178;
          if (puVar7 < local_168) {
            puVar20 = puVar7 + 1;
            *puVar7 = puVar25[2];
          }
          else {
            sVar22 = (long)puVar7 - (long)local_178;
            lVar16 = (long)sVar22 >> 3;
            uVar5 = lVar16 + 1;
            local_170 = puVar7;
            if (uVar5 >> 0x3d != 0) goto LAB_0011c424;
            uVar17 = (long)local_168 - (long)local_178 >> 2;
            if (uVar17 <= uVar5) {
              uVar17 = uVar5;
            }
            if (0x7ffffffffffffff7 < (ulong)((long)local_168 - (long)local_178)) {
              uVar17 = 0x1fffffffffffffff;
            }
            if (uVar17 == 0) {
              puVar6 = (undefined8 *)0x0;
            }
            else {
              if (uVar17 >> 0x3d != 0) goto LAB_0011c400;
              puVar6 = operator_new(uVar17 << 3);
            }
            puVar20 = puVar6 + lVar16 + 1;
            puVar6[lVar16] = puVar25[2];
            memcpy(puVar6,puVar8,sVar22);
            local_178 = puVar6;
            local_168 = puVar6 + uVar17;
            if (puVar8 != (undefined8 *)0x0) {
              local_170 = puVar20;
              operator_delete(puVar8);
            }
          }
          puVar8 = local_148;
          local_170 = puVar20;
          if (local_140 < local_138) {
            puVar6 = local_140 + 1;
            *local_140 = puVar25[3];
LAB_0011b47c:
            puVar8 = local_148;
            if (puVar6 < local_138) {
              puVar7 = puVar6 + 1;
              *puVar6 = puVar25[2];
            }
            else {
              sVar22 = (long)puVar6 - (long)local_148;
              lVar16 = (long)sVar22 >> 3;
              uVar5 = lVar16 + 1;
              local_140 = puVar6;
              if (uVar5 >> 0x3d != 0) goto LAB_0011c42c;
              uVar17 = (long)local_138 - (long)local_148 >> 2;
              if (uVar17 <= uVar5) {
                uVar17 = uVar5;
              }
              if (0x7ffffffffffffff7 < (ulong)((long)local_138 - (long)local_148)) {
                uVar17 = 0x1fffffffffffffff;
              }
              if (uVar17 == 0) {
                puVar6 = (undefined8 *)0x0;
              }
              else {
                if (uVar17 >> 0x3d != 0) goto LAB_0011c400;
                puVar6 = operator_new(uVar17 << 3);
              }
              puVar7 = puVar6 + lVar16 + 1;
              puVar6[lVar16] = puVar25[2];
              memcpy(puVar6,puVar8,sVar22);
              local_148 = puVar6;
              local_138 = puVar6 + uVar17;
              if (puVar8 != (undefined8 *)0x0) {
                local_140 = puVar7;
                operator_delete(puVar8);
              }
            }
            local_188 = puVar25;
            local_140 = puVar7;
            operator_delete(puVar25);
            goto LAB_0011b538;
          }
          sVar22 = (long)local_140 - (long)local_148;
          lVar16 = (long)sVar22 >> 3;
          uVar5 = lVar16 + 1;
          if (uVar5 >> 0x3d == 0) {
            uVar17 = (long)local_138 - (long)local_148 >> 2;
            if (uVar17 <= uVar5) {
              uVar17 = uVar5;
            }
            if (0x7ffffffffffffff7 < (ulong)((long)local_138 - (long)local_148)) {
              uVar17 = 0x1fffffffffffffff;
            }
            if (uVar17 == 0) {
              puVar7 = (undefined8 *)0x0;
            }
            else {
              if (uVar17 >> 0x3d != 0) goto LAB_0011c400;
              puVar7 = operator_new(uVar17 << 3);
            }
            puVar6 = puVar7 + lVar16 + 1;
            puVar7[lVar16] = puVar25[3];
            memcpy(puVar7,puVar8,sVar22);
            local_148 = puVar7;
            local_138 = puVar7 + uVar17;
            if (puVar8 != (undefined8 *)0x0) {
              local_140 = puVar6;
              operator_delete(puVar8);
            }
            goto LAB_0011b47c;
          }
LAB_0011c42c:
          ppuVar14 = &local_148;
        }
        else {
          sVar22 = (long)local_170 - (long)local_178;
          lVar16 = (long)sVar22 >> 3;
          uVar5 = lVar16 + 1;
          if (uVar5 >> 0x3d == 0) {
            uVar17 = (long)local_168 - (long)local_178 >> 2;
            if (uVar17 <= uVar5) {
              uVar17 = uVar5;
            }
            if (0x7ffffffffffffff7 < (ulong)((long)local_168 - (long)local_178)) {
              uVar17 = 0x1fffffffffffffff;
            }
            if (uVar17 == 0) {
              puVar6 = (undefined8 *)0x0;
            }
            else {
              if (uVar17 >> 0x3d != 0) goto LAB_0011c400;
              puVar6 = operator_new(uVar17 << 3);
            }
            puVar7 = puVar6 + lVar16 + 1;
            puVar6[lVar16] = puVar25[1];
            memcpy(puVar6,puVar8,sVar22);
            local_178 = puVar6;
            local_168 = puVar6 + uVar17;
            if (puVar8 != (undefined8 *)0x0) {
              local_170 = puVar7;
              operator_delete(puVar8);
            }
            goto LAB_0011b32c;
          }
LAB_0011c424:
          ppuVar14 = &local_178;
        }
      }
      else {
        sVar22 = (long)local_128 - (long)local_130;
        lVar16 = (long)sVar22 >> 3;
        uVar5 = lVar16 + 1;
        if (uVar5 >> 0x3d == 0) {
          uVar17 = (long)local_120 - (long)local_130 >> 2;
          if (uVar17 <= uVar5) {
            uVar17 = uVar5;
          }
          if (0x7ffffffffffffff7 < (ulong)((long)local_120 - (long)local_130)) {
            uVar17 = 0x1fffffffffffffff;
          }
          if (uVar17 == 0) {
            puVar6 = (undefined8 *)0x0;
          }
          else {
            if (uVar17 >> 0x3d != 0) goto LAB_0011c400;
            puVar6 = operator_new(uVar17 << 3);
          }
          puVar7 = puVar6 + lVar16 + 1;
          puVar6[lVar16] = *puVar25;
          memcpy(puVar6,puVar8,sVar22);
          local_130 = puVar6;
          local_120 = puVar6 + uVar17;
          if (puVar8 != (undefined8 *)0x0) {
            local_128 = puVar7;
            operator_delete(puVar8);
          }
          goto LAB_0011b1dc;
        }
LAB_0011c41c:
        ppuVar14 = &local_130;
      }
LAB_0011c430:
      if (*(long *)(lVar2 + 0x28) == local_80) {
        uVar26 = FUN_0011c980(ppuVar14);
        if (local_178 != (undefined8 *)0x0) {
          local_170 = local_178;
          operator_delete(local_178);
        }
        if (local_160 != (undefined8 *)0x0) {
          local_158 = local_160;
          operator_delete(local_160);
        }
        if (local_148 != (undefined8 *)0x0) {
          local_140 = local_148;
          operator_delete(local_148);
        }
        if (local_130 != (undefined8 *)0x0) {
          local_128 = local_130;
          operator_delete(local_130);
        }
        if (local_118 != (float *)0x0) {
          local_110 = local_118;
          operator_delete(local_118);
        }
        if (local_100 != (float *)0x0) {
          local_f8 = local_100;
          operator_delete(local_100);
        }
        if (local_e8 != (float *)0x0) {
          local_e0 = local_e8;
          operator_delete(local_e8);
        }
        if (local_d0 != (float *)0x0) {
          local_c8 = local_d0;
          operator_delete(local_d0);
        }
        if (local_b8 != (void *)0x0) {
          local_b0 = local_b8;
          operator_delete(local_b8);
        }
        if (*(long *)(lVar2 + 0x28) == local_80) {
                    /* WARNING: Subroutine does not return */
          FUN_0011cac8(uVar26);
        }
      }
      goto LAB_0011c5c0;
    }
LAB_0011b538:
    if (local_f8 != local_100) {
      lVar16 = 0;
      uVar5 = 0;
      do {
        local_198 = 0;
        local_190 = (undefined8 *)0x0;
        MathUtil::polarIntersect
                  ((Vec *)&local_88,(Vec *)((long)local_100 + lVar16),(Point_ *)&local_190);
        MathUtil::polarIntersect
                  ((Vec *)&local_a0,(Vec *)((long)local_100 + lVar16),(Point_ *)&local_198);
        puVar25 = local_160;
        if (local_150 <= local_158) {
          sVar22 = (long)local_158 - (long)local_160;
          lVar18 = (long)sVar22 >> 3;
          uVar17 = lVar18 + 1;
          if (uVar17 >> 0x3d == 0) {
            uVar19 = (long)local_150 - (long)local_160 >> 2;
            if (uVar19 <= uVar17) {
              uVar19 = uVar17;
            }
            if (0x7ffffffffffffff7 < (ulong)((long)local_150 - (long)local_160)) {
              uVar19 = 0x1fffffffffffffff;
            }
            if (uVar19 == 0) {
              puVar8 = (undefined8 *)0x0;
            }
            else {
              if (uVar19 >> 0x3d != 0) goto LAB_0011c36c;
              puVar8 = operator_new(uVar19 << 3);
            }
            puVar6 = puVar8 + lVar18 + 1;
            puVar8[lVar18] = local_190;
            memcpy(puVar8,puVar25,sVar22);
            local_160 = puVar8;
            local_150 = puVar8 + uVar19;
            if (puVar25 != (undefined8 *)0x0) {
              local_158 = puVar6;
              operator_delete(puVar25);
            }
            goto LAB_0011b640;
          }
LAB_0011c2fc:
          if (*(long *)(lVar2 + 0x28) != local_80) goto LAB_0011c5c0;
          FUN_0011c980(&local_160);
          goto LAB_0011c318;
        }
        puVar6 = local_158 + 1;
        *local_158 = local_190;
LAB_0011b640:
        puVar25 = local_160;
        if (puVar6 < local_150) {
          puVar8 = puVar6 + 1;
          *puVar6 = local_198;
        }
        else {
          sVar22 = (long)puVar6 - (long)local_160;
          lVar18 = (long)sVar22 >> 3;
          uVar17 = lVar18 + 1;
          local_158 = puVar6;
          if (uVar17 >> 0x3d != 0) goto LAB_0011c2fc;
          uVar19 = (long)local_150 - (long)local_160 >> 2;
          if (uVar19 <= uVar17) {
            uVar19 = uVar17;
          }
          if (0x7ffffffffffffff7 < (ulong)((long)local_150 - (long)local_160)) {
            uVar19 = 0x1fffffffffffffff;
          }
          if (uVar19 == 0) {
            puVar6 = (undefined8 *)0x0;
          }
          else {
            if (uVar19 >> 0x3d != 0) goto LAB_0011c36c;
            puVar6 = operator_new(uVar19 << 3);
          }
          puVar8 = puVar6 + lVar18 + 1;
          puVar6[lVar18] = local_198;
          memcpy(puVar6,puVar25,sVar22);
          local_160 = puVar6;
          local_150 = puVar6 + uVar19;
          if (puVar25 != (undefined8 *)0x0) {
            local_158 = puVar8;
            operator_delete(puVar25);
          }
        }
        uVar5 = uVar5 + 1;
        lVar16 = lVar16 + 8;
        local_158 = puVar8;
      } while (uVar5 < (ulong)((long)local_f8 - (long)local_100 >> 3));
    }
    if (local_c8 != local_d0) {
      lVar16 = 0;
      uVar5 = 0;
      do {
        local_198 = 0;
        local_190 = (undefined8 *)0x0;
        MathUtil::polarIntersect
                  ((Vec *)&local_90,(Vec *)((long)local_d0 + lVar16),(Point_ *)&local_190);
        MathUtil::polarIntersect
                  ((Vec *)&local_98,(Vec *)((long)local_d0 + lVar16),(Point_ *)&local_198);
        puVar25 = local_130;
        if (local_120 <= local_128) {
          sVar22 = (long)local_128 - (long)local_130;
          lVar18 = (long)sVar22 >> 3;
          uVar17 = lVar18 + 1;
          if (uVar17 >> 0x3d != 0) goto LAB_0011c318;
          uVar19 = (long)local_120 - (long)local_130 >> 2;
          if (uVar19 <= uVar17) {
            uVar19 = uVar17;
          }
          if (0x7ffffffffffffff7 < (ulong)((long)local_120 - (long)local_130)) {
            uVar19 = 0x1fffffffffffffff;
          }
          if (uVar19 == 0) {
            puVar8 = (undefined8 *)0x0;
LAB_0011b7b0:
            puVar6 = puVar8 + lVar18 + 1;
            puVar8[lVar18] = local_190;
            memcpy(puVar8,puVar25,sVar22);
            local_130 = puVar8;
            local_120 = puVar8 + uVar19;
            if (puVar25 != (undefined8 *)0x0) {
              local_128 = puVar6;
              operator_delete(puVar25);
            }
            goto LAB_0011b7e4;
          }
          if (uVar19 >> 0x3d == 0) {
            puVar8 = operator_new(uVar19 << 3);
            goto LAB_0011b7b0;
          }
LAB_0011c384:
          if (*(long *)(lVar2 + 0x28) == local_80) {
                    /* WARNING: Subroutine does not return */
            FUN_0010b4cc();
          }
          goto LAB_0011c5c0;
        }
        puVar6 = local_128 + 1;
        *local_128 = local_190;
LAB_0011b7e4:
        puVar25 = local_130;
        if (puVar6 < local_120) {
          puVar8 = puVar6 + 1;
          *puVar6 = local_198;
        }
        else {
          sVar22 = (long)puVar6 - (long)local_130;
          lVar18 = (long)sVar22 >> 3;
          uVar17 = lVar18 + 1;
          local_128 = puVar6;
          if (uVar17 >> 0x3d != 0) goto LAB_0011c318;
          uVar19 = (long)local_120 - (long)local_130 >> 2;
          if (uVar19 <= uVar17) {
            uVar19 = uVar17;
          }
          if (0x7ffffffffffffff7 < (ulong)((long)local_120 - (long)local_130)) {
            uVar19 = 0x1fffffffffffffff;
          }
          if (uVar19 == 0) {
            puVar6 = (undefined8 *)0x0;
          }
          else {
            if (uVar19 >> 0x3d != 0) goto LAB_0011c384;
            puVar6 = operator_new(uVar19 << 3);
          }
          puVar8 = puVar6 + lVar18 + 1;
          puVar6[lVar18] = local_198;
          memcpy(puVar6,puVar25,sVar22);
          local_130 = puVar6;
          local_120 = puVar6 + uVar19;
          if (puVar25 != (undefined8 *)0x0) {
            local_128 = puVar8;
            operator_delete(puVar25);
          }
        }
        uVar5 = uVar5 + 1;
        lVar16 = lVar16 + 8;
        local_128 = puVar8;
      } while (uVar5 < (ulong)((long)local_c8 - (long)local_d0 >> 3));
    }
    if (local_110 != local_118) {
      lVar16 = 0;
      uVar5 = 0;
      do {
        local_198 = 0;
        local_190 = (undefined8 *)0x0;
        MathUtil::polarIntersect
                  ((Vec *)&local_88,(Vec *)((long)local_118 + lVar16),(Point_ *)&local_190);
        MathUtil::polarIntersect
                  ((Vec *)&local_a0,(Vec *)((long)local_118 + lVar16),(Point_ *)&local_198);
        puVar25 = local_178;
        if (local_168 <= local_170) {
          sVar22 = (long)local_170 - (long)local_178;
          lVar18 = (long)sVar22 >> 3;
          uVar17 = lVar18 + 1;
          if (uVar17 >> 0x3d != 0) goto LAB_0011c334;
          uVar19 = (long)local_168 - (long)local_178 >> 2;
          if (uVar19 <= uVar17) {
            uVar19 = uVar17;
          }
          if (0x7ffffffffffffff7 < (ulong)((long)local_168 - (long)local_178)) {
            uVar19 = 0x1fffffffffffffff;
          }
          if (uVar19 == 0) {
            puVar8 = (undefined8 *)0x0;
LAB_0011b954:
            puVar6 = puVar8 + lVar18 + 1;
            puVar8[lVar18] = local_190;
            memcpy(puVar8,puVar25,sVar22);
            local_178 = puVar8;
            local_168 = puVar8 + uVar19;
            if (puVar25 != (undefined8 *)0x0) {
              local_170 = puVar6;
              operator_delete(puVar25);
            }
            goto LAB_0011b988;
          }
          if (uVar19 >> 0x3d == 0) {
            puVar8 = operator_new(uVar19 << 3);
            goto LAB_0011b954;
          }
LAB_0011c39c:
          if (*(long *)(lVar2 + 0x28) == local_80) {
                    /* WARNING: Subroutine does not return */
            FUN_0010b4cc();
          }
          goto LAB_0011c5c0;
        }
        puVar6 = local_170 + 1;
        *local_170 = local_190;
LAB_0011b988:
        puVar25 = local_178;
        if (puVar6 < local_168) {
          puVar8 = puVar6 + 1;
          *puVar6 = local_198;
        }
        else {
          sVar22 = (long)puVar6 - (long)local_178;
          lVar18 = (long)sVar22 >> 3;
          uVar17 = lVar18 + 1;
          local_170 = puVar6;
          if (uVar17 >> 0x3d != 0) goto LAB_0011c334;
          uVar19 = (long)local_168 - (long)local_178 >> 2;
          if (uVar19 <= uVar17) {
            uVar19 = uVar17;
          }
          if (0x7ffffffffffffff7 < (ulong)((long)local_168 - (long)local_178)) {
            uVar19 = 0x1fffffffffffffff;
          }
          if (uVar19 == 0) {
            puVar6 = (undefined8 *)0x0;
          }
          else {
            if (uVar19 >> 0x3d != 0) goto LAB_0011c39c;
            puVar6 = operator_new(uVar19 << 3);
          }
          puVar8 = puVar6 + lVar18 + 1;
          puVar6[lVar18] = local_198;
          memcpy(puVar6,puVar25,sVar22);
          local_178 = puVar6;
          local_168 = puVar6 + uVar19;
          if (puVar25 != (undefined8 *)0x0) {
            local_170 = puVar8;
            operator_delete(puVar25);
          }
        }
        uVar5 = uVar5 + 1;
        lVar16 = lVar16 + 8;
        local_170 = puVar8;
      } while (uVar5 < (ulong)((long)local_110 - (long)local_118 >> 3));
    }
    if (local_e0 != local_e8) {
      lVar16 = 0;
      uVar5 = 0;
      do {
        local_198 = 0;
        local_190 = (undefined8 *)0x0;
        MathUtil::polarIntersect
                  ((Vec *)&local_90,(Vec *)((long)local_e8 + lVar16),(Point_ *)&local_190);
        MathUtil::polarIntersect
                  ((Vec *)&local_98,(Vec *)((long)local_e8 + lVar16),(Point_ *)&local_198);
        puVar25 = local_148;
        if (local_138 <= local_140) {
          sVar22 = (long)local_140 - (long)local_148;
          lVar18 = (long)sVar22 >> 3;
          uVar17 = lVar18 + 1;
          if (uVar17 >> 0x3d != 0) goto LAB_0011c350;
          uVar19 = (long)local_138 - (long)local_148 >> 2;
          if (uVar19 <= uVar17) {
            uVar19 = uVar17;
          }
          if (0x7ffffffffffffff7 < (ulong)((long)local_138 - (long)local_148)) {
            uVar19 = 0x1fffffffffffffff;
          }
          if (uVar19 == 0) {
            puVar8 = (undefined8 *)0x0;
LAB_0011baf8:
            puVar6 = puVar8 + lVar18 + 1;
            puVar8[lVar18] = local_190;
            memcpy(puVar8,puVar25,sVar22);
            local_148 = puVar8;
            local_138 = puVar8 + uVar19;
            if (puVar25 != (undefined8 *)0x0) {
              local_140 = puVar6;
              operator_delete(puVar25);
            }
            goto LAB_0011bb2c;
          }
          if (uVar19 >> 0x3d == 0) {
            puVar8 = operator_new(uVar19 << 3);
            goto LAB_0011baf8;
          }
LAB_0011c3b4:
          if (*(long *)(lVar2 + 0x28) == local_80) {
                    /* WARNING: Subroutine does not return */
            FUN_0010b4cc();
          }
          goto LAB_0011c5c0;
        }
        puVar6 = local_140 + 1;
        *local_140 = local_190;
LAB_0011bb2c:
        puVar25 = local_148;
        if (puVar6 < local_138) {
          puVar8 = puVar6 + 1;
          *puVar6 = local_198;
        }
        else {
          sVar22 = (long)puVar6 - (long)local_148;
          lVar18 = (long)sVar22 >> 3;
          uVar17 = lVar18 + 1;
          local_140 = puVar6;
          if (uVar17 >> 0x3d != 0) goto LAB_0011c350;
          uVar19 = (long)local_138 - (long)local_148 >> 2;
          if (uVar19 <= uVar17) {
            uVar19 = uVar17;
          }
          if (0x7ffffffffffffff7 < (ulong)((long)local_138 - (long)local_148)) {
            uVar19 = 0x1fffffffffffffff;
          }
          if (uVar19 == 0) {
            puVar6 = (undefined8 *)0x0;
          }
          else {
            if (uVar19 >> 0x3d != 0) goto LAB_0011c3b4;
            puVar6 = operator_new(uVar19 << 3);
          }
          puVar8 = puVar6 + lVar18 + 1;
          puVar6[lVar18] = local_198;
          memcpy(puVar6,puVar25,sVar22);
          local_148 = puVar6;
          local_138 = puVar6 + uVar19;
          if (puVar25 != (undefined8 *)0x0) {
            local_140 = puVar8;
            operator_delete(puVar25);
          }
        }
        uVar5 = uVar5 + 1;
        lVar16 = lVar16 + 8;
        local_140 = puVar8;
      } while (uVar5 < (ulong)((long)local_e0 - (long)local_e8 >> 3));
    }
    uVar5 = (long)local_158 - (long)local_160;
    iVar21 = (int)(uVar5 >> 3);
    iVar1 = iVar21 << 1;
    uVar9 = (**(code **)(*(long *)param_2 + 0x5a8))(param_2,iVar1);
    puVar25 = malloc((long)(uVar5 * 0x20000000) >> 0x1d);
    if (0 < iVar21) {
      uVar17 = uVar5 >> 3 & 0x7fffffff;
      if ((uVar17 < 8) || ((puVar25 < local_160 + uVar17 && (local_160 < puVar25 + uVar17)))) {
        lVar16 = 0;
      }
      else {
        uVar5 = uVar5 >> 3 & 7;
        lVar16 = uVar17 - uVar5;
        puVar8 = local_160 + 4;
        puVar6 = puVar25 + 4;
        lVar18 = lVar16;
        do {
          uVar26 = puVar8[-4];
          uVar11 = puVar8[-1];
          uVar10 = puVar8[-2];
          lVar18 = lVar18 + -8;
          uVar28 = puVar8[1];
          uVar27 = *puVar8;
          uVar13 = puVar8[3];
          uVar12 = puVar8[2];
          puVar6[-3] = puVar8[-3];
          puVar6[-4] = uVar26;
          puVar6[-1] = uVar11;
          puVar6[-2] = uVar10;
          puVar6[1] = uVar28;
          *puVar6 = uVar27;
          puVar6[3] = uVar13;
          puVar6[2] = uVar12;
          puVar8 = puVar8 + 8;
          puVar6 = puVar6 + 8;
        } while (lVar18 != 0);
        if (uVar5 == 0) goto LAB_0011bc50;
      }
      lVar18 = lVar16 - uVar17;
      puVar8 = local_160 + lVar16;
      puVar6 = puVar25 + lVar16;
      do {
        bVar3 = lVar18 != -1;
        lVar18 = lVar18 + 1;
        *puVar6 = *puVar8;
        puVar8 = puVar8 + 1;
        puVar6 = puVar6 + 1;
      } while (bVar3);
    }
LAB_0011bc50:
    (**(code **)(*(long *)param_2 + 0x6a8))(param_2,uVar9,0,iVar1,puVar25);
    free(puVar25);
    uVar5 = (long)local_128 - (long)local_130;
    iVar21 = (int)(uVar5 >> 3);
    iVar1 = iVar21 << 1;
    uVar10 = (**(code **)(*(long *)param_2 + 0x5a8))(param_2,iVar1);
    puVar25 = malloc((long)(uVar5 * 0x20000000) >> 0x1d);
    if (0 < iVar21) {
      uVar17 = uVar5 >> 3 & 0x7fffffff;
      if ((uVar17 < 8) || ((puVar25 < local_130 + uVar17 && (local_130 < puVar25 + uVar17)))) {
        lVar16 = 0;
      }
      else {
        uVar5 = uVar5 >> 3 & 7;
        lVar16 = uVar17 - uVar5;
        puVar8 = local_130 + 4;
        puVar6 = puVar25 + 4;
        lVar18 = lVar16;
        do {
          uVar26 = puVar8[-4];
          uVar12 = puVar8[-1];
          uVar11 = puVar8[-2];
          lVar18 = lVar18 + -8;
          uVar29 = puVar8[1];
          uVar28 = *puVar8;
          uVar27 = puVar8[3];
          uVar13 = puVar8[2];
          puVar6[-3] = puVar8[-3];
          puVar6[-4] = uVar26;
          puVar6[-1] = uVar12;
          puVar6[-2] = uVar11;
          puVar6[1] = uVar29;
          *puVar6 = uVar28;
          puVar6[3] = uVar27;
          puVar6[2] = uVar13;
          puVar8 = puVar8 + 8;
          puVar6 = puVar6 + 8;
        } while (lVar18 != 0);
        if (uVar5 == 0) goto LAB_0011bd08;
      }
      lVar18 = lVar16 - uVar17;
      puVar8 = local_130 + lVar16;
      puVar6 = puVar25 + lVar16;
      do {
        bVar3 = lVar18 != -1;
        lVar18 = lVar18 + 1;
        *puVar6 = *puVar8;
        puVar8 = puVar8 + 1;
        puVar6 = puVar6 + 1;
      } while (bVar3);
    }
LAB_0011bd08:
    (**(code **)(*(long *)param_2 + 0x6a8))(param_2,uVar10,0,iVar1,puVar25);
    free(puVar25);
    uVar5 = (long)local_170 - (long)local_178;
    iVar21 = (int)(uVar5 >> 3);
    iVar1 = iVar21 << 1;
    uVar11 = (**(code **)(*(long *)param_2 + 0x5a8))(param_2,iVar1);
    puVar25 = malloc((long)(uVar5 * 0x20000000) >> 0x1d);
    if (0 < iVar21) {
      uVar17 = uVar5 >> 3 & 0x7fffffff;
      if ((uVar17 < 8) || ((puVar25 < local_178 + uVar17 && (local_178 < puVar25 + uVar17)))) {
        lVar16 = 0;
      }
      else {
        uVar5 = uVar5 >> 3 & 7;
        lVar16 = uVar17 - uVar5;
        puVar8 = local_178 + 4;
        puVar6 = puVar25 + 4;
        lVar18 = lVar16;
        do {
          uVar26 = puVar8[-4];
          uVar13 = puVar8[-1];
          uVar12 = puVar8[-2];
          lVar18 = lVar18 + -8;
          uVar30 = puVar8[1];
          uVar29 = *puVar8;
          uVar28 = puVar8[3];
          uVar27 = puVar8[2];
          puVar6[-3] = puVar8[-3];
          puVar6[-4] = uVar26;
          puVar6[-1] = uVar13;
          puVar6[-2] = uVar12;
          puVar6[1] = uVar30;
          *puVar6 = uVar29;
          puVar6[3] = uVar28;
          puVar6[2] = uVar27;
          puVar8 = puVar8 + 8;
          puVar6 = puVar6 + 8;
        } while (lVar18 != 0);
        if (uVar5 == 0) goto LAB_0011bdc0;
      }
      lVar18 = lVar16 - uVar17;
      puVar8 = local_178 + lVar16;
      puVar6 = puVar25 + lVar16;
      do {
        bVar3 = lVar18 != -1;
        lVar18 = lVar18 + 1;
        *puVar6 = *puVar8;
        puVar8 = puVar8 + 1;
        puVar6 = puVar6 + 1;
      } while (bVar3);
    }
LAB_0011bdc0:
    (**(code **)(*(long *)param_2 + 0x6a8))(param_2,uVar11,0,iVar1,puVar25);
    free(puVar25);
    uVar5 = (long)local_140 - (long)local_148;
    iVar21 = (int)(uVar5 >> 3);
    iVar1 = iVar21 << 1;
    uVar12 = (**(code **)(*(long *)param_2 + 0x5a8))(param_2,iVar1);
    puVar25 = malloc((long)(uVar5 * 0x20000000) >> 0x1d);
    if (0 < iVar21) {
      uVar17 = uVar5 >> 3 & 0x7fffffff;
      if ((uVar17 < 8) || ((puVar25 < local_148 + uVar17 && (local_148 < puVar25 + uVar17)))) {
        lVar16 = 0;
      }
      else {
        uVar5 = uVar5 >> 3 & 7;
        lVar16 = uVar17 - uVar5;
        puVar8 = local_148 + 4;
        puVar6 = puVar25 + 4;
        lVar18 = lVar16;
        do {
          uVar26 = puVar8[-4];
          uVar27 = puVar8[-1];
          uVar13 = puVar8[-2];
          lVar18 = lVar18 + -8;
          uVar31 = puVar8[1];
          uVar30 = *puVar8;
          uVar29 = puVar8[3];
          uVar28 = puVar8[2];
          puVar6[-3] = puVar8[-3];
          puVar6[-4] = uVar26;
          puVar6[-1] = uVar27;
          puVar6[-2] = uVar13;
          puVar6[1] = uVar31;
          *puVar6 = uVar30;
          puVar6[3] = uVar29;
          puVar6[2] = uVar28;
          puVar8 = puVar8 + 8;
          puVar6 = puVar6 + 8;
        } while (lVar18 != 0);
        if (uVar5 == 0) goto LAB_0011be78;
      }
      lVar18 = lVar16 - uVar17;
      puVar8 = local_148 + lVar16;
      puVar6 = puVar25 + lVar16;
      do {
        bVar3 = lVar18 != -1;
        lVar18 = lVar18 + 1;
        *puVar6 = *puVar8;
        puVar8 = puVar8 + 1;
        puVar6 = puVar6 + 1;
      } while (bVar3);
    }
LAB_0011be78:
    (**(code **)(*(long *)param_2 + 0x6a8))(param_2,uVar12,0,iVar1,puVar25);
    free(puVar25);
    local_198 = 0;
    local_190 = (undefined8 *)0x0;
    local_1a8 = 0;
    uStack_1a0 = 0;
    MathUtil::lineIntersect((vector *)&local_130,(vector *)&local_160,(Point_ *)&local_190);
    MathUtil::lineIntersect((vector *)&local_160,(vector *)&local_148,(Point_ *)&local_198);
    MathUtil::lineIntersect((vector *)&local_130,(vector *)&local_178,(Point_ *)&uStack_1a0);
    MathUtil::lineIntersect((vector *)&local_178,(vector *)&local_148,(Point_ *)&local_1a8);
    uVar13 = (**(code **)(*(long *)param_2 + 0x5a8))(param_2,8);
    puVar25 = malloc(0x20);
    lVar16 = *(long *)param_2;
    puVar25[1] = local_198;
    *puVar25 = local_190;
    puVar25[3] = local_1a8;
    puVar25[2] = uStack_1a0;
    (**(code **)(lVar16 + 0x6a8))(param_2,uVar13,0,8,puVar25);
    free(puVar25);
    DAT_00129498 = (_jmethodID *)
                   (**(code **)(*(long *)param_2 + 0x30))
                             (param_2,
                              "com/indymobile/app/task/PSDetectPageBorderTask$TransformDetectBorderInfo"
                             );
    DAT_001294a0 = (**(code **)(*(long *)param_2 + 0x108))
                             (param_2,DAT_00129498,"<init>",&DAT_00104680);
    uVar26 = _JNIEnv::NewObject((_jclass *)param_2,DAT_00129498,DAT_001294a0);
    DAT_001294a8 = (**(code **)(*(long *)param_2 + 0x2f0))
                             (param_2,DAT_00129498,"jFoundContour",&DAT_00104ac2);
    (**(code **)(*(long *)param_2 + 0x348))(param_2,uVar26,DAT_001294a8,uVar4 & 1);
    DAT_001294b0 = (**(code **)(*(long *)param_2 + 0x2f0))
                             (param_2,DAT_00129498,"jAutoDetectRects",&DAT_001048e9);
    (**(code **)(*(long *)param_2 + 0x340))(param_2,uVar26,DAT_001294b0,uVar13);
    DAT_001294b8 = (**(code **)(*(long *)param_2 + 0x2f0))
                             (param_2,DAT_00129498,"jTopPSLines",&DAT_001048e9);
    (**(code **)(*(long *)param_2 + 0x340))(param_2,uVar26,DAT_001294b8,uVar9);
    DAT_001294c0 = (**(code **)(*(long *)param_2 + 0x2f0))
                             (param_2,DAT_00129498,"jLeftPSLines",&DAT_001048e9);
    (**(code **)(*(long *)param_2 + 0x340))(param_2,uVar26,DAT_001294c0,uVar10);
    DAT_001294c8 = (**(code **)(*(long *)param_2 + 0x2f0))
                             (param_2,DAT_00129498,"jBottomPSLines",&DAT_001048e9);
    (**(code **)(*(long *)param_2 + 0x340))(param_2,uVar26,DAT_001294c8,uVar11);
    DAT_001294d0 = (**(code **)(*(long *)param_2 + 0x2f0))
                             (param_2,DAT_00129498,"jRightPSLines",&DAT_001048e9);
    (**(code **)(*(long *)param_2 + 0x340))(param_2,uVar26,DAT_001294d0,uVar12);
    if (local_178 != (undefined8 *)0x0) {
      local_170 = local_178;
      operator_delete(local_178);
    }
    if (local_160 != (undefined8 *)0x0) {
      local_158 = local_160;
      operator_delete(local_160);
    }
    if (local_148 != (undefined8 *)0x0) {
      local_140 = local_148;
      operator_delete(local_148);
    }
    if (local_130 != (undefined8 *)0x0) {
      local_128 = local_130;
      operator_delete(local_130);
    }
    if (local_118 != (float *)0x0) {
      local_110 = local_118;
      operator_delete(local_118);
    }
    if (local_100 != (float *)0x0) {
      local_f8 = local_100;
      operator_delete(local_100);
    }
    if (local_e8 != (float *)0x0) {
      local_e0 = local_e8;
      operator_delete(local_e8);
    }
    if (local_d0 != (float *)0x0) {
      local_c8 = local_d0;
      operator_delete(local_d0);
    }
    if (local_b8 != (void *)0x0) {
      local_b0 = local_b8;
      operator_delete(local_b8);
    }
  }
  if (*(long *)(lVar2 + 0x28) == local_80) {
    return uVar26;
  }
LAB_0011c5c0:
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
LAB_0011c318:
  if (*(long *)(lVar2 + 0x28) == local_80) {
    FUN_0011c980(&local_130);
LAB_0011c334:
    if (*(long *)(lVar2 + 0x28) == local_80) {
      FUN_0011c980(&local_178);
LAB_0011c350:
      if (*(long *)(lVar2 + 0x28) == local_80) {
        FUN_0011c980(&local_148);
LAB_0011c36c:
        if (*(long *)(lVar2 + 0x28) == local_80) {
                    /* WARNING: Subroutine does not return */
          FUN_0010b4cc();
        }
      }
    }
  }
  goto LAB_0011c5c0;
}

