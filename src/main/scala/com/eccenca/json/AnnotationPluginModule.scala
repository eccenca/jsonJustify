package com.eccenca.json

import com.eccenca.json.annotation._
import org.silkframework.runtime.plugin.PluginModule

class AnnotationPluginModule extends PluginModule {

  private val preloadedObjects = Seq(
    DsgvoAttributeAnnotation,
    DsgvoAttributeAnnotation
  )

  override def pluginClasses: Seq[Class[_]] = Seq(
    classOf[DatasetAnnotation],
    classOf[AnnotationInjector],
    classOf[AnnotationExtractor],
    classOf[AnnotationExtractorTask]
  )
}
