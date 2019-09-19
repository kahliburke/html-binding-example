package example
import org.lrng.binding.html
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.raw._
import scalaz.std.list._
import scalaz.std.option._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
@JSExportTopLevel("org.lrng.binding.HtmlBindingExampleApp")
object HtmlBindingExampleApp {

  @JSExport
  def main(): Unit = {
    html.render(
      org.scalajs.dom.document.getElementById("binding").asInstanceOf[Node],
      new HtmlBindingExample().render())
  }
}
