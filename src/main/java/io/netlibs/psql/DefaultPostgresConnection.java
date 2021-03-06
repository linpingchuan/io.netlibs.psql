package io.netlibs.psql;

import io.netlibs.psql.netty.handler.PostgreSQLClientNegotiation;
import io.netlibs.psql.netty.handler.PostgreSQLDecoder;
import io.netlibs.psql.netty.handler.PostgreSQLEncoder;
import io.netlibs.psql.netty.handler.PostgreSQLHandshakeCompleteEvent;
import io.netlibs.psql.netty.handler.PostgreSQLReplicationCopyDataCodec;
import io.netlibs.psql.replication.ReplicationPacketHandler;
import io.netlibs.psql.wire.AuthenticationOk;
import io.netlibs.psql.wire.AuthenticationUnknown;
import io.netlibs.psql.wire.BackendKeyData;
import io.netlibs.psql.wire.CommandComplete;
import io.netlibs.psql.wire.CopyBothResponse;
import io.netlibs.psql.wire.CopyData;
import io.netlibs.psql.wire.CopyDone;
import io.netlibs.psql.wire.DataRow;
import io.netlibs.psql.wire.EmptyQueryResponse;
import io.netlibs.psql.wire.ErrorResponse;
import io.netlibs.psql.wire.Execute;
import io.netlibs.psql.wire.NoticeResponse;
import io.netlibs.psql.wire.NotificationResponse;
import io.netlibs.psql.wire.ParameterStatus;
import io.netlibs.psql.wire.PostgreSQLPacket;
import io.netlibs.psql.wire.PostgreSQLPacketVisitor;
import io.netlibs.psql.wire.Query;
import io.netlibs.psql.wire.ReadyForQuery;
import io.netlibs.psql.wire.RowDescription;
import io.netlibs.psql.wire.StartupMessage;
import io.netlibs.psql.wire.UnknownMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * A PostgreSQL protocol connection. Can be used for replication and SQL connection.
 * 
 * If an error occurs, the connection is terminated. it doens't reconnect. use an SqlClient for higher level logic.
 * 
 * @author theo
 *
 */

@Slf4j
class DefaultPostgresConnection extends AbstractConnection implements PostgresConnection
{

  private PostgresConnectionListener clistener;

  DefaultPostgresConnection(PostgresConnectionBuilder b)
  {
    super(b);
    this.clistener = b.listener;
  }

  public <T extends QueryListener> T query(String query, T listener, CopyDataHandle handle)
  {

    if (!this.handshakePromise.isSuccess())
    {
      throw new IllegalStateException();
    }
    if (this.listener != null)
    {
      throw new IllegalStateException("listener already active");
    }

    this.listener = listener;

    if (handle != null)
    {
      Channel channel = handshakePromise.getNow();
      channel.pipeline().addBefore("handler", "copyhandle", new PostgreSQLReplicationCopyDataCodec());
      channel.pipeline().addLast(new ReplicationPacketHandler(handle));
      System.err.println(channel.pipeline());
    }

    handshakePromise.getNow().writeAndFlush(new Query(query));

    return listener;

  }

  /**
   * handler which dispatches events from the netty thread to the user thread.
   */

  private final class Handler extends SimpleChannelInboundHandler<PostgreSQLPacket> implements PostgreSQLPacketVisitor<Void>
  {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PostgreSQLPacket msg) throws Exception
    {
      msg.apply(this);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {

      if (evt instanceof PostgreSQLHandshakeCompleteEvent)
      {
        log.debug("handshake complete");
        // no need to leave it laying around.
        ctx.channel().pipeline().remove(PostgreSQLClientNegotiation.class);
        handshakePromise.setSuccess(ctx.channel());
        if (clistener != null)
        {
          clistener.ready();
        }
      }

    }

    // all of our handlers ...

    @Override
    public Void visitAuthenticationOk(AuthenticationOk pkt)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Void visitAuthenticationUnknown(AuthenticationUnknown pkt)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Void visitBackendKeyData(BackendKeyData data)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Void visitParameterStatus(ParameterStatus p)
    {
      if (clistener != null)
      {
        clistener.param(p.getKey(), p.getValue());
      }
      return null;
    }

    @Override
    public Void visitStartupMessage(StartupMessage startupMessage)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Void visitQuery(Query query)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Void visitExecute(Execute execute)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Void visitNotificationResponse(NotificationResponse e)
    {
      if (clistener != null)
      {
        clistener.notification(e);
      }
      return null;
    }

    @Override
    public Void visitUnknownMessage(UnknownMessage unknownMessage)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Void visitCopyBothResponse(CopyBothResponse cmd)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Void visitCopyData(CopyData copyData)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Void visitCopyDone(CopyDone copyDone)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Void visitCommandComplete(CommandComplete cmd)
    {
      if (listener != null)
      {
        listener.commandComplete(cmd);
      }
      return null;
    }

    @Override
    public Void visitDataRow(DataRow row)
    {
      if (listener != null)
      {
        listener.row(row);
      }
      return null;
    }

    @Override
    public Void visitErrorResponse(ErrorResponse errorResponse)
    {
      if (listener != null)
      {
        listener.error(errorResponse);
      }
      return null;
    }

    @Override
    public Void visitNoticeResponse(NoticeResponse notice)
    {
      if (listener != null)
      {
        listener.notice(notice);
      }
      return null;
    }

    @Override
    public Void visitReadyForQuery(ReadyForQuery msg)
    {
      if (listener != null)
      {
        listener.readyForQuery(msg);
      }
      listener = null;
      return null;
    }

    @Override
    public Void visitRowDescription(RowDescription rowDescription)
    {
      if (listener != null)
      {
        listener.description(rowDescription);
      }
      return null;
    }

    @Override
    public Void visitEmptyQueryResponse(EmptyQueryResponse empty)
    {
      if (listener != null)
      {
        listener.emptyResponse(empty);
      }
      return null;
    }

  }

  void connect(String host, int port)
  {

    final Bootstrap b = new Bootstrap();

    b.group(group)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          public void initChannel(final SocketChannel ch) throws Exception
          {
            final ChannelPipeline p = ch.pipeline();
            p.addLast(new PostgreSQLDecoder(), new PostgreSQLEncoder());
            p.addLast(new PostgreSQLClientNegotiation(DefaultPostgresConnection.this.params));
            p.addLast("handler", new Handler());
          }
        });

    // attempt to connect.
    this.connectFuture = b.connect(host, port);

  }

}
