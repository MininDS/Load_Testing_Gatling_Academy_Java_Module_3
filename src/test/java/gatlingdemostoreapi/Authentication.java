//Created class Authentication in gatlingdempstoreapi package
package gatlingdemostoreapi;


//Imported some needed Java-libs for code-execution
import io.gatling.javaapi.core.ChainBuilder;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;



//Created class Authentication for user login in api-system
public class Authentication {
    public static ChainBuilder authenticate =
            //If user is not authenticated do authentication method with checks and save token as jwt variable
            doIf(session -> !session.getBoolean("authenticated")).then(
                    exec(http("Authenticate")
                            .post("/api/authenticate")
                            .body(StringBody("{\"username\": \"admin\", \"password\": \"admin\"}"))
                            .check(status().is(200))
                            .check(jmesPath("token").saveAs("jwt")))
                            .exec(session -> session.set("authenticated", true)));
}
