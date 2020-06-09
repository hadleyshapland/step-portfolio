package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/region-data")
public class RegionDataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Map<String, Integer> regionVotes = getVotes();

    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(regionVotes);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String region = request.getParameter("region");

    Entity voteEntity = new Entity("Vote");
    voteEntity.setProperty("name", region);

    writeToDatabase(voteEntity);

    response.sendRedirect("/index.html#forfun");
  }

  private void writeToDatabase(Entity toWrite) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(toWrite);
  }

  private Map<String, Integer> getVotes() {
    Query query = new Query("Vote");
    PreparedQuery results = getFromDatabase(query);

    Map<String, Integer> toReturn = new HashMap<String, Integer>();

    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty("name");

      if (toReturn.containsKey(name)) {
        Integer currentVote = toReturn.get(name);
        toReturn.put(name, currentVote + 1);
      } else {
        toReturn.put(name, 1);
      }
    }
    return toReturn;
  }

  private PreparedQuery getFromDatabase(Query query) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    return results;
  }
}
