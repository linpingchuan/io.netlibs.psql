package io.netlibs.psql;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class AbstractConnectionBuilder<T>
{

  private static final EventLoopGroup DEFAULT_EVENT_LOOP_GROUP = new NioEventLoopGroup();

  public EventLoopGroup group = DEFAULT_EVENT_LOOP_GROUP;
  public String username;
  public String database;

  public T group(EventLoopGroup group)
  {
    this.group = group;
    return (T) this;
  }

  public T username(String username)
  {
    this.username = username;
    return (T) this;
  }

  public T database(String database)
  {
    this.database = database;
    return (T) this;
  }

}
