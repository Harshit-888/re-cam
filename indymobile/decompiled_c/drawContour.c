
/* drawContour(std::__ndk1::vector<cv::Point_<int>, std::__ndk1::allocator<cv::Point_<int> > >,
   cv::Mat) */

void drawContour(vector *param_1,Mat *param_2)

{
  ulong uVar1;
  long lVar2;
  undefined8 *puVar3;
  vector *pvVar4;
  undefined8 *puVar5;
  vector *pvVar6;
  vector *pvVar7;
  void *pvVar8;
  undefined8 uVar9;
  undefined8 *local_f8;
  undefined8 *puVar10;
  long local_e8;
  vector *local_e0;
  vector *local_d8;
  undefined8 local_d0;
  undefined8 **local_c8;
  undefined1 local_c0;
  Mat aMStack_b8 [96];
  long local_58;
  
  lVar2 = tpidr_el0;
  local_58 = *(long *)(lVar2 + 0x28);
  local_e0 = (vector *)0x0;
  local_d8 = (vector *)0x0;
  local_d0 = 0;
  pvVar7 = std::__ndk1::
           vector<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>,std::__ndk1::allocator<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>>>
           ::
           __push_back_slow_path<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>const&>
                     ((vector<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>,std::__ndk1::allocator<std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>>>
                       *)&local_e0,param_1);
  pvVar6 = local_e0;
  local_c8 = &local_f8;
  uVar1 = (long)pvVar7 - (long)local_e0;
  puVar10 = (undefined8 *)0x0;
  local_e8 = 0;
  local_f8 = (undefined8 *)0x0;
  local_c0 = 0;
  local_d8 = pvVar7;
  if (uVar1 != 0) {
    if (0xaaaaaaaaaaaaaaa < (ulong)(((long)uVar1 >> 3) * -0x5555555555555555)) {
      if (*(long *)(lVar2 + 0x28) == local_58) {
        uVar9 = FUN_0011843c(&local_f8);
        FUN_001184b0(&local_c8);
        FUN_00114ba4(&local_e0);
        if (*(long *)(lVar2 + 0x28) == local_58) {
                    /* WARNING: Subroutine does not return */
          FUN_0011cac8(uVar9);
        }
      }
      goto LAB_00114ba0;
    }
    local_f8 = operator_new(uVar1);
    local_e8 = (long)local_f8 + uVar1;
    puVar10 = (undefined8 *)FUN_0011853c(&local_e8,pvVar6,pvVar7,local_f8);
  }
  cv::Mat::Mat(aMStack_b8,param_2);
  drawContours(&local_f8,aMStack_b8);
  cv::Mat::~Mat(aMStack_b8);
  puVar5 = local_f8;
  if (local_f8 != (undefined8 *)0x0) {
    while (puVar3 = puVar10, puVar3 != puVar5) {
      puVar10 = puVar3 + -3;
      pvVar8 = (void *)*puVar10;
      if (pvVar8 != (void *)0x0) {
        puVar3[-2] = pvVar8;
        operator_delete(pvVar8);
      }
    }
    operator_delete(local_f8);
  }
  pvVar6 = local_e0;
  pvVar7 = local_d8;
  if (local_e0 != (vector *)0x0) {
    while (pvVar4 = pvVar7, pvVar4 != pvVar6) {
      pvVar7 = pvVar4 + -0x18;
      pvVar8 = *(void **)pvVar7;
      if (pvVar8 != (void *)0x0) {
        *(void **)(pvVar4 + -0x10) = pvVar8;
        operator_delete(pvVar8);
      }
    }
    local_d8 = pvVar6;
    operator_delete(local_e0);
  }
  if (*(long *)(lVar2 + 0x28) == local_58) {
    return;
  }
LAB_00114ba0:
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

