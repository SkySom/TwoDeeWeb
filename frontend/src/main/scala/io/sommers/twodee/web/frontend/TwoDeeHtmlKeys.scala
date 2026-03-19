package io.sommers.twodee.web.frontend

import com.raquo.laminar.defs.complex.ComplexHtmlKeys
import com.raquo.laminar.keys.HtmlAttr

object TwoDeeHtmlKeys extends ComplexHtmlKeys {
  val onSuccess: HtmlAttr[String] = dataAttr("onsuccess")
}
