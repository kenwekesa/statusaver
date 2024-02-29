package com.statuses.statussaver.fragments.whatsapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.os.StrictMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
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
 * Use the {@link WhatsappImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavedImageFragment extends Fragment {
    private static SavedImageFragment mInstance;
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

    private int STORAGE_PERMISSION_CODE = 11;

    public SavedImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_whatsapp_image, container, false);
        activity = getActivity();

        // MobileAds.initialize(activity, getString(R.string.admob_app_id));
        //mInterstitialAd = new InterstitialAd(activity);
        // mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        // mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInstance = this;
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.ref);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_wa_image);
        progressBar = (ProgressBar) v.findViewById(R.id.progressbar_wa);

        populateRecyclerView();
        implementRecyclerViewClickListeners();


       AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
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
            }
        });
       /* fab =  v.findViewById(R.id.wa_image_fab_save_all);
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

        if(checkPermission()) {
            getStatus();
        }
        else
        {
            requestPermission();
        }
        waImageAdapter = new WhatsappImageAdapter(activity, arrayList);
        recyclerView.setAdapter(waImageAdapter);
        waImageAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    //Implement item click and long click over recycler view
    private void implementRecyclerViewClickListeners() {
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                //If ActionMode not null select item

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
                    String str = waImageAdapter.getItem(position).getPath();
                    try {
                        /*Intent intent = new Intent(getActivity(), SavedImageViewer.class);
                        intent.putExtra("pos", str);
                        intent.putExtra("position", position);
                        startActivityForResult(intent, 1);*/
                        /*Uri uri =  Uri.fromFile(new File(str));
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);*/

                        //String mime = "*/*";
                       /* MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                        if (mimeTypeMap.hasExtension(
                                mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
                            mime = mimeTypeMap.getMimeTypeFromExtension(
                                    mimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                        intent.setDataAndType(uri,mime);
                        showInterstitialAd();
                        startActivity(intent);*/


                        Bundle bundle = new Bundle();
                        bundle.putSerializable("imageslist", arrayList);
                        Intent intent2 = new Intent(getContext(), SavedImageViewer.class);
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
                mActionMode = null;
                onListItemSelect(position);
            }
        }));
    }


    //List item select method
    private void onListItemSelect(int position) {
        waImageAdapter.toggleSelection(position);//Toggle the selection
        List<Fragment> fragments;

        boolean hasCheckedItems = waImageAdapter.getSelectedCount() > 0;//Check if any items are already selected or not


        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ToolbarActionModeCallback(getActivity(), new GenericAdapter<WhatsappImageAdapter>(waImageAdapter), arrayList, new InstanceHandler<SavedImageFragment>(mInstance)));
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
                        File[] listFiles = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsappStatus").toString()).listFiles();
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
                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".png")) {
                    ImageModel model=new ImageModel(file.getAbsolutePath());
                    arrayList.add(model);
                }
            }
        }
    }
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
//        waImageAdapter.notifyDataSetChanged();
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

    public void showInterstitialAd() {

        if (mInterstitialAd != null) {
            mInterstitialAd.show(getActivity());
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }


    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int WRITE_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED) || WRITE_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }



    @TargetApi(23)
    public boolean requestPermission() {
        String[] permissions = {

                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        requestPermissions(permissions, STORAGE_PERMISSION_CODE);
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //do somethings
            getStatus();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

}