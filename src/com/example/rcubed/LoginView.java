package com.example.rcubed;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Landing page for the Photo Album Web App.
 * 
 * Provides ability to login with an email (no password required) or create a new user.
 *
 */
public class LoginView extends CustomComponent implements View
{
  /**
   * Handle to the database connection.
   */
  private MongoClient mongoClient;

  /**
   * Handle to the actual database.
   */
  private MongoDatabase database;

  UserDAO daoUser;
  PhotoDAO daoPhoto;

  /**
   * For Vaadin navigation purposes.
   */
  public static final String NAME = "login";

  /**
   * Page components
   */
  Label logo = new Label("Realistic Reality Recreations");
  TextField usernameField = new TextField();
  Button login = new Button("Login");
  Button newUser = new Button("New User");

  /**
   * Constructor
   */
  public LoginView()
  {
    // Initialise a connection to the db and the DAOs.
    mongoClient = new MongoClient("localhost");
    database = mongoClient.getDatabase("Rcubed");
    daoPhoto = new MongoDbDAOPhoto(database, mongoClient.getDB("Rcubed"));
    daoUser = new MongoDbDAOUser(database);

    // Store the DAOs in the session so we can use them in other views.
    VaadinSession.getCurrent().setAttribute("userDAO", daoUser);
    VaadinSession.getCurrent().setAttribute("photoDAO", daoPhoto);

    // Make this view take up the full browser window space
    setSizeFull();

    // Configure the username input field.
    usernameField.setInputPrompt("<Enter Username>");
    usernameField.setRequired(true);
    usernameField.addValidator(new EmailValidator("Username must be an email address"));
    usernameField.setInvalidAllowed(false);

    // Configure the login button.
    login.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        // Confirm the username is an email address. (not the docs for the Email validator indicate that it is not fully
        // RFC compliant.
        if (usernameField.isValid())
        {
          // Lookup the user.
          User user = daoUser.getUser(daoPhoto, usernameField.getValue());

          if (user != null)
          {
            // Add the user object to this session
            getSession().setAttribute("user", user);

            // Redirect to album view
            getUI().getNavigator().navigateTo(AlbumView.NAME);
          }
          else
          {
            // Send an error response indicating unknown user.
            new Notification("Unknown user: ." + usernameField.getValue(), Notification.Type.WARNING_MESSAGE).show(Page
                .getCurrent());

            // Return focus to the username field.
            usernameField.focus();
          }
        }
      }
    });

    // Configure the new user button.
    newUser.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        if (usernameField.isValid())
        {
          User user = daoUser.getUser(daoPhoto, usernameField.getValue());

          if (user == null)
          {
            // Add this user to the db.
            user = new User(usernameField.getValue(), daoPhoto);
            daoUser.insertUser(user);

            // Add the user object to this session
            getSession().setAttribute("user", user);

            // Redirect to album view
            getUI().getNavigator().navigateTo(AlbumView.NAME);
          }
          else
          {
            // Send an error response indicating unknown user.
            new Notification("Username \"" + usernameField.getValue() + "\" already in use.",
                Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());

            // Return focus to the username field.
            usernameField.focus();
          }
        }
      }
    });

    // Arrange the components within the UI container.
    HorizontalLayout buttonLayout = new HorizontalLayout(login, newUser);
    buttonLayout.setSpacing(true);

    VerticalLayout mainLayout = new VerticalLayout(logo, usernameField, buttonLayout);
    mainLayout.setSpacing(true);
    mainLayout.setMargin(true);

    // Fix the layout.
    setCompositionRoot(mainLayout);
  }

  @Override
  public void enter(ViewChangeEvent event)
  {
    // Focus on the username field when user arrives to the login view.
    usernameField.focus();
  }

}
