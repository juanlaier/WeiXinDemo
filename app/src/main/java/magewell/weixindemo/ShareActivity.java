package magewell.weixindemo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int INTENT_IMAGE_PICKER = 103;
    private final static int INTENT_VIDEO_PICKER = 104;

    private final static String TAG = "MAGEWELL";
    private String strImageUri = "/storage/emulated/0/ProShow/VirtualBackground/pic4.jpg";
    private String strVideoUri = "/storage/emulated/0/gbox/rom/video-0-1.mp4";
    private IWXAPI api;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        api = WXAPIFactory.createWXAPI(this, getString(R.string.APP_ID), false);
        requestQueue = Volley.newRequestQueue(this);

        findViewById(R.id.btnSendText).setOnClickListener(this);
        findViewById(R.id.btnGetImage).setOnClickListener(this);
        findViewById(R.id.btnGetVideo).setOnClickListener(this);
        findViewById(R.id.btnSendVideo).setOnClickListener(this);
        findViewById(R.id.btnSendData).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSendText:
                onClickBtnSendText();
                break;
            case R.id.btnGetImage:
                onClickBtnGetImage();
                break;
            case R.id.btnGetVideo:
                onClickBtnGetVideo();
                break;
            case R.id.btnSendVideo:
                onClickBtnSendVideo();
                break;
            case R.id.btnSendData:
                onClickBtnSendData();
                break;
            default:
                break;
        }
    }

    private void onClickBtnGetImage(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Choose the Image"), INTENT_IMAGE_PICKER);
    }

    private void onClickBtnGetVideo(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Choose the Image"), INTENT_VIDEO_PICKER);
    }

    private void onClickBtnSendText(){
        String text = "Don’t forget the things you once you owned. Treasure the things you can’t get. Don't give up the things that belong to you and keep those lost things in memory.";
        File file = new File(strImageUri);
        if (!file.exists()) {
            Log.i(TAG, "Cannot Find The File!");
            return;
        }

        WXImageObject wxImageObject = new WXImageObject();
        wxImageObject.setImagePath(strImageUri);

        WXMediaMessage wxMediaMessage = new WXMediaMessage();
        wxMediaMessage.title = text;
        wxMediaMessage.description = text;
        wxMediaMessage.mediaObject = wxImageObject;

        Bitmap bmp = BitmapFactory.decodeFile(strImageUri);
        Bitmap thumbData = Bitmap.createScaledBitmap(bmp,150, 150, true);
        bmp.recycle();
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        thumbData.compress(Bitmap.CompressFormat.JPEG, 100, oStream);
        thumbData.recycle();
        wxMediaMessage.thumbData = oStream.toByteArray();
        try {
            oStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SendMessageToWX.Req request = new SendMessageToWX.Req();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = wxMediaMessage;
        request.scene = SendMessageToWX.Req.WXSceneTimeline;
        if (request.checkArgs()) {
            api.sendReq(request);
        }else{
            Log.i(TAG, "Arga ERROR!");
        }
    }

    private void onClickBtnSendVideo(){
        String text = "Problems are guidelines instead of stop signs";
        File file = new File(strVideoUri);
        if (!file.exists()) {
            Log.i(TAG, "Cannot Find The File!");
            return;
        }

        WXVideoObject wxVideoObject = new WXVideoObject();
        wxVideoObject.videoUrl = strVideoUri;//"http://www.iqiyi.com/v_19rr9ettfw.html";

        WXMediaMessage msg = new WXMediaMessage(wxVideoObject);
        msg.title = text;
        msg.description = text;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.thumb2);
        msg.thumbData = bmpTOByteArray(bmp, true);

        SendMessageToWX.Req request = new SendMessageToWX.Req();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = msg;
        request.scene = SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(request);
    }

    private void onClickBtnSendData(){
        String strUrl = "http://192.168.1.139/snapshot/gbox_2016-10-24_09-05-20.jpg";
        StringRequest request = new StringRequest(strUrl, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {

                byte[] byBmp = new byte[response.length()];
                try {
                    String strTemp = new String(response.getBytes("UTF-8"), "GBK");
                    byBmp = response.getBytes("GBK");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                WXImageObject image = new WXImageObject(byBmp);

                WXMediaMessage msg = new WXMediaMessage(image);
                msg.thumbData = bmpTOByteArray(((BitmapDrawable)getDrawable(R.drawable.thumb2)).getBitmap(), true);

                SendMessageToWX.Req request = new SendMessageToWX.Req();
                request.scene = SendMessageToWX.Req.WXSceneTimeline;
                request.transaction = String.valueOf(System.currentTimeMillis());
                request.message = msg;

                api.sendReq(request);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_IMAGE_PICKER && resultCode == Activity.RESULT_OK){
           strImageUri = UriTOString(data.getData());
        } else if (requestCode == INTENT_VIDEO_PICKER && resultCode == Activity.RESULT_OK){
            strVideoUri = UriTOString(data.getData());
            Log.i(TAG, strVideoUri);
        }
    }

    private String UriTOString(Uri uri){
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        Uri contentUri = null;
        if ("image".equals(type)){
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }else if ("video".equals(type)){
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        String selection = MediaStore.Images.Media._ID + "=?";
        String[] selectionArgs = new String[]{split[1]};

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        return cursor.getString(index);
    }

    private byte[] bmpTOByteArray(Bitmap bmp, boolean needRecycle){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private byte[] readFromFile(String fileName){
        byte[] b = null;
        File file = new File(fileName);
        if (!file.exists()){
            Log.i(TAG, "Cannot Find The File!");
            return null;
        }
        try {
            RandomAccessFile in = new RandomAccessFile(fileName, "r");
            b = new byte[(int)file.length()];
            in.readFully(b);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }
}
