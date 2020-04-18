package com.example.facedetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.facedetector.Helper.GraphicOverlay;
import com.example.facedetector.Helper.RectOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

public class FaceDetectorGallery extends AppCompatActivity {

    private String TAG = FaceDetectorGallery.class.getSimpleName();
    private ImageView imageView;
    private Bitmap bitmap;
    private FirebaseVisionImage firebaseVisionImage;
    private FirebaseVisionFaceDetector faceDetector;
    private TextView resultDisplayTV;
    private GraphicOverlay graphicOverlay;
    private Button chooseImage, searchFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detector_gallery);

        // Getting ID's
        imageView = findViewById(R.id.text_image);
        resultDisplayTV = findViewById(R.id.results_tv);
        chooseImage = findViewById(R.id.add_img_btn);
        searchFace = findViewById(R.id.scan_face_btn);
        graphicOverlay = findViewById(R.id.graphicOverlay);

        // Choose Img
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if(ContextCompat.checkSelfPermission(FaceDetectorGallery.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(FaceDetectorGallery.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                    } else {

                        bringImagePicker();
                    }
                } else {

                    bringImagePicker();
                }
            }
        });

        // Search Text
        searchFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultDisplayTV.setText("");

                // Getting bitmap from Imageview
                BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                bitmap = drawable.getBitmap();

                runFaceRecoginition();
            }
        });


    }

    private void runFaceRecoginition() {

        firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector();

        // Running Model on Bitmap
        faceDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                chooseImage.setEnabled(true);
                searchFace.setEnabled(true);

                processFaceRecognitionResult(firebaseVisionFaces);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                chooseImage.setEnabled(false);
                searchFace.setEnabled(false);
                resultDisplayTV.setText("Error : "+e.getMessage());
            }
        });

    }

    private void processFaceRecognitionResult(List<FirebaseVisionFace> firebaseVisionFaces) {

        if(firebaseVisionFaces.size()==0){
            resultDisplayTV.setText("No Face Detected");
            return;
        }

        int countFaces = firebaseVisionFaces.size();
        resultDisplayTV.setText("Results: "+ countFaces+" Faces Detected");
        for(FirebaseVisionFace face : firebaseVisionFaces){

            RectOverlay rectOverlay = new RectOverlay(graphicOverlay, face);
            graphicOverlay.add(rectOverlay);

            Float smilingProb = face.getSmilingProbability();
            Float rightEyeOpenProb = face.getRightEyeOpenProbability();
            Float leftEyeOpenProb = face.getLeftEyeOpenProbability();

            resultDisplayTV.setText(resultDisplayTV.getText()+"\nSmiling Probability: "+smilingProb
                    +"\nRight Eye Open Probability: "+rightEyeOpenProb+"\nLeft Eye Open Probability: "+leftEyeOpenProb);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                resultDisplayTV.setText("");
                graphicOverlay.clear();
                imageView.setImageURI(result.getUri());


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                resultDisplayTV.setText("Error Loading Image: "+error);

            }
        }
    }

    private void bringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1)
                .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                .start(FaceDetectorGallery.this);
    }


}
