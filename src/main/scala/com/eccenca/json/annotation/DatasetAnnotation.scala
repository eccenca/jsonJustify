package com.eccenca.json.annotation

import org.silkframework.config.CustomTask
import org.silkframework.entity.EntitySchema
import org.silkframework.runtime.plugin.{MultilineStringParameter, Param, Plugin}
import org.silkframework.runtime.resource.WritableResource

@Plugin(
  id = "mindsetDatasetAnnotationInjector",
  label = "MINDSET Dataset Annotation Injector",
  description = "A custom task for injecting dataset custom json annotations and dataset metadata."
)
case class DatasetAnnotation(
  @Param(label = "Json Schema", value = "")
  jsonSchema: WritableResource,
  @Param(label = "Json Annotation", value = "")
  jsonAnnotation: MultilineStringParameter = MultilineStringParameter("")
) extends CustomTask {

  override def inputSchemataOpt: Option[Seq[EntitySchema]] = None

  override def outputSchemaOpt: Option[EntitySchema] = None
}
