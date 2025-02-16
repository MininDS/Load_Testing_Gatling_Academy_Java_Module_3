//Created class Categories in gatlingdempstoreapi package
package gatlingdemostoreapi;


//Imported some needed Java-libs for code-execution
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;



//Created class Products with feeders and methods for working with Products api
public class Products {

    //Created feeder with products list in data/products.csv
    public static FeederBuilder.Batchable<String> productsFeeder =
            csv("data/products.csv").circular();


    //Created chainbuilder class with listall method and its checks
    public static ChainBuilder listAll =
            exec(http("List all products")
                    .get("/api/product")
                    .check(jmesPath("[*]").ofList().saveAs("allProducts")));


    //Created chainbuilder class with method list and its checks
    public static ChainBuilder list =
            exec(http("List products")
                    .get("/api/product?category=7")
                    .check(jmesPath("[? categoryId != '7']").ofList().is(Collections.emptyList()))
                    .check(jmesPath("[*].id").ofList().saveAs("allProductIds")));


    //Created chainbuilder-class with method get - get product by its id
    public static ChainBuilder get =
            exec(session -> {
                List<Integer> allProductIds = session.getList("allProductIds");
                return session.set("productId", allProductIds.get(new Random().nextInt(allProductIds.size())));
            })
                    //.exec(
                    //session -> {
                    //System.out.println("allProductIds captured:" + session.get("allProductIds").toString());
                    //System.out.println("productId selected:" + session.get("productId").toString());
                    //return session;
                    //}
                    //)

                    .exec(http("Get product")
                            .get("/api/product/#{productId}")
                            .check(jmesPath("id").ofInt().isEL("#{productId}"))
                            .check(jmesPath("@").ofMap().saveAs("product")));
    //.exec(
    //session -> {
    //System.out.println("value of product:" + session.get("product").toString());
    //return session;
    //}
    //);


    //Created chainbuilder class with method update - put needed new product after authentication
    public static ChainBuilder update =
            exec(Authentication.authenticate)

                    .exec(session -> {
                        Map<String, Object> product = session.getMap("product");
                        return session
                                .set("productCategoryId", product.get("categoryId"))
                                .set("productName", product.get("name"))
                                .set("productDescription", product.get("description"))
                                .set("productImage", product.get("image"))
                                .set("productPrice", product.get("price"))
                                .set("productId", product.get("id"));
                    })

                    .exec(http("Update product #{productName}")
                            .put("/api/product/#{productId}")
                            .headers(Headers.authorizationHeaders)
                            .body(ElFileBody("gatlingdemostoreapi/demostoreapisimulation/create-product.json"))
                            .check(jmesPath("price").isEL("#{productPrice}")));


    //Created chainbuilder class with method create - post needed new product from feeder after authentication
    public static ChainBuilder create =
            exec(Authentication.authenticate)

                    .feed(productsFeeder)
                    .exec(http("Create product #{productName}")
                            .post("/api/product")
                            .headers(Headers.authorizationHeaders)
                            .body(ElFileBody("gatlingdemostoreapi/demostoreapisimulation/create-product.json")));
}