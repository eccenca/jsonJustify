package com.eccenca.json.annotation

import org.silkframework.config.CustomTask
import org.silkframework.entity.EntitySchema
import org.silkframework.runtime.plugin.{Param, Plugin}
import org.silkframework.runtime.resource.WritableResource

@Plugin(
  id = "mindsetDatasetAnnotationExtractor",
  label = "MINDSET Dataset Annotation Extractor",
  description = "A custom task for extracting dataset custom json annotations and dataset metadata (to be used in cooperation with injector of the same name)."
)
case class AnnotationExtractorTask(
   @Param(label = "Annotation File", value = "The extracted json Annotation resource")
   jsonAnnotationResource: WritableResource
 ) extends CustomTask {

  override def inputSchemataOpt: Option[Seq[EntitySchema]] = None

  override def outputSchemaOpt: Option[EntitySchema] = None
}
