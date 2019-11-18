package com.eccenca.json.validate

import java.io.InputStream

import org.leadpony.justify.api.{JsonSchema, JsonValidationService}
import org.leadpony.justify.internal.provider.DefaultJsonValidationProvider
import org.leadpony.justify.internal.validator.JsonValidator
import org.silkframework.runtime.resource.Resource

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Simple Json Validator based on http://json-schema.org schemata
  * @param schemaResource - a json-schema defining the expected structure
  * @param problemHandler - a function ingesting all the encountered problems with validated json documents
  */
class Validator(schemaResource: Resource, problemHandler: ValidationProblemHandler) {

  private val service: JsonValidationService = new DefaultJsonValidationProvider().createService()

  // the parsed schema
  private val schema: JsonSchema = service.readSchema(schemaResource.inputStream)

  /**
    * The validation function returning true, if no validation problem was found.
    * @param is - the resource of the document to be validated as InputStream
    */
  def validate(is: InputStream): Boolean ={
    val parser = service.createParser(is, schema, problemHandler)
    val validator = new JsonValidator(parser, schema, service.getJsonProvider)
    val iterator = validator.getValueStream.iterator().asScala

    Try {iterator.forall(_ => true)}.toOption.getOrElse(false)
  }
}
