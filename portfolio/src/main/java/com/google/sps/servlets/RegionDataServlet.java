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

  // database instance
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Map<String, Long> regionVotes = getVotes();

    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(regionVotes);
    response.getWriter().println(json);
  }

  @Override
  public synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String region = request.getParameter("region");

    Entity regionEntity = new Entity("Region", region);
    regionEntity.setProperty("name", region);

    Map<String, Long> currentVotes = getVotes();

    if (currentVotes.containsKey(region)) {
      // increment vote
      Long preVote = currentVotes.get(region);
      regionEntity.setProperty("votes", preVote + 1);
    } else {
      regionEntity.setProperty("votes", 1);
    }

    writeToDatabase(regionEntity);
    response.sendRedirect("/index.html#forfun");
  }

  private void writeToDatabase(Entity toWrite) {
    datastore.put(toWrite);
  }

  private Map<String, Long> getVotes() {
    Query query = new Query("Region");
    PreparedQuery results = getFromDatabase(query);

    Map<String, Long> toReturn = new HashMap<String, Long>();

    for (Entity entity : results.asIterable()) {
      toReturn.put((String) entity.getProperty("name"), (Long) entity.getProperty("votes"));
    }

    return toReturn;
  }

  private PreparedQuery getFromDatabase(Query query) {
    return datastore.prepare(query);
  }
}
