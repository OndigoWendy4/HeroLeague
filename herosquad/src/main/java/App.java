
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static spark.debug.DebugScreen.enableDebugScreen;
//import spark.debug.DebugScreen;
import dao.Sql2oSquadDao;
import dao.Sql2oHeroDao;
import models.Squad;
import models.Hero;
import org.sql2o.Sql2o;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;
import static spark.Spark.*;


public class App {
  static int getHerokuAssignedPort() {
      enableDebugScreen();
      ProcessBuilder processBuilder = new ProcessBuilder();
      if (processBuilder.environment().get("PORT") != null) {
        return Integer.parseInt(processBuilder.environment().get("PORT"));
      }
      return 4567;
    }
    public static void main(String[] args) {
        port(getHerokuAssignedPort());
        staticFileLocation("/public");

//local
       String connectionString = "jdbc:postgresql://localhost:5432/herosquad";
     Sql2o sql2o = new Sql2o(connectionString, "postgres", "wendy1");
//heroku
       //String connectionString = "jdbc:postgresql://zjaanpjcapxdwn:e4ffa8fce21be355c5a7f8d6665bb1791c5bc4df6bc1398a6e0767f8fc0f9375@ec2-50-17-255-244.compute-1.amazonaws.com:5432/ddr38cjsfgt0uq";
       //  String connectionString = "jdbc:postgresql://ec2-50-17-255-244.compute-1.amazonaws.com:5432/ddr38cjsfgt0uq";
     // Sql2o sql2o = new Sql2o(connectionString, "zjaanpjcapxdwn","e4ffa8fce21be355c5a7f8d6665bb1791c5bc4df6bc1398a6e0767f8fc0f9375");
      //  Sql2o sql2o = new Sql2o("jdbc:postgresql://ec2-50-17-255-244.compute-1.amazonaws.com:5432/ddr38cjsfgt0uq?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory", "zjaanpjcapxdwn","e4ffa8fce21be355c5a7f8d6665bb1791c5bc4df6bc1398a6e0767f8fc0f9375");
        Sql2oHeroDao heroDao = new Sql2oHeroDao(sql2o);
        Sql2oSquadDao squadDao = new Sql2oSquadDao(sql2o);

        //get: show all heroes in all squads and show all squads
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<Squad> allSquads = squadDao.getAll();
            model.put("squads", allSquads);
            List<Hero> heroes =heroDao.getAll();
            model.put("heroes", heroes);
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());


        //get: show a form to create a new squad
        get("/squads/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
         List<Squad> squads = squadDao.getAll();
         model.put("squads", squads);
            return new ModelAndView(model, "squad-form.hbs");
        }, new HandlebarsTemplateEngine());

        //post: process a form to create a new squad
        post("/squads", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String name = req.queryParams("name");
           Squad newSquad = new Squad(name);
          squadDao.add(newSquad);
            res.redirect("/");
            return null;
        }, new HandlebarsTemplateEngine());

        //Delete all squads and heroes
        get("/squads/delete", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            squadDao.clearAllSquads();
          heroDao.clearAllHeroes();
            res.redirect("/");
            return null;
        }, new HandlebarsTemplateEngine());

        //get: delete all heroes
        get("/heroes/delete", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
           heroDao.clearAllHeroes();
            res.redirect("/");
            return null;
        }, new HandlebarsTemplateEngine());

//        //get a specific squad (and the heroes it contains)
        get("/squads/:id", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfSquadToFind = Integer.parseInt(req.params("id")); //new
            Squad foundSquad= squadDao.findById(idOfSquadToFind);
            model.put("squad", foundSquad);
            List<Hero> allHeroesBySquad = squadDao.getAllHeroesBySquad(idOfSquadToFind);
            model.put("heroes", allHeroesBySquad);
            model.put("squads", squadDao.getAll());
            return new ModelAndView(model, "squad-details.hbs");
        }, new HandlebarsTemplateEngine());


        //get: show a form to update a squad
        get("/squads/:id/edit", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("editCategory", true);
            Squad squad = squadDao.findById(Integer.parseInt(req.params("id")));
            model.put("squad", squad);
            model.put("squads", squadDao.getAll());
            return new ModelAndView(model, "squad-form.hbs");
        }, new HandlebarsTemplateEngine());

        //post: process a form to update a squad
        post("/squads/:id", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfSquadToEdit = Integer.parseInt(req.params("id"));
            String newName = req.queryParams("newSquadName");
           squadDao.update(idOfSquadToEdit, newName);
            res.redirect("/");
            return null;
        }, new HandlebarsTemplateEngine());

        //get: delete an individual hero
        get("/squads/:squad_id/heroes/:hero_id/delete", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfHeroToDelete = Integer.parseInt(req.params("hero_id"));
            heroDao.deleteById(idOfHeroToDelete);
            res.redirect("/");
            return null;
        }, new HandlebarsTemplateEngine());


        //get: show new hero form
        get("/squads/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<Squad> squads = squadDao.getAll();
            model.put("squads", squads);
            return new ModelAndView(model, "squad-form.hbs");
        }, new HandlebarsTemplateEngine());


        //get: show new hero form
        get("/heroes/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<Squad> squads = squadDao.getAll();
            model.put("squads", squads);
            return new ModelAndView(model, "hero-form.hbs");
        }, new HandlebarsTemplateEngine());

        //task: process new hero form
        post("/heroes", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<Squad> allSquads = squadDao.getAll();
            model.put("squads", allSquads);
            String description = req.queryParams("description");
            int squadId = Integer.parseInt(req.queryParams("squadId"));
            Hero newHero = new Hero(description, squadId);
            heroDao.add(newHero);
            res.redirect("/");
            return null;
        }, new HandlebarsTemplateEngine());


        //get: show an individual hero that is nested in a squad
        get("/squads/:squad_id/heroes/:hero_id", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfHeroToFind = Integer.parseInt(req.params("hero_id"));
             Hero foundHero = heroDao.findById(idOfHeroToFind);
            int idOfSquadToFind = Integer.parseInt(req.params("squad_id"));
       Squad foundSquad = squadDao.findById(idOfSquadToFind);
            model.put("squad", foundSquad);
            model.put("hero", foundHero);
            model.put("squads", squadDao.getAll());
            return new ModelAndView(model, "hero-detail.hbs");
        }, new HandlebarsTemplateEngine());

        //get: show a form to update a hero
        get("/heroes/:id/edit", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<Squad> allSquads = squadDao.getAll();
            model.put("squads", allSquads);
           Hero hero = heroDao.findById(Integer.parseInt(req.params("id")));
            model.put("hero", hero);
            model.put("editHero", true);
            return new ModelAndView(model, "hero-form.hbs");
        }, new HandlebarsTemplateEngine());

        //task: process a form to update a hero
        post("/heroes/:id", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int taskToEditId = Integer.parseInt(req.params("id"));
            String newContent = req.queryParams("description");
            int newSquadId = Integer.parseInt(req.queryParams("squadId"));
           heroDao.update(taskToEditId, newContent, newSquadId);
            res.redirect("/");
            return null;
        }, new HandlebarsTemplateEngine());


    }
}
