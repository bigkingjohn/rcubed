package com.example.rcubed;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * DAO (data access object) providing the interface for all the MongoDb operations that can be performed on a user.
 */
public class MongoDbDAOUser implements UserDAO
{
  /**
   * The collection in our database containing all Users.
   */
  private static final String USERS_COLLECTION = "Users";

  /**
   * Mongo Db Keys for the various User fields.
   */
  public static final String KEY_ID = "_id";
  public static final String KEY_USERNAME = "Username";
  public static final String KEY_FRIENDS_LIST = "Friends";

  /**
   * Helper function to convert an instance of User to a representation suitable for storing in a Mongo Db Collection.
   * 
   * @param photo  The instance of User to convert.
   * 
   * @return An instance of DBObject representing the input User.
   */
  public static Document convertToDoc(User user)
  {
    Document doc = new Document();
    doc.append(KEY_USERNAME, user.getUsername());

    // Convert the tags into a mongo db list/array.
    BasicBSONList bsonFriends = new BasicBSONList();
    bsonFriends.addAll(user.getFriends());
    doc.append(KEY_FRIENDS_LIST, bsonFriends);

    // Only add the id field if we've got it. If it's not present, assume this is a new photo and mongodb will add it
    // for us when the document is inserted.
    if (user.getId() != null)
    {
      doc.append(KEY_ID, new ObjectId(user.getId()));
    }

    return doc;
  }

  public static User createUser(PhotoDAO photoDAO, Document doc)
  {
    // Create an empty instance of User.
    User user = new User(photoDAO);

    // Populate the relevant fields.
    user.setUsername((String) doc.get(KEY_USERNAME));

    // user.setFriends(alFriends);
    ArrayList<String> friends = (ArrayList<String>) doc.get(KEY_FRIENDS_LIST);
    user.setFriends(friends);

    // Object Id must be converted to a string.
    ObjectId id = (ObjectId) doc.get(KEY_ID);
    user.setId(id.toString());

    return user;
  }

  /**
   * The collection in our database which contains all the photos.
   */
  private MongoCollection<Document> collection;

  public MongoDbDAOUser(MongoDatabase db)
  {
    collection = db.getCollection(USERS_COLLECTION);
  }

  /**
   * @see com.example.rcubed.UserDAO#insertUser(com.example.rcubed.User)
   */
  @Override
  public boolean insertUser(User user)
  {
    // Default return value to false.
    boolean success = false;

    Document doc = convertToDoc(user);
    // @@@ JAS what, no exceptions to handle??? or error cases to deal with?
    collection.insertOne(doc);
    success = true;

    // Update the photo object with the ID thats been assigned by Mongo Db.
    ObjectId id = (ObjectId) doc.get(KEY_ID);
    user.setId(id.toString());

    return success;
  }

  /**
   * @see com.example.rcubed.UserDAO#getUser(com.example.rcubed.PhotoDAO, java.lang.String)
   */
  @Override
  public User getUser(PhotoDAO photoDAO, String username)
  {
    Document result = collection.find(eq(KEY_USERNAME, username)).first();
    User user = null;

    if (result != null)
    {
      user = createUser(photoDAO, result);
    }

    return user;
  }

  /**
   * @see com.example.rcubed.UserDAO#deleteUser(com.example.rcubed.User)
   */
  @Override
  public boolean deleteUser(User user)
  {
    // Default return value to false.
    boolean success = false;

    DeleteResult result = collection.deleteOne(eq(KEY_ID, new ObjectId(user.getId())));

    if (result.getDeletedCount() == 1)
    {
      // Update was successful.
      success = true;
    }

    return success;
  }

  /**
   * @see com.example.rcubed.UserDAO#addFriend(com.example.rcubed.User, java.lang.String)
   */
  @Override
  public boolean addFriend(User user, String newFriend)
  {
    // Default return value to false.
    boolean success = false;

    if (!user.getFriends().contains(newFriend))
    {
      Document match = new Document();
      match.append(KEY_USERNAME, user.getUsername());

      Document newItem = new Document();
      newItem.append(KEY_FRIENDS_LIST, newFriend);

      Document pushQuery = new Document();
      pushQuery.append("$push", newItem);

      UpdateResult result = collection.updateOne(match, pushQuery);

      if (result.getMatchedCount() == 1 && result.getModifiedCount() == 1)
      {
        // Update was successful.
        success = true;

        // Update the user object to reflect the change pushed to the db.
        user.addFriend(newFriend);
      }
    }

    return success;
  }

  /**
   * @see com.example.rcubed.UserDAO#removeFriend(com.example.rcubed.User, java.lang.String)
   */
  @Override
  public boolean removeFriend(User user, String exFriend)
  {
    // Default return value to false.
    boolean success = false;

    if (user.getFriends().contains(exFriend))
    {
      Document match = new Document();
      match.append(KEY_USERNAME, user.getUsername());

      Document removals = new Document();
      removals.append(KEY_FRIENDS_LIST, exFriend);

      Document pullQuery = new Document();
      pullQuery.append("$pull", removals);

      UpdateResult result = collection.updateOne(match, pullQuery);

      if (result.getMatchedCount() == 1 && result.getModifiedCount() == 1)
      {
        // Update was successful.
        success = true;

        // Update the user object to reflect the change pushed to the db.
        user.removeFriend(exFriend);
      }

    }

    return success;
  }
}
