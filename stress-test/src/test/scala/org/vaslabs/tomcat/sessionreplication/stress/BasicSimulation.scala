package org.vaslabs.tomcat.sessionreplication.stress

import io.gatling.core.Predef._ // 2
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.core.session.Expression

class BasicSimulation extends Simulation{

  val keyValues = {
    val testData = List(
      Map("fieldValue" -> "message", "valueObject" -> "lolix"),
      Map("fieldValue" -> "food", "valueObject" -> "souvlakia"),
      Map("fieldValue" -> "drink", "valueObject" -> "frape"),
      Map("fieldValue" -> "breakfast", "valueObject" -> "halloomi me afka"),
    )
    Iterator.continually(
      testData((Math.random()*4).toInt)
    )
  }

  val request = """
       {
 	      "field": "$$fieldValue$$", "value": "$$valueObject$$"
       }
     """

  private[this] lazy val requestPayload = request


  private[this] def templateRequest(
       replace: Expression[(String, String)]) = {
    StringBody {
      session =>
        (replace(session)).map {
          case (f, v) => requestPayload.replace(f"$$fieldValue$$", f)
            .replace(f"$$valueObject$$", v)
        }
    }
  }

  val httpConf = http
    .baseURL("http://localhost:8080")
    .acceptHeader("application/json")
    .userAgentHeader("vaslabs/stressTest")

  protected def sessionInjectRequest = {
    http("sessionspitter")
      .post("/sessionspitterservlet/SessionSpitter")
      .header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
      .body(templateRequest((f"$$fieldValue", f"$$valueObject")))
      .check(status is 200)
  }

  val rampUpSearchLimit = repeat(10) {
    feed(keyValues).exec(sessionInjectRequest).pause(500 milliseconds)
  }

  val rampUpUsers = scenario("Ramp up concurrent users")
    .exec(rampUpSearchLimit)
    .inject(rampUsersPerSec(1).to(5).during(60 seconds))

  val concurrentUsers = scenario("Users at once")
    .exec(pause(1 seconds).exec(rampUpSearchLimit))
    .inject(atOnceUsers(5))

  setUp(
    rampUpUsers
  ).protocols(httpConf)

}
