package edu.sjsu.android.threadeddownloads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ThreadedDownloadActivity extends Activity {
    private EditText url;
    private ImageView image;
    private ProgressDialog dialog;
    private String dialogMsg;

    //Handler handler_runRunnable = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.threaded_download_activity);

        url = (EditText) findViewById(R.id.url);
        url.setHint("Enter website to change default photo: ");

        image = (ImageView) findViewById(R.id.image);

        dialog = new ProgressDialog(this);
        dialogMsg = "";
    }

    public Bitmap downloadBitmap(String urlString)  {
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlC = (HttpURLConnection) url.openConnection();
            urlC.setDoInput(true);
            urlC.connect();
            InputStream input = urlC.getInputStream();
            Bitmap mBitmap = BitmapFactory.decodeStream(input);
            urlC.disconnect();
            dialog.dismiss();
            return mBitmap;
        } catch (IOException e) {
            dialog.dismiss();
            e.printStackTrace();
            return null;
        }
    }

    // Handlers and Runnable model
    public void runRunnable(View view) {
        dialogMsg = "Downloading via runRunnable. Please wait.";
        this.dialog.setMessage(dialogMsg);
        this.dialog.setCancelable(false);

        Handler handler = new Handler();
        String website = url.getText().toString();

        if (website.equals("")){
            Toast.makeText(this, "Please enter a url.", Toast.LENGTH_LONG).show();
        }
        else {
            this.dialog.show();
            Runnable backgroundRunnable = new Runnable() {
                @Override
                public void run() {
                    //download image:
                    Bitmap downloadedBitmapImage = downloadBitmap(url.getText().toString());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            image.setImageBitmap(downloadedBitmapImage);
                        }
                    });
                }
            };
            Thread backgroundThread = new Thread(backgroundRunnable);
            backgroundThread.start();
        }
    }

    // TODO: Handlers and Messages model
    public void runMessage(View view){
        dialogMsg = "Downloading via runMessage. Please wait.";
        this.dialog.setMessage(dialogMsg);
        this.dialog.setCancelable(false);
        
        String urlMessage = url.getText().toString();
        if (url.equals("")){
            Toast.makeText(this, "Please enter a url.", Toast.LENGTH_LONG).show();
        }
        else {
            //show dialog:
            this.dialog.show();

            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    Bitmap downloadedImage = (Bitmap) msg.obj;
                    image.setImageBitmap(downloadedImage);
                }
            };

            //thread:
            Thread background = new Thread(new Runnable() {
                public void run() {
                    try {
                        Bitmap downloadedImage = downloadBitmap(urlMessage);
                        Message msg = handler.obtainMessage(1, downloadedImage);
                        handler.sendMessage(msg);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }

                }
            });
            background.start();
        }
    }

    // TODO: Async Task model
    public void runAsyncTask(View view){
        String urlMessage = url.getText().toString();
        if (urlMessage.equals("")){
            Toast.makeText(this, "Please enter a url.", Toast.LENGTH_LONG).show();
        }
        else {
            new backgroundTask().execute(urlMessage);
        }
    }

    public class backgroundTask extends AsyncTask<String, Void, Void> {
        private final ProgressDialog myDialog = new ProgressDialog(ThreadedDownloadActivity.this);
        String waitMsg = "Downloading via AsyncTask. Please wait.";
        Bitmap downloadedImage;
        protected void onPreExecute(){
            //dialog method
            this.myDialog.setMessage(waitMsg);
            this.myDialog.setCancelable(false);
            this.myDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            String urlMessage = strings[0];
            if (urlMessage.equals("")){
                return null;
            }
            else {
                downloadedImage = downloadBitmap(urlMessage);
            }
            return null;
        }
        // can use UI thread here
        protected void onPostExecute(Void unused){
            image.setImageBitmap(downloadedImage);
            if (this.myDialog.isShowing()){
                this.myDialog.dismiss();
            }
        }
    }

    //reset image
    public void resetImage(View view){
        image.setImageResource(R.drawable.defaultphoto);
    }
}
