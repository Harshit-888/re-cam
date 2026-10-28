
/* MathUtil::polarToCartesianLineEquation(cv::Vec<float, 2> const&, cv::Vec<float, 2>&) */

void MathUtil::polarToCartesianLineEquation(Vec *param_1,Vec *param_2)

{
  float fVar1;
  float fVar2;
  float fVar3;
  float fVar4;
  float local_28;
  float fStack_24;
  
  fVar4 = *(float *)param_1;
  sincosf(*(float *)(param_1 + 4),&fStack_24,&local_28);
  fVar3 = fVar4 * local_28;
  fVar2 = fVar3 - fStack_24 * 1000.0;
  fVar3 = (fVar3 + fStack_24 * 1000.0) - fVar2;
  if (fVar3 == 0.0) {
    fVar3 = NAN;
  }
  else {
    fVar4 = fVar4 * fStack_24;
    fVar1 = fVar4 + local_28 * 1000.0;
    fVar3 = ((fVar4 + local_28 * -1000.0) - fVar1) / fVar3;
    fVar2 = fVar1 - fVar2 * fVar3;
  }
  *(float *)param_2 = fVar3;
  *(float *)(param_2 + 4) = fVar2;
  return;
}

