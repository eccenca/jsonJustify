package com.eccenca.json.validate

import java.io.File

import org.leadpony.justify.api.{JsonValidatingException, Problem}
import org.scalatest.FunSuite
import org.scalatest.exceptions.TestFailedException
import org.silkframework.runtime.resource.FileResource
import scala.collection.JavaConverters._

class ValidatorTest extends FunSuite {

  val schemaResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsgvoJsonSchema.json"))
  val validator = new Validator(schemaResource, new ValidationProblemHandler(throwProblemException))
  var foundProblems: Seq[Problem] = Seq()

  test("test validate with valid json") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsgvoAnnotation.json"))
    validator.validate(testResource.inputStream)
    checkForProblems()
  }

  test("test validate with valid json and dsgvoAufgabe") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsgvoAnnotationAufgabe.json"))
    validator.validate(testResource.inputStream)
    checkForProblems()
  }

  test("test validate with typo") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsgvoAnnotationWithTypo.json"))
    validator.validate(testResource.inputStream)
    intercept[TestFailedException](
      checkForProblems(Some("oneOf"))
    )
  }

  test("test validate with missing required property") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsgvoAnnotationMissingRequired.json"))
    validator.validate(testResource.inputStream)
    intercept[TestFailedException](
      checkForProblems(Some("required"))
    )
  }

  test("test validate with wrong data type") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsgvoAnnotationWrongType.json"))
    validator.validate(testResource.inputStream)
    intercept[TestFailedException](
      checkForProblems(Some("type"))
    )
  }

  test ("test validate with violating pattern") {
    val testResource = FileResource(new File(new File("").getAbsoluteFile, "/src/test/resources/dsgvoAnnotationPatternNotMtaching.json"))
    validator.validate(testResource.inputStream)
    intercept[TestFailedException](
      checkForProblems(Some("pattern"))
    )
  }


  def throwProblemException(problem: JsonValidatingException): Unit = {
    foundProblems = foundProblems ++ problem.getProblems.asScala
  }

  def checkForProblems(expectedProblemKey: Option[String] = None): Unit ={
    if(foundProblems.isEmpty)
      return

    val zw = foundProblems
    foundProblems = Seq()
    if(zw.nonEmpty && expectedProblemKey.nonEmpty && zw.head.getKeyword == expectedProblemKey.get){
      fail(new JsonValidatingException(zw.asJava))
    }
    else{
      throw new IllegalStateException("The expected problem was not found as first problem: " + expectedProblemKey.getOrElse("") +
        ". Actual problem was " + zw.headOption.map(_.getKeyword).getOrElse("no problem found"))
    }
  }
}
