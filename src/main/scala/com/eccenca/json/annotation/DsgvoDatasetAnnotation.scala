package com.eccenca.json.annotation

import java.util.Date

import org.silkframework.runtime.serialization.{ReadContext, WriteContext}
import org.silkframework.serialization.json.metadata.JsonMetadataSerializer
import play.api.libs.json._
import org.silkframework.serialization.json.JsonHelpers._
import play.api.libs.json

import scala.util.Try

case class DsgvoDatasetAnnotation(
 id: String,
 version: String,
 datasetName: String,
 dataTime: Date,
 attributes: Map[String, DsgvoAttributeAnnotation] = Map(),
 previous: Option[String] = None,
 dependend: IndexedSeq[DsgvoDatasetAnnotation] = IndexedSeq()
) extends Serializable

object DsgvoDatasetAnnotation {

  val PARAM_ID = "@id"
  val PARAM_VERSION = "@vers"
  val PARAM_PREVIOUS = "@prev"
  val PARAM_NAME = "datasetName"
  val PARAM_DATETIME = "zeitpunkt"
  val PARAM_ATTRIBUTES = "dsgvoAttribute"
  val PARAM_DEPENDENT = "dependentDatasets"

  def toObjectMap(anno: DsgvoDatasetAnnotation): Map[String, Any] = Map(
    PARAM_ID -> anno.id,
    PARAM_VERSION -> anno.version,
    PARAM_NAME -> anno.datasetName,
    PARAM_DATETIME -> anno.dataTime,
    PARAM_ATTRIBUTES -> anno.attributes,
    PARAM_DEPENDENT -> anno.dependend.map(toObjectMap)
  ) ++ anno.previous.map(p => PARAM_PREVIOUS -> p)

  private def getOrThrow(meta: Map[String, Any], param: String) = meta.getOrElse(param,
    throw new IllegalArgumentException(param + " is not a member of the Dataset Metadata object of this dataset."))

  def fromDatasetMetadata(metadata: Map[String, Any]): DsgvoDatasetAnnotation = {

    DsgvoDatasetAnnotation(
      getOrThrow(metadata, PARAM_ID).toString,
      getOrThrow(metadata, PARAM_VERSION).toString,
      getOrThrow(metadata, PARAM_NAME).toString,
      getOrThrow(metadata, PARAM_DATETIME).asInstanceOf[Date],
      getOrThrow(metadata, PARAM_ATTRIBUTES).asInstanceOf[Map[String, DsgvoAttributeAnnotation]],
      metadata.get(PARAM_PREVIOUS).map(_.toString),
      metadata.get(PARAM_DEPENDENT).toIndexedSeq
        .flatMap(m => m.asInstanceOf[IndexedSeq[Map[String, Any]]].map(m => fromDatasetMetadata(m)))
    )
  }

  val serializer: JsonMetadataSerializer[DsgvoDatasetAnnotation] = new JsonMetadataSerializer[DsgvoDatasetAnnotation] {
    override def read(value: JsValue)(implicit readContext: ReadContext): DsgvoDatasetAnnotation = {
      val id = stringValue(value, PARAM_ID)
      val version = stringValue(value, PARAM_VERSION)
      val prev = Try {
        stringValue(value, PARAM_PREVIOUS)
      }.toOption
      val name = stringValue(value, PARAM_NAME)
      val date = {
        val num = numberValue(value, PARAM_DATETIME)
        new Date(num.longValue())
      }
      val attributesObj = (value \ PARAM_ATTRIBUTES).getOrElse(JsObject(Seq()))
      val attributes = mustBeJsObject(attributesObj)((array: JsObject) =>
        array.value.map(kv => kv._1 -> DsgvoAttributeAnnotation.serializer.read(kv._2))).toMap
      DsgvoDatasetAnnotation(id, version, name, date, attributes, prev)
    }

    override def write(anno: DsgvoDatasetAnnotation)(implicit writeContext: WriteContext[JsValue]): JsValue = {
      JsObject(Seq(
        PARAM_ID -> JsString(anno.id),
        PARAM_VERSION -> JsString(anno.version),
        PARAM_NAME -> JsString(anno.datasetName),
        PARAM_DATETIME -> json.JsNumber(anno.dataTime.getTime),
        PARAM_ATTRIBUTES -> JsObject(anno.attributes.map(kv => kv._1 -> DsgvoAttributeAnnotation.serializer.write(kv._2)))
      ) ++ anno.previous.toSeq.map(p => PARAM_PREVIOUS -> JsString(p)))
    }

    override def metadataId: String = "dsvgo_dataset_annotation"

    override def replaceableMetadata: Boolean = true
  }
}
