package controllers

import java.net.URLEncoder
import models.{UrbanResponse, UrbanResponseResultType}
import play.api.Logger
import play.api.Play.current
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
          val mashapeKey = "zVGRvBkMxvmshNSgfXLth7VqUejlp1TdkV5jsnBvqfAiVw75Yu" // TODO config

          Try {
            val result: WSResponse = Await.result(WS.url(url).withHeaders(("X-Mashape-Key", mashapeKey)).get(), 4 seconds)

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
                val message = createResponse(term, urbanResponse)
                Ok(message)
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

  private def createResponse(term: String, urbanResponse: UrbanResponse): String = {

    val message = urbanResponse.definitions.headOption.map {
      definition =>
        s"""${definition.definition} - by ${definition.author} (${definition.thumbsUp} up / ${definition.thumbsDown} down). More at ${definition.permalink}"""
    }.getOrElse("Not found on Urban Dictionary")

    s"'$term': $message"
  }


  def prettyUrban = Action {
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
          val mashapeKey = "zVGRvBkMxvmshNSgfXLth7VqUejlp1TdkV5jsnBvqfAiVw75Yu" // TODO config

          Try {
            val result: WSResponse = Await.result(WS.url(url).withHeaders(("X-Mashape-Key", mashapeKey)).get(), 4 seconds)

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
                val message = createResponse(term, urbanResponse)
                Ok(message)
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

}