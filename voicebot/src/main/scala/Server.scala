package Voicebot

import akka.actor.typed.{ActorRef, Behavior, PostStop}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http
import akka.persistence.typed.PersistenceId

import scala.concurrent.Future
import scala.util.{Failure, Success}

import Routes.Routes
import Scripts.{PersistScript, TakeMortgage}

object Server {
  sealed trait Message
  private final case class StartFailed(cause: Throwable) extends Message
  private final case class Started(binding: ServerBinding) extends Message
  case object Stop extends Message

  def apply(host: String, port: Int): Behavior[Message] = Behaviors.setup { ctx =>
    implicit val system = ctx.system

    val buildTakeMortgageScript = ctx.spawn(TakeMortgage(), "TakeMortgage")
    val buildPersistScript =
      ctx.spawn(PersistScript("entityId", PersistenceId.ofUniqueId("abc")), "PersistScript")
    val routes = new Routes(buildTakeMortgageScript, buildPersistScript)

    val serverBinding: Future[Http.ServerBinding] =
      Http().newServerAt(host, port).bind(routes.routes)
    ctx.pipeToSelf(serverBinding) {
      case Success(binding) => Started(binding)
      case Failure(ex)      => StartFailed(ex)
    }

    def running(binding: ServerBinding): Behavior[Message] =
      Behaviors
        .receiveMessagePartial[Message] { case Stop =>
          ctx.log.info(
            "Stopping server http://{}:{}/",
            binding.localAddress.getHostString,
            binding.localAddress.getPort
          )
          Behaviors.stopped
        }
        .receiveSignal { case (_, PostStop) =>
          binding.unbind()
          Behaviors.same
        }

    def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
      Behaviors.receiveMessage[Message] {
        case StartFailed(cause) =>
          throw new RuntimeException("Server failed to start", cause)
        case Started(binding) =>
          ctx.log.info(
            "Server online at http://{}:{}/",
            binding.localAddress.getHostString,
            binding.localAddress.getPort
          )
          if (wasStopped) ctx.self ! Stop
          running(binding)
        case Stop =>
          // we got a stop message but haven't completed starting yet,
          // we cannot stop until starting has completed
          starting(wasStopped = true)
      }

    starting(wasStopped = false)
  }
}
