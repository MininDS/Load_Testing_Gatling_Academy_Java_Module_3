// Created gatlingdemostore with classes for load testing of Gatling-demo-store-api
package gatlingdemostoreapi;


//Imported additional libs for code execution
import java.time.Duration;
import java.util.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;


//Imported additional libs for code execution
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;


//Created class DemostoreApiSimulation with variables and scenarios for simulations
public class DemostoreApiSimulation extends Simulation {

    //Created HttpProtocolBuilder for http protocol, address and headers
  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("https://demostore.gatling.io")
    .header("Cache-Control", "no-cache")
    .contentTypeHeader("application/json");



    //Announced several variables USER_COUNT, RAMP_DURATION and TEST_DURATION used in test-scenarios by appropriate way
  private static final int USER_COUNT = Integer.parseInt(System.getProperty("USERS", "5"));

  private static final Duration RAMP_DURATION =
          Duration.ofSeconds(Integer.parseInt(System.getProperty("RAMP_DURATION", "10")));

  private static final Duration TEST_DURATION =
          Duration.ofSeconds(Integer.parseInt(System.getProperty("DURATION", "60")));


  //Added lines with appropriate text in system test execution log
  @Override
  public void before() {
      System.out.printf("Running test with %d users%n", USER_COUNT);
      System.out.printf("Ramping users over %d seconds%n", RAMP_DURATION.getSeconds());
      System.out.printf("Total test duration: %d seconds%n ", TEST_DURATION.getSeconds());
  }

  //Show text "Stress test completed" after test-execution
  @Override
  public void after() {
      System.out.println("Stress test completed");
  }



  //Created initSession variable with authentication property for different user actions
  private static ChainBuilder initSession = exec(session -> session.set("authenticated", false));



  //Created class UserJourneys which described different ways of users' behavior in the system
  private static class UserJourneys {

      //Announced some variables with pause intervals of its duration in ms and seconds
      private static Duration minPause = Duration.ofMillis(200);
      private static Duration maxPause = Duration.ofSeconds(3);

      //Created class-chainbuilder which described admin actions in the system
      private static ChainBuilder admin =
                exec(initSession)

                .exec(Categories.list)
                .pause(minPause, maxPause)

                .exec(Products.list)
                .pause(minPause, maxPause)

                .exec(Products.get)
                .pause(minPause, maxPause)

                .exec(Products.update)
                .pause(minPause, maxPause)

                .repeat(3).on(exec(Products.create))
                .pause(2)

                .exec(Categories.update);


      //Created class-chainbuilder which described user-priceScrapper behavior
      private static ChainBuilder priceScrapper =
              exec(Categories.list)
              .pause(minPause, maxPause)
              .exec(Products.listAll);


      //Created class-chainbuilder which described priceUpdater behavior
      private static ChainBuilder priceUpdater =
              exec(initSession)
                      .exec(Products.listAll)
                      .pause(minPause, maxPause)

                      .repeat("#{allProducts.size()}", "productIndex").on(
                              exec(session -> {
                                          int index = session.getInt("productIndex");
                                          List<Object> allProducts = session.getList("allProducts");
                                          return session.set("product", allProducts.get(index));
                                      })
                      .exec(Products.update)
                      .pause(minPause, maxPause));

  }


  //Created class Scenarios with scenarios for load-testing
  private static class Scenarios {
      //Created defaultscn with admin, pricescrapper and priceupdater actions during appropriate timings in scenario - 20-40-40 percents
      public static ScenarioBuilder defaultScn = scenario("Default load test")
              .during(TEST_DURATION)
              .on(
                      randomSwitch().on(
                              Choice.withWeight(20d, exec(UserJourneys.admin)),
                              Choice.withWeight(40d, exec(UserJourneys.priceScrapper)),
                              Choice.withWeight(40d, exec(UserJourneys.priceUpdater))
                      )
              );

      //Created noAdminsScn with pricescrapper and priceupdater actions during appropriate timings in scenario - 60-40 percents
      public static ScenarioBuilder noAdminsScn = scenario("Load test without admin users")
              .during(Duration.ofSeconds(60))
              .on(
                      randomSwitch().on(
                              Choice.withWeight(60d, exec(UserJourneys.priceScrapper)),
                              Choice.withWeight(40d, exec(UserJourneys.priceUpdater))
                      )
              );
  }






  //debug with 1 user
  //{
	  //setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  //}


    //Open model with several users
    //{
        //setUp(
                //scn.injectOpen(
                        //atOnceUsers(3),
                        //nothingFor(Duration.ofSeconds(5)),
                        //rampUsers(10).during(Duration.ofSeconds(20)),
                        //nothingFor(Duration.ofSeconds(10)),
                        //constantUsersPerSec(1).during(Duration.ofSeconds(20))))
                        //.protocols(httpProtocol);

    //}


    //Closed model with several users
    //{
        //setUp(
                //scn.injectClosed(
                        //constantConcurrentUsers(5).during(Duration.ofSeconds(20)),
                        //rampConcurrentUsers(1). to(5).during(Duration.ofSeconds(20))))
                        //.protocols(httpProtocol);

    //}


    //Throttle simulation
    //{
        //setUp(
                //Scenarios.defaultScn.injectOpen(
                                //constantUsersPerSec(2).during(Duration.ofMinutes(3)))
                        //.protocols(httpProtocol)
                        //.throttle(
                                //reachRps(10).in(Duration.ofSeconds(30)),
                                //holdFor(Duration.ofSeconds(60)),
                                //jumpToRps(20),
                                //holdFor(Duration.ofSeconds(60))))
                //.maxDuration(Duration.ofMinutes(3));
    //}


    //debug with 5 users
    //{
        //setUp(
                //Scenarios.defaultScn
                        //.injectOpen(rampUsers(USER_COUNT).during(RAMP_DURATION))
                        //.protocols(httpProtocol));

    //}

    //Sequence of scenarios
    //{
        //setUp(
                //Scenarios.defaultScn
                        //.injectOpen(rampUsers(USER_COUNT).during(RAMP_DURATION)).protocols(httpProtocol)
                        //.andThen(
                                //Scenarios.noAdminsScn
                                        //.injectOpen(rampUsers(5).during(Duration.ofSeconds(10))).protocols(httpProtocol)));
    //}

    //Parallel scenarios
    {
        setUp(
                Scenarios.defaultScn.injectOpen(rampUsers(USER_COUNT).during(RAMP_DURATION)),
                Scenarios.noAdminsScn.injectOpen(rampUsers(5).during(Duration.ofSeconds(30))))
                .protocols(httpProtocol);
    }




}
