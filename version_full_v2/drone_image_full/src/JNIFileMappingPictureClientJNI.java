


public class JNIFileMappingPictureClientJNI {
  public final static native int STRUCT_DATAPICTURE_BUFFER_SIZE_get();
  public final static native void cVirtualPicture_MutexBlocAccess_set(long jarg1, cVirtualPicture jarg1_, boolean jarg2);
  public final static native boolean cVirtualPicture_MutexBlocAccess_get(long jarg1, cVirtualPicture jarg1_);
  public final static native void cVirtualPicture_DataPictureSize_set(long jarg1, cVirtualPicture jarg1_, int jarg2);
  public final static native int cVirtualPicture_DataPictureSize_get(long jarg1, cVirtualPicture jarg1_);
  public final static native void cVirtualPicture_PicturePath_set(long jarg1, cVirtualPicture jarg1_, String jarg2);
  public final static native String cVirtualPicture_PicturePath_get(long jarg1, cVirtualPicture jarg1_);
  public final static native void cVirtualPicture_PictureData_set(long jarg1, cVirtualPicture jarg1_, long jarg2);
  public final static native long cVirtualPicture_PictureData_get(long jarg1, cVirtualPicture jarg1_);
  public final static native void cVirtualPicture_PictureDataPtr_set(long jarg1, cVirtualPicture jarg1_, long jarg2);
  public final static native long cVirtualPicture_PictureDataPtr_get(long jarg1, cVirtualPicture jarg1_);
  public final static native long new_cVirtualPicture();
  public final static native void delete_cVirtualPicture(long jarg1);
  public final static native long new_cFileMappingPictureClient(boolean jarg1);
  public final static native void delete_cFileMappingPictureClient(long jarg1);
  public final static native boolean cFileMappingPictureClient_OpenClient(long jarg1, cFileMappingPictureClient jarg1_, String jarg2);
  public final static native void cFileMappingPictureClient_CloseClient(long jarg1, cFileMappingPictureClient jarg1_);
  public final static native boolean cFileMappingPictureClient_FileToMapFile(long jarg1, cFileMappingPictureClient jarg1_, String jarg2, long jarg3, cVirtualPicture jarg3_);
  public final static native int cFileMappingPictureClient_MapFileToFile(long jarg1, cFileMappingPictureClient jarg1_, String jarg2, long jarg3, cVirtualPicture jarg3_);
  public final static native int cFileMappingPictureClient_ReadFileToVirtualPictureStruct(long jarg1, cFileMappingPictureClient jarg1_, String jarg2, long jarg3, cVirtualPicture jarg3_);
  public final static native long cFileMappingPictureClient_ReadMapFileToVirtualPictureStruct(long jarg1, cFileMappingPictureClient jarg1_, long jarg2, cVirtualPicture jarg2_);
  public final static native int cFileMappingPictureClient_WriteVirtualPictureStructToFile(long jarg1, cFileMappingPictureClient jarg1_, String jarg2, long jarg3, cVirtualPicture jarg3_);
  public final static native boolean cFileMappingPictureClient_WriteVirtualPictureStructToMapFile(long jarg1, cFileMappingPictureClient jarg1_, long jarg2, cVirtualPicture jarg2_);
  public final static native void cFileMappingPictureClient_PrintDebug(long jarg1, cFileMappingPictureClient jarg1_, String jarg2, boolean jarg3);
  public final static native void cFileMappingPictureClient_PrintStruct(long jarg1, cFileMappingPictureClient jarg1_, long jarg2, cVirtualPicture jarg2_);
  public final static native long cFileMappingPictureClient_getVirtualPicturePtr(long jarg1, cFileMappingPictureClient jarg1_);
  public final static native boolean cFileMappingPictureClient_getDebugMode(long jarg1, cFileMappingPictureClient jarg1_);
  public final static native int cFileMappingPictureClient_getVirtualPictureDataSize(long jarg1, cFileMappingPictureClient jarg1_);
  public final static native String cFileMappingPictureClient_getVirtualPicturePath(long jarg1, cFileMappingPictureClient jarg1_);
  public final static native boolean cFileMappingPictureClient_getVirtualPictureMutexBlocAccess(long jarg1, cFileMappingPictureClient jarg1_);
  public final static native short cFileMappingPictureClient_getMapFileOneByOneUnsignedChar(long jarg1, cFileMappingPictureClient jarg1_, int jarg2);
  public final static native short[] cFileMappingPictureClient_getMapFileBufferData(long jarg1, cFileMappingPictureClient jarg1_);
  public final static native int cFileMappingPictureClient_getFileSize(long jarg1, cFileMappingPictureClient jarg1_, String jarg2);
  public final static native void cFileMappingPictureClient_setVirtualPicturePtr(long jarg1, cFileMappingPictureClient jarg1_, long jarg2, cVirtualPicture jarg2_);
  public final static native void cFileMappingPictureClient_setDebugMode(long jarg1, cFileMappingPictureClient jarg1_, boolean jarg2);
  public final static native void cFileMappingPictureClient_setVirtualPictureDataSize(long jarg1, cFileMappingPictureClient jarg1_, int jarg2);
  public final static native void cFileMappingPictureClient_setVirtualPicturePath(long jarg1, cFileMappingPictureClient jarg1_, String jarg2);
  public final static native void cFileMappingPictureClient_setVirtualPictureMutexBlocAccess(long jarg1, cFileMappingPictureClient jarg1_, boolean jarg2);
  public final static native boolean cFileMappingPictureClient_setMapFileOneByOneUnsignedChar(long jarg1, cFileMappingPictureClient jarg1_, int jarg2, short jarg3);
  public final static native void cFileMappingPictureClient_setMapFileBufferData(long jarg1, cFileMappingPictureClient jarg1_, short[] jarg2);
}
