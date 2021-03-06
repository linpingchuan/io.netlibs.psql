package io.netlibs.psql.wire;

import lombok.Value;

@Value
public class UnknownMessage implements PostgreSQLPacket
{
  
  private final int type;

  @Override
  public <T> T apply(PostgreSQLPacketVisitor<T> visitor)
  {
    return visitor.visitUnknownMessage(this);
  }

}
