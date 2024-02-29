package com.statuses.statussaver.viewer;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.statuses.statussaver.BuildConfig;
import com.statuses.statussaver.HelperMethods;
import com.statuses.statussaver.R;
import com.statuses.statussaver.adapter.ImageViewPagerAdapter;
import com.statuses.statussaver.model.ImageModel;

import java.io.File;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class ImageViewer extends AppCompatActivity {
    HelperMethods helperMethods;
    private InterstitialAd mInterstitialAd;
    FloatingActionMenu floatingMenu;
    int position=0;
    File f;

    public ViewPager viewPager;
    ArrayList<ImageModel> imageList= new ArrayList<>();
    ImageViewPagerAdapter adapter;


    class SomeClass implements View.OnClickListener {
        private final ImageViewer imageViewer;
        private final File file;

        class SomeOtherClass implements Runnable {
            private final SomeClass context;
            private final File file;

            SomeOtherClass(SomeClass someClass, File file) {
                context = someClass;
                this.file = file;
            }


            @Override
            public void run() {
                try {
                    HelperMethods.transfer(this.file);
                    Toast.makeText(getApplicationContext(), "Image successfully saved to Gallery", Toast.LENGTH_SHORT).show();
                   /* if (mInterstitialAd.isLoaded()) {
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

        SomeClass(ImageViewer imageViewer, File file) {
            this.imageViewer = imageViewer;
            this.file = file;

        }

        @Override
        public void onClick(View view) {
            new SomeOtherClass(this, this.file).run();
        }
    }






    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        helperMethods = new HelperMethods(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setIcon(R.drawable.business_notif);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().hide();
        final Intent intent = getIntent();
        String string = intent.getExtras().getString("pos");
        position = intent.getExtras().getInt("position");


        imageList =(ArrayList<ImageModel>) intent.getSerializableExtra("imageslist");
        int positionn = intent.getIntExtra("position",0);


        adapter = new ImageViewPagerAdapter(this, imageList);

      /*  MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
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

        /*mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
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


        viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(positionn);
        adapter.notifyDataSetChanged();

        String str = imageList.get(viewPager.getCurrentItem()).getPath();
        f = new File(str);

       // f = new File(string);

        PhotoView photoView = (PhotoView) findViewById(R.id.photo);
        floatingMenu = (FloatingActionMenu) findViewById(R.id.menu);
        FloatingActionButton save_fab = (FloatingActionButton) findViewById(R.id.save);
        //FloatingActionButton floatingActionButton2 = (FloatingActionButton) findViewById(R.id.wall);
        FloatingActionButton share_fab = (FloatingActionButton) findViewById(R.id.share);
        FloatingActionButton delete_fab = (FloatingActionButton) findViewById(R.id.dlt);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("deleteFab", true)) {
            delete_fab.setVisibility(View.VISIBLE);
        } else {
            delete_fab.setVisibility(View.GONE);
        }
        save_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             File file_to_save = new File(imageList.get(viewPager.getCurrentItem()).getPath());

                    try {
                        HelperMethods.transfer(file_to_save);
                        Toast.makeText(getApplicationContext(), "Image successfully saved to Gallery", Toast.LENGTH_SHORT).show();
                   /* if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }*/
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("GridView", new StringBuffer().append("onClick: Error: ").append(e.getMessage()).toString());
                    }


            }
        });


       /* floatingActionButton2.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                Intent intent;
                Uri uriForFile;
                if (Build.VERSION.SDK_INT >= 24) {
                    uriForFile = FileProvider.getUriForFile(getApplicationContext(), new StringBuffer().append(getApplicationContext().getPackageName()).append(".provider").toString(),f);
                    intent = new Intent("android.intent.action.ATTACH_DATA");
                    intent.setDataAndType(uriForFile, "image/*");
                    intent.putExtra("mimeType", "image/*");
                    intent.addFlags(1);
                    startActivity(Intent.createChooser(intent, "Set as: "));
                    return;
                }
                uriForFile = Uri.parse(new StringBuffer().append("file://").append(f.getAbsolutePath()).toString());
                intent = new Intent("android.intent.action.ATTACH_DATA");
                intent.setDataAndType(uriForFile, "image/*");
                intent.putExtra("mimeType", "image/*");
                intent.addFlags(Intent.EXTRA_DOCK_STATE_DESK);
                startActivity(Intent.createChooser(intent, "Set as: "));
            }
        });*/
        share_fab.setOnClickListener(new View.OnClickListener() {


            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {

                File file_to_share = new File(imageList.get(viewPager.getCurrentItem()).getPath());
                Intent intent;
                Parcelable uriForFile;
               /* if (Build.VERSION.SDK_INT >= 24) {
                    uriForFile = FileProvider.getUriForFile(getApplicationContext(), new StringBuffer().append(getPackageName()).append(".provider").toString(),f);
                    try {
                        intent = new Intent("android.intent.action.SEND");
                        intent.setType("image/*");
                        intent.setPackage("com.whatsapp");
                        intent.putExtra("android.intent.extra.STREAM", uriForFile);
                        intent.addFlags(Intent.EXTRA_DOCK_STATE_DESK);
                        startActivity(intent);
                        startActivity(intent);
                        return;
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "WhatsApp Not Found on this Phone :(", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                uriForFile = Uri.parse(new StringBuffer().append("file://").append(f.getAbsolutePath()).toString());
                try {
                    intent = new Intent("android.intent.action.SEND");
                    intent.setType("image/*");
                    intent.setPackage("com.whatsapp");
                    intent.putExtra("android.intent.extra.STREAM", uriForFile);
                    startActivity(intent);
                } catch (ActivityNotFoundException e2) {
                    Toast.makeText(getApplicationContext(), "WhatsApp Not Found on this Phone :(", Toast.LENGTH_SHORT).show();
                }*/

                if (mInterstitialAd != null) {
                    mInterstitialAd.show(ImageViewer.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    uriForFile = FileProvider.getUriForFile(getApplicationContext(), new StringBuffer().append(getApplicationContext().getPackageName()).append(".provider").toString(), file_to_share);
                    try {
                        String shareMessage = "\n🌟 Just shared using our awesome Status Saver app! 🚀 Get it now, save and share your favorite statuses! 💥\n\n";
                        shareMessage += "Experience the ultimate convenience: \n";
                        shareMessage += "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";

                        //intent = new Intent("android.intent.action.SEND");
                        intent = new Intent();
                        intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra("android.intent.extra.STREAM", uriForFile);

                        intent.setType("*/*");
                        //intent.setPackage("com.whatsapp");
                        //intent.putExtra("android.intent.extra.STREAM", uriForFile);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.EXTRA_DOCK_STATE_DESK);
                        //startActivity(intent);


                        ImageViewer.this.startActivity(Intent.createChooser(intent, "Share Image on: "));
                        return;
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "App not found on your phone: ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                uriForFile = Uri.parse(new StringBuffer().append("file://").append(f.getAbsolutePath()).toString());
                try {
//                    String shareMessage= "\nStatus shared from one awesome Whatsapp status saver application. Check it out here\n\n";
//                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
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


                    ImageViewer.this.startActivity(Intent.createChooser(intent, "Share image on: "));

                } catch (ActivityNotFoundException e2) {
                    Toast.makeText(getApplicationContext(), "App not yet installed on your phone", 0).show();
                }
            }
        });
        delete_fab.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                File file_to_delete = new File(imageList.get(viewPager.getCurrentItem()).getPath());
               AlertDialog.Builder builder = new AlertDialog.Builder(ImageViewer.this);
               builder.setMessage("Are sure with deletion?").setNegativeButton("No", new DialogInterface.OnClickListener() {

                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       dialogInterface.dismiss();
                   }
               }).setPositiveButton("Delete", new DialogInterface.OnClickListener() {


                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                      digStash(file_to_delete);
                       if (mInterstitialAd != null) {
                           mInterstitialAd.show(ImageViewer.this);
                       } else {
                           Log.d("TAG", "The interstitial ad wasn't ready yet.");
                       }

                       finish();
                   }
               });
               builder.create().show();


            }
        });
    }

    public void digStash(File file_to_delete) {
        if (file_to_delete.exists()) {
            file_to_delete.delete();
            Toast.makeText(getApplicationContext(), "Image Deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
        Intent intent = new Intent();
        intent.putExtra("pos", this.position);
        setResult(-1, intent);
        finish();
    }

    public View.OnClickListener downloadMediaItem(File file_to_save) {
        //File file = new File(imageList.get(viewPager.getCurrentItem()).getPath());


return null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public InterstitialAd getmInterstitialAd() {
        return mInterstitialAd;
    }
}