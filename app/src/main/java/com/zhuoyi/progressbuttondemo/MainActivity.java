package com.zhuoyi.progressbuttondemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zhuoyi.progressbuttondemo.DownLoadProgressButton.TextSizeType;

public class MainActivity extends AppCompatActivity {

    private DownLoadProgressButton mDownLoadProgressButton;

    private int sleepTime=300;


    private int corner=1;
    private int progress=0;
    private Handler mMHandler;

    private TextSizeType Type=TextSizeType.PX;

    private int textSize=10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDownLoadProgressButton = (DownLoadProgressButton) findViewById(R.id.downLoadProgressButton);
        mDownLoadProgressButton.setMaxProgress(100);
    }


    public void contin(View view){
        mDownLoadProgressButton.setState(DownLoadProgressButton.DOWNLOADING_STATE);
    }

    public void pause(View view){
        mDownLoadProgressButton.setState(DownLoadProgressButton.DOWNLOAD_PAUSE_STATE);
    }

    public void wait(View view){
        mDownLoadProgressButton.setState(DownLoadProgressButton.DOWNLOAD_WAITTING_STATE);
    }

    public void down(View view){
        mDownLoadProgressButton.setState(DownLoadProgressButton.DOWNLOAD_FREE_STATE);
    }
    public void install(View view){
        mDownLoadProgressButton.setState(DownLoadProgressButton.DOWNLOAD_COMPLETE_STATE);
    }
    public void progress(View view){
        mDownLoadProgressButton.setState(DownLoadProgressButton.DOWNLOADING_STATE);
        progress=progress+5;
        mDownLoadProgressButton.setProgress(progress);
    }

    public void option(View view){

        mDownLoadProgressButton.setTextSize(textSize, Type);
       // Type=TextSizeType.SP;
        textSize=textSize+5;
    }


    public void corner(View view){

        mDownLoadProgressButton.setCornerRadius(corner);
        corner=corner+1;
    }

}
