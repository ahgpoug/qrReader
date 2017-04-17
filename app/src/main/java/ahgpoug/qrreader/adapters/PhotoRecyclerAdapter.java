package ahgpoug.qrreader.adapters;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import ahgpoug.qrreader.R;
import ahgpoug.qrreader.interfaces.ItemTouchHelperAdapter;
import ahgpoug.qrreader.interfaces.ItemTouchHelperViewHolder;
import ahgpoug.qrreader.interfaces.OnStartDragListener;
import ahgpoug.qrreader.objects.Photo;
import ahgpoug.qrreader.util.Util;

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<PhotoRecyclerAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private Context context;
    private ArrayList<Photo> values;
    private final OnStartDragListener dragStartListener;

    class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        private ImageView image;
        private TextView name;
        private TextView modDate;
        private View bottom;

        private ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            name = (TextView) view.findViewById(R.id.name);
            modDate = (TextView) view.findViewById(R.id.modDate);
            bottom = view.findViewById(R.id.bottom);
        }

        @Override
        public void onItemSelected() {
            //itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            //itemView.setBackgroundColor(0);
        }
    }

    public PhotoRecyclerAdapter(Context context, ArrayList<Photo> values, OnStartDragListener dragStartListener) {
        this.context = context;
        this.values = values;
        this.dragStartListener = dragStartListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            Glide.with(context)
                    .load(values.get(holder.getLayoutPosition()).getUri())
                    .asBitmap()
                    .override(600, 600)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .into(new BitmapImageViewTarget(holder.image) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            super.setResource(resource);
                        }
                    });
            holder.name.setText(values.get(holder.getLayoutPosition()).getName());
            holder.modDate.setText(new SimpleDateFormat("MMM dd, HH:mm:ss").format(values.get(holder.getLayoutPosition()).getModDate()));
            holder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new MaterialDialog.Builder(context)
                            .title("Удаление")
                            .content("Вы действительно хотите удалить данное изображение?")
                            .positiveText(android.R.string.ok)
                            .negativeText(android.R.string.no)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    values.remove(holder.getLayoutPosition());
                                    notifyDataSetChanged();
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    return false;
                }
            });
            holder.bottom.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        dragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    @Override
    public void onItemDismiss(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(values, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(values, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }
}