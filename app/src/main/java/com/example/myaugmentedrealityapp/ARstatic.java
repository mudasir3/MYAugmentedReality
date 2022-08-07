package com.example.myaugmentedrealityapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.SkeletonNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARstatic extends AppCompatActivity {

    private static final String TAG = ARstatic.class.getSimpleName();
    private ArFragment arFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_rstatic);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.setOnTapArPlaneListener(
                (HitResult hitresult, Plane plane, MotionEvent motionevent) -> {
                  createModel(hitresult.createAnchor(),arFragment);
                }
        );
    }

    private void createModel(Anchor anchor, ArFragment arFragment){
        ModelRenderable.
                builder().setSource(this,R.raw.sophia)
                .build()
                .thenAccept(modelRenderable ->
                {
                    AnchorNode anchorNode =new AnchorNode(anchor);
                    //Skeleton Node
                    SkeletonNode skeletonNode = new SkeletonNode();
                    skeletonNode.setParent(anchorNode);
                    skeletonNode.setRenderable(modelRenderable);

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