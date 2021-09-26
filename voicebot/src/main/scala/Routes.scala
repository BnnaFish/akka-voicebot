package Voicebot

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.util.Timeout

import Domain.JsonFormats
import Domain.Entities._
import Domain.Intents.Intent
import Domain.Replies._
import Scripts.PersistScript
import Scripts.TakeMortrage

object Routes {

  class Routes(
    buildTakeMortrageScript: ActorRef[TakeMortrage.Command],
    buildPersistScript: ActorRef[PersistScript.Command]
  )(implicit system: ActorSystem[_])
      extends JsonFormats {
    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

    implicit val timeout: Timeout = 3.seconds

    lazy val whatToReplyRoute: Route =
      pathPrefix("api" / "v1" / "what-to-reply") {
        post {
          entity(as[WhatToReplyRequest]) { whatToReplyRequest =>
            {
              system.log.info("request: {}", whatToReplyRequest)
              val intent = Intent(whatToReplyRequest.recognizedText)
              system.log.info("intent: {}", intent)
              val operationPerformed: Future[Reply] =
                buildTakeMortrageScript.ask(
                  TakeMortrage.WhatToReply(intent = intent, _)
                )
              onSuccess(operationPerformed) { response =>
                response match {
                  case response @ ToSmallInitialSumReply(_, part) =>
                    complete(
                      StatusCodes.OK,
                      WhatToReplyResponse(textToReply = s"${response.textToReply} $part")
                    )
                  case response: Reply =>
                    complete(
                      StatusCodes.OK,
                      WhatToReplyResponse(textToReply = response.textToReply)
                    )
                }
              }
            }
          }
        }
      }

    lazy val whatToReplyRouteV2: Route =
      pathPrefix("api" / "v2" / "what-to-reply") {
        post {
          entity(as[WhatToReplyRequest]) { whatToReplyRequest =>
            {
              system.log.info("request: {}", whatToReplyRequest)
              val intent = Intent(whatToReplyRequest.recognizedText)
              system.log.info("intent: {}", intent)
              val persistIntent
                : Future[akka.pattern.StatusReply[PersistScript.UserSaidEventDone.type]] =
                buildPersistScript.ask(
                  PersistScript.UserSaidCommand(intent = intent, _)
                )
              val operationPerformed: Future[Reply] =
                buildPersistScript.ask(
                  PersistScript.WhatToReply(_)
                )
              onSuccess(operationPerformed) { response =>
                response match {
                  case response @ ToSmallInitialSumReply(_, part) =>
                    complete(
                      StatusCodes.OK,
                      WhatToReplyResponse(textToReply = s"${response.textToReply} $part")
                    )
                  case response: Reply =>
                    complete(
                      StatusCodes.OK,
                      WhatToReplyResponse(textToReply = response.textToReply)
                    )
                }
              }
            }
          }
        }
      }

    lazy val routes = whatToReplyRoute ~ whatToReplyRouteV2
  }
}
