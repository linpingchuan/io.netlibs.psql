package io.netlibs.psql.wire;

public interface PostgreSQLPacketVisitor<T>
{

  T visitAuthenticationOk(AuthenticationOk pkt);

  T visitAuthenticationUnknown(AuthenticationUnknown pkt);

  T visitBackendKeyData(BackendKeyData data);

  T visitCommandComplete(CommandComplete cmd);

  T visitUnknownMessage(UnknownMessage unknownMessage);

  T visitCopyBothResponse(CopyBothResponse cmd);

  T visitCopyData(CopyData copyData);

  T visitCopyDone(CopyDone copyDone);

  T visitDataRow(DataRow dataRow);

  T visitErrorResponse(ErrorResponse errorResponse);

  T visitNoticeResponse(NoticeResponse noticeResponse);

  T visitParameterStatus(ParameterStatus parameterStatus);

  T visitReadyForQuery(ReadyForQuery readyForQuery);

  T visitRowDescription(RowDescription e);

  T visitStartupMessage(StartupMessage e);

  T visitQuery(Query e);

  T visitExecute(Execute e);

  T visitEmptyQueryResponse(EmptyQueryResponse e);

  T visitNotificationResponse(NotificationResponse e);

}
