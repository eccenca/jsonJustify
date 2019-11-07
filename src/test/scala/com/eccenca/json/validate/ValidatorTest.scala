package com.eccenca.json.validate

import java.io.File

import org.leadpony.justify.api.JsonValidatingException
import org.scalatest.FunSuite
import org.scalatest.exceptions.TestFailedException
import org.silkframework.runtime.resource.FileResource

class ValidatorTest extends FunSuite {

  val schemaResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsvgoJsonSchema.json"))
  val validator = new Validator(schemaResource, throwProblemException)
  var foundProblems: Seq[JsonValidatingException] = Seq()

  test("test validate with valid json") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsvgoAnnotation.json"))
    validator.validate(testResource)
    checkForProblems()
  }

  test("test validate with typo") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsvgoAnnotationWithTypo.json"))
    validator.validate(testResource)
    intercept[TestFailedException](
      checkForProblems()
    )
  }

  test("test validate with missing required property") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsvgoAnnotationMissingRequired.json"))
    validator.validate(testResource)
    intercept[TestFailedException](
      checkForProblems()
    )
  }

  test("test validate with wrong data type") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsvgoAnnotationWrongType.json"))
    validator.validate(testResource)
    intercept[TestFailedException](
      checkForProblems()
    )
  }

  test("test validate with violating pattern") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsvgoAnnotationPatternNotMtaching.json"))
    validator.validate(testResource)
    intercept[TestFailedException](
      checkForProblems()
    )
  }


  def throwProblemException(problem: JsonValidatingException): Unit = {
    foundProblems ++= Seq(problem)
  }

  def checkForProblems(): Unit ={
    val zw = foundProblems
    foundProblems = Seq()
    zw.foreach(p => fail(p))
  }
}
