package com.example.rcubed;

import java.util.ArrayList;

import com.example.rcubed.Photo.Visibility;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class testing
{
  public enum test
  {
    FIRST,
    SECOND,
    THIRD,
  }

  public static void main(String[] args)
  {
    System.out.println("Testing.");

    MongoClient client = new MongoClient("localhost");
    MongoDatabase db = client.getDatabase("Rcubed");
    DB oldStyleDb = client.getDB("Rcubed");

    // testUser(db, oldStyleDb);
    // testPhoto(db, oldStyleDb);
    // testEnum();
    testComments(db, oldStyleDb);

    client.close();
  }

  private static void testEnum()
  {
    System.out.println("first: " + String.valueOf(test.FIRST.ordinal()));
    System.out.println("second: " + String.valueOf(test.SECOND.ordinal()));
    System.out.println("third: " + String.valueOf(test.THIRD.ordinal()));
  }

  private static void testComments(MongoDatabase db, DB oldStyleDb)
  {
    PhotoDAO photoDAO = new MongoDbDAOPhoto(db, oldStyleDb);
    UserDAO userDAO = new MongoDbDAOUser(db);

    User user = userDAO.getUser(photoDAO, "bob@s.com");

    ArrayList<Photo> photos = photoDAO.getPhotos("bob@s.com", "animal", Visibility.PUBLIC);

    System.out.println("There are: " + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      photoDAO.addComment(user, photo, "Test comment");
    }
  }

  private static void testPhoto(MongoDatabase db, DB oldStyleDb)
  {
    PhotoDAO photoDAO = new MongoDbDAOPhoto(db, oldStyleDb);
    UserDAO userDAO = new MongoDbDAOUser(db);

    // Create Photo1
    Photo p1 = new Photo();
    p1.setOwner("Fred");
    ArrayList<String> p1Tags = new ArrayList<String>();
    p1Tags.add("Christmas");
    p1Tags.add("bob");
    p1.setTags(p1Tags);
    p1.setVisibility(Photo.Visibility.PUBLIC);
    photoDAO.insertPhoto(p1);

    // Create Photo2
    Photo p2 = new Photo();
    p2.setOwner("Fred");
    ArrayList<String> p2Tags = new ArrayList<String>();
    p2Tags.add("bob");
    p2.setTags(p2Tags);
    p2.setVisibility(Photo.Visibility.FRIENDS);
    photoDAO.insertPhoto(p2);

    // Create Photo3
    Photo p3 = new Photo();
    p3.setOwner("Fred");
    ArrayList<String> p3Tags = new ArrayList<String>();
    p3Tags.add("Christmas");
    p3Tags.add("Wife");
    p3.setTags(p3Tags);
    p3.setVisibility(Photo.Visibility.PRIVATE);
    photoDAO.insertPhoto(p3);

    // Create Photo4
    Photo p4 = new Photo();
    p4.setOwner("Fred");
    ArrayList<String> p4Tags = new ArrayList<String>();
    p4Tags.add("Wife");
    p4.setTags(p4Tags);
    p4.setVisibility(Photo.Visibility.PRIVATE);
    photoDAO.insertPhoto(p4);

    // Create Photo5
    Photo p5 = new Photo();
    p5.setOwner("Fred");
    ArrayList<String> p5Tags = new ArrayList<String>();
    p5.setTags(p5Tags);
    p5.setVisibility(Photo.Visibility.PUBLIC);
    photoDAO.insertPhoto(p5);

    // Create photo owner with bob as a friend
    User owner = new User(photoDAO);
    owner.setUsername("Fred");
    ArrayList<String> fredsFriends = new ArrayList<String>();
    fredsFriends.add("bob");
    owner.setFriends(fredsFriends);
    userDAO.insertUser(owner);

    // Create friend (bob)
    User friend = new User(photoDAO);
    friend.setUsername("bob");
    friend.setFriends(new ArrayList<String>());
    userDAO.insertUser(friend);

    // Create random user
    User stranger = new User(photoDAO);
    stranger.setUsername("Alice");
    stranger.setFriends(new ArrayList<String>());
    userDAO.insertUser(stranger);

    // Get all Fred's Photos.
    ArrayList<Photo> photos = owner.getPhotos(owner, null);
    System.out.println("Fred's Photos: " + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      printPhoto(photo);
    }

    // Get Fred's Christmas photos.
    photos = owner.getPhotos(owner, "Christmas");
    System.out.println("Fred's christmas Photos: " + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      printPhoto(photo);
    }

    // Get Fred's Wife photos.
    photos = owner.getPhotos(owner, "Wife");
    System.out.println("Fred's Wife Photos: " + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      printPhoto(photo);
    }

    // Bob wants to see all Fred;s photos.
    // But he's not allowed to see the ones of Fred'wife.
    photos = friend.getPhotos(owner, null);
    System.out.println("Bob looking at all Freds photos (should be 3): " + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      printPhoto(photo);
    }

    // Now Bob wants to see just the christmas photos.
    // Again, he's not allowed to see the one of Fred'wife.
    photos = friend.getPhotos(owner, "Christmas");
    System.out.println("Bob's looking at Fred's christmas photos (should be 1): " + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      printPhoto(photo);
    }

    // A stranger should only ever get to see the public photos.
    photos = stranger.getPhotos(owner, null);
    System.out.println("a Stranger is looking at Fred's photos (should be 2): " + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      printPhoto(photo);
    }

    // Bob attempts to change the visibility of fred's wife's christmas photo.
    // Bob should still only see the one photo.
    photoDAO.changeVisibility(friend, p3, Photo.Visibility.FRIENDS);
    photos = friend.getPhotos(owner, "Christmas");
    System.out.println("Bob's attempted to hack the christmas wife but failed (should be 1): "
        + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      printPhoto(photo);
    }

    // Fred has relented and is now willing to show bob the photo.
    photoDAO.changeVisibility(owner, p3, Photo.Visibility.FRIENDS);
    photos = friend.getPhotos(owner, "Christmas");
    System.out.println("Fred is now showing Bob the photo of his wife at christmas (should be 2): "
        + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      printPhoto(photo);
    }

    // Bob points out that he is in the photo of Fred's wife at christmas time so fred add's a tag.
    photoDAO.addTag(owner, p3, "bob");
    photos = owner.getPhotos(owner, "bob");
    System.out.println("Photo is now tagged with bob (should be 3): " + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      printPhoto(photo);
    }

    // Fred lets out a sigh of relief when bob realises it can't be Fred's wife. It's the office secretary in the photo.
    photoDAO.removeTag(owner, p3, "Wife");
    photos = owner.getPhotos(owner, "Wife");
    System.out.println("There is now only the 1 photo of Fred's wife: " + String.valueOf(photos.size()));

    for (Photo photo : photos)
    {
      printPhoto(photo);
    }

    // tidy up photos
    photoDAO.deletePhoto(owner, p1);
    photoDAO.deletePhoto(owner, p2);
    photoDAO.deletePhoto(owner, p3);
    photoDAO.deletePhoto(owner, p4);
    photoDAO.deletePhoto(owner, p5);

    // Tidy up users
    userDAO.deleteUser(owner);
    userDAO.deleteUser(friend);
    userDAO.deleteUser(stranger);
  }

  private static void testUser(MongoDatabase db, DB oldStyleDb)
  {
    UserDAO userDAO = new MongoDbDAOUser(db);
    PhotoDAO photoDAO = new MongoDbDAOPhoto(db, oldStyleDb);

    // Create user1
    User user1 = new User(photoDAO);
    user1.setUsername("bob");
    user1.setFriends(new ArrayList<String>());
    userDAO.insertUser(user1);

    // Create user2
    User user2 = new User(photoDAO);
    user2.setUsername("Fred");
    ArrayList<String> fredsFriends = new ArrayList<String>();
    fredsFriends.add("bob");
    user2.setFriends(fredsFriends);
    userDAO.insertUser(user2);

    // Do user1 and user2 exist?
    System.out.println("Users added");
    System.out.println("bob's Id: " + user1.getId());
    System.out.println("Fred's Id: " + user2.getId());

    // Delete user1
    userDAO.deleteUser(user1);
    System.out.println("Deleted bob");

    // Pull user2 from the db.
    user2 = userDAO.getUser(photoDAO, "Fred");
    System.out.println("new Fred's Id: " + user2.getId());
    System.out.println("  name: " + user2.getUsername());
    System.out.println("  friends:");

    for (String friend : user2.getFriends())
    {
      System.out.println("    " + friend);
    }

    // Add a friend to user2
    userDAO.addFriend(user2, "Alice");

    user2 = userDAO.getUser(photoDAO, "Fred");
    System.out.println("new Fred's Id: " + user2.getId());
    System.out.println("  name: " + user2.getUsername());
    System.out.println("  friends:");

    for (String friend : user2.getFriends())
    {
      System.out.println("    " + friend);
    }

    // Add same friend to user2 - should not be updated!
    userDAO.addFriend(user2, "Alice");
    user2 = userDAO.getUser(photoDAO, "Fred");
    System.out.println("new Fred's Id: " + user2.getId());
    System.out.println("  name: " + user2.getUsername());
    System.out.println("  friends:");

    for (String friend : user2.getFriends())
    {
      System.out.println("    " + friend);
    }

    // Add new friend to user2.
    userDAO.addFriend(user2, "Julia");
    user2 = userDAO.getUser(photoDAO, "Fred");
    System.out.println("new Fred's Id: " + user2.getId());
    System.out.println("  name: " + user2.getUsername());
    System.out.println("  friends:");

    for (String friend : user2.getFriends())
    {
      System.out.println("    " + friend);
    }

    // Bob is no longer user2's friend
    userDAO.removeFriend(user2, "bob");
    user2 = userDAO.getUser(photoDAO, "Fred");
    System.out.println("new Fred's Id: " + user2.getId());
    System.out.println("  name: " + user2.getUsername());
    System.out.println("  friends:");

    for (String friend : user2.getFriends())
    {
      System.out.println("    " + friend);
    }

    // delete bob as user2s friend again and ensure there is no error.
    userDAO.removeFriend(user2, "bob");
    user2 = userDAO.getUser(photoDAO, "Fred");
    System.out.println("new Fred's Id: " + user2.getId());
    System.out.println("  name: " + user2.getUsername());
    System.out.println("  friends:");

    for (String friend : user2.getFriends())
    {
      System.out.println("    " + friend);
    }

    // Delete user2 to clean up!
    userDAO.deleteUser(user2);

    // What happens with an unknown user?
    user2 = userDAO.getUser(photoDAO, "Fred");

    if (user2 == null)
    {
      System.out.println("No user found");
    }
  }

  public static void printPhoto(Photo photo)
  {
    System.out.println("  _id: " + photo.getId());
    System.out.println("    Owner: " + photo.getOwner());
    System.out.println("    Visibility: " + photo.getVisibility().toString());
    System.out.println("    tags:");
    for (String tag : photo.getTags())
    {
      System.out.println("      " + tag);
    }
  }
}
