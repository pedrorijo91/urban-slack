package models

import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, _}
import scala.language.postfixOps
import scala.util.Try

case class UrbanResponse(resultType: UrbanResponseResultType.Value, definitions: List[UrbanTerm])

object UrbanResponse {

  implicit val userReads: Reads[UrbanResponse] = (
    (__ \ "result_type").read[String].map(UrbanResponseResultType.findByName) and
      (__ \ "list").read[List[UrbanTerm]]
    ) (UrbanResponse.apply _)
}

object UrbanResponseResultType extends Enumeration {
  val Exact = Value("exact")
  val NoResults = Value("no_results")
  val Unknown = Value("unknown")

  def findByName(name: String): UrbanResponseResultType.Value = {
    Try(UrbanResponseResultType.withName(name)).getOrElse {
      Logger.warn(s"Found unexpected UrbanDictionary API result: $name")
      Unknown
    }
  }
}

case class UrbanTerm(definition: String, permalink: String, thumbsUp: Int, thumbsDown: Int, author: String, example: String)

object UrbanTerm {

  implicit val userReads: Reads[UrbanTerm] = (
    (__ \ "definition").read[String] and
      (__ \ "permalink").read[String] and
      (__ \ "thumbs_up").read[Int] and
      (__ \ "thumbs_down").read[Int] and
      (__ \ "author").read[String] and
      (__ \ "example").read[String]
    ) (UrbanTerm.apply _)
}
