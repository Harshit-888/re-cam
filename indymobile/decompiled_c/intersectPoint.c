
/* MathUtil::intersectPoint(cv::Vec<float, 2> const&, cv::Vec<float, 2> const&, cv::Point_<float>&)
    */

undefined8 MathUtil::intersectPoint(Vec *param_1,Vec *param_2,Point_ *param_3)

{
  float fVar1;
  float fVar2;
  float fVar3;
  float fVar4;
  float fVar5;
  
  fVar2 = *(float *)param_1;
  fVar1 = *(float *)(param_1 + 4);
  fVar4 = *(float *)param_2;
  fVar3 = *(float *)(param_2 + 4);
  if (NAN(fVar2)) {
    if (NAN(fVar4)) {
      return 0;
    }
    *(float *)param_3 = fVar1;
    *(float *)(param_3 + 4) = fVar3 + fVar1 * fVar4;
    return 1;
  }
  if (NAN(fVar4)) {
    fVar1 = fVar1 + fVar3 * fVar2;
    *(float *)param_3 = fVar3;
  }
  else {
    if (fVar2 - fVar4 == 0.0) {
      return 0;
    }
    fVar5 = (fVar3 - fVar1) / (fVar2 - fVar4);
    *(float *)param_3 = fVar5;
    if (1.0 <= ABS(fVar2)) {
      fVar1 = fVar3 + fVar5 * fVar4;
    }
    else {
      fVar1 = fVar1 + fVar5 * fVar2;
    }
  }
  *(float *)(param_3 + 4) = fVar1;
  return 1;
}

