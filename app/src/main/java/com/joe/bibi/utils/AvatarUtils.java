package com.joe.bibi.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import cn.bmob.v3.BmobInstallation;

/**
 * Created by Joe on 2016/1/28.
 */
public class AvatarUtils {
    private static final int CHOOSE_PHOTO =1 ;
    private static final int CROP_PHOTO = 2;
    private Activity mActivity;
    private Uri outUri;

    public String getmAvatarPath() {
        return mAvatarPath;
    }

    public void setmAvatarPath(String mAvatarPath) {
        this.mAvatarPath = mAvatarPath;
    }

    public static String mAvatarPath;
    public AvatarUtils(Activity activity) {
        this.mActivity =activity;
        mAvatarPath = Environment.getExternalStorageDirectory()+"/BiBi/"+
                BmobInstallation.getInstallationId(activity)+"_avatar.jpg";
        PrefUtils.putString(activity, ConsUtils.AVATAR, mAvatarPath);
    }

    public void upLoadAvatar(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        mActivity.startActivityForResult(intent, CHOOSE_PHOTO);//打开相册
    }
    public void choosePhoto(Intent data){
        if (Build.VERSION.SDK_INT >= 19) {
            handleImageOnKitKat(data);
        } else {
            handleImageBeforeKitkat(data);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void handleImageBeforeKitkat(Intent data) {
        String imagePath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(mActivity, uri)){
            String docid=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docid.split(":")[1];
                String selection= MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(docid));
                imagePath=getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath=getImagePath(uri,null);
        }
        //剪裁图片
        cropImage(imagePath);
    }

    public void handleImageOnKitKat(Intent data) {
        Uri uri=data.getData();
        String imagePath=getImagePath(uri, null);
        cropImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path=null;
        Cursor cursor= mActivity.getContentResolver().query(uri, null, selection, null, null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //剪裁图片
    public void cropImage(String imagePath) {
        Log.d("BB", imagePath);
        File inputfile=new File(imagePath);
         File outputFile = new File(mAvatarPath);
        try {
            if(outputFile.exists()){
                outputFile.delete();
            }
            outputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outUri = Uri.fromFile(outputFile);
        Log.d("BB",outUri.toString());
        Uri uri=Uri.fromFile(inputfile);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale",true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        mActivity.startActivityForResult(intent, CROP_PHOTO);
    }

}
