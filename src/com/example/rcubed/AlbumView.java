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

    // set the date format to use.
    dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);

    // Insantiate the photo comparator.
    pdc = new PhotoDateComparator();

    // Make this view take up the full browser window space
    setSizeFull();

    logo = new Label(user.getUsername() + "'s Realistic Reality Recreations");

    // Configure the tag input field.
    tagSearch.setInputPrompt("<tag to filter photos by. If empty, get all photos.>");
    tagSearch.setNullRepresentation(null);

    friendSearch.setInputPrompt("<Enter a friend's name to see their photos>");
    friendSearch.setNullRepresentation(null);

    // Configure the login button.
    logout.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        // "Logout" the user
        getSession().setAttribute("user", null);

        // Refresh the view - should redirect to login page because of some "magic" in the view change handler detecting
        // that we've set the user to null.
        getUI().getNavigator().navigateTo(LoginView.NAME);
      }
    });

    // Arrange the components within the view.
    HorizontalLayout header = new HorizontalLayout(logo, logout);
    header.setSpacing(true);

    // Configure the upload photo button.
    getPhotos.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        User owner = ((friendSearch.getValue() == null) || ("".equals(friendSearch.getValue()))) ? user : daoUser
            .getUser(daoPhoto, friendSearch.getValue());

        if (owner != null)
        {
          ArrayList<Photo> photos = user.getPhotos(owner, tagSearch.getValue());
          VerticalLayout photoList = new VerticalLayout();
          photoList.setSpacing(true);
          photos.sort(pdc);

          for (Photo photo : photos)
          {
            // Create an UI image resource from the photo's image data.
            // Grid layout has one colum. We begin with 1 row, because the grid layout is clever enough to extend this
            // every time a new component is added to it.
            GridLayout imageDataContainer = new GridLayout(1, 1);
            imageDataContainer.setMargin(true);
            imageDataContainer.setWidth("100%");

            // Add the Photos Meta Data
            imageDataContainer.addComponent(new Label("Title: " + photo.getTitle()));
            imageDataContainer.addComponent(new Label("Upload Date: " + dateFormat.format(photo.getTimeStamp())));

            // The tags are present as a caption on the photo rather than a Label for no particular reason.
            Image photoInUI = new Image(photo.getTags().toString(), new StreamResource(photo, photo.getId()));

            imageDataContainer.addComponent(photoInUI);
            // Scale the image to fit the container.
            photoInUI.setWidth("100%");

            // Add a new comment box and button.
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
            // @@@ JAS how will these be ordered?
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
          // owner is null. That can only happen if some text was in the friend search bar that didnt match a user of
          // our system. Give some indication to the user that there's been a problem.
          new Notification("Unknown user: ." + friendSearch.getValue(), Notification.Type.WARNING_MESSAGE).show(Page
              .getCurrent());
        }
      }
    });

    HorizontalLayout searchBar = new HorizontalLayout(tagSearch, getPhotos);
    searchBar.setSpacing(true);

    friendsContainer.addContainerProperty("Friends", String.class, "unknown");

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

    // Configure the remove friendbutton.
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

          // Removing a non existent ex-friend doesnt cause any problems (according to the method comments).
          // But that shouldnt be possible because we require a user selection.
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

    photoPanel.setWidth(500, Unit.PIXELS);
    photoPanel.setHeight(500, Unit.PIXELS);

    HorizontalLayout mainWindow = new HorizontalLayout(photoPanel, friendBar);
    mainWindow.setExpandRatio(photoPanel, 3);
    mainWindow.setExpandRatio(friendBar, 1);

    newTitle.setInputPrompt("<Title>");
    newTitle.setCaption("Add a title to your photo.");

    newTags.setInputPrompt("<Add Tags>");
    newTags.setCaption("Add tags to your photograph.  This can be a comma separated list or empty.");

    // Configure the upload photo button.
    PhotoReceiver photoRec = new PhotoReceiver(user.getUsername(), newTitle, newTags, 2, daoPhoto);
    newPhoto = new Upload("Upload new photo.", photoRec);
    newPhoto.setCaption("Upload");
    newPhoto.addSucceededListener(photoRec);

    VerticalLayout uploadBox = new VerticalLayout(newPhoto, newTitle, newTags);
    uploadBox.setSpacing(true);

    VerticalLayout mainLayout = new VerticalLayout(header, searchBar, friendSearch, mainWindow, uploadBox);
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
