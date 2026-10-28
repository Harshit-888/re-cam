
/* OpenCVUtil::findContoursGray(cv::Mat&, std::__ndk1::vector<cv::Point_<int>,
   std::__ndk1::allocator<cv::Point_<int> > >&) */

undefined8 OpenCVUtil::findContoursGray(Mat *param_1,vector *param_2)

{
  long lVar1;
  bool bVar2;
  undefined8 *puVar3;
  contour_info *pcVar4;
  undefined8 *puVar5;
  contour_info *pcVar6;
  bool bVar7;
  Mat *this;
  void *pvVar8;
  undefined8 *puVar9;
  contour_info *pcVar10;
  undefined8 *local_3a0;
  undefined8 *local_398;
  undefined8 local_390;
  undefined4 local_388 [2];
  undefined4 *local_380;
  undefined8 local_378;
  undefined4 local_370 [2];
  Mat *local_368;
  undefined8 uStack_360;
  undefined4 local_358 [2];
  Mat *local_350;
  undefined8 local_348;
  undefined8 *local_340;
  undefined8 *local_338;
  undefined8 local_330;
  contour_info *local_328;
  contour_info *local_320;
  undefined8 local_318;
  Mat aMStack_310 [96];
  undefined4 local_2b0;
  undefined4 uStack_2ac;
  Mat *local_2a8;
  undefined8 auStack_2a0 [12];
  Mat aMStack_240 [96];
  Mat aMStack_1e0 [144];
  undefined8 local_150;
  Mat *pMStack_148;
  undefined8 uStack_140;
  undefined8 uStack_138;
  Mat aMStack_130 [96];
  Mat aMStack_d0 [96];
  long local_70;
  
  lVar1 = tpidr_el0;
  local_70 = *(long *)(lVar1 + 0x28);
  cv::Mat::Mat(aMStack_d0);
  cv::Mat::Mat(aMStack_130);
  auStack_2a0[0] = 0;
  local_2b0 = 0x1010000;
  local_150._0_4_ = 0x2010000;
  uStack_140 = 0;
  local_2a8 = param_1;
  pMStack_148 = aMStack_d0;
  cv::medianBlur((_InputArray *)&local_2b0,(_OutputArray *)&local_150,9);
  auStack_2a0[0] = 0;
  local_2b0 = 0x1010000;
  local_150 = CONCAT44(local_150._4_4_,0x2010000);
  uStack_140 = 0;
  local_2a8 = aMStack_d0;
  pMStack_148 = aMStack_d0;
  cv::cvtColor((cv *)&local_2b0,&local_150,6,0,0);
  local_328 = (contour_info *)0x0;
  local_320 = (contour_info *)0x0;
  local_318 = 0;
  local_340 = (undefined8 *)0x0;
  local_338 = (undefined8 *)0x0;
  local_330 = 0;
  bVar7 = true;
  do {
    bVar2 = bVar7;
    if (bVar2) {
      auStack_2a0[0] = 0;
      local_2b0 = 0x1010000;
      local_150 = CONCAT44(local_150._4_4_,0x2010000);
      uStack_140 = 0;
      local_2a8 = aMStack_d0;
      pMStack_148 = aMStack_130;
      cv::Canny((_InputArray *)&local_2b0,(_OutputArray *)&local_150,10.0,20.0,3,false);
      local_358[0] = 0x1010000;
      local_370[0] = 0x2010000;
      uStack_360 = 0;
      local_348 = 0;
      local_368 = aMStack_130;
      local_350 = aMStack_130;
      cv::Mat::Mat((Mat *)&local_2b0);
      local_388[0] = 0x1010000;
      local_378 = 0;
      pMStack_148 = (Mat *)0x7fefffffffffffff;
      local_150 = 0x7fefffffffffffff;
      uStack_138 = 0x7fefffffffffffff;
      uStack_140 = 0x7fefffffffffffff;
      local_380 = &local_2b0;
      cv::dilate(local_358,local_370,local_388,0xffffffffffffffff,1,0,&local_150);
      this = (Mat *)&local_2b0;
    }
    else {
      cv::operator>=(aMStack_d0,255.0);
      (**(code **)(*(long *)CONCAT44(uStack_2ac,local_2b0) + 0x18))
                ((long *)CONCAT44(uStack_2ac,local_2b0),&local_2b0,aMStack_130,0xffffffff);
      cv::Mat::~Mat(aMStack_1e0);
      cv::Mat::~Mat(aMStack_240);
      this = (Mat *)auStack_2a0;
    }
    cv::Mat::~Mat(this);
    auStack_2a0[0] = 0;
    local_2b0 = 0x1010000;
    local_150 = CONCAT44(local_150._4_4_,0x8204000c);
    uStack_140 = 0;
    local_2a8 = aMStack_130;
    pMStack_148 = (Mat *)&local_340;
    cv::findContours(&local_2b0,&local_150,1,2,0);
    filterRectInContours
              (CONCAT44(**(undefined4 **)(param_1 + 0x40),(*(undefined4 **)(param_1 + 0x40))[1]),
               &local_340,&local_328);
    bVar7 = false;
  } while (bVar2);
  local_3a0 = (undefined8 *)0x0;
  local_398 = (undefined8 *)0x0;
  local_390 = 0;
  std::__ndk1::vector<contour_info,std::__ndk1::allocator<contour_info>>::
  __init_with_size_abi_ne190000_<contour_info*,contour_info*>
            ((vector<contour_info,std::__ndk1::allocator<contour_info>> *)&local_3a0,local_328,
             local_320,(long)local_320 - (long)local_328 >> 6);
  cv::Mat::Mat(aMStack_310,param_1);
  drawContoursInfo(&local_3a0,aMStack_310);
  cv::Mat::~Mat(aMStack_310);
  puVar5 = local_3a0;
  puVar9 = local_398;
  if (local_3a0 != (undefined8 *)0x0) {
    while (puVar3 = puVar9, puVar3 != puVar5) {
      pvVar8 = (void *)puVar3[-5];
      if (pvVar8 != (void *)0x0) {
        puVar3[-4] = pvVar8;
        operator_delete(pvVar8);
      }
      puVar9 = puVar3 + -8;
      pvVar8 = (void *)*puVar9;
      if (pvVar8 != (void *)0x0) {
        puVar3[-7] = pvVar8;
        operator_delete(pvVar8);
      }
    }
    local_398 = puVar5;
    operator_delete(local_3a0);
  }
  puVar5 = local_340;
  puVar9 = local_338;
  if (local_340 != (undefined8 *)0x0) {
    while (puVar3 = puVar9, puVar3 != puVar5) {
      puVar9 = puVar3 + -3;
      pvVar8 = (void *)*puVar9;
      if (pvVar8 != (void *)0x0) {
        puVar3[-2] = pvVar8;
        operator_delete(pvVar8);
      }
    }
    local_338 = puVar5;
    operator_delete(local_340);
  }
  pcVar6 = local_328;
  pcVar10 = local_320;
  if (local_328 != (contour_info *)0x0) {
    while (pcVar4 = pcVar10, pcVar4 != pcVar6) {
      pvVar8 = *(void **)(pcVar4 + -0x28);
      if (pvVar8 != (void *)0x0) {
        *(void **)(pcVar4 + -0x20) = pvVar8;
        operator_delete(pvVar8);
      }
      pcVar10 = pcVar4 + -0x40;
      pvVar8 = *(void **)pcVar10;
      if (pvVar8 != (void *)0x0) {
        *(void **)(pcVar4 + -0x38) = pvVar8;
        operator_delete(pvVar8);
      }
    }
    local_320 = pcVar6;
    operator_delete(local_328);
  }
  cv::Mat::~Mat(aMStack_130);
  cv::Mat::~Mat(aMStack_d0);
  if (*(long *)(lVar1 + 0x28) != local_70) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail();
  }
  return 0;
}

