package com.example.rcubed;

import javax.servlet.annotation.WebServlet;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Theme("rcubed")
public class RcubedUI extends UI
{
  /**
   * The url of the database we want to connect to.
   */
  public static final String DATABASE_LOCATION = "localhost";

  /**
   * The name of the database to connect to.
   */
  public static final String DATABASE_NAME = "Rcubed";

  /**
   * Handle to the database connection.
   */
  private MongoClient mongoClient;

  /**
   * Handle to the actual database.
   */
  private MongoDatabase database;

  /**
   * Data Access object for handling photos.
   */
  PhotoDAO daoPhoto;

  /**
   * Data Access object for handling users.
   */
  UserDAO daoUser;

  /**
   * A simple system for ensuring that the input username contains only ascii alphanumeric characters.
   */
  private static final String STRING_VALIDATOR = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";

  /**
   * Arbitrary restriction on the username field.
   */
  private static final int MINIMUM_USERNAME_LENGTH = 6;

  /**
   * Constructor
   */
  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = false, ui = RcubedUI.class)
  public static class Servlet extends VaadinServlet
  {

  }

  @Override
  protected void init(VaadinRequest request)
  {
    // initResources();

    // Create a new instance of the app navigator (which attaches itself to this View).
    new Navigator(this, this);

    getNavigator().addView("", LoginView.class);
    // Add the login view to the navigator.
    getNavigator().addView(LoginView.NAME, LoginView.class);

    // Add the album view to the navigator.
    getNavigator().addView(AlbumView.NAME, AlbumView.class);

    // Set up a ViewChange handler to ensure the user is always redirected to the login page if he/she is not logged in.
    getNavigator().addViewChangeListener(new ViewChangeListener()
    {
      @Override
      public boolean beforeViewChange(ViewChangeEvent event)
      {
        // Check if a user has logged in
        boolean isLoggedIn = getSession().getAttribute("user") != null;
        boolean isLoginView = event.getNewView() instanceof LoginView;

        if (!isLoggedIn && !isLoginView)
        {
          // Redirect to login view always if a user has not yet
          // logged in
          getNavigator().navigateTo(LoginView.NAME);
          return false;
        }
        else if (isLoggedIn && isLoginView)
        {
          // If someone tries to access the login view while logged in,
          // then cancel
          return false;
        }

        return true;
      }

      @Override
      public void afterViewChange(ViewChangeEvent event)
      {
        // Nothing to do here.
      }
    });
  }

  private void initResources()
  {
    // Initialise a connection to the database.
    // @@@ JAS - MongoDb docs suggest I should only have one instance of MongoClient for the entire application
    // As far As I can tell though, the init() method of this Vaadin UI is called once per visitor to the page.
    mongoClient = new MongoClient("localhost");
    database = mongoClient.getDatabase("Rcubed");
    daoPhoto = new MongoDbDAOPhoto(database, mongoClient.getDB("Rcubed"));
    daoUser = new MongoDbDAOUser(database);

    // Store the DAOs in the session so we can use them in other views.
    getSession().setAttribute("userDAO", daoUser);
    getSession().setAttribute("photoDAO", daoPhoto);
  }

  /**
   * Check that the input string contains only allowed characters (as described by the validator) 
   * 
   * Not a particularly efficient or complete algorithm, but for the test application it suffices as a placeholder
   * for input sanitisation.
   * 
   * @param aString     The string to check.
   * @param validator   The sequence of valid characters.
   * @return False if the input string was found to contain a character not present in the validator sequence, otherwise True. 
   */
  private boolean validateValue(String aString, String validator, int minLength)
  {
    // Default return value to true.
    boolean isValid = true;

    if (aString.length() < minLength)
    {
      // Arbitrary rule for string length.
      // primarily added to cover the empty string case, but it's the caller's responsibility to set this value to at
      // least 1.
      isValid = false;
    }
    else
    {
      // Loop over the input string one character at a time. Checking that it matches one of our valid characters.
      // If it doesn't match then the input string is invalid.
      for (int ii = 0; ii < aString.length(); ii++)
      {
        char testChar = aString.charAt(ii);

        // Is this a valid character?
        if (!validator.contains(Character.toString(testChar)))
        {
          // Found an invalid character.
          isValid = false;

          // No point in going further now we've found one invalid character.
          break;
        }
      }
    }

    return isValid;
  }

  // @Override
  protected void destroy()
  {
    mongoClient.close();
  }
}
/*
 * boolean isValid = validateValue(username, STRING_VALIDATOR, MINIMUM_USERNAME_LENGTH);
 * 
 * if (isValid) { // Lookup this user in the database. User user = daoUser.getUser(daoPhoto, username);
 * 
 * if (user == null) { // No user with his login already. Redirect to album page. } else { // Send an error response
 * indicating username has already been taken. } } else { // Send an error response indicating the username can contain
 * only alphanumeric characters }
 */