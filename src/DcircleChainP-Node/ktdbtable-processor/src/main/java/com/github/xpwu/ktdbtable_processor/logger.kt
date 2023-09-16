package com.github.xpwu.ktdbtable_processor

import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class Logger(private val messager: Messager) {
  fun error(e: Element?, msg: String?, vararg args: Any?) {
    messager.printMessage(
      Diagnostic.Kind.ERROR, String.format(msg!!, *args), e
    )
  }

  fun log(e: Element?, msg: String?, vararg args: Any?) {
    messager.printMessage(
      Diagnostic.Kind.NOTE, String.format(msg!!, *args), e
    )
  }

  fun warn(e: Element?, msg: String?, vararg args: Any?) {
    messager.printMessage(
      Diagnostic.Kind.WARNING, String.format(msg!!, *args), e
    )
  }
}