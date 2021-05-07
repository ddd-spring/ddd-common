package io.dddspring.common.grpc;

import com.google.common.base.Preconditions;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.util.MutableHandlerRegistry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class GrpcServerJunit5 {
    private ManagedChannel channel;
    private Server server;
    private String serverName;
    private MutableHandlerRegistry serviceRegistry;
    private boolean useDirectExecutor;

    public static GrpcServerJunit5 grpcServerJunit5;

    public GrpcServerJunit5() {
    }

    public final GrpcServerJunit5 directExecutor() {
        Preconditions.checkState(this.serverName == null, "directExecutor() can only be called at the rule instantiation");
        this.useDirectExecutor = true;
        try {
            this.start();
        }catch (Throwable e){

            e.printStackTrace();
        }
        return this;
    }

    public static GrpcServerJunit5 getGrpcServer() {
        if(grpcServerJunit5!=null){
               return grpcServerJunit5;
        }
        else
            {
                GrpcServerJunit5.grpcServerJunit5= new GrpcServerJunit5().directExecutor();
             return GrpcServerJunit5.grpcServerJunit5;
        }
    }

    public final ManagedChannel getChannel() {
        return this.channel;
    }

    public final Server getServer() {
        return this.server;
    }

    public final String getServerName() {
        return this.serverName;
    }

    public final MutableHandlerRegistry getServiceRegistry() {
        return this.serviceRegistry;
    }
    protected void finalize()throws Throwable{
        try {
        this.clear();
        } catch (Throwable e){}
    }

    public void clear() {
//        this.serverName = null;
//        this.serviceRegistry = null;
//        this.channel.shutdown();
//        this.server.shutdown();



        try {
            if(this.channel.awaitTermination(1L, TimeUnit.MINUTES)
               && this.server.awaitTermination(1L, TimeUnit.MINUTES)){

//                this.channel.shutdown();
//                this.server.shutdown();
            }

        } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(var5);
        } finally {
//            this.channel.shutdownNow();
//            this.server.shutdownNow();

        }

    }

    public void start() throws Throwable {
        this.serverName = UUID.randomUUID().toString();
        this.serviceRegistry = new MutableHandlerRegistry();
        InProcessServerBuilder serverBuilder = (InProcessServerBuilder)InProcessServerBuilder.forName(this.serverName).fallbackHandlerRegistry(this.serviceRegistry);
        if (this.useDirectExecutor) {
            serverBuilder.directExecutor();
        }

        this.server = serverBuilder.build().start();
        InProcessChannelBuilder channelBuilder = InProcessChannelBuilder.forName(this.serverName);
        if (this.useDirectExecutor) {
            channelBuilder.directExecutor();
        }
        System.out.println("this.serverName-start::"+this.serverName);
        this.channel = channelBuilder.build();
        System.out.println("this.serverName-end::"+this.serverName);
    }
}
