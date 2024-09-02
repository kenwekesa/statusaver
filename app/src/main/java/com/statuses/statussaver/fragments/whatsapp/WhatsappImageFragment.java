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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
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
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.statuses.statussaver.GenericAdapter;
import com.statuses.statussaver.HelperMethods;
import com.statuses.statussaver.InstanceHandler;
import com.statuses.statussaver.R;
import com.statuses.statussaver.adapter.WhatsappImageAdapter;
import com.statuses.statussaver.model.ImageModel;
import com.statuses.statussaver.recycler.RecyclerClickListener;
import com.statuses.statussaver.recycler.RecyclerTouchListener;
import com.statuses.statussaver.recycler.ToolbarActionModeCallback;
import com.statuses.statussaver.viewer.ImageViewer;
import com.statuses.statussaver.viewer.SavedImageViewer;
import com.statuses.statussaver.viewer.SavedVideoViewer;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

/*
 * A simple {@link Fragment} subclass.
 * Use the {@link WhatsappImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WhatsappImageFragment extends Fragment {
    private static WhatsappImageFragment mInstance;
    RecyclerView recyclerView;
    FragmentActivity activity;
    ProgressBar progressBar;
    private AdView mAdView;
    FloatingActionButton fab;
    WhatsappImageAdapter waImageAdapter;
    private InterstitialAd mInterstitialAd;
    ArrayList<ImageModel> arrayList = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    private static View v;
    private ActionMode mActionMode;
    Fragment frg;
    FragmentTransaction ft=null;

    List<File> selected_Files = new ArrayList<>();

    private int STORAGE_PERMISSION_CODE = 11;

    public WhatsappImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_whatsapp_image, container, false);
        activity = getActivity();

      /* MobileAds.initialize(activity, getString(R.string.admob_app_id));
       mInterstitialAd = new InterstitialAd(activity);
       mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
       mInterstitialAd.loadAd(new AdRequest.Builder().build());*/

        mInstance = this;
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.ref);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_wa_image);

        waImageAdapter = new WhatsappImageAdapter(activity, arrayList);
        progressBar = (ProgressBar) v.findViewById(R.id.progressbar_wa);

        populateRecyclerView();
        implementRecyclerViewClickListeners();





      /*  AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");*/

        //Initialise mobile ads
        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        mAdView = v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


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
        swipeRefreshLayout.setColorSchemeResources(new int[]{R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimaryDark});
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(getActivity());
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
            }
        });
        /*fab =  v.findViewById(R.id.wa_image_fab_save_all);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(activity, "prob", Toast.LENGTH_SHORT).show();
                saveAll();
              *//*  if (mInterstitialAd.isLoaded()) {
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

//        waImageAdapter = new WhatsappImageAdapter(activity, arrayList);
        recyclerView.setAdapter(waImageAdapter);
        waImageAdapter.notifyDataSetChanged();
       if(checkPermission()) {
           getStatus();
       }
       else
       {
           requestPermission();
       }

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
                if (mActionMode != null)
                    onListItemSelect(position);
                else {
                    String str = waImageAdapter.getItem(position).getPath();
                    try {
                      /*  Intent intent = new Intent(getActivity(), ImageViewer.class);
                        intent.putExtra("pos", str);
                        intent.putExtra("position", position);
                        startActivityForResult(intent, 1);*/


                        Bundle bundle = new Bundle();
                        bundle.putSerializable("imageslist", arrayList);
                        Intent intent2 = new Intent(getContext(), ImageViewer.class);
                        // intent.putExtra("image_path",imageslist.get(position).getImage_path());
                        intent2.putExtra("imageslist", arrayList);
                        intent2.putExtra("position",position);
                        startActivity(intent2);
                    } catch (Throwable e) {
                        throw new NoClassDefFoundError(e.getMessage());
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //Select item on long click
              // mActionMode = null;
               //onListItemSelect(position);
            }
        }));
    }


    //List item select method
    private void onListItemSelect(int position) {
        waImageAdapter.toggleSelection(position);//Toggle the selection
        List<Fragment> fragments;
        File[] listFiles = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsApp/Media/.Statuses/").toString()).listFiles();
        selected_Files.add(listFiles[position]);
        boolean hasCheckedItems = waImageAdapter.getSelectedCount() > 0;//Check if any items are already selected or not


        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ToolbarActionModeCallback(getActivity(), new GenericAdapter<WhatsappImageAdapter>(waImageAdapter), arrayList, new InstanceHandler<WhatsappImageFragment>(mInstance)));
        }
        else if (!hasCheckedItems && mActionMode != null)
        // there no selected items, finish the actionMode
        {
            mActionMode.finish();
            mActionMode=null;
        }

        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(waImageAdapter
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
                .setMessage("This Action will Save all the available Image Statuses... \nDo you want to Continue?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        File[] listFiles1 = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsApp/Media/.Statuses/").toString()).listFiles();
                        File[] listFiles2 = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("Android/media/com.whatsapp/WhatsApp/Media/.Statuses/").toString()).listFiles();


                        List list = new ArrayList(Arrays.asList(listFiles1)); //returns a list view of an array
//returns a list view of str2 and adds all elements of str2 into list
                        list.addAll(Arrays.asList(listFiles2));


                        File[] listFiles = (File[]) list.toArray();         //converting list to array

                        if (waImageAdapter.getItemCount() == 0) {
                            Toast.makeText(activity, "No Status available to Save...", Toast.LENGTH_SHORT).show();
                        } else {
                            int i = 0;
                            while (i < listFiles.length) {
                                try {
                                    File file = listFiles[i];
                                    String str = file.getName().toString();
                                    if (str.endsWith(".jpg") || str.endsWith(".jpeg") || str.endsWith(".png")) {
                                        HelperMethods helperMethods = new HelperMethods(activity.getApplicationContext());
                                        HelperMethods.transfer(file);
                                    }
                                    i++;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }
                            Toast.makeText(activity, "Images saved.", Toast.LENGTH_SHORT).show();
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


    private void saveSelected() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);

        // set title
        alertDialogBuilder.setTitle("Save");

        // set dialog message
        alertDialogBuilder
                .setMessage("This Action will Save all the available Image Statuses... \nDo you want to Continue?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        File[] listFiles = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsApp/Media/.Statuses/").toString()).listFiles();
                        if (waImageAdapter.getItemCount() == 0) {
                            Toast.makeText(activity, "No Status available to Save...", Toast.LENGTH_SHORT).show();
                        } else {
                            int i = 0;
                            while (i < selected_Files.size()) {
                                try {
                                    File file = selected_Files.get(i);
                                    String str = file.getName().toString();
                                    if (str.endsWith(".jpg") || str.endsWith(".jpeg") || str.endsWith(".png")) {
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



    public void getStatus() {
        arrayList.clear(); // Clear existing items

        ContentResolver contentResolver = getContext().getContentResolver();
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA
        };

        String selection = MediaStore.Images.Media.DISPLAY_NAME + " LIKE ? AND " +
                MediaStore.Images.Media.DISPLAY_NAME + " NOT LIKE ? AND " +
                MediaStore.Images.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[] {
                "%.jpg",
                "%.nomedia",
                "%WhatsApp%"
        };

        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";

        try (Cursor cursor = contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            while (cursor.moveToNext()) {
                String filePath = cursor.getString(dataColumn);
                ImageModel model = new ImageModel(filePath);
                arrayList.add(model);
            }
        }
        if (waImageAdapter != null) {
        waImageAdapter.notifyDataSetChanged();
        } else {
            Log.e("WhatsappImageFragment", "Adapter is null");
        }
    }


    //    public void getStatus(){
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
//               //converting list to array
//        if (listFiles != null && listFiles.length >= 1) {
//            Arrays.sort(listFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
//        }
//        if (listFiles != null) {
//            for (File file : listFiles) {
//                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".png")) {
//                    ImageModel model=new ImageModel(file.getAbsolutePath());
//                    arrayList.add(model);
//                }
//            }
//        }
//    }
    public void deleteRows() {
        SparseBooleanArray selected = waImageAdapter
                .getSelectedIds();//Get selected ids

        //Loop all selected ids
        for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i)) {
                //If current id is selected remove the item via key
                arrayList.remove(selected.keyAt(i));
                waImageAdapter.notifyDataSetChanged();//notify adapter

            }
        }
        Toast.makeText(activity, selected.size() + " item deleted.", Toast.LENGTH_SHORT).show();//Show Toast
        mActionMode.finish();//Finish action mode after use

    }
    public void refresh() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
//        WhatsappImageAdapter.notifyDataSetChanged();
        waImageAdapter.updateData(new ArrayList<ImageModel>());
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

    public InterstitialAd getmInterstitialAd() {
        return mInterstitialAd;
    }


    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int WRITE_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED) || WRITE_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }



    @TargetApi(Build.VERSION_CODES.M)
    public boolean requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, STORAGE_PERMISSION_CODE);
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
                // Handle permission denied
                Toast.makeText(getContext(), "Permissions are required to access statuses", Toast.LENGTH_LONG).show();
            }
        }
    }
}