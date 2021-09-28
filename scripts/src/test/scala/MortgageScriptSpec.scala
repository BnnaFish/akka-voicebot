import akka.actor.testkit.typed.scaladsl.LogCapturing
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.typed.PersistenceId
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.serialization._

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.BeforeAndAfterEach
import org.scalatest.GivenWhenThen

import Scripts.PersistScript
import Domain.Entities._
import Domain.Replies._
import Domain.Intents._

class MortgageScriptSpec
  extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config)
  with AnyFlatSpecLike
  with BeforeAndAfterEach
  with GivenWhenThen {
  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  private val eventSourcedTestKit =
    EventSourcedBehaviorTestKit[PersistScript.Command, PersistScript.Event, PersistScript.State](
      system,
      PersistScript("1", PersistenceId("Account", "1")))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    eventSourcedTestKit.clear()
  }

  "Call just started" should "greet user" in {
    val script = testKit.spawn(PersistScript("entityId", PersistenceId.ofUniqueId("abc")), "PersistScript")

    Given("bot greet first")
    val probe = testKit.createTestProbe[Reply]()

    When("user keep silence")
    script ! PersistScript.WhatToReply(probe.ref)

    Then("bot ask")
    probe.expectMessage(WantToTakeMortgageReply())
  }
}