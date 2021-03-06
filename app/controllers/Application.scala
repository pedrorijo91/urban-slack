package controllers

import java.net.URLEncoder
import models.{UrbanResponse, UrbanResponseResultType}
import org.apache.commons.lang3.StringEscapeUtils
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Application extends Controller {

  def index = Action {
    Ok("urbanSlackPlay")
  }

  def urban = Action {
    request =>

      request.body.asFormUrlEncoded.fold {
        Logger.error(s"Problem on Slack request, no params were found on request: $request")
        BadRequest("Problem on Slack request")
      } {
        keyValueMap =>

          val terms = keyValueMap.getOrElse("text", Seq.empty) //FIXME empty

          val term = terms.mkString(" ")
          val encodedTerm = URLEncoder.encode(terms.mkString(" "), "UTF-8")

          val url = s"https://mashape-community-urban-dictionary.p.mashape.com/define?term=$encodedTerm"
          val mashapeKey = getMashapeKey

          Try {
            val result: WSResponse = Await.result(WS.url(url).withHeaders(("X-Mashape-Key", mashapeKey.getOrElse(""))).get(), 4 seconds)

            val response = result.json.validate[UrbanResponse].asEither

            response match {
              case Left(e) =>
                Logger.error(s"Problem validating API response: $e")
                InternalServerError("A error was found. Please try again")
              case Right(urbanResponse) if urbanResponse.resultType == UrbanResponseResultType.Unknown =>
                Logger.error(s"Unexpected resultType for $term")
                InternalServerError("Unexpected response returned. Please try again")
              case Right(urbanResponse) if urbanResponse.resultType == UrbanResponseResultType.NoResults =>
                Logger.info(s"No results: ${urbanResponse.definitions}")
                NotFound(s"No definitions were found to '$term' on Urban Dictionary")
              case Right(urbanResponse) =>
                Logger.info(s"Success response: ${urbanResponse.resultType} => ${urbanResponse.definitions.size}")
                createJsonResponse(term, urbanResponse)
            }
          } match {
            case Failure(ex) =>
              Logger.error(s"Exception on API request: $ex")
              RequestTimeout("Some network problem was found. Please try again")
            case Success(r) =>
              r
          }
      }
  }

  private def createJsonResponse(term: String, urbanResponse: UrbanResponse): Result = {

    urbanResponse.definitions.headOption.map {
      definition =>

        val json =
          s"""
             |{
             |    "response_type": "in_channel",
             |    "attachments": [
             |        {
             |            "fallback": "Urban dictionary for $term",
             |            "title": "$term",
             |            "title_link": "${definition.permalink}",
             |            "text":"${StringEscapeUtils.escapeJava(definition.definition)}",
             |            "fields": [
             |                {
             |                    "title": "Thumbs",
             |                    "value": "${definition.thumbsUp} Up / ${definition.thumbsDown} Down",
             |                    "short": true
             |                },
             |                {
             |                    "title": "Author",
             |                    "value": "${definition.author}",
             |                    "short": true
             |                }
             |            ]
             |        }
             |    ]
             |}
        """.stripMargin

        Ok(Json.parse(json)).withHeaders(("content-type", "application/json"))
    }.getOrElse(NotFound("Not found on Urban Dictionary"))
  }

}