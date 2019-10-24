package edu.ggc.anderson.recaplant;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Identify extends AppCompatActivity {

    //1 request photo
    static final int REQUEST_TAKE_PHOTO = 1;
    //path for photo
    String currentPhotoPath;
    //captured picture req
    static final int REQUEST_IMAGE_CAPTURE = 1;
    //Show img
    ImageView imageView;
    //req code
    public static final int REQUEST_CODE = 999;

    @Override
    //return nothing but accept the bundle -> saveInstanceState
    protected void onCreate(Bundle savedInstanceState) {
        //From the super class -> use the saveInstanceState on create
        super.onCreate(savedInstanceState);
        // set The content view -> using the R layout==> of activity_identify.xml
        setContentView(R.layout.activity_identify);

        //Create a camera button --> which opens the camera on the emulator
        Button btnCamera = findViewById(R.id.btnCamera);

        //Design on top before the open camera button.
        ImageView imageView = findViewById(R.id.ivIdentify);

        //When the button is clicked
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open this function
                dispatchTakePictureIntent();
                //startActivityForResult(intent, REQUEST_CODE);


            }
        });



//        btnCamera.setOnClickListener((v)  -> dispatchTakePictureIntent();)


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
        //super.onActivityResult(requestCode, resultCode, data);
        //Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        //imageView.setImageBitmap(bitmap);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "edu.ggc.anderson.recaplant",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}
