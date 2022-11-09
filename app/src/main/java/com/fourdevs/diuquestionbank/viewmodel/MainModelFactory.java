package com.fourdevs.diuquestionbank.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.fourdevs.diuquestionbank.repository.MainRepository;

public class MainModelFactory implements ViewModelProvider.Factory {
    private final MainRepository repository;
    public MainModelFactory(MainRepository mainRepository) {
        this.repository = mainRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MainViewModel(repository);
    }
}
