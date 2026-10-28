
/* WARNING: Globals starting with '_' overlap smaller symbols at the same address */
/* getAngleABC(cv::Point_<int>, cv::Point_<int>, cv::Point_<int>) */

int getAngleABC(undefined8 param_1,undefined8 param_2,undefined8 param_3)

{
  int iVar1;
  int iVar2;
  int iVar3;
  int iVar4;
  float fVar5;
  
  iVar4 = (int)((ulong)param_2 >> 0x20);
  iVar1 = (int)param_2 - (int)param_1;
  iVar2 = (int)param_2 - (int)param_3;
  iVar3 = iVar4 - (int)((ulong)param_1 >> 0x20);
  iVar4 = iVar4 - (int)((ulong)param_3 >> 0x20);
  fVar5 = atan2f((float)(iVar4 * iVar1 - iVar3 * iVar2),(float)(iVar2 * iVar1 + iVar4 * iVar3));
  return (int)(((double)fVar5 * 180.0) / _DAT_00104558 + 0.5);
}

