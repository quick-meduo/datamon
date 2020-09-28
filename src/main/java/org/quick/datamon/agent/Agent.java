package org.quick.datamon.agent;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.sql.Driver;

import com.p6spy.engine.spy.P6SpyDriver;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.eclipse.jdt.annotation.Nullable;

public class Agent {
    protected static final String PREFIX = "[org-quick-datamon-agent]";
    private static boolean loaded = false;

    public static void premain(final String agentArgs, final Instrumentation inst) throws IOException {
        loadAgent(agentArgs, inst);
    }

    public static void agentmain(final String agentArgs, final Instrumentation inst) {
        loadAgent(agentArgs, inst);
    }

    private static void loadAgent(final String agentArgs, final Instrumentation inst) {
        System.out.print(PREFIX + " Loading...");
        new AgentBuilder.Default()
        .with(new AgentBuilder.Listener.Adapter() {
            @Override
            public void onError(final String typeName, final @Nullable ClassLoader classLoader,
                    final @Nullable JavaModule module,
                    final boolean loaded1, final Throwable throwable) {
                System.err.println(PREFIX + " ERROR " + typeName);
                throwable.printStackTrace(System.err);
            }

        })
        .type(ElementMatchers.isSubTypeOf(Driver.class).and(ElementMatchers.noneOf(P6SpyDriver.class)))
        .transform(new AgentBuilder.Transformer() {
            public Builder<?> transform(final Builder<?> builder, final TypeDescription typeDescription,
                    final @Nullable ClassLoader classLoader, final @Nullable JavaModule module) {
                System.out.println(PREFIX + " Transforming " + typeDescription + " for interception");
                return   builder
                        .method(named("connect"))
                        .intercept(MethodDelegation.withDefaultConfiguration()
                                .filter(ElementMatchers.isMethod().and(named("connect")))
                                .to(new DriverInterceptor()))
                ;
            }
        })
        .installOn(inst);
        loaded = true;

        try {
            Class.forName("com.p6spy.engine.spy.P6SpyDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("OK");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
