import play.api.Play

package object controllers {

  def getMashapeKey: Option[String] = Play.current.configuration.getString("mashape.key.urban")

}
