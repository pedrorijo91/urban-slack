package controllers

import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, _}
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/*
{
  "tags":["what","wut","wtf","wats","lol","fuck","the","you","huh","u"],
  "result_type":"exact", # no_results
  "list":[
    {
      "definition":"The only proper response to something that makes absolutely no sense.",
      "permalink":"http://wat.urbanup.com/3322419",
      "thumbs_up":3159,
      "author":"watwat",
      "word":"wat",
      "defid":3322419,
      "current_vote":"",
      "example":"1: If all the animals on the equator were capable of flattery, Halloween and Easter would fall on the same day.\r\n2: wat\r\n\r\n1: Wow your cock is almost as big as my dad's.\r\n2: wat\r\n\r\n1: I accidentially a whole coke bottle\r\n2: You accidentially what?\r\n1: A whole coke bottle\r\n2: wat",
      "thumbs_down":370
    }
  ],
  "sounds":["http://media.urbandictionary.com/sound/wat-8530.mp3","http://media.urbandictionary.com/sound/wat-8531.mp3"]
 }
 */
case class UrbanResponse(resultType: String, list: List[UrbanTerm])

object UrbanResponse {

  implicit val userReads: Reads[UrbanResponse] = (
    (__ \ "result_type").read[String] and
      (__ \ "list").read[List[UrbanTerm]]
    ) (UrbanResponse.apply _)
}

case class UrbanTerm(definition: String, permalink: String, thumbsUp: Int, thumbsDown: Int, author: String, example: String)

object UrbanTerm {
 // implicit val fmt: Format[UrbanTerm] = Json.format[UrbanTerm]

  implicit val userReads: Reads[UrbanTerm] = (
    (__ \ "definition").read[String] and
    (__ \ "permalink").read[String] and
    (__ \ "thumbs_up").read[Int] and
    (__ \ "thumbs_down").read[Int] and
    (__ \ "author").read[String] and
    (__ \ "example").read[String]
    ) (UrbanTerm.apply _)
}

object Application extends Controller {

  def index = Action {

    val term="wat"
    val url = s"https://mashape-community-urban-dictionary.p.mashape.com/define?term=$term"
    val mashapeKey = "zVGRvBkMxvmshNSgfXLth7VqUejlp1TdkV5jsnBvqfAiVw75Yu"

    val result: WSResponse = Await.result(WS.url(url).withHeaders(("X-Mashape-Key", mashapeKey)).get(), 2 seconds) //TODO 2 seconds

    val valid: JsResult[UrbanResponse] = result.json.validate[UrbanResponse]

    println(s"VALIDATE: ${valid}")

    Ok(views.html.index(valid))
  }


}