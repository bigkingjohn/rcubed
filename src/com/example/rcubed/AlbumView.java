package com.example.rcubed;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

/**
 * The UI/Page for the main part of the Photo Album Web App.
 * 
 * Allows users to view photos (theirs and their friends) upload photos, and manage their friends list.
 */
public class AlbumView extends CustomComponent implements View
{
  UserDAO daoUser;
  PhotoDAO daoPhoto;
  User user;

  /**
   * For Vaadin navigation purposes.
   */
  public static final String NAME = "album";

  /**
   * Page components
   */
  Label logo;
  Button logout = new Button("Log Out");

  TextField tagSearch = new TextField();
  TextField friendSearch = new TextField();
  Button getPhotos = new Button("Get Photos!");

  Panel photoPanel = new Panel();

  Grid friendsList = new Grid();
  IndexedContainer friendsContainer = new IndexedContainer();
  TextField newFriend = new TextField();
  Button addFriend = new Button("Add Friend");
  Button removeFriend = new Button("Remove Friend");

  Upload newPhoto;
  TextField newTitle = new TextField();
  TextField newTags = new TextField();
  Button uploadPhoto = new Button("Upload");

  /**
   * Format of the date/time presented the upload time of photographs.
   */
  private static final String DATE_FORMAT_STRING = "dd/MM/yyyy HH:mm:ss";
  private DateFormat dateFormat;

  /**
   * Custom comparator for sorting photos by their upload time.
   */
  private PhotoDateComparator pdc;

  /**
   * Constructor
   */
  public AlbumView()
  {
    // Get persistent state objects from the session.
    daoUser = (UserDAO) VaadinSession.getCurrent().getAttribute("userDAO");
    daoPhoto = (PhotoDAO) VaadinSession.getCurrent().getAttribute("photoDAO");
    user = (User) VaadinSession.getCurrent().getAttribute("user");

    if ((user == null) || (daoUser == null) || (daoPhoto == null))
    {
      // Something has gone wrong with the login process - return the user to the login view.
      getUI().getNavigator().navigateTo(LoginView.NAME);

      // TODO ideally display some kind of error message.
    }

    // set the date format to use.
    dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);

    // Insantiate the photo comparator.
    pdc = new PhotoDateComparator();

    // Make this view take up the full browser window space
    setSizeFull();

    // Sort out the UI.
    logo = new Label(user.getUsername() + "'s Realistic Reality Recreations");

    // Configure the logout button.
    logout.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        // "Logout" the user
        getSession().setAttribute("user", null);

        // Return to Login View.
        getUI().getNavigator().navigateTo(LoginView.NAME);
      }
    });

    // Arrange the header components within a container.
    HorizontalLayout header = new HorizontalLayout(logo, logout);
    header.setSpacing(true);

    // Tag input field.
    tagSearch.setInputPrompt("<tag to filter photos by. If empty, get all photos.>");
    tagSearch.setNullRepresentation(null);

    // Friends photos search input field.
    friendSearch.setInputPrompt("<Enter another user's name to see their photos>");
    friendSearch.setNullRepresentation(null);

    // Configure the get photo button.
    getPhotos.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        // Work out who's photos to get. If there is a value in the friend search field then we use that.
        // If that field is empty, then we get the current user's photos.
        User owner = ((friendSearch.getValue() == null) || ("".equals(friendSearch.getValue()))) ? user : daoUser
            .getUser(daoPhoto, friendSearch.getValue());

        if (owner != null)
        {
          // Display the Photos in the UI.
          ArrayList<Photo> photos = user.getPhotos(owner, tagSearch.getValue());
          VerticalLayout photoList = new VerticalLayout();
          photoList.setSpacing(true);
          photos.sort(pdc);

          for (Photo photo : photos)
          {
            // Create a UI image resource from the photo's image data.
            // Grid layout has one column. We begin with 1 row, because we don't know exactly how many we need and the
            // grid layout is clever enough to extend itself every time a new component is added.
            GridLayout imageDataContainer = new GridLayout(1, 1);
            imageDataContainer.setMargin(true);
            imageDataContainer.setWidth("100%");

            // Add the Photo's Meta Data
            imageDataContainer.addComponent(new Label("Title: " + photo.getTitle()));
            imageDataContainer.addComponent(new Label("Upload Date: " + dateFormat.format(photo.getTimeStamp())));

            // The tags are present as a caption on the photo rather than a Label for no particular reason.
            Image photoInUI = new Image(photo.getTags().toString(), new StreamResource(photo, photo.getId()));

            // Add the image data to the parent UI container.
            imageDataContainer.addComponent(photoInUI);

            // Scale the image to fit the container.
            photoInUI.setWidth("100%");

            // Add the new comment box and button.
            HorizontalLayout newCommentContainer = new HorizontalLayout();
            TextField newComment = new TextField();
            newComment.setInputPrompt("<Add new comment>");

            Button addComment = new Button("Add");

            addComment.addClickListener(new Button.ClickListener()
            {
              @Override
              public void buttonClick(ClickEvent event)
              {
                if (newComment.getValue() != null && !"".equals(newComment.getValue()))
                {
                  daoPhoto.addComment(user, photo, newComment.getValue());

                  // Add the new comment to the UI.
                  imageDataContainer.addComponent(new Label(newComment.getValue()));

                  // Clear the comment box
                  newComment.setValue("");
                }
              }
            });

            newCommentContainer.addComponent(newComment);
            newCommentContainer.addComponent(addComment);

            imageDataContainer.addComponent(newCommentContainer);

            // Add each comment as a new label below the photo.
            // The comments are ordered by their insertion into the database. We don't explicitly set their
            // chronological order the way we do for the photos themselves.
            for (String comment : photo.getComments())
            {
              imageDataContainer.addComponent(new Label(comment));
            }

            // Add the Image and all its associated data to the container.
            photoList.addComponent(imageDataContainer);
          }

          // Set the Photo Panel's content - this will replace any existing photos.
          photoPanel.setContent(photoList);
        }
        else
        {
          // Owner is null. That can only happen if some text was in the friend search bar that didn't match a user of
          // our system. Give some indication to the user that there's been a problem.
          new Notification("Unknown user: ." + friendSearch.getValue(), Notification.Type.WARNING_MESSAGE).show(Page
              .getCurrent());
        }
      }
    });

    HorizontalLayout photoSearchBar = new HorizontalLayout(tagSearch, getPhotos);
    photoSearchBar.setSpacing(true);

    // Friends list.
    friendsContainer.addContainerProperty("Friends", String.class, "unknown");

    // Add friends to the list.
    for (String name : user.getFriends())
    {
      Item newItem = friendsContainer.getItem(friendsContainer.addItem());
      newItem.getItemProperty("Friends").setValue(name);
    }

    friendsList.setHeightByRows(12);
    friendsList.setWidth(150, Unit.PIXELS);
    friendsList.setContainerDataSource(friendsContainer);

    newFriend.setInputPrompt("<Friends Name>");
    newFriend.addValidator(new EmailValidator("Friends name must be an email address"));
    newFriend.setInvalidAllowed(false);

    // Configure the add friend button.
    addFriend.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        String newFriendName = newFriend.getValue();

        // Check if the input name is valid, and is not the user.
        if (newFriend.isValid() && !user.getUsername().equals(newFriendName))
        {
          // Check if name being added is a user of our system.
          User friend = daoUser.getUser(daoPhoto, newFriendName);

          if (friend != null)
          {
            // Add the friend to this user (backend only).
            daoUser.addFriend(user, newFriendName);

            // Add the friend to the frontend container.
            Item newItem = friendsContainer.getItem(friendsContainer.addItem());
            newItem.getItemProperty("Friends").setValue(newFriendName);

            // Reset the input field.
            newFriend.setValue("");
          }
          else
          {
            // Send an error response indicating unknown user.
            new Notification("Unable to add " + newFriendName + ": Unknown user.", Notification.Type.ERROR_MESSAGE)
                .show(Page.getCurrent());

            // Return focus to the username field.
            newFriend.focus();
          }
        }
      }
    });

    // Configure the remove friend button.
    removeFriend.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        // Identify the friend(s) to be removed.
        Object selectedFriend = friendsList.getSelectedRow();

        if (selectedFriend != null)
        {
          Item exFriend = friendsContainer.getItem(selectedFriend);

          // Removing a non existent ex-friend doesn't cause any problems (according to the method comments).
          // But that shouldn't be possible because we require a user selection.
          daoUser.removeFriend(user, (String) exFriend.getItemProperty("Friends").getValue());

          // Now remove him from front end container.
          friendsContainer.removeItem(selectedFriend);
        }
        else
        {
          // Send an error response indicating unknown user.
          new Notification("Select a friend to remove.", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
        }
      }
    });

    VerticalLayout friendBar = new VerticalLayout(friendsList, newFriend, addFriend, removeFriend);

    // Main panel (contains the image list and the friends list).
    photoPanel.setWidth(500, Unit.PIXELS);
    photoPanel.setHeight(500, Unit.PIXELS);

    HorizontalLayout mainWindow = new HorizontalLayout(photoPanel, friendBar);
    mainWindow.setExpandRatio(photoPanel, 3);
    mainWindow.setExpandRatio(friendBar, 1);

    // New Photo Upload section.
    newTitle.setInputPrompt("<Title>");
    newTitle.setCaption("Add a title to your photo.");

    newTags.setInputPrompt("<Add Tags>");
    newTags.setCaption("Add tags to your photograph.  This can be a comma separated list or empty.");

    // Configure the upload photo button.
    // The button is part of the Upload component, and it's on click listener calls recieveUpload() of the
    // PhotoReceiver.
    // TODO There is no built in validation that the user has actually selected a file.
    PhotoReceiver photoRec = new PhotoReceiver(user.getUsername(), newTitle, newTags, 2, daoPhoto);
    newPhoto = new Upload("Upload new photo.", photoRec);
    newPhoto.setCaption("Upload");
    newPhoto.addSucceededListener(photoRec);

    VerticalLayout uploadBox = new VerticalLayout(newPhoto, newTitle, newTags);
    uploadBox.setSpacing(true);

    // Add everything to the top level UI container.
    VerticalLayout mainLayout = new VerticalLayout(header, photoSearchBar, friendSearch, mainWindow, uploadBox);
    mainLayout.setSpacing(true);
    mainLayout.setMargin(true);

    // Fix the layout.
    setCompositionRoot(mainLayout);
  }

  @Override
  public void enter(ViewChangeEvent event)
  {
    // Nothing to do here.
  }

}
