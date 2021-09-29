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
    Scenario("User dont want mortgage") {
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

  Feature("Discard morgage from our side") {
    Scenario("User got no enough initial sum ") {
      Given("bot asked initial sum")
      val result1 =  eventSourcedTestKit.runCommand(UserSaidCommand(intent=Intent("Allo"), _))
      result1.stateOfType[AskedQuestion.type].whatToReply.textToReply shouldBe "Do you what to buy a house?"
      val result2 = eventSourcedTestKit.runCommand(UserSaidCommand(intent=Intent("yes"), _))
      val initialSum = 1000000
      info(s"User initial sum $initialSum")
      result2.stateOfType[YesAnswerResived.type].whatToReply.textToReply shouldBe "What amount of initial sum do you allready have?"
      val result3 = eventSourcedTestKit.runCommand(UserSaidCommand(intent=Intent(initialSum.toString), _))
      result3.stateOfType[AskedTotalSumQuestion].initialSum shouldBe initialSum

      When("user want mortgage more expensive then threshold")
      result3.stateOfType[AskedTotalSumQuestion].whatToReply.textToReply shouldBe "What amount of mortgage do you need?"
      val totalSum = initialSum * (1 / TotalSumAnswered(initialSum, initialSum).threshold).toLong * 2
      info(f"User total sum $totalSum%d with part ${initialSum.toDouble / totalSum}%2.2f%%")
      val result4 = eventSourcedTestKit.runCommand(UserSaidCommand(intent=Intent(totalSum.toString), _))

      Then("we discard mortgage")
      result4.stateOfType[TotalSumAnswered].whatToReply.textToReply shouldBe "Sorry, but not enough initial sum."
    }
  }

  Feature("Approved mortgage") {
    Scenario("Everithing ok") {
      Given("bot asked initial sum")
      val result1 =  eventSourcedTestKit.runCommand(UserSaidCommand(intent=Intent("Allo"), _))
      result1.stateOfType[AskedQuestion.type].whatToReply.textToReply shouldBe "Do you what to buy a house?"
      val result2 = eventSourcedTestKit.runCommand(UserSaidCommand(intent=Intent("yes"), _))
      val initialSum = 1000000
      info(s"User initial sum $initialSum")
      result2.stateOfType[YesAnswerResived.type].whatToReply.textToReply shouldBe "What amount of initial sum do you allready have?"
      val result3 = eventSourcedTestKit.runCommand(UserSaidCommand(intent=Intent(initialSum.toString), _))
      result3.stateOfType[AskedTotalSumQuestion].initialSum shouldBe initialSum

      When("user want mortgage cheaper then threshold")
      result3.stateOfType[AskedTotalSumQuestion].whatToReply.textToReply shouldBe "What amount of mortgage do you need?"
      val totalSum = initialSum * (1 / TotalSumAnswered(initialSum, initialSum).threshold * 0.9).toLong
      info(f"User total sum $totalSum%d with part ${initialSum.toDouble / totalSum}%2.2f%%")
      val result4 = eventSourcedTestKit.runCommand(UserSaidCommand(intent=Intent(totalSum.toString), _))

      Then("we congrats")
      result4.stateOfType[TotalSumAnswered].whatToReply.textToReply shouldBe "Mortgage is approved. Congratulations!"
    }
  }
}
