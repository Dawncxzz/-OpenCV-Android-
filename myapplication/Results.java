package com.example.myapplication;

import android.graphics.Bitmap;

public class Results {
    private double result;
    Bitmap rotateBitmap;
    public Results(double result, Bitmap rotateBitmap){
        this.result = result;
        this.rotateBitmap = rotateBitmap;
    }

    public double getResult() {
        return result;
    }

    public Bitmap getRotateBitmap() {
        return rotateBitmap;
    }
}
