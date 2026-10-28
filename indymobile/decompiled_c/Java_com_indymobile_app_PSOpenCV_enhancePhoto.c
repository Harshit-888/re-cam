
/* WARNING: Globals starting with '_' overlap smaller symbols at the same address */

void Java_com_indymobile_app_PSOpenCV_enhancePhoto
               (_JNIEnv *param_1,undefined8 param_2,_jobject *param_3,Mat *param_4,Mat *param_5,
               int param_6,int param_7,int param_8)

{
  long lVar1;
  undefined8 uVar2;
  undefined8 uVar3;
  undefined8 uVar4;
  undefined8 uVar5;
  undefined8 uVar6;
  undefined8 uVar7;
  ulong uVar8;
  long lVar9;
  undefined1 uVar10;
  uint uVar11;
  undefined4 local_100 [2];
  Mat *local_f8;
  undefined8 uStack_f0;
  undefined4 local_e8 [2];
  Mat *local_e0;
  undefined8 local_d8;
  undefined4 local_d0 [2];
  Mat *local_c8;
  undefined8 local_c0;
  Mat aMStack_b8 [16];
  undefined8 *local_a8;
  long local_58;
  
  lVar1 = tpidr_el0;
  local_58 = *(long *)(lVar1 + 0x28);
  uVar8 = OpenCVUtil::checkAuthentication(param_1,param_3);
  if ((uVar8 & 1) != 0) {
    cv::Mat::Mat(aMStack_b8,1,0x100,0);
    uVar6 = _DAT_00105180;
    uVar5 = _UNK_00105178;
    uVar4 = _DAT_00105170;
    uVar3 = _UNK_00105038;
    uVar2 = _DAT_00105030;
    if (param_7 - param_6 < 0) {
      local_a8[1] = _UNK_00105188;
      *local_a8 = uVar6;
      local_a8[3] = uVar5;
      local_a8[2] = uVar4;
      uVar7 = _UNK_00105108;
      uVar6 = _DAT_00105100;
      uVar5 = s_0123456789__<_>__abcdefghijklmno_00105050._8_8_;
      uVar4 = s_0123456789__<_>__abcdefghijklmno_00105050._0_8_;
      local_a8[5] = uVar3;
      local_a8[4] = uVar2;
      local_a8[7] = uVar5;
      local_a8[6] = uVar4;
      uVar5 = _UNK_00105128;
      uVar4 = _DAT_00105120;
      uVar3 = s_0123456789__<_>__abcdefghijklmno_00105050._24_8_;
      uVar2 = s_0123456789__<_>__abcdefghijklmno_00105050._16_8_;
      local_a8[9] = uVar7;
      local_a8[8] = uVar6;
      local_a8[0xb] = uVar5;
      local_a8[10] = uVar4;
      uVar7 = _UNK_00105168;
      uVar6 = _DAT_00105160;
      uVar5 = _UNK_001050d8;
      uVar4 = _DAT_001050d0;
      local_a8[0xd] = uVar3;
      local_a8[0xc] = uVar2;
      local_a8[0xf] = uVar5;
      local_a8[0xe] = uVar4;
      uVar5 = _UNK_00105148;
      uVar4 = _DAT_00105140;
      uVar3 = _UNK_001050b8;
      uVar2 = _DAT_001050b0;
      local_a8[0x11] = uVar7;
      local_a8[0x10] = uVar6;
      local_a8[0x13] = uVar3;
      local_a8[0x12] = uVar2;
      uVar7 = _UNK_00105158;
      uVar6 = _DAT_00105150;
      uVar3 = _UNK_00105138;
      uVar2 = _DAT_00105130;
      local_a8[0x15] = uVar5;
      local_a8[0x14] = uVar4;
      local_a8[0x17] = uVar3;
      local_a8[0x16] = uVar2;
      uVar5 = _UNK_001050e8;
      uVar4 = _DAT_001050e0;
      uVar3 = _UNK_001050c8;
      uVar2 = _DAT_001050c0;
      local_a8[0x19] = uVar7;
      local_a8[0x18] = uVar6;
      local_a8[0x1b] = uVar5;
      local_a8[0x1a] = uVar4;
      uVar5 = _UNK_00105118;
      uVar4 = _DAT_00105110;
      local_a8[0x1d] = uVar3;
      local_a8[0x1c] = uVar2;
      local_a8[0x1f] = uVar5;
      local_a8[0x1e] = uVar4;
    }
    else {
      lVar9 = 0;
      do {
        if (param_6 < lVar9) {
          if (lVar9 < param_7) {
            uVar11 = (uint)(((float)((int)lVar9 - param_6) / (float)(param_7 - param_6)) * 255.0 +
                           0.5);
            uVar11 = uVar11 & ((int)uVar11 >> 0x1f ^ 0xffffffffU);
            if (0xfe < uVar11) {
              uVar11 = 0xff;
            }
            uVar10 = (undefined1)uVar11;
          }
          else {
            uVar10 = 0xff;
          }
        }
        else {
          uVar10 = 0;
        }
        *(undefined1 *)((long)local_a8 + lVar9) = uVar10;
        lVar9 = lVar9 + 1;
      } while (lVar9 != 0x100);
    }
    local_d0[0] = 0x1010000;
    local_e8[0] = 0x1010000;
    local_e0 = aMStack_b8;
    local_c0 = 0;
    local_d8 = 0;
    local_100[0] = 0x2010000;
    uStack_f0 = 0;
    local_f8 = param_5;
    local_c8 = param_4;
    cv::LUT((_InputArray *)local_d0,(_InputArray *)local_e8,(_OutputArray *)local_100);
    if (param_8 == 2) {
      local_c0 = 0;
      local_d0[0] = 0x1010000;
      local_e8[0] = 0x2010000;
      local_d8 = 0;
      local_e0 = param_5;
      local_c8 = param_5;
      cv::cvtColor((cv *)local_d0,local_e8,6,0,0);
    }
    cv::Mat::~Mat(aMStack_b8);
  }
  if (*(long *)(lVar1 + 0x28) == local_58) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

