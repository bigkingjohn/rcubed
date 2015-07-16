package com.example.rcubed;

import java.util.ArrayList;

/**
 * Object representation of a photo/image which has been uploaded to the database.
 */
public class Photo
{
  /**
   * The unique id that identifies this photograph in the database.
   * 
   * This is created by mongodb when we insert this Photo (as a document) into
   * the database.
   */
  private String id;

  /**
   * The username of the owner of this photograph.
   */
  private String owner;

  /**
   * A list of tags that the owner has attached to this photograph.
   */
  private ArrayList<String> tags;

  /**
   * The visibility of this Photo - determines who is able to view it.
   * One of:
   *   PRIVATE - Only the owner can view this photo.
   *   FRIENDS - The owner and anyone on his friends list can view this photo.
   *   PUBLIC  - Anyone with access to the photo app can view this photo.
   */
  private Visibility visibility;

  public enum Visibility
  {
    PRIVATE,
    FRIENDS,
    PUBLIC,
  }

  /**
   * A static final array representing the visibility enum.
   * 
   * Enum values cannot be stored in all database solutions (thinking of MongoDb) so, when reading a Photo from the
   * database we need to reverse lookup the enum value corresponding to whatever format has been chosen for said
   * database.  Using ints as the storage type allows equality comparisons which are useful for determining which
   * photos can be seen by a specific user.  This array then provides a neat and maintainable way for that reverse
   * lookup. 
   */
  public static final Visibility[] VisibilityValues = Visibility.values();

  /**
   * @@@ JAS photo metadata???
   */
  private String timestamp;

  /**
   * Constructor.
   * 
   * @param id
   * @param owner
   * @param tags
   * @param visibility
   */
  public Photo()
  {
    this.id = null;
    this.owner = null;
    this.tags = null;
    this.visibility = null;
  }

  /**
   * Add a tag to this photo.
   * 
   * @param newTag
   */
  public void addTags(String newTag)
  {

  }

  /**
   * Remove a tag from this photo.
   * 
   * @param unwantedTag
   */
  public void removeTag(String unwantedTag)
  {

  }

  /**
   * Set the database object id for this Photo. 
   * 
   * @param id
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * Set the owner of this Photo.
   * 
   * @param owner
   */
  public void setOwner(String owner)
  {
    this.owner = owner;
  }

  /**
   * Set the tags for this Photo.
   * 
   * @param tags
   */
  public void setTags(ArrayList<String> tags)
  {
    this.tags = tags;
  }

  /**
   * Set the visibility of this photo.
   */
  public void setVisibility(Visibility newVisiblity)
  {
    visibility = newVisiblity;
  }

  /**
   * Get the database object id of this photo.
   */
  public String getId()
  {
    return id;
  }

  /**
   * Get the owner of this photo.
   */
  public String getOwner()
  {
    return owner;
  }

  /**
   * Get the tags for this photo.
   */
  public ArrayList<String> getTags()
  {
    return tags;
  }

  /**
   * Get the visibility of this photo.
   */
  public Visibility getVisibility()
  {
    return visibility;
  }
}
