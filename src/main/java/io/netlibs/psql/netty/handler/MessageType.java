package io.netlibs.psql.netty.handler;

import lombok.Getter;

public enum MessageType
{

  NoticeResponse((byte) 'N'),

  ErrorResponse((byte) 'E'),

  AuthRequest((byte) 'R'),

  ParameterStatus((byte) 'S'),

  BackendKeyData((byte) 'K'),

  ReadyForQuery((byte) 'Z'),

  CommandComplete((byte) 'C'),

  DataRow((byte) 'D'),

  CopyBothResponse((byte) 'W'),

  CopyData((byte) 'd'),

  CopyDone((byte) 'c'),

  RowDescription((byte) 'T'),

  NotificationResponse((byte) 'A'),

  ;

  @Getter
  private byte type;

  MessageType(final byte type)
  {
    this.type = type;
  }

  public static MessageType getType(final byte type)
  {

    for (final MessageType t : MessageType.values())
    {
      if (t.type == type)
      {
        return t;
      }
    }

    throw new RuntimeException(String.format("Unknown message type '%s'", type));

  }

}
