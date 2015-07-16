package com.example.rcubed;

import java.util.ArrayList;

public interface PhotoDAO
{

  public abstract boolean insertPhoto(Photo photo);

  public abstract boolean deletePhoto(User user, Photo photo);

  public abstract boolean addTag(User user, Photo photo, String newTag);

  public abstract boolean removeTag(User user, Photo photo, String exTag);

  public abstract boolean changeVisibility(User user, Photo photo, Photo.Visibility newVisibility);

  public abstract ArrayList<Photo> getPhotos(String owner, String tag, Photo.Visibility allowedVisibility);

}