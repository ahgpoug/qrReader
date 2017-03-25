package ahgpoug.qrreader.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ahgpoug.qrreader.R;
import ahgpoug.qrreader.objects.Photo;
import ahgpoug.qrreader.util.Util;

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<PhotoRecyclerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Photo> values;

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView name;
        private TextView modDate;

        private ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            name = (TextView) view.findViewById(R.id.name);
            modDate = (TextView) view.findViewById(R.id.modDate);
        }
    }

    public PhotoRecyclerAdapter(Context context, ArrayList<Photo> values) {
        this.context = context;
        this.values = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try {
            holder.image.setImageBitmap(Util.cropBitmapCenter(values.get(position).getBitmap()));
            holder.name.setText(values.get(position).getName());
            holder.modDate.setText(new SimpleDateFormat("MMM dd, HH:mm:ss").format(values.get(position).getModDate()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
}