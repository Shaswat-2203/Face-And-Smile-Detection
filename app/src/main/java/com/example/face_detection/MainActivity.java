package com.example.face_detection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button cameraButton;
    private final static int REQUEST_TIME_CAPRTURE=456;
    private FirebaseVisionImage image;
    private FirebaseVisionFaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        cameraButton=findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null ){
                    startActivityForResult(takePictureIntent,REQUEST_TIME_CAPRTURE);
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_TIME_CAPRTURE&&resultCode==RESULT_OK)
        {
            Bundle extras=data.getExtras();
            Bitmap bitmap=(Bitmap) extras.get("data");
            detectFace(bitmap);
        }
    }

    private void detectFace(Bitmap bitmap) {

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .build();

        try {
            image=FirebaseVisionImage.fromBitmap(bitmap);
            detector=FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                String resultText="";
                int i=1;
                for (FirebaseVisionFace face: firebaseVisionFaces){
                    resultText=resultText.concat("\n"+i+".").concat("\nSmile: "+face.getSmilingProbability()*100+"%")
                    .concat("\nLeft eye open:"+face.getLeftEyeOpenProbability()*100+"%").concat("\nRight eye open:"+face.getRightEyeOpenProbability()*100+"%");

                    i++;
                }
                if (firebaseVisionFaces.size()==0){
                    Toast.makeText(MainActivity.this,"No Faces",Toast.LENGTH_SHORT).show();
                }
                else{
                    Bundle bundle=new Bundle();
                    bundle.putString(FaceDectect.RESULT_TEXT,resultText);
                    DialogFragment resultDialog=new ResultDialog();
                    resultDialog.setArguments(bundle);
                    resultDialog.setCancelable(false);
                    resultDialog.show(getSupportFragmentManager(),FaceDectect.RESULT_DIALOG);
                }
            }
        });

    }
}