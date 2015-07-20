package com.example.rcubed;

/**
 * DAO Interface for manipulating/querying Users in the database.
 */
public interface UserDAO
{

  /**
   * Insert the user into the database.
   * 
   * @param user  The User to add.
   * 
   * @return True if the database operation was successful, otherwise false.
   */
  public abstract boolean insertUser(User user);

  /**
   * Create an instance of User for the db entry matching the input username.
   * 
   * @param username  The Id of the User to create.
   * 
   * @return An instance of User.
   *         Will be null if the username is not found.
   */
  public abstract User getUser(PhotoDAO photoDAO, String username);

  /**
   * Remove the user from the database.
   * 
   * @param user  The user to remove.
   * 
   * @return True if the database operation was successful, otherwise false.
   */
  public abstract boolean deleteUser(User user);

  /**
   * Add a name to the user's friend list.
   * 
   * @param user       The user to add the friend to.
   * @param newFriend  The name of the friend to add.
   * 
   * @return True if the database operation was successful, otherwise false.
   */
  public abstract boolean addFriend(User user, String newFriend);

  /**
   * Remove a name from the user's friend list.
   * 
   * @param user      The user to remove the friend from.
   * @param exFriend  The name of the friend to remove.
   * 
   * @return True if the database operation was successful, otherwise false.
   */
  public abstract boolean removeFriend(User user, String exFriend);

}