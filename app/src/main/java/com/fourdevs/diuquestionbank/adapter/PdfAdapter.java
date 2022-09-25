package com.fourdevs.diuquestionbank.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fourdevs.diuquestionbank.databinding.ItmeContainerPdfBinding;

import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder>{

    private final List<Bitmap> pdfFile;

    public PdfAdapter(List<Bitmap> pdfFile) {
        this.pdfFile = pdfFile;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItmeContainerPdfBinding itmeContainerPdfBinding = ItmeContainerPdfBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(itmeContainerPdfBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setPdfImage(pdfFile.get(position));

    }

    @Override
    public int getItemCount() {
        return pdfFile.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItmeContainerPdfBinding binding;


        public ViewHolder(ItmeContainerPdfBinding itmeContainerPdfBinding) {
            super(itmeContainerPdfBinding.getRoot());
            binding = itmeContainerPdfBinding;
        }

        public void setPdfImage(Bitmap bitmap) {
            binding.pdfview.setImageBitmap(bitmap);

        }

    }
}
