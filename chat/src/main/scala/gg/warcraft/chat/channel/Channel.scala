package gg.warcraft.chat.channel

import java.util.UUID

import gg.warcraft.chat.profile.ChatProfileService
import gg.warcraft.chat.{Message, MessageAdapter}
import gg.warcraft.monolith.api.core.command.{CommandHandler, CommandSender}
import gg.warcraft.monolith.api.util.ColorCode

trait Channel extends CommandHandler {
  val name: String
  val aliases: Set[String]
  val shortcut: Option[String]
  val color: ColorCode
  val formatString: String

  protected implicit val messageAdapter: MessageAdapter
  protected implicit val profileService: ChatProfileService

  def broadcast(
      sender: CommandSender,
      text: String,
      recipients: Iterable[UUID]
  ): Unit = {
    val message = sender match {
      case CommandSender(_, Some(playerId)) =>
        val profile = profileService.profiles(playerId)
        Message(this, profile, text)
      case _ => Message.server(text)
    }

    recipients.foreach(messageAdapter.send(message, _))
    if (recipients.size == 1 && sender.isPlayer) {
      messageAdapter.send(Message.mute, sender.playerId.get)
    }

    // NOTE option to log message here
  }

  def makeHome(playerId: UUID): Boolean = {
    val profile = profileService.profiles(playerId)
    if (profile.home != name) {
      profileService.saveProfile(profile.copy(home = name))
      // NOTE option to send join message here
      true
    } else false
  }
}