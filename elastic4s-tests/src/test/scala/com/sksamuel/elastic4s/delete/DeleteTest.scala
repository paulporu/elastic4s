package com.sksamuel.elastic4s.delete

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.testkit.DockerTests
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try

class DeleteTest extends FlatSpec with DockerTests with Matchers {

  Try {
    http.execute {
      deleteIndex("places")
    }.await
  }

  http.execute(
    bulk(
      indexInto("places/cities") id "99" fields(
        "name" -> "London",
        "country" -> "UK"
      ),
      indexInto("places/cities") id "44" fields(
        "name" -> "Philadelphia",
        "country" -> "USA"
      ),
      indexInto("places/cities") id "615" fields(
        "name" -> "Middlesbrough",
        "country" -> "UK",
        "continent" -> "Europe"
      )
    ).refreshImmediately
  ).await

  "a delete by id query" should "return success but with result = not_found when a document does not exist" in {

    http.execute {
      delete("141212") from "places" / "cities" refresh RefreshPolicy.Immediate
    }.await.result.result shouldBe "not_found"

    http.execute {
      searchWithType("places" / "cities").limit(0)
    }.await.result.totalHits shouldBe 3
  }

  it should "return an error when the index does not exist" in {

    http.execute {
      delete("141212") from "wooop/la" refresh RefreshPolicy.Immediate
    }.await.error.`type` shouldBe "index_not_found_exception"

    http.execute {
      searchWithType("places" / "cities").limit(0)
    }.await.result.totalHits shouldBe 3
  }

  it should "remove a document when deleting by id" in {
    http.execute {
      delete("99") from "places/cities" refresh RefreshPolicy.Immediate
    }.await.result.result shouldBe "deleted"

    http.execute {
      searchWithType("places" / "cities").limit(0)
    }.await.result.totalHits shouldBe 2
  }
}
