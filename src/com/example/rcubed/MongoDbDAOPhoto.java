package com.example.rcubed;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;

import com.example.rcubed.Photo.Visibility;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * DAO (data access object) providing an interface for all the MongoDb operations that can be performed on a Photo.
 */
public class MongoDbDAOPhoto implements PhotoDAO
{
  /**
   * The collection in our database containing all photos.
   */
  private static final String PHOTOS_COLLECTION = "Photos";

  /**
   * Mongo Db Keys for the various Photo fields.
   */
  public static final String KEY_ID = "_id";
  public static final String KEY_OWNER = "Owner";
  public static final String KEY_TAGS = "Tags";
  public static final String KEY_VISIBILITY = "Visibility";

  /**
   * Helper function to convert an instance of Photo to a representation suitable for storing in a Mongo Db Collection.
   * 
   * @param photo  The instance of Photo to convert.
   * 
   * @return An instance of DBObject representing the input Photo.
   */
  public static Document convertToDoc(Photo photo)
  {
    Document doc = new Document();

    doc.append(KEY_OWNER, photo.getOwner());

    // Convert the tags into a list/array structure suitable for Mongo Db.
    BasicBSONList bsonTags = new BasicBSONList();
    bsonTags.addAll(photo.getTags());
    doc.append(KEY_TAGS, bsonTags);

    doc.append(KEY_VISIBILITY, photo.getVisibility().ordinal());

    // Only add the id field if we've got it. If it's not present, assume this is a new photo and mongodb will add it
    // for us when the document is inserted.
    if (photo.getId() != null)
    {
      doc.append(KEY_ID, new ObjectId(photo.getId()));
    }

    return doc;
  }

  /**
   * Helper function to convert a document retrieved from the Photos collection into an instance of Photo.
   * 
   * @param doc  The document to be converted.
   *  
   * @return An instance of Photo built from the input document. 
   */
  public static Photo createPhoto(Document doc)
  {
    // Create an empty instance of User.
    Photo photo = new Photo();

    // Populate the relevant fields.
    photo.setOwner((String) doc.get(KEY_OWNER));

    // Convert the db list representation to an array list.
    ArrayList<String> tags = (ArrayList<String>) doc.get(KEY_TAGS);
    photo.setTags(tags);

    // Visibility must be converted to the relevant enum value.
    photo.setVisibility(Photo.VisibilityValues[(int) doc.get(KEY_VISIBILITY)]);

    // Object Id must be converted to a string.
    ObjectId id = (ObjectId) doc.get(KEY_ID);
    photo.setId(id.toString());

    return photo;
  }

  /**
   * The collection in our database which contains all the photos.
   */
  private MongoCollection<Document> collection;

  public MongoDbDAOPhoto(MongoDatabase db)
  {
    collection = db.getCollection(PHOTOS_COLLECTION);
  }

  @Override
  public boolean insertPhoto(Photo photo)
  {
    // Default return value to false.
    boolean success = false;

    Document doc = convertToDoc(photo);
    // @@@ JAS what, no exceptions to handle??? or error cases to deal with?
    collection.insertOne(doc);
    success = true;

    // Update the photo object with the ID thats been assigned by Mongo Db.
    ObjectId id = (ObjectId) doc.get(KEY_ID);
    photo.setId(id.toString());

    return success;
  }

  @Override
  public boolean deletePhoto(User user, Photo photo)
  {
    // Default return value to false.
    boolean success = false;

    // Only allowed to delet photo if the user doing the changing is the photo owner.
    // @@@ JAS should be making log that someone is doing something we dont want.
    if (user.getUsername().equals(photo.getOwner()))
    {
      DeleteResult result = collection.deleteOne(eq(KEY_ID, new ObjectId(photo.getId())));

      if (result.getDeletedCount() == 1)
      {
        // Update was successful.
        success = true;
      }
    }

    return success;
  }

  @Override
  public boolean addTag(User user, Photo photo, String newTag)
  {
    // Default return value to false.
    boolean success = false;

    // Only allowed to change photo properties if the user doing the changing is the photo owner.
    // @@@ JAS should be making log that someone is doing something we dont want.
    if (user.getUsername().equals(photo.getOwner()))
    {
      // There is no point in troubling the database if the tag in question is already present.
      if (!photo.getTags().contains(newTag))
      {
        Document match = new Document();
        match.append(KEY_ID, new ObjectId(photo.getId()));

        Document newItem = new Document();
        newItem.append(KEY_TAGS, newTag);

        Document pushQuery = new Document();
        pushQuery.append("$push", newItem);

        UpdateResult result = collection.updateOne(match, pushQuery);

        if (result.getMatchedCount() == 1 && result.getModifiedCount() == 1)
        {
          // Update was successful.
          success = true;
        }
      }
    }

    return success;
  }

  @Override
  public boolean removeTag(User user, Photo photo, String exTag)
  {
    // Default return value to false.
    boolean success = false;

    // Only allowed to change photo properties if the user doing the changing is the photo owner.
    // @@@ JAS should be making log that someone is doing something we dont want.
    if (user.getUsername().equals(photo.getOwner()))
    {
      // There is no point in troubling the database if the tag in question isn't present.
      if (photo.getTags().contains(exTag))
      {
        Document match = new Document();
        match.append(KEY_ID, new ObjectId(photo.getId()));

        Document removals = new Document();
        removals.append(KEY_TAGS, exTag);

        Document pullQuery = new Document();
        pullQuery.append("$pull", removals);

        UpdateResult result = collection.updateOne(match, pullQuery);

        if (result.getMatchedCount() == 1 && result.getModifiedCount() == 1)
        {
          // Update was successful.
          success = true;
        }
      }
    }

    return success;
  }

  @Override
  public ArrayList<Photo> getPhotos(String owner, String tag, Photo.Visibility allowedVisibility)
  {
    ArrayList<Photo> photos = new ArrayList<Photo>();

    Document query = new Document();
    query.append(KEY_OWNER, owner);

    if (tag != null)
    {
      query.append(KEY_TAGS, tag);
    }

    Document visibilityCheck = new Document();
    visibilityCheck.append("$gte", allowedVisibility.ordinal());
    query.append(KEY_VISIBILITY, visibilityCheck);

    MongoCursor<Document> cursor = collection.find(query).iterator();

    try
    {
      while (cursor.hasNext())
      {
        Photo photo = createPhoto(cursor.next());
        photos.add(photo);
      }
    }
    finally
    {
      cursor.close();
    }

    return photos;
  }

  @Override
  public boolean changeVisibility(User user, Photo photo, Visibility newVisibility)
  {
    // Default return value to false.
    boolean success = false;

    // Only allowed to change photo properties if the user doing the changing is the photo owner.
    // @@@ JAS should be making log that someone is doing something we dont want.
    if (user.getUsername().equals(photo.getOwner()))
    {
      Document match = new Document();
      match.append(KEY_ID, new ObjectId(photo.getId()));

      Document newValue = new Document();
      newValue.append(KEY_VISIBILITY, newVisibility.ordinal());

      Document update = new Document();
      update.append("$set", newValue);

      UpdateResult result = collection.updateOne(match, update);

      if (result.getMatchedCount() == 1 && result.getModifiedCount() == 1)
      {
        // Update was successful.
        success = true;
      }
    }

    return success;
  }
}
