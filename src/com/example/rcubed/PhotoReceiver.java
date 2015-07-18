package com.example.rcubed;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

/**
 * Handles upload of images to the server.
 */
public class PhotoReceiver implements Receiver, SucceededListener
{
  /**
   * The output stream which stores the file once uploaded.
   */
  private ByteArrayOutputStream fileBuffer;

  /**
   * The name of the user uploading this file.
   */
  private String owner;

  /**
   * The UI component holding the title the user has assign to this photo.
   */
  private TextField titleField;

  /**
   * The UI component holding any tags the user wishes to assign to this photo.
   */
  private TextField tagsField;

  /**
   * The UI component holding the information about the desired visibility of this photo.
   */
  private int visibilityField;

  /**
   * The DAO for accessing the database.
   */
  PhotoDAO photoDAO;

  /**
   * Constructor
   * 
   * @param owner            The name of the user uploading the photo.
   * @param tagsField        A TextField containing a comma separated of tags the user wants to attach to this photo.
   * @param visibilityField  The visibility setting the user is assigning to this photo.
   * @param photoDAO         The DAO for accessing the database.
   */
  public PhotoReceiver(String owner, TextField titleField, TextField tagsField, int visibilityField, PhotoDAO photoDAO)
  {
    fileBuffer = null;
    this.owner = owner;
    this.titleField = titleField;
    this.tagsField = tagsField;
    this.visibilityField = visibilityField;
    this.photoDAO = photoDAO;
  }

  /**
   * See com.vaadin.ui.Upload.Receiver.receiveUpload(String filename, String mimeType)
   */
  @Override
  public OutputStream receiveUpload(String filename, String mimeType)
  {
    try
    {
      // Create a buffer to store the image data.
      fileBuffer = new ByteArrayOutputStream();
    }
    catch (Exception ex)
    {
      // Give some indication to the user that there's been a problem.
      new Notification("Exception" + ex.getStackTrace(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
    }

    return fileBuffer;
  }

  /**
   * See com.vaadin.ui.Upload.SucceededListener.uploadSucceeded(SucceededEvent event)
   */
  @Override
  public void uploadSucceeded(SucceededEvent event)
  {
    // Convert the tags to the necessary data structure.
    ArrayList<String> tags = new ArrayList<String>();
    String[] tagsArray = tagsField.getValue().split(",");

    for (String tag : tagsArray)
    {
      tags.add(tag);
    }

    Photo photo = new Photo(owner, titleField.getValue(), tags, Photo.VisibilityValues[visibilityField],
        fileBuffer.toByteArray());

    // Save the photo to the database.
    photoDAO.insertPhoto(photo);

    // Clear the new photo info fields.
    titleField.setValue("");
    tagsField.setValue("");

    // Alert the user of the successful upload.
    new Notification("Success", Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());

  }
}
