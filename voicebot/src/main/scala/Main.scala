package Voicebot

import akka.actor.typed.ActorSystem
import Server._

object Voicebot {

  def main(args: Array[String]): Unit = {
    import system.executionContext

    val system: ActorSystem[Server.Message] =
      ActorSystem(Server("localhost", 8080), "BuildEchoServer")
  }
}
