package com.example.rcubed;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

/**
 * Entry point to the Web App.
 *
 * Redirects user to the login view.
 */
@SuppressWarnings("serial")
@Theme("rcubed")
public class RcubedUI extends UI
{
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
    // Create a new instance of the app navigator (which attaches itself to this View).
    new Navigator(this, this);

    // Vaadin directs new visitors to the site to the view identified with an empty string (Because the visitor will
    // have landed here using "http://<IP address>/rcubed/". Direct access to a view normally contains it's name (ie
    // rcubed/!#login). We add the login view as both the default empty string and under the login id.
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
}