package org.lrng.ionic.webcomponents

import com.thoughtworks.binding.Binding
import org.lrng.binding.html.NodeBinding.Interpolated.MountPointBuilder
import org.lrng.binding.html.{AttributeFactory, ElementFactory, NodeBinding}
import org.lrng.binding.{AttributeFactories, html}
import org.scalajs.dom.document
import org.scalajs.dom.raw.{CustomEvent, Event, HTMLElement}

import scala.language.dynamics
import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
  * @author Kahli Burke
  */
object xml {
  type IonicElement = HTMLElement
  class IonicCheckedElement extends IonicElement

  def elementBuilder[T <: IonicElement](tag: String) = {
    new html.NodeBinding.Constant.ElementBuilder(
      document
        .createElement(tag)
        .asInstanceOf[T])
  }

  @inline def comment(data: String) =
    new NodeBinding.Constant.NodeBuilder(document.createComment(data))

  @inline def interpolation = Binding

  /**
    * Only some elements from the ionic library are specified here as an example.
    */
  object elements {

    object `ion-toggle` extends ElementFactory[IonicCheckedElement] {
      @inline protected def tagName = "ion-toggle"
    }

    object `ion-buttons` extends ElementFactory[IonicElement] {
      @inline protected def tagName = "ion-buttons"
    }

    object `ion-toolbar` extends ElementFactory[IonicElement] {
      @inline protected def tagName = "ion-toolbar"
    }

    object `ion-icon` extends ElementFactory[IonicElement] {
      @inline protected def tagName = "ion-icon"
    }

    object `ion-title` extends ElementFactory[IonicElement] {
      @inline protected def tagName = "ion-title"
    }

    object `ion-button` extends ElementFactory[IonicElement] {
      @inline protected def tagName = "ion-button"
    }

    object `ion-menu-button` extends ElementFactory[IonicElement] {
      @inline protected def tagName = "ion-menu-button"
    }

    object `ion-label` extends ElementFactory[IonicElement] {
      @inline protected def tagName = "ion-label"
    }

    object `ion-item` extends ElementFactory[IonicElement] {
      @inline protected def tagName = "ion-item"
    }

  }

  object attributes {
    abstract class EventAttr[T, EventType <: Event](attrName: String)
        extends MountPointBuilder[HTMLElement, T, js.Function1[EventType, _]] {

      def mountProperty(element: HTMLElement,
                        binding: Binding[js.Function1[EventType, _]]) = {
        Binding.BindingInstances.map(binding)(l =>
          element.addEventListener(attrName, l))
      }
    }
    abstract class TypedAttr[ElementType <: IonicElement, AttrType, ValueType](
        attrName: String)
        extends MountPointBuilder[ElementType, AttrType, ValueType] {
      def mountProperty(element: ElementType, binding: Binding[ValueType]) = {
        Binding.BindingInstances.map(binding)(v =>
          element.setAttribute(attrName, v.toString))
      }
    }

    @inline def onclick = AttributeFactories.onclick
    @inline object onIonChange extends AttributeFactory.Typed {
      implicit object handler
          extends EventAttr[this.type, CustomEvent]("ionChange")
    }

    // Type safety for attributes if they are given their own types, like IonicCheckedElement
    object checked extends AttributeFactory.Typed {
      implicit object handler
          extends TypedAttr[IonicCheckedElement, this.type, Boolean]("checked")
    }

  }

  val texts = org.lrng.binding.html.autoImports.xml.texts

  implicit final class IonicElementUriOps(
      uriFactory: html.autoImports.xml.uris.type) {
    @inline def `https://ionicframework.com/webcomponents` = xml
  }

  implicit class IonChangeEvent(e: CustomEvent) {
    def dynDetail = e.detail.asInstanceOf[scalajs.js.Dynamic]
    def checked = {
      dynDetail.checked.asInstanceOf[UndefOr[Boolean]]
    }
  }

  implicit def ionChangeEvent(eventFunc: (IonChangeEvent) => Unit)
    : scala.scalajs.js.Function1[CustomEvent, Unit] = { e: CustomEvent =>
    eventFunc(e)
  }
}
