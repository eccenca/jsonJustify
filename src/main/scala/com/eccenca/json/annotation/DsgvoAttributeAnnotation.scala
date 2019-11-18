package com.eccenca.json.annotation

import org.silkframework.runtime.serialization.{ReadContext, WriteContext}
import org.silkframework.serialization.json.metadata.JsonMetadataSerializer
import org.silkframework.serialization.json.JsonHelpers._
import play.api.libs.json.{JsObject, JsString, JsValue}

case class DsgvoAttributeAnnotation(
 dsgvoPersonenbezug: Option[DsgvoPersonenbezug.Value],
 dsgvoEnstellung: Option[DsgvoEnstellung.Value],
 dsgvoAnmerkung: String = ""
) extends Serializable

object DsgvoAttributeAnnotation {

  val PARAM_REMARKS = "dsgvoAnmerkung"
  val PARAM_OBFUSCATION = "dsgvoEnstellung"
  val PARAM_PERSONALIZED = "dsgvoPersonenbezug"

  def toObjectMap(anno: DsgvoAttributeAnnotation): Map[String, Any] = Map(
    PARAM_REMARKS -> anno.dsgvoAnmerkung
  ) ++ anno.dsgvoPersonenbezug.map(p => PARAM_PERSONALIZED -> p) ++
    anno.dsgvoEnstellung.map(p => PARAM_OBFUSCATION -> p)

  def fromDatasetMetadata(metadata: Map[String, Any]): DsgvoAttributeAnnotation = {
    DsgvoAttributeAnnotation(
      metadata.get(PARAM_PERSONALIZED).map(x => DsgvoPersonenbezug.withName(x.toString)),
      metadata.get(PARAM_OBFUSCATION).map(x => DsgvoEnstellung.withName(x.toString)),
      metadata.get(PARAM_REMARKS).map(_.toString).getOrElse("")
    )
  }

  val serializer: JsonMetadataSerializer[DsgvoAttributeAnnotation] = new JsonMetadataSerializer[DsgvoAttributeAnnotation] {

    override def read(value: JsValue)(implicit readContext: ReadContext): DsgvoAttributeAnnotation = {
      val pers = stringValueOption(value, PARAM_PERSONALIZED).filterNot(_ == "none").map(p => DsgvoPersonenbezug.withName(p))
      val obfus = stringValueOption(value, PARAM_OBFUSCATION).filterNot(_ == "none").map(p => DsgvoEnstellung.withName(p))
      val remarks = stringValueOption(value, PARAM_REMARKS).getOrElse("")
      DsgvoAttributeAnnotation(pers, obfus, remarks)
    }

    override def write(anno: DsgvoAttributeAnnotation)(implicit writeContext: WriteContext[JsValue]): JsValue = {
      JsObject(Seq(
        PARAM_REMARKS -> JsString(anno.dsgvoAnmerkung)
      ) ++ anno.dsgvoEnstellung.toSeq.map(p => PARAM_OBFUSCATION -> JsString(p.toString)) ++
        anno.dsgvoPersonenbezug.toSeq.map(p => PARAM_PERSONALIZED -> JsString(p.toString))
      )
    }

    override def metadataId: String = "dsgvo_attribute_annotation"

    override def replaceableMetadata: Boolean = true
  }
}
