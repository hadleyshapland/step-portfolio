package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for creating new comments AND listing comments. */
@WebServlet("/comments")
public class CommentServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    String name = getParameter(request, "user-name", "Anonymous");
    String text = getParameter(request, "user-text", "[blank]");
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("text", text);
    commentEntity.setProperty("timestamp", timestamp);

    writeToDatabase(commentEntity);

    response.sendRedirect("/index.html#comments");
  }

  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    
    if (value.equals("")) {
      return defaultValue;
    }
    return value;
  }

  private void writeToDatabase(Entity toWrite) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(toWrite);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    List<Comment> comments = getComments();

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  private List<Comment> getComments() {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = getFromDatabase(query);

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String name = (String) entity.getProperty("name");
      String text = (String) entity.getProperty("text");
      long timestamp = (long) entity.getProperty("timestamp");

      Comment commentFinal = new Comment(id, name, timestamp, text);
      comments.add(commentFinal);
    }

    return comments;
  }

  private PreparedQuery getFromDatabase(Query query) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    return results;
  }
}