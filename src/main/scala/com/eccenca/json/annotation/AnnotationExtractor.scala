package com.eccenca.json.annotation

import com.eccenca.di.spark.SparkExecutor
import com.eccenca.di.spark.context.SparkExecution
import com.eccenca.di.spark.entities.SparkEntities
import com.eccenca.json.annotation.DsgvoDatasetAnnotation._
import org.silkframework.config.{Prefixes, Task}
import org.silkframework.entity._
import org.silkframework.entity.paths.{TypedPath, UntypedPath}
import org.silkframework.execution.{ExecutionReport, ExecutorOutput}
import org.silkframework.runtime.activity.{ActivityContext, UserContext}
import org.silkframework.runtime.serialization.WriteContext
import org.silkframework.util.Uri

class AnnotationExtractor extends SparkExecutor[AnnotationExtractorTask] {
  override def execute(
    task: Task[AnnotationExtractorTask],
    inputs: Seq[SparkEntities],
    output: ExecutorOutput,
    execution: SparkExecution,
    context: ActivityContext[ExecutionReport]
  )(implicit userContext: UserContext, prefixes: Prefixes): Option[SparkEntities] = {
    assert(inputs.size == 1 , "Dataset annotation task expects exactly one input.")

    val annotation = DsgvoDatasetAnnotation.fromDatasetMetadata(inputs.head.metadata)
    val jsValue = DsgvoDatasetAnnotation.serializer.write(annotation)(WriteContext())
    task.jsonAnnotationResource.writeString(jsValue.toString())

    //FIXME this is a workaround, since JsonDataset does not support writing json files yet,
    // use the functions below (or similar) to create Entity and Schema to forward to a Datset in future
    None
  }

  private def attributeSchema(subPath: String): EntitySchema = EntitySchema(
    "",
    IndexedSeq(
      TypedPath(DsgvoAttributeAnnotation.PARAM_PERSONALIZED, StringValueType),
      TypedPath(DsgvoAttributeAnnotation.PARAM_OBFUSCATION, StringValueType),
      TypedPath(DsgvoAttributeAnnotation.PARAM_REMARKS, StringValueType)),
    Restriction.empty,
    UntypedPath(subPath)
  )

  private def getAttributeEntity(uri: Uri, anno: DsgvoAttributeAnnotation, schema: EntitySchema): Option[Entity] ={
    Some(Entity(
      uri, IndexedSeq(
        anno.dsgvoPersonenbezug.map(_.toString).toSeq,
        anno.dsgvoEnstellung.map(_.toString).toSeq,
        Seq(anno.dsgvoAnmerkung)
      ),
      schema
    ))
  }

  private def createSchemaAndEntity(annotation: DsgvoDatasetAnnotation): (EntitySchema, Entity) ={
    val staticTypedPathValues: Map[TypedPath, Seq[String]] = Map(
      TypedPath(PARAM_ID, StringValueType) -> Seq(annotation.id),
      TypedPath(PARAM_VERSION, StringValueType) -> Seq(annotation.version),
      TypedPath(PARAM_NAME, StringValueType) -> Seq(annotation.datasetName),
      TypedPath(PARAM_DATETIME, LongValueType) -> Seq(annotation.dataTime.getTime.toString),
      TypedPath(PARAM_PREVIOUS, StringValueType) -> annotation.previous.toSeq
    )
    val dependentPairs = annotation.dependend.map(d => createSchemaAndEntity(d))
    val schema = new MultiEntitySchema(
      EntitySchema("", staticTypedPathValues.keys.toIndexedSeq),
      annotation.attributes.map(a => attributeSchema(a._1)).toIndexedSeq ++ dependentPairs.map(_._1)
    )
    val subs = annotation.attributes.map(a => getAttributeEntity(Uri(annotation.id + "_" + a._1), a._2, attributeSchema(a._1)))
      .toIndexedSeq ++ dependentPairs.map(e => Some(e._2))
    (schema, Entity(annotation.id, staticTypedPathValues.values.toIndexedSeq, schema, subs))
  }
}
