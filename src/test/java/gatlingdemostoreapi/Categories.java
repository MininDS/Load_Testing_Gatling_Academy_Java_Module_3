//Created class Categories in gatlingdempstoreapi package
package gatlingdemostoreapi;

//Imported some needed Java-libs for code-execution
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import java.util.List;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.http.HttpDsl.http;



//Created class Categories with methods for working with categories api
public class Categories {

    //Created feeder categoriesFeeder which takes categories names from categories.csv
    public static FeederBuilder.Batchable<String> categoriesFeeder =
            csv("data/categories.csv").random();

    //Created class-chainbuilder which can get list of all categories and check one of them - id == 6 by name
    public static ChainBuilder list =
            exec(http("List categories")
                    .get("/api/category")
                    .check(jmesPath("[? id == `6`].name").ofList().is(List.of("For Her"))));

    //Created class-chainbuilder update which can authenticate user and create new category by file categories.csv, check name of category is created
    public static ChainBuilder update =
            feed(categoriesFeeder)

                    .exec(Authentication.authenticate)

                    .exec(http("Update category")
                            .put("/api/category/#{categoryId}")
                            .headers(Headers.authorizationHeaders)
                            .body(StringBody("{\"name\": \"#{categoryName}\"}"))
                            .check(jmesPath("name").isEL("#{categoryName}")));
}
