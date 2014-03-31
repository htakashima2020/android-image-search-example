package com.example.imagesearch;

import java.util.ArrayList;

public interface GoogleImageAPIListener {
    public void gotNewURLs(ArrayList<String> urls);
}
