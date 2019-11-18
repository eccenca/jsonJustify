package com.eccenca.json.annotation

import com.eccenca.di.spark.context.SparkExecution
import com.eccenca.di.spark.entities._
import com.eccenca.di.spark.metadata.DatasetMetadata
import com.eccenca.di.spark.{SparkExecutor, entities}
import com.eccenca.json.validate.{ValidationProblemHandler, Validator}
import org.leadpony.justify.api.JsonValidatingException
import org.silkframework.config.{Prefixes, Task}
import org.silkframework.entity.paths.PathWithMetadata
import org.silkframework.entity.{EntitySchema, MultiEntitySchema}
import org.silkframework.execution.{ExecutionReport, ExecutorOutput}
import org.silkframework.runtime.activity.{ActivityContext, UserContext}
import org.silkframework.runtime.serialization.ReadContext
import play.api.Logger
import play.api.libs.json.Json

class AnnotationInjector extends SparkExecutor[DatasetAnnotation] {

  private val logger = Logger(this.getClass)

  override def execute(
    task: Task[DatasetAnnotation],
    inputs: Seq[SparkEntities],
    output: ExecutorOutput,
    execution: SparkExecution,
    context: ActivityContext[ExecutionReport]
  )(implicit userContext: UserContext, prefixes: Prefixes): Option[SparkEntities] = {
    assert(inputs.size == 1 , "Dataset annotation task expects exactly one input.")

    def handleValidationExceptions(valExc: JsonValidatingException): Unit = {
      context.log.severe(valExc.getMessage)
    }

    implicit val readContext: ReadContext = ReadContext()
    val annoValidator = new Validator(task.jsonSchema, new ValidationProblemHandler(handleValidationExceptions))
    val is = new java.io.ByteArrayInputStream(task.jsonAnnotation.str.getBytes(java.nio.charset.StandardCharsets.UTF_8.name))

    if(annoValidator.validate(is)) {
      val jsVal = Json.parse(task.jsonAnnotation.str)
      val annotation = DsgvoDatasetAnnotation.serializer.read(jsVal)
      Some(annotateDataset(inputs.head, annotation))
    }
    else{
      throw new IllegalArgumentException() //TODO
    }
  }

  private def annotateSchema(entitySchema: EntitySchema, attributeMetadata: Map[String, DsgvoAttributeAnnotation]): EntitySchema = entitySchema match{
    case mes: MultiEntitySchema => new MultiEntitySchema(
      annotateSchema(mes.pivotSchema, attributeMetadata),
      mes.subSchemata.map(s => annotateSchema(s, attributeMetadata))
    )
    case es: EntitySchema =>
      val annotatedPaths = es.typedPaths.map(tp => attributeMetadata.get(tp.normalizedSerialization) match{
        case Some(meta) =>
          val tmpTp = PathWithMetadata.fromTypedPath(tp)    // we need to add the required metadata keys as well
          PathWithMetadata(tp.operators, tp.valueType, DsgvoAttributeAnnotation.toObjectMap(meta) ++ tmpTp.metadata)
        case None => tp
      })
      es.copy(typedPaths = annotatedPaths)
  }

  private def annotateDataset(se: SparkEntities, annotation: DsgvoDatasetAnnotation): SparkEntities ={
    val metadata = new DatasetMetadata(se.task.id, se.datasetId, DsgvoDatasetAnnotation.toObjectMap(annotation))
    val schema = annotateSchema(se.entitySchema, annotation.attributes)
    se match{
      case df: SparkDataFrameEntities => entities.SparkSimpleEntities(df, df.entitySchema, df.task, Some(df.datasetId), Some(df.dataFrame), metadata)
      case ce: SparkComplexEntities =>
        val subs = annotation.dependend
        SparkComplexEntities(annotateDataset(ce.baseData, annotation), schema, ce.referencedEntityTables.map(d => annotateDataset(d, subs.head)), ce.datasetId, false, ce.task) //TODO
      case oe: SparkOrderedEntities => SparkOrderedEntities(oe, schema, oe.task, Some(oe.datasetId), metadata)
      case se: SparkSimpleEntities => entities.SparkSimpleEntities(se, schema, se.task, Some(se.datasetId), None, metadata)
    }
  }
}
