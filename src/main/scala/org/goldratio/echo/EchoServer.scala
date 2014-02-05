package org.goldratio.echo

/**
 * Created by GoldRatio on 2/5/14.
 */
import java.net.InetSocketAddress

import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, ChannelHandlerAdapter, ChannelInitializer}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.buffer.Unpooled

class EchoServerHandler extends ChannelHandlerAdapter {

  override def channelRead(ctx: ChannelHandlerContext, msg: Object) {
    System.out.println("Server received: " + msg);
    ctx.write(msg)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext ) {
    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
      .addListener(ChannelFutureListener.CLOSE)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable ) {
    cause.printStackTrace()
    ctx.close()
  }
}

class EchoServer(port: Int) {

  def start() = {
    val group = new NioEventLoopGroup()
    try {
      val b = new ServerBootstrap()
      b.group(group)
        .channel(classOf[NioServerSocketChannel])
        .localAddress(new InetSocketAddress(port))
        .childHandler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel) = {
            ch.pipeline().addLast(new EchoServerHandler());
          }
        }
      )
      val f = b.bind().sync()
      System.out.println(classOf[EchoServer].getName() + "started and listen on " + f.channel().localAddress())
      f.channel().closeFuture().sync()
    }
    finally {
      group.shutdownGracefully().sync()
    }
  }
}

object EchoServer {
  def main(args: Array[String]): Unit = {
    val port = {
      if(args.length < 1) {
        8080
      }
      else {
        Integer.parseInt(args(0))
      }
    }
    System.out.print("start server")
    new EchoServer(port).start()
  }
}
