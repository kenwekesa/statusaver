package com.statuses.statussaver.viewer;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;


import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.statuses.statussaver.BuildConfig;
import com.statuses.statussaver.HelperMethods;
import com.statuses.statussaver.R;

import java.io.File;

import static android.content.ContentValues.TAG;

public class VideoPlayer extends AppCompatActivity{
    HelperMethods helperMethods;
    FloatingActionMenu menu;
    private InterstitialAd mInterstitialAd;

    VideoView player;

    int position=0;
    File f;
    class SomeClass implements View.OnClickListener {
        private final VideoPlayer videoPlayer;
        private final File file;

        class SomeOtherClass implements Runnable {
            private final VideoPlayer.SomeClass context;
            private final File file;

            SomeOtherClass(VideoPlayer.SomeClass someClass, File file) {
                context = someClass;
                this.file = file;
            }



            @Override
            public void run() {
                try {
                    HelperMethods.transfer(this.file);
                    Toast.makeText(getApplicationContext(), "Video successfully saved to gallery.", Toast.LENGTH_SHORT).show();
                    /*if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("GridView", new StringBuffer().append("onClick: Error: ").append(e.getMessage()).toString());
                }

            }
        }

        SomeClass(VideoPlayer videoPlayer, File file) {
            this.videoPlayer = videoPlayer;
            this.file = file;
        }

        @Override
        public void onClick(View view) {
            new VideoPlayer.SomeClass.SomeOtherClass(this, this.file).run();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        helperMethods = new HelperMethods(this);
        this.helperMethods = new HelperMethods(this);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
//        getSupportActionBar().setIcon(R.drawable.business_notif);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().hide();
        Intent intent = getIntent();
        this.f = new File(intent.getExtras().getString("pos"));
        this.position = intent.getExtras().getInt("position");

       /* MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());*/


        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-7913609625908071/5998375754", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });

       /* mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                Log.d("TAG", "The ad was dismissed.");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when fullscreen content failed to show.
                Log.d("TAG", "The ad failed to show.");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                // Make sure to set your reference to null so you don't
                // show it a second time.
                mInterstitialAd = null;
                Log.d("TAG", "The ad was shown.");
            }
        });*/


        this.menu = (FloatingActionMenu) findViewById(R.id.menu);
        FloatingActionButton save_fab = (FloatingActionButton) findViewById(R.id.save);
        FloatingActionButton share_fab = (FloatingActionButton) findViewById(R.id.share);
        FloatingActionButton delete_fab = (FloatingActionButton) findViewById(R.id.dlt);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("deleteFab", true)) {
            delete_fab.setVisibility(View.VISIBLE);
        } else {
            delete_fab.setVisibility(View.GONE);
        }
        save_fab.setOnClickListener(downloadMediaItem(this.f));
        share_fab.setOnClickListener(new View.OnClickListener() {


            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                Parcelable uriForFile;
                Intent intent;

                if (mInterstitialAd != null) {
                    mInterstitialAd.show(VideoPlayer.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    uriForFile = FileProvider.getUriForFile(getApplicationContext(), new StringBuffer().append(getApplicationContext().getPackageName()).append(".provider").toString(), f);
                    try {
                        //intent = new Intent("android.intent.action.SEND");
                        String shareMessage = "\n🌟 Just shared using our awesome Status Saver app! 🚀 Get it now, save and share your favorite statuses! 💥\n\n";
                        shareMessage += "Experience the ultimate convenience: \n";
                        shareMessage += "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
                        intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra("android.intent.extra.STREAM", uriForFile);
                        intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        intent.setType("*/*");
                        //intent.setPackage("com.whatsapp");
                        //intent.putExtra("android.intent.extra.STREAM", uriForFile);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.EXTRA_DOCK_STATE_DESK);
                        //startActivity(intent);


                        VideoPlayer.this.startActivity(Intent.createChooser(intent, "Share video via: "));
                        return;
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "App not found on your phone: ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                uriForFile = Uri.parse(new StringBuffer().append("file://").append(f.getAbsolutePath()).toString());
                try {
                    String shareMessage = "\n🌟 Just shared using our awesome Status Saver app! 🚀 Get it now, save and share your favorite statuses! 💥\n\n";
                    shareMessage += "Experience the ultimate convenience: \n";
                    shareMessage += "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra("android.intent.extra.STREAM", uriForFile);
                    intent.putExtra(Intent.EXTRA_TEXT, shareMessage);

                    intent.setType("*/*");
                    //intent.setPackage("com.whatsapp");
                    //intent.putExtra("android.intent.extra.STREAM", uriForFile);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.EXTRA_DOCK_STATE_DESK);
                    //startActivity(intent);


                    VideoPlayer.this.startActivity(Intent.createChooser(intent, "Share video via: "));

                } catch (ActivityNotFoundException e2) {
                    Toast.makeText(getApplicationContext(), "App not yet installed on your phone", 0).show();
                }
            }
        });
        delete_fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
              AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayer.this);
              builder.setMessage("Are you sure with the deletion?").setNegativeButton("No", new DialogInterface.OnClickListener() {

                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                      dialogInterface.dismiss();
                  }
              }).setPositiveButton("Delete", new DialogInterface.OnClickListener() {


                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                      DeleteVideo();
                      Toast.makeText(getApplicationContext(), "Video Deleted", Toast.LENGTH_SHORT).show();

                      if (mInterstitialAd != null) {
                          mInterstitialAd.show(VideoPlayer.this);
                      } else {
                          Log.d("TAG", "The interstitial ad wasn't ready yet.");
                      }

                      finish();

                  }
              });
              builder.create().show();

            }
        });
        player = findViewById(R.id.player);
        //this.player.setCallback(this);
        Uri ur = Uri.fromFile(this.f);
        player.setVideoURI(Uri.fromFile(this.f));

        //setSource(Uri.fromFile(this.f));

        final MediaController mediaController = new MediaController(this);
        player.setMediaController(mediaController);

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {

                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                        mediaController.setAnchorView(player);
                    }
                });


                    player.start();

            }
        });




        //this.player.start();
    }

    public void DeleteVideo() {
        if (this.f.exists()) {
            this.f.delete();
        }
        Intent intent = new Intent();
        intent.putExtra("pos", this.position);
        setResult(-1, intent);
    }


    public View.OnClickListener downloadMediaItem(File file) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(VideoPlayer.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
        return new VideoPlayer.SomeClass(this, file);
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.player.pause();
    }



//    @Override
//    public void onClickVideoFrame(EasyVideoPlayer easyVideoPlayer) {
//        if (this.menu.isMenuButtonHidden()) {
//            this.menu.showMenuButton(true);
//            easyVideoPlayer.hideControls();
//            return;
//        }
//        this.menu.hideMenuButton(true);
//        easyVideoPlayer.showControls();
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }


}
