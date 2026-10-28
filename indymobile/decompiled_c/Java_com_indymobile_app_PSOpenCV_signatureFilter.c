
undefined8
Java_com_indymobile_app_PSOpenCV_signatureFilter
          (_JNIEnv *param_1,undefined8 param_2,_jobject *param_3,undefined8 param_4,int param_5,
          int param_6,int param_7)

{
  uint uVar1;
  uint uVar2;
  ulong uVar3;
  undefined1 (*pauVar4) [16];
  undefined8 uVar5;
  ulong uVar6;
  long lVar7;
  ulong uVar8;
  uint *puVar9;
  undefined1 (*pauVar10) [16];
  uint uVar11;
  ulong uVar12;
  undefined1 auVar13 [16];
  
  uVar3 = OpenCVUtil::checkAuthentication(param_1,param_3);
  if ((uVar3 & 1) == 0) {
    return 0;
  }
  pauVar4 = (undefined1 (*) [16])(**(code **)(*(long *)param_1 + 0x5d8))(param_1,param_4,0);
  if (pauVar4 == (undefined1 (*) [16])0x0) {
    return 0;
  }
  uVar2 = param_6 * param_5;
  uVar3 = (ulong)uVar2;
  if (0 < (int)uVar2) {
    if (uVar2 < 4) {
      uVar6 = 0;
    }
    else {
      uVar6 = uVar3 & 0x7ffffffc;
      uVar8 = uVar6;
      pauVar10 = pauVar4;
      do {
        auVar13 = *pauVar10;
        uVar8 = uVar8 - 4;
        uVar12 = CONCAT44(auVar13._4_4_ >> 0x10,auVar13._0_4_ >> 0x10) & 0xffff00ffffff00ff;
        auVar13 = NEON_umull(CONCAT26((ushort)auVar13[0xd] + (ushort)auVar13[0xc] +
                                      (auVar13._14_2_ & 0xff),
                                      CONCAT24((ushort)auVar13[9] + (ushort)auVar13[8] +
                                               (auVar13._10_2_ & 0xff),
                                               CONCAT22((ushort)auVar13[5] + (ushort)auVar13[4] +
                                                        (short)(uVar12 >> 0x20),
                                                        (ushort)auVar13[1] + (ushort)auVar13[0] +
                                                        (short)uVar12))),0xaaabaaabaaabaaab,2);
        (*pauVar10)[8] = 0;
        (*pauVar10)[9] = 0;
        (*pauVar10)[10] = 0;
        (*pauVar10)[0xb] = (char)(-(uint)((int)(auVar13._8_4_ >> 0x11) < param_7) >> 0x18);
        (*pauVar10)[0xc] = 0;
        (*pauVar10)[0xd] = 0;
        (*pauVar10)[0xe] = 0;
        (*pauVar10)[0xf] = (char)(-(uint)((int)(auVar13._12_4_ >> 0x11) < param_7) >> 0x18);
        (*pauVar10)[0] = 0;
        (*pauVar10)[1] = 0;
        (*pauVar10)[2] = 0;
        (*pauVar10)[3] = (char)(-(uint)((int)(auVar13._0_4_ >> 0x11) < param_7) >> 0x18);
        (*pauVar10)[4] = 0;
        (*pauVar10)[5] = 0;
        (*pauVar10)[6] = 0;
        (*pauVar10)[7] = (char)(-(uint)((int)(auVar13._4_4_ >> 0x11) < param_7) >> 0x18);
        pauVar10 = pauVar10 + 1;
      } while (uVar8 != 0);
      if (uVar6 == uVar3) goto LAB_0010f950;
    }
    lVar7 = uVar3 - uVar6;
    puVar9 = (uint *)(*pauVar4 + uVar6 * 4);
    do {
      uVar1 = *puVar9;
      uVar11 = 0xff000000;
      if (param_7 <= (int)(((uVar1 >> 8 & 0xff) + (uVar1 & 0xff) + (uVar1 >> 0x10 & 0xff)) / 3)) {
        uVar11 = 0;
      }
      lVar7 = lVar7 + -1;
      *puVar9 = uVar11;
      puVar9 = puVar9 + 1;
    } while (lVar7 != 0);
  }
LAB_0010f950:
  uVar5 = (**(code **)(*(long *)param_1 + 0x598))(param_1,uVar2);
  (**(code **)(*(long *)param_1 + 0x698))(param_1,uVar5,0,uVar2,pauVar4);
  (**(code **)(*(long *)param_1 + 0x618))(param_1,param_4,pauVar4,0);
  return uVar5;
}

