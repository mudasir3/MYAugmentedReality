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
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARwithAnimation extends AppCompatActivity {

    private static final String TAG = ARwithAnimation.class.getSimpleName();

    private ArFragment arFragment;
    private AnchorNode anchorNode;
    private ModelAnimator animator;
    private int nextAnimation;
    private ModelRenderable animation;
    private TransformableNode transformableNode;

    private int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_rwith_animation);

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
                    //Transformable Node
                    transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                    //Scale model
                    transformableNode.getScaleController().setMinScale(0.09f);
                    transformableNode.getScaleController().setMaxScale(0.1f);
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(modelRenderable);
//
                    arFragment.getArSceneView().getScene().addChild(anchorNode);

                    Button button =findViewById(R.id.btn);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            animateModel(modelRenderable);
                        }
                    });
                })
                .exceptionally(throwable -> {
                            Log.e("ARRRR", "exceptionally");
                            Toast.makeText(arFragment.getContext(), "Error:" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            return null;
                        }
                );
    }

    private void animateModel(ModelRenderable modelRenderable){
        if(animator != null && animator.isRunning())
            animator.end();

        int animationCount =modelRenderable.getAnimationDataCount();

        if(i == animationCount)
            i=0;

        AnimationData animationData =modelRenderable.getAnimationData(i);
        ModelAnimator modelAnimator= new ModelAnimator(animationData,modelRenderable);
        modelAnimator.start();
        i++;
    }

}