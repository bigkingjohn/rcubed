package com.example.rcubed;

import javax.servlet.annotation.WebServlet;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("rcubed")
public class RcubedUI extends UI
{

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = false, ui = RcubedUI.class)
  public static class Servlet extends VaadinServlet
  {
    MongoClient mongoClient = new MongoClient("localhost");
    MongoDatabase database = mongoClient.getDatabase("Rcubed");
    MongoCollection<Document> collection = database.getCollection("Photos");
  }

  @Override
  protected void init(VaadinRequest request)
  {
    final VerticalLayout layout = new VerticalLayout();
    layout.setMargin(true);
    setContent(layout);

    Document testdoc = new Document("name", "bob");
    testdoc.append("sex", "male");

    Button button = new Button("Click Me");
    button.addClickListener(new Button.ClickListener()
    {
      public void buttonClick(ClickEvent event)
      {
        layout.addComponent(new Label("Thank you for clicking"));

        /*
         * MongoClient mongoClient = new MongoClient("localhost"); MongoDatabase
         * database = mongoClient.getDatabase("Rcubed");
         * MongoCollection<Document> collection =
         * database.getCollection("Photos"); collection.insertOne(testdoc);
         * mongoClient.close();
         */
      }
    });
    layout.addComponent(button);
  }
}