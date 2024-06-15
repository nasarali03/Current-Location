package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoriteLocationAdapter extends RecyclerView.Adapter<FavoriteLocationAdapter.ViewHolder> {

    private List<FavoriteLocation> favoriteLocations;
    private OnFavoriteLocationClickListener listener;
    private OnFavoriteLocationEditListener editListener;
    private OnFavoriteLocationDeleteListener deleteListener;

    public interface OnFavoriteLocationClickListener {
        void onFavoriteLocationClick(FavoriteLocation favoriteLocation);
    }

    public interface OnFavoriteLocationEditListener {
        void onFavoriteLocationEdit(FavoriteLocation favoriteLocation);
    }

    public interface OnFavoriteLocationDeleteListener {
        void onFavoriteLocationDelete(FavoriteLocation favoriteLocation);


    }

    public FavoriteLocationAdapter(List<FavoriteLocation> favoriteLocations, OnFavoriteLocationClickListener listener, OnFavoriteLocationEditListener editListener, OnFavoriteLocationDeleteListener deleteListener) {
        this.favoriteLocations = favoriteLocations;
        this.listener = listener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_location_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteLocation favoriteLocation = favoriteLocations.get(position);
        holder.nameTextView.setText(favoriteLocation.name);
        holder.itemView.setOnClickListener(v -> listener.onFavoriteLocationClick(favoriteLocation));
        holder.editButton.setOnClickListener(v -> editListener.onFavoriteLocationEdit(favoriteLocation));
        holder.deleteButton.setOnClickListener(v -> deleteListener.onFavoriteLocationDelete(favoriteLocation));
    }

    @Override
    public int getItemCount() {
        return favoriteLocations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageButton editButton;
        public ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}