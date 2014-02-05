package org.goldratio.echo

/**
 * Created by GoldRatio on 2/5/14.
 */

import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil

class EchoClientHandler extends ChannelHandlerAdapter {

  override def channelActive(ctx: ChannelHandlerContext) = {
    ctx.write(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8))
    //保证数据传输到server
    ctx.flush()
  }

  override def channelRead(ctx: ChannelHandlerContext , msg: Object ) = {
    ctx.write(msg)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext ) = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext , cause: Throwable ) {
    ctx.close();
  }

}

class EchoClient(host: String, port: Int) {

  def start() = {
    val group = new NioEventLoopGroup()
    try {
      val b = new Bootstrap()
      b.group(group)
        .channel(classOf[NioSocketChannel])
        .remoteAddress(host, port)
        .handler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel) = {
            ch.pipeline().addLast(new EchoClientHandler())
          }
        }
      )
      val f = b.connect().sync()
      System.out.println(classOf[EchoServer].getName() + "started and listen on " + f.channel().localAddress())
      f.channel().closeFuture().sync()
    }
    finally {
      group.shutdownGracefully().sync()
    }
  }
}

object EchoClient{
  def main(args: Array[String]): Unit = {
    val (host, port) = {
      if(args.length < 2) {
        ("localhost", 8080)
      }
      else {
        (args(0), Integer.parseInt(args(1)))
      }
    }
    System.out.print("start server")
    new EchoClient(host, port).start()
  }
}
