import Helpers._
import org.mongodb.scala._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Aggregates._
import org.scalatest._
import scala.concurrent._
/**
  * Created by jahangir on 11/26/16.
  */

object Helpers {
  import scala.concurrent.duration._

  implicit class DocumentObservable[C](val observable: Observable[org.mongodb.scala.Document]) extends ImplicitObservable[Document] {
    override val converter: (Document) => String = (doc) => doc.toJson
  }

  implicit class GenericObservable[C](val observable: Observable[C]) extends ImplicitObservable[C] {
    override val converter: (C) => String = (doc) => doc.toString
  }

  trait ImplicitObservable[C] {
    val observable: Observable[C]
    val converter: (C) => String

    def results(): Seq[C] = Await.result(observable.toFuture(), 10.seconds)

    def headResult() = Await.result(observable.head(), 10.seconds)

    def printResults(initial: String = ""): Unit = {
      if (initial.length > 0) print(initial)
      results().foreach(res => println(converter(res)))
    }

    def printHeadResult(initial: String = ""): Unit = println(s"${initial}${converter(headResult())}")
  }

}

class SliceSpec extends FunSpec with Matchers with BeforeAndAfterAll{

  import org.mongodb.scala.model.Projections._


  val mongoClient: MongoClient = MongoClient("mongodb://192.168.99.100:27017")

  val database: MongoDatabase = mongoClient.getDatabase("mojo")

  val testCollection: MongoCollection[Document] = database.getCollection("foo")

  val filterBson = org.mongodb.scala.model.Filters.equal("name","MongoDB")

  override def beforeAll() {
    val doc: Document = org.mongodb.scala.Document(
      """
        |{
        | "name" : "MongoDB",
        | "type" : "Database",
        | "comments": [ "Good Database",
        | "Nice database",
        | "Awesomeness",
        | "Document-oriented DB"
        | ]
        |}
      """.stripMargin)

    testCollection.insertOne(doc).printResults()

  }

  describe("Mongo Scala Driver") {
    describe("projections") {
      it("Include Query should result in Document") {
        val testIncludeQuery = testCollection.aggregate(
          List(
            `match`(filterBson),
            project(fields(org.mongodb.scala.model.Projections.include("name", "type"), excludeId()))
          )
        )
        testIncludeQuery.printResults()
        testIncludeQuery.headResult() shouldBe a[Document]
      }

      it("Slice Query with limit should result in Document") {
        val testSliceQuery = testCollection.aggregate(
          List(
            `match`(filterBson),
            project(fields(org.mongodb.scala.model.Projections.include("name", "type"), excludeId(), slice("comments",6)))
          )
        )
        testSliceQuery.printResults()
        testSliceQuery.headResult() shouldBe a[Document]
      }

      it("Slice Query with skip and limit should result in Document") {
        val testSliceQuery = testCollection.aggregate(
          List(
            `match`(filterBson),
            project(fields(org.mongodb.scala.model.Projections.include("name", "type"), excludeId(), slice("comments",1,3)))
          )
        )

        testSliceQuery.printResults()
        testSliceQuery.headResult() shouldBe a[Document]
      }
    }
  }

  override def afterAll() {
    testCollection.drop().results()
  }

}
