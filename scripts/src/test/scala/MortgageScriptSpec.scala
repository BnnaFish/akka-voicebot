import akka.actor.testkit.typed.scaladsl.LogCapturing
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.typed.PersistenceId
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.serialization._

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.BeforeAndAfterEach
import org.scalatest.GivenWhenThen

import Scripts.PersistScript
import Scripts.PersistScript._
import Domain.Entities._
import Domain.Replies._
import Domain.Intents._

class MortgageScriptSpec
  extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config)
  with AnyFeatureSpecLike
  with BeforeAndAfterEach
  with GivenWhenThen {
  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  private val eventSourcedTestKit =
    EventSourcedBehaviorTestKit[Command, Event, State](
      system,
      PersistScript("1", PersistenceId("Account", "1")))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    eventSourcedTestKit.clear()
  }

  Feature("User can discard mortgage offer") {
    Scenario("Bot asked user about martgage") {
      Given("new call just started")

      When("bot asked about mortgage")
      eventSourcedTestKit.runCommand(UserSaidCommand(intent=Intent("Allo"), _))
      val asked = eventSourcedTestKit.runCommand(WhatToReply(_))
      asked.stateOfType[AskedQuestion.type].whatToReply.textToReply shouldBe "Do you what to buy a house?"

      Then("user said no")
      val answer = eventSourcedTestKit.runCommand(UserSaidCommand(intent=Intent("no"), _))
      answer.stateOfType[NoAnswerResived.type].whatToReply.textToReply shouldBe "Any other questions?"
    }
  }
}
