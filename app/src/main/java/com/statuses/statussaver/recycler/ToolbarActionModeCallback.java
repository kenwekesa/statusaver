package com.statuses.statussaver.recycler;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.view.ActionMode;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.statuses.statussaver.GenericAdapter;
import com.statuses.statussaver.HelperMethods;
import com.statuses.statussaver.InstanceHandler;
import com.statuses.statussaver.R;
import com.statuses.statussaver.adapter.WhatsappImageAdapter;
import com.statuses.statussaver.adapter.WhatsappVideoAdapter;
import com.statuses.statussaver.fragments.whatsapp.WhatsappImageFragment;
import com.statuses.statussaver.fragments.whatsapp.WhatsappVideosFragment;
import com.statuses.statussaver.model.ImageModel;

import java.io.File;
import java.util.ArrayList;

public class ToolbarActionModeCallback implements androidx.appcompat.view.ActionMode.Callback {

    private Context context;
   private WhatsappVideoAdapter whatsappVideoAdapter;
    private WhatsappImageAdapter whatsappImageAdapter;
    private ArrayList<ImageModel> message_models;
    private InterstitialAd mInterstitialAd;
    WhatsappImageFragment waImageFragment;
    WhatsappVideosFragment waVideoFragment;
    //BWAImageFragment bwaImageFragment;
    //BWAVideoFragment bwaVideoFragment;
    String s = "";


    public ToolbarActionModeCallback(Context context, GenericAdapter<?> adapter, ArrayList<ImageModel> message_models, InstanceHandler<?> instance) {
        this.context = context;
        //this.waVideoAdapter = waVideoAdapter;
        this.message_models = message_models;
        s = instance.getValue().getClass().getSimpleName();
        switch (s) {
            case "WAVideoFragment":
                waVideoFragment = (WhatsappVideosFragment) instance.getValue();
                //waVideoAdapter = (WAVideoAdapter) adapter.getValue();
              //  mInterstitialAd= waVideoFragment.getmInterstitialAd();
                break;
            case "BWAImageFragment":
                /*bwaImageFragment = (BWAImageFragment) instance.getValue();
                waImageAdapter = (WAImageAdapter) adapter.getValue();
                mInterstitialAd= bwaImageFragment.getmInterstitialAd();*/
                break;
            case "WAImageFragment":
                waImageFragment = (WhatsappImageFragment) instance.getValue();
                mInterstitialAd= waImageFragment.getmInterstitialAd();
                whatsappImageAdapter = (WhatsappImageAdapter) adapter.getValue();
                break;
            case "BWAVideoFragment":
               /* bwaVideoFragment = (BWAVideoFragment) instance.getValue();
                waVideoAdapter = (WAVideoAdapter) adapter.getValue();
                mInterstitialAd= bwaVideoFragment.getmInterstitialAd();*/
                break;
        }

    }

    @Override
    public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.selection_menu, menu);//Inflate the menu_main over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {

        //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
        //So here show action menu_main according to SDK Levels
        if (Build.VERSION.SDK_INT < 11) {
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_delete), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_save), MenuItemCompat.SHOW_AS_ACTION_NEVER);
        } else {
            menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return true;
    }

    @Override
    public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
        SparseBooleanArray selectedIds;
        int size;
        switch (item.getItemId()) {
            case R.id.action_delete:
                switch (s) {
                    case "WAVideoFragment":
                        selectedIds = whatsappVideoAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                String str = (String) whatsappVideoAdapter.getItem(selectedIds.keyAt(size)).getPath();
                                File file = new File(str);
                                try {
                                    if (file.exists() && file.isFile()) {
                                        file.delete();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        waVideoFragment.deleteRows();
                        waVideoFragment.refresh();
                        mode.finish();
                        return true;
                    case "BWAVideoFragment":
                        /*selectedIds = waVideoAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                String str = (String) waVideoAdapter.getItem(selectedIds.keyAt(size)).getPath();
                                File file = new File(str);
                                try {
                                    if (file.exists() && file.isFile()) {
                                        file.delete();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        bwaVideoFragment.deleteRows();
                        bwaVideoFragment.refresh();
                        mode.finish();*/
                        return true;
                    case "WAImageFragment":
                        selectedIds = whatsappImageAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                String str = (String) whatsappImageAdapter.getItem(selectedIds.keyAt(size)).getPath();
                                File file = new File(str);
                                try {
                                    if (file.exists() && file.isFile()) {
                                        file.delete();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        waImageFragment.deleteRows();
                        waImageFragment.refresh();
                        mode.finish();
                        return true;
                    case "BWAImageFragment":
                        selectedIds = whatsappImageAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                String str = (String) whatsappImageAdapter.getItem(selectedIds.keyAt(size)).getPath();
                                File file = new File(str);
                                try {
                                    if (file.exists() && file.isFile()) {
                                        file.delete();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        /*bwaImageFragment.deleteRows();
                        bwaImageFragment.refresh();*/
                        mode.finish();
                        return true;
                }
            case R.id.action_save:
                switch (s) {
                    case "WhatsappVideosFragment":
                      selectedIds = whatsappVideoAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                HelperMethods.transfer(new File((String) whatsappVideoAdapter.getItem(selectedIds.keyAt(size)).getPath()));
                            }
                        }
                        Toast.makeText(context, "Done! :)", Toast.LENGTH_SHORT).show();
                        mode.finish();//Finish action mode
                       /* if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
                        }*/
                        return true;
                    case "BWAVideoFragment":
                        /*selectedIds = waVideoAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                HelperMethods.transfer(new File((String) waVideoAdapter.getItem(selectedIds.keyAt(size)).getPath()));
                            }
                        }
                        Toast.makeText(context, "Done! :)", Toast.LENGTH_SHORT).show();
                        mode.finish();//Finish action mode
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
                        }
                        return true;*/
                    case "WhatsappImageFragment":
                        selectedIds = whatsappImageAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                HelperMethods.transfer(new File((String) whatsappImageAdapter.getItem(selectedIds.keyAt(size)).getPath()));
                            }
                        }
                        Toast.makeText(context, "Done! :)", Toast.LENGTH_SHORT).show();
                        mode.finish();//Finish action mode
                       /* if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
                        }*/
                        return true;
                    case "BWAImageFragment":
                        selectedIds = whatsappImageAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                HelperMethods.transfer(new File((String) whatsappImageAdapter.getItem(selectedIds.keyAt(size)).getPath()));
                            }
                        }
                        Toast.makeText(context, "Done! :)", Toast.LENGTH_SHORT).show();
                        mode.finish();//Finish action mode
                        /*if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
                        }*/
                        return true;

                }
                return false;
        }
        return false;
    }


    @Override
    public void onDestroyActionMode (ActionMode mode){

        //When action mode destroyed remove selected selections and set action mode to null
        //First check current fragment action mode
        Fragment recyclerFragment;
        switch (s) {
            case "WhatsappVideosFragment":
                whatsappVideoAdapter.removeSelection();  // remove selection
                recyclerFragment = ((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_wa_video);//Get recycler fragment
                if (recyclerFragment != null)
                    ((WhatsappVideosFragment) recyclerFragment).setNullToActionMode();//Set action mode null
                break;
            case "BWAVideoFragment":
                /*waVideoAdapter.removeSelection();  // remove selection
                recyclerFragment = ((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_wa_video);//Get recycler fragment
                if (recyclerFragment != null)
                    ((BWAVideoFragment) recyclerFragment).setNullToActionMode();//Set action mode null*/
                break;
            case "WhatsappImageFragment":
                whatsappImageAdapter.removeSelection();  // remove selection
                recyclerFragment = ((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_wa_image);//Get recycler fragment
                if (recyclerFragment != null)
                    ((WhatsappImageFragment) recyclerFragment).setNullToActionMode();//Set action mode null
                break;
            case "BWAImageFragment":
               /* waImageAdapter.removeSelection();  // remove selection
                recyclerFragment = ((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_wa_image);//Get recycler fragment
                if (recyclerFragment != null)
                    ((BWAImageFragment) recyclerFragment).setNullToActionMode();//Set action mode null*/
                break;

        }

    }


}