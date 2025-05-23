/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */


public class cFileMappingDroneCharTelemetryServeur {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected cFileMappingDroneCharTelemetryServeur(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(cFileMappingDroneCharTelemetryServeur obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(cFileMappingDroneCharTelemetryServeur obj) {
    long ptr = 0;
    if (obj != null) {
      if (!obj.swigCMemOwn)
        throw new RuntimeException("Cannot release ownership as memory is not owned");
      ptr = obj.swigCPtr;
      obj.swigCMemOwn = false;
      obj.delete();
    }
    return ptr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        JNIFileMappingDroneCharTelemetryServeurJNI.delete_cFileMappingDroneCharTelemetryServeur(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public cFileMappingDroneCharTelemetryServeur(boolean _DebugFlag) {
    this(JNIFileMappingDroneCharTelemetryServeurJNI.new_cFileMappingDroneCharTelemetryServeur(_DebugFlag), true);
  }

  public boolean OpenServer(String NameSharedMem) {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_OpenServer(swigCPtr, this, NameSharedMem);
  }

  public void CloseServer() {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_CloseServer(swigCPtr, this);
  }

  public cVirtualDroneCharTelemetry ReadMapFileToVirtualDroneCharTelemetryStruct(cVirtualDroneCharTelemetry Data) {
    long cPtr = JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_ReadMapFileToVirtualDroneCharTelemetryStruct(swigCPtr, this, cVirtualDroneCharTelemetry.getCPtr(Data), Data);
    return (cPtr == 0) ? null : new cVirtualDroneCharTelemetry(cPtr, false);
  }

  public boolean WriteVirtualDroneCharTelemetryStructToMapFile(cVirtualDroneCharTelemetry Data) {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_WriteVirtualDroneCharTelemetryStructToMapFile(swigCPtr, this, cVirtualDroneCharTelemetry.getCPtr(Data), Data);
  }

  public void PrintDebug(String msg, boolean _Return) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_PrintDebug(swigCPtr, this, msg, _Return);
  }

  public void PrintStruct(cVirtualDroneCharTelemetry Data) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_PrintStruct(swigCPtr, this, cVirtualDroneCharTelemetry.getCPtr(Data), Data);
  }

  public cVirtualDroneCharTelemetry getVirtualDroneCharTelemetryPtr() {
    long cPtr = JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryPtr(swigCPtr, this);
    return (cPtr == 0) ? null : new cVirtualDroneCharTelemetry(cPtr, false);
  }

  public boolean getDebugMode() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getDebugMode(swigCPtr, this);
  }

  public int getVirtualDroneCharTelemetryBatteryValue() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryBatteryValue(swigCPtr, this);
  }

  public String getVirtualDroneCharTelemetryDriveTime() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryDriveTime(swigCPtr, this);
  }

  public String getVirtualDroneCharTelemetryTempC() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryTempC(swigCPtr, this);
  }

  public String getVirtualDroneCharTelemetryTempF() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryTempF(swigCPtr, this);
  }

  public String getVirtualDroneCharTelemetryAltitude() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryAltitude(swigCPtr, this);
  }

  public double getVirtualDroneCharTelemetryAx() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryAx(swigCPtr, this);
  }

  public double getVirtualDroneCharTelemetryAy() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryAy(swigCPtr, this);
  }

  public double getVirtualDroneCharTelemetryAz() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryAz(swigCPtr, this);
  }

  public double getVirtualDroneCharTelemetryFrontDistance() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryFrontDistance(swigCPtr, this);
  }

  public double getVirtualDroneCharTelemetryBackDistance() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryBackDistance(swigCPtr, this);
  }

  public double getVirtualDroneCharTelemetryPressure() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryPressure(swigCPtr, this);
  }

  public boolean getVirtualDroneCharTelemetryMutexBlocAccess() {
    return JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_getVirtualDroneCharTelemetryMutexBlocAccess(swigCPtr, this);
  }

  public void setVirtualDroneCharTelemetryPtr(cVirtualDroneCharTelemetry VTStruct) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryPtr(swigCPtr, this, cVirtualDroneCharTelemetry.getCPtr(VTStruct), VTStruct);
  }

  public void setDebugMode(boolean _DebugMode) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setDebugMode(swigCPtr, this, _DebugMode);
  }

  public void setVirtualDroneCharTelemetryBatteryValue(int value) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryBatteryValue(swigCPtr, this, value);
  }

  public void setVirtualDroneCharTelemetryDriveTime(String DriveTime) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryDriveTime(swigCPtr, this, DriveTime);
  }

  public void setVirtualDroneCharTelemetryTempC(String TempC) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryTempC(swigCPtr, this, TempC);
  }

  public void setVirtualDroneCharTelemetryTempF(String TempF) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryTempF(swigCPtr, this, TempF);
  }

  public void setVirtualDroneCharTelemetryAltitude(String Altitude) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryAltitude(swigCPtr, this, Altitude);
  }

  public void setVirtualDroneCharTelemetryAx(double Ax) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryAx(swigCPtr, this, Ax);
  }

  public void setVirtualDroneCharTelemetryAy(double Ay) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryAy(swigCPtr, this, Ay);
  }

  public void setVirtualDroneCharTelemetryAz(double Az) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryAz(swigCPtr, this, Az);
  }

  public void setVirtualDroneCharTelemetryFrontDistance(double FrontDistance) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryFrontDistance(swigCPtr, this, FrontDistance);
  }

  public void setVirtualDroneCharTelemetryBackDistance(double BackDistance) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryBackDistance(swigCPtr, this, BackDistance);
  }

  public void setVirtualDroneCharTelemetryPressure(double Pressure) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryPressure(swigCPtr, this, Pressure);
  }

  public void setVirtualDroneCharTelemetryMutexBlocAccess(boolean blocaccess) {
    JNIFileMappingDroneCharTelemetryServeurJNI.cFileMappingDroneCharTelemetryServeur_setVirtualDroneCharTelemetryMutexBlocAccess(swigCPtr, this, blocaccess);
  }

}
