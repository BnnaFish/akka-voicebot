package Domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import Domain.Entities._

trait JsonFormats extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val WhatToReplyRequestFormat = jsonFormat3(WhatToReplyRequest)
  implicit val WhatToReplyResponseFormat = jsonFormat1(WhatToReplyResponse)
}

trait JsonSerializable
