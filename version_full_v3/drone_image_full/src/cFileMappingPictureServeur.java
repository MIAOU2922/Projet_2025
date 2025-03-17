


public class cFileMappingPictureServeur {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected cFileMappingPictureServeur(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(cFileMappingPictureServeur obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(cFileMappingPictureServeur obj) {
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
        JNIFileMappingPictureServeurJNI.delete_cFileMappingPictureServeur(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public cFileMappingPictureServeur(boolean _DebugFlag) {
    this(JNIFileMappingPictureServeurJNI.new_cFileMappingPictureServeur(_DebugFlag), true);
  }

  public boolean OpenServer(String NameSharedMem) {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_OpenServer(swigCPtr, this, NameSharedMem);
  }

  public void CloseServer() {
    JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_CloseServer(swigCPtr, this);
  }

  public int FileToMapFile(String Name, cVirtualPicture Data) {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_FileToMapFile(swigCPtr, this, Name, cVirtualPicture.getCPtr(Data), Data);
  }

  public int MapFileToFile(String Name, cVirtualPicture Data) {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_MapFileToFile(swigCPtr, this, Name, cVirtualPicture.getCPtr(Data), Data);
  }

  public int ReadFileToVirtualPictureStruct(String Name, cVirtualPicture Data) {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_ReadFileToVirtualPictureStruct(swigCPtr, this, Name, cVirtualPicture.getCPtr(Data), Data);
  }

  public cVirtualPicture ReadMapFileToVirtualPictureStruct(cVirtualPicture Data) {
    long cPtr = JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_ReadMapFileToVirtualPictureStruct(swigCPtr, this, cVirtualPicture.getCPtr(Data), Data);
    return (cPtr == 0) ? null : new cVirtualPicture(cPtr, false);
  }

  public int WriteVirtualPictureStructToFile(String Name, cVirtualPicture Data) {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_WriteVirtualPictureStructToFile(swigCPtr, this, Name, cVirtualPicture.getCPtr(Data), Data);
  }

  public boolean WriteVirtualPictureStructToMapFile(cVirtualPicture Data) {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_WriteVirtualPictureStructToMapFile(swigCPtr, this, cVirtualPicture.getCPtr(Data), Data);
  }

  public void PrintDebug(String msg, boolean _Return) {
    JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_PrintDebug(swigCPtr, this, msg, _Return);
  }

  public void PrintStruct(cVirtualPicture Data) {
    JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_PrintStruct(swigCPtr, this, cVirtualPicture.getCPtr(Data), Data);
  }

  public cVirtualPicture getVirtualPicturePtr() {
    long cPtr = JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_getVirtualPicturePtr(swigCPtr, this);
    return (cPtr == 0) ? null : new cVirtualPicture(cPtr, false);
  }

  public boolean getDebugMode() {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_getDebugMode(swigCPtr, this);
  }

  public int getVirtualPictureDataSize() {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_getVirtualPictureDataSize(swigCPtr, this);
  }

  public String getVirtualPicturePath() {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_getVirtualPicturePath(swigCPtr, this);
  }

  public boolean getVirtualPictureMutexBlocAccess() {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_getVirtualPictureMutexBlocAccess(swigCPtr, this);
  }

  public short getMapFileOneByOneUnsignedChar(int pos) {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_getMapFileOneByOneUnsignedChar(swigCPtr, this, pos);
  }

  public short[] getMapFileBufferData() {
return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_getMapFileBufferData(swigCPtr, this);
}

  public int getFileSize(String Name) {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_getFileSize(swigCPtr, this, Name);
  }

  public void setVirtualPicturePtr(cVirtualPicture VPStruct) {
    JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_setVirtualPicturePtr(swigCPtr, this, cVirtualPicture.getCPtr(VPStruct), VPStruct);
  }

  public void setDebugMode(boolean _DebugMode) {
    JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_setDebugMode(swigCPtr, this, _DebugMode);
  }

  public void setVirtualPictureDataSize(int size) {
    JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_setVirtualPictureDataSize(swigCPtr, this, size);
  }

  public void setVirtualPicturePath(String path) {
    JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_setVirtualPicturePath(swigCPtr, this, path);
  }

  public void setVirtualPictureMutexBlocAccess(boolean blocaccess) {
    JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_setVirtualPictureMutexBlocAccess(swigCPtr, this, blocaccess);
  }

  public boolean setMapFileOneByOneUnsignedChar(int pos, short value) {
    return JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_setMapFileOneByOneUnsignedChar(swigCPtr, this, pos, value);
  }

  public void setMapFileBufferData(short[] Buf) {
    JNIFileMappingPictureServeurJNI.cFileMappingPictureServeur_setMapFileBufferData(swigCPtr, this, Buf);
  }

}
