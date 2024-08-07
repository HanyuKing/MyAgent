package test;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

/**
 * 检测方法的执行时间
 */
public class MyTransformer implements ClassFileTransformer {

    final static String prefix = "\nlong startTime = System.currentTimeMillis();\n";
    final static String postfix = "\nlong endTime = System.currentTimeMillis();\n";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer){
        //java自带的方法不进行处理
        if(className.startsWith("java") || className.startsWith("sun")){
            return null;
        }
        className = className.replace("/", ".");
        CtClass ctclass = null;
        try {
            ctclass = ClassPool.getDefault().get(className);// 使用全称,用于取得字节码类<使用javassist>
            for(CtMethod ctMethod : ctclass.getDeclaredMethods()){
                String methodName = ctMethod.getName();
                String newMethodName = methodName + "$old";// 新定义一个方法叫做比如sayHello$old
                ctMethod.setName(newMethodName);// 将原来的方法名字修改

                // 创建新的方法，复制原来的方法，名字为原来的名字
                CtMethod newMethod = CtNewMethod.copy(ctMethod, methodName, ctclass, null);

                // 构建新的方法体
                StringBuilder bodyStr = new StringBuilder();
                bodyStr.append("{");
                bodyStr.append("System.out.println(\"==============Enter Method: " + className + "." + methodName + " ==============\");");
                bodyStr.append(prefix);
                bodyStr.append(newMethodName + "($$);\n");// 调用原有代码，类似于method();($$)表示所有的参数
                bodyStr.append(postfix);
                bodyStr.append("System.out.println(\"==============Exit Method: " + className + "." + methodName + " Cost:\" +(endTime - startTime) +\"ms " + "===\");");
                bodyStr.append("}");

                newMethod.setBody(bodyStr.toString());// 替换新方法
                ctclass.addMethod(newMethod);// 增加新方法
            }
            return ctclass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
