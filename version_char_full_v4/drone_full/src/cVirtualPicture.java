


public class cVirtualPicture {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected cVirtualPicture(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(cVirtualPicture obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(cVirtualPicture obj) {
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
        JNIFileMappingPictureClientJNI.delete_cVirtualPicture(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setMutexBlocAccess(boolean value) {
    JNIFileMappingPictureClientJNI.cVirtualPicture_MutexBlocAccess_set(swigCPtr, this, value);
  }

  public boolean getMutexBlocAccess() {
    return JNIFileMappingPictureClientJNI.cVirtualPicture_MutexBlocAccess_get(swigCPtr, this);
  }

  public void setDataPictureSize(int value) {
    JNIFileMappingPictureClientJNI.cVirtualPicture_DataPictureSize_set(swigCPtr, this, value);
  }

  public int getDataPictureSize() {
    return JNIFileMappingPictureClientJNI.cVirtualPicture_DataPictureSize_get(swigCPtr, this);
  }

  public void setPicturePath(String value) {
    JNIFileMappingPictureClientJNI.cVirtualPicture_PicturePath_set(swigCPtr, this, value);
  }

  public String getPicturePath() {
    return JNIFileMappingPictureClientJNI.cVirtualPicture_PicturePath_get(swigCPtr, this);
  }

  public void setPictureData(SWIGTYPE_p_unsigned_char value) {
    JNIFileMappingPictureClientJNI.cVirtualPicture_PictureData_set(swigCPtr, this, SWIGTYPE_p_unsigned_char.getCPtr(value));
  }

  public SWIGTYPE_p_unsigned_char getPictureData() {
    long cPtr = JNIFileMappingPictureClientJNI.cVirtualPicture_PictureData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_char(cPtr, false);
  }

  public void setPictureDataPtr(SWIGTYPE_p_unsigned_char value) {
    JNIFileMappingPictureClientJNI.cVirtualPicture_PictureDataPtr_set(swigCPtr, this, SWIGTYPE_p_unsigned_char.getCPtr(value));
  }

  public SWIGTYPE_p_unsigned_char getPictureDataPtr() {
    long cPtr = JNIFileMappingPictureClientJNI.cVirtualPicture_PictureDataPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_char(cPtr, false);
  }

  public cVirtualPicture() {
    this(JNIFileMappingPictureClientJNI.new_cVirtualPicture(), true);
  }

}
