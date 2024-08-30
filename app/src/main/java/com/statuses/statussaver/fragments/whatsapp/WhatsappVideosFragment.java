package com.statuses.statussaver.fragments.whatsapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
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
import com.statuses.statussaver.viewer.VideoPlayer;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * A simple {@link Fragment} subclass.
 * Use the {@link WhatsappVideosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WhatsappVideosFragment extends Fragment {
    private static WhatsappVideosFragment mInstance;
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

    public WhatsappVideosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_whatsapp_videos, container, false);
        activity = getActivity();
        mInstance = this;
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.ref_wa_video);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_wa_video);
        progressBar = (ProgressBar) v.findViewById(R.id.progressbar_wa_video);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                populateRecyclerView();
            } else {
                requestPermission();
            }
        } else {
            populateRecyclerView();
        }

        implementRecyclerViewClickListeners();
        implementRecyclerViewClickListeners();

        /*MobileAds.initialize(activity, getString(R.string.admob_app_id));
        mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());*/

        AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        mAdView = v.findViewById(R.id.vadView);
        AdRequest adRequest = new AdRequest.Builder().build();
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
                if (mActionMode != null)
                    onListItemSelect(position);
                else {
                    String str = waVideoAdapter.getItem(position).getPath();
                    try {
                        Intent intent = new Intent(getActivity(), VideoPlayer.class);
                        intent.putExtra("pos", str);
                        intent.putExtra("position", position);
                        startActivityForResult(intent, 101);
                    } catch (Throwable e) {
                        throw new NoClassDefFoundError(e.getMessage());
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //Select item on long click
                 mActionMode = null;
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
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ToolbarActionModeCallback(getActivity(),new GenericAdapter<WhatsappVideoAdapter>(waVideoAdapter), arrayList,new InstanceHandler<WhatsappVideosFragment>(mInstance)));
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
                        File[] listFiles = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsApp/Media/.Statuses/").toString()).listFiles();
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
                            Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
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

//    public void getStatus(){
//
//        File[] listFiles ={};
//
//        File[] listFiles1 = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsApp/Media/.Statuses/").toString()).listFiles();
//        File[] listFiles2 = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/Android/media/com.whatsapp/WhatsApp/Media/.Statuses/").toString()).listFiles();
//
//
//        if(listFiles1!=null && listFiles2!=null) {
//            listFiles = Arrays.copyOf(listFiles1, listFiles1.length + listFiles2.length);
//            System.arraycopy(listFiles2, 0, listFiles, listFiles1.length, listFiles2.length);
//        }
//        else if(listFiles1==null && listFiles2!=null)
//        {
//            listFiles = listFiles2;
//        }
//        else if(listFiles2==null && listFiles1!=null)
//        {
//            listFiles=listFiles1;
//        }
//
//
//        if (listFiles != null && listFiles.length >= 1) {
//            Arrays.sort(listFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
//        }
//        if (listFiles != null) {
//            for (File file : listFiles) {
//                if (file.getName().endsWith(".mp4") || file.getName().endsWith(".avi") || file.getName().endsWith(".mkv") || file.getName().endsWith(".gif")) {
//                    ImageModel model=new ImageModel(file.getAbsolutePath());
//                    arrayList.add(model);
//                }
//            }
//        }
//    }



//    public void getStatus() {
//        ArrayList<ImageModel> arrayList = new ArrayList<>();
//
//        // Check for the status directory using the appropriate storage access methods
//        Context context = getContext(); // Assuming you are in a context-aware environment, otherwise replace getContext() with your Context object
//
//        // Using MediaStore to access WhatsApp videos (considering internal storage)
//        String selection = MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE '%WhatsApp%' AND (" +
//                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?  OR" +  // Add OR operator
//                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)";
//        String[] selectionArgs = new String[]{
//                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
//                // Add video extensions here (ensure . at the beginning)
//                ".mp4",
//                ".mkv",
//                ".avi",
//                ".gif",
//                ".mov",  // Example extension
//                ".3gp"   // Example extension
//        };
//
//        Cursor cursor = context.getContentResolver().query(
//                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                null,
//                selection,
//                selectionArgs,
//                MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
//        );
//
//        if (cursor != null) {
//            try {
//                while (cursor.moveToNext()) {
//                    String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
//                    File file = new File(filePath);
//                    if (file.exists()) {
//                        ImageModel model = new ImageModel(filePath); // Change to VideoModel if applicable
//                        arrayList.add(model);
//                    }
//                }
//            } finally {
//                cursor.close();
//            }
//        }
//
//        // Now, 'arrayList' should contain most WhatsApp video statuses
//    }


    public void getStatus() {
        arrayList.clear(); // Clear existing items

        ContentResolver contentResolver = getContext().getContentResolver();
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[] {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DATA
        };

        String selection = MediaStore.Video.Media.DISPLAY_NAME + " LIKE ? AND " +
                MediaStore.Video.Media.DISPLAY_NAME + " NOT LIKE ? AND " +
                MediaStore.Video.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[] {
                "%.mp4",
                "%.nomedia",
                "%WhatsApp%"
        };

        String sortOrder = MediaStore.Video.Media.DATE_TAKEN + " DESC";

        try (Cursor cursor = contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

            while (cursor.moveToNext()) {
                String filePath = cursor.getString(dataColumn);
                ImageModel model = new ImageModel(filePath);
                arrayList.add(model);
            }
        }

        waVideoAdapter.notifyDataSetChanged();
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

    private static final int STORAGE_PERMISSION_CODE = 1;

    @TargetApi(Build.VERSION_CODES.M)
    public boolean requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, STORAGE_PERMISSION_CODE);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                getStatus();
            } else {
                Toast.makeText(getContext(), "Permissions are required to access statuses", Toast.LENGTH_LONG).show();
            }
        }
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
    public InterstitialAd getmInterstitialAd() {
        return mInterstitialAd;
    }
}