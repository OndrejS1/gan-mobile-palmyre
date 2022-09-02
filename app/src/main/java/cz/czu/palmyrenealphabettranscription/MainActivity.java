package cz.czu.palmyrenealphabettranscription;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.ActionBar;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    Button buttonMenuDrawLetter;
    Button buttonMenuCaptureLetter;
    Button buttonMenuAlphabet;
    Button buttonMenuHelp;
    Button buttonMenuInfo;

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if main activity is opened from result to scan another letter with camera
        boolean openCamera = getIntent().getBooleanExtra("OpenCamera", false);
        if (openCamera) {
            checkPermissionsAndOpenCamera();
        }

        buttonMenuDrawLetter = findViewById(R.id.button_drawLetter);
        buttonMenuDrawLetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent drawLetterActivityIntent = new Intent(MainActivity.this, DrawActivity.class);
                startActivity(drawLetterActivityIntent);
            }
        });

        buttonMenuCaptureLetter = findViewById(R.id.button_captureLetter);
        buttonMenuCaptureLetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionsAndOpenCamera();
            }
        });



        buttonMenuAlphabet = findViewById(R.id.button_showAlphabet);
        buttonMenuAlphabet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent alphabetActivityIntent = new Intent(MainActivity.this, AlphabetActivity.class);
                startActivity(alphabetActivityIntent);
            }
        });

        buttonMenuHelp = findViewById(R.id.button_help);
        buttonMenuHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent helpActivityIntent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(helpActivityIntent);
            }
        });
        buttonMenuInfo = findViewById(R.id.button_info);
        buttonMenuInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent infoActivityIntent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(infoActivityIntent);
            }
        });
    }
    private void checkPermissionsAndOpenCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED){
                //permission not enabled, request it
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                //show popup to request permissions
                requestPermissions(permission, PERMISSION_CODE);
            }
            else {
                //permission already granted
                openCamera();
            }
        }
        else {
            //system os < marshmallow
            openCamera();
        }
    }
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        try {
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Camera activity not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        switch (requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    //permission from popup was granted
                    openCamera();
                }
                else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //called when image was captured from camera
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap capturedBitmap = (Bitmap) extras.get("data");
            Log.d(TAG, "Photo captured. Photo thumbnail size: " + capturedBitmap.getWidth() + "x" + capturedBitmap.getHeight());
            Intent resultActivityIntent = new Intent(getBaseContext(), ResultActivity.class);
            resultActivityIntent.putExtra("InputType", "photo");
            resultActivityIntent.putExtra("BitmapForAnalysis", capturedBitmap); //shitty way to pass bitmap, but ok for this usage (max bitmap size is 1MB)
            startActivity(resultActivityIntent);
        }
    }
}