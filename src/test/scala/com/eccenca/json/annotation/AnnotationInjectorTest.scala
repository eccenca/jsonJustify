package com.eccenca.json.annotation

import java.io.File
import java.nio.file.Paths

import com.eccenca.di.spark.context.SparkExecution
import com.eccenca.di.spark.entities.{SparkEntities, SparkSimpleEntities}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, MustMatchers}
import org.silkframework.config.{MetaData, Prefixes}
import org.silkframework.entity.paths.TypedPath
import org.silkframework.entity.{Entity, EntitySchema, StringValueType}
import org.silkframework.execution.ExecutorOutput
import org.silkframework.runtime.activity.UserContext
import org.silkframework.runtime.plugin.MultilineStringParameter
import org.silkframework.runtime.resource.FileResource
import org.silkframework.workspace._
import org.silkframework.workspace.resources.PerProjectFileRepository
import org.silkframework.workspace.xml.FileWorkspaceProvider

class AnnotationInjectorTest extends FlatSpec with MustMatchers with BeforeAndAfterAll {
  implicit val userContext: UserContext = UserContext.Empty
  implicit val prefixes: Prefixes = Prefixes.empty
  private val execution = SparkExecution()
  private val workspaceDir = new File(Paths.get("").toAbsolutePath + "/src/test/resources")
  private val schemaFile = FileResource(new File(workspaceDir, "dsvgoJsonSchema.json"))
  private val workspace = new Workspace(FileWorkspaceProvider(workspaceDir.getAbsolutePath), PerProjectFileRepository(workspaceDir.getAbsolutePath))

  private def getProject(name: String) = workspace.projects.find(p => p.name.toString() == name) match {
    case Some(p) => p
    case None => workspace.createProject(new ProjectConfig(name))
  }

  val entitySchema = EntitySchema("", IndexedSeq(
    TypedPath("attrName1", StringValueType),
    TypedPath("attrName2", StringValueType),
    TypedPath("attrName3", StringValueType)
  ))

  val entities = Seq(
    Entity("e1", IndexedSeq(Seq("2"), Seq("2"), Seq("2")), entitySchema),
    Entity("e2", IndexedSeq(Seq("2"), Seq("2"), Seq("2")), entitySchema),
    Entity("e3", IndexedSeq(Seq("2"), Seq("2"), Seq("2")), entitySchema)
  )

  private var se: SparkEntities = _

  it should "inject dsgvo metadata without error" in {
    val testId = "annotations_test"
    val project = getProject(testId)
    val activity = new AnnotationInjector()
    val annotation =
      """
        |{
        |  "@id": "datasetid",
        |  "@vers": "20191108",
        |  "@prev": "20191101",
        |  "datasetName": "Name of Dataset",
        |  "datenBeschreibung": "A description",
        |  "dsgvoAttribute": {
        |    "attrName1": {
        |      "dsgvoPersonenbezug": "personenbeziehbar",
        |      "dsgvoEntstellt": "pseudonymisiert",
        |      "dsgvoVerwendungszweck": "",
        |      "dsgvoAnmerkung": "Eine weitere Anmerkung."
        |    },
        |    "attrName2": {
        |      "dsgvoPersonenbezug": "personenbezogen",
        |      "dsgvoEntstellt": "annonymisiert",
        |      "dsgvoVerwendungszweck": "",
        |      "dsgvoAnmerkung": "Eine weitere Anmerkung."
        |    },
        |    "attrName3": {
        |      "dsgvoPersonenbezug": "none",
        |      "dsgvoEntstellt": "none",
        |      "dsgvoVerwendungszweck": "Any purpose",
        |      "dsgvoAnmerkung": ""
        |    }
        |  },
        |  "zeitpunkt": 45678765435678
        |}
      """.stripMargin

    val defaultTask = DatasetAnnotation(schemaFile, MultilineStringParameter(annotation))
    val t = new ProjectTask[DatasetAnnotation](project.name + "_task", defaultTask, MetaData.empty, new Module(null, project))
    val sparkEntities = SparkSimpleEntities(entities, entitySchema, t, None)
    val res = activity.execute(t, Seq(sparkEntities), ExecutorOutput.empty, execution)

    res.nonEmpty mustBe true
    se = res.head
  }

  it should "extract dsgvo metadata from the result" in {
    val testId = "annotations_test2"
    val project = getProject(testId)
    val activity = new AnnotationExtractor()

    val outputFile = project.resources.get("outputFile.json")
    val defaultTask = AnnotationExtractorTask(outputFile)
    val t = new ProjectTask[AnnotationExtractorTask](project.name + "_task", defaultTask, MetaData.empty, new Module(null, project))
    val res = activity.execute(t, Seq(se), ExecutorOutput.empty, execution)

    res.nonEmpty mustBe false
    val lines = outputFile.loadLines
    lines.nonEmpty mustBe true
  }
}
