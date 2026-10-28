
/* OpenCVUtil::findContoursYCrCb(cv::Mat&, std::__ndk1::vector<cv::Point_<int>,
   std::__ndk1::allocator<cv::Point_<int> > >&) */

undefined8 OpenCVUtil::findContoursYCrCb(Mat *param_1,vector *param_2)

{
  long lVar1;
  undefined8 *puVar2;
  contour_info *pcVar3;
  undefined8 *puVar4;
  contour_info *pcVar5;
  void *pvVar6;
  undefined8 *puVar7;
  contour_info *pcVar8;
  int iVar9;
  undefined8 *local_418;
  undefined8 *local_410;
  undefined8 local_408;
  undefined4 local_400 [2];
  Mat *local_3f8;
  undefined8 uStack_3f0;
  undefined8 *local_3e8;
  undefined8 *local_3e0;
  undefined8 local_3d8;
  contour_info *local_3d0;
  contour_info *local_3c8;
  undefined8 local_3c0;
  Mat aMStack_3b8 [96];
  undefined4 local_358;
  undefined4 uStack_354;
  Mat *local_350;
  undefined8 auStack_348 [12];
  Mat aMStack_2e8 [96];
  Mat aMStack_288 [144];
  int local_1f8;
  undefined4 local_1f4;
  Mat aMStack_1f0 [96];
  undefined4 local_190 [2];
  Mat *local_188;
  undefined8 local_180;
  Mat aMStack_130 [96];
  Mat aMStack_d0 [64];
  undefined4 *local_90;
  long local_70;
  
  lVar1 = tpidr_el0;
  local_70 = *(long *)(lVar1 + 0x28);
  cv::Mat::Mat(aMStack_d0);
  cv::Mat::Mat(aMStack_130);
  auStack_348[0] = 0;
  local_358 = 0x1010000;
  local_190[0] = 0x2010000;
  local_180 = 0;
  local_350 = param_1;
  local_188 = aMStack_d0;
  cv::medianBlur((_InputArray *)&local_358,(_OutputArray *)local_190,9);
  auStack_348[0] = 0;
  local_358 = 0x1010000;
  local_190[0] = 0x2010000;
  local_180 = 0;
  local_350 = aMStack_d0;
  local_188 = aMStack_130;
  cv::cvtColor((cv *)&local_358,local_190,0x24,0,0);
  cv::Mat::Mat((Mat *)local_190,CONCAT44(*local_90,local_90[1]),0);
  cv::Mat::Mat(aMStack_1f0);
  iVar9 = 0;
  local_3d0 = (contour_info *)0x0;
  local_3c8 = (contour_info *)0x0;
  local_3c0 = 0;
  local_3e8 = (undefined8 *)0x0;
  local_3e0 = (undefined8 *)0x0;
  local_3d8 = 0;
  do {
    local_1f4 = 0;
    local_1f8 = iVar9;
    cv::mixChannels(aMStack_130,1,(Mat *)local_190,1,&local_1f8,1);
    auStack_348[0] = 0;
    local_358 = 0x1010000;
    local_400[0] = 0x2010000;
    uStack_3f0 = 0;
    local_3f8 = aMStack_1f0;
    local_350 = (Mat *)local_190;
    cv::Canny((_InputArray *)&local_358,(_OutputArray *)local_400,10.0,20.0,3,false);
    auStack_348[0] = 0;
    local_358 = 0x1010000;
    local_400[0] = 0x8204000c;
    uStack_3f0 = 0;
    local_3f8 = (Mat *)&local_3e8;
    local_350 = aMStack_1f0;
    cv::findContours(&local_358,local_400,1,2,0);
    filterRectInContours
              (CONCAT44(**(undefined4 **)(param_1 + 0x40),(*(undefined4 **)(param_1 + 0x40))[1]),
               &local_3e8,&local_3d0);
    cv::operator>=((Mat *)local_190,255.0);
    (**(code **)(*(long *)CONCAT44(uStack_354,local_358) + 0x18))
              ((long *)CONCAT44(uStack_354,local_358),&local_358,aMStack_1f0,0xffffffff);
    cv::Mat::~Mat(aMStack_288);
    cv::Mat::~Mat(aMStack_2e8);
    cv::Mat::~Mat((Mat *)auStack_348);
    auStack_348[0] = 0;
    local_358 = 0x1010000;
    local_400[0] = 0x8204000c;
    uStack_3f0 = 0;
    local_3f8 = (Mat *)&local_3e8;
    local_350 = aMStack_1f0;
    cv::findContours(&local_358,local_400,1,2,0);
    filterRectInContours
              (CONCAT44(**(undefined4 **)(param_1 + 0x40),(*(undefined4 **)(param_1 + 0x40))[1]),
               &local_3e8,&local_3d0);
    iVar9 = iVar9 + 1;
  } while (iVar9 != 3);
  local_418 = (undefined8 *)0x0;
  local_410 = (undefined8 *)0x0;
  local_408 = 0;
  std::__ndk1::vector<contour_info,std::__ndk1::allocator<contour_info>>::
  __init_with_size_abi_ne190000_<contour_info*,contour_info*>
            ((vector<contour_info,std::__ndk1::allocator<contour_info>> *)&local_418,local_3d0,
             local_3c8,(long)local_3c8 - (long)local_3d0 >> 6);
  cv::Mat::Mat(aMStack_3b8,param_1);
  drawContoursInfo(&local_418,aMStack_3b8);
  cv::Mat::~Mat(aMStack_3b8);
  puVar4 = local_418;
  puVar7 = local_410;
  if (local_418 != (undefined8 *)0x0) {
    while (puVar2 = puVar7, puVar2 != puVar4) {
      pvVar6 = (void *)puVar2[-5];
      if (pvVar6 != (void *)0x0) {
        puVar2[-4] = pvVar6;
        operator_delete(pvVar6);
      }
      puVar7 = puVar2 + -8;
      pvVar6 = (void *)*puVar7;
      if (pvVar6 != (void *)0x0) {
        puVar2[-7] = pvVar6;
        operator_delete(pvVar6);
      }
    }
    local_410 = puVar4;
    operator_delete(local_418);
  }
  puVar4 = local_3e8;
  puVar7 = local_3e0;
  if (local_3e8 != (undefined8 *)0x0) {
    while (puVar2 = puVar7, puVar2 != puVar4) {
      puVar7 = puVar2 + -3;
      pvVar6 = (void *)*puVar7;
      if (pvVar6 != (void *)0x0) {
        puVar2[-2] = pvVar6;
        operator_delete(pvVar6);
      }
    }
    local_3e0 = puVar4;
    operator_delete(local_3e8);
  }
  pcVar5 = local_3d0;
  pcVar8 = local_3c8;
  if (local_3d0 != (contour_info *)0x0) {
    while (pcVar3 = pcVar8, pcVar3 != pcVar5) {
      pvVar6 = *(void **)(pcVar3 + -0x28);
      if (pvVar6 != (void *)0x0) {
        *(void **)(pcVar3 + -0x20) = pvVar6;
        operator_delete(pvVar6);
      }
      pcVar8 = pcVar3 + -0x40;
      pvVar6 = *(void **)pcVar8;
      if (pvVar6 != (void *)0x0) {
        *(void **)(pcVar3 + -0x38) = pvVar6;
        operator_delete(pvVar6);
      }
    }
    local_3c8 = pcVar5;
    operator_delete(local_3d0);
  }
  cv::Mat::~Mat(aMStack_1f0);
  cv::Mat::~Mat((Mat *)local_190);
  cv::Mat::~Mat(aMStack_130);
  cv::Mat::~Mat(aMStack_d0);
  if (*(long *)(lVar1 + 0x28) != local_70) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail();
  }
  return 0;
}

