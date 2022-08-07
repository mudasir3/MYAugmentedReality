package com.example.myaugmentedrealityapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SkeletonNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
//    private ArFragment arFragment;


    private ArSceneView arSceneView;
    private ArFragment arFragment;
    private AnchorNode anchorNode;
    private ModelAnimator animator;
    private int nextAnimation;
    private ModelRenderable animation;
    private TransformableNode transformableNode;

    private int i=0;

    Session mSession;
    private boolean modelAdded = false; // add model once
    private boolean sessionConfigured = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this))
            return;

        setContentView(R.layout.activity_main);


        //2 Dragon
//        arFragment = (ArFragment)getSupportFragmentManager()
//                .findFragmentById(R.id.ux_fragment);
//        //Tap on plane event
//        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
//            @Override
//            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
//                if(animation ==null)
//                    return;
//                //Create the Anchor
//                Anchor anchor = hitResult.createAnchor();
//                if(anchorNode == null) //If crab is not place on plane
//                {
//                    anchorNode = new AnchorNode(anchor);
//                    anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//                    transformableNode = new TransformableNode(arFragment.getTransformationSystem());
//                    //Scale model
//                    transformableNode.getScaleController().setMinScale(0.09f);
//                    transformableNode.getScaleController().setMaxScale(0.1f);
//                    transformableNode.setParent(anchorNode);
//                    transformableNode.setRenderable(animation);
//                }
//            }
//        });
//
//        setupModel();

                arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        // hiding the plane discovery
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        arSceneView= arFragment.getArSceneView();

        arFragment.setOnTapArPlaneListener(
                (HitResult hitresult, Plane plane, MotionEvent motionevent) -> {
                  createModel(hitresult.createAnchor(),arFragment);
                }
        );

    }


    private void createModel(Anchor anchor,ArFragment arFragment){
        ModelRenderable.
                builder().setSource(this,R.raw.biplane)
                        .build()
                .thenAccept(modelRenderable ->
                        {
                            AnchorNode anchorNode =new AnchorNode(anchor);
                            SkeletonNode skeletonNode =new SkeletonNode();
                            skeletonNode.setParent(anchorNode);
                            skeletonNode.setRenderable(modelRenderable);
//                    transformableNode = new TransformableNode(arFragment.getTransformationSystem());
//                    //Scale model
//                    transformableNode.getScaleController().setMinScale(0.09f);
//                    transformableNode.getScaleController().setMaxScale(0.1f);
//                    transformableNode.setParent(anchorNode);
//                    transformableNode.setRenderable(modelRenderable);
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

    //config can be intiated using new Config(session) where session is arfragment scene session
    private boolean setupAugmentedImageDb(Config config) {
        AugmentedImageDatabase augmentedImageDatabase;
        Bitmap augmentedImageBitmap = loadAugmentedImage();
        if (augmentedImageBitmap == null) {
            return false;
        }
        augmentedImageDatabase = new AugmentedImageDatabase(mSession);
        augmentedImageDatabase.addImage("car", augmentedImageBitmap);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }
    private Bitmap loadAugmentedImage(){
        try (InputStream is = getAssets().open("car.jpeg")){
            return BitmapFactory.decodeStream(is);
        }
        catch (IOException e){
            Log.e("ImageLoad", "IO Exception while loading", e);
        }
        return null;
    }

    private void onUpdateFrame(FrameTime frameTime){
        Frame frame = arFragment.getArSceneView().getArFrame();

        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage augmentedImage : augmentedImages){
            if (augmentedImage.getTrackingState() == TrackingState.TRACKING){

                if (augmentedImage.getName().contains("car") && !modelAdded){
                    renderObject(arFragment,
                            augmentedImage.createAnchor(augmentedImage.getCenterPose()),
                            R.raw.sophia);

                    modelAdded = true;
                }
            }
        }

    }


    private void renderObject(ArFragment fragment, Anchor anchor, int model){
        ModelRenderable.builder()
                .setSource(this, model)
                .build()
                .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                .exceptionally((throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Error!");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return null;
                }));

    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable){
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSession != null) {

            arSceneView.pause();
            mSession.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSession == null) {
            String message = null;
            Exception exception = null;
            try {
                mSession = new Session(this);
            } catch (UnavailableArcoreNotInstalledException
                    e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update android";
                exception = e;
            } catch (Exception e) {
                message = "AR is not supported";
                exception = e;
            }

            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
            sessionConfigured = true;

        }
        if (sessionConfigured) {
            configureSession();
            sessionConfigured = false;

            arSceneView.setupSession(mSession);
        }


    }
    private void configureSession() {
        Config config = new Config(mSession);
        if (!setupAugmentedImageDb(config)) {
            Toast.makeText(this, "Unable to setup augmented", Toast.LENGTH_SHORT).show();
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        mSession.configure(config);
    }

//    private void setupModel() {
//        ModelRenderable.builder()
//                .setSource(this, R.raw.dragon)
//                .build()
//                .thenAccept(renderable -> animation = renderable)
//                .exceptionally(throwable -> {
//                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                    return null;
//                });
//    }

    private boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

//    private void placeObject(ArFragment arFragment, Anchor anchor, int uri) {
//
//        Log.e("ARRRR", "placeObject" + uri);
//
//        ModelRenderable.builder()
//                .setSource(arFragment.getContext(), uri)
//                .build()
//                .thenAccept(modelRenderable -> addNodeToScene(arFragment, anchor, modelRenderable))
//                .exceptionally(throwable -> {
//                    Log.e("ARRRR", "exceptionally");
//                    Toast.makeText(arFragment.getContext(), "Error:" + throwable.getMessage(), Toast.LENGTH_LONG).show();
//                            return null;
//                        }
//                );
//    }
//
//    private void addNodeToScene(ArFragment arFragment, Anchor anchor, Renderable renderable) {
//        Log.e("ARRRR", "addNodeToScene");
//
//        AnchorNode anchorNode = new AnchorNode(anchor);
//        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
//        node.setRenderable(renderable);
//        node.setParent(anchorNode);
//        arFragment.getArSceneView().getScene().addChild(anchorNode);
//        node.select();
//    }
}