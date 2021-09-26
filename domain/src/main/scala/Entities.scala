package Domain

object Entities {
  type RecognizedText = String
  type TextToReply = String

  type SessionId = Long
  type UserId = Long

  final case class WhatToReplyRequest(
    sessionId: SessionId,
    userId: UserId,
    recognizedText: RecognizedText
  )

  final case class WhatToReplyResponse(textToReply: TextToReply)
}
