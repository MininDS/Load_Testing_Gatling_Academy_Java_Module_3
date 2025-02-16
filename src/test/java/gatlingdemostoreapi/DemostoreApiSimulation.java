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



//Created several setUp-methods with different types of user-test-executions with appropriate scenarios


  //***Basic simulation for script-debugging - executes Scenarios.defaultScn with Open model of simulation - injects only one user in the system, using http-protocol
  {
	  setUp(Scenarios.defaultScn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }


    //***Regular Simulation -  executes Scenarios.defaultScn with Open model using http-protocol
    //{
        //setUp(
                //Scenarios.defaultScn.injectOpen(
                        //Inject 3 users at start one time
                        //atOnceUsers(3),
                        //Get pause for 5 seconds
                        //nothingFor(Duration.ofSeconds(5)),
                        //Sequentially inject up to 10 users and hold them on 20 seconds then decrease sequentially
                        //rampUsers(10).during(Duration.ofSeconds(20)),
                        //Get pause for 10 seconds
                        //nothingFor(Duration.ofSeconds(10)),
                        //Inject 1 user per second sequentially, hold them during 20 seconds then decrease count of users sequentially
                        //constantUsersPerSec(1).during(Duration.ofSeconds(20))))
                //.protocols(httpProtocol);

    //}


    //***Closed model simulation - executes Scenarios.noAdminsScn with Closed model using http-protocol
    //{
        //setUp(
                //Scenarios.noAdminsScn.injectClosed(
                        //Inject and hold 5 users (concurrent with another 5 users) in the system during 20 seconds
                        //constantConcurrentUsers(5).during(Duration.ofSeconds(20)),
                        //Inject 1 user in the system and increase it to 5 users during 20 seconds
                        //rampConcurrentUsers(1). to(5).during(Duration.ofSeconds(20))))
                //.protocols(httpProtocol);

    //}


    //***Throttle simulation - executes ScenarioBuilder-class named "scenario" with Open model using http-protocol with 1 user per second during 3 minutes and throttling
    //{
        //setUp(
                //Inject 2 users per second sequentially, hold them during 3 minutes then decrease count of users sequentially
                //Scenarios.defaultScn.injectOpen(
                        //constantUsersPerSec(2).during(Duration.ofMinutes(3)))
                        //.protocols(httpProtocol)
                        //.throttle(
                                //Reach 10 requests per seconds in 30 second interval
                                //reachRps(10).in(Duration.ofSeconds(30)),
                                //Hold this situation for 60 seconds
                                //holdFor(Duration.ofSeconds(60)),
                                //Jump to 20 requests per second immediately
                                //jumpToRps(20),
                                //Hold this situation for 60 seconds
                                //holdFor(Duration.ofSeconds(60))))
                //Maximal duration of test simulation is 3 minutes
                //.maxDuration(Duration.ofMinutes(3));
    //}


    //***Simulation with Scenarios class and system parameters by default - execute Open model load test with defaultPurchase scenario actions
    //{
        //setUp(
                //Scenarios.defaultScn
                        //Injects USER_COUNT of users in the system sequentially, hold them during RAMP_DURATION seconds using http-protocol
                        //.injectOpen(rampUsers(USER_COUNT).during(RAMP_DURATION))
                        //.protocols(httpProtocol));
    //}


    //***Sequential executing of scenarios-class - first is defaultScn, second - noAdminsScn after it
    //{
        //setUp(
                //Execute Scenarios.defaultScn first
                //Scenarios.defaultScn
                        //Inject USER_COUNT users sequentially and hold them on RAMP_DURATION seconds using http-protocol
                        //.injectOpen(rampUsers(USER_COUNT).during(RAMP_DURATION)).protocols(httpProtocol)
                        //Then execute Scenarios.noAdminsScn
                        //.andThen(
                                //Scenarios.noAdminsScn
                                        //Inject users sequentially to 5, hold 5 users of 10 seconds using http-protocol
                                        //.injectOpen(rampUsers(5).during(Duration.ofSeconds(10))).protocols(httpProtocol)));
    //}


    //***Parallel executing scenarios-class - defaultScn and noAdminsScn at the same time using http-protocol
    //{
        //setUp(
                //Inject USER_COUNT users sequentially during RAMP_DURATION seconds period
                //Scenarios.defaultScn.injectOpen(rampUsers(USER_COUNT).during(RAMP_DURATION)),
                //Inject 5 users during 30 seconds period sequentially
                //Scenarios.noAdminsScn.injectOpen(rampUsers(5).during(Duration.ofSeconds(30))))
                //.protocols(httpProtocol);
    //}
}