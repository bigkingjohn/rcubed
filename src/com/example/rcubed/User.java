package com.example.rcubed;

import java.util.ArrayList;

import com.example.rcubed.Photo.Visibility;

/**
 * Object representation of a user of the photo app.
 */
public class User
{
  /**
   * The unique id that identifies this photograph in the database.
   * 
   * This is created by mongodb when we insert this Photo (as a document) into
   * the database.
   */
  String id;

  /**
   * This user's unique identifier.
   */
  String username;

  /**
   * A list of other user's this user has identified as known people.
   */
  ArrayList<String> friendsList;

  /**
   * Access point for retrieving/adding/modifying this user's photos. 
   */
  PhotoDAO photoDAO;

  /**
   * Empty constructor - used for building a User from an entry in the db.
   * @param photoDAO
   */
  public User(PhotoDAO photoDAO)
  {
    this.id = null;
    this.username = null;
    this.friendsList = null;
    this.photoDAO = photoDAO;
  }

  /**
   * New User constructor.
   * 
   * @param username
   * @param photoDAO
   */
  public User(String username, PhotoDAO photoDAO)
  {
    this.id = null;
    this.username = username;
    this.friendsList = new ArrayList<String>();
    this.photoDAO = photoDAO;
  }

  /**
   * Set the database object id for this User. 
   * 
   * @param id
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * Set the username for this User.
   * 
   * @param username
   */
  public void setUsername(String username)
  {
    this.username = username;
  }

  /**
   * Set the friends list for this User.
   * 
   * @param friendsList
   */
  public void setFriends(ArrayList<String> friendsList)
  {
    this.friendsList = friendsList;
  }

  /**
   * Get the database object id of this user.
   */
  public String getId()
  {
    return id;
  }

  /**
   * Get the username of this user.
   */
  public String getUsername()
  {
    return username;
  }

  public ArrayList<String> getFriends()
  {
    return friendsList;
  }

  public ArrayList<Photo> getPhotos(User photoOwner, String tag)
  {
    // Default the visibility setting to the least secure.
    Photo.Visibility allowedVisibility = Visibility.PUBLIC;

    String ownersName = photoOwner.getUsername();

    if (username.equals(ownersName))
    {
      // This user is the owner so is allowed to see the most secure photos.
      allowedVisibility = Visibility.PRIVATE;
    }
    else if (photoOwner.getFriends().contains(username))
    {
      // This user is on the photograph owner's friends list.
      allowedVisibility = Visibility.FRIENDS;

    }

    ArrayList<Photo> photos = photoDAO.getPhotos(ownersName, tag, allowedVisibility);
    return photos;
  }
}
