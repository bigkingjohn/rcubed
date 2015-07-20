package com.example.rcubed;

import static com.mongodb.client.model.Filters.eq;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;

import com.example.rcubed.Photo.Visibility;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * DAO (data access object) providing the interface for all the MongoDb operations that can be performed on a Photo.
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
  public static final String KEY_GFS_ID = "gfsid";
  public static final String KEY_OWNER = "Owner";
  public static final String KEY_TITLE = "Title";
  public static final String KEY_TAGS = "Tags";
  public static final String KEY_COMMENTS = "Comments";
  public static final String KEY_TIMESTAMP = "Timestamp";
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

    doc.append(KEY_GFS_ID, photo.getGFSPhotoId());
    doc.append(KEY_OWNER, photo.getOwner());
    doc.append(KEY_TITLE, photo.getTitle());

    // Convert the tags into a list/array structure suitable for Mongo Db.
    BasicBSONList bsonTags = new BasicBSONList();
    bsonTags.addAll(photo.getTags());
    doc.append(KEY_TAGS, bsonTags);

    // Convert the comments into a list/array structure suitable for Mongo Db.
    BasicBSONList bsonComments = new BasicBSONList();
    bsonComments.addAll(photo.getComments());
    doc.append(KEY_COMMENTS, bsonComments);

    doc.append(KEY_TIMESTAMP, photo.getTimeStamp().toInstant().getEpochSecond());
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
  public static Photo createPhoto(Document doc, GridFS gfs)
  {
    // Create an empty instance of User.
    Photo photo = new Photo();

    // Populate the relevant fields.

    // Object Id must be converted to a string.
    ObjectId id = (ObjectId) doc.get(KEY_ID);
    photo.setId(id.toString());

    // We dont' bother convierting this objectObject Id must be converted to a string.
    photo.setGFSPhotoId(doc.get(KEY_GFS_ID));

    photo.setOwner(doc.getString(KEY_OWNER));
    photo.setTitle(doc.getString(KEY_TITLE));

    // Convert the db list representation to an array list.
    ArrayList<String> tags = (ArrayList<String>) doc.get(KEY_TAGS);
    photo.setTags(tags);

    // Convert the db list representation to an array list.
    ArrayList<String> comments = (ArrayList<String>) doc.get(KEY_COMMENTS);
    photo.setComments(comments);

    photo.setTimeStamp(Date.from(Instant.ofEpochMilli(doc.getLong(KEY_TIMESTAMP))));

    // Visibility must be converted to the relevant enum value.
    photo.setVisibility(Photo.VisibilityValues[(int) doc.get(KEY_VISIBILITY)]);

    // Raw data for the image is accessed through GridFS
    GridFSDBFile out = gfs.findOne(new BasicDBObject("_id", photo.getGFSPhotoId()));
    ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();

    try
    {
      out.writeTo(imageBuffer);
      photo.setImage(imageBuffer.toByteArray());
    }
    catch (IOException ex)
    {
      // TODO Probably want to display some default error image.
    }

    return photo;
  }

  /**
   * The collection in our database which contains all the photos.
   */
  private MongoCollection<Document> collection;

  /**
   * Handle to the GridFS object which is used for storing the raw image data in the db.
   */
  private GridFS gfs;

  /**
   * Constructor
   * 
   * @param db          A handle to the mongo database.
   * @param oldStyleDb  The most recent version of mongo db did not include updates for GridFS
   *                    The grid FS constructor therefore still requires the the older DB class and not the newer
   *                    MongoDatabase class.
   */
  public MongoDbDAOPhoto(MongoDatabase db, DB oldStyleDb)
  {
    collection = db.getCollection(PHOTOS_COLLECTION);
    gfs = new GridFS(oldStyleDb);
  }

  /**
   * See com.example.rcubed.PhotoDAO.insertPhoto(Photo photo)
   */
  @Override
  public boolean insertPhoto(Photo photo)
  {
    // Default return value to false.
    boolean success = false;

    // Because images can be larger than 16Mb (which is a MongoDb limit for single documents) we use GridFS to store the
    // image data. This actually stores it in a couple of collections which are different from the one with
    GridFSInputFile in = gfs.createFile(photo.getImage());
    photo.setGFSPhotoId(in.getId());
    in.save();

    Document doc = convertToDoc(photo);

    // No return type or exceptions thrown... this must always succeed!
    collection.insertOne(doc);
    success = true;

    // Update the photo object with the ID thats been assigned by Mongo Db.
    ObjectId id = (ObjectId) doc.get(KEY_ID);
    photo.setId(id.toString());

    return success;
  }

  /**
   * See com.example.rcubed.PhotoDAO.deletePhoto(User user, Photo photo)
   */
  @Override
  public boolean deletePhoto(User user, Photo photo)
  {
    // Default return value to false.
    boolean success = false;

    // Only allowed to delete photo if the user doing the changing is the photo owner.
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

  /**
   * See com.example.rcubed.PhotoDAO.addTag(User user, Photo photo, String newTag)
   */
  @Override
  public boolean addTag(User user, Photo photo, String newTag)
  {
    // Default return value to false.
    boolean success = false;

    // Only allowed to change photo properties if the user doing the changing is the photo owner.
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

          // Modify the local copy.
          photo.addTag(newTag);
        }
      }
    }

    return success;
  }

  /**
   * See com.example.rcubed.PhotoDAO.removeTag(User user, Photo photo, String exTag
   */
  @Override
  public boolean removeTag(User user, Photo photo, String exTag)
  {
    // Default return value to false.
    boolean success = false;

    // Only allowed to change photo properties if the user doing the changing is the photo owner.
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

          // Update the local copy
          photo.removeTag(exTag);
        }
      }
    }

    return success;
  }

  /**
   * See com.example.rcubed.PhotoDAO.addComment(User user, Photo photo, String newComment)
   */
  @Override
  public boolean addComment(User user, Photo photo, String newComment)
  {
    // Default return value to false.
    boolean success = false;

    String commentWithUser = user.getUsername() + ": " + newComment;

    Document match = new Document();
    match.append(KEY_ID, new ObjectId(photo.getId()));

    Document newItem = new Document();
    newItem.append(KEY_COMMENTS, commentWithUser);

    Document pushQuery = new Document();
    pushQuery.append("$push", newItem);

    UpdateResult result = collection.updateOne(match, pushQuery);

    if (result.getMatchedCount() == 1 && result.getModifiedCount() == 1)
    {
      // Update was successful.
      success = true;

      // Modify the local copy.
      photo.addComment(commentWithUser);
    }

    return success;
  }

  /**
   * See com.example.rcubed.PhotoDAO.getPhotos(String owner, String tag, Photo.Visibility allowedVisibility)
   */
  @Override
  public ArrayList<Photo> getPhotos(String owner, String tag, Photo.Visibility allowedVisibility)
  {
    // Structure to hold the data pulled from the db.
    ArrayList<Photo> photos = new ArrayList<Photo>();

    // Generate the query.
    Document query = new Document();
    query.append(KEY_OWNER, owner);

    // Should we limit the search by tag?
    if (tag != null && !"".equals(tag))
    {
      query.append(KEY_TAGS, tag);
    }

    // Only allowed to see documents above the specified visibility level.
    Document visibilityCheck = new Document();
    visibilityCheck.append("$gte", allowedVisibility.ordinal());
    query.append(KEY_VISIBILITY, visibilityCheck);

    // Run the query.
    MongoCursor<Document> cursor = collection.find(query).iterator();

    // Iterate over the results.
    try
    {
      while (cursor.hasNext())
      {
        Photo photo = createPhoto(cursor.next(), gfs);
        photos.add(photo);
      }
    }
    finally
    {
      cursor.close();
    }

    return photos;
  }

  /**
   * See com.example.rcubed.PhotoDAO.changeVisibility(User user, Photo photo, Visibility newVisibility)
   */
  @Override
  public boolean changeVisibility(User user, Photo photo, Visibility newVisibility)
  {
    // Default return value to false.
    boolean success = false;

    // Only allowed to change photo properties if the user doing the changing is the photo owner.
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

        // Update the local copy.
        photo.setVisibility(newVisibility);
      }
    }

    return success;
  }
}
