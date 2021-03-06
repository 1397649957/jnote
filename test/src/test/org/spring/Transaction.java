package org.spring;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.Test;
import org.spring.depen.CallCountingTransactionManager;
import org.spring.depen.SerializationTestUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;


/**
 * @author ☞ 🏀 huqingfeng
 * @date 2019-07-12
 */
public class Transaction {

    private static DataSource dataSource;

    static {
        MysqlDataSource dataSourceMysql = new MysqlDataSource();
        dataSourceMysql.setUrl("jdbc:mysql://182.254.131.140/pes_jd_sub_01?" +
                "serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true");
        dataSourceMysql.setUser("wangmeng");
        dataSourceMysql.setPassword("Ywc201405");
        dataSource =  dataSourceMysql;
    }
    @Test
    public void test1() throws Exception{
        TestBean1 tb = new TestBean1();
        DataSourceTransactionManager ptm = new DataSourceTransactionManager(dataSource);
        AnnotationTransactionAttributeSource tas = new AnnotationTransactionAttributeSource();
        TransactionInterceptor ti = new TransactionInterceptor(ptm, tas);

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(ITestBean1.class);
        proxyFactory.addAdvice(ti);
        proxyFactory.setTarget(tb);
        ITestBean1 proxy = (ITestBean1) proxyFactory.getProxy();
        proxy.getAge();
//        assertThat(ptm.commits).isEqualTo(1);

//        ITestBean1 serializedProxy = (ITestBean1) SerializationTestUtils.serializeAndDeserialize(proxy);
//        serializedProxy.getAge();
//        Advised advised = (Advised) serializedProxy;
//        TransactionInterceptor serializedTi = (TransactionInterceptor) advised.getAdvisors()[0].getAdvice();
//        CallCountingTransactionManager serializedPtm =
//                (CallCountingTransactionManager) serializedTi.getTransactionManager();
//        assertThat(serializedPtm.commits).isEqualTo(2);
    }

    interface ITestBean1 {

        int getAge() throws Exception;

        void setAge(int age);

        String getName();

        void setName(String name);
    }

    static class TestBean1 implements ITestBean1, Serializable {

        private String name;

        private int age;

        public TestBean1() {
        }

        public TestBean1(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }



        @Override
        @Transactional(rollbackFor = Exception.class)
        public int getAge()  throws Exception{
            // 手动失败
            Connection connection = DataSourceUtils.getConnection(dataSource);
            PreparedStatement preparedStatement = connection.prepareStatement("insert into test(id,name) value (null , '888888')");
            preparedStatement.execute();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return age;
        }

        @Override
        public void setAge(int age) {
            this.age = age;
        }
    }
}
