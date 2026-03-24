package io.sommers.twodee.web.frontend.util

import com.raquo.laminar.codecs.StringAsIsCodec
import com.raquo.laminar.defs.complex.ComplexHtmlKeys
import com.raquo.laminar.keys.HtmlAttr

object BootstrapProps extends ComplexHtmlKeys {
  val dataToggle: HtmlAttr[String] = dataAttr("bs-toggle")
  
  val labelFor: HtmlAttr[String] = HtmlAttr[String]("for", StringAsIsCodec)
}
