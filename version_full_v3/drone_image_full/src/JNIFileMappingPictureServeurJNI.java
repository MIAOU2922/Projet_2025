


public class JNIFileMappingPictureServeurJNI {
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
  public final static native long new_cFileMappingPictureServeur(boolean jarg1);
  public final static native void delete_cFileMappingPictureServeur(long jarg1);
  public final static native boolean cFileMappingPictureServeur_OpenServer(long jarg1, cFileMappingPictureServeur jarg1_, String jarg2);
  public final static native void cFileMappingPictureServeur_CloseServer(long jarg1, cFileMappingPictureServeur jarg1_);
  public final static native int cFileMappingPictureServeur_FileToMapFile(long jarg1, cFileMappingPictureServeur jarg1_, String jarg2, long jarg3, cVirtualPicture jarg3_);
  public final static native int cFileMappingPictureServeur_MapFileToFile(long jarg1, cFileMappingPictureServeur jarg1_, String jarg2, long jarg3, cVirtualPicture jarg3_);
  public final static native int cFileMappingPictureServeur_ReadFileToVirtualPictureStruct(long jarg1, cFileMappingPictureServeur jarg1_, String jarg2, long jarg3, cVirtualPicture jarg3_);
  public final static native long cFileMappingPictureServeur_ReadMapFileToVirtualPictureStruct(long jarg1, cFileMappingPictureServeur jarg1_, long jarg2, cVirtualPicture jarg2_);
  public final static native int cFileMappingPictureServeur_WriteVirtualPictureStructToFile(long jarg1, cFileMappingPictureServeur jarg1_, String jarg2, long jarg3, cVirtualPicture jarg3_);
  public final static native boolean cFileMappingPictureServeur_WriteVirtualPictureStructToMapFile(long jarg1, cFileMappingPictureServeur jarg1_, long jarg2, cVirtualPicture jarg2_);
  public final static native void cFileMappingPictureServeur_PrintDebug(long jarg1, cFileMappingPictureServeur jarg1_, String jarg2, boolean jarg3);
  public final static native void cFileMappingPictureServeur_PrintStruct(long jarg1, cFileMappingPictureServeur jarg1_, long jarg2, cVirtualPicture jarg2_);
  public final static native long cFileMappingPictureServeur_getVirtualPicturePtr(long jarg1, cFileMappingPictureServeur jarg1_);
  public final static native boolean cFileMappingPictureServeur_getDebugMode(long jarg1, cFileMappingPictureServeur jarg1_);
  public final static native int cFileMappingPictureServeur_getVirtualPictureDataSize(long jarg1, cFileMappingPictureServeur jarg1_);
  public final static native String cFileMappingPictureServeur_getVirtualPicturePath(long jarg1, cFileMappingPictureServeur jarg1_);
  public final static native boolean cFileMappingPictureServeur_getVirtualPictureMutexBlocAccess(long jarg1, cFileMappingPictureServeur jarg1_);
  public final static native short cFileMappingPictureServeur_getMapFileOneByOneUnsignedChar(long jarg1, cFileMappingPictureServeur jarg1_, int jarg2);
  public final static native short[] cFileMappingPictureServeur_getMapFileBufferData(long jarg1, cFileMappingPictureServeur jarg1_);
  public final static native int cFileMappingPictureServeur_getFileSize(long jarg1, cFileMappingPictureServeur jarg1_, String jarg2);
  public final static native void cFileMappingPictureServeur_setVirtualPicturePtr(long jarg1, cFileMappingPictureServeur jarg1_, long jarg2, cVirtualPicture jarg2_);
  public final static native void cFileMappingPictureServeur_setDebugMode(long jarg1, cFileMappingPictureServeur jarg1_, boolean jarg2);
  public final static native void cFileMappingPictureServeur_setVirtualPictureDataSize(long jarg1, cFileMappingPictureServeur jarg1_, int jarg2);
  public final static native void cFileMappingPictureServeur_setVirtualPicturePath(long jarg1, cFileMappingPictureServeur jarg1_, String jarg2);
  public final static native void cFileMappingPictureServeur_setVirtualPictureMutexBlocAccess(long jarg1, cFileMappingPictureServeur jarg1_, boolean jarg2);
  public final static native boolean cFileMappingPictureServeur_setMapFileOneByOneUnsignedChar(long jarg1, cFileMappingPictureServeur jarg1_, int jarg2, short jarg3);
  public final static native void cFileMappingPictureServeur_setMapFileBufferData(long jarg1, cFileMappingPictureServeur jarg1_, short[] jarg2);
}
