
/* WARNING: Type propagation algorithm not settling */
/* WARNING: Globals starting with '_' overlap smaller symbols at the same address */
/* OpenCVUtil::drawLines(cv::Mat&, std::__ndk1::vector<cv::Vec<float, 2>,
   std::__ndk1::allocator<cv::Vec<float, 2> > >&) */

void OpenCVUtil::drawLines(Mat *param_1,vector *param_2)

{
  long lVar1;
  float fVar2;
  undefined8 uVar3;
  undefined8 uVar4;
  long lVar5;
  long lVar6;
  ulong uVar7;
  float fVar8;
  float local_a8;
  float afStack_a4 [3];
  Mat *local_98;
  undefined8 uStack_90;
  undefined8 local_88;
  undefined8 uStack_80;
  undefined8 local_78;
  undefined8 uStack_70;
  long local_68;
  
  uVar4 = _UNK_001050a8;
  uVar3 = _DAT_001050a0;
  lVar1 = tpidr_el0;
  local_68 = *(long *)(lVar1 + 0x28);
  lVar5 = *(long *)param_2;
  if (*(long *)(param_2 + 8) != lVar5) {
    lVar6 = 0;
    uVar7 = 0;
    do {
      fVar8 = *(float *)(lVar5 + lVar6);
      sincosf(((float *)(lVar5 + lVar6))[1],afStack_a4,&local_a8);
      afStack_a4[1] = 3.7909693e-37;
      uStack_90 = 0;
      local_88 = 0;
      uStack_80 = 0;
      fVar2 = fVar8 * local_a8;
      fVar8 = fVar8 * afStack_a4[0];
      uStack_70 = uVar4;
      local_78 = uVar3;
      local_98 = param_1;
      cv::line(afStack_a4 + 1,
               (long)(double)(long)(fVar2 + afStack_a4[0] * -1000.0) & 0xffffffffU |
               (long)(double)(long)(fVar8 + local_a8 * 1000.0) << 0x20,
               (long)(double)(long)(fVar2 + afStack_a4[0] * 1000.0) & 0xffffffffU |
               (long)(double)(long)(fVar8 + local_a8 * -1000.0) << 0x20,&local_88,1,8,0);
      lVar5 = *(long *)param_2;
      uVar7 = uVar7 + 1;
      lVar6 = lVar6 + 8;
    } while (uVar7 < (ulong)(*(long *)(param_2 + 8) - lVar5 >> 3));
  }
  if (*(long *)(lVar1 + 0x28) == local_68) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

