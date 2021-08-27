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
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


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

        final CharSequence[] items = { "Take Photo", "Choose from Gallery",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 100);
                } else if (items[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            101);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case 100:
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    imageView.setImageBitmap(photo);

                    break;
                case 101:
                    Uri uri = data.getData();
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
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
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