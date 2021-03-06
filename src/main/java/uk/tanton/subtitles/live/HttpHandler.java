package uk.tanton.subtitles.live;


import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class HttpHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LogManager.getLogger(HttpHandler.class);
    private static final Gson GSON = new Gson();

    private final SubtitleCache subtitleCache;

    public HttpHandler(SubtitleCache subtitleCache) {
        this.subtitleCache = subtitleCache;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            final FullHttpRequest request = (FullHttpRequest) msg;

            FullHttpResponse response = null;
            try {
                response = generateResponse(request);
            } catch (Exception e) {
                LOG.error("EXCEPTION", e);
                response = buildDefaultResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, e.getMessage().length());
            }

            ctx.writeAndFlush(response);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private FullHttpResponse generateResponse(FullHttpRequest request) throws IOException {
        String uri = request.getUri();
        LOG.info(String.format("Access: %s", uri));
        FullHttpResponse response;
        String responseMessage = "";
        String contentType = "";
        if (request.getMethod().equals(HttpMethod.POST)) {

//                POST
            final String requestMsg = request.content().toString(CharsetUtil.UTF_8);
            subtitleCache.setText(requestMsg);
            response = buildDefaultResponse(HttpResponseStatus.OK, null);
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, 0);
        } else {

//                Other
            contentType = "text/html";

            if (uri.startsWith("/admin")) {
//                    admin
                InputStream resourceAsStream = this.getClass().getResourceAsStream("/admin.html");
                responseMessage = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);

            } else if (uri.startsWith("/view")) {
//                    view
                InputStream resourceAsStream = this.getClass().getResourceAsStream("/view.html");
                responseMessage = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
                String bgcolour = "0f0";
                String textboxo = "0.9";

                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri, StandardCharsets.UTF_8);

                List<String> bgcolourList = queryStringDecoder.parameters().get("bgcolour");
                if (bgcolourList != null && bgcolourList.size() > 0) {
                    bgcolour = bgcolourList.get(0);
                }

                List<String> textboxoList = queryStringDecoder.parameters().get("textboxo");
                if (textboxoList != null && textboxoList.size() > 0) {
                    textboxo = textboxoList.get(0);
                }

                responseMessage = responseMessage.replace("#0f0", "#" + bgcolour);
                responseMessage = responseMessage.replace("0.9", textboxo);

            } else {
//                must be api
                contentType = "text/plain";
                responseMessage = subtitleCache.getText();
                if (responseMessage == null) {
                    responseMessage = "";
                }

            }

            response = buildDefaultResponse(HttpResponseStatus.OK, responseMessage);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseMessage.length());
        }

        return response;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("An exception was caught", cause);
        ctx.writeAndFlush(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                copiedBuffer(cause.getMessage().getBytes())
        ));
    }

    private FullHttpResponse buildDefaultResponse(final HttpResponseStatus status, final String message) {
        if (message != null) {
            return new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    status,
                    copiedBuffer(message.getBytes())
            );
        } else {
            return new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    status
            );
        }
    }
}
