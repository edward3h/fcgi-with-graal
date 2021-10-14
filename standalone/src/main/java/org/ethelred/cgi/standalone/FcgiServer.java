package org.ethelred.cgi.standalone;

import com.github.fmjsjx.libnetty.fastcgi.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.ethelred.cgi.CgiHandler;
import org.ethelred.cgi.CgiRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class FcgiServer {
    private final NioEventLoopGroup group;
    private volatile CgiHandler cgiHandler = CgiHandler.NOT_IMPLEMENTED;

    // see https://github.com/fmjsjx/libnetty/tree/v2.x/libnetty-fastcgi
    public FcgiServer(int fcgiPort) {
        var encoder = new FcgiMessageEncoder();
        this.group = new NioEventLoopGroup();
        var b = new ServerBootstrap().group(group)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 512)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(encoder)
                                .addLast(new FcgiMessageDecoder())
                                .addLast(new Handler());
                    }
                });
        try {
            b.bind(fcgiPort).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Future<?> shutdown() {
        return group.shutdownGracefully();
    }

    public void start(CgiHandler handler) {
        this.cgiHandler = handler;
    }

    class Handler extends SimpleChannelInboundHandler<FcgiMessage>{

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FcgiMessage msg) throws Exception {
            if (msg instanceof FcgiRequest) {
                var r = (FcgiRequest) msg;
                if (r.beginRequest().role() == FcgiRole.RESPONDER) {
                    var env = r.params()
                            .pairs()
                            .stream()
                            .collect(Collectors.toMap(
                                    FcgiNameValuePairs.NameValuePair::name,
                                    FcgiNameValuePairs.NameValuePair::value
                            ));
                    var in = new ByteBufInputStream(r.stdin().content());
                    var out = new ByteBufOutputStream(Unpooled.buffer());
                    var cgiRequest = new StandaloneCgiRequest(env, in, out);
                    cgiHandler.handleRequest(cgiRequest);
                    var response = new FcgiResponse(r.protocolVersion(), r.requestId(), 0, out.buffer());
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                }
                else {
                    throw new IllegalArgumentException(String.valueOf(r.beginRequest().role()));
                }
            }
            else {
                throw new IllegalArgumentException(String.valueOf(msg));
            }
        }
    }

    static class StandaloneCgiRequest implements CgiRequest {
        private final Map<String, String> env;
        private final InputStream in;
        private final OutputStream out;

        StandaloneCgiRequest(Map<String, String> env, InputStream in, OutputStream out) {
            this.env = env;
            this.in = in;
            this.out = out;
        }

        @Nonnull
        @Override
        public Map<String, String> getEnv() {
            return env;
        }

        @CheckForNull
        @Override
        public InputStream getBody() {
            return in;
        }

        @Nonnull
        @Override
        public OutputStream getOutput() {
            return out;
        }
    }
}
