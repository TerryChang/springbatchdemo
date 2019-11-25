package com.terry.springbatchdemo.config;

import com.terry.springbatchdemo.entity.ShoppingCart;
import com.terry.springbatchdemo.vo.LineResultVO;
import com.terry.springbatchdemo.vo.ShoppingCartVO;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

public class CustomShoppingCartJpaItemWriter extends JpaItemWriter<ShoppingCart> {
    private EntityManagerFactory entityManagerFactory;
    private DataShareBean dataShareBean;

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        super.setEntityManagerFactory(entityManagerFactory);
    }

    public DataShareBean getDataShareBean() {
        return dataShareBean;
    }

    public void setDataShareBean(DataShareBean dataShareBean) {
        this.dataShareBean = dataShareBean;
    }

    @Override
    public void write(List<? extends ShoppingCart> items) {
        EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
        if (entityManager == null) {
            throw new DataAccessResourceFailureException("Unable to obtain a transactional EntityManager");
        }
        doWrite(entityManager, items);
        try {
            entityManager.flush();
        } catch (Exception e) {
            // DB 작업 예외에 대한 기록을 하기 위해 작업을 해주어야 한다
        }

    }
}
