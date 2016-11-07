package magewell.weixindemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "MAGEWELL";
    private final static int INTENT_SHARE_CODE = 102;
    private IWXAPI api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = WXAPIFactory.createWXAPI(this,getString(R.string.APP_ID), false);

        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.btnLaunch).setOnClickListener(this);
        findViewById(R.id.btnShare).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnRegister:
                onClickBtnRegister();
                break;
            case R.id.btnLaunch:
                onClickBtnLaunch();
                break;
            case R.id.btnShare:
                onClickBtnShare();
                break;
            default:
                break;
        }
    }

    private void onClickBtnRegister(){
        api.registerApp(getString(R.string.APP_ID));
    }

    private void onClickBtnLaunch(){
        if (api.openWXApp()){
            Log.i(TAG, "Launch WEIXIN!");
        }else {
            Log.i(TAG, "Cannot Launch WEIXIN!");
        }
    }

    private void onClickBtnShare(){
        startActivity(new Intent(MainActivity.this, ShareActivity.class));
    }
}
