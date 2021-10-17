package com.bustracking.myschool.bustracker.Models;

public class Upload
{
    public String imageName;

    public Upload(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Upload(){}
}
