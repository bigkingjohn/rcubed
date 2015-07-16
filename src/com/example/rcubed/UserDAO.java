package com.example.rcubed;

public interface UserDAO
{

  public abstract boolean insertUser(User user);

  /**
   * Create an instance of User for the db entry matching the input username.
   * 
   * @param username  The Id of the User to create.
   * @return An instance of User.
   *         Will be null if the username is not found.
   */
  public abstract User getUser(PhotoDAO photoDAO, String username);

  public abstract boolean deleteUser(User user);

  public abstract boolean addFriend(User user, String newFriend);

  public abstract boolean removeFriend(User user, String exFriend);

}