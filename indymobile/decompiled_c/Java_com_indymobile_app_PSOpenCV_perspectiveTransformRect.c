
void Java_com_indymobile_app_PSOpenCV_perspectiveTransformRect
               (float param_1,float param_2,float param_3,float param_4,float param_5,float param_6,
               float param_7,float param_8,_JNIEnv *param_9,undefined8 param_10,_jobject *param_11,
               undefined8 param_12,undefined8 param_13)

{
  ulong uVar1;
  undefined4 in_stack_00000000;
  
  uVar1 = OpenCVUtil::checkAuthentication(param_9,param_11);
  if ((uVar1 & 1) != 0) {
    OpenCVUtil::perspectiveTransformRect
              (in_stack_00000000,param_12,param_13,CONCAT44((int)param_2,(int)param_1),
               CONCAT44((int)param_4,(int)param_3),CONCAT44((int)param_6,(int)param_5),
               CONCAT44((int)param_8,(int)param_7));
    return;
  }
  return;
}

