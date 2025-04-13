package com.example.projectfour;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfour.R;

public class HoleAdapter extends RecyclerView.Adapter<HoleAdapter.HoleViewHolder> {

    private final int holeCount;

    public HoleAdapter(int holeCount) {
        this.holeCount = holeCount;
    }

    @NonNull
    @Override
    public HoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hole_item, parent, false);
        return new HoleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoleViewHolder holder, int position) {
        holder.holeNumberTextView.setText("Hole " + (position + 1));
    }

    @Override
    public int getItemCount() {
        return holeCount;
    }

    static class HoleViewHolder extends RecyclerView.ViewHolder {
        TextView holeNumberTextView;

        public HoleViewHolder(@NonNull View itemView) {
            super(itemView);
            holeNumberTextView = itemView.findViewById(R.id.holeNumberTextView);
        }
    }
}
