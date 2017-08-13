package jp.techacademy.kawai.momoko.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    ImageView mImage;
    Button mNextButton;
    Button mPrevButton;
    Button mStartStopButton;
    boolean mPermission;
    Cursor mCursor;
    int flgBtn = 1;

    Timer mTimer;
    double mTimerSec = 0.0;
    Handler mHandler = new Handler();
    boolean mStartStop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImage = (ImageView) findViewById(R.id.imageView);
        mNextButton = (Button) findViewById(R.id.btnNext);
        mPrevButton = (Button) findViewById(R.id.btnPrev);
        mStartStopButton = (Button) findViewById(R.id.btnStartStop);

        getPermission();
        if (mPermission){
            getContentsFirst();
        }

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flgBtn = 1;
                getPermission();
                if (mPermission){
                    getContentsNext();
                }
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flgBtn = 2;
                getPermission();
                if (mPermission){
                    getContentsPrev();
                }
            }
        });

        mStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flgBtn = 3;
                if (mStartStop){
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                    mStartStop = false;
                    mNextButton.setEnabled(true);
                    mPrevButton.setEnabled(true);
                    mStartStopButton.setText("再生");
                }
                else{
                    getPermission();
                    if (mPermission){
                        runSlideShow();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermission = true;
                } else {
                    mPermission= false;
                }
                break;
            default:
                break;
        }
        if (mPermission){
            switch (flgBtn) {
                case 1:
                    getContentsNext();
                    break;
                case 2:
                    getContentsPrev();
                    break;
                case 3:
                    getContentsFirst();
                    runSlideShow();
                    break;
            };
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCursor != null) {
            mCursor.close();
        }
    }

    private void getPermission(){
        // パーミッションの許可状態を確認する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                mPermission = true;
            } else {
                mPermission= false;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        }else{
            mPermission = true;
        }
    }

    private void getContentsFirst() {
        getCursor();
        if(mCursor != null){
            if (mCursor.moveToFirst()) {

                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = mCursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                mImage.setImageURI(imageUri);
            }
        }
    }

    private void getContentsLast() {
        getCursor();
        if(mCursor != null){
            if (mCursor.moveToLast()) {

                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = mCursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                mImage.setImageURI(imageUri);
            }
        }
    }

    private void getContentsNext() {
        // 画像の情報を取得する
        getCursor();
        if(mCursor != null){
            if (mCursor.moveToNext()) {

                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = mCursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                mImage.setImageURI(imageUri);
            }else{
                getContentsFirst();
            }
        }
    }

    private void getContentsPrev() {
        getCursor();
        if(mCursor != null){
            if (mCursor.moveToPrevious()) {

                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = mCursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                mImage.setImageURI(imageUri);
            }else{
                getContentsLast();
            }
        }
    }

    private void getCursor() {
        // 画像の情報を取得する
        if (mCursor == null) {
            ContentResolver resolver = getContentResolver();
            mCursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                    null, // 項目(null = 全項目)
                    null, // フィルタ条件(null = フィルタなし)
                    null, // フィルタ用パラメータ
                    null // ソート (null ソートなし)
            );
        }
    }

    private void runSlideShow() {
        if (mTimer == null) {
            // タイマーの作成
            mTimer = new Timer();
            // タイマーの始動
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mTimerSec += 2;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getContentsNext();
                        }
                    });
                }
            }, 2000, 2000);    // 最初に始動させるまで 2000ミリ秒、ループの間隔を 2000ミリ秒 に設定
        }
        mStartStop = true;
        mNextButton.setEnabled(false);
        mPrevButton.setEnabled(false);
        mStartStopButton.setText("停止");
    }

}
