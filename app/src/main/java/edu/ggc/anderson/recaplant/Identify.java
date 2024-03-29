package edu.ggc.anderson.recaplant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.widget.TextView;

public class Identify extends AppCompatActivity {


    static final int REQUEST_TAKE_PHOTO = 1;
    String currentPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 3;
    ImageView imageView;
    public static final int REQUEST_CODE = 999;

    private static final int SELECT_PICTURE = 11;
    private String selectedImagePath;

    static final int CAMERA_REQUEST_CODE = 2;
    FirebaseVisionImage image;
    public String text;

    //creating a firebase model object builder
    public FirebaseAutoMLRemoteModel remoteModel =
            new FirebaseAutoMLRemoteModel.Builder("PoisonFlowers_2019124211524").build();
    Button firstAidButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);

        //defining buttons for camera functions
        Button btnCamera = findViewById(R.id.btnCamera);
        ImageView imageView = findViewById(R.id.ivIdentify);
        Button btnOpenGal = findViewById((R.id.btnOpenGal));



        //download the cloud model for use
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        //downloads a local copy of remote model. If user has no WiFi, it will use the most recently downloaded model.
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.v("FOUNDMODEL","DONE LOADING");
                    }
                });


        //listen for a person clicking the camera button
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call take picture method
                dispatchTakePictureIntent();

            }
        });

        //listen for person clicking gallery button
        btnOpenGal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open phone gallery and save image
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_PICTURE);

            }
        });

    }
     //Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            //sending the picture to firebase by creating a file path to send via firebase
            if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {

                    Uri selectedImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getPathFromURI(selectedImageUri);
                    if (path != null) {
                        File f = new File(path);
                        selectedImageUri = Uri.fromFile(f);
                        Log.v("GOTTHETAG","gotit");
                    }
                    // Set the image in ImageView
                    ImageView imageView = (ImageView)findViewById(R.id.ivIdentify);
                    imageView.setImageURI(null);
                    imageView.setImageURI(selectedImageUri);

                    //FirebaseVisionImage image;
                    try {
                        image = FirebaseVisionImage.fromFilePath(getApplicationContext(), selectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                    //check if model is downloaded
                    FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                @Override
                                public void onSuccess(Boolean isDownloaded) {
                                    FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder = null;
                                    if (isDownloaded) {
                                        optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel);
                                    } else {
                                        Log.v("ModelError","Coudltn Find remote model");
                                       // optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel);
                                    }
                                    //setting the confidence level of the model. If it is 51% sure it's a correct prediction, then present the prediction.
                                    FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder
                                            .setConfidenceThreshold(0.51f)  // Evaluate your model in the Firebase console
                                            // to determine an appropriate threshold.
                                            .build();

                                    //firebase code if the prediction is a success IN THE GALLERY, present the data in an image view, and populate text view with name of prediction and confidence level.
                                    FirebaseVisionImageLabeler labeler;
                                    try {
                                        labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
                                        labeler.processImage(image)
                                                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                                    @Override
                                                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                                                        // Task completed successfully
                                                        Log.v("TESTCMPLETE","HOLY TOLEDO");
                                                        for (FirebaseVisionImageLabel label: labels) {
                                                             text = label.getText();
                                                            float confidence = label.getConfidence();
                                                            Log.v("Label",text);
                                                            //String entityId = label.getEntityId();
                                                            TextView classifier = findViewById(R.id.classifier);
                                                            classifier.setText(text + "\n" + (100 * confidence) +"% Confidence");

                                                        }
                                                        //makes first aid button visible if prediction is poisonous plant
                                                        if(text.toLowerCase().contains("pois") || text.toLowerCase().contains("bull")){
                                                            Button firstAidButton = findViewById(R.id.firsAidButton);
                                                            firstAidButton.setVisibility(View.VISIBLE);
                                                            Log.v("Button","ISVISIBLE");

                                                            firstAidButton.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Intent intent = new Intent(Identify.this, FirstAidTest.class);
                                                                    startActivityForResult(intent,998);
                                                                }
                                                            });

                                                        }else{
                                                            Button firstAidButton = findViewById(R.id.firsAidButton);
                                                            firstAidButton.setVisibility(View.INVISIBLE);
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Task failed with an exception
                                                        // ...
                                                    }
                                                });
                                    } catch (FirebaseMLException e) {
                                        // Error.
                                    }
                                }

                            });

                }

            //firebase code if the prediction is a success CAMERA RESULT, present the data in an image view, and populate text view with name of prediction and confidence level.
                else if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {
                    // Show preview
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ImageView imageView = findViewById(R.id.ivIdentify);
                    imageView.setImageBitmap(imageBitmap);
                    image = FirebaseVisionImage.fromBitmap(imageBitmap);


                FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean isDownloaded) {
                                FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder = null;
                                if (isDownloaded) {
                                    optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel);
                                } else {
                                    Log.v("ModelError","Coudltn Find remote model");
                                    // optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel);
                                }
                                FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder
                                        .setConfidenceThreshold(0.51f)  // Evaluate your model in the Firebase console
                                        // to determine an appropriate threshold.
                                        .build();

                                FirebaseVisionImageLabeler labeler;
                                try {
                                    labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
                                    labeler.processImage(image)
                                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                                @Override
                                                public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                                                    // Task completed successfully
                                                    Log.v("TESTCMPLETE","HOLY TOLEDO");
                                                    for (FirebaseVisionImageLabel label: labels) {
                                                        text = label.getText();
                                                        float confidence = label.getConfidence();
                                                        Log.v("Label",text);
                                                        //String entityId = label.getEntityId();
                                                        TextView classifier = findViewById(R.id.classifier);
                                                        classifier.setText(text + "" + "\n" + "" + (100 * confidence) + "% Confidence");

                                                    }
                                                    //makes first aid button visible if prediction is poisonous plant
                                                    if(text.toLowerCase().contains("pois") || text.toLowerCase().contains("bull")){
                                                        Button firstAidButton = findViewById(R.id.firsAidButton);
                                                        firstAidButton.setVisibility(View.VISIBLE);
                                                        Log.v("Button","ISVISIBLE");

                                                        firstAidButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Intent intent = new Intent(Identify.this, FirstAidTest.class);
                                                                startActivityForResult(intent,998);
                                                            }
                                                        });

                                                    }else{
                                                        Button firstAidButton = findViewById(R.id.firsAidButton);
                                                        firstAidButton.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            })

                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Task failed with an exception
                                                    // ...
                                                }
                                            });
                                } catch (FirebaseMLException e) {
                                    // Error.
                                }
                            }
                        });
                }


        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }

    }

    //takes the image presented, creates a file to be able to send to firebase.
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("FragmentActivity" ,"Error creating image file: " + ex);
            }

            // If the File was successfully created, start camera app
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "edu.ggc.anderson.recaplant.fileprovider",
                        photoFile);

                //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }

        }
    }

    private File createImageFile() throws IOException {

        // Create a unique filename
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFilename = "photo_" + timeStamp + ".jpg";

        // Create the file in the Pictures directory on external storage
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFilename);
        return image;
    }

    //non working function. Future use is to save the captured image to phones gallery.
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    //function used to get the photos unique path and send to firebase.
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public String getText() {
        return text;
    }
}
