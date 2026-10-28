
/* std::__ndk1::vector<cv::Point_<int>, std::__ndk1::allocator<cv::Point_<int> > >*
   std::__ndk1::vector<std::__ndk1::vector<cv::Point_<int>, std::__ndk1::allocator<cv::Point_<int> >
   >, std::__ndk1::allocator<std::__ndk1::vector<cv::Point_<int>,
   std::__ndk1::allocator<cv::Point_<int> > > >
   >::__push_back_slow_path<std::__ndk1::vector<cv::Point_<int>,
   std::__ndk1::allocator<cv::Point_<int> > > const&>(std::__ndk1::vector<cv::Point_<int>,
   std::__ndk1::allocator<cv::Point_<int> > > const&) */

vector * __thiscall
std::__ndk1::
vector<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>,std::__ndk1::allocator<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>>>
::
__push_back_slow_path<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>const&>
          (vector<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>,std::__ndk1::allocator<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>>>
           *this,vector *param_1)

{
  ulong uVar1;
  void *__src;
  long lVar2;
  vector *pvVar3;
  void *pvVar4;
  void *__dest;
  undefined8 uVar5;
  undefined8 *puVar6;
  undefined8 *puVar7;
  long lVar8;
  ulong uVar9;
  long lVar10;
  long lVar11;
  undefined8 *puVar12;
  vector *pvVar13;
  undefined8 *puVar14;
  void *pvVar15;
  void *local_90;
  vector *local_88;
  vector *local_80;
  void *local_78;
  vector<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>,std::__ndk1::allocator<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>>>
  *local_70;
  long local_68;
  
  lVar2 = tpidr_el0;
  local_68 = *(long *)(lVar2 + 0x28);
  puVar14 = *(undefined8 **)this;
  puVar12 = *(undefined8 **)(this + 8);
  lVar11 = (long)puVar12 - (long)puVar14;
  lVar10 = lVar11 >> 3;
  uVar1 = lVar10 * -0x5555555555555555 + 1;
  if (uVar1 < 0xaaaaaaaaaaaaaab) {
    local_70 = this + 0x10;
    lVar8 = *(long *)local_70 - (long)puVar14 >> 3;
    uVar9 = lVar8 * 0x5555555555555556;
    if (uVar9 < uVar1 || uVar9 - uVar1 == 0) {
      uVar9 = uVar1;
    }
    if (0x555555555555554 < (ulong)(lVar8 * -0x5555555555555555)) {
      uVar9 = 0xaaaaaaaaaaaaaaa;
    }
    if (uVar9 == 0) {
      pvVar4 = (void *)0x0;
    }
    else {
      if (0xaaaaaaaaaaaaaaa < uVar9) goto LAB_00118360;
      pvVar4 = operator_new(uVar9 * 0x18);
    }
    pvVar13 = (vector *)((long)pvVar4 + lVar10 * 8);
    pvVar15 = (void *)((long)pvVar4 + uVar9 * 0x18);
    __src = *(void **)param_1;
    uVar1 = *(long *)(param_1 + 8) - (long)__src;
    *(undefined8 *)(pvVar13 + 8) = 0;
    *(undefined8 *)(pvVar13 + 0x10) = 0;
    *(undefined8 *)pvVar13 = 0;
    local_90 = pvVar4;
    local_88 = pvVar13;
    local_78 = pvVar15;
    if (uVar1 != 0) {
      local_80 = pvVar13;
      if ((long)uVar1 < 0) {
        if (*(long *)(lVar2 + 0x28) == local_68) {
          uVar5 = FUN_00117b34(pvVar13);
          pvVar4 = *(void **)pvVar13;
          if (pvVar4 != (void *)0x0) {
            *(void **)(pvVar13 + 8) = pvVar4;
            operator_delete(pvVar4);
          }
          FUN_001183cc(&local_90);
          if (*(long *)(lVar2 + 0x28) == local_68) {
                    /* WARNING: Subroutine does not return */
            FUN_0011cac8(uVar5);
          }
        }
        goto LAB_001183c8;
      }
      __dest = operator_new(uVar1);
      *(void **)pvVar13 = __dest;
      *(void **)(pvVar13 + 8) = __dest;
      *(ulong *)(pvVar13 + 0x10) = (long)__dest + uVar1;
      memcpy(__dest,__src,uVar1);
      *(ulong *)(pvVar13 + 8) = (long)__dest + uVar1;
    }
    local_80 = pvVar13 + 0x18;
    if (puVar14 != puVar12) {
      puVar6 = (undefined8 *)((long)pvVar4 + 0x10);
      puVar7 = puVar14;
      do {
        *puVar6 = 0;
        uVar5 = *puVar7;
        puVar6[-1] = puVar7[1];
        puVar6[-2] = uVar5;
        *puVar6 = puVar7[2];
        *puVar7 = 0;
        puVar7[1] = 0;
        puVar7[2] = 0;
        puVar7 = puVar7 + 3;
        puVar6 = puVar6 + 3;
      } while (puVar7 != puVar12);
      do {
        pvVar4 = (void *)*puVar14;
        if (pvVar4 != (void *)0x0) {
          puVar14[1] = pvVar4;
          operator_delete(pvVar4);
        }
        puVar14 = puVar14 + 3;
      } while (puVar14 != puVar12);
      puVar12 = *(undefined8 **)this;
      pvVar15 = local_78;
    }
    pvVar3 = local_80;
    *(long *)this = (long)pvVar13 - lVar11;
    *(vector **)(this + 8) = local_80;
    *(void **)(this + 0x10) = pvVar15;
    if (puVar12 != (undefined8 *)0x0) {
      operator_delete(puVar12);
    }
    if (*(long *)(lVar2 + 0x28) == local_68) {
      return pvVar3;
    }
  }
  else {
    if (*(long *)(lVar2 + 0x28) != local_68) goto LAB_001183c8;
    FUN_0011843c(this);
LAB_00118360:
    if (*(long *)(lVar2 + 0x28) == local_68) {
                    /* WARNING: Subroutine does not return */
      FUN_0010b4cc();
    }
  }
LAB_001183c8:
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

