//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package io.dddspring.common;

import io.dddspring.common.domain.model.DomainEventPublisher;
import io.dddspring.common.spring.SpringHibernateSessionProvider;
//import junit.framework.TestCase;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

//@ExtendWith(SpringExtension.class)
//@Import(CommonTestCase.TestConfig.class)

@SpringJUnitConfig(locations = "classpath:applicationContext-common.xml")
public abstract class CommonTestCase {

//    @TestConfiguration
//    @ImportResource(locations = {"classpath*:applicationContext-common.xml"})
//    public static class TestConfig {
//
//    }

    private String fName;
    @Autowired
    protected ApplicationContext applicationContext;
    protected SpringHibernateSessionProvider sessionProvider;
    private Transaction transaction;

    public CommonTestCase() {
        this.fName = null;
    }

    public CommonTestCase(String name) {
        this.fName = name;
    }

    protected Session session() {
        Session session = this.sessionProvider.session();

        return session;

    }
    private void loadApplicationContext(){
//        this.applicationContext = new ClassPathXmlApplicationContext("applicationContext-common.xml");

        String[] beans =  applicationContext.getBeanDefinitionNames();

        System.out.println("\n\n");
        for (String bean : beans)
        {
            System.out.println("beans-CommonTestCase::"+bean + " of Type :: " + applicationContext.getBean(bean).getClass());
        }

        this.sessionProvider = (SpringHibernateSessionProvider) this.applicationContext.getBean("sessionProvider");

        this.setTransaction(this.session().beginTransaction());
        System.out.println(">>>>>>>>>>>>>>>>>>>> (start)" + this.getName());
    }

    protected Transaction transaction() {
        return this.transaction;
    }
    @BeforeEach
    public void setUp() throws Exception {

        DomainEventPublisher.instance().reset();

        this.loadApplicationContext();

    }

    @AfterEach
    public void tearDown() throws Exception {

//        this.transaction().rollback();
      this.transaction().commit();//需要在数据库中核对结果时开启
        this.setTransaction(null);

        this.session().clear();

        System.out.println("<<<<<<<<<<<<<<<<<<<< (end)");

//        super.tearDown();
    }

    public String getName() {
        return this.fName;
    }
    private void setTransaction(Transaction aTransaction) {
        this.transaction = aTransaction;
    }
}
