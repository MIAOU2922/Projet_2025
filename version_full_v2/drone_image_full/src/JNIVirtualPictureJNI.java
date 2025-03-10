


public class JNIVirtualPictureJNI {
  public final static native int STRUCT_DATAPICTURE_BUFFER_SIZE_get();
  public final static native void cVirtualPicture_MutexBlocAccess_set(long jarg1, cVirtualPicture jarg1_, boolean jarg2);
  public final static native boolean cVirtualPicture_MutexBlocAccess_get(long jarg1, cVirtualPicture jarg1_);
  public final static native void cVirtualPicture_DataPictureSize_set(long jarg1, cVirtualPicture jarg1_, int jarg2);
  public final static native int cVirtualPicture_DataPictureSize_get(long jarg1, cVirtualPicture jarg1_);
  public final static native void cVirtualPicture_PicturePath_set(long jarg1, cVirtualPicture jarg1_, String jarg2);
  public final static native String cVirtualPicture_PicturePath_get(long jarg1, cVirtualPicture jarg1_);
  public final static native void cVirtualPicture_PictureData_set(long jarg1, cVirtualPicture jarg1_, short[] jarg2);
  public final static native short[] cVirtualPicture_PictureData_get(long jarg1, cVirtualPicture jarg1_);
  public final static native void cVirtualPicture_PictureDataPtr_set(long jarg1, cVirtualPicture jarg1_, short[] jarg2);
  public final static native short[] cVirtualPicture_PictureDataPtr_get(long jarg1, cVirtualPicture jarg1_);
  public final static native long new_cVirtualPicture();
  public final static native void delete_cVirtualPicture(long jarg1);
}
