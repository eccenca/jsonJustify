package com.eccenca.json.validate

import java.util

import org.leadpony.justify.api.{JsonValidatingException, Problem, ProblemHandler}

/**
  * A ProblemHandler dealing with JsonValidatingExceptions
  */
class ValidationProblemHandler(problemFunc: JsonValidatingException => Unit) extends ProblemHandler {
  override def handleProblems(problems: util.List[Problem]): Unit = {
    problemFunc(new JsonValidatingException(problems))
  }
}
