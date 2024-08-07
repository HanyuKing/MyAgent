import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;

public class MyAgent {

    /**
     * JVM 首先尝试在代理类上调用以下方法
     * @param agentArgs
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("MyAgent#premain start");

        AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
            return builder
                    .method(ElementMatchers.any()) // 拦截任意方法
                    .intercept(MethodDelegation.to(MethodCostTime.class)); // 委托
        };

        AgentBuilder.Listener listener = new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
                //System.out.println("AgentBuilder.Listener.onDiscovery");
            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
                //System.out.println("AgentBuilder.Listener.onTransformation");
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {
                //System.out.println("AgentBuilder.Listener.onIgnored");
            }

            @Override
            public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
                //System.out.println("AgentBuilder.Listener.onError");
            }

            @Override
            public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
                //System.out.println("AgentBuilder.Listener.onComplete");
            }

        };

        new AgentBuilder
                .Default()
                .type(ElementMatchers.nameStartsWith("bytebuddy")) // 指定需要拦截的类
                .transform(transformer)
                .with(listener)
                .installOn(inst);

        System.out.println("MyAgent#premain end");
    }

    /**
     * 如果代理类没有实现上面的方法，那么 JVM 将尝试调用该方法
     * @param agentArgs
     */
    public static void premain(String agentArgs) {
        System.out.println("premain方法2" + agentArgs);
    }

}