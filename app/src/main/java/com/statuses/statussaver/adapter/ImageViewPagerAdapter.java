package com.statuses.statussaver.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.statuses.statussaver.R;
import com.statuses.statussaver.model.ImageModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ImageViewPagerAdapter extends PagerAdapter {

    // Context object
    Context context;

    // Array of images
    ArrayList<ImageModel> images;

    File f;

    // Layout Inflater
    LayoutInflater mLayoutInflater;


    // Viewpager Constructor
    public ImageViewPagerAdapter(Context context, ArrayList<ImageModel> images) {
        this.context = context;
        this.images = images;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        // return the number of images
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
    {
        return view == ((RelativeLayout) object);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        // inflating the item.xml
        View itemView = mLayoutInflater.inflate(R.layout.image_view, container, false);


       /* PhotoView photoView = (PhotoView) findViewById(R.id.photo);
        Glide.with(this).load(this.f).into(photoView);
        */
        String image = images.get(position).getPath();
        f = new File(image);

        // referencing the image view from the item.xml file
        //ImageView imageView = (ImageView) itemView.findViewById(R.id.image_view);
        PhotoView photoView = (PhotoView) itemView.findViewById(R.id.photo);
        Glide.with(this.context).load(this.f).into(photoView);



        // setting the image in the imageView
       // imageView.setImageURI(Uri.parse(images.get(position).getImage_path()));

        // Adding the View
        Objects.requireNonNull(container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((RelativeLayout) object);
    }
}
