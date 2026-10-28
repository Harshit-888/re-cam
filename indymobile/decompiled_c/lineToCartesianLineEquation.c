
/* MathUtil::lineToCartesianLineEquation(cv::Point_<float> const&, cv::Point_<float> const&,
   cv::Vec<float, 2>&) */

void MathUtil::lineToCartesianLineEquation(Point_ *param_1,Point_ *param_2,Vec *param_3)

{
  float fVar1;
  float fVar2;
  float fVar3;
  
  fVar1 = *(float *)param_1;
  if (*(float *)param_2 - fVar1 == 0.0) {
    *(undefined4 *)param_3 = 0x7fc00000;
    *(float *)(param_3 + 4) = fVar1;
    return;
  }
  fVar3 = *(float *)(param_1 + 4);
  fVar2 = (*(float *)(param_2 + 4) - fVar3) / (*(float *)param_2 - fVar1);
  *(float *)param_3 = fVar2;
  *(float *)(param_3 + 4) = fVar3 - fVar1 * fVar2;
  return;
}

