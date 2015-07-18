package com.example.rcubed;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import com.vaadin.server.StreamResource.StreamSource;

/**
 * Object representation of a photo/image which has been uploaded to the database.
 */
public class Photo implements StreamSource
{
  /**
   * The unique id that identifies this photograph in the database.
   * 
   * We could create this ourselves, but it's easier to let Mongo DB generate it when the data is added to
   * the Database.  Note that this does make it more difficult to swap MongoDb for another database.
   */
  private String id;

  /**
   * The id used to store and retrieve the image data.
   * For performance reasons, the raw image data is stored in the database separately from the other meta data.
   * 
   * We could create this ourselves, but it's easier to let GridFS/Mongo DB generate it when the photo is added to
   * the Database.  Note that this does make it more difficult to swap MongoDb for another database.
   */
  private Object gfsid;

  /**
   * The username of the owner of this photograph.
   */
  private String owner;

  /**
   * A title the user has attached to the photo.
   */
  private String title;

  /**
   * A list of tags that the owner has attached to this photograph.
   */
  private ArrayList<String> tags;

  /**
   * Any comments that have been left by users about the photo.
   */
  private ArrayList<String> comments;

  /**
   * The time the photo was uploaded.
   */
  private Date timestamp;

  /**
   * A buffer containing the image data.
   */
  private byte[] image;

  /**
   * The visibility of this Photo - determines who is able to view it.
   * One of:
   *   PRIVATE - Only the owner can view this photo.
   *   FRIENDS - The owner and anyone on his friends list can view this photo.
   *   PUBLIC  - Anyone with access to the photo app can view this photo.
   */
  private Visibility visibility;

  /**
   * Valid Visibility states.
   */
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
   * Empty Constructor.
   */
  public Photo()
  {
    this.id = null;
    this.gfsid = null;
    this.owner = null;
    this.title = null;
    this.tags = null;
    this.comments = null;
    this.timestamp = null;
    this.image = null;
    this.visibility = null;
  }

  /**
   * Partial constructor
   * 
   * @param owner       The name of the user who owns this photo.
   * @param title       The title the user has associate with this photo. 
   * @param tags        A list of user defined tags the user has associated with the photo.
   * @param visibility  The desired visibility of the photo.
   * @param imageData   byte array containing the image data.
   */
  public Photo(String owner, String title, ArrayList<String> tags, Visibility visibility, byte[] imageData)
  {
    // id and gfsid are generated once the image has been saved to the database.
    this.id = null;
    this.gfsid = null;

    this.owner = owner;
    this.title = title;
    this.tags = tags;

    // User can't set comments when uploading the photo.
    this.comments = new ArrayList<String>();

    // Set the timestamp to the current time.
    this.timestamp = new Date();
    this.image = imageData;
    this.visibility = visibility;
  }

  /**
   * Add a tag to this photo.
   * 
   * @param newTag
   */
  public void addTag(String newTag)
  {
    tags.add(newTag);
  }

  /**
   * Remove a tag from this photo.
   * 
   * @param unwantedTag
   */
  public void removeTag(String unwantedTag)
  {
    tags.remove(unwantedTag);
  }

  /**
   * Add a comment to this photo.
   * 
   * @param newTag
   */
  public void addComment(String newComment)
  {
    comments.add(newComment);
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
   * Set the GridFS ID for this photo.
   * 
   * @param gfsid
   */
  public void setGFSPhotoId(Object gfsid)
  {
    this.gfsid = gfsid;
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
   * Set the title for this Photo
   * 
   * @param title
   */
  public void setTitle(String title)
  {
    this.title = title;
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
   * Set the comments for this Photo.
   * 
   * @param tags
   */
  public void setComments(ArrayList<String> comments)
  {
    this.comments = comments;
  }

  /**
   * Set the timestamp attached to this photo.
   * 
   * @param timestamp
   */
  public void setTimeStamp(Date timestamp)
  {
    this.timestamp = timestamp;
  }

  /**
   * Set the visibility of this photo.
   * 
   * @param newVisibility
   */
  public void setVisibility(Visibility newVisibility)
  {
    visibility = newVisibility;
  }

  /**
   * Set the binary data for the photo.
   * 
   * @param imageData
   */
  public void setImage(byte[] imageData)
  {
    image = imageData;
  }

  /**
   * Get the database object id of this photo.
   */
  public String getId()
  {
    return id;
  }

  /**
   * Get the GridFS Id for this photo.
   */
  public Object getGFSPhotoId()
  {
    return gfsid;
  }

  /**
   * Get the owner of this photo.
   */
  public String getOwner()
  {
    return owner;
  }

  /**
   * Get the title of this photo.
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * Get the tags for this photo.
   */
  public ArrayList<String> getTags()
  {
    return tags;
  }

  /**
   * Get the comments for this photo.
   */
  public ArrayList<String> getComments()
  {
    return comments;
  }

  /**
   * Get the timestamp for this photo.
   */
  public Date getTimeStamp()
  {
    return timestamp;
  }

  /**
   * Get the visibility of this photo.
   */
  public Visibility getVisibility()
  {
    return visibility;
  }

  /**
   * Get the image data for this photo.
   */
  public byte[] getImage()
  {
    return image;
  }

  /**
   * See com.vaadin.server.StreamResource.StreamSource.getStream()
   */
  @Override
  public InputStream getStream()
  {
    return new ByteArrayInputStream(image);
  }
}
