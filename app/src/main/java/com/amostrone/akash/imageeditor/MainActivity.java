package com.amostrone.akash.imageeditor;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


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

                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(imageView.getWidth(), imageView.getHeight());
                params.setMargins(0, 0, 0, 0);
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

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(imageView.getWidth(), imageView.getHeight());
            params.setMargins(0, 0, 0, 0);
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

                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(imageView.getWidth(), imageView.getHeight());
                params.setMargins(0, 0, 0, 0);
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
            File file = saveBitMap(this, linearLayout);    //which view you want to pass that view as parameter
            if (file != null) {
                Log.i("TAG", "Drawing saved to the gallery!");
            } else {
                Log.i("TAG", "Oops! Image could not be saved.");
            }
            Toast.makeText(getApplicationContext(), "Image saved in pictures", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Select the image first", Toast.LENGTH_SHORT).show();
        }
    }
    private File saveBitMap(Context context, View drawView){
        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"AkashEditor");
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if(!isDirectoryCreated)
                Log.i("ATG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() +File.separator+ System.currentTimeMillis()+".jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap =getBitmapFromView(drawView);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue saving the image.");
        }
        scanGallery( context,pictureFile.getAbsolutePath());
        return pictureFile;
    }
    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
    // used for scanning gallery
    private void scanGallery(Context cntx, String path) {
        try {
            MediaScannerConnection.scanFile(cntx, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}