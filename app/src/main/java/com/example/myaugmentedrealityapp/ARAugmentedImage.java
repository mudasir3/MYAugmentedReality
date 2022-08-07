package com.example.myaugmentedrealityapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class ARAugmentedImage extends AppCompatActivity {

    private static final String TAG = ARAugmentedImage.class.getSimpleName();
    private ArSceneView arSceneView;
    private ArFragment arFragment;

    Session mSession;
    private boolean modelAdded = false; // add model once
    private boolean sessionConfigured = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r_augmented_image);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        // hiding the plane discovery
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        arSceneView= arFragment.getArSceneView();
    }


    //config can be intiated using new Config(session) where session is arfragment scene session
    private boolean setupAugmentedImageDb(Config config) {
        Log.e("AugmentImageee "," setupAugmentedImageDb ");

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

        Log.e("AugmentImageee "," loadAugmentedImage");

        try (InputStream is = getAssets().open("car.jpeg")){

            Log.e("AugmentImageee "," tryyy");

            return BitmapFactory.decodeStream(is);
        }
        catch (IOException e){
            Log.e("AugmentImageee", "IO Exception while loading", e);
        }
        return null;
    }

    private void onUpdateFrame(FrameTime frameTime){

        Log.e("AugmentImageee "," onUpdateFrame ");

        Frame frame = arFragment.getArSceneView().getArFrame();

        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage augmentedImage : augmentedImages){
            if (augmentedImage.getTrackingState() == TrackingState.TRACKING){
                Log.e("AugmentImageee "," TrackingState.TRACKING ");

                if (augmentedImage.getName().contains("car") && !modelAdded){

                    Log.e("AugmentImageee "," contains(\"car\") ");

                    renderObject(arFragment,
                            augmentedImage.createAnchor(augmentedImage.getCenterPose()),
                            R.raw.sophia);

                    modelAdded = true;
                }
            }
        }

    }


    private void renderObject(ArFragment fragment, Anchor anchor, int model){

        Log.e("AugmentImageee "," renderObject ");

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

        Log.e("AugmentImageee "," addNodeToScene ");

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
        Log.e("AugmentImageee "," configureSession ");

        Config config = new Config(mSession);
        if (!setupAugmentedImageDb(config)) {
            Toast.makeText(this, "Unable to setup augmented", Toast.LENGTH_SHORT).show();
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        mSession.configure(config);
    }
}