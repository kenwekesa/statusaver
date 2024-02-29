package com.statuses.statussaver.fragments.whatsapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.statuses.statussaver.GenericAdapter;
import com.statuses.statussaver.HelperMethods;
import com.statuses.statussaver.InstanceHandler;
import com.statuses.statussaver.R;
import com.statuses.statussaver.adapter.WhatsappVideoAdapter;
import com.statuses.statussaver.model.ImageModel;
import com.statuses.statussaver.recycler.RecyclerClickListener;
import com.statuses.statussaver.recycler.RecyclerTouchListener;
import com.statuses.statussaver.recycler.ToolbarActionModeCallback;
import com.statuses.statussaver.viewer.SavedVideoViewer;
import com.statuses.statussaver.viewer.VideoPlayer;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

/*
 * A simple {@link Fragment} subclass.
 * Use the {@link WhatsappVideosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WhatsappSavedVideoFragment extends Fragment {
    private static WhatsappSavedVideoFragment mInstance;
    RecyclerView recyclerView;
    FragmentActivity activity;
    ProgressBar progressBar;
    private AdView mAdView;
    FloatingActionButton fab;
    WhatsappVideoAdapter waVideoAdapter;
    ArrayList<ImageModel> arrayList = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    private static View v;
    private ActionMode mActionMode;
    private InterstitialAd mInterstitialAd;

    public WhatsappSavedVideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_whatsapp_videos, container, false);
        activity = getActivity();
        mInstance = this;
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.ref_wa_video);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_wa_video);
        progressBar = (ProgressBar) v.findViewById(R.id.progressbar_wa_video);

        populateRecyclerView();
        implementRecyclerViewClickListeners();

        /*MobileAds.initialize(activity, getString(R.string.admob_app_id));
        mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());*/

          AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(getContext(),"ca-app-pub-7913609625908071/5998375754", adRequest,
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


        AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        mAdView = v.findViewById(R.id.vadView);
        mAdView.loadAd(adRequest);




        swipeRefreshLayout.setColorSchemeResources(new int[]{R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimaryDark});
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
       /* fab = (FloatingActionButton) v.findViewById(R.id.wa_video_fab_save_all);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAll();
                *//*if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }*//*
            }
        });*/
        return v;
    }

    private void populateRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        getStatus();
        waVideoAdapter = new WhatsappVideoAdapter(activity, arrayList);
        recyclerView.setAdapter(waVideoAdapter);
        waVideoAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    //Implement item click and long click over recycler view
    private void implementRecyclerViewClickListeners() {
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                //If ActionMode not null select item
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(getActivity());
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                if(Build.VERSION.SDK_INT>=24){
                    try{
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                if (mActionMode != null)
                    onListItemSelect(position);
                else {
                    String str = waVideoAdapter.getItem(position).getPath();
                    try {
                        Intent intent = new Intent(getActivity(), SavedVideoViewer.class);
                        intent.putExtra("pos", str);
                        intent.putExtra("position", position);
                        startActivityForResult(intent, 101);
                       /* Uri uri =  Uri.fromFile(new File(str));
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);*/
                      //  String mime = "*/*";
                       /* MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                        if (mimeTypeMap.hasExtension(
                                mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
                            mime = mimeTypeMap.getMimeTypeFromExtension(
                                    mimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                        intent.setDataAndType(uri,mime);
                        showInterstitialAd();
                        startActivity(intent);*/
                    } catch (Throwable e) {
                        throw new NoClassDefFoundError(e.getMessage());
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //Select item on long click
               // mActionMode = null;
               // onListItemSelect(position);
            }
        }));
    }


    //List item select method
    private void onListItemSelect(int position) {
        waVideoAdapter.toggleSelection(position);//Toggle the selection
        List<Fragment> fragments;

        boolean hasCheckedItems = waVideoAdapter.getSelectedCount() > 0;//Check if any items are already selected or not


        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ToolbarActionModeCallback(getActivity(),new GenericAdapter<WhatsappVideoAdapter>(waVideoAdapter), arrayList,new InstanceHandler<WhatsappSavedVideoFragment>(mInstance)));
        }
        else if (!hasCheckedItems && mActionMode != null)
        // there no selected items, finish the actionMode
        {
            mActionMode.finish();
            mActionMode=null;
        }

        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(waVideoAdapter
                    .getSelectedCount()) + " selected");


    }
    //Set action mode null after use
    public void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null;
    }

    private void saveAll() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);

        // set title
        alertDialogBuilder.setTitle("Save All Status");

        // set dialog message
        alertDialogBuilder
                .setMessage("This Action will Save all the available Video Statuses... \nDo you want to Continue?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        File[] listFiles = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatssappStatus").toString()).listFiles();
                        if (waVideoAdapter.getItemCount() == 0) {
                            Toast.makeText(activity, "No Status available to Save...", Toast.LENGTH_SHORT).show();
                        } else {
                            int i = 0;
                            while (i < listFiles.length) {
                                try {
                                    File file = listFiles[i];
                                    String str = file.getName().toString();
                                    if (str.endsWith(".mp4") || str.endsWith(".avi") || str.endsWith(".mkv") || str.endsWith(".gif")) {
                                        HelperMethods helperMethods = new HelperMethods(activity.getApplicationContext());
                                        HelperMethods.transfer(file);
                                    }
                                    i++;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }
                            Toast.makeText(activity, "Done :)", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });


        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black_overlay));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black_overlay));
            }
        });

        // show it
        alertDialog.show();
    }

    public void getStatus(){
        File[] listFiles = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsappStatus").toString()).listFiles();
        if (listFiles != null && listFiles.length >= 1) {
            Arrays.sort(listFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        }
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.getName().endsWith(".mp4") || file.getName().endsWith(".avi") || file.getName().endsWith(".mkv") || file.getName().endsWith(".gif")) {
                    ImageModel model=new ImageModel(file.getAbsolutePath());
                    arrayList.add(model);
                }
            }
        }
    }
    public void deleteRows() {
        SparseBooleanArray selected = waVideoAdapter
                .getSelectedIds();//Get selected ids

        //Loop all selected ids
        for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i)) {
                //If current id is selected remove the item via key
                arrayList.remove(selected.keyAt(i));
                waVideoAdapter.notifyDataSetChanged();//notify adapter

            }
        }
        Toast.makeText(getActivity(), selected.size() + " item deleted.", Toast.LENGTH_SHORT).show();//Show Toast
        mActionMode.finish();//Finish action mode after use

    }
    public void refresh() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        waVideoAdapter.updateData(new ArrayList<ImageModel>());
        populateRecyclerView();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null)  {
            if(resultCode == -1)
            {
                refresh();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                refresh();
            }
        }
    }
    public void showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(getActivity());
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }
}