package com.example.youyou22.cameratest;

import android.hardware.Camera;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.os.Environment;
import android.view.ViewGroup;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static  String TAG = "screencamera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if( savedInstanceState == null ){
            getSupportFragmentManager().beginTransaction().add(R.id.container, new CameraFragment(), TAG).commit();
        }
    }

    public static class CameraFragment extends Fragment {
        private Camera camera_;
        View rootView_;
        SurfaceView surfaceView_;
        private static  String ACCESS_TOKEN = "gocop0ryj842x4w";
        public DbxClientV2 client;

        private SurfaceHolder.Callback surfaceListner_ = new SurfaceHolder.Callback(){
            public void surfaceCreated(SurfaceHolder holder){
                camera_ = Camera.open();
                try {
                    camera_.setPreviewDisplay(holder);
                    Log.d(TAG,"StartCamera");
                } catch (Exception e){
                    e.printStackTrace();
                    Log.d(TAG,"StopCamera");
                }

                DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial","utf-8");
                client = new DbxClientV2(config, ACCESS_TOKEN);
                try {
                    FullAccount account = client.users().getCurrentAccount();
                    Log.d(TAG, account.getName().getDisplayName());
                } catch (DbxException e) {
                    e.printStackTrace();
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder){
                camera_.release();
                camera_ = null;
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height ){

                Camera.Parameters parameters = camera_.getParameters();
                parameters.setPreviewSize(640,480);

                camera_.setParameters(parameters);
                camera_.startPreview();
            }
        };

        private Camera.ShutterCallback shutterListener_ = new Camera.ShutterCallback(){
            public void onShutter() {
            }
        };

        private Camera.PictureCallback pictureListner_ = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                if( data != null ) {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/camera_test.jpg");
                        fos.write(data);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try{
                        InputStream is = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/camera_test.jpg");
                        FileMetadata metadata = client.files().uploadBuilder(Environment.getExternalStorageDirectory().getPath() + "/camera_test.jpg").uploadAndFinish(is);
                        is.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    camera.startPreview();
                }
            }
        };

        View.OnTouchListener ontouchListner_ = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if( event.getAction() == MotionEvent.ACTION_DOWN) {
                    if( camera_ != null ) {
                        camera_.takePicture(shutterListener_,null, pictureListner_);
                    }
                }

                return false;
            }
        };

        public CameraFragment(){
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            rootView_ = inflater.inflate(R.layout.fragment_main, container, false);
            surfaceView_ = (SurfaceView)rootView_.findViewById(R.id.surface_view);

            SurfaceHolder holder = surfaceView_.getHolder();
            holder.addCallback(surfaceListner_);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            rootView_.setOnTouchListener(ontouchListner_);

            return rootView_;
        }
    }
}
