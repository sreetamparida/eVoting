package frontend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.post;
import static spark.Spark.get;

import static spark.Spark.staticFileLocation;

public class Home {
    static int electionid = 0;

    public static int getid(){
        return electionid++;
    }


    public static void main(String[] args) {

        staticFileLocation("/public");

//        get("/hello", (request, response) -> {
//            Map<String, Object> model = new HashMap<>();
//            model.put("hello", "Velocity World");
//            ArrayList<String> options = new ArrayList<>();
//            options.add("C1");
//            options.add("C2");
//            options.add("C3");
//            model.put("options", options);
//            // The vm files are located under the resources directory
//            return new ModelAndView(model, "hello.vm");
//        }, new VelocityTemplateEngine());

//        get("/hellosubmit", (request, response) -> {
//            String username = request.queryParams("username");
//            String option = request.queryParams("optradio");
//            Map<String, Object> model = new HashMap<>();
//            model.put("username", username);
//            model.put("option", option);
//
//            return new ModelAndView(model, "hellosubmit.vm");
//        }, new VelocityTemplateEngine());

        get("/login", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "login.vm");
        }, new VelocityTemplateEngine());

        post("/login", (request, response) -> {
            String username = request.queryParams("inputEmail");
            String password = request.queryParams("inputPassword");
            System.out.println(username +  " " + password);
            Map<String, Object> model = new HashMap<>();
            if(username.equals("abc@b.com") && password.equals("abc")){
                response.redirect("/home");
            }else {
                response.redirect("/login");
            }
            return "ok";
        });

        get("/home", (request, response) -> {
//            Map<String, Object> model = new HashMap<>();
            String path = "E:\\Sem1\\OOAD\\Project\\Data\\Election.json";

            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            Gson gson = new Gson();
            HashMap<String, ArrayList<Map>> model = gson.fromJson(bufferedReader, HashMap.class);

            return new ModelAndView(model, "home.vm");
        }, new VelocityTemplateEngine());


        post("/addelection", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String title = request.queryParams("title");
            String numvoters = request.queryParams("numvoters");
            String description = request.queryParams("desc");

            model.put("electionid", getid());
            model.put("title", request.queryParams("title"));
            model.put("numvoters", request.queryParams("numvoters"));
            model.put("description", request.queryParams("desc"));

            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            String path = "E:\\Sem1\\OOAD\\Project\\Data\\Election.json";

            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            HashMap<String, ArrayList<Map>> model1 = gson.fromJson(bufferedReader, HashMap.class);
            model1.get("elections").add(model);



            try (Writer writer = new FileWriter(path)) {
                System.out.println(model1);
                gson = new GsonBuilder().setLenient().create();
                gson.toJson(model1, writer);
            }

            response.redirect("/home");
            return "ok";
        });

        get("/addelection", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "add_election.vm");
        }, new VelocityTemplateEngine());
    }
}
