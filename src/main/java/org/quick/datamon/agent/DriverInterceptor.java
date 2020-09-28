package org.quick.datamon.agent;

import java.sql.Connection;
import java.util.concurrent.Callable;

import com.p6spy.engine.spy.P6SpyDriver;
import org.eclipse.jdt.annotation.Nullable;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

public class DriverInterceptor {
    public @Nullable Connection connect(final String url, final java.util.Properties info,
            @SuperCall final Callable<Connection> originalConnectionCreator) throws Exception {
        return P6SpyDriver.getInstance().wrapConnection(url, info, originalConnectionCreator);
    }
}