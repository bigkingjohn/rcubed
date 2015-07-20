package com.example.rcubed;

import java.util.ArrayList;

/**
 * DAO Interface for manipulating/querying photos in the database.
 */
public interface PhotoDAO
{

  /**
   * Add the input photo to the database.
   * 
   * @param photo  The photo to add.
   * 
   * @return True if the database operation was successful, otherwise false.
   */
  public abstract boolean insertPhoto(Photo photo);

  /**
   * Remove this photo from the database.
   * 
   * @param user   The user performing the operation (only the owner can delete)
   * @param photo  The photo to remove.
   * 
   * @return True if the database operation was successful, otherwise false.
   */
  public abstract boolean deletePhoto(User user, Photo photo);

  /**
   * Add a tag to the photo.
   * 
   * @param user    The user performing the operation (only the owner of a photo can modify it's properties)
   * @param photo   The photo to add a tag to.
   * @param newTag  The tag to attach to the photo.
   * 
   * @return True if the database operation was successful, otherwise false.
   */
  public abstract boolean addTag(User user, Photo photo, String newTag);

  /**
   * Remove a tag from the photo.
   * 
   * @param user    The user performing the operation (only the owner of a photo can modify it's properties)
   * @param photo   The photo to remove the tag from.
   * @param exTag   The tag to remove from the photo.
   * 
   * @return True if the database operation was successful, otherwise false.
   */
  public abstract boolean removeTag(User user, Photo photo, String exTag);

  /**
   * Add a comment to the photo.
   * 
   * @param user        The user performing the operation (only the owner of a photo can modify it's properties)
   * @param photo       The photo to add a the comment to.
   * @param newComment  The comment to attach to the photo.
   * 
   * 
   * @return True if the database operation was successful, otherwise false.
   */
  public abstract boolean addComment(User user, Photo photo, String newComment);

  /**
   * Change the visibility of the photograph.
   * 
   * @param user          The user performing the operation (only the owner of a photo can modify it's properties)
   * @param photo         The photo to add a the comment to.
   * @param newVisibility The new visibility value.
   * 
   * @return True if the database operation was successful, otherwise false.
   */
  public abstract boolean changeVisibility(User user, Photo photo, Photo.Visibility newVisibility);

  /**
   * Retrieve a set of photos from the databse.
   * 
   * @param owner              The owner of the photos to get.
   * @param tag                The tag to filter the results by.
   * @param allowedVisibility  The Visibility of photos the requesting party is allowed to see.
   *  
   * @return A list of photos owned by "owner" tagged with "tag" of the appropriate Visibility level or higher.
   */
  public abstract ArrayList<Photo> getPhotos(String owner, String tag, Photo.Visibility allowedVisibility);
}