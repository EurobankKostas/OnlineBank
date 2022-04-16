package controllers;

import akka.Done;
import models.Project;

import models.Query;
import models.SearchQueryStats;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletionStage;
import play.data.DynamicForm;
import play.data.FormFactory;
import services.IApiService;
import play.cache.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final IApiService service;

    private AsyncCacheApi cache;
    public String searchKey = "";

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    LinkedHashMap<String, List<Project>> search_list = new LinkedHashMap<>();
    @Inject
    public HomeController(AsyncCacheApi cache,IApiService service) {
        this.service = service;
        this.cache =cache;
    }
    String x = Integer.toString(((int)(Math.random() * 100000)) % 1000);



    public CompletionStage<Result> index() {

        List<Query> queries = new ArrayList<>();
        queries.add(new Query("query", "\""+searchKey+"\""));
        queries.add(new Query("job_details", "true"));
        queries.add(new Query("full_description", "true"));
        queries.add(new Query("limit", "10"));

        return service.getProjects(queries, "/active").thenApply(projects -> {
            if (!searchKey.isEmpty()) {
                search_list.put(searchKey, projects);
            }
            return ok(views.html.freelancelot.render(reverseMap(search_list)));
        });
    }

    @Inject FormFactory formFactory;
    public Result captureSearchKeyword(Http.Request request) {
        DynamicForm dynamicForm = formFactory.form().bindFromRequest(request);
        searchKey = dynamicForm.get("search");
        return redirect(routes.HomeController.index());
    }



    public static <T,Q> LinkedHashMap<T,Q> reverseMap(LinkedHashMap<T,Q> toReverse){
        LinkedHashMap<T,Q> reverseMap = new LinkedHashMap<>();
        List<T> reverseOrderKeys = new ArrayList<>(toReverse.keySet());
        Collections.reverse(reverseOrderKeys);
        reverseOrderKeys.forEach((key) -> reverseMap.put(key, toReverse.get(key)));
        return reverseMap;
    }

    public CompletionStage<Result> skills(String skillId) {
        List<Query> queries = new ArrayList<>();
        queries.add(new Query("job_details", "true"));
        queries.add(new Query("jobs[]", skillId));
        queries.add(new Query("full_description", "true"));
        queries.add(new Query("limit", "10"));

        return service.getProjects(queries, "/active").thenApply(projects -> ok(views.html.skill.render(skillId,projects)));
    }


    public CompletionStage<Result> ownerIDSearch(String ownerId) {
        return service.getOwnerResult(ownerId).thenApply(ownerResult -> {
            var user = ownerResult.getUsers().get(ownerId);
            var projects = ownerResult.getProjects();

            return ok(views.html.owner.render(projects, user));
        });
    }
    public CompletionStage<Result> getGlobalStat() {

        List<Query> queries = new ArrayList<>();
        queries.add(new Query("query", searchKey));
        queries.add(new Query("limit", "250"));
        queries.add(new Query("job_details", "true"));
        queries.add(new Query("full_description", "true"));
        LinkedHashMap<String, List<Project>> projList = new LinkedHashMap<>();

        return service.getProjects(queries, "/active").thenApply(projects -> {
            String stats = "";

            System.out.println("here");
            projList.put(searchKey, projects);
            SearchQueryStats queryStat = new SearchQueryStats(searchKey, projList);

            stats = SearchQueryStats.combineOutput(queryStat.computeWordLevelStat());

            return ok(views.html.globalstats.render(stats));
        });
    }

    public CompletionStage<Result> getProjectIDStat(String projId) {
        List<Query> queries = new ArrayList<>();

        // queries.add(new Query("query", searchKey));
        queries.add(new Query("full_description", "true"));
        queries.add(new Query("job_details", "true"));

        return service.getIDProjects(queries, "/"+projId).thenApply(
                project ->  {
                    try {
                        var out = SearchQueryStats.combineOutput(SearchQueryStats.computeWordLevelStatByProject(project.description));
                        return ok(views.html.projectIDStat.render(out));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return badRequest();
                    }
                });
    }
}

