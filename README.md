# Usage
### 1.Include the library as local library project.

    allprojects {
      repositories {
          jcenter()
          maven { url "https://jitpack.io" }
      }
    }
`compile 'com.github.caipu1618:clipcamera:1.0.0'`

### 2.Add CameraActivity into your AndroidManifest.xml

    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.CAMERA" />
    
    <activity
        android:name="com.caipu1618.clipcamera.camera.CameraActivity"
        android:screenOrientation="portrait"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar"/>
    
### 3.How to use clipcamera.

    CPUtil.startCamera(fileDir, filename, requestCode, activity); -use custom camera
    
    CPUtil.startLocalCamera(fileDir, filename, fileprovider(for android7.0), requestCode, activity); -use local camera
    
### 4.Override onActivityResult method and handle clipcamera result.

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
           if(requestCode == ...){
              Uri resultUri = Uri.fromFile(new File(fileDir, filename));
              try {
                Bitmap photoBmp = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                ivPic.setImageBitmap(photoBmp);
              }catch (Exception e){
                e.printStackTrace();
              }
           }
        }
    }
    
### 5.You may want to add this to your PROGUARD config:

    -dontwarn com.caipu1618.clipcamera**
    -keep class com.caipu1618.clipcamera** { *; }
    -keep interface com.caipu1618.clipcamera** { *; }

# Compatibility

* Library - Android ICS 4.0+ (API 14)
