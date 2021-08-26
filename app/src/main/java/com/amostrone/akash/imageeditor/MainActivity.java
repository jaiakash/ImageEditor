package com.amostrone.akash.imageeditor;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_REQUEST_CODE = 100;
    
    Button btPick;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btPick = findViewById(R.id.bt_pick);
        imageView = findViewById(R.id.image_view);
        
        btPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });
    }

    private void checkPermission() {
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            pickImage();
        }
        else{
            if(permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
            }
            else {
                pickImage();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            pickImage();
        }
        else{
            Toast.makeText(getApplicationContext(), "Please grant permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), new Date().getTime() + "myPic.jpg");
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));

        Intent chooser = Intent.createChooser(galleryIntent, "Choose the file");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });
        startActivityForResult(chooser, 101);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Uri uri = data.getData();
            switch (requestCode){
                case 100:
                    Intent intent = new Intent(MainActivity.this, DsPhotoEditorActivity.class);
                    intent.setData(uri);
                    //Output Directory Name
                    intent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY,
                            "Images");
                    //Set toolbar color
                    intent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR,
                            Color.parseColor("#e91e63"));
                    //Set Background Color
                    intent.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR,
                            Color.parseColor("#FFFFFF"));
                    //Hide Tools
                    intent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE,
                            new int[]{DsPhotoEditorActivity.TOOL_WARMTH,
                            DsPhotoEditorActivity.TOOL_PIXELATE});

                    startActivityForResult(intent,101);
                    break;
                case 101:
                    imageView.setImageURI(uri);
                    break;
            }
        }
    }

    public void toGrayScale(View view) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        imageView.setColorFilter(filter);
    }
}