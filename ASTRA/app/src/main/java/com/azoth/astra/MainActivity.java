package com.azoth.astra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final String SERVER_ADDRESS = "http://10.0.2.2:8080/azoth/";
    ImageView uploadimage, downloadimage;
    Button upload, download;
    EditText up, down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadimage = (ImageView) findViewById(R.id.imageToUpload);
        downloadimage = (ImageView) findViewById(R.id.downloadedImage);

        up = (EditText) findViewById(R.id.etUploadName);
        down = (EditText) findViewById(R.id.etDownloadName);

        upload = (Button) findViewById(R.id.bUploadImage);
        download = (Button) findViewById(R.id.bDownloadImage);

        uploadimage.setOnClickListener(this);
        upload.setOnClickListener(this);
        download.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageToUpload:
                Intent gallary = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //gallary.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(gallary, RESULT_LOAD_IMAGE);
                break;
            case R.id.bUploadImage:
                Bitmap image= ((BitmapDrawable) uploadimage.getDrawable()).getBitmap();
                new UploadImage(image, up.getText().toString()).execute();
                break;

            case R.id.bDownloadImage:
                new DownloadImage(down.getText().toString()).execute();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedimage= data.getData();
            uploadimage.setImageURI(selectedimage);
        }
    }

    private class UploadImage extends AsyncTask<Void, Void, Void>{

        Bitmap image;
        String name;

        public UploadImage(Bitmap image, String name) {
            this.image = image;
            this.name = name;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage= Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);

            ArrayList<NameValuePair> datalist= new ArrayList<>();
            datalist.add(new BasicNameValuePair("image", encodedImage));
            datalist.add(new BasicNameValuePair("name", name));

            HttpParams requestparams= getHttpRequestParams();
            HttpClient client = new DefaultHttpClient(requestparams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "ImageUpload.php");

            try{
                post.setEntity(new UrlEncodedFormEntity(datalist));
                client.execute(post);
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(getApplicationContext(), "Image Uploaded",  Toast.LENGTH_LONG).show();
        }
    }

    private class DownloadImage extends AsyncTask<Void, Void, Bitmap>{
        String name;
        public DownloadImage(String name) {
            this.name=name;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if(bitmap!=null){
                downloadimage.setImageBitmap(bitmap);
            }
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            String url = SERVER_ADDRESS + "uploads/"+ name + ".JPG";

            try{
                URLConnection connection = new URL(url).openConnection();
                connection.setConnectTimeout(1000*30);
                connection.setReadTimeout(1000*30);

                return BitmapFactory.decodeStream((InputStream) connection.getContent(), null, null);

            }catch(Exception e){
                e.printStackTrace();
                return null;
            }

        }
    }

    private HttpParams getHttpRequestParams(){
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 1000*30);
        HttpConnectionParams.setSoTimeout(httpParams, 1000*30);
        return httpParams;
    }

}
