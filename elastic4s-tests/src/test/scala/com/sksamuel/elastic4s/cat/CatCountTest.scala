package com.sksamuel.elastic4s.cat

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.testkit.DockerTests
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try

class CatCountTest extends FlatSpec with Matchers with DockerTests {

  Try {
    http.execute {
      deleteIndex("catcount1")
    }.await
    http.execute {
      deleteIndex("catcount2")
    }.await
  }

  http.execute {
    bulk(
      indexInto("catcount1/landmarks").fields("name" -> "hampton court palace"),
      indexInto("catcount1/landmarks").fields("name" -> "tower of london"),
      indexInto("catcount2/landmarks").fields("name" -> "stonehenge")
    ).refresh(RefreshPolicy.Immediate)
  }.await

  "cats count" should "return count for all cluster" in {
    http.execute {
      catCount()
    }.await.result.count >= 3 shouldBe true
  }

  it should "support counting for a single index" in {
    http.execute {
      catCount("catcount1")
    }.await.result.count shouldBe 2
  }

  it should "support counting for multiple indices" in {
    http.execute {
      catCount("catcount1", "catcount2")
    }.await.result.count shouldBe 3
  }

}
