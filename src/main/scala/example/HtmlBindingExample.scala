package example

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{BindingSeq, Constants, Var}
import com.thoughtworks.binding.bindable._
import org.lrng.binding.html
import org.lrng.binding.html.NodeBinding
import org.scalajs.dom.raw.{CustomEvent, Event, Node}

import scala.xml.{Elem, NodeBuffer}
import org.lrng.ionic.webcomponents.xml._

/**
  * Demonstrates various usage scenarios for @html binding macro.
  *
  * Note the use of the import
  * import com.thoughtworks.binding.bindable._
  *
  * which brings in various implicit functions that make it easier to use Bindings.
  *
  * @author Kahli Burke
  */
class HtmlBindingExample extends IDEHelpers {

  /**
    * The @html macro is used to transform HTML (strictly speaking XHTML) tags into Javascript code
    * that automatically updates the browser DOM elements in response to observed variables.
    *
    * @html should be used whenever there are HTML tags that should be presented to the browser.
    *
    * Although the return type of the method is Elem, the macro will transform this to a Binding[Node],
    * which is a dynamic observed function that can change in response to user input or other events.
    */
  @html def simpleBinding =
    <div>This static tag will be transformed into a Binding[Node]</div>

  /**
    * This render method combines Binding[Node] values that are declared in this class.
    * Bindings can be easily composed inside HTML tags.
    *
    * Note that calling .bind is not required inside tags, but can optionally be used: i.e. {simpleBinding.bind}.
    */
  @html def render() = {
    <div id="container">
      {webComponent}
      {simpleBinding}
      {changingBinding}
      {noHtmlMacroNecessary}
      {sequenceBinding}
      {sequenceWithMethod}
      {useConstants}
      {useList}
      {bindingApply(b)}
      {alternateToBindingApply(b)}
      {sequenceOfSequences}
      {listToSequences}
      {intersperseHtmlWithMethods}
      {sequenceWithMethodAndParam(listToSequences)}
    </div>
  }

  /**
    * Here I declare a Var, a dynamic variable that can change in response to user events.
    * As this Var changes, the state of the DOM automatically updates.
    */
  val state = Var("Click me!")

  /**
    * This function defines HTML tags which will be updated as the state of the Var 'state' changes.
    * The onclick event is defined to modify the 'value' property of the Var, which change the state. Note that
    * the '.value' is accessed in the event handler. Values should not be accessed inside .bind updated methods.
    * However since event handlers are callback functions, this is the appropriate time to change the value of a Var.
    * Vars should always be changed in response to events, whether user driven, network driven, or otherwise initiated
    * by the browser.
    *
    * Because the Var's bound value is used the <div> tag, no additional context is needed.
    */
  @html val changingBinding = {
    <div onclick={_: Event => state.value = "You clicked me!"}>
      {state.bind}
    </div>
  }

  /**
    * An example of using a web component library, see org.lrng.ionic.webcomponents.xml as an example of handling these
    * custom tags.
    */
  val menuTitle = Var("Menu Title Is Dynamic")
  val iconName = Var("star")
  val toggleValue1 = Var(false)
  val toggleValue2 = Var(false)
  @html val webComponent =
    <ion-toolbar xmlns="https://ionicframework.com/webcomponents">
      <ion-buttons data:slot="primary">
        <ion-button onclick={e: Event =>
          menuTitle.value = "Updated Menu Title!"
          iconName.value = "heart"
          toggleValue1.value = !toggleValue1.value
        }>
          <ion-icon data:slot="icon-only" data:name={iconName.bind}></ion-icon>
        </ion-button>
      </ion-buttons>
      <ion-title>{menuTitle.bind}</ion-title>
      <ion-buttons data:slot="end">
        <ion-menu-button data:auto-hide="false"></ion-menu-button>
      </ion-buttons>
      <ion-item>
        <ion-label>Toggled By Menu</ion-label>
        <ion-toggle data:slot="end" checked={toggleValue1.bind}></ion-toggle>
      </ion-item>
      <ion-item>
        <ion-label>This Toggle Value: {toggleValue2.bind.toString}</ion-label>
        <ion-toggle data:slot="end" checked={toggleValue2.bind} onIonChange={ionChangeEvent { e: IonChangeEvent =>
          toggleValue2.value = e.checked.getOrElse(false)
        }}></ion-toggle>
        <!-- Won't compile: <ion-title checked={toggleValue.bind}></ion-title>, it doesn't have a checked attribute -->
      </ion-item>
    </ion-toolbar>

  @html def stringInDiv(s: String) = <div>Your content here: {s}</div>

  /**
    * If a method or value returns a Binding[Node], no @html macro is needed.
    */
  val noHtmlMacroNecessary = stringInDiv("Don't need html here.")

  /**
    * A sequence of nodes can be returned.
    */
  @html val sequenceBinding = {
    <div>Div 1</div>
    <div>Div 2</div>
  }

  /**
    * If a sequence of nodes needs to be returned by interspersing tags and method calls, a different approach is needed.
    *
    */
  @html val sequenceWithMethod: Binding[BindingSeq[Node]] = {
    List(
      <div>Div 3</div>,
      stringInDiv("Div 4"),
      <div>Div 5</div>
    ).bindSeq
  }

  @html def sequenceWithMethodAndParam(
      content: Binding[BindingSeq[Node]]): Binding[BindingSeq[Node]] = Binding {
    {
      List(
        <div>Div 3</div>,
        stringInDiv("Div 4")
      ).bindSeq.all.bind ++
        content.bind.all.bind ++
        List(<div>Div 5</div>).bindSeq.all.bind
    }.bindSeq
  }

  @html def combineSequences(
      content: Binding[BindingSeq[Node]]): Binding[BindingSeq[Node]] = Binding {
    Constants(List(
                <div>Div 3</div>,
                stringInDiv("Div 4")
              ).bindSeq,
              content.bind,
              List(<div>Div 5</div>).bindSeq)
      .flatMap(_.bind)
  }
  @html def intDiv(i: Int) = <div>int = {i.toString}</div>

  /**
    * Constants can be used to turn lists of objects into lists of DOM elements.
    */
  val useConstants = Constants(1 to 2: _*) map intDiv

  /**
    * Or another sequence type can be used, but note that this doesn't produce a BindingSeq[Node],
    * which may be necessary depending on how an interface is defined.
    */
  val useList: List[NodeBinding[Node]] = List(1 to 2: _*) map intDiv

  @html val falseDiv = stringInDiv("False")
  val b = Var(true)

  /**
    * When a Binding context is needed but different branches return tags and values or methods, the following pattern is
    * useful.
    */
  @html def bindingApply(boolVar: Binding[Boolean]) =
    Binding apply {
      boolVar.bind match {
        case true => <div>True</div>
        case _    => falseDiv
      }
    }.bind

  /**
    * Alternatively this pattern can be used, but .bind must be called on every return value, so the prior method is preferred.
    * @param boolVar
    * @return
    */
  @html def alternateToBindingApply(boolVar: Binding[Boolean]) =
    Binding {
      boolVar.bind match {
        case true => <div>True</div>.bind
        case _    => falseDiv.bind
      }
    }

  /**
    * If a sequence of nodes needs to be returned by interspersing tags and method calls, a different approach is needed.
    */
  @html def sequence(s: String) = {
    <div>First: {s}</div>
    <div>Second: {s}</div>
  }

  /**
    * One way is by using Constants and mapping over them.
    */
  @html def sequenceOfSequences = {
    <div>
      {Constants((1 to 2): _*) flatMap { i => sequence(i.toString).bind}}
    </div>
  }

  /**
    * The new macro can also embed lists of nodes directly.
    */
  @html def intersperseHtmlWithMethods = {
    List(
      <div>A tag</div>,
      intDiv(42),
      <div>Another tag</div>
    )
  }

  /**
    * The .bindSeq method can be used to turn a collection into a BindingSeq which can then be mapped or
    * flat mapped.
    * @return
    */
  def listToSequences: Binding[BindingSeq[Node]] = {
    List(1, 2).bindSeq flatMap { i =>
      sequence(i.toString)
    }
  }

  /**
    * The new macro can also embed lists of nodes directly.
    */
  @html def intersperseHtmlWithMethodsBindSeq: Binding[BindingSeq[Node]] = {
    List(
      <div>A tag</div>,
      intDiv(42),
      <div>Another tag</div>
    ).bindSeq
  }

}

/**
  * Since types are transformed by the @html macro, IntelliJ and other IDEs can have a hard time tracking the
  * type of Binding[Node] objects. Although the code is valid the IDE may present red code markers to indicate
  * an error even when there is no error.
  *
  * This class defines a few helpers for these situations.
  */
trait IDEHelpers {
  implicit def makeIntellijHappy(e: Elem): Binding[Node] = ???
  implicit def makeIntellijHappy(e: List[Elem]): List[NodeBinding[Node]] = ???
  implicit def makeIntellijHappy(
      e: BindingSeq[Elem]): Binding[BindingSeq[Node]] = ???
  implicit def makeIntellijHappy(o: Binding[Object]): Binding[Node] = ???
  implicit def makeIntellijHappy(b: NodeBuffer): BindingSeq[Node] = ???
}
