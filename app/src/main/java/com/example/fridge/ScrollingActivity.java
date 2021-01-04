package com.example.fridge;

import android.os.Bundle;
import android.os.Environment;
import android.widget.LinearLayout;
import androidx.cardview.widget.CardView;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.Activity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.content.Intent;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;
import androidx.annotation.NonNull;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Base64;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.util.Locale;
import java.util.UUID;

public class ScrollingActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static LinearLayout viewList;
    private Context context;
    private String bananaStr = "banana";
    private String grapefruitStr = "grapefruit";
    private String dragonStr = "dragon_fruit";

    Uri myPicture = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        this.viewList = findViewById(R.id.list);
        toolBarLayout.setTitle(getTitle());

        AndroidNetworking.initialize(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void captureImage(View view){
        ContentValues values = new ContentValues();
        values.put(Media.TITLE, "My demo image");
        values.put(Media.DESCRIPTION, "Image Captured by Camera via an Intent");
        myPicture = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, myPicture);
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encodedStr = Base64.encodeToString(byteArray, Base64.NO_WRAP);

            try {
                UUID uuid=UUID.randomUUID();
                String filename = uuid.toString() + ".json";

                JSONObject jsonObject = new JSONObject();
                JSONObject imageObject = new JSONObject();
                JSONObject payloadObject = new JSONObject();
                imageObject.put("imageBytes", encodedStr);
                jsonObject.put("image", imageObject);
                payloadObject.put("payload", jsonObject);
                String jsonStr = "{\"payload\":{\"image\":{\"imageBytes\": \""+encodedStr +"\"}}}";

            AndroidNetworking.post("https://automl.googleapis.com/v1beta1/projects/650918406643/locations/us-central1/models/ICN6627831902977916928:predict")
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("Authorization", "Bearer $(your gcloud access-token)")
                    .addStringBody(jsonStr)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String name = (String)((JSONObject)((JSONArray)response.get("payload")).get(0)).get("displayName");
                                if (name.toLowerCase().equals(bananaStr)) {
                                    CardView card = new CardView(context);
                                    ImageView image = new ImageView(context);
                                    image.setImageResource(R.drawable.banana);
                                    image.setMaxHeight(5);
                                    image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    card.addView(image);
                                    viewList.addView(card);
                                } else if (name.toLowerCase().equals(grapefruitStr)) {
                                    CardView card = new CardView(context);
                                    ImageView image = new ImageView(context);
                                    image.setImageResource(R.drawable.grapefruit);
                                    image.setMaxHeight(5);
                                    image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    card.addView(image);
                                    viewList.addView(card);
                                } else {
                                    CardView card = new CardView(context);
                                    ImageView image = new ImageView(context);
                                    image.setImageResource(R.drawable.dragon);
                                    image.setMaxHeight(5);
                                    image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    card.addView(image);
                                    viewList.addView(card);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onError(ANError error) {
                            System.out.println("onError errorCode : " + error.getErrorCode());
                            System.out.println("onError errorBody : " + error.getErrorBody());
                            System.out.println("onError errorDetail : " + error.getErrorDetail());
                            System.out.println(error.toString());
                        }
                    });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}