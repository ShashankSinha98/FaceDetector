package com.example.facedetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.facedetector.Helper.GraphicOverlay;
import com.example.facedetector.Helper.RectOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class FaceDetectorCamera extends AppCompatActivity {

    private Button faceDetectBtn;
    private GraphicOverlay graphicOverlay;
    private CameraView cameraView;
    private TextView resultDisplayTV;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detector_camera);

        faceDetectBtn = findViewById(R.id.detect_face_btn);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        cameraView = findViewById(R.id.camera_view);
        resultDisplayTV = findViewById(R.id.results_tv);

        alertDialog = new SpotsDialog.Builder().setContext(this)
                            .setMessage("Processing Image, Please Wait...")
                            .setCancelable(false)
                            .build();

        faceDetectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.start();
                cameraView.captureImage();
                graphicOverlay.clear();
            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                alertDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop();

                processFaceDetection(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });



    }

    private void processFaceDetection(Bitmap bitmap) {

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector();

        detector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {

                faceDetectBtn.setEnabled(true);
                processFaceRecognitionResult(firebaseVisionFaces);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                faceDetectBtn.setEnabled(false);
                resultDisplayTV.setText("Error : "+e.getMessage());
            }
        });
    }

    private void processFaceRecognitionResult(List<FirebaseVisionFace> firebaseVisionFaces) {

        if(firebaseVisionFaces.size()==0){
            resultDisplayTV.setText("No Face Detected");
            alertDialog.dismiss();
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
        alertDialog.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cameraView.stop();
    }
}
