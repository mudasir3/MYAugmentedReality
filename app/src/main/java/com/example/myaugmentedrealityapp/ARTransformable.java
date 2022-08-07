package com.example.myaugmentedrealityapp;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARTransformable extends AppCompatActivity {

    private static final String TAG = ARTransformable.class.getSimpleName();
    private ArFragment arFragment;
    private TransformableNode transformableNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r_transformable);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.setOnTapArPlaneListener(
                (HitResult hitresult, Plane plane, MotionEvent motionevent) -> {
                    createModel(hitresult.createAnchor(), arFragment);
                }
        );

    }

    private void createModel(Anchor anchor, ArFragment arFragment) {
        ModelRenderable.
                builder().setSource(this, R.raw.sophia)
                .build()
                .thenAccept(modelRenderable ->
                {
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    //Transformable Node
                    transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                    //Scale model
                    transformableNode.getScaleController().setMinScale(0.09f);
                    transformableNode.getScaleController().setMaxScale(0.1f);
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(modelRenderable);
//
                    arFragment.getArSceneView().getScene().addChild(anchorNode);

                })
                .exceptionally(throwable -> {
                            Log.e("ARRRR", "exceptionally");
                            Toast.makeText(arFragment.getContext(), "Error:" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            return null;
                        }
                );
    }
}