package com.eccenca.json.validate

import org.leadpony.justify.api.{JsonSchema, JsonValidatingException, JsonValidationService}
import org.leadpony.justify.internal.validator.{JsonValidator, JsonValidatorFactory}
import org.silkframework.runtime.resource.Resource

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Simple Json Validator based on http://json-schema.org schemata
  * @param schemaResource - a json-schema defining the expected structure
  * @param problemFunction - a function ingesting all the encountered problems with validated json documents
  */
class Validator(schemaResource: Resource, problemFunction: JsonValidatingException => Unit) {

  private val service: JsonValidationService = JsonValidationService.newInstance

  // the parsed schema
  private val schema: JsonSchema = service.readSchema(schemaResource.inputStream)

  private val problemHandler = new ValidationProblemHandler(problemFunction)

  /**
    * The validation function returning true, if no validation problem was found.
    * @param jsonResource - the resource of the document to be validated
    */
  def validate(jsonResource: Resource): Boolean ={
    val parser = service.createParser(jsonResource.inputStream, schema, problemHandler)
    val validator = new JsonValidator(parser, schema, service.getJsonProvider)
    val iterator = validator.getValueStream.iterator().asScala

    Try {iterator.forall(_ => true)}.toOption.getOrElse(false)
  }
}
