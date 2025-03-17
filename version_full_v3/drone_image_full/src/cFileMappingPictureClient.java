


public class cFileMappingPictureClient {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected cFileMappingPictureClient(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(cFileMappingPictureClient obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(cFileMappingPictureClient obj) {
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
        JNIFileMappingPictureClientJNI.delete_cFileMappingPictureClient(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public cFileMappingPictureClient(boolean _DebugFlag) {
    this(JNIFileMappingPictureClientJNI.new_cFileMappingPictureClient(_DebugFlag), true);
  }

  public boolean OpenClient(String NameSharedMem) {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_OpenClient(swigCPtr, this, NameSharedMem);
  }

  public void CloseClient() {
    JNIFileMappingPictureClientJNI.cFileMappingPictureClient_CloseClient(swigCPtr, this);
  }

  public boolean FileToMapFile(String Name, cVirtualPicture Data) {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_FileToMapFile(swigCPtr, this, Name, cVirtualPicture.getCPtr(Data), Data);
  }

  public int MapFileToFile(String Name, cVirtualPicture Data) {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_MapFileToFile(swigCPtr, this, Name, cVirtualPicture.getCPtr(Data), Data);
  }

  public int ReadFileToVirtualPictureStruct(String Name, cVirtualPicture Data) {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_ReadFileToVirtualPictureStruct(swigCPtr, this, Name, cVirtualPicture.getCPtr(Data), Data);
  }

  public cVirtualPicture ReadMapFileToVirtualPictureStruct(cVirtualPicture Data) {
    long cPtr = JNIFileMappingPictureClientJNI.cFileMappingPictureClient_ReadMapFileToVirtualPictureStruct(swigCPtr, this, cVirtualPicture.getCPtr(Data), Data);
    return (cPtr == 0) ? null : new cVirtualPicture(cPtr, false);
  }

  public int WriteVirtualPictureStructToFile(String Name, cVirtualPicture Data) {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_WriteVirtualPictureStructToFile(swigCPtr, this, Name, cVirtualPicture.getCPtr(Data), Data);
  }

  public boolean WriteVirtualPictureStructToMapFile(cVirtualPicture Data) {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_WriteVirtualPictureStructToMapFile(swigCPtr, this, cVirtualPicture.getCPtr(Data), Data);
  }

  public void PrintDebug(String msg, boolean _Return) {
    JNIFileMappingPictureClientJNI.cFileMappingPictureClient_PrintDebug(swigCPtr, this, msg, _Return);
  }

  public void PrintStruct(cVirtualPicture Data) {
    JNIFileMappingPictureClientJNI.cFileMappingPictureClient_PrintStruct(swigCPtr, this, cVirtualPicture.getCPtr(Data), Data);
  }

  public cVirtualPicture getVirtualPicturePtr() {
    long cPtr = JNIFileMappingPictureClientJNI.cFileMappingPictureClient_getVirtualPicturePtr(swigCPtr, this);
    return (cPtr == 0) ? null : new cVirtualPicture(cPtr, false);
  }

  public boolean getDebugMode() {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_getDebugMode(swigCPtr, this);
  }

  public int getVirtualPictureDataSize() {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_getVirtualPictureDataSize(swigCPtr, this);
  }

  public String getVirtualPicturePath() {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_getVirtualPicturePath(swigCPtr, this);
  }

  public boolean getVirtualPictureMutexBlocAccess() {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_getVirtualPictureMutexBlocAccess(swigCPtr, this);
  }

  public short getMapFileOneByOneUnsignedChar(int pos) {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_getMapFileOneByOneUnsignedChar(swigCPtr, this, pos);
  }

  public short[] getMapFileBufferData() {
return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_getMapFileBufferData(swigCPtr, this);
}

  public int getFileSize(String Name) {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_getFileSize(swigCPtr, this, Name);
  }

  public void setVirtualPicturePtr(cVirtualPicture VPStruct) {
    JNIFileMappingPictureClientJNI.cFileMappingPictureClient_setVirtualPicturePtr(swigCPtr, this, cVirtualPicture.getCPtr(VPStruct), VPStruct);
  }

  public void setDebugMode(boolean _DebugMode) {
    JNIFileMappingPictureClientJNI.cFileMappingPictureClient_setDebugMode(swigCPtr, this, _DebugMode);
  }

  public void setVirtualPictureDataSize(int size) {
    JNIFileMappingPictureClientJNI.cFileMappingPictureClient_setVirtualPictureDataSize(swigCPtr, this, size);
  }

  public void setVirtualPicturePath(String path) {
    JNIFileMappingPictureClientJNI.cFileMappingPictureClient_setVirtualPicturePath(swigCPtr, this, path);
  }

  public void setVirtualPictureMutexBlocAccess(boolean blocaccess) {
    JNIFileMappingPictureClientJNI.cFileMappingPictureClient_setVirtualPictureMutexBlocAccess(swigCPtr, this, blocaccess);
  }

  public boolean setMapFileOneByOneUnsignedChar(int pos, short value) {
    return JNIFileMappingPictureClientJNI.cFileMappingPictureClient_setMapFileOneByOneUnsignedChar(swigCPtr, this, pos, value);
  }

  public void setMapFileBufferData(short[] Buf) {
    JNIFileMappingPictureClientJNI.cFileMappingPictureClient_setMapFileBufferData(swigCPtr, this, Buf);
  }

}
