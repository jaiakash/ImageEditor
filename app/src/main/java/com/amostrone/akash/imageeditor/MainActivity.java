package com.amostrone.akash.imageeditor;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_REQUEST_CODE = 100;
    
    Button btPick;
    ImageView imageView;
    static EditCanvas editCanvas;

    boolean isimageselected=false;
    boolean isdrawnoncanvas=false;
    static boolean isAddTextchosen=false;

    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = findViewById(R.id.linearLayout);
        
        btPick = findViewById(R.id.bt_pick);
        imageView = findViewById(R.id.image_view);
        
        btPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isimageselected=true;
                checkPermission();
            }
        });

        Button btn1 = (Button)findViewById(R.id.reset_button);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Restart the app
                PackageManager packageManager = getApplicationContext().getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
                ComponentName componentName = intent.getComponent();
                Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                getApplicationContext().startActivity(mainIntent);
                Runtime.getRuntime().exit(0);
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
        EditCanvas.text="";

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
                case 101:
                    imageView.setImageURI(uri);
                    break;
            }
        }
    }

    public void toGrayScale(View view) {
        if(isimageselected){
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            imageView.setColorFilter(filter);

            Drawable drawable = imageView.getDrawable();
            drawable.setBounds(20, 30, drawable.getIntrinsicWidth()+20, drawable.getIntrinsicHeight()+30);

            if(!isdrawnoncanvas){
                editCanvas =new EditCanvas(this);
                editCanvas.setBackgroundDrawable(drawable);
                linearLayout.addView(editCanvas);
                imageView.setVisibility(View.GONE);
                isdrawnoncanvas=true;
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Select the image first", Toast.LENGTH_SHORT).show();
        }
    }

    public void toAddText(View view) {
        if(isimageselected){
        Drawable drawable = imageView.getDrawable();
        drawable.setBounds(20, 30, drawable.getIntrinsicWidth()+20, drawable.getIntrinsicHeight()+30);
        isAddTextchosen=true;
        if(!isdrawnoncanvas){
            editCanvas =new EditCanvas(this);
            editCanvas.setBackgroundDrawable(drawable);
            linearLayout.addView(editCanvas);
            imageView.setVisibility(View.GONE);
            isdrawnoncanvas=true;
        }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter the text");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditCanvas.text= input.getText().toString();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }
        else{
            Toast.makeText(getApplicationContext(), "Select the image first", Toast.LENGTH_SHORT).show();
        }
    }

    public void toDoodle(View view) {
        if(isimageselected) {
            isAddTextchosen=false;
            Drawable drawable = imageView.getDrawable();
            drawable.setBounds(20, 30, drawable.getIntrinsicWidth() + 20, drawable.getIntrinsicHeight() + 30);
            if (!isdrawnoncanvas) {
                editCanvas = new EditCanvas(this);
                editCanvas.setBackgroundDrawable(drawable);
                linearLayout.addView(editCanvas);
                imageView.setVisibility(View.GONE);
                isdrawnoncanvas = true;
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Select the image first", Toast.LENGTH_SHORT).show();
        }
    }

    public void toSave(View view) {
        if(isimageselected) {
            Toast.makeText(getApplicationContext(), "App development is in progress", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Select the image first", Toast.LENGTH_SHORT).show();
        }
    }
}